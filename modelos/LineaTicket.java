package modelos;

import java.io.Serializable;
import java.math.BigDecimal;

public class LineaTicket implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idProducto;
    private String codigoBarras;
    private String nombre;
    private BigDecimal precioUnitario;
    private int cantidad;
    private int stockDisponible;

    public LineaTicket() {
    }

    public LineaTicket(Producto producto) {
        this.idProducto = producto.getIdProducto();
        this.codigoBarras = producto.getCodigoBarras();
        this.nombre = producto.getNombre();
        this.precioUnitario = producto.getPrecioVenta();
        this.cantidad = 1;
        this.stockDisponible = producto.getStockActual();
    }

    public LineaTicket(LineaTicket origen) {
        this.idProducto = origen.idProducto;
        this.codigoBarras = origen.codigoBarras;
        this.nombre = origen.nombre;
        this.precioUnitario = origen.precioUnitario;
        this.cantidad = origen.cantidad;
        this.stockDisponible = origen.stockDisponible;
    }

    public BigDecimal getSubtotal() {
        if (precioUnitario == null) {
            return BigDecimal.ZERO;
        }
        return precioUnitario.multiply(
                BigDecimal.valueOf(cantidad));
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getStockDisponible() {
        return stockDisponible;
    }

    public void setStockDisponible(int stockDisponible) {
        this.stockDisponible = stockDisponible;
    }
}
