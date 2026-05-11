import java.time.LocalDate;

/**
 * Práctica 3 — Programa principal.
 *
 * Demuestra:
 *   - Creacion de clientes con validaciones activas.
 *   - Composicion Cliente-Membresia.
 *   - Que las validaciones de los setters rechazan datos invalidos.
 *   - Que los atributos privados NO son accesibles desde fuera de la
 *     clase (los intentos estan comentados con la explicacion del error).
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("=== P3: Encapsulamiento y validaciones ===\n");

        // --- 1. Creacion de un cliente valido con membresia ---
        Cliente ana = new Cliente(1001, "Ana Gabriela Perez Soto",
                                  "ana.perez@correo.mx");
        ana.setPesoKg(62.5);
        ana.setAlturaCm(165);
        ana.setMembresia(new Membresia(Membresia.TIPO_PREMIUM));

        System.out.println("Cliente creado:");
        System.out.println("  " + ana);
        System.out.println("  Membresia: " + ana.getMembresia());
        System.out.printf ("  IMC: %.2f%n%n", ana.calcularIMC());

        // --- 2. Demostracion de acceso PUBLICO vs PRIVADO ---
        //
        // En P2 podia escribir directamente:   ana.pesoKg = -500;
        // En P3 esa linea NO COMPILA porque pesoKg es private.
        //
        // Si quitas el comentario de la linea siguiente, veras el error:
        //   "pesoKg has private access in Cliente"
        //
        // ana.pesoKg = -500;   // <-- error de compilacion
        //
        // Lo unico permitido desde fuera es pasar por el setter, que sí
        // valida el dato antes de aceptarlo:
        try {
            ana.setPesoKg(-500);
        } catch (IllegalArgumentException e) {
            System.out.println("Intento de peso invalido rechazado:");
            System.out.println("  -> " + e.getMessage() + "\n");
        }

        // --- 3. Bateria de validaciones en setters ---

        System.out.println("--- Email invalido ---");
        intentarCrear(() -> new Cliente(2, "Bruno", "bruno-sin-arroba"));
        intentarCrear(() -> new Cliente(2, "Bruno", "bruno@sinpunto"));
        intentarCrear(() -> new Cliente(2, "Bruno", "@correo.mx"));

        System.out.println("\n--- Peso fuera de rango ---");
        Cliente tmp = new Cliente(3, "Test User", "test@correo.mx");
        intentar(() -> tmp.setPesoKg(15));     // muy bajo
        intentar(() -> tmp.setPesoKg(450));    // muy alto

        System.out.println("\n--- Altura fuera de rango ---");
        intentar(() -> tmp.setAlturaCm(80));   // muy baja
        intentar(() -> tmp.setAlturaCm(280));  // imposible

        System.out.println("\n--- Fecha de registro futura ---");
        intentar(() -> tmp.setFechaRegistro(LocalDate.now().plusDays(5)));

        System.out.println("\n--- Nombre demasiado corto ---");
        intentarCrear(() -> new Cliente(4, "A", "a@correo.mx"));

        System.out.println("\n--- Tipo de membresia invalido ---");
        intentar(() -> new Membresia("Premiun"));   // typo intencional

        System.out.println("\n--- Renovacion con dias negativos ---");
        Membresia m = new Membresia(Membresia.TIPO_BASICA);
        intentar(() -> m.renovar(-10));

        // --- 4. Caso especial: peso 0 (cliente que aun no se pesa) ---
        System.out.println("\n--- Caso valido: peso 0 (sin pesar) ---");
        Cliente nuevo = new Cliente(5, "Recien Inscrito", "nuevo@correo.mx");
        nuevo.setPesoKg(0);      // permitido
        nuevo.setAlturaCm(0);    // permitido
        System.out.println("  " + nuevo);
        System.out.printf ("  IMC: %s (n/d porque faltan datos)%n",
                nuevo.calcularIMC() < 0 ? "n/d" : String.valueOf(nuevo.calcularIMC()));

        // --- 5. Composicion: cambiar membresia del cliente ---
        System.out.println("\n--- Composicion: cliente cambia de plan ---");
        System.out.println("  Ana antes:  " + ana.getMembresia());
        ana.setMembresia(new Membresia(Membresia.TIPO_VIP));
        System.out.println("  Ana ahora:  " + ana.getMembresia());

        System.out.println("\nFin de la demostracion.");
    }

    // -------- helpers para mantener legible el main --------

    /** Intenta crear un Cliente; imprime el motivo del rechazo. */
    private static void intentarCrear(java.util.function.Supplier<Cliente> accion) {
        try {
            accion.get();
            System.out.println("  (no se lanzo excepcion, revisar validacion)");
        } catch (IllegalArgumentException e) {
            System.out.println("  -> " + e.getMessage());
        }
    }

    /** Ejecuta una accion que podria lanzar IllegalArgumentException. */
    private static void intentar(Runnable accion) {
        try {
            accion.run();
            System.out.println("  (no se lanzo excepcion, revisar validacion)");
        } catch (IllegalArgumentException e) {
            System.out.println("  -> " + e.getMessage());
        }
    }
}
