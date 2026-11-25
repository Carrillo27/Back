package servlets;

import extra.http.ApiException;
import extra.http.EntradaJson;
import extra.http.Respuestas;
import extra.http.SesionServlet;

import dao.HistorialVentasDAO;
import modelos.Usuario;
import extra.servicio.VentaServicio;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonNumber;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Tabla public.ventas; modelo correspondiente: {@link modelos.Venta}. */
@WebServlet(name = "VentasServlet", urlPatterns = "/api/ventas/*")
public class VentasServlet extends JsonServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            prepararPeticion(request, response);
            Usuario usuario = SesionServlet.usuario(request); String[] ruta = segmentos(request);
            HistorialVentasDAO dao = new HistorialVentasDAO();
            if (ruta.length == 0) {
                LocalDate fin = fecha(request.getParameter("hasta"), LocalDate.now(), "hasta");
                LocalDate inicio = fecha(request.getParameter("desde"), fin.withDayOfMonth(1), "desde");
                if (inicio.isAfter(fin)) throw new ApiException(400, "La fecha inicial no puede ser posterior a la final.");
                if (inicio.plusYears(1).isBefore(fin)) throw new ApiException(400, "El rango máximo es de un año.");
                String estado = request.getParameter("estado");
                if (estado != null && !estado.isBlank()
                        && !Set.of("Pendiente", "Cobrado", "Cancelado").contains(estado)) {
                    throw new ApiException(400, "Estado no permitido.");
                }
                ok(response, dao.listar(usuario.getIdSucursal(), inicio, fin,
                        estado == null || estado.isBlank() ? null : estado));
            } else if (ruta.length == 1) {
                int id = id(ruta[0]);
                Map<String, Object> venta = new LinkedHashMap<>(Respuestas.existente(
                        dao.venta(usuario.getIdSucursal(), id)));
                venta.put("lineas", dao.lineas(id));
                ok(response, venta);
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
            if (segmentos(request).length != 0) {
                json(response, HttpServletResponse.SC_NOT_FOUND, Map.of("mensaje", "La ruta solicitada no existe."));
                return;
            }
            JsonObject body = leerObjeto(request);
            if (!(body.get("lineas") instanceof JsonArray lineas)) {
                throw new ApiException(400, "Envía las líneas del ticket.");
            }
            List<VentaServicio.Detalle> detalles = new ArrayList<>();
            for (var valor : lineas) {
                if (!(valor instanceof JsonObject linea)) throw new ApiException(400, "Línea inválida.");
                detalles.add(new VentaServicio.Detalle(EntradaJson.entero(linea, "idProducto"),
                        EntradaJson.entero(linea, "cantidad")));
            }
            ok(response, new VentaServicio().registrar(SesionServlet.usuario(request), detalles,
                    EntradaJson.importe(body, "recibido", false), EntradaJson.importe(body, "totalEsperado", true)));
        } catch (Exception ex) {
            manejarError(response, ex);
        }
    }

    private static LocalDate fecha(String texto, LocalDate predeterminada, String campo) {
        try { return texto == null || texto.isBlank() ? predeterminada : LocalDate.parse(texto); }
        catch (DateTimeParseException ex) {
            throw new ApiException(400, "La fecha " + campo + " debe tener el formato AAAA-MM-DD.");
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
