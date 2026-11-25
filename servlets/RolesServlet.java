package servlets;

import extra.http.ApiException;
import extra.http.EntradaJson;
import extra.http.Respuestas;

import dao.AdministracionDAO;
import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Tabla public.roles; modelo correspondiente: {@link modelos.Rol}. */
@WebServlet(name = "RolesServlet", urlPatterns = "/api/roles/*")
public class RolesServlet extends JsonServlet {
    private final AdministracionDAO dao = new AdministracionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            prepararPeticion(request, response);
            String[] ruta = segmentos(request);
            if (ruta.length == 0) {
                ok(response, dao.roles());
            } else if (ruta.length == 2 && "permisos".equals(ruta[1])) {
                int id = id(ruta[0]);
                Respuestas.existente(dao.rol(id));
                ok(response, dao.permisosDeRol(id));
            } else {
                json(response, 404, Map.of("mensaje", "La ruta solicitada no existe."));
            }
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
            ok(response, dao.rol(dao.crearRol(EntradaJson.texto(body, "nombre", 50))));
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
            JsonObject body = leerObjeto(request);
            if (ruta.length == 1) {
                Respuestas.encontrado(dao.actualizarRol(id, EntradaJson.texto(body, "nombre", 50)));
                ok(response, dao.rol(id));
                return;
            }
            if (!"permisos".equals(ruta[1])) {
                json(response, 404, Map.of("mensaje", "La ruta solicitada no existe."));
                return;
            }
            Respuestas.existente(dao.rol(id));
            if (!(body.get("permisos") instanceof JsonArray permisos) || permisos.size() > 200) {
                throw new ApiException(400, "Envía la lista de permisos.");
            }
            List<Integer> ids = new ArrayList<>();
            for (var valor : permisos) {
                if (!(valor instanceof JsonNumber numero) || !numero.isIntegral()) {
                    throw new ApiException(400, "Cada permiso debe ser un identificador entero.");
                }
                try {
                    ids.add(numero.intValueExact());
                } catch (ArithmeticException error) {
                    throw new ApiException(400, "Cada permiso debe ser un identificador entero.");
                }
            }
            dao.asignarPermisos(id, ids.stream().distinct().toList());
            ok(response, dao.permisosDeRol(id));
        } catch (Exception ex) {
            manejarError(response, ex);
        }
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
