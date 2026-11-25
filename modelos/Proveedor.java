package modelos;

import java.io.Serializable;

/**
 * Datos de public.proveedores; las relaciones se representan mediante sus identificadores.
 * @see servlets.ProveedoresServlet
 */
public class Proveedor implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Columna id_proveedor. */
    private int idProveedor;

    /** Columna nombre_empresa. */
    private String nombreEmpresa;

    /** Columna contacto_nombres. */
    private String contactoNombres;

    /** Columna contacto_apellido_paterno. */
    private String contactoApellidoPaterno;

    /** Columna contacto_apellido_materno. */
    private String contactoApellidoMaterno;

    /** Columna telefono. */
    private String telefono;

    /** Columna correo. */
    private String correo;

    /** Columna direccion. */
    private String direccion;

    /** Columna activo. */
    private boolean activo;

    public Proveedor() {
    }

    public Proveedor(int idProveedor,
            String nombreEmpresa,
            String contactoNombres,
            String contactoApellidoPaterno,
            String contactoApellidoMaterno,
            String telefono,
            String correo,
            String direccion,
            boolean activo) {
        this.idProveedor = idProveedor;
        this.nombreEmpresa = nombreEmpresa;
        this.contactoNombres = contactoNombres;
        this.contactoApellidoPaterno = contactoApellidoPaterno;
        this.contactoApellidoMaterno = contactoApellidoMaterno;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
        this.activo = activo;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getContactoNombres() {
        return contactoNombres;
    }

    public void setContactoNombres(String contactoNombres) {
        this.contactoNombres = contactoNombres;
    }

    public String getContactoApellidoPaterno() {
        return contactoApellidoPaterno;
    }

    public void setContactoApellidoPaterno(String contactoApellidoPaterno) {
        this.contactoApellidoPaterno = contactoApellidoPaterno;
    }

    public String getContactoApellidoMaterno() {
        return contactoApellidoMaterno;
    }

    public void setContactoApellidoMaterno(String contactoApellidoMaterno) {
        this.contactoApellidoMaterno = contactoApellidoMaterno;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

}
