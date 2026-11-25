package modelos;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Datos de public.servicios_precios; las relaciones se representan mediante sus identificadores.
 * @see servlets.ServiciosServlet
 */
public class ServicioPrecio implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Columna id_servicio. */
    private int idServicio;

    /** Columna tipo_servicio. */
    private String tipoServicio;

    /** Columna color. */
    private String color;

    /** Columna tamano. */
    private String tamano;

    /** Columna doble_cara. */
    private boolean dobleCara;

    /** Columna precio_vigente. */
    private BigDecimal precioVigente;

    /** Columna activo. */
    private boolean activo;

    public ServicioPrecio() {
    }

    public ServicioPrecio(int idServicio,
            String tipoServicio,
            String color,
            String tamano,
            boolean dobleCara,
            BigDecimal precioVigente,
            boolean activo) {
        this.idServicio = idServicio;
        this.tipoServicio = tipoServicio;
        this.color = color;
        this.tamano = tamano;
        this.dobleCara = dobleCara;
        this.precioVigente = precioVigente;
        this.activo = activo;
    }

    public int getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(int idServicio) {
        this.idServicio = idServicio;
    }

    public String getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(String tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTamano() {
        return tamano;
    }

    public void setTamano(String tamano) {
        this.tamano = tamano;
    }

    public boolean isDobleCara() {
        return dobleCara;
    }

    public void setDobleCara(boolean dobleCara) {
        this.dobleCara = dobleCara;
    }

    public BigDecimal getPrecioVigente() {
        return precioVigente;
    }

    public void setPrecioVigente(BigDecimal precioVigente) {
        this.precioVigente = precioVigente;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

}
