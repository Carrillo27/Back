package servlets;

import extra.http.ApiException;
import extra.http.EntradaJson;
import extra.http.SesionServlet;

import dao.UsuarioDAO;
import modelos.Usuario;
import jakarta.json.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@WebServlet(name = "AccesoServlet", urlPatterns = {"/api/salud", "/api/auth/*"})
public class AccesoServlet extends JsonServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            prepararPeticion(request, response);
            if ("/api/salud".equals(request.getServletPath())) {
                ok(response, Map.of("estado", "ok", "aplicacion", "Papeleria Sol API"));
            } else if ("/me".equals(request.getPathInfo())) {
                ok(response, SesionServlet.usuario(request));
            } else {
                json(response, HttpServletResponse.SC_NOT_FOUND, Map.of("mensaje", "La ruta solicitada no existe."));
            }
        } catch (Exception ex) {
            manejarError(response, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            prepararPeticion(request, response);
            if ("/login".equals(request.getPathInfo())) iniciar(request, response);
            else if ("/logout".equals(request.getPathInfo())) salir(request, response);
            else json(response, HttpServletResponse.SC_NOT_FOUND, Map.of("mensaje", "La ruta solicitada no existe."));
        } catch (Exception ex) {
            manejarError(response, ex);
        }
    }

    private void iniciar(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        JsonObject body = leerObjeto(request);
        String username = EntradaJson.texto(body, "username", 40).trim();
        String password = EntradaJson.texto(body, "password", 200);
        Usuario usuario = new UsuarioDAO().autenticar(username, password);
        if (usuario == null || !usuario.isValidoParaOperar()) {
            throw new ApiException(HttpServletResponse.SC_UNAUTHORIZED, "Usuario o contraseña incorrectos.");
        }
        if (request.getSession(false) != null) request.getSession(false).invalidate();
        request.getSession(true).setAttribute(SesionServlet.USUARIO, usuario);
        request.changeSessionId();
        ok(response, usuario);
    }

    private void salir(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getSession(false) != null) request.getSession(false).invalidate();
        ok(response, Map.of("mensaje", "Sesión cerrada."));
    }

    @Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            prepararPeticion(request, response);
            metodoNoPermitido(response, "PATCH");
        } catch (Exception ex) {
            manejarError(response, ex);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            prepararPeticion(request, response);
            metodoNoPermitido(response, "DELETE");
        } catch (Exception ex) {
            manejarError(response, ex);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            prepararPeticion(request, response);
            metodoNoPermitido(response, "PUT");
        } catch (Exception ex) {
            manejarError(response, ex);
        }
    }
}
