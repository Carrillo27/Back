package modelos;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Datos de public.renta_computo; las relaciones se representan mediante sus identificadores.
 * @see servlets.RentaComputoServlet
 */
public class RentaComputo implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Columna id_renta. */
    private int idRenta;

    /** Columna id_equipo. */
    private int idEquipo;

    /** Columna id_venta. */
    private Integer idVenta;

    /** Columna hora_inicio. */
    private LocalDateTime horaInicio;

    /** Columna hora_fin. */
    private LocalDateTime horaFin;

    /** Columna tarifa_por_hora. */
    private BigDecimal tarifaPorHora;

    /** Columna monto_calculado. */
    private BigDecimal montoCalculado;

    /** Columna estado_renta. */
    private String estadoRenta;

    public RentaComputo() {
    }

    public RentaComputo(int idRenta,
            int idEquipo,
            Integer idVenta,
            LocalDateTime horaInicio,
            LocalDateTime horaFin,
            BigDecimal tarifaPorHora,
            BigDecimal montoCalculado,
            String estadoRenta) {
        this.idRenta = idRenta;
        this.idEquipo = idEquipo;
        this.idVenta = idVenta;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.tarifaPorHora = tarifaPorHora;
        this.montoCalculado = montoCalculado;
        this.estadoRenta = estadoRenta;
    }

    public int getIdRenta() {
        return idRenta;
    }

    public void setIdRenta(int idRenta) {
        this.idRenta = idRenta;
    }

    public int getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(int idEquipo) {
        this.idEquipo = idEquipo;
    }

    public Integer getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(Integer idVenta) {
        this.idVenta = idVenta;
    }

    public LocalDateTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalDateTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalDateTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalDateTime horaFin) {
        this.horaFin = horaFin;
    }

    public BigDecimal getTarifaPorHora() {
        return tarifaPorHora;
    }

    public void setTarifaPorHora(BigDecimal tarifaPorHora) {
        this.tarifaPorHora = tarifaPorHora;
    }

    public BigDecimal getMontoCalculado() {
        return montoCalculado;
    }

    public void setMontoCalculado(BigDecimal montoCalculado) {
        this.montoCalculado = montoCalculado;
    }

    public String getEstadoRenta() {
        return estadoRenta;
    }

    public void setEstadoRenta(String estadoRenta) {
        this.estadoRenta = estadoRenta;
    }

}
