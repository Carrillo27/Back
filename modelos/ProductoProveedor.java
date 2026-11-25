package modelos;

import java.io.Serializable;

/**
 * Datos de public.productos_proveedores; las relaciones se representan mediante sus identificadores.
 * @see servlets.ProductosProveedoresServlet
 */
public class ProductoProveedor implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Columna id_producto. */
    private int idProducto;

    /** Columna id_proveedor. */
    private int idProveedor;

    public ProductoProveedor() {
    }

    public ProductoProveedor(int idProducto,
            int idProveedor) {
        this.idProducto = idProducto;
        this.idProveedor = idProveedor;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

}
