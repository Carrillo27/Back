package servlets;

import extra.http.ApiException;
import extra.http.Respuestas;
import extra.http.SesionServlet;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Base común para Servlets JSON: validación de escritura, serialización y errores. */
public abstract class JsonServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(JsonServlet.class.getName());
    /** Distribuye el verbo personalizado; los verbos estándar conservan su despacho original. */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("PUSH".equals(request.getMethod())) {
            doPush(request, response);
        } else {
            super.service(request, response);
        }
    }

    protected JsonObject leerObjeto(HttpServletRequest request) throws IOException {
        String tipo = request.getContentType();
        if (tipo == null || !tipo.toLowerCase(java.util.Locale.ROOT).startsWith("application/json")) {
            throw new ApiException(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                    "El contenido debe enviarse como application/json.");
        }
        try (JsonReader reader = Json.createReader(request.getReader())) {
            return reader.readObject();
        } catch (RuntimeException ex) {
            throw new ApiException(HttpServletResponse.SC_BAD_REQUEST,
                    "El cuerpo debe contener un objeto JSON válido.");
        }
    }

    protected void json(HttpServletResponse response, int status, Object contenido) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try (Jsonb jsonb = JsonbBuilder.create()) {
            jsonb.toJson(contenido, response.getWriter());
        } catch (Exception ex) {
            throw new IOException("No fue posible serializar la respuesta JSON.", ex);
        }
    }

    protected void ok(HttpServletResponse response, Object contenido) throws IOException {
        json(response, HttpServletResponse.SC_OK, contenido);
    }

    protected String[] segmentos(HttpServletRequest request) {
        String ruta = request.getPathInfo();
        if (ruta == null || ruta.isBlank() || "/".equals(ruta)) return new String[0];
        return java.util.Arrays.stream(ruta.split("/"))
                .filter(segmento -> !segmento.isBlank()).toArray(String[]::new);
    }

    protected int id(String texto) {
        try {
            return Respuestas.id(Integer.parseInt(texto));
        } catch (NumberFormatException ex) {
            throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Identificador inválido.");
        }
    }

    /** Validaciones compartidas; cada método HTTP captura sus propios errores. */
    protected void prepararPeticion(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");
        validarCabeceraEscritura(request);
        validarSesion(request);
    }

    protected void metodoNoPermitido(HttpServletResponse response, String metodo) throws IOException {
        json(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                Map.of("mensaje", "Método " + metodo + " no disponible para esta ruta."));
    }
    private void validarSesion(HttpServletRequest request) {
        String ruta = request.getServletPath() + (request.getPathInfo() == null ? "" : request.getPathInfo());
        if ("/api/salud".equals(ruta) || "/api/auth/login".equals(ruta)
                || "/api/auth/logout".equals(ruta)) return;
        SesionServlet.usuario(request);
    }
    private void validarCabeceraEscritura(HttpServletRequest request) {
        String metodo = request.getMethod();
        if (!"GET".equals(metodo) && !"HEAD".equals(metodo) && !"OPTIONS".equals(metodo)
                && !"1".equals(request.getHeader("X-POS-Request"))) {
            throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Falta la cabecera de la aplicación.");
        }
    }

    protected void manejarError(HttpServletResponse response, Exception error) throws IOException {
        int status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        String mensaje = "No fue posible completar la operación.";
        if (error instanceof ApiException api) {
            status = api.getStatus();
            mensaje = api.getMessage();
        } else if (error instanceof IllegalArgumentException) {
            status = HttpServletResponse.SC_BAD_REQUEST;
            mensaje = error.getMessage();
        } else if (error instanceof SQLException sql) {
            String state = sql.getSQLState();
            if ("23505".equals(state)) {
                status = HttpServletResponse.SC_CONFLICT;
                mensaje = "Ya existe un registro con esos datos.";
            } else if ("23503".equals(state)) {
                status = HttpServletResponse.SC_CONFLICT;
                mensaje = "El registro está relacionado con otros datos o no existe.";
            } else if ("23514".equals(state) || "22001".equals(state)) {
                status = HttpServletResponse.SC_BAD_REQUEST;
                mensaje = "Alguno de los datos enviados no es válido.";
            } else if ("P0001".equals(state) || state == null && sql.getCause() == null
                    && sql.getMessage() != null && !sql.getMessage().startsWith("La variable")
                    && !sql.getMessage().startsWith("No se encontró el controlador")) {
                status = HttpServletResponse.SC_CONFLICT;
                mensaje = sql.getMessage();
            } else {
                status = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
                mensaje = "No fue posible acceder a los datos. Comprueba el estado antes de reintentar.";
            }
        }
        if (status >= 500) LOGGER.log(Level.SEVERE, "Error en el backend", error);
        json(response, status, Map.of("mensaje", mensaje == null ? "Error inesperado." : mensaje));
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            prepararPeticion(request, response);
            metodoNoPermitido(response, "GET");
        } catch (Exception ex) {
            manejarError(response, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            prepararPeticion(request, response);
            metodoNoPermitido(response, "POST");
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

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            prepararPeticion(request, response);
            metodoNoPermitido(response, "PUT");
        } catch (Exception ex) {
            manejarError(response, ex);
        }
    }

    /** PUSH reservado: este módulo no define eventos ni sincronización adicionales. */
    protected void doPush(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            prepararPeticion(request, response);
            metodoNoPermitido(response, "PUSH");
        } catch (Exception ex) {
            manejarError(response, ex);
        }
    }
}
