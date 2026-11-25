package modelos;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Datos de public.detalle_ventas_servicios; las relaciones se representan mediante sus identificadores.
 * @see servlets.DetalleVentasServiciosServlet
 */
public class DetalleVentaServicio implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Columna id_detalle. */
    private int idDetalle;

    /** Columna id_venta. */
    private int idVenta;

    /** Columna id_servicio. */
    private int idServicio;

    /** Columna cantidad. */
    private BigDecimal cantidad;

    /** Columna precio_unitario. */
    private BigDecimal precioUnitario;

    public DetalleVentaServicio() {
    }

    public DetalleVentaServicio(int idDetalle,
            int idVenta,
            int idServicio,
            BigDecimal cantidad,
            BigDecimal precioUnitario) {
        this.idDetalle = idDetalle;
        this.idVenta = idVenta;
        this.idServicio = idServicio;
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

    public int getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(int idServicio) {
        this.idServicio = idServicio;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

}
