import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Práctica 5 — Servicio de entrenamiento personal (uno a uno).
 *
 * Caracteristicas distintivas:
 *   - Tiene un entrenador asignado.
 *   - Tiene duracion en minutos (afecta el precio: extra por sesion larga).
 *   - Las sobrecargas de descuento aqui responden al modelo del negocio:
 *     porcentual, descuento por horario "valle" (manana temprano), o
 *     paquete de N sesiones (descuento por volumen).
 */
public class EntrenamientoPersonal extends Servicio {

    private final String entrenador;
    private final int    duracionMinutos;
    private final LocalTime horaInicio;

    /** Recargo por sesion de mas de 60 min. */
    private static final double RECARGO_SESION_LARGA = 1.20;

    public EntrenamientoPersonal(String nombre, String cliente, LocalDate fecha,
                                  double precioBase, String entrenador,
                                  int duracionMinutos, LocalTime horaInicio) {
        super(nombre, cliente, fecha, precioBase);
        if (entrenador == null || entrenador.isBlank()) {
            throw new IllegalArgumentException("El entrenador es obligatorio.");
        }
        if (duracionMinutos < 30 || duracionMinutos > 180) {
            throw new IllegalArgumentException(
                "Duracion fuera de rango (30-180 min). Recibida: " + duracionMinutos);
        }
        this.entrenador      = entrenador.trim();
        this.duracionMinutos = duracionMinutos;
        this.horaInicio      = horaInicio;
    }

    // ---------- Sobrescritura de calcularSubtotal ----------
    //
    // Sesiones de >60 min llevan 20% extra antes de descuentos.
    // Esto demuestra OVERRIDE (no overload): mismo metodo del padre,
    // mismo nombre y parametros, comportamiento distinto.

    @Override
    public double calcularSubtotal() {
        double base = precioBase;
        if (duracionMinutos > 60) {
            base *= RECARGO_SESION_LARGA;
        }
        return Math.max(0, base - descuentoAplicado);
    }

    @Override
    public boolean validarCliente() {
        // Solo se vende si la fecha es valida y el horario no choca con
        // las horas de cierre del gimnasio (asumimos 5am-23pm).
        if (fechaServicio.isBefore(LocalDate.now())) return false;
        if (horaInicio == null) return false;
        if (horaInicio.isBefore(LocalTime.of(5, 0))) return false;
        if (horaInicio.isAfter(LocalTime.of(23, 0))) return false;
        return true;
    }

    @Override
    public String emitirComprobante() {
        return String.format(
            "COMPROBANTE - Entrenamiento Personal%n" +
            "  Cliente:     %s%n" +
            "  Entrenador:  %s%n" +
            "  Fecha:       %s a las %s%n" +
            "  Duracion:    %d min%n" +
            "  Subtotal:    $%.2f%n" +
            "  IVA:         $%.2f%n" +
            "  TOTAL:       $%.2f",
            clienteNombre, entrenador, fechaServicio, horaInicio, duracionMinutos,
            calcularSubtotal(), calcularImpuestos(), calcularTotal());
    }

    @Override
    public String tipoServicio() {
        return "Entrenamiento Personal";
    }

    // ---------- SOBRECARGAS de aplicarDescuento ----------

    /**
     * Sobrecarga 2: descuento por horario valle.
     * Si la hora de inicio cae entre 6am y 9am, se aplica 25% automatico
     * (horario valle del gimnasio). El argumento boolean indica si se
     * desea forzar esta validacion.
     */
    public double aplicarDescuento(boolean horarioValle) {
        if (!horarioValle) {
            return calcularSubtotal();
        }
        if (horaInicio == null
                || horaInicio.isBefore(LocalTime.of(6, 0))
                || horaInicio.isAfter(LocalTime.of(9, 0))) {
            throw new IllegalArgumentException(
                "El descuento de horario valle aplica solo entre 6:00 y 9:00. "
                + "Hora actual: " + horaInicio);
        }
        return super.aplicarDescuento(0.25);
    }

    /**
     * Sobrecarga 3: descuento por paquete de N sesiones.
     * Compra >= 4 sesiones: 15% directo (precio total ya pactado).
     */
    public double aplicarDescuento(int sesionesEnPaquete) {
        if (sesionesEnPaquete <= 0) {
            throw new IllegalArgumentException(
                "El paquete debe tener al menos 1 sesion.");
        }
        double pct;
        if      (sesionesEnPaquete >= 10) pct = 0.20;
        else if (sesionesEnPaquete >=  4) pct = 0.15;
        else if (sesionesEnPaquete >=  2) pct = 0.05;
        else                              pct = 0.00;
        return super.aplicarDescuento(pct);
    }

    // ---------- Getters propios ----------

    public String getEntrenador()  { return entrenador; }
    public int getDuracionMinutos() { return duracionMinutos; }
    public LocalTime getHoraInicio() { return horaInicio; }
}
