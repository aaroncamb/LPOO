import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Práctica 10 — Torniquete del gimnasio.
 *
 * Extiende Thread: cada torniquete corre en su propio hilo. Toma
 * accesos del buffer compartido, simula el tiempo de validacion
 * (Thread.sleep) y registra el acceso en el contador.
 *
 * Por que `extends Thread` y no `implements Runnable`:
 *   La consigna pide al menos una clase de cada. Aqui uso Thread
 *   porque el torniquete TIENE una identidad propia (tiene id, nombre,
 *   estadisticas internas), no es solo "una tarea a ejecutar". Cuando
 *   la cosa que corre es una entidad con estado, Thread se siente mas
 *   natural; cuando es solo "ejecuta esto", Runnable se siente mejor.
 *
 *   En produccion la recomendacion estandar es preferir Runnable +
 *   Thread (composicion sobre herencia), pero para esta practica
 *   ambos enfoques son didacticamente validos.
 */
public class Torniquete extends Thread {

    private static final DateTimeFormatter TS_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final int idTorniquete;
    private final BufferAccesos buffer;
    private final ContadorAccesos contador;
    private final String archivoLog;

    /** Tiempo simulado de validacion (ms). Variable para realismo. */
    private final int tiempoMinMs;
    private final int tiempoMaxMs;

    private int procesados = 0;
    private volatile boolean detenerse = false;

    public Torniquete(int idTorniquete, BufferAccesos buffer,
                      ContadorAccesos contador, String archivoLog) {
        // setName del Thread: aparece en los logs y facilita debuggear.
        super("Torniquete-" + idTorniquete);
        this.idTorniquete = idTorniquete;
        this.buffer = buffer;
        this.contador = contador;
        this.archivoLog = archivoLog;
        this.tiempoMinMs = 80;
        this.tiempoMaxMs = 250;
    }

    /**
     * Senial cooperativa de detencion. El torniquete revisa esta bandera
     * y termina ordenadamente cuando esta en true.
     *
     * `volatile` garantiza que el cambio de detenerse=true desde otro
     * hilo sea visible inmediatamente para este hilo (sin volatile, el
     * compilador podria cachear el valor en un registro y nunca verlo
     * cambiar).
     */
    public void detener() {
        this.detenerse = true;
        this.interrupt();  // despierta si estaba en wait/sleep
    }

    @Override
    public void run() {
        log("Torniquete iniciado");
        while (!detenerse) {
            try {
                // Tomar del buffer. Si el buffer esta vacio, wait() libera
                // el monitor y este hilo queda dormido hasta que llegue
                // un acceso o nos interrumpan.
                AccesoSolicitado a = buffer.tomar();

                // Simular tiempo de validacion (lectura de tarjeta, etc).
                // Usamos sleep aqui porque solo queremos pausar este
                // torniquete; no estamos esperando una condicion, solo
                // imitando latencia.
                int latencia = ThreadLocalRandom.current()
                        .nextInt(tiempoMinMs, tiempoMaxMs + 1);
                Thread.sleep(latencia);

                // Decidir tipo de membresia (simulado).
                String tipo = simularTipoMembresia();

                // Registrar en el contador compartido (synchronized).
                contador.registrar(tipo);
                procesados++;

                long espera = Duration.between(a.getMomentoLlegada(),
                        LocalDateTime.now()).toMillis();
                String linea = String.format(
                        "Torniquete-%d proceso %s [%s, espera=%dms]",
                        idTorniquete, a, tipo, espera);
                log(linea);
                escribirLog(linea);

            } catch (InterruptedException e) {
                // Si nos interrumpieron mientras esperabamos en wait()
                // o sleep(), salimos del bucle ordenadamente.
                Thread.currentThread().interrupt();  // re-marcar el flag
                break;
            }
        }
        log("Torniquete detenido (proceso " + procesados + " accesos)");
    }

    private String simularTipoMembresia() {
        int dado = new Random().nextInt(10);
        if (dado < 5) return "BASICA";
        if (dado < 8) return "PREMIUM";
        return "VIP";
    }

    public int getProcesados() { return procesados; }

    private void log(String mensaje) {
        System.out.printf("[%s][T%d] %s%n",
                LocalDateTime.now().format(TS_FORMAT),
                idTorniquete, mensaje);
    }

    /**
     * Escribir al archivo de log compartido. Sincronizamos sobre la
     * clase para que dos torniquetes no escriban interleaved.
     */
    private static final Object LOCK_ARCHIVO = new Object();
    private void escribirLog(String mensaje) {
        if (archivoLog == null) return;
        synchronized (LOCK_ARCHIVO) {
            try (BufferedWriter w = new BufferedWriter(
                    new FileWriter(archivoLog, true))) {
                w.write(String.format("[%s] %s%n",
                        LocalDateTime.now().format(TS_FORMAT), mensaje));
            } catch (IOException e) {
                System.err.println("No se pudo escribir log: " + e.getMessage());
            }
        }
    }
}
