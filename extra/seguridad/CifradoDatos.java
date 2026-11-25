package extra.seguridad;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.sql.*;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/** Cifrado AES-128/ECB/PKCS5Padding solicitado para la práctica.
 * Base64 solo representa los bytes. ECB no autentica el contenido y revela bloques repetidos.
 * La clave se conserva fuera del código y nunca se genera al reiniciar la aplicación.
 */
public final class CifradoDatos {
    private static final String VERSION = "ecb1:";
    private final SecretKey clave;

    public CifradoDatos(SecretKey clave) {
        Objects.requireNonNull(clave, "Falta la clave AES.");
        byte[] bytes = clave.getEncoded();
        if (!"AES".equalsIgnoreCase(clave.getAlgorithm()) || bytes == null || bytes.length != 16)
            throw new IllegalArgumentException("La clave debe ser AES-128: exactamente 16 bytes.");
        this.clave = new SecretKeySpec(bytes, "AES");
    }

    /** Solo para preparar una nueva instalación: guardar la clave antes de cifrar datos. */
    public static SecretKey generarClave() throws GeneralSecurityException {
        KeyGenerator generador = KeyGenerator.getInstance("AES");
        generador.init(128);
        return generador.generateKey();
    }

    public static CifradoDatos desdeEntorno() {
        return new CifradoDatos(new SecretKeySpec(clave(), "AES"));
    }

    public String cifrar(String texto) throws GeneralSecurityException {
        Objects.requireNonNull(texto, "Falta el texto.");
        Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
        aes.init(Cipher.ENCRYPT_MODE, clave);
        byte[] cifrado = aes.doFinal(texto.getBytes(StandardCharsets.UTF_8));
        return VERSION + Base64.getEncoder().encodeToString(cifrado);
    }

    public String descifrar(String valor) throws GeneralSecurityException {
        if (valor == null || !valor.startsWith(VERSION))
            throw new GeneralSecurityException("Formato de cifrado no válido.");
        try {
            byte[] cifrado = Base64.getDecoder().decode(valor.substring(VERSION.length()));
            if (cifrado.length == 0 || cifrado.length % 16 != 0)
                throw new GeneralSecurityException("Contenido cifrado incompleto.");
            Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
            aes.init(Cipher.DECRYPT_MODE, clave);
            return new String(aes.doFinal(cifrado), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new GeneralSecurityException("Contenido cifrado no válido.", ex);
        }
    }

    // Adaptación a los campos y consultas existentes de la aplicación.
    private static byte[] clave() {
        String clave = System.getenv("PAPELERIA_AES_KEY_BASE64");
        if (clave == null || clave.isBlank()) throw new IllegalStateException("Falta la clave de cifrado del servidor.");
        return Base64.getDecoder().decode(clave);
    }
    public static String cifrar(String valor, String campo) throws SQLException {
        if (valor == null) return null;
        try { return CifradoDatos.desdeEntorno().cifrar(campo + "\0" + valor); }
        catch (GeneralSecurityException | RuntimeException e) { throw error(e); }
    }
    public static String descifrar(String valor, String campo) throws SQLException {
        if (valor == null) return null;
        try {
            String texto = CifradoDatos.desdeEntorno().descifrar(valor);
            if (!texto.startsWith(campo + "\0")) throw new GeneralSecurityException("Campo no coincidente.");
            return texto.substring(campo.length()+1);
        } catch (GeneralSecurityException | RuntimeException e) { throw error(e); }
    }
    /** HMAC separado por dominio: búsqueda exacta/unicidad sin almacenar username legible. */
    public static String indiceUsuario(String usuario) throws SQLException {
        return indiceUsuario(usuario, clave());
    }
    static String indiceUsuario(String usuario, byte[] llave) throws SQLException {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(llave, "HmacSHA256"));
            byte[] derivada = mac.doFinal("papeleria:indice-username:v1".getBytes(StandardCharsets.UTF_8));
            mac.init(new SecretKeySpec(derivada, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(usuario.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException | RuntimeException e) { throw error(e); }
    }
    public static Map<String,Object> usuario(Map<String,Object> fila) throws SQLException {
        return descifrarFila(fila, "usuarios", "username", "nombres", "apellidoPaterno", "apellidoMaterno");
    }
    public static Map<String,Object> proveedor(Map<String,Object> fila) throws SQLException {
        return descifrarFila(fila, "proveedores", "telefono", "correo", "direccion");
    }
    private static Map<String,Object> descifrarFila(Map<String,Object> fila, String tabla, String... campos) throws SQLException {
        if (fila != null) for (String campo:campos) {
            String columna = campo.replace("apellidoPaterno","apellido_paterno").replace("apellidoMaterno","apellido_materno");
            fila.put(campo, descifrar((String)fila.get(campo), tabla+"."+columna));
        }
        return fila;
    }
    public static List<Map<String,Object>> usuarios(List<Map<String,Object>> filas, String busqueda) throws SQLException {
        for (var fila:filas) usuario(fila);
        return filtrar(filas,busqueda,"username","username","nombres","apellidoPaterno");
    }
    public static List<Map<String,Object>> proveedores(List<Map<String,Object>> filas, String busqueda) throws SQLException {
        for (var fila:filas) proveedor(fila);
        return filtrar(filas,busqueda,"nombreEmpresa","nombreEmpresa","contactoNombres","correo");
    }
    private static List<Map<String,Object>> filtrar(List<Map<String,Object>> filas,String busqueda,String ordenar,String... campos) {
        String filtro=busqueda==null?"":busqueda.trim().toLowerCase(Locale.ROOT);
        filas.removeIf(fila -> Arrays.stream(campos).noneMatch(c -> Objects.toString(fila.get(c),"").toLowerCase(Locale.ROOT).contains(filtro)));
        filas.sort(Comparator.comparing(f -> Objects.toString(f.get(ordenar),""),String.CASE_INSENSITIVE_ORDER));
        return filas;
    }
    public static Map<String,Object> venta(Map<String,Object> fila) throws SQLException {
        if (fila!=null) fila.put("usuario",descifrar((String)fila.get("usuario"),"usuarios.username"));
        return fila;
    }
    public static List<Map<String,Object>> ventas(List<Map<String,Object>> filas) throws SQLException {
        for (var fila:filas) venta(fila); return filas;
    }
    public static void comprobar(Connection c) throws SQLException {
        try (var s=c.createStatement();var r=s.executeQuery("SELECT comprobante FROM public.seguridad_cifrado WHERE id=1")) {
            if (!r.next() || !"papeleria-campos-v1".equals(descifrar(r.getString(1),"configuracion")))
                throw new SQLException("La migración de cifrado no está preparada.","08004");
        }
    }
    private static SQLException error(Exception e) {
        return new SQLException("No fue posible proteger o recuperar los datos. Revisa la clave y la migración del servidor.","58000",e);
    }

    // Compatibilidad temporal con contraseñas antiguas: no pueden recuperarse desde su hash.
    public static final String LEGADO="bcrypt-aes:";
    public static boolean esLegado(String valor) {return valor!=null && valor.startsWith(LEGADO);}
    public static String cifrarContrasena(String password) throws SQLException {
        return CifradoDatos.cifrar(password,"usuarios.password");
    }
    public static String protegerHashAnterior(String hash) throws SQLException {
        if(hash==null || !hash.matches("\\$2[aby]\\$.*")) throw new SQLException("Credencial anterior no reconocida.");
        return LEGADO+CifradoDatos.cifrar(hash,"usuarios.password_hash");
    }
    public static boolean verificar(Connection c,String password,String almacenada) throws SQLException {
        if(password==null || almacenada==null)return false;
        if(esLegado(almacenada)) {
            // El hash no permite recuperar la contraseña. Solo tras verificarla se convierte a AES.
            String hash=CifradoDatos.descifrar(almacenada.substring(LEGADO.length()),"usuarios.password_hash");
            if(password.getBytes(StandardCharsets.UTF_8).length>72)return false;
            try(var ps=c.prepareStatement("SELECT public.crypt(?, ?) = ?")) {
                ps.setString(1,password);ps.setString(2,hash);ps.setString(3,hash);
                try(var rs=ps.executeQuery()){return rs.next()&&rs.getBoolean(1);}
            }
        }
        String real=CifradoDatos.descifrar(almacenada,"usuarios.password");
        return MessageDigest.isEqual(real.getBytes(StandardCharsets.UTF_8),password.getBytes(StandardCharsets.UTF_8));
    }
}
