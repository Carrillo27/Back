package modelos;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Datos de public.ventas; las relaciones se representan mediante sus identificadores.
 * @see servlets.VentasServlet
 */
public class Venta implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Columna id_venta. */
    private int idVenta;

    /** Columna id_sucursal. */
    private int idSucursal;

    /** Columna id_usuario. */
    private int idUsuario;

    /** Columna id_metodo_pago. */
    private Integer idMetodoPago;

    /** Columna fecha_hora. */
    private LocalDateTime fechaHora;

    /** Columna porcentaje_impuesto. */
    private BigDecimal porcentajeImpuesto;

    /** Columna subtotal. */
    private BigDecimal subtotal;

    /** Columna total. */
    private BigDecimal total;

    /** Columna monto_recibido. */
    private BigDecimal montoRecibido;

    /** Columna folio_pago. */
    private String folioPago;

    /** Columna estado. */
    private String estado;

    /** Columna id_turno_caja. */
    private Integer idTurnoCaja;

    public Venta() {
    }

    public Venta(int idVenta,
            int idSucursal,
            int idUsuario,
            Integer idMetodoPago,
            LocalDateTime fechaHora,
            BigDecimal porcentajeImpuesto,
            BigDecimal subtotal,
            BigDecimal total,
            BigDecimal montoRecibido,
            String folioPago,
            String estado,
            Integer idTurnoCaja) {
        this.idVenta = idVenta;
        this.idSucursal = idSucursal;
        this.idUsuario = idUsuario;
        this.idMetodoPago = idMetodoPago;
        this.fechaHora = fechaHora;
        this.porcentajeImpuesto = porcentajeImpuesto;
        this.subtotal = subtotal;
        this.total = total;
        this.montoRecibido = montoRecibido;
        this.folioPago = folioPago;
        this.estado = estado;
        this.idTurnoCaja = idTurnoCaja;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public int getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(int idSucursal) {
        this.idSucursal = idSucursal;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdMetodoPago() {
        return idMetodoPago;
    }

    public void setIdMetodoPago(Integer idMetodoPago) {
        this.idMetodoPago = idMetodoPago;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public BigDecimal getPorcentajeImpuesto() {
        return porcentajeImpuesto;
    }

    public void setPorcentajeImpuesto(BigDecimal porcentajeImpuesto) {
        this.porcentajeImpuesto = porcentajeImpuesto;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getMontoRecibido() {
        return montoRecibido;
    }

    public void setMontoRecibido(BigDecimal montoRecibido) {
        this.montoRecibido = montoRecibido;
    }

    public String getFolioPago() {
        return folioPago;
    }

    public void setFolioPago(String folioPago) {
        this.folioPago = folioPago;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getIdTurnoCaja() {
        return idTurnoCaja;
    }

    public void setIdTurnoCaja(Integer idTurnoCaja) {
        this.idTurnoCaja = idTurnoCaja;
    }

}
