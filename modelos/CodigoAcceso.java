package modelos;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Datos de public.codigos_acceso; las relaciones se representan mediante sus identificadores.
 * @see servlets.CodigosAccesoServlet
 */
public class CodigoAcceso implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Columna id_codigo. */
    private int idCodigo;

    /** Columna id_usuario. */
    private int idUsuario;

    /** Columna codigo_hash. */
    private String codigoHash;

    /** Columna generado_en. */
    private LocalDateTime generadoEn;

    /** Columna expira_en. */
    private LocalDateTime expiraEn;

    /** Columna usado. */
    private boolean usado;

    public CodigoAcceso() {
    }

    public CodigoAcceso(int idCodigo,
            int idUsuario,
            String codigoHash,
            LocalDateTime generadoEn,
            LocalDateTime expiraEn,
            boolean usado) {
        this.idCodigo = idCodigo;
        this.idUsuario = idUsuario;
        this.codigoHash = codigoHash;
        this.generadoEn = generadoEn;
        this.expiraEn = expiraEn;
        this.usado = usado;
    }

    public int getIdCodigo() {
        return idCodigo;
    }

    public void setIdCodigo(int idCodigo) {
        this.idCodigo = idCodigo;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    @jakarta.json.bind.annotation.JsonbTransient
    public String getCodigoHash() {
        return codigoHash;
    }

    @jakarta.json.bind.annotation.JsonbTransient
    public void setCodigoHash(String codigoHash) {
        this.codigoHash = codigoHash;
    }

    public LocalDateTime getGeneradoEn() {
        return generadoEn;
    }

    public void setGeneradoEn(LocalDateTime generadoEn) {
        this.generadoEn = generadoEn;
    }

    public LocalDateTime getExpiraEn() {
        return expiraEn;
    }

    public void setExpiraEn(LocalDateTime expiraEn) {
        this.expiraEn = expiraEn;
    }

    public boolean isUsado() {
        return usado;
    }

    public void setUsado(boolean usado) {
        this.usado = usado;
    }

}
