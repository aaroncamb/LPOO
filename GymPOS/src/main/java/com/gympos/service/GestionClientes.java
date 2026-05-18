package com.gympos.service;

import com.gympos.exceptions.EntradaInvalidaException;
import com.gympos.model.Cliente;
import com.gympos.persistence.GestorArchivos;
import com.gympos.util.Loggers;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * GymPOS - Modulo de gestion de clientes.
 *
 * Reutiliza el diseño de P8: combina 4 estructuras de coleccion
 * sincronizadas:
 *   - ArrayList<Cliente>          : listado principal en orden de insercion
 *   - HashMap<Integer, Cliente>   : indice O(1) para lookup por id
 *   - HashSet<String>             : emails unicos
 *   - Comparators reutilizables   : ordenamientos
 *
 * Persiste a binario serializado a traves de GestorArchivos.
 * Las operaciones CRUD lanzan EntradaInvalidaException cuando reciben
 * datos invalidos (bug del programador).
 */
public class GestionClientes {

    private static final String ARCHIVO_DATOS = "data/clientes.dat";

    private final List<Cliente> clientes = new ArrayList<>();
    private final Map<Integer, Cliente> indicePorId = new HashMap<>();
    private final Set<String> emailsRegistrados = new HashSet<>();
    private final GestorArchivos archivos;

    public static final Comparator<Cliente> POR_NOMBRE =
            Comparator.comparing(Cliente::getNombreCompleto, String.CASE_INSENSITIVE_ORDER);
    public static final Comparator<Cliente> POR_ANTIGUEDAD =
            Comparator.comparing(Cliente::getFechaRegistro);
    public static final Comparator<Cliente> POR_PUNTOS_DESC =
            Comparator.comparingInt(Cliente::getPuntos).reversed();

    public GestionClientes(GestorArchivos archivos) {
        this.archivos = archivos;
    }

    // ============================================================
    //   CARGA / PERSISTENCIA
    // ============================================================

    /** Carga clientes desde el archivo binario al iniciar. */
    public void cargarDesdeDisco() throws IOException, ClassNotFoundException {
        List<Cliente> cargados = archivos.cargarLista(ARCHIVO_DATOS);
        for (Cliente c : cargados) {
            agregarSinValidacion(c);
        }
        Loggers.info("GestionClientes: " + clientes.size() + " clientes cargados.");
    }

    /** Guarda el estado actual al archivo binario. */
    public void guardarEnDisco() throws IOException {
        archivos.guardarLista(ARCHIVO_DATOS, clientes);
    }

    /** Inserta sin verificar duplicados (uso interno al cargar). */
    private void agregarSinValidacion(Cliente c) {
        clientes.add(c);
        indicePorId.put(c.getId(), c);
        emailsRegistrados.add(c.getEmail().toLowerCase());
    }

    // ============================================================
    //   CRUD
    // ============================================================

    /**
     * Agrega un cliente nuevo. Devuelve true si se agrego, false si
     * habia conflicto (id o email duplicado).
     */
    public boolean agregar(Cliente c) {
        if (c == null) throw new EntradaInvalidaException("cliente", null, "cliente no puede ser null");

        if (indicePorId.containsKey(c.getId())) {
            Loggers.warn("agregar: id " + c.getId() + " ya existe");
            return false;
        }
        String emailLower = c.getEmail().toLowerCase();
        if (!emailsRegistrados.add(emailLower)) {
            Loggers.warn("agregar: email " + c.getEmail() + " ya existe");
            return false;
        }

        clientes.add(c);
        indicePorId.put(c.getId(), c);
        Loggers.info("Cliente agregado: " + c);
        return true;
    }

    public Optional<Cliente> buscarPorId(int id) {
        return Optional.ofNullable(indicePorId.get(id));
    }

    public List<Cliente> buscarPorNombreOEmail(String texto) {
        if (texto == null || texto.isBlank()) return new ArrayList<>(clientes);
        String aguja = texto.trim().toLowerCase();
        return clientes.stream()
                .filter(c -> c.getNombreCompleto().toLowerCase().contains(aguja)
                          || c.getEmail().toLowerCase().contains(aguja))
                .collect(Collectors.toList());
    }

    /**
     * Elimina por id, manteniendo sincronizadas las 3 estructuras.
     */
    public boolean eliminarPorId(int id) {
        Cliente c = indicePorId.remove(id);
        if (c == null) return false;
        clientes.remove(c);
        emailsRegistrados.remove(c.getEmail().toLowerCase());
        Loggers.info("Cliente eliminado: " + c);
        return true;
    }

    public boolean actualizar(Cliente c) {
        Cliente existente = indicePorId.get(c.getId());
        if (existente == null) return false;
        // Actualizamos los campos sobre el mismo objeto para que la UI
        // (suscrita a las Properties) detecte los cambios.
        existente.setNombreCompleto(c.getNombreCompleto());

        // Si cambio el email, mantener sincronizado el set
        String emailViejo = existente.getEmail().toLowerCase();
        String emailNuevo = c.getEmail().toLowerCase();
        if (!emailViejo.equals(emailNuevo)) {
            emailsRegistrados.remove(emailViejo);
            emailsRegistrados.add(emailNuevo);
            existente.setEmail(c.getEmail());
        }
        existente.setFechaRegistro(c.getFechaRegistro());
        existente.setPesoKg(c.getPesoKg());
        existente.setTipoMembresia(c.getTipoMembresia());
        Loggers.info("Cliente actualizado: " + existente);
        return true;
    }

    // ============================================================
    //   LISTADOS Y FILTROS
    // ============================================================

    public List<Cliente> todos() {
        return new ArrayList<>(clientes);
    }

    public int total() { return clientes.size(); }

    public List<Cliente> ordenadosPor(Comparator<Cliente> orden) {
        List<Cliente> copia = new ArrayList<>(clientes);
        copia.sort(orden);
        return copia;
    }

    public List<Cliente> filtrarActivos() {
        return clientes.stream()
                .filter(Cliente::isActivo)
                .collect(Collectors.toList());
    }

    public List<Cliente> filtrarPorTipo(Cliente.TipoMembresia tipo) {
        return clientes.stream()
                .filter(c -> c.getTipoMembresia() == tipo)
                .collect(Collectors.toList());
    }

    /**
     * Cliente con membresia premium/VIP activos registrados despues de
     * la fecha. Es la busqueda compuesta de P8 traida al sistema.
     */
    public List<Cliente> nuevosPremiumDesde(LocalDate fechaCorte, int limite) {
        return clientes.stream()
                .filter(Cliente::isActivo)
                .filter(c -> c.getTipoMembresia() == Cliente.TipoMembresia.PREMIUM
                          || c.getTipoMembresia() == Cliente.TipoMembresia.VIP)
                .filter(c -> c.getFechaRegistro().isAfter(fechaCorte))
                .sorted(POR_ANTIGUEDAD.reversed())
                .limit(limite)
                .collect(Collectors.toList());
    }

    /** Conteo por tipo de membresia para reportes. */
    public Map<Cliente.TipoMembresia, Long> conteoPorTipo() {
        return clientes.stream()
                .collect(Collectors.groupingBy(
                        Cliente::getTipoMembresia,
                        Collectors.counting()));
    }
}
