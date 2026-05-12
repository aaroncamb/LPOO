import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Práctica 4 — Clase contenedora de la jerarquia.
 *
 * Gestiona una lista de Membresia (de cualquier subtipo concreto).
 * Aqui es donde el polimorfismo brilla: el contenedor no sabe ni le
 * importa si una membresia es Basica, Premium o VIP. Trata a todas
 * por la interfaz comun definida en Membresia.
 */
public class GestorMembresias {

    private final List<Membresia> membresias = new ArrayList<>();

    public void agregar(Membresia m) {
        if (m == null) {
            throw new IllegalArgumentException("No se puede agregar null.");
        }
        membresias.add(m);
    }

    public int total() {
        return membresias.size();
    }

    public Optional<Membresia> buscarPorTitular(String nombre) {
        for (Membresia m : membresias) {
            if (m.getTitularNombre().equalsIgnoreCase(nombre.trim())) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    /**
     * Calcula los ingresos esperados si todas las membresias vigentes
     * facturaran HOY su proxima cuota. Suma cuotas anuales y mensuales
     * mezcladas, asi que el resultado es "lo que entraria si nadie
     * cancela este ciclo".
     */
    public double ingresosEsperados() {
        double total = 0;
        for (Membresia m : membresias) {
            if (m.estaVigente()) {
                total += m.calcularPrecio();   // polimorfismo: cada subclase decide
            }
        }
        return total;
    }

    /**
     * Renueva TODAS las membresias en bloque. Polimorfico: Basica/Premium
     * suman 30 dias, VIP suma 365, todo con la misma linea m.renovar().
     */
    public void renovarTodas() {
        for (Membresia m : membresias) {
            m.renovar();
        }
    }

    /** Lista solo las membresias del tipo solicitado. */
    public List<Membresia> filtrarPorTipo(Class<? extends Membresia> tipo) {
        List<Membresia> resultado = new ArrayList<>();
        for (Membresia m : membresias) {
            if (tipo.isInstance(m)) {
                resultado.add(m);
            }
        }
        return resultado;
    }

    public List<Membresia> todas() {
        return new ArrayList<>(membresias);   // copia defensiva
    }

    public void imprimirReporte() {
        if (membresias.isEmpty()) {
            System.out.println("(sin membresias registradas)");
            return;
        }
        System.out.println("--- Reporte de membresias (" + membresias.size() + ") ---");
        for (Membresia m : membresias) {
            System.out.println("  " + m);
            System.out.println("    Cuota a cobrar: $" + String.format("%.2f", m.calcularPrecio()));
            System.out.println("    Beneficios: " + m.beneficiosIncluidos());
        }
    }
}
