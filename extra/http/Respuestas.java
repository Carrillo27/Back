package extra.http;

import java.util.Map;
import java.util.Set;

/** Respuestas y validaciones compartidas por los Servlets de catálogo. */
public final class Respuestas {
    private Respuestas() {}

    public static Map<String, Object> existente(Map<String, Object> fila) {
        if (fila == null) throw new ApiException(404, "No se encontró el registro solicitado.");
        return fila;
    }

    public static void encontrado(boolean actualizado) {
        if (!actualizado) throw new ApiException(404, "No se encontró el registro solicitado.");
    }

    public static String opcion(String valor, Set<String> permitidas, String campo) {
        if (!permitidas.contains(valor)) {
            throw new ApiException(400,
                    "Valor no permitido en " + campo + ": " + String.join(", ", permitidas) + ".");
        }
        return valor;
    }

    public static String busqueda(String busqueda) {
        if (busqueda != null && busqueda.length() > 120) {
            throw new ApiException(400, "La búsqueda es demasiado larga.");
        }
        return busqueda;
    }

    public static int id(int id) {
        if (id <= 0) throw new ApiException(400, "Identificador inválido.");
        return id;
    }
}
