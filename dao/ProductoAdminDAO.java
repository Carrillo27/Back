package dao;

import extra.config.ConexionBD;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** Alta y mantenimiento de productos (incluye inactivos) para la pantalla de administración. */
public class ProductoAdminDAO {
    private static final String PRODUCTO = """
            SELECT p.id_producto AS "idProducto", p.codigo_barras AS "codigoBarras", p.nombre,
                   p.id_categoria AS "idCategoria", c.nombre AS categoria,
                   p.precio_compra AS "precioCompra", p.precio_venta AS "precioVenta",
                   COALESCE(i.stock_actual, 0) AS "stockActual", COALESCE(i.stock_minimo, 0) AS "stockMinimo",
                   p.activo,
                   (SELECT pp.id_proveedor FROM productos_proveedores pp WHERE pp.id_producto = p.id_producto
                    ORDER BY pp.id_proveedor LIMIT 1) AS "idProveedor",
                   (SELECT pr.nombre_empresa FROM productos_proveedores pp
                    INNER JOIN proveedores pr ON pr.id_proveedor = pp.id_proveedor
                    WHERE pp.id_producto = p.id_producto ORDER BY pp.id_proveedor LIMIT 1) AS proveedor
            FROM productos p
            INNER JOIN categorias c ON c.id_categoria = p.id_categoria
            LEFT JOIN inventario_sucursal i ON i.id_producto = p.id_producto AND i.id_sucursal = ?""";

    public List<Map<String, Object>> listar(int sucursal, String busqueda, Integer categoria) throws SQLException {
        String f = "%" + (busqueda == null ? "" : busqueda.trim()) + "%";
        return Jdbc.consultar(PRODUCTO + " " + """
                 WHERE (LOWER(p.nombre) LIKE LOWER(?) OR p.codigo_barras LIKE ?)
                   AND (CAST(? AS INTEGER) IS NULL OR p.id_categoria = CAST(? AS INTEGER))
                 ORDER BY p.nombre""", sucursal, f, f, categoria, categoria);
    }

    public Map<String, Object> obtener(int sucursal, int id) throws SQLException {
        return Jdbc.uno(PRODUCTO + " WHERE p.id_producto = ?", sucursal, id);
    }

    public int crear(int sucursal, int categoria, String codigo, String nombre, BigDecimal compra,
            BigDecimal venta, int stock, int minimo, Integer proveedor) throws SQLException {
        try (Connection c = ConexionBD.obtenerConexion()) {
            c.setAutoCommit(false);
            try {
                int id = (Integer) Jdbc.uno(c, """
                        INSERT INTO productos (id_categoria, codigo_barras, nombre, precio_compra, precio_venta, activo)
                        VALUES (?, ?, ?, ?, ?, true) RETURNING id_producto AS id""",
                        categoria, codigo, nombre, compra, venta).get("id");
                Jdbc.ejecutar(c, """
                        INSERT INTO inventario_sucursal (id_sucursal, id_producto, stock_actual, stock_minimo)
                        VALUES (?, ?, ?, ?)""", sucursal, id, stock, minimo);
                asignarProveedor(c, id, proveedor);
                c.commit();
                return id;
            } catch (SQLException | RuntimeException e) {
                deshacer(c, e);
                throw e;
            }
        }
    }

    public boolean actualizar(int sucursal, int id, int categoria, String codigo, String nombre, BigDecimal compra,
            BigDecimal venta, int minimo, Integer proveedor) throws SQLException {
        try (Connection c = ConexionBD.obtenerConexion()) {
            c.setAutoCommit(false);
            try {
                int filas = Jdbc.ejecutar(c, """
                        UPDATE productos SET id_categoria = ?, codigo_barras = ?, nombre = ?,
                            precio_compra = ?, precio_venta = ? WHERE id_producto = ?""",
                        categoria, codigo, nombre, compra, venta, id);
                if (filas == 0) {
                    c.rollback();
                    return false;
                }
                // Si la sucursal aún no tiene fila de inventario se crea sin existencias.
                Jdbc.ejecutar(c, """
                        INSERT INTO inventario_sucursal (id_sucursal, id_producto, stock_actual, stock_minimo)
                        VALUES (?, ?, 0, ?)
                        ON CONFLICT (id_sucursal, id_producto) DO UPDATE SET stock_minimo = EXCLUDED.stock_minimo""",
                        sucursal, id, minimo);
                asignarProveedor(c, id, proveedor);
                c.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                deshacer(c, e);
                throw e;
            }
        }
    }

    public boolean estado(int id, boolean activo) throws SQLException {
        return Jdbc.ejecutar("UPDATE productos SET activo = ? WHERE id_producto = ?", activo, id) > 0;
    }

    private static void asignarProveedor(Connection c, int producto, Integer proveedor) throws SQLException {
        Jdbc.ejecutar(c, "DELETE FROM productos_proveedores WHERE id_producto = ?", producto);
        if (proveedor != null) {
            Jdbc.ejecutar(c, "INSERT INTO productos_proveedores (id_producto, id_proveedor) VALUES (?, ?)",
                    producto, proveedor);
        }
    }

    private static void deshacer(Connection c, Exception e) {
        try {
            c.rollback();
        } catch (SQLException rollback) {
            e.addSuppressed(rollback);
        }
    }
}
