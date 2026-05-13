import java.time.LocalDate;

/**
 * Práctica 6 — ClaseGrupal.
 *
 * Combinacion de interfaces: Notificable + Reportable.
 *
 * NO implementa Reagendable porque las clases grupales son horarios
 * publicos del gimnasio (yoga martes 7am, spinning miercoles 6pm).
 * Cuando un cliente no puede asistir, NO se mueve el horario para los
 * demas inscritos: el cliente cancela su inscripcion y se reinscribe
 * en otra clase distinta. Reagendar una clase grupal seria reagendar
 * para todos, lo cual rompe la naturaleza del servicio.
 *
 * Si implementaramos Reagendable aqui solo "porque se puede", la
 * implementacion estaria mintiendo: o no haria nada (devolver true sin
 * mover nada), o moveria el horario afectando al grupo entero. Ambas
 * opciones violarian el contrato. Mejor no implementarla.
 */
public class ClaseGrupal extends Servicio implements Notificable, Reportable {

    private final int cupoMaximo;
    private int inscritos;
    private int asistentesReales;   // se llena al cerrar la clase

    public ClaseGrupal(String nombre, String cliente, LocalDate fecha,
                       double precio, int cupoMaximo) {
        super(nombre, cliente, fecha, precio);
        if (cupoMaximo <= 0)
            throw new IllegalArgumentException("Cupo maximo debe ser positivo.");
        this.cupoMaximo = cupoMaximo;
    }

    // -------- Implementacion de Servicio (abstractos) --------

    @Override
    public boolean validarCliente() {
        if (fechaServicio.isBefore(LocalDate.now())) return false;
        return inscritos < cupoMaximo;
    }

    @Override
    public String emitirComprobante() {
        return String.format("[ClaseGrupal] %s - %s ($%.2f)",
                nombreServicio, clienteNombre, calcularTotal());
    }

    @Override
    public String tipoServicio() { return "Clase Grupal"; }

    // -------- Implementacion de Notificable --------

    @Override
    public String destinatario() {
        return "Grupo " + nombreServicio + " (" + clienteNombre + ")";
    }

    @Override
    public boolean enviarEmail(String asunto, String cuerpo) {
        // Implementacion stub que simula envio. En un sistema real
        // aqui se llamaria a un servicio SMTP.
        System.out.printf("  [EMAIL → %s] Asunto: %s%n", destinatario(), asunto);
        return true;
    }

    @Override
    public boolean enviarSMS(String mensaje) {
        System.out.printf("  [SMS   → %s] %s%n", destinatario(), mensaje);
        return true;
    }

    // -------- Implementacion de Reportable --------

    @Override
    public String tituloReporte()           { return nombreServicio; }
    @Override
    public LocalDate fechaParaReporte()     { return fechaServicio; }
    @Override
    public double montoFacturado()          { return calcularTotal(); }
    @Override
    public String categoriaReporte()        { return "clase-grupal"; }

    // -------- Operaciones propias --------

    public void inscribirAsistente() {
        if (inscritos >= cupoMaximo)
            throw new IllegalStateException("Clase llena: " + nombreServicio);
        inscritos++;
    }

    public void registrarAsistencia(int asistentesReales) {
        if (asistentesReales < 0 || asistentesReales > cupoMaximo)
            throw new IllegalArgumentException("Asistencia fuera de rango.");
        this.asistentesReales = asistentesReales;
    }

    public int getInscritos()         { return inscritos; }
    public int getAsistentesReales()  { return asistentesReales; }
    public int getCupoMaximo()        { return cupoMaximo; }
}
