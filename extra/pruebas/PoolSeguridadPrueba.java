import extra.config.ConexionBD;
import extra.config.UsuarioActual;
import extra.seguridad.CifradoDatos;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import javax.crypto.KeyGenerator;
import java.security.GeneralSecurityException;

/** Prueba de integración: ejecutar solo contra el clúster temporal del script. */
public class PoolSeguridadPrueba {
    private static void comprobar(boolean valor, String mensaje) {
        if (!valor) throw new AssertionError(mensaje);
    }
    private static String contexto(Connection c) throws SQLException {
        try (var ps = c.prepareStatement("SELECT current_setting('app.usuario', true)"); var rs = ps.executeQuery()) {
            rs.next(); String valor=rs.getString(1); return valor==null || valor.isEmpty()?valor:extra.seguridad.CifradoDatos.descifrar(valor,"auditoria.hist_usuario_app");
        }
    }
    private static int pid(Connection c) throws SQLException {
        try (var ps = c.prepareStatement("SELECT pg_backend_pid()"); var rs = ps.executeQuery()) {
            rs.next(); return rs.getInt(1);
        }
    }
    public static void main(String[] args) throws Exception {
        try {
            ConexionBD.iniciarPool();
            UsuarioActual.establecer("usuario_uno");
            int primero;
            try (var c = ConexionBD.obtenerConexion()) {
                primero = pid(c);
                comprobar("usuario_uno".equals(contexto(c)), "Falta usuario inicial");
                try (var s = c.createStatement()) {
                    s.execute("CREATE EXTENSION IF NOT EXISTS pgcrypto");
                    s.execute("CREATE TABLE prueba_pool (id integer)");
                }
                c.setAutoCommit(false);
                try (var s = c.createStatement()) { s.execute("INSERT INTO prueba_pool VALUES (1)"); }
                // Cierre con transacción pendiente: Hikari debe hacer rollback.
            }
            UsuarioActual.limpiar();
            try (var c = ConexionBD.obtenerConexion()) {
                comprobar(pid(c) == primero, "No se reutilizó la conexión física");
                comprobar("".equals(contexto(c)), "Se filtró el usuario anterior");
                comprobar(c.getAutoCommit(), "No se restauró autoCommit");
                try (var s = c.createStatement(); var rs = s.executeQuery("SELECT count(*) FROM prueba_pool")) {
                    rs.next(); comprobar(rs.getInt(1) == 0, "No se revirtió la transacción pendiente");
                }
                String hash1, hash2;
                try (var ps = c.prepareStatement("SELECT public.crypt(?, public.gen_salt('bf',12)), public.crypt(?, public.gen_salt('bf',12))")) {
                    ps.setString(1, "Prueba-segura-2026"); ps.setString(2, "Prueba-segura-2026");
                    try (var rs = ps.executeQuery()) { rs.next(); hash1=rs.getString(1); hash2=rs.getString(2); }
                }
                comprobar(!hash1.equals(hash2) && hash1.startsWith("$2a$12$"), "BCrypt no genera sal/costo correctos");
                try (var ps = c.prepareStatement("SELECT ? = public.crypt(?, ?), ? = public.crypt(?, ?)")) {
                    ps.setString(1,hash1); ps.setString(2,"Prueba-segura-2026"); ps.setString(3,hash1);
                    ps.setString(4,hash1); ps.setString(5,"incorrecta"); ps.setString(6,hash1);
                    try (var rs = ps.executeQuery()) { rs.next(); comprobar(rs.getBoolean(1) && !rs.getBoolean(2), "Validación BCrypt incorrecta"); }
                }
            }
            var executor = Executors.newFixedThreadPool(4);
            try {
                var jobs = new ArrayList<Callable<Boolean>>();
                for (int i=0;i<12;i++) {
                    final String usuario="usuario_"+i;
                    jobs.add(() -> {
                        UsuarioActual.establecer(usuario);
                        try (var c=ConexionBD.obtenerConexion()) { return usuario.equals(contexto(c)); }
                        finally { UsuarioActual.limpiar(); }
                    });
                }
                for (var future: executor.invokeAll(jobs)) comprobar(future.get(), "Contexto mezclado entre peticiones concurrentes");
            } finally { executor.shutdownNow(); }
            KeyGenerator generator=KeyGenerator.getInstance("AES"); generator.init(128);
            var clave=generator.generateKey(); var cifrador=new CifradoDatos(clave);
            String texto="Datos de prueba: ñ, á, 漢字", uno=cifrador.cifrar(texto), dos=cifrador.cifrar(texto);
            comprobar(uno.equals(dos), "ECB debe producir el mismo cifrado con la misma clave y texto");
            comprobar(texto.equals(new CifradoDatos(clave).descifrar(uno)), "Error al recuperar el texto con la misma clave");
            comprobar("".equals(cifrador.descifrar(cifrador.cifrar(""))), "Error con texto vacío");
            for (String incorrecto : List.of("ecb1:!", "ecb1:AA==", "v1:AAAA")) {
                try { cifrador.descifrar(incorrecto); throw new AssertionError("Se aceptó formato inválido"); }
                catch (GeneralSecurityException expected) { }
            }
            try {new CifradoDatos(new javax.crypto.spec.SecretKeySpec(new byte[32],"AES"));throw new AssertionError("Se aceptó clave de 256 bits");}
            catch(IllegalArgumentException expected) {}
            System.out.println("OK: reutilización del pool, rollback, autoCommit, auditoría aislada, concurrencia, compatibilidad de credenciales y AES-128-ECB.");
        } finally {
            UsuarioActual.limpiar();
            ConexionBD.cerrarPool();
            ConexionBD.cerrarPool();
        }
        try { ConexionBD.obtenerConexion(); throw new AssertionError("El pool cerrado volvió a abrirse"); }
        catch (SQLException expected) { comprobar("08003".equals(expected.getSQLState()), "Estado de cierre incorrecto"); }
        System.out.println("OK: cierre idempotente del pool y rechazo de nuevas conexiones tras apagarlo.");
    }
}
