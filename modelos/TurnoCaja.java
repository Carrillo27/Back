package modelos;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Datos de public.turnos_caja; las relaciones se representan mediante sus identificadores.
 * @see servlets.TurnosCajaServlet
 */
public class TurnoCaja implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Columna id_turno_caja. */
    private int idTurnoCaja;

    /** Columna id_sucursal. */
    private int idSucursal;

    /** Columna id_usuario_apertura. */
    private int idUsuarioApertura;

    /** Columna fecha_apertura. */
    private LocalDateTime fechaApertura;

    /** Columna fondo_inicial. */
    private BigDecimal fondoInicial;

    /** Columna estado. */
    private String estado;

    /** Columna fecha_cierre. */
    private LocalDateTime fechaCierre;

    /** Columna id_usuario_cierre. */
    private Integer idUsuarioCierre;

    /** Columna efectivo_esperado. */
    private BigDecimal efectivoEsperado;

    /** Columna efectivo_contado. */
    private BigDecimal efectivoContado;

    /** Columna diferencia. */
    private BigDecimal diferencia;

    public TurnoCaja() {
    }

    public TurnoCaja(int idTurnoCaja,
            int idSucursal,
            int idUsuarioApertura,
            LocalDateTime fechaApertura,
            BigDecimal fondoInicial,
            String estado,
            LocalDateTime fechaCierre,
            Integer idUsuarioCierre,
            BigDecimal efectivoEsperado,
            BigDecimal efectivoContado,
            BigDecimal diferencia) {
        this.idTurnoCaja = idTurnoCaja;
        this.idSucursal = idSucursal;
        this.idUsuarioApertura = idUsuarioApertura;
        this.fechaApertura = fechaApertura;
        this.fondoInicial = fondoInicial;
        this.estado = estado;
        this.fechaCierre = fechaCierre;
        this.idUsuarioCierre = idUsuarioCierre;
        this.efectivoEsperado = efectivoEsperado;
        this.efectivoContado = efectivoContado;
        this.diferencia = diferencia;
    }

    public int getIdTurnoCaja() {
        return idTurnoCaja;
    }

    public void setIdTurnoCaja(int idTurnoCaja) {
        this.idTurnoCaja = idTurnoCaja;
    }

    public int getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(int idSucursal) {
        this.idSucursal = idSucursal;
    }

    public int getIdUsuarioApertura() {
        return idUsuarioApertura;
    }

    public void setIdUsuarioApertura(int idUsuarioApertura) {
        this.idUsuarioApertura = idUsuarioApertura;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDateTime fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public BigDecimal getFondoInicial() {
        return fondoInicial;
    }

    public void setFondoInicial(BigDecimal fondoInicial) {
        this.fondoInicial = fondoInicial;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public Integer getIdUsuarioCierre() {
        return idUsuarioCierre;
    }

    public void setIdUsuarioCierre(Integer idUsuarioCierre) {
        this.idUsuarioCierre = idUsuarioCierre;
    }

    public BigDecimal getEfectivoEsperado() {
        return efectivoEsperado;
    }

    public void setEfectivoEsperado(BigDecimal efectivoEsperado) {
        this.efectivoEsperado = efectivoEsperado;
    }

    public BigDecimal getEfectivoContado() {
        return efectivoContado;
    }

    public void setEfectivoContado(BigDecimal efectivoContado) {
        this.efectivoContado = efectivoContado;
    }

    public BigDecimal getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(BigDecimal diferencia) {
        this.diferencia = diferencia;
    }

}
