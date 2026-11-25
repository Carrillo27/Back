package extra.validacion;

import java.math.BigDecimal;

/** Reglas compartidas por el formulario y la persistencia de caja. */
public final class ValidacionImporte {
    private ValidacionImporte() { }

    public static String error(BigDecimal importe, boolean debeSerPositivo) {
        if (importe == null) return "Debes ingresar un importe.";
        if (importe.scale() > 2) return "El importe debe tener máximo dos decimales.";
        if (debeSerPositivo && importe.signum() <= 0) return "El importe debe ser mayor que cero.";
        if (!debeSerPositivo && importe.signum() < 0) return "El importe no puede ser negativo.";
        return null;
    }
}
