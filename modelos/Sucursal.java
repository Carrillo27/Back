package modelos;

import java.io.Serializable;

/**
 * Datos de public.sucursales; las relaciones se representan mediante sus identificadores.
 * @see servlets.SucursalesServlet
 */
public class Sucursal implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Columna id_sucursal. */
    private int idSucursal;

    /** Columna nombre. */
    private String nombre;

    /** Columna direccion. */
    private String direccion;

    /** Columna telefono. */
    private String telefono;

    /** Columna activa. */
    private boolean activa;

    public Sucursal() {
    }

    public Sucursal(int idSucursal,
            String nombre,
            String direccion,
            String telefono,
            boolean activa) {
        this.idSucursal = idSucursal;
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.activa = activa;
    }

    public int getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(int idSucursal) {
        this.idSucursal = idSucursal;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

}
