package extra.servicio;

import dao.CajaDAO;
import modelos.Usuario;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;

public class CajaServicio {
    private final CajaDAO dao = new CajaDAO();

    public Map<String, Object> actual(Usuario usuario) throws SQLException {
        Integer turno = dao.obtenerTurnoAbierto(usuario.getIdSucursal());
        if (turno == null) return Map.of("abierta", false, "efectivoEsperado", BigDecimal.ZERO);
        return Map.of("abierta", true, "idTurno", turno,
                "efectivoEsperado", dao.obtenerEfectivoEsperado(turno));
    }

    public Map<String, Object> abrir(Usuario usuario, BigDecimal fondo) throws SQLException {
        int turno = dao.abrirCaja(usuario.getIdSucursal(), usuario.getIdUsuario(), fondo);
        return Map.of("mensaje", "Caja iniciada correctamente.", "idTurno", turno);
    }

    public Map<String, Object> movimiento(Usuario usuario, int turno, BigDecimal monto,
            String motivo, boolean entrada) throws SQLException {
        comprobarTurno(usuario, turno);
        if (entrada) dao.registrarEntrada(turno, usuario.getIdUsuario(), monto, motivo);
        else dao.registrarRetiro(turno, usuario.getIdUsuario(), monto, motivo);
        return Map.of("mensaje", entrada ? "Entrada registrada." : "Retiro registrado.");
    }

    public Map<String, Object> cerrar(Usuario usuario, int turno, BigDecimal contado) throws SQLException {
        comprobarTurno(usuario, turno);
        BigDecimal diferencia = dao.cerrarCaja(turno, usuario.getIdUsuario(), contado);
        return Map.of("mensaje", "Caja cerrada correctamente.", "idTurno", turno,
                "efectivoContado", contado, "efectivoEsperado", contado.subtract(diferencia),
                "diferencia", diferencia);
    }

    private void comprobarTurno(Usuario usuario, int turno) throws SQLException {
        Integer actual = dao.obtenerTurnoAbierto(usuario.getIdSucursal());
        if (actual == null || actual != turno) {
            throw new IllegalArgumentException("El turno cambió o ya está cerrado. Actualiza la caja.");
        }
    }
}
