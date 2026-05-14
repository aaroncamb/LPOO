import java.util.ArrayList;
import java.util.List;

/**
 * Práctica 10 — Demostracion empirica de race condition.
 *
 * Lanza N hilos que incrementan el mismo contador M veces cada uno.
 * Esperado: total = N * M.
 *
 * Sin sincronizar, el resultado SUELE ser menor que N * M porque dos
 * hilos pueden leer el mismo valor y escribir el mismo resultado,
 * perdiendo incrementos.
 *
 * Con sincronizar, el resultado SIEMPRE es N * M.
 *
 * La diferencia es la prueba mas directa de por que importa
 * synchronized. La uso en el SINCRONIZACION.md.
 */
public class DemoRaceCondition {

    private static final int HILOS_POR_PRUEBA = 10;
    private static final int INCREMENTOS_POR_HILO = 10_000;
    private static final int ESPERADO = HILOS_POR_PRUEBA * INCREMENTOS_POR_HILO;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Demo de race condition ===");
        System.out.printf("%d hilos x %d incrementos = %d esperado%n%n",
                HILOS_POR_PRUEBA, INCREMENTOS_POR_HILO, ESPERADO);

        // ---- Caso 1: SIN sincronizar (race condition garantizada) ----
        ContadorAccesos contador1 = new ContadorAccesos();
        ejecutarPrueba("SIN synchronized", contador1, false);
        int totalSinSinc = contador1.getTotal();
        int perdidos = ESPERADO - totalSinSinc;

        System.out.println();

        // ---- Caso 2: CON sincronizar ----
        ContadorAccesos contador2 = new ContadorAccesos();
        ejecutarPrueba("CON synchronized", contador2, true);
        int totalConSinc = contador2.getTotal();

        // ---- Resumen ----
        System.out.println("\n=== Resumen ===");
        System.out.printf("  Esperado:           %d%n", ESPERADO);
        System.out.printf("  Sin synchronized:   %d (%s%d incrementos perdidos)%n",
                totalSinSinc,
                perdidos > 0 ? "" : "+",
                perdidos);
        System.out.printf("  Con synchronized:   %d (%s)%n",
                totalConSinc,
                totalConSinc == ESPERADO ? "correcto" : "INCORRECTO");

        System.out.println();
        if (perdidos > 0) {
            System.out.println("RACE CONDITION DETECTADA: el contador sin synchronized perdio");
            System.out.println(perdidos + " incrementos. Esto NO sucede con synchronized.");
        } else {
            System.out.println("Esta corrida no exhibio race visible (puede pasar:");
            System.out.println("la race depende del scheduling). Vuelve a correr.");
        }
    }

    private static void ejecutarPrueba(String etiqueta, ContadorAccesos contador,
                                       boolean conSincronizacion)
            throws InterruptedException {
        System.out.println("--- " + etiqueta + " ---");

        List<Thread> hilos = new ArrayList<>();
        for (int i = 0; i < HILOS_POR_PRUEBA; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < INCREMENTOS_POR_HILO; j++) {
                    if (conSincronizacion) {
                        contador.registrar("BASICA");
                    } else {
                        contador.registrarSinSinc("BASICA");
                    }
                }
            }, "race-" + i);
            hilos.add(t);
            t.start();
        }

        // Esperar a que todos terminen.
        for (Thread t : hilos) {
            t.join();
        }

        System.out.printf("  Total contado: %d%n", contador.getTotal());
    }
}
