import java.time.LocalDate;
import java.util.List;

/**
 * Práctica 4 — Pruebas unitarias manuales para la jerarquia completa.
 *
 * Cubre cada clase concreta de la jerarquia, ademas de comportamientos
 * polimorficos en GestorMembresias.
 */
public class MembresiaTest {

    private static int pasadas = 0;
    private static int falladas = 0;

    public static void main(String[] args) {
        System.out.println("=== Pruebas - P4 Herencia ===\n");

        // Basica
        pruebaBasicaSeCreaConPrecioBase();
        pruebaBasicaSinDescuento();
        pruebaBasicaSeRenuevaPorTreintaDias();

        // Premium
        pruebaPremiumAplicaDescuentoSiVigente();
        pruebaPremiumPrecioCompletoSiNoVigente();
        pruebaPremiumAgendaClases();

        // VIP
        pruebaVIPSeRenuevaPorTrescientosSesentaYCincoDias();
        pruebaVIPTieneCuotaAnual();
        pruebaVIPRegistraAccesosSpa();

        // Polimorfismo via gestor
        pruebaGestorSumaIngresosVigentes();
        pruebaGestorFiltraPorTipo();
        pruebaGestorRenovarTodasActualizaFechas();

        // Reglas heredadas
        pruebaCancelarApagaActiva();
        pruebaMembresiaVencidaNoEstaVigente();
        pruebaTitularVacioEsRechazado();

        // Resumen
        System.out.println("\n=== Resumen ===");
        System.out.println("Pasadas:  " + pasadas);
        System.out.println("Falladas: " + falladas);
        System.out.println("Total:    " + (pasadas + falladas));

        if (falladas > 0) System.exit(1);
    }

    // ---------- Basica ----------

    private static void pruebaBasicaSeCreaConPrecioBase() {
        try {
            MembresiaBasica b = new MembresiaBasica("Test", LocalDate.now());
            assertEqual(b.calcularPrecio(), MembresiaBasica.PRECIO_BASE, "precio Basica");
            pasar("Basica se crea con precio base " + MembresiaBasica.PRECIO_BASE);
        } catch (AssertionError e) {
            fallar("Basica precio base", e.getMessage());
        }
    }

    private static void pruebaBasicaSinDescuento() {
        MembresiaBasica b = new MembresiaBasica("Test", LocalDate.now());
        if (b.descuentoRenovacion() == 0.0) {
            pasar("Basica no tiene descuento de renovacion");
        } else {
            fallar("Basica descuento", "esperaba 0, hubo " + b.descuentoRenovacion());
        }
    }

    private static void pruebaBasicaSeRenuevaPorTreintaDias() {
        MembresiaBasica b = new MembresiaBasica("Test", LocalDate.now());
        LocalDate finOriginal = b.getFechaFin();
        b.renovar();
        long dias = java.time.temporal.ChronoUnit.DAYS.between(finOriginal, b.getFechaFin());
        if (dias == 30) {
            pasar("Basica renueva sumando 30 dias");
        } else {
            fallar("Basica renovacion", "esperaba 30 dias, hubo " + dias);
        }
    }

    // ---------- Premium ----------

    private static void pruebaPremiumAplicaDescuentoSiVigente() {
        MembresiaPremium p = new MembresiaPremium("Test", LocalDate.now());
        double esperado = MembresiaPremium.PRECIO_BASE * 0.95;
        if (Math.abs(p.calcularPrecio() - esperado) < 0.01) {
            pasar("Premium vigente cobra con 5% descuento");
        } else {
            fallar("Premium descuento", "esperaba " + esperado + ", hubo " + p.calcularPrecio());
        }
    }

    private static void pruebaPremiumPrecioCompletoSiNoVigente() {
        MembresiaPremium p = new MembresiaPremium("Test", LocalDate.now().minusDays(40));
        // ya vencida (no estaVigente)
        if (Math.abs(p.calcularPrecio() - MembresiaPremium.PRECIO_BASE) < 0.01) {
            pasar("Premium vencida cobra precio completo sin descuento");
        } else {
            fallar("Premium precio completo", "hubo " + p.calcularPrecio());
        }
    }

    private static void pruebaPremiumAgendaClases() {
        MembresiaPremium p = new MembresiaPremium("Test", LocalDate.now());
        p.agendarClaseGrupal();
        p.agendarClaseGrupal();
        p.agendarClaseGrupal();
        if (p.getClasesGrupalesAgendadasEsteMes() == 3) {
            pasar("Premium contabiliza clases grupales agendadas");
        } else {
            fallar("Premium clases", "esperaba 3, hubo " + p.getClasesGrupalesAgendadasEsteMes());
        }
    }

    // ---------- VIP ----------

    private static void pruebaVIPSeRenuevaPorTrescientosSesentaYCincoDias() {
        MembresiaVIP v = new MembresiaVIP("Test", LocalDate.now());
        LocalDate finOriginal = v.getFechaFin();
        v.renovar();
        long dias = java.time.temporal.ChronoUnit.DAYS.between(finOriginal, v.getFechaFin());
        if (dias == 365) {
            pasar("VIP renueva sumando 365 dias (anual)");
        } else {
            fallar("VIP renovacion", "esperaba 365 dias, hubo " + dias);
        }
    }

    private static void pruebaVIPTieneCuotaAnual() {
        MembresiaVIP v = new MembresiaVIP("Test", LocalDate.now().minusDays(400));
        // vencida, sin descuento
        if (Math.abs(v.calcularPrecio() - MembresiaVIP.CUOTA_ANUAL) < 0.01) {
            pasar("VIP cobra cuota anual sin descuento si vencida");
        } else {
            fallar("VIP precio anual", "hubo " + v.calcularPrecio());
        }
    }

    private static void pruebaVIPRegistraAccesosSpa() {
        MembresiaVIP v = new MembresiaVIP("Test", LocalDate.now());
        v.registrarAccesoSpa();
        v.registrarAccesoSpa();
        if (v.getAccesosSpaConsumidosEsteAnio() == 2) {
            pasar("VIP registra accesos a spa");
        } else {
            fallar("VIP spa", "esperaba 2, hubo " + v.getAccesosSpaConsumidosEsteAnio());
        }
    }

    // ---------- Polimorfismo via gestor ----------

    private static void pruebaGestorSumaIngresosVigentes() {
        GestorMembresias g = new GestorMembresias();
        g.agregar(new MembresiaBasica("A", LocalDate.now()));           // 350
        g.agregar(new MembresiaPremium("B", LocalDate.now()));          // 650 * 0.95 = 617.5
        g.agregar(new MembresiaVIP("C", LocalDate.now()));              // 14400 * 0.90 = 12960
        g.agregar(new MembresiaBasica("D", LocalDate.now().minusDays(60))); // vencida, no suma

        double esperado = 350 + (650 * 0.95) + (14400 * 0.90);
        double real = g.ingresosEsperados();
        if (Math.abs(real - esperado) < 0.01) {
            pasar("Gestor suma ingresos polimorficos correctamente");
        } else {
            fallar("Gestor ingresos", "esperaba " + esperado + ", hubo " + real);
        }
    }

    private static void pruebaGestorFiltraPorTipo() {
        GestorMembresias g = new GestorMembresias();
        g.agregar(new MembresiaBasica("A", LocalDate.now()));
        g.agregar(new MembresiaBasica("B", LocalDate.now()));
        g.agregar(new MembresiaPremium("C", LocalDate.now()));
        g.agregar(new MembresiaVIP("D", LocalDate.now()));

        List<Membresia> basicas = g.filtrarPorTipo(MembresiaBasica.class);
        List<Membresia> premiums = g.filtrarPorTipo(MembresiaPremium.class);
        List<Membresia> vips = g.filtrarPorTipo(MembresiaVIP.class);

        if (basicas.size() == 2 && premiums.size() == 1 && vips.size() == 1) {
            pasar("Gestor filtra correctamente por tipo concreto");
        } else {
            fallar("Gestor filtro",
                    String.format("Basicas=%d Premiums=%d VIPs=%d",
                            basicas.size(), premiums.size(), vips.size()));
        }
    }

    private static void pruebaGestorRenovarTodasActualizaFechas() {
        GestorMembresias g = new GestorMembresias();
        MembresiaBasica b = new MembresiaBasica("A", LocalDate.now());
        MembresiaVIP v = new MembresiaVIP("B", LocalDate.now());
        g.agregar(b);
        g.agregar(v);

        LocalDate finBasicaAntes = b.getFechaFin();
        LocalDate finVIPAntes = v.getFechaFin();

        g.renovarTodas();

        long diasB = java.time.temporal.ChronoUnit.DAYS.between(finBasicaAntes, b.getFechaFin());
        long diasV = java.time.temporal.ChronoUnit.DAYS.between(finVIPAntes, v.getFechaFin());

        if (diasB == 30 && diasV == 365) {
            pasar("renovarTodas() aplica reglas polimorficas (30 dias / 365 dias)");
        } else {
            fallar("Gestor renovar todas", "diasB=" + diasB + " diasV=" + diasV);
        }
    }

    // ---------- Heredados de Membresia ----------

    private static void pruebaCancelarApagaActiva() {
        Membresia m = new MembresiaBasica("Test", LocalDate.now());
        m.cancelar();
        if (!m.isActiva() && !m.estaVigente()) {
            pasar("cancelar() apaga activa y rompe vigencia");
        } else {
            fallar("cancelar", "activa=" + m.isActiva() + " vigente=" + m.estaVigente());
        }
    }

    private static void pruebaMembresiaVencidaNoEstaVigente() {
        Membresia m = new MembresiaBasica("Test", LocalDate.now().minusDays(60));
        if (!m.estaVigente()) {
            pasar("membresia con fecha pasada no esta vigente");
        } else {
            fallar("vigencia pasada", "deberia ser no vigente");
        }
    }

    private static void pruebaTitularVacioEsRechazado() {
        try {
            new MembresiaBasica("   ", LocalDate.now());
            fallar("titular vacio", "deberia lanzar excepcion");
        } catch (IllegalArgumentException e) {
            pasar("titular vacio rechazado por constructor padre");
        }
    }

    // ---------- Utilidades ----------

    private static void pasar(String d) {
        pasadas++;
        System.out.println("  [OK] " + d);
    }

    private static void fallar(String d, String detalle) {
        falladas++;
        System.out.println("  [FAIL] " + d + ": " + detalle);
    }

    private static void assertEqual(double real, double esperado, String etiqueta) {
        if (Math.abs(real - esperado) > 0.01) {
            throw new AssertionError(etiqueta + ": esperaba " + esperado + " hubo " + real);
        }
    }
}
