package extra.servicio;

import dao.ProductoDAO;
import dao.VentaDAO;
import modelos.LineaTicket;
import modelos.Usuario;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class VentaServicio {
    public record Detalle(int idProducto, int cantidad) {}

    public Map<String, Object> registrar(Usuario usuario, List<Detalle> detalles,
            BigDecimal recibido, BigDecimal totalEsperado) throws SQLException {
        if (detalles.isEmpty() || detalles.size() > 200) {
            throw new IllegalArgumentException("El ticket debe tener de 1 a 200 conceptos.");
        }
        var ids = new HashSet<Integer>();
        var lineas = new ArrayList<LineaTicket>();
        var productos = new ProductoDAO().buscarProductos("", usuario.getIdSucursal());
        for (Detalle detalle : detalles) {
            if (detalle.cantidad() < 1 || !ids.add(detalle.idProducto())) {
                throw new IllegalArgumentException("Revisa las cantidades y los productos duplicados.");
            }
            var producto = productos.stream().filter(p -> p.getIdProducto() == detalle.idProducto())
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Producto no disponible."));
            if (detalle.cantidad() > producto.getStockActual()) {
                throw new IllegalArgumentException("Existencias insuficientes para " + producto.getNombre());
            }
            var linea = new LineaTicket(producto);
            linea.setCantidad(detalle.cantidad());
            lineas.add(linea);
        }
        BigDecimal total = lineas.stream().map(LineaTicket::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(totalEsperado) != 0) {
            throw new IllegalArgumentException("El precio cambió. Actualiza el ticket antes de cobrar.");
        }
        int id = new VentaDAO().registrarVenta(usuario.getIdSucursal(), usuario.getIdUsuario(),
                2, total, recibido, lineas);
        return Map.of("idVenta", id, "total", total, "recibido", recibido,
                "cambio", recibido.subtract(total), "mensaje", "Venta registrada correctamente.");
    }
}
