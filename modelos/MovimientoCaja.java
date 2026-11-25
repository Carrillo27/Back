package modelos;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Datos de public.movimientos_caja; las relaciones se representan mediante sus identificadores.
 * @see servlets.MovimientosCajaServlet
 */
public class MovimientoCaja implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Columna id_movimiento_caja. */
    private int idMovimientoCaja;

    /** Columna id_turno_caja. */
    private int idTurnoCaja;

    /** Columna id_usuario. */
    private int idUsuario;

    /** Columna id_venta. */
    private Integer idVenta;

    /** Columna tipo. */
    private String tipo;

    /** Columna monto. */
    private BigDecimal monto;

    /** Columna concepto. */
    private String concepto;

    /** Columna fecha_hora. */
    private LocalDateTime fechaHora;

    public MovimientoCaja() {
    }

    public MovimientoCaja(int idMovimientoCaja,
            int idTurnoCaja,
            int idUsuario,
            Integer idVenta,
            String tipo,
            BigDecimal monto,
            String concepto,
            LocalDateTime fechaHora) {
        this.idMovimientoCaja = idMovimientoCaja;
        this.idTurnoCaja = idTurnoCaja;
        this.idUsuario = idUsuario;
        this.idVenta = idVenta;
        this.tipo = tipo;
        this.monto = monto;
        this.concepto = concepto;
        this.fechaHora = fechaHora;
    }

    public int getIdMovimientoCaja() {
        return idMovimientoCaja;
    }

    public void setIdMovimientoCaja(int idMovimientoCaja) {
        this.idMovimientoCaja = idMovimientoCaja;
    }

    public int getIdTurnoCaja() {
        return idTurnoCaja;
    }

    public void setIdTurnoCaja(int idTurnoCaja) {
        this.idTurnoCaja = idTurnoCaja;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(Integer idVenta) {
        this.idVenta = idVenta;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

}
