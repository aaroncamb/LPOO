import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Práctica 10 — Pruebas unitarias del comportamiento concurrente.
 *
 * Probar concurrencia es DIFICIL porque los bugs son no deterministicos:
 * una corrida puede pasar y otra fallar. La estrategia es:
 *   - Repetir cada operacion miles de veces para forzar la race.
 *   - Verificar INVARIANTES (cosas que deben ser ciertas siempre,
 *     no propiedades especificas).
 *   - Limitar el tiempo de espera con timeouts.
 */
public class ConcurrenciaTest {

    private static int pasadas = 0;
    private static int falladas = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== Pruebas - P10 Concurrencia ===\n");

        pruebaContadorSincronizadoEsCorrecto();
        pruebaContadorSinSincronizarPierdeIncrementos();
        pruebaBufferProductorConsumidorSinPerdidas();
        pruebaBufferRespetaCapacidadMaxima();
        pruebaTorniqueteSeDetieneOrdenadamente();
        pruebaMultiplesTorniquetesProcesanTodosLosAccesos();
        pruebaExecutorServiceLanzaRunnables();

        System.out.println("\n=== Resumen ===");
        System.out.println("Pasadas:  " + pasadas);
        System.out.println("Falladas: " + falladas);
        System.out.println("Total:    " + (pasadas + falladas));

        if (falladas > 0) System.exit(1);
    }

    // ---------- ContadorAccesos ----------

    /**
     * Con synchronized, el contador debe llegar exactamente al valor
     * esperado, sin perdidas.
     */
    private static void pruebaContadorSincronizadoEsCorrecto() throws InterruptedException {
        int N_HILOS = 10;
        int INCS = 1_000;
        ContadorAccesos c = new ContadorAccesos();

        List<Thread> hilos = new ArrayList<>();
        for (int i = 0; i < N_HILOS; i++) {
            hilos.add(new Thread(() -> {
                for (int j = 0; j < INCS; j++) c.registrar("BASICA");
            }));
        }
        for (Thread t : hilos) t.start();
        for (Thread t : hilos) t.join();

        int esperado = N_HILOS * INCS;
        check(c.getTotal() == esperado,
              "Contador con synchronized: " + esperado + " incrementos llegan completos",
              c.getTotal());
    }

    /**
     * Sin synchronized, casi siempre se pierden incrementos. Si por
     * casualidad esta corrida no pierde ninguno, no es FAIL: es ruido
     * del scheduling, no un bug.
     */
    private static void pruebaContadorSinSincronizarPierdeIncrementos() throws InterruptedException {
        int N_HILOS = 10;
        int INCS = 10_000;
        ContadorAccesos c = new ContadorAccesos();

        List<Thread> hilos = new ArrayList<>();
        for (int i = 0; i < N_HILOS; i++) {
            hilos.add(new Thread(() -> {
                for (int j = 0; j < INCS; j++) c.registrarSinSinc("BASICA");
            }));
        }
        for (Thread t : hilos) t.start();
        for (Thread t : hilos) t.join();

        int esperado = N_HILOS * INCS;
        // No es falla si exactamente esta corrida no perdio nada;
        // verificamos solo que el contador <= esperado (nunca mas).
        check(c.getTotal() <= esperado,
              "Contador SIN sync: total <= esperado (no incrementa de mas)",
              c.getTotal() + " vs " + esperado);

        if (c.getTotal() < esperado) {
            System.out.printf("       (esta corrida perdio %d incrementos por race)%n",
                    esperado - c.getTotal());
        }
    }

    // ---------- BufferAccesos ----------

    /**
     * Invariante clave: lo que se deposita = lo que se toma. Ningun
     * acceso desaparece ni se duplica.
     */
    private static void pruebaBufferProductorConsumidorSinPerdidas() throws InterruptedException {
        int N_PRODUCTORES = 4;
        int N_CONSUMIDORES = 3;
        int POR_PRODUCTOR = 50;
        BufferAccesos buffer = new BufferAccesos(10);

        AtomicInteger totalConsumido = new AtomicInteger(0);

        // Productores
        List<Thread> productores = new ArrayList<>();
        for (int p = 0; p < N_PRODUCTORES; p++) {
            final int idP = p;
            productores.add(new Thread(() -> {
                try {
                    for (int i = 0; i < POR_PRODUCTOR; i++) {
                        buffer.depositar(new AccesoSolicitado(
                                idP * 1000 + i, "p" + idP));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "prod-" + p));
        }

        // Consumidores - los marcamos como daemon para que terminen
        // automaticamente al finalizar main
        List<Thread> consumidores = new ArrayList<>();
        for (int c = 0; c < N_CONSUMIDORES; c++) {
            Thread t = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        buffer.tomar();
                        totalConsumido.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }, "cons-" + c);
            t.setDaemon(true);
            consumidores.add(t);
            t.start();
        }

        for (Thread p : productores) p.start();
        for (Thread p : productores) p.join();

        // Esperar a que los consumidores drenen el buffer
        int totalEsperado = N_PRODUCTORES * POR_PRODUCTOR;
        long limiteEspera = System.currentTimeMillis() + 5000;
        while (totalConsumido.get() < totalEsperado
                && System.currentTimeMillis() < limiteEspera) {
            Thread.sleep(50);
        }

        // Detener consumidores
        for (Thread c : consumidores) c.interrupt();

        check(totalConsumido.get() == totalEsperado,
              "Buffer productor-consumidor: " + totalEsperado + " mensajes sin perdidas",
              totalConsumido.get() + " vs " + totalEsperado);
        check(buffer.getTotalProducidos() == buffer.getTotalConsumidos(),
              "Buffer: total producido == total consumido",
              buffer.getTotalProducidos() + " vs " + buffer.getTotalConsumidos());
    }

    /**
     * El buffer nunca debe superar su capacidad maxima.
     */
    private static void pruebaBufferRespetaCapacidadMaxima() throws InterruptedException {
        int CAP = 3;
        BufferAccesos buffer = new BufferAccesos(CAP);
        AtomicInteger maxObservado = new AtomicInteger(0);
        AtomicInteger violacion = new AtomicInteger(0);

        // Hilo observador: mide constantemente el tamanio
        Thread observador = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                int actual = buffer.tamanio();
                if (actual > CAP) violacion.incrementAndGet();
                if (actual > maxObservado.get()) maxObservado.set(actual);
                try { Thread.sleep(1); }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
        observador.setDaemon(true);
        observador.start();

        // Lanzar productores agresivos y un solo consumidor lento
        List<Thread> productores = new ArrayList<>();
        for (int p = 0; p < 5; p++) {
            final int idP = p;
            Thread t = new Thread(() -> {
                try {
                    for (int i = 0; i < 20; i++) {
                        buffer.depositar(new AccesoSolicitado(i, "p" + idP));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            productores.add(t);
            t.start();
        }

        Thread consumidor = new Thread(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    buffer.tomar();
                    Thread.sleep(5);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        consumidor.start();

        for (Thread p : productores) p.join();
        consumidor.join();
        observador.interrupt();

        check(violacion.get() == 0,
              "Buffer nunca supero la capacidad maxima (cap=" + CAP + ")",
              violacion.get() + " violaciones");
    }

    // ---------- Torniquete ----------

    /**
     * Un torniquete debe detenerse limpio cuando se llama detener(),
     * incluso si esta esperando en el buffer.
     */
    private static void pruebaTorniqueteSeDetieneOrdenadamente() throws InterruptedException {
        BufferAccesos buffer = new BufferAccesos(5);
        ContadorAccesos contador = new ContadorAccesos();
        Torniquete t = new Torniquete(99, buffer, contador, null);
        t.start();
        Thread.sleep(200);  // entra al wait() del buffer vacio
        t.detener();
        t.join(2000);
        check(!t.isAlive(), "Torniquete se detiene en <2s aun estando bloqueado en wait()", null);
    }

    /**
     * Con varios torniquetes y muchos clientes, todos los accesos
     * deben procesarse exactamente una vez.
     */
    private static void pruebaMultiplesTorniquetesProcesanTodosLosAccesos() throws Exception {
        int N_CLIENTES = 30;
        int N_TORNIQUETES = 3;
        BufferAccesos buffer = new BufferAccesos(5);
        ContadorAccesos contador = new ContadorAccesos();

        List<Torniquete> torniquetes = new ArrayList<>();
        for (int i = 1; i <= N_TORNIQUETES; i++) {
            Torniquete t = new Torniquete(i, buffer, contador, null);
            torniquetes.add(t);
            t.start();
        }

        // Productores via executor
        ExecutorService pool = Executors.newFixedThreadPool(4);
        for (int i = 0; i < N_CLIENTES; i++) {
            pool.submit(new ClienteEnFila(i + 1, "Test", buffer));
        }
        pool.shutdown();
        pool.awaitTermination(15, TimeUnit.SECONDS);

        // Esperar que el buffer se drene
        long limite = System.currentTimeMillis() + 10_000;
        while (buffer.tamanio() > 0 && System.currentTimeMillis() < limite) {
            Thread.sleep(50);
        }
        Thread.sleep(500);  // pequeño margen para el ultimo procesamiento

        for (Torniquete t : torniquetes) t.detener();
        for (Torniquete t : torniquetes) t.join(2000);

        check(contador.getTotal() == N_CLIENTES,
              "Multiples torniquetes procesan TODOS los accesos (" + N_CLIENTES + ")",
              contador.getTotal());

        int sumaTorniquetes = torniquetes.stream()
                .mapToInt(Torniquete::getProcesados).sum();
        check(sumaTorniquetes == N_CLIENTES,
              "Suma de procesados por cada torniquete = total clientes",
              sumaTorniquetes + " vs " + N_CLIENTES);
    }

    // ---------- ExecutorService ----------

    private static void pruebaExecutorServiceLanzaRunnables() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(3);
        AtomicInteger ejecutados = new AtomicInteger(0);

        for (int i = 0; i < 50; i++) {
            pool.submit(() -> ejecutados.incrementAndGet());
        }
        pool.shutdown();
        boolean ok = pool.awaitTermination(5, TimeUnit.SECONDS);

        check(ok && ejecutados.get() == 50,
              "ExecutorService ejecuta 50 Runnables en pool de 3 hilos",
              ejecutados.get());
    }

    // ---------- helpers ----------

    private static void check(boolean cond, String d, Object detalle) {
        if (cond) pasar(d);
        else      fallar(d, String.valueOf(detalle));
    }

    private static void pasar(String d) {
        pasadas++;
        System.out.println("  [OK] " + d);
    }

    private static void fallar(String d, String detalle) {
        falladas++;
        System.out.println("  [FAIL] " + d + ": " + detalle);
    }
}
