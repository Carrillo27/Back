package servlets;

import extra.http.ApiException;
import extra.http.EntradaJson;
import extra.http.Respuestas;
import extra.http.SesionServlet;

import dao.ProductoAdminDAO;
import dao.ProductoDAO;
import modelos.Usuario;
import jakarta.json.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/** Tabla public.productos; modelo correspondiente: {@link modelos.Producto}. */
@WebServlet(name = "ProductosServlet", urlPatterns = "/api/productos/*")
public class ProductosServlet extends JsonServlet {
    private final ProductoAdminDAO admin = new ProductoAdminDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            prepararPeticion(request, response);
            Usuario usuario = SesionServlet.usuario(request);
            String[] ruta = segmentos(request);
            if (ruta.length == 0) {
                String busqueda = request.getParameter("busqueda");
                if (busqueda == null) busqueda = "";
                if (busqueda.length() > 120) throw new ApiException(400, "La búsqueda es demasiado larga.");
                ok(response, new ProductoDAO().buscarProductos(busqueda, usuario.getIdSucursal()));
            } else if (ruta.length == 1 && "administracion".equals(ruta[0])) {
                ok(response, admin.listar(usuario.getIdSucursal(), Respuestas.busqueda(request.getParameter("busqueda")),
                        enteroOpcional(request.getParameter("categoria"))));
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
            JsonObject b = leerObjeto(request);
            int id = admin.crear(SesionServlet.usuario(request).getIdSucursal(), EntradaJson.entero(b, "idCategoria"),
                    EntradaJson.texto(b, "codigoBarras", 50), EntradaJson.texto(b, "nombre", 150),
                    EntradaJson.importe(b, "precioCompra", false), EntradaJson.importe(b, "precioVenta", true),
                    cantidad(b, "stockActual"), cantidad(b, "stockMinimo"), proveedor(b));
            ok(response, admin.obtener(SesionServlet.usuario(request).getIdSucursal(), id));
        } catch (Exception ex) {
            manejarError(response, ex);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            prepararPeticion(request, response);
            String[] ruta = segmentos(request); int id = id(ruta.length == 0 ? "" : ruta[0]);
            JsonObject b = leerObjeto(request); int sucursal = SesionServlet.usuario(request).getIdSucursal();
            if (ruta.length == 2 && "estado".equals(ruta[1])) {
                Respuestas.encontrado(admin.estado(id, EntradaJson.booleano(b, "activo")));
            } else if (ruta.length == 1) {
                Respuestas.encontrado(admin.actualizar(sucursal, id, EntradaJson.entero(b, "idCategoria"),
                        EntradaJson.texto(b, "codigoBarras", 50), EntradaJson.texto(b, "nombre", 150),
                        EntradaJson.importe(b, "precioCompra", false), EntradaJson.importe(b, "precioVenta", true),
                        cantidad(b, "stockMinimo"), proveedor(b)));
            } else {
                json(response, HttpServletResponse.SC_NOT_FOUND, Map.of("mensaje", "La ruta solicitada no existe."));
                return;
            }
            ok(response, admin.obtener(sucursal, id));
        } catch (Exception ex) {
            manejarError(response, ex);
        }
    }

    private static Integer enteroOpcional(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try { return Integer.valueOf(valor); }
        catch (NumberFormatException ex) { throw new ApiException(400, "Categoría inválida."); }
    }
    private static int cantidad(JsonObject b, String campo) {
        int valor = EntradaJson.entero(b, campo);
        if (valor < 0) throw new ApiException(400, "El campo " + campo + " no puede ser negativo.");
        return valor;
    }
    private static Integer proveedor(JsonObject b) {
        return b != null && b.get("idProveedor") != null && !b.isNull("idProveedor")
                ? EntradaJson.entero(b, "idProveedor") : null;
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
