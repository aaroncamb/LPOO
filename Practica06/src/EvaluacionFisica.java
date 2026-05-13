import java.time.LocalDate;

/**
 * Práctica 6 — EvaluacionFisica.
 *
 * Combinacion de interfaces: Notificable + Reportable + Reagendable.
 *
 * Es la unica clase que implementa las TRES interfaces, y lo hace por
 * razones del dominio:
 *   - Notificable: el cliente necesita recibir sus resultados (peso,
 *     IMC, recomendaciones) por email o SMS.
 *   - Reportable: los datos agregados (IMC promedio, distribuciones de
 *     grasa corporal) son insumo de valor para decisiones gerenciales
 *     y campanias de salud poblacional del gimnasio.
 *   - Reagendable: a diferencia de las clases grupales, las
 *     evaluaciones son citas individuales. Si el cliente se enferma o
 *     no puede el dia, simplemente se mueve a otra fecha.
 *
 * Las evaluaciones requieren mas anticipacion (3 dias) porque el
 * especialista necesita planear su agenda. Las clases grupales no
 * importarian, pero como esta clase si es Reagendable, la regla aplica.
 */
public class EvaluacionFisica extends Servicio
        implements Notificable, Reportable, Reagendable {

    private final int edadCliente;
    private double imcMedido;
    private double porcentajeGrasaMedido;

    private static final int ANTICIPACION_MINIMA_DIAS = 3;

    public EvaluacionFisica(String nombre, String cliente, LocalDate fecha,
                             double precio, int edadCliente) {
        super(nombre, cliente, fecha, precio);
        if (edadCliente < 0 || edadCliente > 120)
            throw new IllegalArgumentException("Edad fuera de rango.");
        this.edadCliente = edadCliente;
    }

    // -------- Implementacion de Servicio (abstractos) --------

    @Override
    public boolean validarCliente() {
        if (fechaServicio.isBefore(LocalDate.now())) return false;
        return edadCliente >= 14;
    }

    @Override
    public String emitirComprobante() {
        return String.format("[Evaluacion] %s (edad %d) - $%.2f%s",
                clienteNombre, edadCliente, calcularTotal(),
                imcMedido > 0 ? " IMC=" + String.format("%.1f", imcMedido) : "");
    }

    @Override
    public String tipoServicio() { return "Evaluacion Fisica"; }

    @Override
    public double calcularImpuestos() {
        return 0.0;   // servicios de salud-fitness exentos
    }

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

    // -------- Implementacion de Reportable --------

    @Override
    public String tituloReporte()       { return "Eval " + clienteNombre; }
    @Override
    public LocalDate fechaParaReporte() { return fechaServicio; }
    @Override
    public double montoFacturado()      { return calcularTotal(); }
    @Override
    public String categoriaReporte()    { return "evaluacion"; }

    // -------- Implementacion de Reagendable --------

    @Override
    public boolean reagendar(LocalDate nuevaFecha) {
        if (!fechaRespetaAnticipacion(nuevaFecha)) return false;
        this.fechaServicio = nuevaFecha;
        agregarNota("Reagendado a " + nuevaFecha);
        return true;
    }

    @Override
    public int diasAnticipacionMinima() { return ANTICIPACION_MINIMA_DIAS; }

    // -------- Operaciones propias --------

    public void registrarMediciones(double imc, double porcentajeGrasa) {
        if (imc < 10 || imc > 60)
            throw new IllegalArgumentException("IMC fuera de rango sensato.");
        if (porcentajeGrasa < 0 || porcentajeGrasa > 70)
            throw new IllegalArgumentException("% de grasa fuera de rango.");
        this.imcMedido = imc;
        this.porcentajeGrasaMedido = porcentajeGrasa;
    }

    public int getEdadCliente()            { return edadCliente; }
    public double getImcMedido()           { return imcMedido; }
    public double getPorcentajeGrasaMedido() { return porcentajeGrasaMedido; }
}
