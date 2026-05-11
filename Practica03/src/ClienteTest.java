import java.time.LocalDate;

/**
 * Práctica 3 — Pruebas unitarias manuales.
 *
 * No se usa JUnit porque la consigna no lo requiere y no se ha visto
 * todavia en clase. En su lugar, cada prueba es un bloque try/catch que
 * valida una expectativa y reporta exito o fracaso. Al final imprime un
 * resumen con el total de pasadas y falladas.
 *
 * Cubre tres categorias:
 *   1. Encapsulamiento (los getters devuelven lo que el setter aceptó).
 *   2. Validaciones de Cliente (peso, altura, email, nombre).
 *   3. Validaciones de Membresia (tipo, fechas, precio, renovacion).
 */
public class ClienteTest {

    private static int pasadas = 0;
    private static int falladas = 0;

    public static void main(String[] args) {
        System.out.println("=== Pruebas unitarias - Practica 3 ===\n");

        // -------- Grupo 1: Encapsulamiento --------
        pruebaGettersDevuelvenLoAsignado();
        pruebaToStringIncluyeMembresia();

        // -------- Grupo 2: Validaciones de Cliente --------
        pruebaPesoValidoSeAcepta();
        pruebaPesoNegativoEsRechazado();
        pruebaPesoFueraDeRangoEsRechazado();
        pruebaPesoCeroSeAceptaComoCasoEspecial();
        pruebaEmailValidoSeAcepta();
        pruebaEmailSinArrobaEsRechazado();
        pruebaEmailSinPuntoEsRechazado();
        pruebaNombreCortoEsRechazado();
        pruebaFechaFuturaEsRechazada();
        pruebaAlturaFueraDeRangoEsRechazada();

        // -------- Grupo 3: Validaciones de Membresia --------
        pruebaTipoMembresiaInvalidoEsRechazado();
        pruebaPrecioNegativoEsRechazado();
        pruebaRenovacionExtiendeFechaFin();
        pruebaMembresiaVigenteSegunFechas();

        // -------- Resumen --------
        System.out.println();
        System.out.println("=== Resumen ===");
        System.out.println("Pruebas pasadas:  " + pasadas);
        System.out.println("Pruebas falladas: " + falladas);
        System.out.println("Total:            " + (pasadas + falladas));

        if (falladas > 0) {
            System.exit(1);
        }
    }

    // ---------------- Grupo 1: Encapsulamiento ----------------

    private static void pruebaGettersDevuelvenLoAsignado() {
        try {
            Cliente c = new Cliente(7, "Test User", "test@correo.mx");
            c.setPesoKg(75);
            c.setAlturaCm(180);

            if (c.getId() != 7)               { fallar("id se altero", "getters"); return; }
            if (!c.getEmail().equals("test@correo.mx")) { fallar("email se altero", "getters"); return; }
            if (c.getPesoKg() != 75)          { fallar("peso se altero", "getters"); return; }
            if (c.getAlturaCm() != 180)       { fallar("altura se altero", "getters"); return; }
            pasar("getters devuelven lo asignado");
        } catch (Exception e) {
            fallar("excepcion inesperada", "getters: " + e.getMessage());
        }
    }

    private static void pruebaToStringIncluyeMembresia() {
        try {
            Cliente c = new Cliente(8, "Sara Lopez", "sara@correo.mx");
            c.setMembresia(new Membresia(Membresia.TIPO_PREMIUM));
            String repr = c.toString();
            if (!repr.contains("Premium")) {
                fallar("toString no incluye tipo de membresia", "toString");
                return;
            }
            pasar("toString refleja la membresia asociada");
        } catch (Exception e) {
            fallar("excepcion inesperada", "toString: " + e.getMessage());
        }
    }

    // ---------------- Grupo 2: Validaciones de Cliente ----------------

    private static void pruebaPesoValidoSeAcepta() {
        try {
            Cliente c = new Cliente(10, "Valido", "v@correo.mx");
            c.setPesoKg(70);
            if (c.getPesoKg() == 70) {
                pasar("peso 70 kg aceptado");
            } else {
                fallar("peso 70 no quedo asignado", "peso valido");
            }
        } catch (Exception e) {
            fallar("lanzo excepcion con peso valido", e.getMessage());
        }
    }

    private static void pruebaPesoNegativoEsRechazado() {
        Cliente c = new Cliente(11, "Test", "t@correo.mx");
        try {
            c.setPesoKg(-10);
            fallar("peso negativo no fue rechazado", "peso negativo");
        } catch (IllegalArgumentException e) {
            pasar("peso negativo rechazado: " + extracto(e));
        }
    }

    private static void pruebaPesoFueraDeRangoEsRechazado() {
        Cliente c = new Cliente(12, "Test", "t@correo.mx");
        try {
            c.setPesoKg(15);     // por debajo del minimo 30
            fallar("peso 15 (bajo el min) no fue rechazado", "peso fuera de rango");
        } catch (IllegalArgumentException e) {
            // ok
        }
        try {
            c.setPesoKg(500);    // por encima del max 300
            fallar("peso 500 (sobre el max) no fue rechazado", "peso fuera de rango");
        } catch (IllegalArgumentException e) {
            pasar("peso fuera de rango (15 y 500) rechazado");
        }
    }

    private static void pruebaPesoCeroSeAceptaComoCasoEspecial() {
        try {
            Cliente c = new Cliente(13, "Sin pesar", "sp@correo.mx");
            c.setPesoKg(0);
            if (c.getPesoKg() == 0) {
                pasar("peso 0 aceptado como caso especial (no pesado aun)");
            } else {
                fallar("peso 0 no quedo asignado", "peso cero");
            }
        } catch (IllegalArgumentException e) {
            fallar("peso 0 fue rechazado y no deberia", e.getMessage());
        }
    }

    private static void pruebaEmailValidoSeAcepta() {
        try {
            Cliente c = new Cliente(14, "Test", "valido@correo.mx");
            if (c.getEmail().equals("valido@correo.mx")) {
                pasar("email valido aceptado");
            } else {
                fallar("email no quedo asignado correctamente", "email valido");
            }
        } catch (Exception e) {
            fallar("excepcion con email valido", e.getMessage());
        }
    }

    private static void pruebaEmailSinArrobaEsRechazado() {
        try {
            new Cliente(15, "Test", "sin-arroba");
            fallar("email sin arroba no fue rechazado", "email sin @");
        } catch (IllegalArgumentException e) {
            pasar("email sin @ rechazado");
        }
    }

    private static void pruebaEmailSinPuntoEsRechazado() {
        try {
            new Cliente(16, "Test", "x@dominiosinpunto");
            fallar("email sin punto en el dominio no fue rechazado", "email sin .");
        } catch (IllegalArgumentException e) {
            pasar("email sin punto en dominio rechazado");
        }
    }

    private static void pruebaNombreCortoEsRechazado() {
        try {
            new Cliente(17, "A", "a@correo.mx");
            fallar("nombre de 1 caracter no fue rechazado", "nombre corto");
        } catch (IllegalArgumentException e) {
            pasar("nombre demasiado corto rechazado");
        }
    }

    private static void pruebaFechaFuturaEsRechazada() {
        Cliente c = new Cliente(18, "Test", "t@correo.mx");
        try {
            c.setFechaRegistro(LocalDate.now().plusYears(1));
            fallar("fecha futura no fue rechazada", "fecha futura");
        } catch (IllegalArgumentException e) {
            pasar("fecha de registro futura rechazada");
        }
    }

    private static void pruebaAlturaFueraDeRangoEsRechazada() {
        Cliente c = new Cliente(19, "Test", "t@correo.mx");
        try {
            c.setAlturaCm(50);
            fallar("altura 50 cm no fue rechazada", "altura baja");
        } catch (IllegalArgumentException e) {
            // ok
        }
        try {
            c.setAlturaCm(300);
            fallar("altura 300 cm no fue rechazada", "altura imposible");
        } catch (IllegalArgumentException e) {
            pasar("altura fuera de rango (50 y 300 cm) rechazada");
        }
    }

    // ---------------- Grupo 3: Validaciones de Membresia ----------------

    private static void pruebaTipoMembresiaInvalidoEsRechazado() {
        try {
            new Membresia("Diamante");
            fallar("tipo Diamante no fue rechazado", "tipo invalido");
        } catch (IllegalArgumentException e) {
            pasar("tipo de membresia invalido rechazado");
        }
    }

    private static void pruebaPrecioNegativoEsRechazado() {
        Membresia m = new Membresia(Membresia.TIPO_BASICA);
        try {
            m.setPrecioMensual(-100);
            fallar("precio negativo no fue rechazado", "precio negativo");
        } catch (IllegalArgumentException e) {
            pasar("precio negativo rechazado");
        }
    }

    private static void pruebaRenovacionExtiendeFechaFin() {
        Membresia m = new Membresia(Membresia.TIPO_BASICA);
        LocalDate finOriginal = m.getFechaFin();
        m.renovar(30);
        if (m.getFechaFin().isAfter(finOriginal)) {
            pasar("renovacion extendio la fecha de fin");
        } else {
            fallar("renovacion no extendio la fecha", "renovar");
        }
    }

    private static void pruebaMembresiaVigenteSegunFechas() {
        Membresia activa = new Membresia(Membresia.TIPO_PREMIUM);
        if (!activa.estaVigente()) {
            fallar("membresia recien creada no aparece vigente", "vigencia");
            return;
        }
        Membresia vieja = new Membresia(Membresia.TIPO_BASICA,
                LocalDate.now().minusDays(60),
                LocalDate.now().minusDays(30),
                350.0);
        if (vieja.estaVigente()) {
            fallar("membresia vencida aparece vigente", "vigencia");
            return;
        }
        pasar("vigencia se calcula bien (nueva vigente, vencida no)");
    }

    // ---------------- Utilidades de reporte ----------------

    private static void pasar(String descripcion) {
        pasadas++;
        System.out.println("  [OK] " + descripcion);
    }

    private static void fallar(String descripcion, String etiqueta) {
        falladas++;
        System.out.println("  [FAIL] (" + etiqueta + ") " + descripcion);
    }

    /** Devuelve los primeros 60 caracteres del mensaje, para reportes legibles. */
    private static String extracto(Exception e) {
        String m = e.getMessage();
        return m.length() <= 60 ? m : m.substring(0, 57) + "...";
    }
}
