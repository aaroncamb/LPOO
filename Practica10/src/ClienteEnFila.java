import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Práctica 10 — Cliente que llega al gimnasio.
 *
 * Implementa Runnable: cada cliente es una "tarea" a ejecutar (llegar,
 * encolarse en el buffer). No es una entidad con identidad permanente
 * como el Torniquete; es un evento puntual.
 *
 * Por que `implements Runnable` y no `extends Thread`:
 *   El cliente no es el hilo, es la TAREA del hilo. El hilo viene del
 *   ExecutorService que lo ejecuta. Esta separacion permite:
 *   - Reutilizar hilos del pool (50 clientes pueden compartir 10 hilos).
 *   - Cambiar la estrategia de ejecucion sin tocar la logica.
 *   - El executor maneja la vida del hilo, no nosotros.
 *
 * Es el patron preferido en codigo profesional: Runnable describe el
 * trabajo, el executor decide quien y como lo ejecuta.
 */
public class ClienteEnFila implements Runnable {

    private static final DateTimeFormatter TS_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final int idCliente;
    private final String nombreCliente;
    private final BufferAccesos buffer;

    public ClienteEnFila(int idCliente, String nombreCliente,
                          BufferAccesos buffer) {
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.buffer = buffer;
    }

    @Override
    public void run() {
        try {
            AccesoSolicitado acceso = new AccesoSolicitado(idCliente, nombreCliente);
            log("llego a la fila");
            // Si el buffer esta lleno, depositar bloquea hasta que haya
            // espacio (wait() interno).
            buffer.depositar(acceso);
            log("encolado en el buffer");
        } catch (InterruptedException e) {
            // Si nos interrumpieron mientras esperabamos espacio en el
            // buffer, salimos limpiamente.
            Thread.currentThread().interrupt();
            log("interrumpido antes de encolarse");
        }
    }

    private void log(String mensaje) {
        System.out.printf("[%s][cliente %d (%s)] %s%n",
                LocalDateTime.now().format(TS_FORMAT),
                idCliente, nombreCliente, mensaje);
    }
}
