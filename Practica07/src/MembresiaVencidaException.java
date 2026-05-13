import java.time.LocalDate;

/**
 * Práctica 7 — Excepción cuando un cliente intenta hacer una operacion
 * (entrar al gimnasio, reservar una clase, hacerse una evaluacion) con
 * una membresia que ya vencio.
 *
 * Contexto incluido: id del cliente y fecha de vencimiento, para que el
 * flujo pueda redirigirlo a renovacion sin tener que volver a consultar
 * la base de datos.
 */
public class MembresiaVencidaException extends GymException {

    private static final long serialVersionUID = 1L;

    private final String clienteNombre;
    private final LocalDate fechaVencimiento;
    private final long diasDeVencida;

    public MembresiaVencidaException(String clienteNombre, LocalDate fechaVencimiento) {
        super(String.format(
            "La membresia de '%s' vencio el %s (hace %d dias).",
            clienteNombre, fechaVencimiento,
            java.time.temporal.ChronoUnit.DAYS.between(fechaVencimiento, LocalDate.now())));
        this.clienteNombre    = clienteNombre;
        this.fechaVencimiento = fechaVencimiento;
        this.diasDeVencida    = java.time.temporal.ChronoUnit.DAYS.between(
                fechaVencimiento, LocalDate.now());
    }

    public String getClienteNombre()         { return clienteNombre; }
    public LocalDate getFechaVencimiento()   { return fechaVencimiento; }
    public long getDiasDeVencida()           { return diasDeVencida; }

    @Override
    public String categoria() {
        return "MEMBRESIA_VENCIDA";
    }
}
