import java.util.ArrayList;
import java.util.List;

/**
 * Práctica 5 — Procesador de pagos del gimnasio.
 *
 * Demuestra polimorfismo: opera sobre List<Cobrable>, sin saber ni
 * importarle el tipo concreto de cada servicio. La unica garantia que
 * necesita es que cada elemento sepa calcular su total (la interfaz
 * Cobrable).
 *
 * Tambien demuestra como funciona la separacion de responsabilidades:
 *   Servicio sabe COMO calcular su precio.
 *   CajaRegistradora sabe COMO COBRAR varios servicios juntos.
 */
public class CajaRegistradora {

    private final List<Cobrable> tickets = new ArrayList<>();
    private double totalIngresado = 0.0;

    public void agregarTicket(Cobrable cobrable) {
        if (cobrable == null) {
            throw new IllegalArgumentException("No se puede agregar null al ticket.");
        }
        tickets.add(cobrable);
    }

    /**
     * Calcula el total de todos los tickets activos en caja. Polimorfico:
     * cada Cobrable resuelve su calcularTotal() segun su tipo concreto.
     */
    public double totalEnCaja() {
        double total = 0;
        for (Cobrable c : tickets) {
            total += c.calcularTotal();
        }
        return total;
    }

    /**
     * Procesa todos los tickets en cola. Para los que son Servicio,
     * llama al Template Method procesarVenta(). Para Cobrables que no
     * sean Servicio (en caso de que se agreguen otros tipos en el futuro),
     * simplemente cobra el total.
     */
    public List<ResultadoVenta> cerrarCaja() {
        List<ResultadoVenta> resultados = new ArrayList<>();
        for (Cobrable c : tickets) {
            if (c instanceof Servicio s) {
                ResultadoVenta r = s.procesarVenta();
                resultados.add(r);
                if (r.isExitoso()) {
                    totalIngresado += r.getTotalCobrado();
                }
            } else {
                // Cobrable generico: cobramos sin protocolo de Servicio
                double total = c.calcularTotal();
                totalIngresado += total;
                resultados.add(new ResultadoVenta(true, total,
                        "Cobro directo de Cobrable"));
            }
        }
        tickets.clear();
        return resultados;
    }

    /** Aplica un descuento porcentual a TODOS los tickets actuales. */
    public void aplicarDescuentoGlobal(double porcentaje) {
        for (Cobrable c : tickets) {
            c.aplicarDescuento(porcentaje);
        }
    }

    /** Cuenta tickets de un tipo concreto. Usa instanceof + cast. */
    public int contarPorTipo(Class<? extends Servicio> tipo) {
        int n = 0;
        for (Cobrable c : tickets) {
            if (tipo.isInstance(c)) n++;
        }
        return n;
    }

    public int totalTickets() {
        return tickets.size();
    }

    public double getTotalIngresado() {
        return totalIngresado;
    }

    public void imprimirEstadoCaja() {
        System.out.println("--- Caja registradora ---");
        System.out.println("  Tickets activos: " + tickets.size());
        for (Cobrable c : tickets) {
            System.out.printf("    %s -> total $%.2f%n",
                    c.getClass().getSimpleName(), c.calcularTotal());
        }
        System.out.printf("  Total en caja: $%.2f%n", totalEnCaja());
        System.out.printf("  Total ingresado historico: $%.2f%n", totalIngresado);
    }
}
