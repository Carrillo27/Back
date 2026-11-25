package extra.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.SQLException;

/** Gestiona el pool una vez por aplicación, no una vez por servlet. */
@WebListener
public class AppContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            ConexionBD.iniciarPool();
            try (var conexion = ConexionBD.obtenerConexion()) { extra.seguridad.CifradoDatos.comprobar(conexion); }
            event.getServletContext().log("Pool de PostgreSQL inicializado.");
        } catch (SQLException e) {
            ConexionBD.cerrarPool();
            throw new IllegalStateException("No se pudo iniciar el pool de PostgreSQL. Revisa la configuración del servidor.", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        ConexionBD.cerrarPool();
    }
}
