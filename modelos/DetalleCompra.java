package modelos;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Datos de public.detalle_compras; las relaciones se representan mediante sus identificadores.
 * @see servlets.DetalleComprasServlet
 */
public class DetalleCompra implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Columna id_detalle_compra. */
    private int idDetalleCompra;

    /** Columna id_compra. */
    private int idCompra;

    /** Columna id_producto. */
    private int idProducto;

    /** Columna cantidad. */
    private int cantidad;

    /** Columna costo_unitario. */
    private BigDecimal costoUnitario;

    public DetalleCompra() {
    }

    public DetalleCompra(int idDetalleCompra,
            int idCompra,
            int idProducto,
            int cantidad,
            BigDecimal costoUnitario) {
        this.idDetalleCompra = idDetalleCompra;
        this.idCompra = idCompra;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
    }

    public int getIdDetalleCompra() {
        return idDetalleCompra;
    }

    public void setIdDetalleCompra(int idDetalleCompra) {
        this.idDetalleCompra = idDetalleCompra;
    }

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
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

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

}
