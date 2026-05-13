import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Práctica 8 — Pruebas unitarias manuales.
 *
 * Cobertura:
 *   - CRUD completo (agregar / buscar / actualizar / eliminar).
 *   - Las 4 estructuras (ArrayList, HashMap, HashSet, LinkedList).
 *   - Comparable (orden natural) y Comparators (los dos).
 *   - Streams: filtrado, busqueda compuesta.
 *   - Iteradores: contar + iterator.remove().
 *   - Cola FIFO de notificaciones.
 */
public class GestorTest {

    private static int pasadas = 0;
    private static int falladas = 0;

    public static void main(String[] args) {
        System.out.println("=== Pruebas - P8 Colecciones ===\n");

        pruebaAgregarYBuscarPorId();
        pruebaIdDuplicadoRechazado();
        pruebaEmailDuplicadoRechazado();
        pruebaBuscarPorNombreParcial();
        pruebaEliminarLimpiaLasTresEstructuras();
        pruebaCambiarEstado();
        pruebaOrdenNaturalPorNombre();
        pruebaComparatorPorAntiguedad();
        pruebaComparatorPorMasReciente();
        pruebaSoloActivos();
        pruebaFiltrarPorTipo();
        pruebaBusquedaCompuestaPremium();
        pruebaBusquedaCompuestaSinResultados();
        pruebaContarConIterador();
        pruebaPurgarInactivosConIteratorRemove();
        pruebaColaNotificacionesFIFO();
        pruebaProcesarColaVacia();
        pruebaConteoPorTipo();
        pruebaPesoPromedio();

        System.out.println("\n=== Resumen ===");
        System.out.println("Pasadas:  " + pasadas);
        System.out.println("Falladas: " + falladas);
        System.out.println("Total:    " + (pasadas + falladas));

        if (falladas > 0) System.exit(1);
    }

    // ---------- CRUD ----------

    private static void pruebaAgregarYBuscarPorId() {
        GestorClientes g = new GestorClientes();
        Cliente c = nuevoCliente(1);
        check(g.agregar(c), "agregar nuevo devuelve true", null);
        check(g.buscarPorId(1).isPresent(), "buscarPorId encuentra al recien agregado", null);
        check(g.total() == 1, "total despues de agregar = 1", g.total());
    }

    private static void pruebaIdDuplicadoRechazado() {
        GestorClientes g = new GestorClientes();
        g.agregar(nuevoCliente(1));
        Cliente otro = new Cliente(1, "Otro", "otro@x.mx",
                LocalDate.now(), 0, Cliente.TipoMembresia.BASICA);
        check(!g.agregar(otro), "agregar con id duplicado devuelve false", null);
        check(g.total() == 1, "total no aumenta tras rechazar duplicado", g.total());
    }

    private static void pruebaEmailDuplicadoRechazado() {
        GestorClientes g = new GestorClientes();
        g.agregar(nuevoCliente(1));
        Cliente otro = new Cliente(2, "Distinto",
                "c1@correo.mx",     // mismo email
                LocalDate.now(), 0, Cliente.TipoMembresia.BASICA);
        check(!g.agregar(otro), "agregar con email duplicado devuelve false", null);
    }

    private static void pruebaBuscarPorNombreParcial() {
        GestorClientes g = new GestorClientes();
        g.agregar(new Cliente(1, "Ana Lopez Vargas", "ana@x.mx",
                LocalDate.now(), 0, Cliente.TipoMembresia.BASICA));
        g.agregar(new Cliente(2, "Anabel Rios", "anabel@x.mx",
                LocalDate.now(), 0, Cliente.TipoMembresia.BASICA));
        g.agregar(new Cliente(3, "Pedro Suarez", "pedro@x.mx",
                LocalDate.now(), 0, Cliente.TipoMembresia.BASICA));
        List<Cliente> r = g.buscarPorNombre("ana");
        check(r.size() == 2, "busqueda parcial encuentra 'Ana' y 'Anabel'", r.size());
    }

    private static void pruebaEliminarLimpiaLasTresEstructuras() {
        GestorClientes g = new GestorClientes();
        g.agregar(nuevoCliente(1));
        boolean ok = g.eliminarPorId(1);
        check(ok, "eliminarPorId devuelve true cuando existe", null);
        check(g.total() == 0, "lista queda vacia", g.total());
        check(g.buscarPorId(1).isEmpty(), "hashmap sale del indice", null);
        check(!g.emailYaRegistrado("c1@correo.mx"),
                "email queda liberado en hashset", null);

        // Y se puede volver a registrar con el mismo email
        check(g.agregar(nuevoCliente(1)),
                "se puede re-agregar el mismo cliente despues de eliminar", null);
    }

    private static void pruebaCambiarEstado() {
        GestorClientes g = new GestorClientes();
        g.agregar(nuevoCliente(1));
        g.cambiarEstado(1, false);
        check(!g.buscarPorId(1).get().esActivo(),
                "cambiarEstado a false desactiva el cliente", null);
    }

    // ---------- Ordenamiento ----------

    private static void pruebaOrdenNaturalPorNombre() {
        GestorClientes g = new GestorClientes();
        g.agregar(new Cliente(1, "Carlos Ruiz", "c@x.mx",
                LocalDate.now(), 0, Cliente.TipoMembresia.BASICA));
        g.agregar(new Cliente(2, "Andres Lopez", "a@x.mx",
                LocalDate.now(), 0, Cliente.TipoMembresia.BASICA));
        g.agregar(new Cliente(3, "Beatriz Mora", "b@x.mx",
                LocalDate.now(), 0, Cliente.TipoMembresia.BASICA));
        List<Cliente> r = g.ordenadosPorNombre();
        check(r.get(0).getNombreCompleto().equals("Andres Lopez")
                && r.get(2).getNombreCompleto().equals("Carlos Ruiz"),
                "orden natural por nombre A-Z", null);
    }

    private static void pruebaComparatorPorAntiguedad() {
        GestorClientes g = new GestorClientes();
        g.agregar(new Cliente(1, "X", "x1@x.mx", LocalDate.of(2024, 1, 1), 0, Cliente.TipoMembresia.BASICA));
        g.agregar(new Cliente(2, "Y", "x2@x.mx", LocalDate.of(2023, 1, 1), 0, Cliente.TipoMembresia.BASICA));
        g.agregar(new Cliente(3, "Z", "x3@x.mx", LocalDate.of(2025, 1, 1), 0, Cliente.TipoMembresia.BASICA));
        List<Cliente> r = g.ordenadosPorAntiguedad();
        check(r.get(0).getId() == 2 && r.get(2).getId() == 3,
                "Comparator por antiguedad: el mas viejo primero", null);
    }

    private static void pruebaComparatorPorMasReciente() {
        GestorClientes g = new GestorClientes();
        g.agregar(new Cliente(1, "X", "x1@x.mx", LocalDate.of(2024, 1, 1), 0, Cliente.TipoMembresia.BASICA));
        g.agregar(new Cliente(2, "Y", "x2@x.mx", LocalDate.of(2023, 1, 1), 0, Cliente.TipoMembresia.BASICA));
        g.agregar(new Cliente(3, "Z", "x3@x.mx", LocalDate.of(2025, 1, 1), 0, Cliente.TipoMembresia.BASICA));
        List<Cliente> r = g.ordenadosPorMasReciente();
        check(r.get(0).getId() == 3 && r.get(2).getId() == 2,
                "Comparator por mas reciente: el mas nuevo primero", null);
    }

    // ---------- Streams ----------

    private static void pruebaSoloActivos() {
        GestorClientes g = new GestorClientes();
        g.agregar(nuevoCliente(1));
        g.agregar(nuevoCliente(2));
        g.agregar(nuevoCliente(3));
        g.cambiarEstado(2, false);
        check(g.soloActivos().size() == 2,
                "soloActivos filtra correctamente con Stream", null);
    }

    private static void pruebaFiltrarPorTipo() {
        GestorClientes g = new GestorClientes();
        DatosPrueba.cargar(g);
        long premium = g.filtrarPorTipoMembresia(Cliente.TipoMembresia.PREMIUM).size();
        check(premium > 0,
                "filtrarPorTipoMembresia(PREMIUM) devuelve resultados", premium);
    }

    private static void pruebaBusquedaCompuestaPremium() {
        GestorClientes g = new GestorClientes();
        DatosPrueba.cargar(g);
        // Filtro: activos + Premium/VIP + despues de 2025-01-01, top 5
        List<Cliente> r = g.nuevosPremiumDesde(LocalDate.of(2025, 1, 1), 5);

        // Validamos las propiedades de la consulta compuesta
        boolean todosCumplen = r.stream().allMatch(c ->
                c.esActivo()
                && (c.getTipoMembresia() == Cliente.TipoMembresia.PREMIUM
                 || c.getTipoMembresia() == Cliente.TipoMembresia.VIP)
                && c.getFechaRegistro().isAfter(LocalDate.of(2025, 1, 1)));
        check(todosCumplen, "Busqueda compuesta: todos cumplen los 3 filtros", r.size());
        check(r.size() <= 5, "Busqueda compuesta respeta limit(5)", r.size());

        // Y estan ordenados por mas reciente
        boolean ordenados = true;
        for (int i = 1; i < r.size(); i++) {
            if (r.get(i - 1).getFechaRegistro().isBefore(r.get(i).getFechaRegistro())) {
                ordenados = false;
                break;
            }
        }
        check(ordenados, "Busqueda compuesta ordenados por mas reciente", null);
    }

    private static void pruebaBusquedaCompuestaSinResultados() {
        GestorClientes g = new GestorClientes();
        DatosPrueba.cargar(g);
        // Fecha del futuro, nadie cumple
        List<Cliente> r = g.nuevosPremiumDesde(LocalDate.of(2030, 1, 1), 10);
        check(r.isEmpty(), "Busqueda compuesta con fecha futura devuelve vacio", r.size());
    }

    // ---------- Iteradores ----------

    private static void pruebaContarConIterador() {
        GestorClientes g = new GestorClientes();
        DatosPrueba.cargar(g);
        int activos = g.contarConIterador(Cliente::esActivo);
        check(activos == g.soloActivos().size(),
                "contarConIterador da el mismo resultado que stream", activos);
    }

    private static void pruebaPurgarInactivosConIteratorRemove() {
        GestorClientes g = new GestorClientes();
        DatosPrueba.cargar(g);
        int inactivos = (int) g.todos().stream().filter(c -> !c.esActivo()).count();
        int eliminados = g.purgarInactivos();
        check(eliminados == inactivos,
                "purgarInactivos elimina exactamente los inactivos", eliminados);
        check(g.soloActivos().size() == g.total(),
                "despues de purgar, todos los restantes son activos", null);
    }

    // ---------- Cola FIFO de notificaciones ----------

    private static void pruebaColaNotificacionesFIFO() {
        GestorClientes g = new GestorClientes();
        g.encolarNotificacion(new Notificacion(1, "A", Notificacion.Canal.EMAIL));
        g.encolarNotificacion(new Notificacion(2, "B", Notificacion.Canal.SMS));
        g.encolarNotificacion(new Notificacion(3, "C", Notificacion.Canal.EMAIL));

        Optional<Notificacion> primera = g.procesarSiguienteNotificacion();
        Optional<Notificacion> segunda = g.procesarSiguienteNotificacion();
        check(primera.isPresent() && primera.get().getMensaje().equals("A"),
                "FIFO: primera salida es la primera entrada (A)", null);
        check(segunda.isPresent() && segunda.get().getMensaje().equals("B"),
                "FIFO: segunda salida es la segunda entrada (B)", null);
        check(g.notificacionesPendientes() == 1,
                "Quedan pendientes solo las no procesadas", g.notificacionesPendientes());
    }

    private static void pruebaProcesarColaVacia() {
        GestorClientes g = new GestorClientes();
        check(g.procesarSiguienteNotificacion().isEmpty(),
                "Cola vacia devuelve Optional.empty()", null);
    }

    // ---------- Estadisticas ----------

    private static void pruebaConteoPorTipo() {
        GestorClientes g = new GestorClientes();
        DatosPrueba.cargar(g);
        var conteo = g.conteoPorTipo();
        long total = conteo.values().stream().mapToLong(Long::longValue).sum();
        check(total == g.total(),
                "Suma de conteo por tipo = total", total);
    }

    private static void pruebaPesoPromedio() {
        GestorClientes g = new GestorClientes();
        DatosPrueba.cargar(g);
        double prom = g.pesoPromedioConRegistro();
        check(prom > 30 && prom < 300,
                "Peso promedio en rango plausible", prom);
    }

    // ---------- helpers ----------

    private static Cliente nuevoCliente(int id) {
        return new Cliente(id, "Cliente " + id, "c" + id + "@correo.mx",
                LocalDate.now().minusMonths(id), 70, Cliente.TipoMembresia.BASICA);
    }

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
