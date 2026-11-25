package dao;

import extra.config.ConexionBD;

import java.math.BigDecimal;
import extra.validacion.ValidacionImporte;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CajaDAO {

    public CajaDAO() {
    }
    // 1. CONSULTAR CAJA ABIERTA
    public Integer obtenerTurnoAbierto(
            int idSucursal
    ) throws SQLException {
        if (idSucursal <= 0) {
            throw new SQLException("La sucursal no es válida.");
        }

        String sql = """
            SELECT id_turno_caja
            FROM turnos_caja
            WHERE id_sucursal = ?
              AND estado = 'Abierta'
            """;
        try (Connection conexion =
                ConexionBD.obtenerConexion();
             PreparedStatement ps =
                conexion.prepareStatement(sql)) {
            ps.setInt(1, idSucursal);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_turno_caja");
                }
                return null;
            }
        }
    }
    // 2. ABRIR CAJA
    public int abrirCaja(
            int idSucursal,
            int idUsuario,
            BigDecimal fondoInicial
    ) throws SQLException {

        validarImporte(
                fondoInicial,
                false
        );

        String sql = """
            INSERT INTO turnos_caja (
                id_sucursal,
                id_usuario_apertura,
                fondo_inicial,
                estado
            )
            SELECT ?, ?, ?, 'Abierta'
            WHERE EXISTS (
                SELECT 1
                FROM usuarios u
                INNER JOIN sucursales s
                    ON s.id_sucursal = u.id_sucursal
                WHERE u.id_usuario = ?
                  AND u.id_sucursal = ?
                  AND u.activo = TRUE
                  AND s.activa = TRUE
            )
            RETURNING id_turno_caja
            """;
        try (Connection conexion =
                ConexionBD.obtenerConexion();
             PreparedStatement ps =
                conexion.prepareStatement(sql)) {
            ps.setInt(1, idSucursal);
            ps.setInt(2, idUsuario);
            ps.setBigDecimal(3, fondoInicial);
            ps.setInt(4, idUsuario);
            ps.setInt(5, idSucursal);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_turno_caja");
                }
                throw new SQLException(
                        "No se puede abrir la caja. "
                        + "Verifica el usuario y la sucursal."
                );
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new SQLException(
                        "Ya existe una caja abierta "
                        + "para esta sucursal.",
                        "P0001",
                        e
                );
            }
            throw e;
        }
    }
    // 3. CONSULTAR EFECTIVO ESPERADO
    public BigDecimal obtenerEfectivoEsperado(
            int idTurnoCaja
    ) throws SQLException {
        try (Connection conexion =
                ConexionBD.obtenerConexion()) {
            return calcularEfectivoEsperado(
                    conexion,
                    idTurnoCaja
            );
        }
    }

    private BigDecimal calcularEfectivoEsperado(
            Connection conexion,
            int idTurnoCaja
    ) throws SQLException {

        String sql = """
            SELECT
                t.fondo_inicial
                + COALESCE(
                    SUM(
                        CASE
                            WHEN m.tipo IN ('VENTA', 'ENTRADA')
                                THEN m.monto
                            WHEN m.tipo = 'RETIRO'
                                THEN -m.monto
                            ELSE 0
                        END
                    ),
                    0
                  ) AS efectivo_esperado
            FROM turnos_caja t
            LEFT JOIN movimientos_caja m
                ON m.id_turno_caja = t.id_turno_caja
            WHERE t.id_turno_caja = ?
            GROUP BY
                t.id_turno_caja,
                t.fondo_inicial
            """;
        try (PreparedStatement ps =
                conexion.prepareStatement(sql)) {
            ps.setInt(1, idTurnoCaja);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("efectivo_esperado");
                }
                throw new SQLException("No se encontró el turno de caja.");
            }
        }
    }
    // 4. REGISTRAR ENTRADA DE EFECTIVO
    public void registrarEntrada(
            int idTurnoCaja,
            int idUsuario,
            BigDecimal monto,
            String concepto
    ) throws SQLException {

        registrarMovimientoManual(
                idTurnoCaja,
                idUsuario,
                "ENTRADA",
                monto,
                concepto
        );
    }
    // 5. REGISTRAR RETIRO DE EFECTIVO
    public void registrarRetiro(
            int idTurnoCaja,
            int idUsuario,
            BigDecimal monto,
            String concepto
    ) throws SQLException {

        registrarMovimientoManual(
                idTurnoCaja,
                idUsuario,
                "RETIRO",
                monto,
                concepto
        );
    }
    // 6. REGISTRO DE MOVIMIENTOS MANUALES
    private void registrarMovimientoManual(
            int idTurnoCaja,
            int idUsuario,
            String tipo,
            BigDecimal monto,
            String concepto
    ) throws SQLException {

        validarImporte(monto, true);
        if (concepto == null
                || concepto.isBlank()
                || concepto.trim().length() > 200) {
            throw new SQLException(
                    "El concepto debe contener entre "
                    + "1 y 200 caracteres."
            );
        }

        String sql = """
            INSERT INTO movimientos_caja (
                id_turno_caja,
                id_usuario,
                tipo,
                monto,
                concepto
            )
            VALUES (?, ?, ?, ?, ?)
            """;
        try (Connection conexion =
                ConexionBD.obtenerConexion()) {

            conexion.setAutoCommit(false);
            try {

                int idSucursal = bloquearCajaAbierta(
                        conexion,
                        idTurnoCaja
                );

                validarUsuarioSucursal(
                        conexion,
                        idUsuario,
                        idSucursal
                );

                // Evitar retiros superiores al
                // efectivo esperado.
                if ("RETIRO".equals(tipo)) {

                    BigDecimal disponible =
                            calcularEfectivoEsperado(
                                    conexion,
                                    idTurnoCaja
                            );
                    if (monto.compareTo(disponible) > 0) {
                        throw new SQLException(
                                "El retiro supera el "
                                + "efectivo disponible."
                        );
                    }
                }
                try (PreparedStatement ps =
                        conexion.prepareStatement(sql)) {
                    ps.setInt(1, idTurnoCaja);
                    ps.setInt(2, idUsuario);
                    ps.setString(3, tipo);
                    ps.setBigDecimal(4, monto);
                    ps.setString(5, concepto.trim());
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
    // 7. CERRAR CAJA
    public BigDecimal cerrarCaja(
            int idTurnoCaja,
            int idUsuario,
            BigDecimal efectivoContado
    ) throws SQLException {

        validarImporte(
                efectivoContado,
                false
        );

        String sql = """
            UPDATE turnos_caja
            SET estado = 'Cerrada',
                fecha_cierre = CURRENT_TIMESTAMP,
                id_usuario_cierre = ?,
                efectivo_esperado = ?,
                efectivo_contado = ?,
                diferencia = ?
            WHERE id_turno_caja = ?
              AND estado = 'Abierta'
            """;
        try (Connection conexion =
                ConexionBD.obtenerConexion()) {

            conexion.setAutoCommit(false);
            try {

                int idSucursal = bloquearCajaAbierta(
                        conexion,
                        idTurnoCaja
                );

                validarUsuarioSucursal(
                        conexion,
                        idUsuario,
                        idSucursal
                );

                BigDecimal efectivoEsperado =
                        calcularEfectivoEsperado(
                                conexion,
                                idTurnoCaja
                        );

                BigDecimal diferencia =
                        efectivoContado.subtract(efectivoEsperado);
                try (PreparedStatement ps =
                        conexion.prepareStatement(sql)) {
                    ps.setInt(1, idUsuario);
                    ps.setBigDecimal(
                            2,
                            efectivoEsperado
                    );
                    ps.setBigDecimal(
                            3,
                            efectivoContado
                    );
                    ps.setBigDecimal(
                            4,
                            diferencia
                    );
                    ps.setInt(5, idTurnoCaja);

                    int filas = ps.executeUpdate();
                    if (filas != 1) {
                        throw new SQLException("No se pudo cerrar la caja.");
                    }
                }

                conexion.commit();
                return diferencia;
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
    // 8. BLOQUEAR Y VALIDAR CAJA ABIERTA
    private int bloquearCajaAbierta(
            Connection conexion,
            int idTurnoCaja
    ) throws SQLException {

        String sql = """
            SELECT id_sucursal
            FROM turnos_caja
            WHERE id_turno_caja = ?
              AND estado = 'Abierta'
            FOR UPDATE
            """;
        try (PreparedStatement ps =
                conexion.prepareStatement(sql)) {
            ps.setInt(1, idTurnoCaja);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_sucursal");
                }
                throw new SQLException("La caja no existe o ya está cerrada.");
            }
        }
    }
    // 9. VALIDAR USUARIO Y SUCURSAL
    private void validarUsuarioSucursal(
            Connection conexion,
            int idUsuario,
            int idSucursal
    ) throws SQLException {

        String sql = """
            SELECT u.id_usuario
            FROM usuarios u
            INNER JOIN sucursales s
                ON s.id_sucursal = u.id_sucursal
            WHERE u.id_usuario = ?
              AND u.id_sucursal = ?
              AND u.activo = TRUE
              AND s.activa = TRUE
            """;
        try (PreparedStatement ps =
                conexion.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idSucursal);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException(
                            "El usuario no está activo "
                            + "o no pertenece a la sucursal."
                    );
                }
            }
        }
    }
    // 10. VALIDAR IMPORTES
    private void validarImporte(BigDecimal importe, boolean debeSerPositivo) throws SQLException {
        String error = ValidacionImporte.error(importe, debeSerPositivo);
        if (error != null) throw new SQLException(error);
    }
}
