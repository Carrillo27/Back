package modelos;

import java.io.Serializable;

/**
 * Datos de public.permisos; las relaciones se representan mediante sus identificadores.
 * @see servlets.PermisosServlet
 */
public class Permiso implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Columna id_permiso. */
    private int idPermiso;

    /** Columna clave. */
    private String clave;

    /** Columna descripcion. */
    private String descripcion;

    public Permiso() {
    }

    public Permiso(int idPermiso,
            String clave,
            String descripcion) {
        this.idPermiso = idPermiso;
        this.clave = clave;
        this.descripcion = descripcion;
    }

    public int getIdPermiso() {
        return idPermiso;
    }

    public void setIdPermiso(int idPermiso) {
        this.idPermiso = idPermiso;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

}
