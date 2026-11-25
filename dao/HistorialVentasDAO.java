package dao;

import extra.seguridad.CifradoDatos;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Consulta de ventas de la sucursal y su detalle. */
public class HistorialVentasDAO {
    public List<Map<String, Object>> listar(int sucursal, LocalDate desde, LocalDate hasta, String estado)
            throws SQLException {
        return CifradoDatos.ventas(Jdbc.consultar("""
                SELECT v.id_venta AS "idVenta", v.fecha_hora AS "fechaHora", u.username AS usuario,
                       mp.nombre AS "metodoPago", v.subtotal, v.total, v.estado
                FROM ventas v
                INNER JOIN usuarios u ON u.id_usuario = v.id_usuario
                LEFT JOIN metodos_pago mp ON mp.id_metodo = v.id_metodo_pago
                WHERE v.id_sucursal = ?
                  AND v.fecha_hora >= CAST(? AS DATE)
                  AND v.fecha_hora < CAST(? AS DATE) + 1
                  AND (CAST(? AS VARCHAR) IS NULL OR v.estado = CAST(? AS VARCHAR))
                ORDER BY v.fecha_hora DESC, v.id_venta DESC
                LIMIT 500""", sucursal, desde, hasta, estado, estado));
    }

    public Map<String, Object> venta(int sucursal, int id) throws SQLException {
        return CifradoDatos.venta(Jdbc.uno("""
                SELECT v.id_venta AS "idVenta", v.fecha_hora AS "fechaHora", u.username AS usuario,
                       mp.nombre AS "metodoPago", v.porcentaje_impuesto AS "porcentajeImpuesto", v.subtotal,
                       v.total, v.monto_recibido AS "montoRecibido", v.folio_pago AS "folioPago", v.estado
                FROM ventas v
                INNER JOIN usuarios u ON u.id_usuario = v.id_usuario
                LEFT JOIN metodos_pago mp ON mp.id_metodo = v.id_metodo_pago
                WHERE v.id_sucursal = ? AND v.id_venta = ?""", sucursal, id));
    }

    public List<Map<String, Object>> lineas(int id) throws SQLException {
        return Jdbc.consultar("""
                SELECT 'Producto' AS tipo, p.nombre AS descripcion, d.cantidad::numeric AS cantidad,
                       d.precio_unitario AS "precioUnitario", d.cantidad * d.precio_unitario AS importe
                FROM detalle_ventas_productos d INNER JOIN productos p ON p.id_producto = d.id_producto
                WHERE d.id_venta = ?
                UNION ALL
                SELECT 'Servicio', s.tipo_servicio || ' ' || s.color || ' ' || s.tamano
                       || CASE WHEN s.doble_cara THEN ' doble cara' ELSE '' END,
                       d.cantidad, d.precio_unitario, d.cantidad * d.precio_unitario
                FROM detalle_ventas_servicios d INNER JOIN servicios_precios s ON s.id_servicio = d.id_servicio
                WHERE d.id_venta = ?
                ORDER BY 1, 2""", id, id);
    }
}
