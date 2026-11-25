package modelos;

import java.io.Serializable;

/**
 * Datos de public.roles; las relaciones se representan mediante sus identificadores.
 * @see servlets.RolesServlet
 */
public class Rol implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Columna id_rol. */
    private int idRol;

    /** Columna nombre. */
    private String nombre;

    public Rol() {
    }

    public Rol(int idRol,
            String nombre) {
        this.idRol = idRol;
        this.nombre = nombre;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

}
