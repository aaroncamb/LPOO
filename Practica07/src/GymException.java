import java.time.LocalDateTime;

/**
 * Práctica 7 — Excepción raíz de las operaciones del gimnasio.
 *
 * Extiende Exception (NO RuntimeException) porque representa errores
 * del mundo externo que el llamador SI puede y debe manejar:
 *   - Un cliente intenta pagar y la tarjeta es rechazada (no es bug,
 *     es realidad del banco).
 *   - Una clase grupal esta llena y un nuevo cliente quiere inscribirse.
 *   - Un cliente intenta usar una membresia vencida.
 *
 * En todos los casos la aplicacion debe poder REACCIONAR (mostrar un
 * mensaje, sugerir otra clase, ofrecer renovacion). Por eso son
 * checked: el compilador obliga a manejarlas, lo cual es exactamente
 * lo que queremos en flujos de negocio criticos.
 *
 * Se declara abstracta porque NUNCA se debe lanzar una "GymException
 * generica". Cada problema concreto debe usar su subclase especifica.
 * Esto mejora el catch granular y el log.
 */
public abstract class GymException extends Exception {

    private static final long serialVersionUID = 1L;

    private final LocalDateTime timestamp;

    protected GymException(String mensaje) {
        super(mensaje);
        this.timestamp = LocalDateTime.now();
    }

    protected GymException(String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.timestamp = LocalDateTime.now();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Categoría general del problema, para clasificar logs y reportes.
     * Cada subclase la implementa para identificarse.
     */
    public abstract String categoria();
}
