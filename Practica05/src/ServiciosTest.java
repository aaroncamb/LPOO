import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Práctica 5 — Pruebas unitarias manuales.
 *
 * Cobertura por categoria:
 *   - Calculos de Cobrable en cada clase concreta.
 *   - Sobrecargas: cada firma de aplicarDescuento devuelve lo esperado.
 *   - Template Method procesarVenta: orden, validaciones, abortos.
 *   - Polimorfismo via CajaRegistradora.
 */
public class ServiciosTest {

    private static int pasadas = 0;
    private static int falladas = 0;

    public static void main(String[] args) {
        System.out.println("=== Pruebas - P5 Polimorfismo ===\n");

        // Calculos basicos
        pruebaSubtotalClaseGrupal();
        pruebaImpuestosClaseGrupal();
        pruebaTotalClaseGrupal();
        pruebaEntrenamientoConRecargoPorDuracion();
        pruebaEvaluacionExentaDeIVA();

        // Sobrecargas
        pruebaSobrecargaDoubleEnClaseGrupal();
        pruebaSobrecargaIntEnClaseGrupal();
        pruebaSobrecargaStringEnClaseGrupal();
        pruebaSobrecargaCuponInvalidoLanzaExcepcion();
        pruebaSobrecargaPaqueteEnEntrenamiento();
        pruebaSobrecargaHorarioValleEnEntrenamiento();
        pruebaSobrecargaEdadEnEvaluacion();

        // Template Method
        pruebaProcesarVentaExitoso();
        pruebaProcesarVentaAbortaPorFechaPasada();
        pruebaProcesarVentaAbortaPorEdadInsuficiente();

        // Polimorfismo en caja
        pruebaCajaSumaPolimorficamente();
        pruebaCajaCuentaPorTipo();
        pruebaCajaDescuentoGlobal();

        // Resumen
        System.out.println("\n=== Resumen ===");
        System.out.println("Pasadas:  " + pasadas);
        System.out.println("Falladas: " + falladas);
        System.out.println("Total:    " + (pasadas + falladas));

        if (falladas > 0) System.exit(1);
    }

    // ---------- calculos basicos ----------

    private static void pruebaSubtotalClaseGrupal() {
        ClaseGrupal c = new ClaseGrupal("Yoga", "X", LocalDate.now().plusDays(1), 200, 10);
        check(c.calcularSubtotal() == 200, "ClaseGrupal subtotal = precio base", c.calcularSubtotal());
    }

    private static void pruebaImpuestosClaseGrupal() {
        ClaseGrupal c = new ClaseGrupal("Yoga", "X", LocalDate.now().plusDays(1), 200, 10);
        check(eq(c.calcularImpuestos(), 32.0), "ClaseGrupal IVA 16%", c.calcularImpuestos());
    }

    private static void pruebaTotalClaseGrupal() {
        ClaseGrupal c = new ClaseGrupal("Yoga", "X", LocalDate.now().plusDays(1), 200, 10);
        check(eq(c.calcularTotal(), 232.0), "ClaseGrupal total = 200 + 32 IVA", c.calcularTotal());
    }

    private static void pruebaEntrenamientoConRecargoPorDuracion() {
        // 90 min > 60 min => recargo 1.20
        EntrenamientoPersonal e = new EntrenamientoPersonal("S", "X",
                LocalDate.now().plusDays(1), 500, "Coach", 90, LocalTime.of(8, 0));
        // subtotal = 500 * 1.20 = 600
        check(eq(e.calcularSubtotal(), 600.0), "EntrenamientoPersonal recargo sesion larga", e.calcularSubtotal());
    }

    private static void pruebaEvaluacionExentaDeIVA() {
        EvaluacionFisica ef = new EvaluacionFisica("Eval", "X",
                LocalDate.now().plusDays(1), 350, 25, false);
        check(ef.calcularImpuestos() == 0.0, "Evaluacion exenta de IVA", ef.calcularImpuestos());
        check(eq(ef.calcularTotal(), 350.0), "Evaluacion total = subtotal sin IVA", ef.calcularTotal());
    }

    // ---------- sobrecargas ----------

    private static void pruebaSobrecargaDoubleEnClaseGrupal() {
        ClaseGrupal c = new ClaseGrupal("Yoga", "X", LocalDate.now().plusDays(1), 200, 10);
        double r = c.aplicarDescuento(0.10);   // 10%
        check(eq(r, 180.0), "Sobrecarga double 10% en ClaseGrupal", r);
    }

    private static void pruebaSobrecargaIntEnClaseGrupal() {
        ClaseGrupal c = new ClaseGrupal("Yoga", "X", LocalDate.now().plusDays(1), 200, 10);
        double r = c.aplicarDescuento(30);   // $30 fijos
        check(eq(r, 170.0), "Sobrecarga int $30 fijos en ClaseGrupal", r);
    }

    private static void pruebaSobrecargaStringEnClaseGrupal() {
        ClaseGrupal c = new ClaseGrupal("Yoga", "X", LocalDate.now().plusDays(1), 200, 10);
        double r = c.aplicarDescuento("BIENVENIDA");   // 20%
        check(eq(r, 160.0), "Sobrecarga String cupon BIENVENIDA", r);
    }

    private static void pruebaSobrecargaCuponInvalidoLanzaExcepcion() {
        ClaseGrupal c = new ClaseGrupal("Yoga", "X", LocalDate.now().plusDays(1), 200, 10);
        try {
            c.aplicarDescuento("XYZ_INEXISTENTE");
            fallar("cupon invalido", "no lanzo excepcion");
        } catch (IllegalArgumentException e) {
            pasar("cupon invalido lanza IllegalArgumentException");
        }
    }

    private static void pruebaSobrecargaPaqueteEnEntrenamiento() {
        EntrenamientoPersonal e = new EntrenamientoPersonal("S", "X",
                LocalDate.now().plusDays(1), 500, "Coach", 60, LocalTime.of(8, 0));
        // paquete de 5 => 15% sobre 500 = 75 descuento, subtotal = 425
        double r = e.aplicarDescuento(5);
        check(eq(r, 425.0), "Sobrecarga int paquete 5 sesiones", r);
    }

    private static void pruebaSobrecargaHorarioValleEnEntrenamiento() {
        EntrenamientoPersonal e = new EntrenamientoPersonal("S", "X",
                LocalDate.now().plusDays(1), 500, "Coach", 60, LocalTime.of(7, 30));
        // valle => 25% sobre 500 = 125 descuento, subtotal = 375
        double r = e.aplicarDescuento(true);
        check(eq(r, 375.0), "Sobrecarga boolean horario valle", r);
    }

    private static void pruebaSobrecargaEdadEnEvaluacion() {
        EvaluacionFisica ef = new EvaluacionFisica("Eval", "X",
                LocalDate.now().plusDays(1), 350, 65, false);
        // senior 60+ => 40% sobre 350 = 140 descuento, subtotal = 210
        double r = ef.aplicarDescuento(true);
        check(eq(r, 210.0), "Sobrecarga boolean por edad senior (40%)", r);
    }

    // ---------- Template Method ----------

    private static void pruebaProcesarVentaExitoso() {
        ClaseGrupal c = new ClaseGrupal("Yoga", "Ana", LocalDate.now().plusDays(1), 200, 10);
        ResultadoVenta r = c.procesarVenta();
        check(r.isExitoso() && eq(r.getTotalCobrado(), 232.0),
              "procesarVenta exitoso con total esperado", r);
    }

    private static void pruebaProcesarVentaAbortaPorFechaPasada() {
        ClaseGrupal c = new ClaseGrupal("Yoga", "Ana", LocalDate.now().minusDays(1), 200, 10);
        ResultadoVenta r = c.procesarVenta();
        check(!r.isExitoso(), "procesarVenta aborta por fecha pasada", r);
    }

    private static void pruebaProcesarVentaAbortaPorEdadInsuficiente() {
        EvaluacionFisica ef = new EvaluacionFisica("Eval", "X",
                LocalDate.now().plusDays(1), 350, 12, false);  // 12 anios, no valida
        ResultadoVenta r = ef.procesarVenta();
        check(!r.isExitoso(), "procesarVenta aborta por edad < 14", r);
    }

    // ---------- polimorfismo en caja ----------

    private static void pruebaCajaSumaPolimorficamente() {
        CajaRegistradora caja = new CajaRegistradora();
        caja.agregarTicket(new ClaseGrupal("A", "X", LocalDate.now().plusDays(1), 100, 5));  // 116
        caja.agregarTicket(new EvaluacionFisica("B", "Y", LocalDate.now().plusDays(1), 200, 30, false)); // 200 (sin IVA)
        double esperado = 116 + 200;
        check(eq(caja.totalEnCaja(), esperado), "Caja suma totales polimorficos", caja.totalEnCaja());
    }

    private static void pruebaCajaCuentaPorTipo() {
        CajaRegistradora caja = new CajaRegistradora();
        caja.agregarTicket(new ClaseGrupal("A", "X", LocalDate.now().plusDays(1), 100, 5));
        caja.agregarTicket(new ClaseGrupal("B", "Y", LocalDate.now().plusDays(1), 100, 5));
        caja.agregarTicket(new EvaluacionFisica("C", "Z", LocalDate.now().plusDays(1), 100, 30, false));

        check(caja.contarPorTipo(ClaseGrupal.class) == 2
              && caja.contarPorTipo(EvaluacionFisica.class) == 1,
              "Caja cuenta correctamente por tipo concreto", null);
    }

    private static void pruebaCajaDescuentoGlobal() {
        CajaRegistradora caja = new CajaRegistradora();
        ClaseGrupal c = new ClaseGrupal("A", "X", LocalDate.now().plusDays(1), 100, 5);
        caja.agregarTicket(c);
        caja.aplicarDescuentoGlobal(0.10);
        check(eq(c.calcularSubtotal(), 90.0), "Descuento global aplicado polimorficamente", c.calcularSubtotal());
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
