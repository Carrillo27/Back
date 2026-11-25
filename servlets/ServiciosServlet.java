package servlets;

import extra.http.EntradaJson;
import extra.http.Respuestas;

import dao.CatalogoDAO;
import jakarta.json.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/** Tabla public.servicios_precios; modelo correspondiente: {@link modelos.ServicioPrecio}. */
@WebServlet(name = "ServiciosServlet", urlPatterns = "/api/servicios/*")
public class ServiciosServlet extends JsonServlet {
    private static final Set<String> COLORES = Set.of("Color", "B/N");
    private final CatalogoDAO dao = new CatalogoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            prepararPeticion(request, response);
            if (segmentos(request).length != 0) {
                json(response, 404, Map.of("mensaje", "La ruta solicitada no existe."));
                return;
            }
            ok(response, dao.servicios(Respuestas.busqueda(request.getParameter("busqueda"))));
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
            int id = dao.crearServicio(EntradaJson.texto(body, "tipoServicio", 60),
                    Respuestas.opcion(EntradaJson.texto(body, "color", 10), COLORES, "color"),
                    EntradaJson.texto(body, "tamano", 30), EntradaJson.booleano(body, "dobleCara"),
                    EntradaJson.importe(body, "precioVigente", false));
            ok(response, dao.servicio(id));
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
            boolean actualizado;
            if (ruta.length == 2 && "estado".equals(ruta[1])) {
                actualizado = dao.estadoServicio(id, EntradaJson.booleano(body, "activo"));
            } else if (ruta.length == 1) {
                actualizado = dao.actualizarServicio(id, EntradaJson.texto(body, "tipoServicio", 60),
                        Respuestas.opcion(EntradaJson.texto(body, "color", 10), COLORES, "color"),
                        EntradaJson.texto(body, "tamano", 30), EntradaJson.booleano(body, "dobleCara"),
                        EntradaJson.importe(body, "precioVigente", false));
            } else {
                json(response, 404, Map.of("mensaje", "La ruta solicitada no existe."));
                return;
            }
            Respuestas.encontrado(actualizado);
            ok(response, dao.servicio(id));
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

    /** PUSH reservado: este módulo no define eventos ni sincronización adicionales. */
    @Override
    protected void doPush(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            prepararPeticion(request, response);
            metodoNoPermitido(response, "PUSH");
        } catch (Exception ex) {
            manejarError(response, ex);
        }
    }
}
