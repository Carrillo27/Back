package extra.http;

import modelos.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class SesionServlet {
    public static final String USUARIO = "usuarioApi";
    private SesionServlet() {}

    public static Usuario usuario(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null || !(sesion.getAttribute(USUARIO) instanceof Usuario usuario)
                || !usuario.isValidoParaOperar()) {
            throw new ApiException(401, "Inicia sesión para continuar.");
        }
        return usuario;
    }
}
