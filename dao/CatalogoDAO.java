package dao;

import extra.seguridad.CifradoDatos;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Catálogos administrables: categorías, métodos de pago, proveedores, servicios y equipos. */
public class CatalogoDAO {
    private static String filtro(String busqueda) {
        return "%" + (busqueda == null ? "" : busqueda.trim()) + "%";
    }

    // ---- Categorías ----
    private static final String CATEGORIA = """
            SELECT id_categoria AS "idCategoria", nombre, descripcion, activa FROM categorias""";

    public List<Map<String, Object>> categorias(String busqueda) throws SQLException {
        return Jdbc.consultar(CATEGORIA + " WHERE LOWER(nombre) LIKE LOWER(?) ORDER BY nombre", filtro(busqueda));
    }

    public Map<String, Object> categoria(int id) throws SQLException {
        return Jdbc.uno(CATEGORIA + " WHERE id_categoria = ?", id);
    }

    public int crearCategoria(String nombre, String descripcion) throws SQLException {
        return (Integer) Jdbc.uno(
                "INSERT INTO categorias (nombre, descripcion) VALUES (?, ?) RETURNING id_categoria AS id",
                nombre, descripcion).get("id");
    }

    public boolean actualizarCategoria(int id, String nombre, String descripcion) throws SQLException {
        return Jdbc.ejecutar("UPDATE categorias SET nombre = ?, descripcion = ? WHERE id_categoria = ?",
                nombre, descripcion, id) > 0;
    }

    public boolean estadoCategoria(int id, boolean activa) throws SQLException {
        return Jdbc.ejecutar("UPDATE categorias SET activa = ? WHERE id_categoria = ?", activa, id) > 0;
    }

    // ---- Métodos de pago ----
    private static final String METODO = """
            SELECT id_metodo AS "idMetodo", nombre, tipo, requiere_folio AS "requiereFolio", activo FROM metodos_pago""";

    public List<Map<String, Object>> metodosPago(String busqueda) throws SQLException {
        return Jdbc.consultar(METODO + " WHERE LOWER(nombre) LIKE LOWER(?) ORDER BY nombre", filtro(busqueda));
    }

    public Map<String, Object> metodoPago(int id) throws SQLException {
        return Jdbc.uno(METODO + " WHERE id_metodo = ?", id);
    }

    public int crearMetodoPago(String nombre, String tipo, boolean requiereFolio) throws SQLException {
        return (Integer) Jdbc.uno("""
                INSERT INTO metodos_pago (nombre, tipo, requiere_folio) VALUES (?, ?, ?) RETURNING id_metodo AS id""",
                nombre, tipo, requiereFolio).get("id");
    }

    public boolean actualizarMetodoPago(int id, String nombre, String tipo, boolean requiereFolio)
            throws SQLException {
        return Jdbc.ejecutar("UPDATE metodos_pago SET nombre = ?, tipo = ?, requiere_folio = ? WHERE id_metodo = ?",
                nombre, tipo, requiereFolio, id) > 0;
    }

    public boolean estadoMetodoPago(int id, boolean activo) throws SQLException {
        return Jdbc.ejecutar("UPDATE metodos_pago SET activo = ? WHERE id_metodo = ?", activo, id) > 0;
    }

    // ---- Proveedores ----
    private static final String PROVEEDOR = """
            SELECT id_proveedor AS "idProveedor", nombre_empresa AS "nombreEmpresa",
                   contacto_nombres AS "contactoNombres", contacto_apellido_paterno AS "contactoApellidoPaterno",
                   contacto_apellido_materno AS "contactoApellidoMaterno", telefono, correo, direccion, activo
            FROM proveedores""";

    public List<Map<String, Object>> proveedores(String busqueda) throws SQLException {
        return CifradoDatos.proveedores(Jdbc.consultar(PROVEEDOR), busqueda);
    }

    public Map<String, Object> proveedor(int id) throws SQLException {
        return CifradoDatos.proveedor(Jdbc.uno(PROVEEDOR + " WHERE id_proveedor = ?", id));
    }

    public int crearProveedor(String empresa, String nombres, String paterno, String materno,
            String telefono, String correo, String direccion) throws SQLException {
        return (Integer) Jdbc.uno("""
                INSERT INTO proveedores (nombre_empresa, contacto_nombres, contacto_apellido_paterno,
                    contacto_apellido_materno, telefono, correo, direccion)
                VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id_proveedor AS id""",
                empresa, nombres, paterno, materno, CifradoDatos.cifrar(telefono, "proveedores.telefono"),
                CifradoDatos.cifrar(correo, "proveedores.correo"), CifradoDatos.cifrar(direccion, "proveedores.direccion")).get("id");
    }

    public boolean actualizarProveedor(int id, String empresa, String nombres, String paterno, String materno,
            String telefono, String correo, String direccion) throws SQLException {
        return Jdbc.ejecutar("""
                UPDATE proveedores SET nombre_empresa = ?, contacto_nombres = ?, contacto_apellido_paterno = ?,
                    contacto_apellido_materno = ?, telefono = ?, correo = ?, direccion = ?
                WHERE id_proveedor = ?""", empresa, nombres, paterno, materno, CifradoDatos.cifrar(telefono, "proveedores.telefono"),
                CifradoDatos.cifrar(correo, "proveedores.correo"), CifradoDatos.cifrar(direccion, "proveedores.direccion"), id) > 0;
    }

    public boolean estadoProveedor(int id, boolean activo) throws SQLException {
        return Jdbc.ejecutar("UPDATE proveedores SET activo = ? WHERE id_proveedor = ?", activo, id) > 0;
    }

    // ---- Servicios y precios ----
    private static final String SERVICIO = """
            SELECT id_servicio AS "idServicio", tipo_servicio AS "tipoServicio", color, tamano,
                   doble_cara AS "dobleCara", precio_vigente AS "precioVigente", activo FROM servicios_precios""";

    public List<Map<String, Object>> servicios(String busqueda) throws SQLException {
        return Jdbc.consultar(SERVICIO
                + " WHERE LOWER(tipo_servicio) LIKE LOWER(?) ORDER BY tipo_servicio, color, tamano, doble_cara",
                filtro(busqueda));
    }

    public Map<String, Object> servicio(int id) throws SQLException {
        return Jdbc.uno(SERVICIO + " WHERE id_servicio = ?", id);
    }

    public int crearServicio(String tipo, String color, String tamano, boolean doble, BigDecimal precio)
            throws SQLException {
        return (Integer) Jdbc.uno("""
                INSERT INTO servicios_precios (tipo_servicio, color, tamano, doble_cara, precio_vigente)
                VALUES (?, ?, ?, ?, ?) RETURNING id_servicio AS id""", tipo, color, tamano, doble, precio).get("id");
    }

    public boolean actualizarServicio(int id, String tipo, String color, String tamano, boolean doble,
            BigDecimal precio) throws SQLException {
        return Jdbc.ejecutar("""
                UPDATE servicios_precios SET tipo_servicio = ?, color = ?, tamano = ?, doble_cara = ?,
                    precio_vigente = ? WHERE id_servicio = ?""", tipo, color, tamano, doble, precio, id) > 0;
    }

    public boolean estadoServicio(int id, boolean activo) throws SQLException {
        return Jdbc.ejecutar("UPDATE servicios_precios SET activo = ? WHERE id_servicio = ?", activo, id) > 0;
    }

    // ---- Equipos de cómputo (por sucursal) ----
    private static final String EQUIPO = """
            SELECT id_equipo AS "idEquipo", numero_equipo AS "numeroEquipo", descripcion, modelo,
                   fecha_adquisicion AS "fechaAdquisicion", estado_actual AS "estadoActual" FROM equipos_computo""";

    public List<Map<String, Object>> equipos(int sucursal, String busqueda) throws SQLException {
        String f = filtro(busqueda);
        return Jdbc.consultar(EQUIPO + " " + """
                 WHERE id_sucursal = ? AND (LOWER(descripcion) LIKE LOWER(?) OR LOWER(COALESCE(modelo, '')) LIKE LOWER(?)
                    OR CAST(numero_equipo AS TEXT) LIKE ?) ORDER BY numero_equipo""", sucursal, f, f, f);
    }

    public Map<String, Object> equipo(int sucursal, int id) throws SQLException {
        return Jdbc.uno(EQUIPO + " WHERE id_sucursal = ? AND id_equipo = ?", sucursal, id);
    }

    public int crearEquipo(int sucursal, int numero, String descripcion, String modelo, LocalDate adquisicion)
            throws SQLException {
        return (Integer) Jdbc.uno("""
                INSERT INTO equipos_computo (id_sucursal, numero_equipo, descripcion, modelo, fecha_adquisicion,
                                             estado_actual)
                VALUES (?, ?, ?, ?, ?, 'Sin servicio') RETURNING id_equipo AS id""",
                sucursal, numero, descripcion, modelo, adquisicion).get("id");
    }

    public boolean actualizarEquipo(int sucursal, int id, int numero, String descripcion, String modelo,
            LocalDate adquisicion) throws SQLException {
        return Jdbc.ejecutar("""
                UPDATE equipos_computo SET numero_equipo = ?, descripcion = ?, modelo = ?, fecha_adquisicion = ?
                WHERE id_sucursal = ? AND id_equipo = ?""", numero, descripcion, modelo, adquisicion, sucursal, id) > 0;
    }

    public boolean estadoEquipo(int sucursal, int id, String estado) throws SQLException {
        return Jdbc.ejecutar("UPDATE equipos_computo SET estado_actual = ? WHERE id_sucursal = ? AND id_equipo = ?",
                estado, sucursal, id) > 0;
    }
}
