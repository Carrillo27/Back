package extra.validacion;

import modelos.LineaTicket;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Reglas del ticket antes de iniciar la transacción de venta. */
public final class ValidacionVenta {
    private ValidacionVenta() { }

    public static BigDecimal validar(int idSucursal, int idUsuario, int idMetodoPago,
            BigDecimal total, BigDecimal montoRecibido, List<LineaTicket> lineas)
            throws SQLException {
        if (idSucursal <= 0 || idUsuario <= 0) {
            throw new SQLException("El usuario o la sucursal no son válidos.");
        }
        if (idMetodoPago <= 0) {
            throw new SQLException("El método de pago no es válido.");
        }
        if (lineas == null || lineas.isEmpty()) {
            throw new SQLException("No se puede registrar una venta sin productos.");
        }

        BigDecimal totalCalculado = BigDecimal.ZERO;
        Set<Integer> productosRegistrados = new HashSet<>();

        for (LineaTicket linea : lineas) {
            if (linea == null) {
                throw new SQLException("El ticket contiene una línea inválida.");
            }
            if (linea.getIdProducto() <= 0) {
                throw new SQLException("El ticket contiene un producto inválido.");
            }
            if (!productosRegistrados.add(
                    linea.getIdProducto())) {
                throw new SQLException("El ticket contiene productos duplicados.");
            }
            if (linea.getCantidad() <= 0) {
                throw new SQLException(
                        "Cantidad inválida para: "
                        + linea.getNombre()
                );
            }

            BigDecimal precio = linea.getPrecioUnitario();
            if (precio == null
                    || precio.compareTo(BigDecimal.ZERO) < 0
                    || precio.scale() > 2) {
                throw new SQLException(
                        "Precio inválido para: "
                        + linea.getNombre()
                );
            }
            totalCalculado = totalCalculado.add(
                    precio.multiply(
                            BigDecimal.valueOf(
                                    linea.getCantidad()
                            )
                    )
            );
        }
        if (total == null
                || total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new SQLException("El total de la venta debe ser mayor que cero.");
        }
        if (total.compareTo(totalCalculado) != 0) {
            throw new SQLException("El total de la venta no coincide con el ticket.");
        }
        if (total.scale() > 2) {
            throw new SQLException("El total debe tener máximo dos decimales.");
        }
        if (montoRecibido == null) {
            throw new SQLException("Debes ingresar el dinero recibido.");
        }
        if (montoRecibido.compareTo(BigDecimal.ZERO) < 0) {
            throw new SQLException("El dinero recibido no puede ser negativo.");
        }
        if (montoRecibido.scale() > 2) {
            throw new SQLException("El importe recibido debe tener máximo dos decimales.");
        }
        if (montoRecibido.compareTo(totalCalculado) < 0) {
            throw new SQLException("El dinero recibido es insuficiente.");
        }
        return totalCalculado;
    }
}
