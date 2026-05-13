import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Práctica 8 — Gestor de clientes del gimnasio.
 *
 * USA 4 ESTRUCTURAS DE COLECCION DISTINTAS, cada una para una
 * responsabilidad concreta:
 *
 *   1. ArrayList<Cliente>          - listado principal en orden de insercion.
 *   2. HashMap<Integer, Cliente>   - lookup O(1) por id.
 *   3. HashSet<String>             - emails unicos (rechaza duplicados).
 *   4. LinkedList<Notificacion>    - cola FIFO de notificaciones pendientes.
 *
 * La justificacion completa de cada eleccion vive en el README.md.
 *
 * Implementa CRUD completo (Create, Read, Update, Delete), busqueda con
 * Streams, una busqueda compuesta como Elemento de Decision Propia, y
 * dos Comparators ademas del orden natural de Cliente.
 */
public class GestorClientes {

    // ============================================================
    //   LAS 4 ESTRUCTURAS DE COLECCION
    // ============================================================

    /** Listado principal, en orden de insercion. ArrayList = O(1) por indice. */
    private final List<Cliente> clientes = new ArrayList<>();

    /** Indice por id para busqueda O(1) en lugar de O(n) recorriendo la lista. */
    private final Map<Integer, Cliente> indicePorId = new HashMap<>();

    /** Emails ya usados. add() devuelve false si existia, ideal para validar unicidad. */
    private final Set<String> emailsRegistrados = new HashSet<>();

    /** Cola FIFO de notificaciones pendientes (offer al final, poll al frente). */
    private final LinkedList<Notificacion> notificacionesPendientes = new LinkedList<>();

    // ============================================================
    //   COMPARATORS REUTILIZABLES (Entregable 3)
    // ============================================================

    /** Por antiguedad (mas viejo primero, es decir, fecha de registro mas antigua). */
    public static final Comparator<Cliente> POR_ANTIGUEDAD =
            Comparator.comparing(Cliente::getFechaRegistro);

    /**
     * Por antiguedad descendente (mas reciente primero).
     * Util para listas "ultimas inscripciones".
     */
    public static final Comparator<Cliente> POR_INSCRIPCION_RECIENTE =
            POR_ANTIGUEDAD.reversed();

    // ============================================================
    //   CRUD - Create
    // ============================================================

    /**
     * Agrega un cliente. Devuelve true si se agrego, false si ya
     * existia un cliente con ese id o con ese email.
     */
    public boolean agregar(Cliente c) {
        if (c == null) return false;
        if (indicePorId.containsKey(c.getId())) return false;
        if (!emailsRegistrados.add(c.getEmail())) return false;  // ya estaba

        clientes.add(c);
        indicePorId.put(c.getId(), c);
        return true;
    }

    // ============================================================
    //   CRUD - Read
    // ============================================================

    /** Busqueda O(1) por id usando el HashMap. */
    public Optional<Cliente> buscarPorId(int id) {
        return Optional.ofNullable(indicePorId.get(id));
    }

    /**
     * Busqueda parcial por substring del nombre, insensible a case.
     * Devuelve TODOS los que coincidan. Implementacion con Streams.
     */
    public List<Cliente> buscarPorNombre(String texto) {
        if (texto == null || texto.isBlank()) return Collections.emptyList();
        String aguja = texto.trim().toLowerCase();
        return clientes.stream()
                .filter(c -> c.getNombreCompleto().toLowerCase().contains(aguja))
                .collect(Collectors.toList());
    }

    /** Listado inmutable (copia defensiva). */
    public List<Cliente> todos() {
        return new ArrayList<>(clientes);
    }

    public int total() {
        return clientes.size();
    }

    public boolean emailYaRegistrado(String email) {
        if (email == null) return false;
        return emailsRegistrados.contains(email.trim().toLowerCase());
    }

    // ============================================================
    //   CRUD - Update
    // ============================================================

    /**
     * Cambia el estado activo/inactivo de un cliente.
     * Devuelve true si se encontro y aplico, false si no existe.
     */
    public boolean cambiarEstado(int id, boolean activo) {
        Cliente c = indicePorId.get(id);
        if (c == null) return false;
        if (activo) c.reactivar();
        else        c.desactivar();
        return true;
    }

    // ============================================================
    //   CRUD - Delete
    // ============================================================

    /**
     * Elimina por id. Hay que actualizar las TRES estructuras que
     * referencian al cliente (lista, mapa, set) para que queden
     * consistentes.
     */
    public boolean eliminarPorId(int id) {
        Cliente c = indicePorId.remove(id);   // sale del mapa
        if (c == null) return false;
        clientes.remove(c);                    // sale de la lista
        emailsRegistrados.remove(c.getEmail()); // libera el email
        return true;
    }

    // ============================================================
    //   ORDENAMIENTO - usando Comparable (natural) y Comparators
    // ============================================================

    /** Devuelve copia ordenada por el orden natural de Cliente (nombre). */
    public List<Cliente> ordenadosPorNombre() {
        List<Cliente> copia = new ArrayList<>(clientes);
        Collections.sort(copia);   // usa Comparable
        return copia;
    }

    /** Devuelve copia ordenada por antiguedad (mas viejo primero). */
    public List<Cliente> ordenadosPorAntiguedad() {
        List<Cliente> copia = new ArrayList<>(clientes);
        copia.sort(POR_ANTIGUEDAD);
        return copia;
    }

    /** Devuelve copia ordenada por inscripcion mas reciente. */
    public List<Cliente> ordenadosPorMasReciente() {
        List<Cliente> copia = new ArrayList<>(clientes);
        copia.sort(POR_INSCRIPCION_RECIENTE);
        return copia;
    }

    // ============================================================
    //   STREAMS - filtrado simple
    // ============================================================

    public List<Cliente> soloActivos() {
        return clientes.stream()
                .filter(Cliente::esActivo)
                .collect(Collectors.toList());
    }

    public List<Cliente> filtrarPorTipoMembresia(Cliente.TipoMembresia tipo) {
        return clientes.stream()
                .filter(c -> c.getTipoMembresia() == tipo)
                .collect(Collectors.toList());
    }

    // ============================================================
    //   ELEMENTO DE DECISION PROPIA - busqueda compuesta con Streams
    // ============================================================

    /**
     * Consulta compuesta: clientes ACTIVOS con membresia PREMIUM o VIP
     * registrados DESPUES de la fecha dada, ordenados por antiguedad
     * descendente (mas recientes primero), tomando solo los primeros N.
     *
     * Es una consulta del mundo real del gimnasio: "dame los nuevos
     * clientes premium del ultimo trimestre, para enviarles una campaña
     * de fidelidad personalizada". Cumple 4 criterios simultaneos:
     *   - filtro por estado (activo)
     *   - filtro por tipo (Premium o VIP)
     *   - filtro por fecha (despues de X)
     *   - orden + limite (los primeros N por inscripcion reciente)
     *
     * Documentada en el README.
     */
    public List<Cliente> nuevosPremiumDesde(LocalDate fechaCorte, int limite) {
        if (fechaCorte == null) return Collections.emptyList();
        if (limite < 0)         return Collections.emptyList();

        return clientes.stream()
                .filter(Cliente::esActivo)
                .filter(c -> c.getTipoMembresia() == Cliente.TipoMembresia.PREMIUM
                          || c.getTipoMembresia() == Cliente.TipoMembresia.VIP)
                .filter(c -> c.getFechaRegistro().isAfter(fechaCorte))
                .sorted(POR_INSCRIPCION_RECIENTE)
                .limit(limite)
                .collect(Collectors.toList());
    }

    // ============================================================
    //   ITERADORES (Entregable 4 - "uso de iteradores")
    // ============================================================

    /**
     * Cuenta cuantos clientes cumplen un criterio iterando explicitamente.
     * Demuestra el uso del Iterator (lo que esta detras del for-each).
     */
    public int contarConIterador(java.util.function.Predicate<Cliente> criterio) {
        int count = 0;
        Iterator<Cliente> it = clientes.iterator();
        while (it.hasNext()) {
            if (criterio.test(it.next())) count++;
        }
        return count;
    }

    /**
     * Elimina con iterador.remove() todos los clientes inactivos. Esto
     * es lo CORRECTO: si haces `clientes.remove(c)` dentro de un
     * for-each estandar, lanza ConcurrentModificationException. El
     * iterator.remove() es la unica forma segura de modificar mientras
     * iteras.
     */
    public int purgarInactivos() {
        int eliminados = 0;
        Iterator<Cliente> it = clientes.iterator();
        while (it.hasNext()) {
            Cliente c = it.next();
            if (!c.esActivo()) {
                it.remove();
                indicePorId.remove(c.getId());
                emailsRegistrados.remove(c.getEmail());
                eliminados++;
            }
        }
        return eliminados;
    }

    // ============================================================
    //   COLA DE NOTIFICACIONES (LinkedList)
    // ============================================================

    /** Encolar una notificacion al final. */
    public void encolarNotificacion(Notificacion n) {
        notificacionesPendientes.offer(n);
    }

    /**
     * Procesar la siguiente notificacion (poll = sacar del frente).
     * Devuelve Optional.empty() si no hay nada pendiente.
     */
    public Optional<Notificacion> procesarSiguienteNotificacion() {
        return Optional.ofNullable(notificacionesPendientes.poll());
    }

    public int notificacionesPendientes() {
        return notificacionesPendientes.size();
    }

    // ============================================================
    //   ESTADISTICAS via reduce/agregaciones de stream
    // ============================================================

    public double pesoPromedioConRegistro() {
        return clientes.stream()
                .mapToDouble(Cliente::getPesoKg)
                .filter(p -> p > 0)
                .average()
                .orElse(0);
    }

    public Map<Cliente.TipoMembresia, Long> conteoPorTipo() {
        return clientes.stream()
                .collect(Collectors.groupingBy(
                        Cliente::getTipoMembresia,
                        Collectors.counting()));
    }
}
