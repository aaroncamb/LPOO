import java.time.LocalDate;

/**
 * Práctica 5 — Servicio de evaluacion fisica.
 *
 * Mide IMC, fuerza, resistencia, porcentaje de grasa. El gimnasio
 * la ofrece como servicio puntual (no recurrente) y suele regalarla
 * a clientes con membresia activa.
 *
 * Caracteristicas distintivas:
 *   - Sin IVA (servicio de salud-fitness).
 *   - Validacion: solo mayores de 14 años.
 *   - Sobrecargas: porcentual, por edad (jovenes/seniors), por
 *     primera evaluacion (gratis).
 */
public class EvaluacionFisica extends Servicio {

    private final int edadCliente;
    private final boolean primeraEvaluacion;

    public EvaluacionFisica(String nombre, String cliente, LocalDate fecha,
                             double precioBase, int edadCliente,
                             boolean primeraEvaluacion) {
        super(nombre, cliente, fecha, precioBase);
        if (edadCliente < 0 || edadCliente > 120) {
            throw new IllegalArgumentException(
                "Edad fuera de rango (0-120). Recibida: " + edadCliente);
        }
        this.edadCliente       = edadCliente;
        this.primeraEvaluacion = primeraEvaluacion;
    }

    // ---------- OVERRIDE: las evaluaciones no tienen IVA ----------
    //
    // Las evaluaciones medicas/fitness estan exentas de IVA segun la
    // politica del negocio. Sobrescribir el metodo concreto del padre
    // es legal y comun cuando la regla general no aplica a este caso.

    @Override
    public double calcularImpuestos() {
        return 0.0;   // exento
    }

    @Override
    public boolean validarCliente() {
        if (fechaServicio.isBefore(LocalDate.now())) return false;
        // Politica del gimnasio: no se evaluan menores de 14.
        return edadCliente >= 14;
    }

    @Override
    public String emitirComprobante() {
        return String.format(
            "COMPROBANTE - Evaluacion Fisica%n" +
            "  Cliente:     %s (edad %d)%n" +
            "  Evaluacion:  %s%n" +
            "  Fecha:       %s%n" +
            "  Subtotal:    $%.2f%n" +
            "  IVA:         exento%n" +
            "  TOTAL:       $%.2f%n" +
            "  %s",
            clienteNombre, edadCliente, nombreServicio, fechaServicio,
            calcularSubtotal(), calcularTotal(),
            primeraEvaluacion ? "(Primera evaluacion del cliente)" : "");
    }

    @Override
    public String tipoServicio() {
        return "Evaluacion Fisica";
    }

    // ---------- SOBRECARGAS de aplicarDescuento ----------

    /**
     * Sobrecarga 2: descuento automatico por categoria etaria.
     * Jovenes (14-17): 30%. Seniors (60+): 40%. Otros: sin descuento.
     */
    public double aplicarDescuento(boolean activarPorEdad) {
        if (!activarPorEdad) return calcularSubtotal();

        double pct;
        if      (edadCliente >= 14 && edadCliente <= 17) pct = 0.30;
        else if (edadCliente >= 60)                       pct = 0.40;
        else                                              pct = 0.00;

        return super.aplicarDescuento(pct);
    }

    /**
     * Sobrecarga 3: si es la primera evaluacion del cliente, gratis.
     * Si no es primera, lanza excepcion para que el cajero use otra
     * forma de descuento.
     */
    public double aplicarDescuento(String motivo) {
        if ("primera".equalsIgnoreCase(motivo) && primeraEvaluacion) {
            return super.aplicarDescuento(1.0);   // 100% de descuento
        }
        throw new IllegalArgumentException(
            "Solo aplica si motivo='primera' y es la primera evaluacion del cliente. "
            + "Motivo='" + motivo + "', primera=" + primeraEvaluacion);
    }

    // ---------- Getters propios ----------

    public int getEdadCliente()           { return edadCliente; }
    public boolean isPrimeraEvaluacion()  { return primeraEvaluacion; }
}
