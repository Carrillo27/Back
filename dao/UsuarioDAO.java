package dao;

import extra.seguridad.CifradoDatos;

import extra.config.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelos.Usuario;
public class UsuarioDAO {

    public boolean existeUsuarioActivo(String username)
            throws SQLException {

        String sql = """
            SELECT 1
            FROM usuarios
            WHERE username_busqueda = ?
              AND activo = true
            """;
        try (Connection conexion =
                ConexionBD.obtenerConexion();
             PreparedStatement ps =
                conexion.prepareStatement(sql)) {
            ps.setString(1, CifradoDatos.indiceUsuario(username));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Usuario autenticar(String username, String password)
            throws SQLException {

        String sql = """
            SELECT id_usuario, id_rol, id_sucursal,
                   username, nombres, apellido_paterno,
                   apellido_materno, activo, password_hash
            FROM usuarios
            WHERE username_busqueda = ?
              AND activo = true
            """;
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, CifradoDatos.indiceUsuario(username));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String almacenada = rs.getString("password_hash");
                    if (!extra.seguridad.CifradoDatos.verificar(conexion, password, almacenada)) return null;
                    if (extra.seguridad.CifradoDatos.esLegado(almacenada)) {
                        try (var cambio = conexion.prepareStatement("UPDATE usuarios SET password_hash=? WHERE id_usuario=? AND password_hash=?")) {
                            cambio.setString(1, extra.seguridad.CifradoDatos.cifrarContrasena(password));
                            cambio.setInt(2, rs.getInt("id_usuario"));
                            cambio.setString(3, almacenada);
                            if (cambio.executeUpdate()!=1) return null;
                        }
                    }

                    Usuario usuario = new Usuario();

                    usuario.setIdUsuario(rs.getInt("id_usuario"));
                    usuario.setIdRol(rs.getInt("id_rol"));
                    usuario.setIdSucursal(rs.getInt("id_sucursal"));
                    usuario.setUsername(CifradoDatos.descifrar(rs.getString("username"), "usuarios.username"));
                    usuario.setNombres(CifradoDatos.descifrar(rs.getString("nombres"), "usuarios.nombres"));
                    usuario.setApellidoPaterno(
                            CifradoDatos.descifrar(rs.getString("apellido_paterno"), "usuarios.apellido_paterno"));
                    usuario.setApellidoMaterno(
                            CifradoDatos.descifrar(rs.getString("apellido_materno"), "usuarios.apellido_materno"));
                    usuario.setActivo(rs.getBoolean("activo"));
                    return usuario;
                }
            }
        }
        return null;
    }
}
