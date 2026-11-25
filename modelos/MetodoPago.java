package modelos;

import java.io.Serializable;

/**
 * Datos de public.metodos_pago; las relaciones se representan mediante sus identificadores.
 * @see servlets.MetodosPagoServlet
 */
public class MetodoPago implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Columna id_metodo. */
    private int idMetodo;

    /** Columna nombre. */
    private String nombre;

    /** Columna tipo. */
    private String tipo;

    /** Columna requiere_folio. */
    private boolean requiereFolio;

    /** Columna activo. */
    private boolean activo;

    public MetodoPago() {
    }

    public MetodoPago(int idMetodo,
            String nombre,
            String tipo,
            boolean requiereFolio,
            boolean activo) {
        this.idMetodo = idMetodo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.requiereFolio = requiereFolio;
        this.activo = activo;
    }

    public int getIdMetodo() {
        return idMetodo;
    }

    public void setIdMetodo(int idMetodo) {
        this.idMetodo = idMetodo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isRequiereFolio() {
        return requiereFolio;
    }

    public void setRequiereFolio(boolean requiereFolio) {
        this.requiereFolio = requiereFolio;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

}
