package modelos;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Datos de public.compras; las relaciones se representan mediante sus identificadores.
 * @see servlets.ComprasServlet
 */
public class Compra implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Columna id_compra. */
    private int idCompra;

    /** Columna id_sucursal. */
    private int idSucursal;

    /** Columna id_usuario. */
    private int idUsuario;

    /** Columna id_proveedor. */
    private int idProveedor;

    /** Columna tipo_compra. */
    private String tipoCompra;

    /** Columna fecha_compra. */
    private LocalDateTime fechaCompra;

    /** Columna total_compra. */
    private BigDecimal totalCompra;

    /** Columna estado. */
    private String estado;

    public Compra() {
    }

    public Compra(int idCompra,
            int idSucursal,
            int idUsuario,
            int idProveedor,
            String tipoCompra,
            LocalDateTime fechaCompra,
            BigDecimal totalCompra,
            String estado) {
        this.idCompra = idCompra;
        this.idSucursal = idSucursal;
        this.idUsuario = idUsuario;
        this.idProveedor = idProveedor;
        this.tipoCompra = tipoCompra;
        this.fechaCompra = fechaCompra;
        this.totalCompra = totalCompra;
        this.estado = estado;
    }

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
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

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getTipoCompra() {
        return tipoCompra;
    }

    public void setTipoCompra(String tipoCompra) {
        this.tipoCompra = tipoCompra;
    }

    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(LocalDateTime fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public BigDecimal getTotalCompra() {
        return totalCompra;
    }

    public void setTotalCompra(BigDecimal totalCompra) {
        this.totalCompra = totalCompra;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}
