package modelos;

import java.io.Serializable;

/**
 * Datos de public.tipos_movimiento; las relaciones se representan mediante sus identificadores.
 * @see servlets.TiposMovimientoServlet
 */
public class TipoMovimiento implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Columna id_tipo. */
    private int idTipo;

    /** Columna nombre. */
    private String nombre;

    /** Columna signo. */
    private short signo;

    public TipoMovimiento() {
    }

    public TipoMovimiento(int idTipo,
            String nombre,
            short signo) {
        this.idTipo = idTipo;
        this.nombre = nombre;
        this.signo = signo;
    }

    public int getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(int idTipo) {
        this.idTipo = idTipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public short getSigno() {
        return signo;
    }

    public void setSigno(short signo) {
        this.signo = signo;
    }

}
