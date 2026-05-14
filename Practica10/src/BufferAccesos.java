import java.util.LinkedList;
import java.util.Queue;

/**
 * Práctica 10 — Buffer compartido entre productores y consumidores.
 *
 * Implementa el patron clasico Productor-Consumidor:
 *   - Productores (clientes que llegan) llaman a depositar(acceso).
 *   - Consumidores (torniquetes) llaman a tomar() para procesar uno.
 *
 * Garantias:
 *   - Si el buffer esta LLENO, depositar() espera hasta que haya espacio.
 *   - Si el buffer esta VACIO, tomar() espera hasta que llegue algo.
 *
 * SINCRONIZACION:
 *   - Cada metodo es `synchronized` -> solo un hilo a la vez puede
 *     manipular la cola interna.
 *   - wait() libera el monitor mientras espera, permitiendo que OTROS
 *     hilos entren a depositar() o tomar(). Si usara Thread.sleep(),
 *     el hilo seguiria sosteniendo el monitor y todos los demas
 *     quedarian bloqueados → deadlock garantizado.
 *   - notifyAll() despierta a TODOS los hilos esperando. Aqui es
 *     critico usar notifyAll y no notify porque tenemos productores
 *     esperando en depositar() (cuando esta lleno) y consumidores
 *     esperando en tomar() (cuando esta vacio); notify podria despertar
 *     al hilo equivocado y dejar a otro durmiendo eternamente.
 *
 * El bucle `while (cond)` en lugar de `if (cond)` es esencial: cuando
 * un hilo se despierta de wait(), debe REVERIFICAR la condicion porque
 * otro hilo pudo haber consumido el elemento entre el notify y el
 * regreso de wait. Esto se llama "spurious wakeup" y es uno de los
 * errores clasicos de concurrencia.
 */
public class BufferAccesos {

    private final Queue<AccesoSolicitado> cola = new LinkedList<>();
    private final int capacidadMaxima;
    private long totalProducidos = 0;
    private long totalConsumidos = 0;

    public BufferAccesos(int capacidadMaxima) {
        if (capacidadMaxima <= 0) {
            throw new IllegalArgumentException("capacidad debe ser positiva");
        }
        this.capacidadMaxima = capacidadMaxima;
    }

    /**
     * Producir: agrega un acceso al final de la cola.
     * Si el buffer esta lleno, espera (libera el monitor con wait).
     */
    public synchronized void depositar(AccesoSolicitado acceso) throws InterruptedException {
        while (cola.size() >= capacidadMaxima) {
            log("buffer LLENO, productor espera");
            wait();  // libera el monitor; quedo dormido hasta notifyAll
        }
        cola.offer(acceso);
        totalProducidos++;
        log("DEPOSITO " + acceso + " (cola=" + cola.size() + "/" + capacidadMaxima + ")");
        notifyAll();   // despierto a posibles consumidores que esperan en tomar()
    }

    /**
     * Consumir: saca un acceso del frente de la cola.
     * Si el buffer esta vacio, espera (libera el monitor con wait).
     */
    public synchronized AccesoSolicitado tomar() throws InterruptedException {
        while (cola.isEmpty()) {
            log("buffer VACIO, consumidor espera");
            wait();  // libera el monitor; quedo dormido hasta notifyAll
        }
        AccesoSolicitado a = cola.poll();
        totalConsumidos++;
        log("TOMA " + a + " (cola=" + cola.size() + "/" + capacidadMaxima + ")");
        notifyAll();   // despierto a posibles productores que esperan en depositar()
        return a;
    }

    public synchronized int tamanio()         { return cola.size(); }
    public synchronized long getTotalProducidos() { return totalProducidos; }
    public synchronized long getTotalConsumidos() { return totalConsumidos; }

    private void log(String mensaje) {
        // El System.out es thread-safe a nivel de println pero no
        // garantiza orden estricto. Para una simulacion didactica es
        // suficiente.
        System.out.printf("[%s][buffer] %s%n",
                Thread.currentThread().getName(), mensaje);
    }
}
