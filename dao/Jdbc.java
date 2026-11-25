package dao;

import extra.config.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Consultas parametrizadas que devuelven filas como mapas; los alias SQL definen las claves JSON. */
public final class Jdbc {
    private Jdbc() { }

    public static List<Map<String, Object>> consultar(String sql, Object... parametros) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return consultar(conexion, sql, parametros);
        }
    }

    public static List<Map<String, Object>> consultar(Connection conexion, String sql, Object... parametros)
            throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            enlazar(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                return filas(rs);
            }
        }
    }

    public static Map<String, Object> uno(String sql, Object... parametros) throws SQLException {
        List<Map<String, Object>> filas = consultar(sql, parametros);
        return filas.isEmpty() ? null : filas.get(0);
    }

    public static Map<String, Object> uno(Connection conexion, String sql, Object... parametros)
            throws SQLException {
        List<Map<String, Object>> filas = consultar(conexion, sql, parametros);
        return filas.isEmpty() ? null : filas.get(0);
    }

    public static int ejecutar(String sql, Object... parametros) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return ejecutar(conexion, sql, parametros);
        }
    }

    public static int ejecutar(Connection conexion, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            enlazar(ps, parametros);
            return ps.executeUpdate();
        }
    }

    private static void enlazar(PreparedStatement ps, Object[] parametros) throws SQLException {
        for (int i = 0; i < parametros.length; i++) ps.setObject(i + 1, parametros[i]);
    }

    private static List<Map<String, Object>> filas(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        List<Map<String, Object>> filas = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> fila = new LinkedHashMap<>();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                Object valor = rs.getObject(i);
                if (valor instanceof Timestamp t) valor = t.toLocalDateTime().toString();
                else if (valor instanceof java.sql.Date d) valor = d.toLocalDate().toString();
                fila.put(meta.getColumnLabel(i), valor);
            }
            filas.add(fila);
        }
        return filas;
    }
}
