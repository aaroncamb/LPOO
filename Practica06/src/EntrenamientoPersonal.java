import java.time.LocalDate;

/**
 * Práctica 6 — EntrenamientoPersonal.
 *
 * Combinacion de interfaces: Notificable + Reagendable.
 *
 * NO implementa Reportable porque los reportes operacionales del
 * gimnasio se centran en metricas agregadas: cuantos asistieron a yoga,
 * cuantas evaluaciones se hicieron en mayo, ingresos por categoria.
 * Los entrenamientos personales son servicios premium hechos a medida;
 * cada sesion es unica y sus metricas viven en otro canal (el CRM o el
 * historial del cliente, no en los reportes operacionales).
 *
 * Si implementaramos Reportable solo por consistencia, el reporte
 * gerencial se inundaria con cientos de filas individuales sin valor
 * comparativo, ahogando las metricas realmente accionables.
 */
public class EntrenamientoPersonal extends Servicio
        implements Notificable, Reagendable {

    private final String entrenador;
    private final int duracionMinutos;

    /** Entrenamientos requieren al menos 1 dia de anticipacion. */
    private static final int ANTICIPACION_MINIMA_DIAS = 1;

    public EntrenamientoPersonal(String nombre, String cliente, LocalDate fecha,
                                  double precio, String entrenador,
                                  int duracionMinutos) {
        super(nombre, cliente, fecha, precio);
        if (entrenador == null || entrenador.isBlank())
            throw new IllegalArgumentException("El entrenador es obligatorio.");
        if (duracionMinutos < 30 || duracionMinutos > 180)
            throw new IllegalArgumentException("Duracion 30-180 min.");
        this.entrenador      = entrenador.trim();
        this.duracionMinutos = duracionMinutos;
    }

    // -------- Implementacion de Servicio (abstractos) --------

    @Override
    public boolean validarCliente() {
        return !fechaServicio.isBefore(LocalDate.now());
    }

    @Override
    public String emitirComprobante() {
        return String.format("[Entrenamiento] %s con %s, %d min ($%.2f)",
                clienteNombre, entrenador, duracionMinutos, calcularTotal());
    }

    @Override
    public String tipoServicio() { return "Entrenamiento Personal"; }

    // -------- Implementacion de Notificable --------

    @Override
    public String destinatario() { return clienteNombre; }

    @Override
    public boolean enviarEmail(String asunto, String cuerpo) {
        System.out.printf("  [EMAIL → %s] %s%n", destinatario(), asunto);
        return true;
    }

    @Override
    public boolean enviarSMS(String mensaje) {
        System.out.printf("  [SMS   → %s] %s%n", destinatario(), mensaje);
        return true;
    }

    // -------- Implementacion de Reagendable --------

    @Override
    public boolean reagendar(LocalDate nuevaFecha) {
        // Uso el metodo default fechaRespetaAnticipacion() de la
        // interfaz para no duplicar el calculo aqui.
        if (!fechaRespetaAnticipacion(nuevaFecha)) return false;
        this.fechaServicio = nuevaFecha;
        agregarNota("Reagendado a " + nuevaFecha);
        return true;
    }

    @Override
    public int diasAnticipacionMinima() { return ANTICIPACION_MINIMA_DIAS; }

    // -------- Operaciones propias --------

    public String getEntrenador()    { return entrenador; }
    public int getDuracionMinutos()  { return duracionMinutos; }
}
