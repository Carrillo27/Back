import extra.config.ConexionBD;
import extra.seguridad.CifradoDatos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Convierte una base sin cifrar al formato que exige la API. Se ejecuta UNA sola vez.
 * No forma parte del WAR.
 *
 *   (sin argumentos)  ensayo: hace todo y lo deshace
 *   --aplicar         guarda los cambios
 *   --generar-clave   imprime una clave AES-128 nueva y termina
 *
 * Usa las mismas variables que la API: PAPELERIA_DB_URL, PAPELERIA_DB_USER,
 * PAPELERIA_DB_PASSWORD (o el archivo privado) y PAPELERIA_AES_KEY_BASE64.
 */
public class MigrarCifrado {
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("--generar-clave")) {
            System.out.println(Base64.getEncoder().encodeToString(CifradoDatos.generarClave().getEncoded()));
            return;
        }
        boolean aplicar = args.length > 0 && args[0].equals("--aplicar");
        try (Connection c = ConexionBD.obtenerConexion()) {
            if (existeTabla(c, "seguridad_cifrado"))
                throw new IllegalStateException("La base ya tiene seguridad_cifrado: no se vuelve a migrar.");
            c.setAutoCommit(false);
            try {
                migrar(c);
                CifradoDatos.comprobar(c);
                if (aplicar) {
                    c.commit();
                    System.out.println("APLICADO: los cambios quedaron guardados.");
                } else {
                    c.rollback();
                    System.out.println("ENSAYO correcto: no se guardó nada. Usa --aplicar para guardar.");
                }
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        } finally {
            ConexionBD.cerrarPool();
        }
    }

    private static void migrar(Connection c) throws SQLException {
        ejecutar(c, "ALTER TABLE usuarios DISABLE TRIGGER trg_historial");
        ejecutar(c, "ALTER TABLE proveedores DISABLE TRIGGER trg_historial");
        ejecutar(c, "ALTER TABLE usuarios_hist DISABLE TRIGGER trg_historial_inmutable");
        ejecutar(c, "ALTER TABLE proveedores_hist DISABLE TRIGGER trg_historial_inmutable");

        for (String tabla : new String[]{"usuarios", "usuarios_hist"}) {
            for (String col : new String[]{"username", "password_hash", "nombres", "apellido_paterno", "apellido_materno"})
                ejecutar(c, "ALTER TABLE " + tabla + " ALTER COLUMN " + col + " TYPE text");
            // El historial copia todas las columnas, así que también la necesita.
            ejecutar(c, "ALTER TABLE " + tabla + " ADD COLUMN username_busqueda varchar(64)");
        }
        for (String tabla : new String[]{"proveedores", "proveedores_hist"})
            for (String col : new String[]{"telefono", "correo", "direccion"})
                ejecutar(c, "ALTER TABLE " + tabla + " ALTER COLUMN " + col + " TYPE text");

        usuarios(c, "usuarios", "id_usuario");
        usuarios(c, "usuarios_hist", "hist_id");
        cifrarColumnas(c, "proveedores", "id_proveedor", "proveedores", "telefono", "correo", "direccion");
        cifrarColumnas(c, "proveedores_hist", "hist_id", "proveedores", "telefono", "correo", "direccion");
        cifrarColumnas(c, "usuarios_hist", "hist_id", "auditoria", "hist_usuario_app");
        cifrarColumnas(c, "proveedores_hist", "hist_id", "auditoria", "hist_usuario_app");

        ejecutar(c, "ALTER TABLE usuarios ALTER COLUMN username_busqueda SET NOT NULL");
        ejecutar(c, "ALTER TABLE usuarios ADD CONSTRAINT usuarios_username_busqueda_key UNIQUE (username_busqueda)");
        ejecutar(c, "CREATE TABLE seguridad_cifrado (id integer PRIMARY KEY, comprobante text NOT NULL)");
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO seguridad_cifrado VALUES (1, ?)")) {
            ps.setString(1, CifradoDatos.cifrar("papeleria-campos-v1", "configuracion"));
            ps.executeUpdate();
        }

        ejecutar(c, "ALTER TABLE usuarios ENABLE TRIGGER trg_historial");
        ejecutar(c, "ALTER TABLE proveedores ENABLE TRIGGER trg_historial");
        ejecutar(c, "ALTER TABLE usuarios_hist ENABLE TRIGGER trg_historial_inmutable");
        ejecutar(c, "ALTER TABLE proveedores_hist ENABLE TRIGGER trg_historial_inmutable");
    }

    /** Índice de búsqueda, contraseña y datos personales de usuarios. */
    private static void usuarios(Connection c, String tabla, String llave) throws SQLException {
        int filas = 0;
        try (Statement s = c.createStatement();
             var r = s.executeQuery("SELECT " + llave + ", username, password_hash FROM " + tabla);
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE " + tabla + " SET username_busqueda = ?, password_hash = ? WHERE " + llave + " = ?")) {
            while (r.next()) {
                ps.setString(1, CifradoDatos.indiceUsuario(r.getString(2)));
                ps.setString(2, CifradoDatos.protegerHashAnterior(r.getString(3)));
                ps.setLong(3, r.getLong(1));
                filas += ps.executeUpdate();
            }
        }
        System.out.println(tabla + ": " + filas + " filas (índice y contraseña)");
        cifrarColumnas(c, tabla, llave, "usuarios", "username", "nombres", "apellido_paterno", "apellido_materno");
    }

    private static void cifrarColumnas(Connection c, String tabla, String llave, String dominio, String... columnas)
            throws SQLException {
        for (String col : columnas) {
            List<Object[]> valores = new ArrayList<>();
            try (Statement s = c.createStatement();
                 var r = s.executeQuery("SELECT " + llave + ", " + col + " FROM " + tabla + " WHERE " + col + " IS NOT NULL")) {
                while (r.next()) valores.add(new Object[]{r.getLong(1), r.getString(2)});
            }
            try (PreparedStatement ps = c.prepareStatement("UPDATE " + tabla + " SET " + col + " = ? WHERE " + llave + " = ?")) {
                for (Object[] v : valores) {
                    ps.setString(1, CifradoDatos.cifrar((String) v[1], dominio + "." + col));
                    ps.setLong(2, (Long) v[0]);
                    ps.executeUpdate();
                }
            }
            System.out.println(tabla + "." + col + ": " + valores.size() + " valores cifrados");
        }
    }

    private static boolean existeTabla(Connection c, String nombre) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT to_regclass(?) IS NOT NULL")) {
            ps.setString(1, "public." + nombre);
            try (var r = ps.executeQuery()) {
                return r.next() && r.getBoolean(1);
            }
        }
    }

    private static void ejecutar(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.execute(sql);
        }
    }
}
