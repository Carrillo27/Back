package dao;

import extra.config.ConexionBD;
import modelos.LineaTicket;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import extra.validacion.ValidacionVenta;
import java.util.List;

public class VentaDAO {

    public VentaDAO() {
    }
    // 1. REGISTRAR VENTA COMPLETA
    public int registrarVenta(
            int idSucursal,
            int idUsuario,
            int idMetodoPago,
            BigDecimal total,
            BigDecimal montoRecibido,
            List<LineaTicket> lineas
    ) throws SQLException {

        BigDecimal totalCalculado = ValidacionVenta.validar(
                idSucursal, idUsuario, idMetodoPago, total, montoRecibido, lineas);

        // Todas las operaciones utilizan
        // la misma conexión y transacción.
        try (Connection conexion =
                ConexionBD.obtenerConexion()) {

            conexion.setAutoCommit(false);
            try {

                // 1. Verificar pago en efectivo.
                validarMetodoPago(
                        conexion,
                        idMetodoPago
                );

                // 2. Obtener y bloquear la caja abierta.
                int idTurnoCaja = bloquearCajaAbierta(
                        conexion,
                        idSucursal,
                        idUsuario
                );

                validarPrecios(conexion, lineas);

                // 3. Descontar inventario.
                descontarInventario(
                        conexion,
                        idSucursal,
                        lineas
                );

                // 4. Registrar el encabezado
                // asociado al turno de caja.
                int idVenta = insertarEncabezado(
                        conexion,
                        idSucursal,
                        idUsuario,
                        idMetodoPago,
                        idTurnoCaja,
                        totalCalculado,
                        montoRecibido
                );

                // 5. Registrar los detalles.
                insertarDetalles(
                        conexion,
                        idVenta,
                        lineas
                );

                // 6. Registrar salidas de inventario.
                registrarMovimientosInventario(
                        conexion,
                        idVenta,
                        idSucursal,
                        idUsuario,
                        lineas
                );

                // 7. Registrar ingreso en caja.
                registrarIngresoCaja(
                        conexion,
                        idTurnoCaja,
                        idUsuario,
                        idVenta,
                        totalCalculado
                );

                // 8. Confirmar toda la operación.
                conexion.commit();
                return idVenta;
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
    private void validarPrecios(Connection conexion, List<LineaTicket> lineas) throws SQLException {
        String sql = """
            SELECT p.precio_venta FROM productos p
            JOIN categorias c ON c.id_categoria = p.id_categoria
            WHERE p.id_producto = ? AND p.activo AND c.activa
            FOR SHARE OF p, c
            """;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            for (LineaTicket linea : lineas) {
                ps.setInt(1, linea.getIdProducto());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || rs.getBigDecimal(1).compareTo(linea.getPrecioUnitario()) != 0) {
                        throw new SQLException("El producto o su precio cambió. Actualiza el ticket.");
                    }
                }
            }
        }
    }
    // 2. VALIDAR METODO DE PAGO
    private void validarMetodoPago(
            Connection conexion,
            int idMetodoPago
    ) throws SQLException {

        String sql = """
            SELECT id_metodo
            FROM metodos_pago
            WHERE id_metodo = ?
              AND tipo = 'Efectivo'
              AND activo = TRUE
            """;
        try (PreparedStatement ps =
                conexion.prepareStatement(sql)) {
            ps.setInt(1, idMetodoPago);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException(
                            "El método de pago en efectivo "
                            + "no está disponible."
                    );
                }
            }
        }
    }
    // 3. BLOQUEAR CAJA ABIERTA
    private int bloquearCajaAbierta(
            Connection conexion,
            int idSucursal,
            int idUsuario
    ) throws SQLException {

        String sql = """
            SELECT t.id_turno_caja
            FROM turnos_caja t
            INNER JOIN usuarios u
                ON u.id_sucursal = t.id_sucursal
            WHERE t.id_sucursal = ?
              AND t.estado = 'Abierta'
              AND u.id_usuario = ?
              AND u.activo = TRUE
            FOR UPDATE OF t
            """;
        try (PreparedStatement ps =
                conexion.prepareStatement(sql)) {
            ps.setInt(1, idSucursal);
            ps.setInt(2, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_turno_caja");
                }
                throw new SQLException(
                        "No hay una caja abierta para "
                        + "esta sucursal o el usuario "
                        + "no está autorizado."
                );
            }
        }
    }
    // 4. INSERTAR ENCABEZADO
    private int insertarEncabezado(
            Connection conexion,
            int idSucursal,
            int idUsuario,
            int idMetodoPago,
            int idTurnoCaja,
            BigDecimal total,
            BigDecimal montoRecibido
    ) throws SQLException {

        String sql = """
            INSERT INTO ventas (
                id_sucursal,
                id_usuario,
                id_metodo_pago,
                id_turno_caja,
                porcentaje_impuesto,
                subtotal,
                total,
                monto_recibido,
                estado
            )
            VALUES (?, ?, ?, ?, 0, ?, ?, ?, 'Cobrado')
            RETURNING id_venta
            """;
        try (PreparedStatement ps =
                conexion.prepareStatement(sql)) {
            ps.setInt(1, idSucursal);
            ps.setInt(2, idUsuario);
            ps.setInt(3, idMetodoPago);
            ps.setInt(4, idTurnoCaja);
            ps.setBigDecimal(5, total);
            ps.setBigDecimal(6, total);
            ps.setBigDecimal(7, montoRecibido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_venta");
                }
                throw new SQLException("No se pudo obtener el ID de la venta.");
            }
        }
    }
    // 5. INSERTAR DETALLES
    private void insertarDetalles(
            Connection conexion,
            int idVenta,
            List<LineaTicket> lineas
    ) throws SQLException {

        String sql = """
            INSERT INTO detalle_ventas_productos (
                id_venta,
                id_producto,
                cantidad,
                precio_unitario
            )
            VALUES (?, ?, ?, ?)
            """;
        try (PreparedStatement ps =
                conexion.prepareStatement(sql)) {

            for (LineaTicket linea : lineas) {
                ps.setInt(1, idVenta);
                ps.setInt(2, linea.getIdProducto());
                ps.setInt(3, linea.getCantidad());
                ps.setBigDecimal(
                        4,
                        linea.getPrecioUnitario()
                );
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
    // 6. DESCONTAR INVENTARIO
    private void descontarInventario(
            Connection conexion,
            int idSucursal,
            List<LineaTicket> lineas
    ) throws SQLException {

        String sql = """
            UPDATE inventario_sucursal
            SET stock_actual = stock_actual - ?
            WHERE id_sucursal = ?
              AND id_producto = ?
              AND stock_actual >= ?
            """;
        try (PreparedStatement ps =
                conexion.prepareStatement(sql)) {

            for (LineaTicket linea : lineas) {

                int cantidad = linea.getCantidad();
                ps.setInt(1, cantidad);
                ps.setInt(2, idSucursal);
                ps.setInt(3, linea.getIdProducto());
                ps.setInt(4, cantidad);

                int filasActualizadas =
                        ps.executeUpdate();
                if (filasActualizadas != 1) {
                    throw new SQLException(
                            "No hay existencias suficientes para: "
                            + linea.getNombre()
                    );
                }
            }
        }
    }
    // 7. REGISTRAR MOVIMIENTOS DE INVENTARIO
    private void registrarMovimientosInventario(
            Connection conexion,
            int idVenta,
            int idSucursal,
            int idUsuario,
            List<LineaTicket> lineas
    ) throws SQLException {

        String sql = """
            INSERT INTO movimientos_inventario (
                id_sucursal,
                id_producto,
                id_usuario,
                id_tipo_movimiento,
                origen_movimiento,
                cantidad_afectada
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps =
                conexion.prepareStatement(sql)) {

            for (LineaTicket linea : lineas) {
                ps.setInt(1, idSucursal);
                ps.setInt(2, linea.getIdProducto());
                ps.setInt(3, idUsuario);

                // Tipo 1: salida por venta.
                ps.setInt(4, 1);
                ps.setString(
                        5,
                        "VENTA-" + idVenta
                );
                ps.setInt(
                        6,
                        linea.getCantidad()
                );
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
    // 8. REGISTRAR INGRESO EN CAJA
    private void registrarIngresoCaja(
            Connection conexion,
            int idTurnoCaja,
            int idUsuario,
            int idVenta,
            BigDecimal totalVenta
    ) throws SQLException {

        String sql = """
            INSERT INTO movimientos_caja (
                id_turno_caja,
                id_usuario,
                id_venta,
                tipo,
                monto,
                concepto
            )
            VALUES (?, ?, ?, 'VENTA', ?, ?)
            """;
        try (PreparedStatement ps =
                conexion.prepareStatement(sql)) {
            ps.setInt(1, idTurnoCaja);
            ps.setInt(2, idUsuario);
            ps.setInt(3, idVenta);
            ps.setBigDecimal(4, totalVenta);
            ps.setString(
                    5,
                    "Ingreso por venta " + idVenta
            );

            int filasInsertadas = ps.executeUpdate();
            if (filasInsertadas != 1) {
                throw new SQLException(
                        "No se pudo registrar "
                        + "el ingreso de la venta en caja."
                );
            }
        }
    }
}
