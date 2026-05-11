import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Práctica 2 — Clase contenedora.
 *
 * Gestiona la colección de clientes registrados en el gimnasio.
 *
 * En P3 esta clase se va a robustecer (validaciones al agregar, manejo
 * de excepciones para duplicados, etc.). Aquí mantenemos el alcance que
 * pide P2: agregar, buscar y mostrar.
 */
public class GestorClientes {

    private final List<Cliente> clientes;

    public GestorClientes() {
        this.clientes = new ArrayList<>();
    }

    /**
     * Agrega un cliente a la colección.
     * Devuelve true si se agregó, false si el id ya existía.
     * Esta detección de duplicados es una decisión propia: la consigna
     * solo pide "agregar", pero permitir IDs duplicados rompería todo
     * lo que viene en prácticas siguientes (búsquedas, equals, etc.).
     */
    public boolean agregar(Cliente cliente) {
        if (cliente == null) {
            return false;
        }
        if (buscarPorId(cliente.id).isPresent()) {
            return false;
        }
        clientes.add(cliente);
        return true;
    }

    /**
     * Busca un cliente por su id exacto.
     * Devuelve Optional para que el llamador maneje explícitamente el
     * caso "no encontrado" sin caer en NullPointerException.
     */
    public Optional<Cliente> buscarPorId(int id) {
        for (Cliente c : clientes) {
            if (c.id == id) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    /**
     * Búsqueda parcial por nombre, sin distinguir mayúsculas.
     * Devuelve todos los clientes cuyo nombre contiene el texto buscado.
     * Útil cuando el recepcionista solo recuerda parte del nombre.
     */
    public List<Cliente> buscarPorNombre(String texto) {
        List<Cliente> resultado = new ArrayList<>();
        if (texto == null || texto.isBlank()) {
            return resultado;
        }
        String aguja = texto.trim().toLowerCase();
        for (Cliente c : clientes) {
            if (c.nombreCompleto != null
                    && c.nombreCompleto.toLowerCase().contains(aguja)) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    /**
     * Imprime todos los clientes registrados, uno por línea.
     */
    public void mostrarTodos() {
        if (clientes.isEmpty()) {
            System.out.println("(no hay clientes registrados)");
            return;
        }
        System.out.println("--- Clientes registrados (" + clientes.size() + ") ---");
        for (Cliente c : clientes) {
            System.out.println("  " + c);
        }
    }

    public int totalClientes() {
        return clientes.size();
    }
}
