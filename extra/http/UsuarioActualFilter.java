package extra.http;

import extra.config.UsuarioActual;
import modelos.Usuario;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/** Anota en cada petición el usuario con sesión iniciada, para el historial de la base de datos. */
@WebFilter("/*")
public class UsuarioActualFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            if (request instanceof HttpServletRequest http) {
                HttpSession sesion = http.getSession(false);
                if (sesion != null && sesion.getAttribute(SesionServlet.USUARIO) instanceof Usuario usuario) {
                    UsuarioActual.establecer(usuario.getUsername());
                }
            }
            chain.doFilter(request, response);
        } finally {
            UsuarioActual.limpiar();
        }
    }
}
