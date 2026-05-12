import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Práctica 5 — Programa principal.
 *
 * Demuestra:
 *   1. Polimorfismo: array de Servicio[] que mezcla los 3 tipos.
 *   2. instanceof + casting para acceder a metodos especificos.
 *   3. Sobrecarga de aplicarDescuento (3 firmas distintas).
 *   4. Template Method procesarVenta() orquestando el flujo.
 *   5. CajaRegistradora operando sobre List<Cobrable>.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== P5: Polimorfismo y Template Method ===\n");

        // ============================================================
        // 1. ARRAY POLIMORFICO: Servicio[] con instancias de 3 tipos
        // ============================================================
        Servicio[] servicios = {
            new ClaseGrupal("Yoga matutino", "Ana Perez",
                    LocalDate.now().plusDays(2), 200.0, 15),

            new EntrenamientoPersonal("Sesion fuerza", "Bruno Hdz",
                    LocalDate.now().plusDays(1), 500.0,
                    "Coach Lopez", 90, LocalTime.of(7, 30)),

            new EvaluacionFisica("Eval anual", "Carolina M.",
                    LocalDate.now().plusDays(3), 350.0, 16, true),

            new ClaseGrupal("Spinning HIIT", "David O.",
                    LocalDate.now().plusDays(5), 250.0, 12),

            new EntrenamientoPersonal("Cardio basico", "Elena S.",
                    LocalDate.now().plusDays(2), 400.0,
                    "Coach Diaz", 60, LocalTime.of(18, 0)),
        };

        // Recorrer con polimorfismo: misma linea, comportamiento distinto
        System.out.println("--- Tipos y subtotales en el array (polimorfismo) ---");
        for (Servicio s : servicios) {
            System.out.printf("  %-25s subtotal $%.2f, IVA $%.2f, total $%.2f%n",
                    s.tipoServicio(), s.calcularSubtotal(),
                    s.calcularImpuestos(), s.calcularTotal());
        }

        // ============================================================
        // 2. instanceof + CAST para acceder a metodos especificos
        // ============================================================
        System.out.println("\n--- Operaciones especificas por subtipo (instanceof + cast) ---");
        for (Servicio s : servicios) {
            if (s instanceof ClaseGrupal cg) {
                cg.inscribirAsistente();
                System.out.printf("  [ClaseGrupal] %s tiene %d/%d cupos%n",
                        cg.getNombreServicio(), cg.getInscritos(), cg.getCupoMaximo());
            } else if (s instanceof EntrenamientoPersonal ep) {
                System.out.printf("  [Entrenamiento] %s con %s (%d min)%n",
                        ep.getNombreServicio(), ep.getEntrenador(), ep.getDuracionMinutos());
            } else if (s instanceof EvaluacionFisica ef) {
                System.out.printf("  [Evaluacion] %s, cliente edad %d%n",
                        ef.getClienteNombre(), ef.getEdadCliente());
            }
        }

        // ============================================================
        // 3. SOBRECARGA: aplicarDescuento con firmas distintas
        // ============================================================
        System.out.println("\n--- Sobrecargas de aplicarDescuento ---");

        ClaseGrupal yoga = (ClaseGrupal) servicios[0];
        System.out.printf("Yoga sin descuento:           subtotal $%.2f%n", yoga.calcularSubtotal());

        yoga.aplicarDescuento(0.10);   // double - 10% (heredado de Cobrable)
        System.out.printf("Yoga con 10%% porcentual:      subtotal $%.2f%n", yoga.calcularSubtotal());

        yoga.resetearDescuento();
        yoga.aplicarDescuento(50);     // int - $50 fijos (sobrecarga propia)
        System.out.printf("Yoga con $50 fijos:           subtotal $%.2f%n", yoga.calcularSubtotal());

        yoga.resetearDescuento();
        yoga.aplicarDescuento("BIENVENIDA");  // String - cupon (sobrecarga propia)
        System.out.printf("Yoga con cupon BIENVENIDA:    subtotal $%.2f%n", yoga.calcularSubtotal());

        // Tres firmas distintas, mismo nombre. Eso es sobrecarga.

        // Sobrecargas en EntrenamientoPersonal
        EntrenamientoPersonal entren = (EntrenamientoPersonal) servicios[1];
        entren.resetearDescuento();
        System.out.printf("%nEntrenamiento sin descuento:  subtotal $%.2f (incluye recargo por >60min)%n",
                entren.calcularSubtotal());

        entren.aplicarDescuento(0.15);
        System.out.printf("Con 15%% porcentual:           subtotal $%.2f%n", entren.calcularSubtotal());

        entren.resetearDescuento();
        entren.aplicarDescuento(true);   // boolean - horario valle (7:30 esta en rango)
        System.out.printf("Con horario valle (boolean):  subtotal $%.2f%n", entren.calcularSubtotal());

        entren.resetearDescuento();
        entren.aplicarDescuento(5);      // int - paquete de 5 sesiones
        System.out.printf("Con paquete 5 sesiones (int): subtotal $%.2f%n", entren.calcularSubtotal());

        // ============================================================
        // 4. TEMPLATE METHOD: procesarVenta orquesta el flujo
        // ============================================================
        System.out.println("\n--- Template Method procesarVenta() ---");
        EvaluacionFisica eval = (EvaluacionFisica) servicios[2];
        eval.aplicarDescuento("primera");   // gratis por ser primera evaluacion
        ResultadoVenta r = eval.procesarVenta();
        System.out.println("Resultado: " + r);
        System.out.println("Mensaje:\n" + r.getMensaje());

        // ============================================================
        // 5. POLIMORFISMO EN COLECCION: CajaRegistradora
        // ============================================================
        System.out.println("\n--- CajaRegistradora con List<Cobrable> ---");
        CajaRegistradora caja = new CajaRegistradora();
        for (Servicio s : servicios) {
            s.resetearDescuento();
            caja.agregarTicket(s);   // todos van como Cobrable
        }
        caja.imprimirEstadoCaja();

        System.out.println("\nAplicando 5% de descuento global a TODA la caja:");
        caja.aplicarDescuentoGlobal(0.05);
        caja.imprimirEstadoCaja();

        System.out.println("\nClasesGrupales en caja: " + caja.contarPorTipo(ClaseGrupal.class));
        System.out.println("Entrenamientos en caja: " + caja.contarPorTipo(EntrenamientoPersonal.class));
        System.out.println("Evaluaciones en caja:   " + caja.contarPorTipo(EvaluacionFisica.class));

        System.out.println("\nCerrando caja (procesarVenta para cada servicio):");
        List<ResultadoVenta> resultados = caja.cerrarCaja();
        for (ResultadoVenta res : resultados) {
            System.out.println("  -> " + (res.isExitoso() ? "OK $" + String.format("%.2f", res.getTotalCobrado())
                                                          : "FALLO: " + res.getMensaje()));
        }
        System.out.printf("Total ingresado en esta corrida: $%.2f%n", caja.getTotalIngresado());

        // ============================================================
        // 6. CASO DE VALIDACION FALLIDA (procesarVenta atrapa el fallo)
        // ============================================================
        System.out.println("\n--- Servicio que NO pasa validacion ---");
        ClaseGrupal pasada = new ClaseGrupal("Clase antigua", "Test",
                LocalDate.now().minusDays(5), 200.0, 10);
        ResultadoVenta rFallida = pasada.procesarVenta();
        System.out.println("Resultado: " + rFallida);
        System.out.println("(el Template Method detecto fecha pasada y aborto la venta)");

        System.out.println("\nFin de la demostracion.");
    }
}
