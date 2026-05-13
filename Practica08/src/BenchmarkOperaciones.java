import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Práctica 8 — Análisis de tiempo de ejecucion (Entregable 7).
 *
 * Mide empiricamente que las elecciones de estructura son correctas.
 * Compara:
 *   - Busqueda lineal (recorrer ArrayList) vs HashMap.get().
 *   - Sort por orden natural vs por Comparator.
 *   - Filtrado con Stream vs bucle for tradicional.
 *
 * Usa 50,000 clientes sinteticos para que las diferencias sean visibles
 * con nanoTime(). Imprime una tabla legible con conclusiones.
 *
 * Importante: estos numeros DEPENDEN del hardware donde corre. Lo que
 * importa es la PROPORCION entre operaciones, no el valor absoluto.
 */
public class BenchmarkOperaciones {

    private static final int N = 50_000;

    public static void main(String[] args) {
        System.out.println("=== Analisis de tiempo de operaciones ===");
        System.out.println("N = " + N + " clientes sinteticos\n");

        // Generar datos
        List<Cliente> lista = new ArrayList<>(N);
        Map<Integer, Cliente> mapa = new HashMap<>(N);
        generar(lista, mapa);

        // ---- Busqueda por id: lineal vs HashMap ----
        int idObjetivo = N / 2;   // a la mitad de la lista
        long tLineal = medir(() -> buscarLineal(lista, idObjetivo));
        long tHash   = medir(() -> mapa.get(idObjetivo));
        imprimirComparacion("Busqueda por id (id existente)",
                "ArrayList lineal", tLineal,
                "HashMap.get()",    tHash);

        // ---- Busqueda al final de la lista (peor caso para lineal) ----
        int idFinal = N - 1;
        long tLinealFinal = medir(() -> buscarLineal(lista, idFinal));
        long tHashFinal   = medir(() -> mapa.get(idFinal));
        imprimirComparacion("Busqueda por id (peor caso, al final)",
                "ArrayList lineal", tLinealFinal,
                "HashMap.get()",    tHashFinal);

        // ---- Ordenamiento ----
        long tSortNombre = medir(() -> {
            List<Cliente> c = new ArrayList<>(lista);
            c.sort(Comparator.naturalOrder());
        });
        long tSortFecha = medir(() -> {
            List<Cliente> c = new ArrayList<>(lista);
            c.sort(Comparator.comparing(Cliente::getFechaRegistro));
        });
        imprimirComparacion("Sort de la lista completa",
                "Por nombre (Comparable)",     tSortNombre,
                "Por fecha (Comparator)",      tSortFecha);

        // ---- Filtrado: stream vs for tradicional ----
        long tStream = medir(() -> lista.stream()
                .filter(c -> c.getTipoMembresia() == Cliente.TipoMembresia.PREMIUM)
                .count());
        long tFor = medir(() -> {
            int n = 0;
            for (Cliente c : lista) {
                if (c.getTipoMembresia() == Cliente.TipoMembresia.PREMIUM) n++;
            }
            // n calculado, no se retorna porque medir() recibe Runnable
        });
        imprimirComparacion("Filtrado y conteo (clientes Premium)",
                "Stream", tStream,
                "for-each", tFor);

        // ---- Conclusiones ----
        System.out.println();
        System.out.println("=== Conclusiones ===");
        System.out.println(" - La busqueda por HashMap.get() es ORDENES DE MAGNITUD");
        System.out.println("   mas rapida que el barrido lineal. Justifica tener el");
        System.out.println("   indice HashMap<Integer,Cliente> ademas de la ArrayList.");
        System.out.println();
        System.out.println(" - Ordenar por orden natural (Comparable, compareToIgnoreCase)");
        System.out.println("   suele ser mas rapido que por Comparator basado en LocalDate");
        System.out.println("   porque compareTo de String esta muy optimizado, mientras que");
        System.out.println("   LocalDate.compareTo descompone año/mes/dia. Ambos son O(n log n)");
        System.out.println("   asintoticamente; la diferencia es constante por elemento.");
        System.out.println();
        System.out.println(" - Stream vs for-each estan en el mismo orden de magnitud.");
        System.out.println("   Stream es ligeramente mas lento por la abstraccion, pero");
        System.out.println("   gana en legibilidad y compone bien con sort/limit/etc.");
        System.out.println();
        System.out.println("Nota: los tiempos absolutos dependen del hardware. Lo que");
        System.out.println("se compara es la PROPORCION entre operaciones.");
    }

    // ---------------------------------------------------------------

    private static void generar(List<Cliente> lista, Map<Integer, Cliente> mapa) {
        Random rng = new Random(42);
        Cliente.TipoMembresia[] tipos = Cliente.TipoMembresia.values();
        LocalDate base = LocalDate.of(2020, 1, 1);

        for (int i = 1; i <= N; i++) {
            Cliente c = new Cliente(
                    i,
                    "Cliente " + i,
                    "c" + i + "@correo.mx",
                    base.plusDays(rng.nextInt(2000)),
                    30 + rng.nextDouble() * 70,
                    tipos[rng.nextInt(tipos.length)]);
            lista.add(c);
            mapa.put(c.getId(), c);
        }
    }

    private static Cliente buscarLineal(List<Cliente> lista, int id) {
        for (Cliente c : lista) {
            if (c.getId() == id) return c;
        }
        return null;
    }

    /**
     * Mide el tiempo en nanos de ejecutar una operacion. Repite 50 veces
     * y devuelve la mediana aproximada (la media descartando extremos)
     * para que mediciones puntuales no se vean afectadas por GC ni JIT.
     */
    private static long medir(Runnable accion) {
        // Calentamiento (JIT)
        for (int i = 0; i < 10; i++) accion.run();

        long[] tiempos = new long[50];
        for (int i = 0; i < 50; i++) {
            long ini = System.nanoTime();
            accion.run();
            tiempos[i] = System.nanoTime() - ini;
        }
        java.util.Arrays.sort(tiempos);
        // mediana
        return tiempos[tiempos.length / 2];
    }

    private static void imprimirComparacion(String titulo,
                                            String a, long tA,
                                            String b, long tB) {
        System.out.println("--- " + titulo + " ---");
        System.out.printf("  %-30s %12d ns%n", a, tA);
        System.out.printf("  %-30s %12d ns%n", b, tB);
        if (tA > 0 && tB > 0) {
            double ratio = (double) Math.max(tA, tB) / Math.min(tA, tB);
            String ganador = (tA < tB) ? a : b;
            System.out.printf("  → '%s' es %.1fx mas rapida%n", ganador, ratio);
        }
        System.out.println();
    }
}
