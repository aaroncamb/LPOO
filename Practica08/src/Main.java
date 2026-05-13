import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/**
 * Práctica 8 — Programa principal con MENU INTERACTIVO.
 *
 * El menu cubre las operaciones CRUD, ordenamientos, busqueda compuesta
 * y la cola de notificaciones. Esta diseñado para ser navegable por
 * teclado durante la defensa oral.
 */
public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final GestorClientes gestor = new GestorClientes();

    public static void main(String[] args) {
        System.out.println("=== Sistema de gestion de clientes (P8) ===");
        System.out.println("Cargando 20 clientes de prueba...");
        DatosPrueba.cargar(gestor);
        System.out.println("Listo. Total: " + gestor.total() + " clientes.\n");

        boolean salir = false;
        while (!salir) {
            imprimirMenu();
            int op = leerEntero("Opcion: ");
            System.out.println();
            try {
                switch (op) {
                    case 1  -> opListar();
                    case 2  -> opAgregar();
                    case 3  -> opBuscarPorId();
                    case 4  -> opBuscarPorNombre();
                    case 5  -> opEliminar();
                    case 6  -> opOrdenar();
                    case 7  -> opBusquedaCompuesta();
                    case 8  -> opNotificaciones();
                    case 9  -> opEstadisticas();
                    case 0  -> salir = true;
                    default -> System.out.println("Opcion no valida.\n");
                }
            } catch (IllegalArgumentException ex) {
                System.out.println("Entrada invalida: " + ex.getMessage() + "\n");
            }
        }
        System.out.println("Hasta luego.");
    }

    // ---------------------------------------------------------------
    //   MENU
    // ---------------------------------------------------------------

    private static void imprimirMenu() {
        System.out.println("------------------------------------------------");
        System.out.println("  1. Listar todos los clientes");
        System.out.println("  2. Agregar nuevo cliente");
        System.out.println("  3. Buscar por id");
        System.out.println("  4. Buscar por nombre (parcial)");
        System.out.println("  5. Eliminar por id");
        System.out.println("  6. Ordenar (nombre / antiguedad / mas reciente)");
        System.out.println("  7. Busqueda COMPUESTA (decision propia)");
        System.out.println("  8. Cola de notificaciones");
        System.out.println("  9. Estadisticas");
        System.out.println("  0. Salir");
        System.out.println("------------------------------------------------");
    }

    // ---------------------------------------------------------------
    //   OPERACIONES DEL MENU
    // ---------------------------------------------------------------

    private static void opListar() {
        System.out.println("--- Todos los clientes (" + gestor.total() + ") ---");
        for (Cliente c : gestor.todos()) {
            System.out.println("  " + c);
        }
        System.out.println();
    }

    private static void opAgregar() {
        System.out.println("--- Agregar cliente nuevo ---");
        int id = leerEntero("  Id: ");
        String nombre = leerLinea("  Nombre completo: ");
        String email  = leerLinea("  Email: ");
        double peso   = leerDouble("  Peso kg (0 si no medido): ");

        System.out.println("  Tipo de membresia: 1) BASICA  2) PREMIUM  3) VIP");
        int t = leerEntero("  Opcion: ");
        Cliente.TipoMembresia tipo = switch (t) {
            case 2 -> Cliente.TipoMembresia.PREMIUM;
            case 3 -> Cliente.TipoMembresia.VIP;
            default -> Cliente.TipoMembresia.BASICA;
        };

        Cliente nuevo = new Cliente(id, nombre, email, LocalDate.now(), peso, tipo);
        boolean ok = gestor.agregar(nuevo);
        System.out.println(ok
                ? "  Agregado: " + nuevo
                : "  No se agrego (id o email duplicado).");
        System.out.println();
    }

    private static void opBuscarPorId() {
        int id = leerEntero("Id a buscar: ");
        Optional<Cliente> r = gestor.buscarPorId(id);
        System.out.println(r.isPresent()
                ? "  Encontrado: " + r.get()
                : "  No existe cliente con id " + id);
        System.out.println();
    }

    private static void opBuscarPorNombre() {
        String texto = leerLinea("Texto a buscar (substring): ");
        List<Cliente> r = gestor.buscarPorNombre(texto);
        if (r.isEmpty()) System.out.println("  Sin coincidencias.");
        else {
            System.out.println("  Coincidencias (" + r.size() + "):");
            for (Cliente c : r) System.out.println("    " + c);
        }
        System.out.println();
    }

    private static void opEliminar() {
        int id = leerEntero("Id a eliminar: ");
        boolean ok = gestor.eliminarPorId(id);
        System.out.println(ok
                ? "  Eliminado. Quedan " + gestor.total() + " clientes."
                : "  No existe cliente con id " + id);
        System.out.println();
    }

    private static void opOrdenar() {
        System.out.println("  1) Por nombre (orden natural)");
        System.out.println("  2) Por antiguedad (mas viejo primero)");
        System.out.println("  3) Por mas reciente (inscripcion descendente)");
        int t = leerEntero("Opcion: ");
        List<Cliente> ord = switch (t) {
            case 2  -> gestor.ordenadosPorAntiguedad();
            case 3  -> gestor.ordenadosPorMasReciente();
            default -> gestor.ordenadosPorNombre();
        };
        System.out.println();
        for (Cliente c : ord) System.out.println("  " + c);
        System.out.println();
    }

    private static void opBusquedaCompuesta() {
        System.out.println("--- Busqueda compuesta ---");
        System.out.println("\"Nuevos clientes Premium/VIP activos despues de cierta fecha,");
        System.out.println(" ordenados por inscripcion mas reciente, top N.\"\n");

        LocalDate fechaCorte = leerFecha("  Fecha de corte (YYYY-MM-DD): ");
        int limite = leerEntero("  Cuantos resultados (N): ");

        List<Cliente> r = gestor.nuevosPremiumDesde(fechaCorte, limite);
        System.out.println();
        if (r.isEmpty()) {
            System.out.println("  Sin coincidencias.");
        } else {
            System.out.println("  Resultados (" + r.size() + "):");
            for (Cliente c : r) System.out.println("    " + c);
        }
        System.out.println();
    }

    private static void opNotificaciones() {
        System.out.println("  1) Encolar notificacion nueva");
        System.out.println("  2) Procesar (sacar) siguiente");
        System.out.println("  3) Ver cuantas hay pendientes");
        int t = leerEntero("Opcion: ");

        switch (t) {
            case 1 -> {
                int destinatario = leerEntero("  Id del destinatario: ");
                String msg = leerLinea("  Mensaje: ");
                gestor.encolarNotificacion(new Notificacion(
                        destinatario, msg, Notificacion.Canal.EMAIL));
                System.out.println("  Notificacion encolada. Pendientes: "
                        + gestor.notificacionesPendientes());
            }
            case 2 -> {
                Optional<Notificacion> n = gestor.procesarSiguienteNotificacion();
                System.out.println(n.isPresent()
                        ? "  Procesada: " + n.get()
                        : "  No hay notificaciones pendientes.");
            }
            case 3 -> System.out.println("  Pendientes: " + gestor.notificacionesPendientes());
            default -> System.out.println("  Opcion no valida.");
        }
        System.out.println();
    }

    private static void opEstadisticas() {
        System.out.println("--- Estadisticas ---");
        System.out.println("  Total clientes:  " + gestor.total());
        System.out.println("  Activos:         " + gestor.soloActivos().size());
        System.out.printf ("  Peso promedio:   %.1f kg (solo con registro)%n",
                gestor.pesoPromedioConRegistro());

        Map<Cliente.TipoMembresia, Long> conteo = gestor.conteoPorTipo();
        System.out.println("  Por tipo:");
        for (Cliente.TipoMembresia t : Cliente.TipoMembresia.values()) {
            System.out.printf("    %-8s %d%n", t, conteo.getOrDefault(t, 0L));
        }
        System.out.println();
    }

    // ---------------------------------------------------------------
    //   HELPERS PARA LECTURA DE ENTRADA
    // ---------------------------------------------------------------

    private static int leerEntero(String prompt) {
        while (true) {
            System.out.print(prompt);
            String l = sc.nextLine().trim();
            try { return Integer.parseInt(l); }
            catch (NumberFormatException e) {
                System.out.println("  (escribe un numero entero)");
            }
        }
    }

    private static double leerDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String l = sc.nextLine().trim().replace(',', '.');
            try { return Double.parseDouble(l); }
            catch (NumberFormatException e) {
                System.out.println("  (escribe un numero)");
            }
        }
    }

    private static String leerLinea(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private static LocalDate leerFecha(String prompt) {
        while (true) {
            System.out.print(prompt);
            String l = sc.nextLine().trim();
            try { return LocalDate.parse(l); }
            catch (Exception e) {
                System.out.println("  (formato YYYY-MM-DD, ejemplo: 2025-01-15)");
            }
        }
    }
}
