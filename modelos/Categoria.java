package modelos;

import java.io.Serializable;

/**
 * Datos de public.categorias; las relaciones se representan mediante sus identificadores.
 * @see servlets.CategoriasServlet
 */
public class Categoria implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Columna id_categoria. */
    private int idCategoria;

    /** Columna nombre. */
    private String nombre;

    /** Columna descripcion. */
    private String descripcion;

    /** Columna activa. */
    private boolean activa;

    public Categoria() {
    }

    public Categoria(int idCategoria,
            String nombre,
            String descripcion,
            boolean activa) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activa = activa;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

}
