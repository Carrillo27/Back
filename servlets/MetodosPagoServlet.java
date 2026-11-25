package servlets;

import extra.http.ApiException;
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

/** Tabla public.metodos_pago; modelo correspondiente: {@link modelos.MetodoPago}. */
@WebServlet(name = "MetodosPagoServlet", urlPatterns = "/api/metodos-pago/*")
public class MetodosPagoServlet extends JsonServlet {
    private static final int ID_EFECTIVO = 2;
    private static final Set<String> TIPOS = Set.of("Efectivo", "Tarjeta", "Transferencia");
    private final CatalogoDAO dao = new CatalogoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            prepararPeticion(request, response);
            if (segmentos(request).length != 0) {
                json(response, 404, Map.of("mensaje", "La ruta solicitada no existe."));
                return;
            }
            ok(response, dao.metodosPago(Respuestas.busqueda(request.getParameter("busqueda"))));
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
            String tipo = Respuestas.opcion(EntradaJson.texto(body, "tipo", 20), TIPOS, "tipo");
            int id = dao.crearMetodoPago(EntradaJson.texto(body, "nombre", 60), tipo,
                    EntradaJson.booleano(body, "requiereFolio"));
            ok(response, dao.metodoPago(id));
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
            protegerEfectivo(id);
            JsonObject body = leerObjeto(request);
            boolean actualizado;
            if (ruta.length == 2 && "estado".equals(ruta[1])) {
                actualizado = dao.estadoMetodoPago(id, EntradaJson.booleano(body, "activo"));
            } else if (ruta.length == 1) {
                String tipo = Respuestas.opcion(EntradaJson.texto(body, "tipo", 20), TIPOS, "tipo");
                actualizado = dao.actualizarMetodoPago(id, EntradaJson.texto(body, "nombre", 60), tipo,
                        EntradaJson.booleano(body, "requiereFolio"));
            } else {
                json(response, 404, Map.of("mensaje", "La ruta solicitada no existe."));
                return;
            }
            Respuestas.encontrado(actualizado);
            ok(response, dao.metodoPago(id));
        } catch (Exception ex) {
            manejarError(response, ex);
        }
    }

    private static void protegerEfectivo(int id) {
        if (id == ID_EFECTIVO) {
            throw new ApiException(409, "El método Efectivo es requerido por la caja y no puede modificarse.");
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
