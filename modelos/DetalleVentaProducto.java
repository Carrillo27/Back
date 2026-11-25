package modelos;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Datos de public.detalle_ventas_productos; las relaciones se representan mediante sus identificadores.
 * @see servlets.DetalleVentasProductosServlet
 */
public class DetalleVentaProducto implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Columna id_detalle. */
    private int idDetalle;

    /** Columna id_venta. */
    private int idVenta;

    /** Columna id_producto. */
    private int idProducto;

    /** Columna cantidad. */
    private int cantidad;

    /** Columna precio_unitario. */
    private BigDecimal precioUnitario;

    public DetalleVentaProducto() {
    }

    public DetalleVentaProducto(int idDetalle,
            int idVenta,
            int idProducto,
            int cantidad,
            BigDecimal precioUnitario) {
        this.idDetalle = idDetalle;
        this.idVenta = idVenta;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

}
