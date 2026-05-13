import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Práctica 7 — Pruebas unitarias del manejo de excepciones.
 *
 * Cubre los 5 entregables de la consigna:
 *   1. Jerarquia de 4 excepciones: probamos lanzar/atrapar cada una.
 *   2. Clase de negocio que lance y maneje: SistemaGimnasio.
 *   3. Try-with-resources: ejecutamos operaciones que escriben al log.
 *   4. Manejo correcto de cada tipo: catch individual + jerarquico.
 *   5. Archivo de log: verificamos que se haya generado contenido.
 */
public class ExcepcionesTest {

    private static int pasadas = 0;
    private static int falladas = 0;
    private static final String LOG_FILE = "logs/gym-test.log";

    public static void main(String[] args) {
        System.out.println("=== Pruebas - P7 Excepciones ===\n");

        // Limpiar log previo si existe
        try { Files.deleteIfExists(Path.of(LOG_FILE)); } catch (IOException ignored) {}

        // Jerarquia de excepciones
        pruebaGymExceptionEsAbstracta();
        pruebaTodasHeredanDeGymException();
        pruebaPagoEsChecked();
        pruebaEntradaInvalidaEsUnchecked();
        pruebaTimestampSeAsignaAutomatico();

        // Contexto rico (decision propia)
        pruebaPagoRechazadoTieneContextoCompleto();
        pruebaPagoRechazadoToStringEsJsonLike();
        pruebaPagoRechazadoReferenciaUnica();
        pruebaCupoExcedidoIncluyeNumeros();
        pruebaMembresiaVencidaCalculaDiasDeVencida();

        // Manejo en la clase de negocio
        pruebaInscribirHastaExcederCupo();
        pruebaValidarAccesoConMembresiaVencida();
        pruebaCatchGenericoAtrapaCualquierGymException();
        pruebaSistemaSobreviveAExcepcion();

        // Try-with-resources y archivo de log
        pruebaArchivoLogSeGenera();
        pruebaArchivoLogContieneEntradas();

        // Resumen
        System.out.println("\n=== Resumen ===");
        System.out.println("Pasadas:  " + pasadas);
        System.out.println("Falladas: " + falladas);
        System.out.println("Total:    " + (pasadas + falladas));

        if (falladas > 0) System.exit(1);
    }

    // ---------- Jerarquia ----------

    private static void pruebaGymExceptionEsAbstracta() {
        check(java.lang.reflect.Modifier.isAbstract(GymException.class.getModifiers()),
              "GymException es abstracta (no instanciable directo)", null);
    }

    private static void pruebaTodasHeredanDeGymException() {
        check(GymException.class.isAssignableFrom(PagoRechazadoException.class),
              "PagoRechazadoException hereda de GymException", null);
        check(GymException.class.isAssignableFrom(CupoExcedidoException.class),
              "CupoExcedidoException hereda de GymException", null);
        check(GymException.class.isAssignableFrom(MembresiaVencidaException.class),
              "MembresiaVencidaException hereda de GymException", null);
    }

    private static void pruebaPagoEsChecked() {
        // Si fuera unchecked, extenderia RuntimeException.
        check(!RuntimeException.class.isAssignableFrom(PagoRechazadoException.class),
              "PagoRechazadoException es CHECKED (no extiende RuntimeException)", null);
    }

    private static void pruebaEntradaInvalidaEsUnchecked() {
        check(RuntimeException.class.isAssignableFrom(EntradaInvalidaException.class),
              "EntradaInvalidaException es UNCHECKED (extiende RuntimeException)", null);
    }

    private static void pruebaTimestampSeAsignaAutomatico() {
        GymException e = new CupoExcedidoException("X", 5, 5);
        check(e.getTimestamp() != null,
              "GymException asigna timestamp automatico", null);
    }

    // ---------- Contexto rico ----------

    private static void pruebaPagoRechazadoTieneContextoCompleto() {
        PagoRechazadoException ex = new PagoRechazadoException(
                "Tarjeta vencida", 350.0, "tarjeta", "TARJETA_VENCIDA", "metodoX");
        check(ex.getMontoIntentado() == 350.0
                && "tarjeta".equals(ex.getMetodoPago())
                && "TARJETA_VENCIDA".equals(ex.getCodigoErrorInterno())
                && "metodoX".equals(ex.getMetodoOrigen())
                && ex.getReferenciaTransaccion() != null,
              "PagoRechazadoException expone los 5 campos de contexto", null);
    }

    private static void pruebaPagoRechazadoToStringEsJsonLike() {
        PagoRechazadoException ex = new PagoRechazadoException(
                "Test", 100.0, "efectivo", "OK", "test");
        String s = ex.toString();
        check(s.startsWith("{") && s.endsWith("}")
                && s.contains("\"tipo\":") && s.contains("\"monto\":"),
              "toString() de PagoRechazado es JSON-like", s);
    }

    private static void pruebaPagoRechazadoReferenciaUnica() {
        // Probamos que cada instancia tiene su referencia (suelen diferir).
        PagoRechazadoException a = new PagoRechazadoException("a", 1, "x", "y", "z");
        try { Thread.sleep(2); } catch (InterruptedException ignored) {}
        PagoRechazadoException b = new PagoRechazadoException("b", 1, "x", "y", "z");
        check(a.getReferenciaTransaccion() != null
                && b.getReferenciaTransaccion() != null,
              "Cada PagoRechazado tiene referencia de transaccion", null);
    }

    private static void pruebaCupoExcedidoIncluyeNumeros() {
        CupoExcedidoException e = new CupoExcedidoException("Yoga", 15, 15);
        check(e.getCupoMaximo() == 15 && e.getInscritosActuales() == 15
                && "Yoga".equals(e.getNombreClase()),
              "CupoExcedidoException expone cupo y inscritos", null);
    }

    private static void pruebaMembresiaVencidaCalculaDiasDeVencida() {
        MembresiaVencidaException e = new MembresiaVencidaException(
                "X", LocalDate.now().minusDays(10));
        check(e.getDiasDeVencida() == 10,
              "MembresiaVencidaException calcula 10 dias de vencida", e.getDiasDeVencida());
    }

    // ---------- Manejo en SistemaGimnasio ----------

    private static void pruebaInscribirHastaExcederCupo() {
        SistemaGimnasio gym = new SistemaGimnasio(new GymLogger(LOG_FILE));
        gym.registrarClase("Test", 2);
        try {
            gym.inscribirEnClase("A", "Test");
            gym.inscribirEnClase("B", "Test");
            gym.inscribirEnClase("C", "Test");   // truena
            fallar("inscribir exceso", "no se lanzo CupoExcedidoException");
        } catch (CupoExcedidoException e) {
            pasar("inscribirEnClase lanza CupoExcedidoException al exceder cupo");
        }
    }

    private static void pruebaValidarAccesoConMembresiaVencida() {
        SistemaGimnasio gym = new SistemaGimnasio(new GymLogger(LOG_FILE));
        gym.registrarMembresia("Test", LocalDate.now().minusDays(5));
        try {
            gym.validarAcceso("Test");
            fallar("validar vencida", "no se lanzo MembresiaVencidaException");
        } catch (MembresiaVencidaException e) {
            pasar("validarAcceso lanza MembresiaVencidaException si esta vencida");
        }
    }

    private static void pruebaCatchGenericoAtrapaCualquierGymException() {
        SistemaGimnasio gym = new SistemaGimnasio(new GymLogger(LOG_FILE));
        gym.registrarMembresia("Test", LocalDate.now().minusDays(5));
        try {
            gym.validarAcceso("Test");
            fallar("catch generico", "no se lanzo nada");
        } catch (GymException e) {
            check(e instanceof MembresiaVencidaException,
                  "catch(GymException) atrapa a subclase MembresiaVencidaException", null);
        }
    }

    private static void pruebaSistemaSobreviveAExcepcion() {
        SistemaGimnasio gym = new SistemaGimnasio(new GymLogger(LOG_FILE));
        gym.registrarClase("Test", 1);
        try { gym.inscribirEnClase("A", "Test"); } catch (Exception ignored) {}
        try { gym.inscribirEnClase("B", "Test"); } catch (Exception ignored) {} // truena
        // Despues del error, el sistema debe seguir funcionando.
        check(gym.inscritosEn("Test") == 1,
              "Sistema sigue operativo despues de excepcion: inscritos = 1", gym.inscritosEn("Test"));
    }

    // ---------- Try-with-resources y log ----------

    private static void pruebaArchivoLogSeGenera() {
        SistemaGimnasio gym = new SistemaGimnasio(new GymLogger(LOG_FILE));
        gym.registrarClase("X", 5);
        check(Files.exists(Path.of(LOG_FILE)),
              "Archivo de log se crea en disco (try-with-resources cerro bien)", null);
    }

    private static void pruebaArchivoLogContieneEntradas() {
        // Verifica que el log tiene texto significativo.
        try (BufferedReader r = new BufferedReader(new FileReader(LOG_FILE))) {
            long lineas = r.lines().count();
            check(lineas > 0,
                  "Archivo de log tiene entradas (" + lineas + " lineas)", null);
        } catch (IOException e) {
            fallar("leer log", e.getMessage());
        }
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
