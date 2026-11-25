package modelos;

import java.io.Serializable;

/**
 * Datos de public.inventario_sucursal; las relaciones se representan mediante sus identificadores.
 * @see servlets.InventarioSucursalServlet
 */
public class InventarioSucursal implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Columna id_sucursal. */
    private int idSucursal;

    /** Columna id_producto. */
    private int idProducto;

    /** Columna stock_actual. */
    private int stockActual;

    /** Columna stock_minimo. */
    private int stockMinimo;

    public InventarioSucursal() {
    }

    public InventarioSucursal(int idSucursal,
            int idProducto,
            int stockActual,
            int stockMinimo) {
        this.idSucursal = idSucursal;
        this.idProducto = idProducto;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
    }

    public int getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(int idSucursal) {
        this.idSucursal = idSucursal;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public int getStockActual() {
        return stockActual;
    }

    public void setStockActual(int stockActual) {
        this.stockActual = stockActual;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

}
