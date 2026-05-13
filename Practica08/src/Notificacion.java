import java.time.LocalDateTime;

/**
 * Práctica 8 — Notificacion pendiente de envío.
 *
 * Se usa como elemento de una cola FIFO (LinkedList<Notificacion>): se
 * agregan al final y se procesan por el frente. Inmutable: cada
 * notificacion representa un hecho consumado al momento de creacion.
 */
public class Notificacion {

    public enum Canal { EMAIL, SMS, PUSH }

    private final int destinatarioId;
    private final String mensaje;
    private final Canal canal;
    private final LocalDateTime momento;

    public Notificacion(int destinatarioId, String mensaje, Canal canal) {
        this.destinatarioId = destinatarioId;
        this.mensaje = mensaje;
        this.canal = canal;
        this.momento = LocalDateTime.now();
    }

    public int getDestinatarioId()  { return destinatarioId; }
    public String getMensaje()      { return mensaje; }
    public Canal getCanal()         { return canal; }
    public LocalDateTime getMomento() { return momento; }

    @Override
    public String toString() {
        return String.format("[%s → cliente %d] %s",
                canal, destinatarioId, mensaje);
    }
}
