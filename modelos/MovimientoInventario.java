package modelos;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Datos de public.movimientos_inventario; las relaciones se representan mediante sus identificadores.
 * @see servlets.MovimientosInventarioServlet
 */
public class MovimientoInventario implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Columna id_movimiento. */
    private int idMovimiento;

    /** Columna id_sucursal. */
    private int idSucursal;

    /** Columna id_producto. */
    private int idProducto;

    /** Columna id_usuario. */
    private int idUsuario;

    /** Columna id_tipo_movimiento. */
    private int idTipoMovimiento;

    /** Columna origen_movimiento. */
    private String origenMovimiento;

    /** Columna cantidad_afectada. */
    private int cantidadAfectada;

    /** Columna fecha_movimiento. */
    private LocalDateTime fechaMovimiento;

    public MovimientoInventario() {
    }

    public MovimientoInventario(int idMovimiento,
            int idSucursal,
            int idProducto,
            int idUsuario,
            int idTipoMovimiento,
            String origenMovimiento,
            int cantidadAfectada,
            LocalDateTime fechaMovimiento) {
        this.idMovimiento = idMovimiento;
        this.idSucursal = idSucursal;
        this.idProducto = idProducto;
        this.idUsuario = idUsuario;
        this.idTipoMovimiento = idTipoMovimiento;
        this.origenMovimiento = origenMovimiento;
        this.cantidadAfectada = cantidadAfectada;
        this.fechaMovimiento = fechaMovimiento;
    }

    public int getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(int idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public int getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(int idSucursal) {
        this.idSucursal = idSucursal;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdTipoMovimiento() {
        return idTipoMovimiento;
    }

    public void setIdTipoMovimiento(int idTipoMovimiento) {
        this.idTipoMovimiento = idTipoMovimiento;
    }

    public String getOrigenMovimiento() {
        return origenMovimiento;
    }

    public void setOrigenMovimiento(String origenMovimiento) {
        this.origenMovimiento = origenMovimiento;
    }

    public int getCantidadAfectada() {
        return cantidadAfectada;
    }

    public void setCantidadAfectada(int cantidadAfectada) {
        this.cantidadAfectada = cantidadAfectada;
    }

    public LocalDateTime getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(LocalDateTime fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

}
