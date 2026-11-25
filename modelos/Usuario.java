package modelos;

public class Usuario implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    public boolean isValidoParaOperar() {
        return activo && idUsuario > 0 && idSucursal > 0;
    }

    private int idUsuario;
    private int idRol;
    private int idSucursal;
    private String username;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private boolean activo;

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public int getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(int idSucursal) {
        this.idSucursal = idSucursal;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    /** Columna password_hash. */
    private String passwordHash;

    public Usuario() {
    }

    public Usuario(int idUsuario,
            int idRol,
            int idSucursal,
            String username,
            String passwordHash,
            String nombres,
            String apellidoPaterno,
            String apellidoMaterno,
            boolean activo) {
        this.idUsuario = idUsuario;
        this.idRol = idRol;
        this.idSucursal = idSucursal;
        this.username = username;
        this.passwordHash = passwordHash;
        this.nombres = nombres;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.activo = activo;
    }

    @jakarta.json.bind.annotation.JsonbTransient
    public String getPasswordHash() {
        return passwordHash;
    }

    @jakarta.json.bind.annotation.JsonbTransient
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

}
