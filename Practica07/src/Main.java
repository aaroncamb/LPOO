import java.time.LocalDate;

/**
 * Práctica 7 — Programa principal.
 *
 * Demuestra:
 *   - Manejo correcto de las 4 excepciones (3 checked + 1 unchecked).
 *   - Try-with-resources (vive dentro de GymLogger.escribir).
 *   - Captura granular: distintos catch para distintos tipos.
 *   - Excepcion rica (PagoRechazadoException) y su info de contexto.
 *   - El sistema sigue funcionando despues de una excepcion.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== P7: Manejo de excepciones ===\n");

        GymLogger logger = new GymLogger("logs/gym.log");
        SistemaGimnasio gym = new SistemaGimnasio(logger);

        // ---- Setup ----
        gym.registrarClase("Yoga matutino", 2);          // cupo bajo a proposito
        gym.registrarClase("Spinning HIIT", 5);
        gym.registrarMembresia("Ana Perez",       LocalDate.now().plusDays(30));
        gym.registrarMembresia("Bruno Hernandez", LocalDate.now().plusDays(15));
        gym.registrarMembresia("Carolina Mendez", LocalDate.now().minusDays(7));  // ya vencida
        gym.registrarMembresia("David Ortega",    LocalDate.now().plusDays(60));

        System.out.println("\n--- ESCENARIO 1: catch granular de cada excepcion ---\n");

        // 1a. PagoRechazadoException (con la seed fija el 1er pago FALLA)
        System.out.println("[1a] Intentando cobrar a Ana...");
        try {
            gym.procesarPago("Ana Perez", 350.0, "tarjeta");
            System.out.println("    Pago exitoso.");
        } catch (PagoRechazadoException e) {
            System.out.println("    PAGO RECHAZADO");
            System.out.println("    Mensaje:     " + e.getMessage());
            System.out.println("    Referencia:  " + e.getReferenciaTransaccion());
            System.out.println("    Codigo:      " + e.getCodigoErrorInterno());
            System.out.println("    Origen:      " + e.getMetodoOrigen());
            System.out.println("    Monto:       $" + e.getMontoIntentado());
            System.out.println("    Categoria:   " + e.categoria());
            System.out.println("    JSON-dump:");
            System.out.println("      " + e);   // toString rico
        }

        // 1b. CupoExcedidoException
        System.out.println("\n[1b] Llenando 'Yoga matutino' hasta exceder cupo...");
        try {
            gym.inscribirEnClase("Ana Perez",       "Yoga matutino");   // 1/2
            gym.inscribirEnClase("Bruno Hernandez", "Yoga matutino");   // 2/2
            gym.inscribirEnClase("David Ortega",    "Yoga matutino");   // truena
            System.out.println("    (no deberia llegar aqui)");
        } catch (CupoExcedidoException e) {
            System.out.println("    CUPO EXCEDIDO");
            System.out.println("    Mensaje:     " + e.getMessage());
            System.out.println("    Inscritos:   " + e.getInscritosActuales() + "/" + e.getCupoMaximo());
            System.out.println("    Clase:       " + e.getNombreClase());
            System.out.println("    Sugerencia:  Inscribir a David en otra clase con cupo.");
        }

        // 1c. MembresiaVencidaException
        System.out.println("\n[1c] Carolina (membresia vencida) intenta entrar...");
        try {
            gym.validarAcceso("Carolina Mendez");
        } catch (MembresiaVencidaException e) {
            System.out.println("    MEMBRESIA VENCIDA");
            System.out.println("    Mensaje:        " + e.getMessage());
            System.out.println("    Vencio el:      " + e.getFechaVencimiento());
            System.out.println("    Dias de vencida: " + e.getDiasDeVencida());
        }

        // 1d. EntradaInvalidaException (UNCHECKED, no esta en catch del flujo normal)
        System.out.println("\n[1d] Pasando monto invalido (bug del programador)...");
        try {
            gym.procesarPago("Ana Perez", -500, "efectivo");
        } catch (EntradaInvalidaException e) {
            System.out.println("    ENTRADA INVALIDA (unchecked)");
            System.out.println("    Mensaje: " + e.getMessage());
            System.out.println("    Campo:   " + e.getCampo());
            System.out.println("    Valor:   " + e.getValorRecibido());
            System.out.println("    Nota: este catch es OPCIONAL (es RuntimeException).");
        } catch (PagoRechazadoException e) {
            // este catch nunca se ejecutara aqui
        }

        // ---- ESCENARIO 2: catch jerarquico (catch del padre comun) ----
        System.out.println("\n--- ESCENARIO 2: catch jerarquico GymException ---\n");
        try {
            gym.validarAcceso("Carolina Mendez");   // lanza MembresiaVencidaException
        } catch (GymException e) {
            // un solo catch maneja cualquier subtipo
            System.out.println("    Capturado como GymException generica");
            System.out.println("    Categoria: " + e.categoria());
            System.out.println("    Mensaje:   " + e.getMessage());
            System.out.println("    Esto demuestra que catch(GymException) atrapa a todas las hijas.");
        }

        // ---- ESCENARIO 3: flujo compuesto con recuperacion ----
        System.out.println("\n--- ESCENARIO 3: flujo compuesto, sigue funcionando despues de error ---\n");
        // Intentamos 4 flujos seguidos; algunos van a fallar pero el sistema no se cae.
        for (String cliente : new String[]{"Ana Perez", "Bruno Hernandez", "Carolina Mendez", "David Ortega"}) {
            boolean ok = gym.intentarFlujoCompleto(cliente, "Spinning HIIT", 250.0, "tarjeta");
            System.out.printf("  [%s] %s%n", ok ? "OK   " : "FALLO", cliente);
        }

        // ---- ESCENARIO 4: el log se escribio en disco ----
        System.out.println("\n--- ESCENARIO 4: revisar archivo de log ---\n");
        System.out.println("Las ultimas entradas del log estan en: " + logger.getArchivoLog());
        System.out.println("(abrelo con un editor para ver el registro completo)");

        System.out.println("\nFin de la demostracion.");
    }
}
