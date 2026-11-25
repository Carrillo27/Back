package extra.http;

import extra.validacion.ValidacionImporte;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import java.math.BigDecimal;

/** Validación compartida del contrato JSON de los endpoints. */
public final class EntradaJson {
    private EntradaJson() {}

    public static String texto(JsonObject body, String campo, int maximo) {
        if (body == null || !(body.get(campo) instanceof JsonString texto)
                || texto.getString().isBlank() || texto.getString().length() > maximo) {
            throw new ApiException(400, "Revisa el campo " + campo + ".");
        }
        return texto.getString();
    }

    public static String textoOpcional(JsonObject body, String campo, int maximo) {
        if (body == null || body.get(campo) == null || body.isNull(campo)) return null;
        if (!(body.get(campo) instanceof JsonString texto) || texto.getString().length() > maximo) {
            throw new ApiException(400, "Revisa el campo " + campo + ".");
        }
        return texto.getString().isBlank() ? null : texto.getString().trim();
    }

    public static boolean booleano(JsonObject body, String campo) {
        if (body != null && (body.get(campo) == JsonValue.TRUE || body.get(campo) == JsonValue.FALSE)) {
            return body.getBoolean(campo);
        }
        throw new ApiException(400, "El campo " + campo + " debe ser verdadero o falso.");
    }

    public static int entero(JsonObject body, String campo) {
        try {
            if (body != null && body.get(campo) instanceof JsonNumber numero) return numero.intValueExact();
        } catch (ArithmeticException ignored) {
            // Fracciones y desbordamientos producen el mismo error de contrato.
        }
        throw new ApiException(400, "El campo " + campo + " debe ser un entero.");
    }

    public static BigDecimal importe(JsonObject body, String campo, boolean positivo) {
        if (body == null || !(body.get(campo) instanceof JsonNumber numero)) {
            throw new ApiException(400, "Ingresa un importe numérico en " + campo + ".");
        }
        BigDecimal valor = numero.bigDecimalValue();
        String error = ValidacionImporte.error(valor, positivo);
        if (error != null || valor.precision() - valor.scale() > 10) {
            throw new ApiException(400,
                    "Revisa " + campo + ": importe fuera de rango o con más de dos decimales.");
        }
        return valor;
    }
}
