package extra.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Un único pool por despliegue, compartido por todos los DAO. */
public final class ConexionBD {
    private static HikariDataSource dataSource;
    private static boolean cerrado;

    private ConexionBD() { }

    private static String configuracion(String nombre, String predeterminado) {
        String valor = System.getenv(nombre);
        return valor == null || valor.isBlank() ? predeterminado : valor;
    }

    private static int entero(String nombre, int predeterminado) {
        return Integer.parseInt(configuracion(nombre, Integer.toString(predeterminado)));
    }

    /** La contraseña se configura fuera del repositorio y del WAR.
     * En Render usar PAPELERIA_DB_PASSWORD. En esta laptop se admite el archivo privado.
     */
    private static String passwordConfigurada() throws SQLException {
        String valor = System.getenv("PAPELERIA_DB_PASSWORD");
        if (valor != null && !valor.isBlank()) return valor;
        String archivo = System.getenv("PAPELERIA_CONFIG_FILE");
        Path ruta = archivo == null || archivo.isBlank()
                ? Path.of(System.getProperty("user.home"), ".papeleria", "bank-render.properties")
                : Path.of(archivo);
        if (!Files.exists(ruta)) return null;
        Properties propiedades = new Properties();
        try (Reader reader = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            propiedades.load(reader);
            return propiedades.getProperty("password");
        } catch (IOException ex) {
            throw new SQLException("No se pudo leer la configuración privada de Bank.", "08001", ex);
        }
    }
    private static synchronized HikariDataSource pool() throws SQLException {
        if (cerrado) throw new SQLException("El pool de conexiones está cerrado.", "08003");
        if (dataSource != null) return dataSource;
        String password = passwordConfigurada();
        if (password == null || password.isBlank()) {
            throw new SQLException("Configura PAPELERIA_DB_PASSWORD o password en el archivo privado bank-render.properties.", "08001");
        }
        try {
            int maximo = entero("PAPELERIA_POOL_MAX", 10);
            int minimo = entero("PAPELERIA_POOL_MIN_IDLE", 2);
            if (maximo < 1 || minimo < 0 || minimo > maximo) {
                throw new IllegalArgumentException("Se requiere 0 <= mínimo <= máximo y máximo >= 1.");
            }
            HikariConfig config = new HikariConfig();
            config.setPoolName("PapeleriaPool");
            config.setJdbcUrl(configuracion("PAPELERIA_DB_URL", "jdbc:postgresql://dpg-dc3hd93m8hqs739567pg-a.oregon-postgres.render.com:5432/papeleria_sol_2?sslmode=require"));
            config.setUsername(configuracion("PAPELERIA_DB_USER", "papoi"));
            config.setPassword(password);
            config.setDriverClassName("org.postgresql.Driver");
            config.setMaximumPoolSize(maximo);
            config.setMinimumIdle(minimo);
            config.setIdleTimeout(30_000);
            config.setConnectionTimeout(20_000);
            config.setValidationTimeout(5_000);
            config.setMaxLifetime(1_800_000);
            config.setAutoCommit(true);
            // Propiedades de pgJDBC, no las opciones de caché de MySQL del ejemplo.
            config.addDataSourceProperty("preparedStatementCacheQueries", "250");
            config.addDataSourceProperty("preparedStatementCacheSizeMiB", "5");
            dataSource = new HikariDataSource(config);
            return dataSource;
        } catch (RuntimeException e) {
            throw new SQLException("No fue posible inicializar el pool de PostgreSQL.", "08001", e);
        }
    }

    public static void iniciarPool() throws SQLException {
        pool();
    }

    /** close() devuelve la conexión al pool; los DAO conservan su try-with-resources. */
    public static Connection obtenerConexion() throws SQLException {
        Connection conexion = pool().getConnection();
        try {
            registrarUsuarioApp(conexion);
            return conexion;
        } catch (SQLException e) {
            try { conexion.close(); } catch (SQLException cierre) { e.addSuppressed(cierre); }
            throw e;
        }
    }

    /** Sobrescribe SIEMPRE el contexto de auditoría al prestar una conexión reutilizada. */
    private static void registrarUsuarioApp(Connection conexion) throws SQLException {
        String usuarioApp = UsuarioActual.obtener();
        try (PreparedStatement ps = conexion.prepareStatement("SELECT set_config('app.usuario', ?, false)")) {
            ps.setString(1, usuarioApp == null ? "" : extra.seguridad.CifradoDatos.cifrar(usuarioApp, "auditoria.hist_usuario_app"));
            ps.execute();
        }
    }

    /** Se invoca al retirar o apagar la aplicación; no permite reabrir el pool retirado. */
    public static synchronized void cerrarPool() {
        cerrado = true;
        if (dataSource != null) dataSource.close();
    }
}
