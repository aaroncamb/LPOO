import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Práctica 10 — Programa principal.
 *
 * Orquesta la simulacion completa:
 *   1. Crea el buffer compartido (capacidad 5).
 *   2. Lanza 3 torniquetes (extends Thread).
 *   3. Lanza 25 clientes a traves de un ExecutorService (Runnables).
 *   4. Espera a que los clientes terminen de encolarse, luego espera
 *      a que el buffer se drene, y finalmente detiene los torniquetes.
 *   5. Imprime estadisticas: total procesado, distribucion por tipo,
 *      cuantos proceso cada torniquete.
 *
 * El log de ejecucion se guarda en logs/accesos.log para revision.
 */
public class Main {

    private static final int CAPACIDAD_BUFFER = 5;
    private static final int NUM_TORNIQUETES  = 3;
    private static final int NUM_CLIENTES     = 25;
    private static final int TAMANIO_POOL_CLIENTES = 8;
    private static final String ARCHIVO_LOG   = "logs/accesos.log";

    public static void main(String[] args) throws Exception {
        System.out.println("=== Simulacion concurrente: torniquetes del gimnasio ===");
        System.out.printf("  %d torniquetes, %d clientes, buffer capacidad %d%n%n",
                NUM_TORNIQUETES, NUM_CLIENTES, CAPACIDAD_BUFFER);

        prepararLog();

        BufferAccesos buffer = new BufferAccesos(CAPACIDAD_BUFFER);
        ContadorAccesos contador = new ContadorAccesos();

        // ---- 1. Lanzar torniquetes (consumidores) ----
        List<Torniquete> torniquetes = new ArrayList<>();
        for (int i = 1; i <= NUM_TORNIQUETES; i++) {
            Torniquete t = new Torniquete(i, buffer, contador, ARCHIVO_LOG);
            torniquetes.add(t);
            t.start();
        }

        // ---- 2. Lanzar clientes con ExecutorService ----
        //
        // newFixedThreadPool(N) crea N hilos reutilizables. Si lanzamos
        // 25 tareas, los hilos se reutilizan: a lo sumo N tareas
        // corriendo en paralelo. Las demas esperan en la cola interna
        // del executor.
        ExecutorService poolClientes = Executors.newFixedThreadPool(
                TAMANIO_POOL_CLIENTES,
                runnable -> {
                    Thread t = new Thread(runnable);
                    t.setName("PoolCliente");
                    return t;
                });

        String[] nombres = {
            "Ana", "Bruno", "Carolina", "David", "Elena",
            "Fernando", "Gabriela", "Hector", "Isabel", "Jorge",
            "Karla", "Luis", "Mariana", "Nicolas", "Olivia",
            "Patricio", "Quetzalli", "Raul", "Sofia", "Tomas",
            "Ulises", "Valeria", "Walter", "Ximena", "Yolanda"
        };

        for (int i = 0; i < NUM_CLIENTES; i++) {
            String nombre = nombres[i % nombres.length];
            ClienteEnFila cliente = new ClienteEnFila(i + 1, nombre, buffer);
            poolClientes.submit(cliente);

            // Pequeña pausa entre llegadas para que el patron se vea
            // en los logs. En el mundo real los clientes no llegan
            // simultaneamente todos.
            Thread.sleep(40);
        }

        // ---- 3. Cerrar el pool: no acepta tareas nuevas pero espera
        //         a que las pendientes terminen ----
        poolClientes.shutdown();
        boolean terminoOk = poolClientes.awaitTermination(30, TimeUnit.SECONDS);
        if (!terminoOk) {
            System.err.println("ATENCION: el pool no termino en 30s, forzando cierre.");
            poolClientes.shutdownNow();
        }

        // ---- 4. Esperar a que el buffer se drene ----
        // (los torniquetes siguen consumiendo)
        while (buffer.tamanio() > 0) {
            Thread.sleep(100);
        }

        // Pequeña espera adicional para que el ultimo torniquete
        // termine de procesar lo que estaba en mano.
        Thread.sleep(500);

        // ---- 5. Detener los torniquetes ----
        for (Torniquete t : torniquetes) {
            t.detener();
        }
        for (Torniquete t : torniquetes) {
            t.join();
        }

        // ---- 6. Estadisticas ----
        System.out.println("\n=== Resumen de la simulacion ===");
        System.out.println(contador);
        System.out.println("\nProcesado por cada torniquete:");
        int totalProcesado = 0;
        for (Torniquete t : torniquetes) {
            System.out.printf("  %s: %d accesos%n",
                    t.getName(), t.getProcesados());
            totalProcesado += t.getProcesados();
        }
        System.out.printf("%nTotal procesado por torniquetes: %d%n", totalProcesado);
        System.out.printf("Total en contador:               %d%n",
                contador.getTotal());

        // Invariantes que deben cumplirse despues de la corrida
        System.out.println("\n--- Invariantes ---");
        check("Total contador == clientes enviados",
                contador.getTotal() == NUM_CLIENTES,
                contador.getTotal() + " vs " + NUM_CLIENTES);
        check("Total procesado torniquetes == total contador",
                totalProcesado == contador.getTotal(),
                totalProcesado + " vs " + contador.getTotal());
        check("Buffer vacio al final",
                buffer.tamanio() == 0,
                String.valueOf(buffer.tamanio()));
        check("Total producido == total consumido en buffer",
                buffer.getTotalProducidos() == buffer.getTotalConsumidos(),
                buffer.getTotalProducidos() + " vs " + buffer.getTotalConsumidos());

        System.out.println("\nLog de accesos guardado en: " + ARCHIVO_LOG);
    }

    private static void prepararLog() throws IOException {
        Path logPath = Path.of(ARCHIVO_LOG);
        Files.createDirectories(logPath.getParent());
        Files.deleteIfExists(logPath);
    }

    private static void check(String descripcion, boolean cond, String detalle) {
        System.out.printf("  [%s] %s%s%n",
                cond ? "OK  " : "FAIL",
                descripcion,
                cond ? "" : "  (" + detalle + ")");
    }
}
