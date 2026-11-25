package servlets;

import extra.http.ApiException;
import extra.http.EntradaJson;
import extra.http.Respuestas;
import extra.http.SesionServlet;

import dao.CatalogoDAO;
import jakarta.json.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Set;

/** Tabla public.equipos_computo; modelo correspondiente: {@link modelos.EquipoComputo}. */
@WebServlet(name = "EquiposServlet", urlPatterns = "/api/equipos/*")
public class EquiposServlet extends JsonServlet {
    private static final Set<String> ESTADOS = Set.of("Disponible", "En uso", "Sin servicio");
    private final CatalogoDAO dao = new CatalogoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            prepararPeticion(request, response);
            if (segmentos(request).length != 0) {
                json(response, 404, Map.of("mensaje", "La ruta solicitada no existe."));
                return;
            }
            ok(response, dao.equipos(sucursal(request),
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
            int sucursal = sucursal(request);
            int id = dao.crearEquipo(sucursal, numero(body), EntradaJson.texto(body, "descripcion", 120),
                    EntradaJson.textoOpcional(body, "modelo", 80), fecha(body));
            ok(response, dao.equipo(sucursal, id));
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
            boolean actualizado;
            if (ruta.length == 2 && "estado".equals(ruta[1])) {
                String estado = Respuestas.opcion(EntradaJson.texto(body, "estado", 20), ESTADOS, "estado");
                actualizado = dao.estadoEquipo(sucursal, id, estado);
            } else if (ruta.length == 1) {
                actualizado = dao.actualizarEquipo(sucursal, id, numero(body),
                        EntradaJson.texto(body, "descripcion", 120),
                        EntradaJson.textoOpcional(body, "modelo", 80), fecha(body));
            } else {
                json(response, 404, Map.of("mensaje", "La ruta solicitada no existe."));
                return;
            }
            Respuestas.encontrado(actualizado);
            ok(response, dao.equipo(sucursal, id));
        } catch (Exception ex) {
            manejarError(response, ex);
        }
    }

    private int sucursal(HttpServletRequest request) {
        return SesionServlet.usuario(request).getIdSucursal();
    }

    private static int numero(JsonObject body) {
        int numero = EntradaJson.entero(body, "numeroEquipo");
        if (numero <= 0) {
            throw new ApiException(400, "El número de equipo debe ser mayor que cero.");
        }
        return numero;
    }

    private static LocalDate fecha(JsonObject body) {
        String texto = EntradaJson.textoOpcional(body, "fechaAdquisicion", 10);
        try {
            return texto == null ? null : LocalDate.parse(texto);
        } catch (DateTimeParseException error) {
            throw new ApiException(400, "La fecha debe tener el formato AAAA-MM-DD.");
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
