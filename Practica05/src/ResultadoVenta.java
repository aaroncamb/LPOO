/**
 * Práctica 5 — Resultado de una operacion procesarVenta().
 *
 * Encapsula los tres datos que el llamador necesita despues de
 * intentar cobrar un servicio: si tuvo exito, cuanto se cobro, y un
 * mensaje (comprobante si exito, error si fallo).
 *
 * Inmutable: los campos son final y no hay setters. Una vez creado,
 * representa un hecho consumado de venta.
 */
public class ResultadoVenta {

    private final boolean exitoso;
    private final double  totalCobrado;
    private final String  mensaje;

    public ResultadoVenta(boolean exitoso, double totalCobrado, String mensaje) {
        this.exitoso      = exitoso;
        this.totalCobrado = totalCobrado;
        this.mensaje      = mensaje;
    }

    public boolean isExitoso()    { return exitoso; }
    public double getTotalCobrado() { return totalCobrado; }
    public String getMensaje()    { return mensaje; }

    @Override
    public String toString() {
        if (exitoso) {
            return String.format("OK ($%.2f) - %s", totalCobrado, mensaje);
        }
        return "FALLO - " + mensaje;
    }
}
