package dao;

import extra.config.ConexionBD;
import modelos.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public List<Producto> buscarProductos(
            String busqueda, int idSucursal)
            throws SQLException {

        List<Producto> productos = new ArrayList<>();

        String sql = """
            SELECT
                p.id_producto,
                p.id_categoria,
                p.codigo_barras,
                p.nombre,
                c.nombre AS categoria,
                p.precio_compra,
                p.precio_venta,
                COALESCE(i.stock_actual, 0) AS stock_actual,
                COALESCE(i.stock_minimo, 0) AS stock_minimo,
                p.activo
            FROM productos p
            INNER JOIN categorias c
                ON p.id_categoria = c.id_categoria
            LEFT JOIN inventario_sucursal i
                ON p.id_producto = i.id_producto
                AND i.id_sucursal = ?
            WHERE p.activo = true
              AND c.activa = true
              AND (
                  LOWER(p.nombre) LIKE LOWER(?)
                  OR p.codigo_barras LIKE ?
              )
            ORDER BY p.nombre
            """;

        String filtro = "%" +
                (busqueda == null ? "" : busqueda.trim())
                + "%";
        try (Connection conexion =
                ConexionBD.obtenerConexion();
             PreparedStatement ps =
                conexion.prepareStatement(sql)) {
            ps.setInt(1, idSucursal);
            ps.setString(2, filtro);
            ps.setString(3, filtro);
            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Producto producto = new Producto();

                    producto.setIdProducto(
                        rs.getInt("id_producto"));

                    producto.setIdCategoria(
                        rs.getInt("id_categoria"));

                    producto.setCodigoBarras(
                        rs.getString("codigo_barras"));

                    producto.setNombre(
                        rs.getString("nombre"));

                    producto.setCategoria(
                        rs.getString("categoria"));

                    producto.setPrecioCompra(
                        rs.getBigDecimal("precio_compra"));

                    producto.setPrecioVenta(
                        rs.getBigDecimal("precio_venta"));

                    producto.setStockActual(
                        rs.getInt("stock_actual"));

                    producto.setStockMinimo(
                        rs.getInt("stock_minimo"));

                    producto.setActivo(
                        rs.getBoolean("activo"));

                    productos.add(producto);
                }
            }
        }
        return productos;
    }

    public void guardarProducto(
            Producto producto, int idSucursal)
            throws SQLException {
        if (producto == null || idSucursal <= 0) {
            throw new IllegalArgumentException(
                    "Producto o sucursal inválidos.");
        }

        String sqlProducto = """
            INSERT INTO productos (
                id_categoria,
                codigo_barras,
                nombre,
                precio_compra,
                precio_venta,
                activo
            )
            VALUES (?, ?, ?, ?, ?, true)
            RETURNING id_producto
            """;

        String sqlInventario = """
            INSERT INTO inventario_sucursal (
                id_sucursal,
                id_producto,
                stock_actual,
                stock_minimo
            )
            VALUES (?, ?, ?, ?)
            """;
        try (Connection conexion =
                ConexionBD.obtenerConexion()) {

            conexion.setAutoCommit(false);
            try {
                int idProducto;
                try (PreparedStatement ps =
                        conexion.prepareStatement(sqlProducto)) {
                    ps.setInt(1, producto.getIdCategoria());
                    ps.setString(2, producto.getCodigoBarras());
                    ps.setString(3, producto.getNombre());
                    ps.setBigDecimal(4, producto.getPrecioCompra());
                    ps.setBigDecimal(5, producto.getPrecioVenta());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException(
                                "No se pudo registrar el producto.");
                        }
                        idProducto = rs.getInt("id_producto");
                    }
                }
                try (PreparedStatement ps =
                        conexion.prepareStatement(sqlInventario)) {
                    ps.setInt(1, idSucursal);
                    ps.setInt(2, idProducto);
                    ps.setInt(3, producto.getStockActual());
                    ps.setInt(4, producto.getStockMinimo());
                    ps.executeUpdate();
                }

                conexion.commit();
            } catch (SQLException | RuntimeException e) {
                try {
                    conexion.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
    }
}
