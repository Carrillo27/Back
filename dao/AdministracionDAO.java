package dao;

import extra.seguridad.CifradoDatos;

import extra.config.ConexionBD;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** Usuarios de la sucursal, roles y asignación de permisos. */
public class AdministracionDAO {
    private static final String USUARIO = """
            SELECT u.id_usuario AS "idUsuario", u.username, u.nombres, u.apellido_paterno AS "apellidoPaterno",
                   u.apellido_materno AS "apellidoMaterno", u.id_rol AS "idRol", r.nombre AS rol, u.activo
            FROM usuarios u INNER JOIN roles r ON r.id_rol = u.id_rol""";

    public List<Map<String, Object>> usuarios(int sucursal, String busqueda) throws SQLException {
        return CifradoDatos.usuarios(Jdbc.consultar(USUARIO + " WHERE u.id_sucursal = ?", sucursal), busqueda);
    }

    public Map<String, Object> usuario(int sucursal, int id) throws SQLException {
        return CifradoDatos.usuario(Jdbc.uno(USUARIO + " WHERE u.id_sucursal = ? AND u.id_usuario = ?", sucursal, id));
    }

    public int crearUsuario(int sucursal, int rol, String username, String contrasena, String nombres,
            String paterno, String materno) throws SQLException {
        return (Integer) Jdbc.uno("""
                INSERT INTO usuarios (id_rol, id_sucursal, username, username_busqueda, password_hash, nombres,
                    apellido_paterno, apellido_materno)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id_usuario AS id""", rol, sucursal, CifradoDatos.cifrar(username, "usuarios.username"), CifradoDatos.indiceUsuario(username), extra.seguridad.CifradoDatos.cifrarContrasena(contrasena),
                CifradoDatos.cifrar(nombres, "usuarios.nombres"), CifradoDatos.cifrar(paterno, "usuarios.apellido_paterno"), CifradoDatos.cifrar(materno, "usuarios.apellido_materno"))
                .get("id");
    }

    public boolean actualizarUsuario(int sucursal, int id, int rol, String nombres, String paterno,
            String materno) throws SQLException {
        return Jdbc.ejecutar("""
                UPDATE usuarios SET id_rol = ?, nombres = ?, apellido_paterno = ?, apellido_materno = ?
                WHERE id_sucursal = ? AND id_usuario = ?""", rol, CifradoDatos.cifrar(nombres, "usuarios.nombres"), CifradoDatos.cifrar(paterno, "usuarios.apellido_paterno"),
                CifradoDatos.cifrar(materno, "usuarios.apellido_materno"), sucursal, id) > 0;
    }

    public boolean estadoUsuario(int sucursal, int id, boolean activo) throws SQLException {
        return Jdbc.ejecutar("UPDATE usuarios SET activo = ? WHERE id_sucursal = ? AND id_usuario = ?",
                activo, sucursal, id) > 0;
    }

    public boolean cambiarContrasena(int sucursal, int id, String contrasena) throws SQLException {
        return Jdbc.ejecutar("""
                UPDATE usuarios SET password_hash = ?
                WHERE id_sucursal = ? AND id_usuario = ?""", extra.seguridad.CifradoDatos.cifrarContrasena(contrasena), sucursal, id) > 0;
    }

    // ---- Roles y permisos ----
    private static final String ROL = """
            SELECT r.id_rol AS "idRol", r.nombre,
                   (SELECT COUNT(*) FROM usuarios u WHERE u.id_rol = r.id_rol) AS usuarios,
                   (SELECT COUNT(*) FROM roles_permisos rp WHERE rp.id_rol = r.id_rol) AS permisos
            FROM roles r""";

    public List<Map<String, Object>> roles() throws SQLException {
        return Jdbc.consultar(ROL + " ORDER BY r.nombre");
    }

    public Map<String, Object> rol(int id) throws SQLException {
        return Jdbc.uno(ROL + " WHERE r.id_rol = ?", id);
    }

    public int crearRol(String nombre) throws SQLException {
        return (Integer) Jdbc.uno("INSERT INTO roles (nombre) VALUES (?) RETURNING id_rol AS id", nombre).get("id");
    }

    public boolean actualizarRol(int id, String nombre) throws SQLException {
        return Jdbc.ejecutar("UPDATE roles SET nombre = ? WHERE id_rol = ?", nombre, id) > 0;
    }

    /**
     * Crea los permisos estándar que faltan (mismo contenido que database/004-permisos.sql).
     * Así la pantalla de roles funciona aunque nadie haya ejecutado ese script.
     */
    private void asegurarPermisosBase() throws SQLException {
        Jdbc.ejecutar("""
                INSERT INTO permisos (clave, descripcion)
                SELECT v.clave, v.descripcion FROM (VALUES
                    ('ventas.cobrar',        'Registrar ventas y cobrar'),
                    ('inventario.ver',       'Ver inventario / catálogo de productos'),
                    ('servicios.configurar', 'Configurar servicios e impresiones'),
                    ('reportes.ver',         'Ver reportes financieros y corte de caja'),
                    ('usuarios.administrar', 'Administrar usuarios')
                ) AS v(clave, descripcion)
                WHERE NOT EXISTS (SELECT 1 FROM permisos p WHERE p.clave = v.clave)""");
    }

    public List<Map<String, Object>> permisos() throws SQLException {
        asegurarPermisosBase();
        return Jdbc.consultar("""
                SELECT id_permiso AS "idPermiso", clave, descripcion FROM permisos ORDER BY clave""");
    }

    public List<Map<String, Object>> permisosDeRol(int rol) throws SQLException {
        return Jdbc.consultar("""
                SELECT p.id_permiso AS "idPermiso", p.clave, p.descripcion FROM roles_permisos rp
                INNER JOIN permisos p ON p.id_permiso = rp.id_permiso WHERE rp.id_rol = ? ORDER BY p.clave""", rol);
    }

    /** Reemplaza todos los permisos del rol en una sola transacción. */
    public void asignarPermisos(int rol, List<Integer> permisos) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                Jdbc.ejecutar(conexion, "DELETE FROM roles_permisos WHERE id_rol = ?", rol);
                for (int permiso : permisos) {
                    Jdbc.ejecutar(conexion, "INSERT INTO roles_permisos (id_rol, id_permiso) VALUES (?, ?)",
                            rol, permiso);
                }
                conexion.commit();
            } catch (SQLException | RuntimeException e) {
                try {
                    conexion.rollback();
                } catch (SQLException rollback) {
                    e.addSuppressed(rollback);
                }
                throw e;
            }
        }
    }
}
