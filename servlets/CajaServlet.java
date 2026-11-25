package servlets;

import extra.http.EntradaJson;
import extra.http.SesionServlet;

import modelos.Usuario;
import extra.servicio.CajaServicio;
import jakarta.json.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@WebServlet(name = "CajaServlet", urlPatterns = "/api/caja/*")
public class CajaServlet extends JsonServlet {
    private final CajaServicio servicio = new CajaServicio();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            prepararPeticion(request, response);
            String[] ruta = segmentos(request);
            if (ruta.length != 1 || !"actual".equals(ruta[0])) {
                json(response, HttpServletResponse.SC_NOT_FOUND, Map.of("mensaje", "La ruta solicitada no existe."));
                return;
            }
            ok(response, servicio.actual(SesionServlet.usuario(request)));
        } catch (Exception ex) {
            manejarError(response, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            prepararPeticion(request, response);
            String[] ruta = segmentos(request);
            JsonObject body = leerObjeto(request);
            Usuario usuario = SesionServlet.usuario(request);
            if (ruta.length != 1) {
                json(response, HttpServletResponse.SC_NOT_FOUND, Map.of("mensaje", "La ruta solicitada no existe."));
            } else switch (ruta[0]) {
                case "aperturas" -> ok(response, servicio.abrir(usuario,
                        EntradaJson.importe(body, "fondoInicial", false)));
                case "entradas" -> ok(response, movimiento(servicio, usuario, body, true));
                case "retiros" -> ok(response, movimiento(servicio, usuario, body, false));
                case "cierres" -> ok(response, servicio.cerrar(usuario, EntradaJson.entero(body, "idTurno"),
                        EntradaJson.importe(body, "efectivoContado", false)));
                default -> json(response, HttpServletResponse.SC_NOT_FOUND,
                        Map.of("mensaje", "La ruta solicitada no existe."));
            }
        } catch (Exception ex) {
            manejarError(response, ex);
        }
    }

    private static Map<String, Object> movimiento(CajaServicio servicio, Usuario usuario,
            JsonObject body, boolean entrada) throws SQLException {
        return servicio.movimiento(usuario, EntradaJson.entero(body, "idTurno"),
                EntradaJson.importe(body, "monto", true), EntradaJson.texto(body, "motivo", 200), entrada);
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
