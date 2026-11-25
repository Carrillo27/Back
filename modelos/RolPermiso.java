package modelos;

import java.io.Serializable;

/**
 * Datos de public.roles_permisos; las relaciones se representan mediante sus identificadores.
 * @see servlets.RolesPermisosServlet
 */
public class RolPermiso implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Columna id_rol. */
    private int idRol;

    /** Columna id_permiso. */
    private int idPermiso;

    public RolPermiso() {
    }

    public RolPermiso(int idRol,
            int idPermiso) {
        this.idRol = idRol;
        this.idPermiso = idPermiso;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public int getIdPermiso() {
        return idPermiso;
    }

    public void setIdPermiso(int idPermiso) {
        this.idPermiso = idPermiso;
    }

}
