package servlets;

import extra.http.ApiException;
import extra.http.EntradaJson;
import extra.http.Respuestas;
import extra.http.SesionServlet;

import dao.AdministracionDAO;
import jakarta.json.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/** Tabla public.usuarios; modelo correspondiente: {@link modelos.Usuario}. */
@WebServlet(name = "UsuariosServlet", urlPatterns = "/api/usuarios/*")
public class UsuariosServlet extends JsonServlet {
    private final AdministracionDAO dao = new AdministracionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            prepararPeticion(request, response);
            if (segmentos(request).length != 0) {
                json(response, 404, Map.of("mensaje", "La ruta solicitada no existe."));
                return;
            }
            ok(response, dao.usuarios(sucursal(request),
                    Respuestas.busqueda(request.getParameter("busqueda"))));
        } catch (Exception ex) {
            manejarError(response, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            prepararPeticion(request, response);
            if (segmentos(request).length != 0) {
                json(response, 404, Map.of("mensaje", "La ruta solicitada no existe."));
                return;
            }
            JsonObject body = leerObjeto(request);
            String username = EntradaJson.texto(body, "username", 50);
            if (!username.matches("[A-Za-z0-9._-]{3,50}")) {
                throw new ApiException(400,
                        "El usuario admite letras, números, punto, guion y guion bajo (3 a 50).");
            }
            int id = dao.crearUsuario(sucursal(request), EntradaJson.entero(body, "idRol"), username,
                    contrasena(body), EntradaJson.texto(body, "nombres", 100),
                    EntradaJson.texto(body, "apellidoPaterno", 100),
                    EntradaJson.textoOpcional(body, "apellidoMaterno", 100));
            ok(response, dao.usuario(sucursal(request), id));
        } catch (Exception ex) {
            manejarError(response, ex);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            prepararPeticion(request, response);
            String[] ruta = segmentos(request);
            if (ruta.length < 1 || ruta.length > 2) {
                json(response, 404, Map.of("mensaje", "La ruta solicitada no existe."));
                return;
            }
            int id = id(ruta[0]);
            int sucursal = sucursal(request);
            JsonObject body = leerObjeto(request);
            if (ruta.length == 1) {
                boolean actualizado = dao.actualizarUsuario(sucursal, id, EntradaJson.entero(body, "idRol"),
                        EntradaJson.texto(body, "nombres", 100),
                        EntradaJson.texto(body, "apellidoPaterno", 100),
                        EntradaJson.textoOpcional(body, "apellidoMaterno", 100));
                Respuestas.encontrado(actualizado);
                ok(response, dao.usuario(sucursal, id));
                return;
            }
            if ("estado".equals(ruta[1])) {
                boolean activo = EntradaJson.booleano(body, "activo");
                if (!activo && id == SesionServlet.usuario(request).getIdUsuario()) {
                    throw new ApiException(409, "No puedes desactivar tu propio usuario.");
                }
                Respuestas.encontrado(dao.estadoUsuario(sucursal, id, activo));
                ok(response, dao.usuario(sucursal, id));
            } else if ("contrasena".equals(ruta[1])) {
                Respuestas.encontrado(dao.cambiarContrasena(sucursal, id, contrasena(body)));
                ok(response, Map.of("mensaje", "Contraseña actualizada."));
            } else {
                json(response, 404, Map.of("mensaje", "La ruta solicitada no existe."));
            }
        } catch (Exception ex) {
            manejarError(response, ex);
        }
    }

    private int sucursal(HttpServletRequest request) {
        return SesionServlet.usuario(request).getIdSucursal();
    }

    private static String contrasena(JsonObject body) {
        String valor = EntradaJson.texto(body, "contrasena", 72);
        if (valor.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new ApiException(400, "La contraseña admite como máximo 72 bytes UTF-8.");
        }
        if (valor.length() < 8) {
            throw new ApiException(400, "La contraseña debe tener al menos 8 caracteres.");
        }
        return valor;
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
}
