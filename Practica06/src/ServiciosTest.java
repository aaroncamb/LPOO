import java.time.LocalDate;
import java.util.List;

/**
 * Práctica 6 — Pruebas unitarias manuales.
 *
 * Verifica:
 *   - Cada clase concreta implementa LAS interfaces correctas y SOLO esas.
 *   - Los metodos default funcionan correctamente.
 *   - El CentroOperaciones opera correctamente por interfaz.
 *   - Polimorfismo: lo mismo dicho con interfaces que con clases.
 */
public class ServiciosTest {

    private static int pasadas = 0;
    private static int falladas = 0;

    public static void main(String[] args) {
        System.out.println("=== Pruebas - P6 ===\n");

        // Verificacion de combinaciones de interfaces
        pruebaClaseGrupalImplementaNotificableYReportable();
        pruebaClaseGrupalNO_ImplementaReagendable();
        pruebaEntrenamientoImplementaNotificableYReagendable();
        pruebaEntrenamientoNO_ImplementaReportable();
        pruebaEvaluacionImplementaLasTres();

        // Metodos default
        pruebaDefaultNotificarMultiplesCanales();
        pruebaDefaultFechaRespetaAnticipacion();
        pruebaCSVHeaderEsEstatico();
        pruebaDefaultToCsvLine();

        // Comportamiento de Reagendable
        pruebaReagendarRespetaAnticipacion();
        pruebaReagendarRechazaFechaCercana();

        // Centro de operaciones
        pruebaCentroNotificaSoloANotificables();
        pruebaCentroReporteCsvSoloIncluyeReportables();
        pruebaCentroReagendarSoloAfectaAReagendables();
        pruebaCentroFiltrarPorInterfaz();
        pruebaIngresoTotalSoloDeReportables();

        // Servicio abstracto
        pruebaServicioTieneMetodoConcretoResumen();
        pruebaServicioAtributoProtectedNotas();

        // Resumen
        System.out.println("\n=== Resumen ===");
        System.out.println("Pasadas:  " + pasadas);
        System.out.println("Falladas: " + falladas);
        System.out.println("Total:    " + (pasadas + falladas));

        if (falladas > 0) System.exit(1);
    }

    // ---------- Combinaciones de interfaces ----------

    private static void pruebaClaseGrupalImplementaNotificableYReportable() {
        ClaseGrupal c = new ClaseGrupal("Yoga", "G", LocalDate.now().plusDays(1), 200, 10);
        check(c instanceof Notificable, "ClaseGrupal es Notificable", null);
        check(c instanceof Reportable,  "ClaseGrupal es Reportable",  null);
    }

    private static void pruebaClaseGrupalNO_ImplementaReagendable() {
        ClaseGrupal c = new ClaseGrupal("Yoga", "G", LocalDate.now().plusDays(1), 200, 10);
        check(!(c instanceof Reagendable),
              "ClaseGrupal NO es Reagendable (horario publico)", null);
    }

    private static void pruebaEntrenamientoImplementaNotificableYReagendable() {
        EntrenamientoPersonal e = new EntrenamientoPersonal("S", "X",
                LocalDate.now().plusDays(2), 500, "C", 60);
        check(e instanceof Notificable, "Entrenamiento es Notificable", null);
        check(e instanceof Reagendable, "Entrenamiento es Reagendable", null);
    }

    private static void pruebaEntrenamientoNO_ImplementaReportable() {
        EntrenamientoPersonal e = new EntrenamientoPersonal("S", "X",
                LocalDate.now().plusDays(2), 500, "C", 60);
        check(!(e instanceof Reportable),
              "Entrenamiento NO es Reportable (servicio premium privado)", null);
    }

    private static void pruebaEvaluacionImplementaLasTres() {
        EvaluacionFisica ef = new EvaluacionFisica("E", "X",
                LocalDate.now().plusDays(5), 350, 30);
        check(ef instanceof Notificable, "Evaluacion es Notificable", null);
        check(ef instanceof Reportable,  "Evaluacion es Reportable",  null);
        check(ef instanceof Reagendable, "Evaluacion es Reagendable", null);
    }

    // ---------- Metodos default ----------

    private static void pruebaDefaultNotificarMultiplesCanales() {
        // Capturamos stdout no es trivial; aqui solo validamos el retorno.
        ClaseGrupal c = new ClaseGrupal("Yoga", "G", LocalDate.now().plusDays(1), 200, 10);
        boolean r = c.notificarMultiplesCanales("test", "hola");
        check(r, "notificarMultiplesCanales (default) coordina email+SMS", null);
    }

    private static void pruebaDefaultFechaRespetaAnticipacion() {
        EntrenamientoPersonal e = new EntrenamientoPersonal("S", "X",
                LocalDate.now().plusDays(2), 500, "C", 60);
        check(e.fechaRespetaAnticipacion(LocalDate.now().plusDays(5)),
              "fechaRespetaAnticipacion (default) acepta fecha lejana", null);
        check(!e.fechaRespetaAnticipacion(LocalDate.now()),
              "fechaRespetaAnticipacion (default) rechaza hoy mismo", null);
    }

    private static void pruebaCSVHeaderEsEstatico() {
        check(Reportable.csvHeader().equals("fecha,titulo,monto,categoria"),
              "Reportable.csvHeader devuelve el header esperado", null);
    }

    private static void pruebaDefaultToCsvLine() {
        EvaluacionFisica ef = new EvaluacionFisica("Eval", "Ana", LocalDate.of(2026, 5, 1),
                300, 30);
        String linea = ef.toCsvLine();
        check(linea.startsWith("2026-05-01") && linea.contains("evaluacion"),
              "toCsvLine (default) construye linea CSV con campos del Reportable",
              linea);
    }

    // ---------- Reagendable ----------

    private static void pruebaReagendarRespetaAnticipacion() {
        EntrenamientoPersonal e = new EntrenamientoPersonal("S", "X",
                LocalDate.now().plusDays(2), 500, "C", 60);
        LocalDate nueva = LocalDate.now().plusDays(7);
        boolean ok = e.reagendar(nueva);
        check(ok && e.getFechaServicio().equals(nueva),
              "reagendar mueve la fecha cuando respeta anticipacion", null);
    }

    private static void pruebaReagendarRechazaFechaCercana() {
        EvaluacionFisica ef = new EvaluacionFisica("E", "X",
                LocalDate.now().plusDays(5), 350, 30);
        // Eval pide 3 dias min; intentamos con 1
        boolean ok = ef.reagendar(LocalDate.now().plusDays(1));
        check(!ok, "reagendar rechaza fecha que no respeta anticipacion", null);
    }

    // ---------- CentroOperaciones ----------

    private static void pruebaCentroNotificaSoloANotificables() {
        CentroOperaciones c = new CentroOperaciones();
        c.agregar(new ClaseGrupal("Y", "G", LocalDate.now().plusDays(1), 200, 10));
        c.agregar(new EvaluacionFisica("E", "X", LocalDate.now().plusDays(5), 300, 30));
        int n = c.notificarTodos("hola", "mensaje");
        check(n == 2, "Centro notifica solo a los Notificables (los 2)", n);
    }

    private static void pruebaCentroReporteCsvSoloIncluyeReportables() {
        CentroOperaciones c = new CentroOperaciones();
        c.agregar(new ClaseGrupal("Y", "G", LocalDate.now().plusDays(1), 200, 10));
        c.agregar(new EntrenamientoPersonal("S", "X",
                LocalDate.now().plusDays(2), 500, "C", 60));   // NO reportable
        c.agregar(new EvaluacionFisica("E", "Z", LocalDate.now().plusDays(5), 350, 30));
        String csv = c.generarReporteCSV();
        // header + 2 lineas (no incluye el entrenamiento)
        long lineas = csv.lines().count();
        check(lineas == 3, "Reporte CSV incluye solo Reportables (header + 2)", lineas);
    }

    private static void pruebaCentroReagendarSoloAfectaAReagendables() {
        CentroOperaciones c = new CentroOperaciones();
        c.agregar(new ClaseGrupal("Y", "G", LocalDate.now().plusDays(1), 200, 10));   // no
        c.agregar(new EntrenamientoPersonal("S", "X",
                LocalDate.now().plusDays(2), 500, "C", 60));                          // si
        c.agregar(new EvaluacionFisica("E", "Z", LocalDate.now().plusDays(5), 350, 30)); // si
        int n = c.reagendarTodosNDias(10);
        check(n == 2, "Reagendar afecta solo a los Reagendables (2 de 3)", n);
    }

    private static void pruebaCentroFiltrarPorInterfaz() {
        CentroOperaciones c = new CentroOperaciones();
        c.agregar(new ClaseGrupal("Y", "G", LocalDate.now().plusDays(1), 200, 10));
        c.agregar(new EntrenamientoPersonal("S", "X",
                LocalDate.now().plusDays(2), 500, "C", 60));
        c.agregar(new EvaluacionFisica("E", "Z", LocalDate.now().plusDays(5), 350, 30));

        List<Servicio> reps = c.filtrarPorInterfaz(Reportable.class);
        List<Servicio> reas = c.filtrarPorInterfaz(Reagendable.class);
        List<Servicio> nots = c.filtrarPorInterfaz(Notificable.class);

        check(reps.size() == 2 && reas.size() == 2 && nots.size() == 3,
              "Filtrado por interfaz: Reportables=2, Reagendables=2, Notificables=3",
              String.format("R=%d A=%d N=%d", reps.size(), reas.size(), nots.size()));
    }

    private static void pruebaIngresoTotalSoloDeReportables() {
        CentroOperaciones c = new CentroOperaciones();
        c.agregar(new ClaseGrupal("Y", "G", LocalDate.now().plusDays(1), 100, 10)); // 116
        c.agregar(new EntrenamientoPersonal("S", "X",
                LocalDate.now().plusDays(2), 500, "C", 60));                          // no cuenta
        c.agregar(new EvaluacionFisica("E", "Z", LocalDate.now().plusDays(5), 300, 30)); // 300 sin IVA
        double esperado = 116 + 300;
        double real = c.ingresoTotalReportable();
        check(eq(real, esperado),
              "ingresoTotalReportable suma solo a los Reportables",
              "esperado " + esperado + " real " + real);
    }

    // ---------- Servicio abstracto ----------

    private static void pruebaServicioTieneMetodoConcretoResumen() {
        Servicio s = new ClaseGrupal("Y", "G", LocalDate.now().plusDays(1), 200, 10);
        String r = s.resumen();
        check(r != null && r.contains("Clase Grupal"),
              "Servicio.resumen() es concreto y heredado por las hijas", null);
    }

    private static void pruebaServicioAtributoProtectedNotas() {
        Servicio s = new ClaseGrupal("Y", "G", LocalDate.now().plusDays(1), 200, 10);
        s.agregarNota("primera");
        s.agregarNota("segunda");
        check(s.getNotas().equals("primera | segunda"),
              "Atributo protected 'notas' se gestiona por metodos del padre", null);
    }

    // ---------- utilidades ----------

    private static boolean eq(double a, double b) { return Math.abs(a - b) < 0.01; }

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
