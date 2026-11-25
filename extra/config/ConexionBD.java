package extra.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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

    private static synchronized HikariDataSource pool() throws SQLException {
        if (cerrado) throw new SQLException("El pool de conexiones está cerrado.", "08003");
        if (dataSource != null) return dataSource;
        String password = System.getenv("PAPELERIA_DB_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new SQLException("La variable PAPELERIA_DB_PASSWORD no está configurada en GlassFish.", "08001");
        }
        try {
            int maximo = entero("PAPELERIA_POOL_MAX", 10);
            int minimo = entero("PAPELERIA_POOL_MIN_IDLE", 2);
            if (maximo < 1 || minimo < 0 || minimo > maximo) {
                throw new IllegalArgumentException("Se requiere 0 <= mínimo <= máximo y máximo >= 1.");
            }
            HikariConfig config = new HikariConfig();
            config.setPoolName("PapeleriaPool");
            config.setJdbcUrl(configuracion("PAPELERIA_DB_URL", "jdbc:postgresql://localhost:5432/Pepeleria_Sol"));
            config.setUsername(configuracion("PAPELERIA_DB_USER", "postgres"));
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
