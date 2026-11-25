package extra.config;

/**
 * Guarda, durante una petición HTTP, el usuario de la aplicación que la hace.
 * ConexionBD lo envía a PostgreSQL para que el historial (database/005-historial.sql)
 * registre quién hizo cada cambio.
 */
public final class UsuarioActual {

    private static final ThreadLocal<String> ACTUAL = new ThreadLocal<>();

    private UsuarioActual() {}

    public static void establecer(String username) {
        if (username == null || username.isBlank()) {
            ACTUAL.remove();
        } else {
            ACTUAL.set(username);
        }
    }

    public static String obtener() {
        return ACTUAL.get();
    }

    public static void limpiar() {
        ACTUAL.remove();
    }
}
