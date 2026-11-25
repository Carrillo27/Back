package modelos;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Datos de public.equipos_computo; las relaciones se representan mediante sus identificadores.
 * @see servlets.EquiposServlet
 */
public class EquipoComputo implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Columna id_equipo. */
    private int idEquipo;

    /** Columna id_sucursal. */
    private int idSucursal;

    /** Columna numero_equipo. */
    private int numeroEquipo;

    /** Columna descripcion. */
    private String descripcion;

    /** Columna modelo. */
    private String modelo;

    /** Columna fecha_adquisicion. */
    private LocalDate fechaAdquisicion;

    /** Columna estado_actual. */
    private String estadoActual;

    public EquipoComputo() {
    }

    public EquipoComputo(int idEquipo,
            int idSucursal,
            int numeroEquipo,
            String descripcion,
            String modelo,
            LocalDate fechaAdquisicion,
            String estadoActual) {
        this.idEquipo = idEquipo;
        this.idSucursal = idSucursal;
        this.numeroEquipo = numeroEquipo;
        this.descripcion = descripcion;
        this.modelo = modelo;
        this.fechaAdquisicion = fechaAdquisicion;
        this.estadoActual = estadoActual;
    }

    public int getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(int idEquipo) {
        this.idEquipo = idEquipo;
    }

    public int getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(int idSucursal) {
        this.idSucursal = idSucursal;
    }

    public int getNumeroEquipo() {
        return numeroEquipo;
    }

    public void setNumeroEquipo(int numeroEquipo) {
        this.numeroEquipo = numeroEquipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public LocalDate getFechaAdquisicion() {
        return fechaAdquisicion;
    }

    public void setFechaAdquisicion(LocalDate fechaAdquisicion) {
        this.fechaAdquisicion = fechaAdquisicion;
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(String estadoActual) {
        this.estadoActual = estadoActual;
    }

}
