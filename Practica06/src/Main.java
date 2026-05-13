import java.time.LocalDate;
import java.util.List;

/**
 * Práctica 6 — Programa principal.
 *
 * Demuestra:
 *   - Clase abstracta Servicio (no instanciable, con abstractos y concretos).
 *   - Tres interfaces (Notificable, Reportable, Reagendable) implementadas
 *     en combinaciones DISTINTAS por cada subclase.
 *   - Metodos default de las interfaces en accion.
 *   - Clase gestora que opera por interfaz (polimorfismo sin conocer tipos).
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== P6: Clases abstractas e interfaces ===\n");

        // ============================================================
        // 1. Crear servicios de los tres tipos concretos
        // ============================================================
        ClaseGrupal yoga = new ClaseGrupal(
                "Yoga matutino", "Grupo A",
                LocalDate.now().plusDays(2), 200.0, 15);

        ClaseGrupal spinning = new ClaseGrupal(
                "Spinning HIIT", "Grupo B",
                LocalDate.now().plusDays(4), 250.0, 12);

        EntrenamientoPersonal sesionFuerza = new EntrenamientoPersonal(
                "Sesion de fuerza", "Bruno Hernandez",
                LocalDate.now().plusDays(3), 500.0, "Coach Lopez", 60);

        EvaluacionFisica evalAnual = new EvaluacionFisica(
                "Evaluacion anual", "Carolina Mendez",
                LocalDate.now().plusDays(7), 350.0, 32);

        // ============================================================
        // 2. NO se puede instanciar la clase abstracta directamente
        // ============================================================
        //
        // La siguiente linea NO COMPILA:
        //     Servicio s = new Servicio("X", "Y", LocalDate.now(), 0);
        //
        // El compilador dice: "Servicio is abstract; cannot be instantiated"
        // porque tiene metodos abstractos sin cuerpo. Solo las subclases
        // concretas pueden instanciarse.

        // Servicio SI puede usarse como TIPO de variable:
        Servicio servicioGenerico = yoga;   // OK, polimorfismo
        System.out.println("Servicio generico: " + servicioGenerico.resumen());

        // ============================================================
        // 3. Combinaciones de interfaces por clase concreta
        // ============================================================
        System.out.println("\n--- Combinaciones de interfaces ---");
        imprimirInterfaces(yoga);
        imprimirInterfaces(sesionFuerza);
        imprimirInterfaces(evalAnual);

        // ============================================================
        // 4. Metodos default de las interfaces en accion
        // ============================================================
        System.out.println("\n--- Metodo default Notificable.notificarMultiplesCanales ---");
        // El metodo default coordina email + SMS. La clase no implementa
        // notificarMultiplesCanales(), lo hereda DE LA INTERFAZ.
        yoga.notificarMultiplesCanales("Cambio de salon",
                "Tu clase de yoga se movio al salon B.");

        System.out.println("\n--- Metodo default Reagendable.fechaRespetaAnticipacion ---");
        // La interfaz Reagendable define el calculo de anticipacion.
        // Cada implementacion lo aprovecha sin reescribirlo.
        LocalDate fechaCercana = LocalDate.now().plusDays(1);
        LocalDate fechaAdecuada = LocalDate.now().plusDays(5);
        System.out.println("Sesion fuerza acepta fecha en 1 dia?  "
                + sesionFuerza.fechaRespetaAnticipacion(fechaCercana));
        System.out.println("Eval anual acepta fecha en 1 dia?     "
                + evalAnual.fechaRespetaAnticipacion(fechaCercana));
        System.out.println("Eval anual acepta fecha en 5 dias?    "
                + evalAnual.fechaRespetaAnticipacion(fechaAdecuada));
        System.out.println("(Sesion fuerza pide 1 dia; Eval anual pide 3)");

        // ============================================================
        // 5. Centro de operaciones: polimorfismo por interfaz
        // ============================================================
        System.out.println("\n--- CentroOperaciones agrupa todos los servicios ---");
        CentroOperaciones centro = new CentroOperaciones();
        centro.agregar(yoga);
        centro.agregar(spinning);
        centro.agregar(sesionFuerza);
        centro.agregar(evalAnual);

        centro.imprimirInventario();

        System.out.println("\n--- Notificar a todos los Notificables ---");
        int noti = centro.notificarTodos("Aviso general",
                "Manana habra revision de equipos en horario matutino.");
        System.out.println(">> Se notificaron " + noti + " servicios.");

        System.out.println("\n--- Reporte CSV de los Reportables ---");
        String csv = centro.generarReporteCSV();
        System.out.println(csv);

        System.out.println("--- Reagendar todos los Reagendables a +10 dias ---");
        int rea = centro.reagendarTodosNDias(10);
        System.out.println(">> " + rea + " servicios reagendados");
        System.out.println("(Yoga y Spinning no reagendaron porque ClaseGrupal NO es Reagendable)");

        // Imprimir el estado nuevo despues del reagendar.
        System.out.println();
        centro.imprimirInventario();

        // ============================================================
        // 6. Filtrado por interfaz
        // ============================================================
        System.out.println("\n--- Servicios filtrados por interfaz ---");
        List<Servicio> reportables = centro.filtrarPorInterfaz(Reportable.class);
        System.out.println("Reportables (" + reportables.size() + "):");
        for (Servicio s : reportables) {
            System.out.println("  - " + s.resumen());
        }

        List<Servicio> reagendables = centro.filtrarPorInterfaz(Reagendable.class);
        System.out.println("Reagendables (" + reagendables.size() + "):");
        for (Servicio s : reagendables) {
            System.out.println("  - " + s.resumen());
        }

        System.out.printf("%nIngreso total reportable: $%.2f%n",
                centro.ingresoTotalReportable());

        // ============================================================
        // 7. Polimorfismo combinado: array que se castea segun interfaz
        // ============================================================
        System.out.println("\n--- Misma coleccion, distintas operaciones segun interfaz ---");
        for (Servicio s : centro.todos()) {
            System.out.println("Procesando: " + s.tipoServicio());

            // Una misma iteracion descubre que puede hacer cada servicio
            // segun las interfaces que implemente.
            if (s instanceof Reagendable r) {
                System.out.println("  -> se puede reagendar (anticipacion: "
                        + r.diasAnticipacionMinima() + " dias)");
            }
            if (s instanceof Reportable rep) {
                System.out.println("  -> aparece en reportes como: " + rep.categoriaReporte());
            }
            if (s instanceof Notificable) {
                System.out.println("  -> se puede notificar al cliente");
            }
        }

        System.out.println("\nFin de la demostracion.");
    }

    private static void imprimirInterfaces(Servicio s) {
        StringBuilder sb = new StringBuilder(s.tipoServicio() + " implementa: ");
        boolean primero = true;
        if (s instanceof Notificable) { sb.append("Notificable"); primero = false; }
        if (s instanceof Reportable)  { if (!primero) sb.append(", "); sb.append("Reportable"); primero = false; }
        if (s instanceof Reagendable) { if (!primero) sb.append(", "); sb.append("Reagendable"); }
        System.out.println("  " + sb);
    }
}
