import java.time.LocalDate;
import java.util.List;

/**
 * Práctica 4 — Programa principal.
 *
 * Demuestra:
 *   - Herencia de 3 niveles (Membresia > Estandar > {Basica, Premium})
 *     + rama directa (Membresia > VIP).
 *   - Polimorfismo: tratar a las membresias como Membresia y dejar que
 *     cada subclase decida que hacer.
 *   - Uso de super() en constructores.
 *   - Operaciones especificas de cada subclase (clases grupales, spa).
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("=== P4: Herencia y polimorfismo ===\n");

        // --- 1. Crear instancias de cada nivel concreto ---
        LocalDate hoy = LocalDate.now();

        Membresia b1 = new MembresiaBasica("Ana Perez", hoy.minusDays(10));
        Membresia b2 = new MembresiaBasica("Luis Castillo", hoy.minusDays(40));  // vencida
        Membresia p1 = new MembresiaPremium("Bruno Hernandez", hoy.minusDays(5));
        Membresia p2 = new MembresiaPremium("Carolina Mendez", hoy.minusDays(20));
        Membresia v1 = new MembresiaVIP("David Ortega", hoy.minusDays(100));
        Membresia v2 = new MembresiaVIP("Elena Salinas", hoy.minusDays(380));    // vencida

        // --- 2. Polimorfismo en colecciones ---
        GestorMembresias gestor = new GestorMembresias();
        gestor.agregar(b1);
        gestor.agregar(b2);
        gestor.agregar(p1);
        gestor.agregar(p2);
        gestor.agregar(v1);
        gestor.agregar(v2);

        gestor.imprimirReporte();

        // --- 3. Resultado polimorfico: cada calcularPrecio() es distinto ---
        System.out.println("\n--- Ingresos esperados (solo vigentes) ---");
        System.out.printf("Total: $%.2f%n", gestor.ingresosEsperados());
        System.out.println("Notar que la suma mezcla cuotas mensuales y anuales.");

        // --- 4. Demostracion: misma linea, comportamiento distinto ---
        System.out.println("\n--- 'm.renovar()' actua distinto segun subclase ---");
        for (Membresia m : gestor.todas()) {
            LocalDate antes = m.getFechaFin();
            m.renovar();
            long diasSumados = java.time.temporal.ChronoUnit.DAYS.between(antes, m.getFechaFin());
            // diasSumados puede ser negativo si "antes" era pasado y ahora se reactivo
            System.out.printf("  %s -> renovo, fechaFin paso de %s a %s%n",
                    m.tipoLegible(), antes, m.getFechaFin());
        }

        // --- 5. Filtrado por tipo concreto ---
        System.out.println("\n--- Solo VIPs ---");
        List<Membresia> vips = gestor.filtrarPorTipo(MembresiaVIP.class);
        for (Membresia m : vips) {
            System.out.println("  " + m);
        }

        // --- 6. Funcionalidad propia de cada subclase ---
        // Para usar metodos especificos hay que castear (o usar pattern matching).
        System.out.println("\n--- Operaciones especificas por tipo ---");

        if (p1 instanceof MembresiaPremium prem) {
            prem.agendarClaseGrupal();
            prem.agendarClaseGrupal();
            System.out.println("  " + prem.getTitularNombre() + " agendo "
                    + prem.getClasesGrupalesAgendadasEsteMes() + " clases este mes.");
        }

        if (v1 instanceof MembresiaVIP vip) {
            vip.registrarAccesoSpa();
            vip.registrarAccesoSpa();
            vip.registrarAccesoSpa();
            System.out.println("  " + vip.getTitularNombre() + " uso el spa "
                    + vip.getAccesosSpaConsumidosEsteAnio() + " veces este año.");
        }

        // --- 7. Demostracion de descuentos ---
        System.out.println("\n--- Descuento por renovacion segun nivel ---");
        for (Membresia m : new Membresia[]{
                new MembresiaBasica("X", hoy),
                new MembresiaPremium("Y", hoy),
                new MembresiaVIP("Z", hoy)}) {
            System.out.printf("  %-20s descuento: %.0f%%%n",
                    m.tipoLegible(), m.descuentoRenovacion() * 100);
        }

        System.out.println("\nFin de la demostracion.");
    }
}
