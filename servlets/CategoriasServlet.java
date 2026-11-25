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

/** Tabla public.categorias; modelo correspondiente: {@link modelos.Categoria}. */
@WebServlet(name = "CategoriasServlet", urlPatterns = "/api/categorias/*")
public class CategoriasServlet extends JsonServlet {
    private final CatalogoDAO dao = new CatalogoDAO();
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            prepararPeticion(req, res);
            if (segmentos(req).length != 0) { json(res, 404, Map.of("mensaje", "La ruta solicitada no existe.")); return; }
            ok(res, dao.categorias(Respuestas.busqueda(req.getParameter("busqueda"))));
        } catch (Exception ex) {
            manejarError(res, ex);
        }
    }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            prepararPeticion(req, res);
            if (segmentos(req).length != 0) { json(res, 404, Map.of("mensaje", "La ruta solicitada no existe.")); return; }
            JsonObject b = leerObjeto(req);
            int id = dao.crearCategoria(EntradaJson.texto(b, "nombre", 100), EntradaJson.textoOpcional(b, "descripcion", 255));
            ok(res, dao.categoria(id));
        } catch (Exception ex) {
            manejarError(res, ex);
        }
    }
    @Override protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            prepararPeticion(req, res);
            String[] r=segmentos(req); if (r.length < 1 || r.length > 2) { json(res,404,Map.of("mensaje","La ruta solicitada no existe.")); return; }
            int id=id(r[0]); JsonObject b=leerObjeto(req); boolean actualizado;
            if (r.length==2 && "estado".equals(r[1])) actualizado=dao.estadoCategoria(id, EntradaJson.booleano(b,"activa"));
            else if (r.length==1) actualizado=dao.actualizarCategoria(id, EntradaJson.texto(b,"nombre",100), EntradaJson.textoOpcional(b,"descripcion",255));
            else { json(res,404,Map.of("mensaje","La ruta solicitada no existe.")); return; }
            Respuestas.encontrado(actualizado); ok(res,dao.categoria(id));
        } catch (Exception ex) {
            manejarError(res, ex);
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
