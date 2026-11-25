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

/** Tabla public.proveedores; modelo correspondiente: {@link modelos.Proveedor}. */
@WebServlet(name = "ProveedoresServlet", urlPatterns = "/api/proveedores/*")
public class ProveedoresServlet extends JsonServlet {
    private final CatalogoDAO dao = new CatalogoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            prepararPeticion(request, response);
            if (segmentos(request).length != 0) {
                json(response, 404, Map.of("mensaje", "La ruta solicitada no existe."));
                return;
            }
            ok(response, dao.proveedores(Respuestas.busqueda(request.getParameter("busqueda"))));
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
            int id = dao.crearProveedor(EntradaJson.texto(body, "nombreEmpresa", 120),
                    EntradaJson.textoOpcional(body, "contactoNombres", 100),
                    EntradaJson.textoOpcional(body, "contactoApellidoPaterno", 100),
                    EntradaJson.textoOpcional(body, "contactoApellidoMaterno", 100),
                    EntradaJson.textoOpcional(body, "telefono", 30), correo(body),
                    EntradaJson.textoOpcional(body, "direccion", 255));
            ok(response, dao.proveedor(id));
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
                actualizado = dao.estadoProveedor(id, EntradaJson.booleano(body, "activo"));
            } else if (ruta.length == 1) {
                actualizado = dao.actualizarProveedor(id, EntradaJson.texto(body, "nombreEmpresa", 120),
                        EntradaJson.textoOpcional(body, "contactoNombres", 100),
                        EntradaJson.textoOpcional(body, "contactoApellidoPaterno", 100),
                        EntradaJson.textoOpcional(body, "contactoApellidoMaterno", 100),
                        EntradaJson.textoOpcional(body, "telefono", 30), correo(body),
                        EntradaJson.textoOpcional(body, "direccion", 255));
            } else {
                json(response, 404, Map.of("mensaje", "La ruta solicitada no existe."));
                return;
            }
            Respuestas.encontrado(actualizado);
            ok(response, dao.proveedor(id));
        } catch (Exception ex) {
            manejarError(response, ex);
        }
    }

    private static String correo(JsonObject body) {
        String correo = EntradaJson.textoOpcional(body, "correo", 120);
        if (correo != null && !correo.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new ApiException(400, "Revisa el campo correo.");
        }
        return correo;
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
