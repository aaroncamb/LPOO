import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Práctica 6 — Clase gestora del centro de operaciones del gimnasio.
 *
 * Mantiene una coleccion de servicios y los procesa POR INTERFAZ.
 * Esto significa que las operaciones genericas (notificar a todos,
 * generar reporte, reagendar masivo) NO necesitan conocer los tipos
 * concretos: basta con preguntar "¿este servicio implementa X
 * interfaz?" y actuar en consecuencia.
 *
 * Esta gestora es distinta a CajaRegistradora de P5: aquella operaba
 * sobre Cobrable (interfaz financiera). Esta opera sobre las nuevas
 * interfaces de P6 (operacionales). Ambas conviven sin problema.
 */
public class CentroOperaciones {

    private final List<Servicio> servicios = new ArrayList<>();

    public void agregar(Servicio s) {
        if (s == null) throw new IllegalArgumentException("No se puede agregar null.");
        servicios.add(s);
    }

    public int total() { return servicios.size(); }

    public List<Servicio> todos() { return new ArrayList<>(servicios); }

    // ============================================================
    //  OPERACIONES POR INTERFAZ
    // ============================================================

    /**
     * Envia una notificacion a todos los servicios que implementen
     * Notificable. Los que no la implementen se ignoran silenciosamente.
     * Aqui se ve el polimorfismo basado en interfaces: ni siquiera
     * miramos el tipo concreto, solo "¿puede ser notificado?".
     */
    public int notificarTodos(String asunto, String mensaje) {
        int notificados = 0;
        for (Servicio s : servicios) {
            if (s instanceof Notificable n) {
                if (n.notificarMultiplesCanales(asunto, mensaje)) {
                    notificados++;
                }
            }
        }
        return notificados;
    }

    /**
     * Construye un reporte CSV con todos los servicios Reportable.
     * Los que no son Reportable simplemente no aparecen.
     */
    public String generarReporteCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append(Reportable.csvHeader()).append('\n');
        for (Servicio s : servicios) {
            if (s instanceof Reportable r) {
                sb.append(r.toCsvLine()).append('\n');
            }
        }
        return sb.toString();
    }

    /**
     * Reagenda en masa: a los servicios Reagendable les pide moverse N
     * dias adelante. Devuelve cuantos pudieron reagendarse exitosamente.
     */
    public int reagendarTodosNDias(int diasAdelante) {
        int reagendados = 0;
        LocalDate nuevaFechaBase = LocalDate.now().plusDays(diasAdelante);
        for (Servicio s : servicios) {
            if (s instanceof Reagendable r) {
                if (r.reagendar(nuevaFechaBase)) {
                    reagendados++;
                }
            }
        }
        return reagendados;
    }

    /**
     * Listado de servicios filtrados por interfaz.
     * El parametro es la Class de la interfaz: Notificable.class, etc.
     */
    public <T> List<Servicio> filtrarPorInterfaz(Class<T> interfaz) {
        List<Servicio> resultado = new ArrayList<>();
        for (Servicio s : servicios) {
            if (interfaz.isInstance(s)) {
                resultado.add(s);
            }
        }
        return resultado;
    }

    /** Totaliza el monto facturado (solo los Reportable). */
    public double ingresoTotalReportable() {
        double total = 0;
        for (Servicio s : servicios) {
            if (s instanceof Reportable r) {
                total += r.montoFacturado();
            }
        }
        return total;
    }

    public void imprimirInventario() {
        System.out.println("--- Inventario de servicios (" + servicios.size() + ") ---");
        for (Servicio s : servicios) {
            String banderas = "";
            if (s instanceof Notificable) banderas += "N";
            if (s instanceof Reportable)  banderas += "R";
            if (s instanceof Reagendable) banderas += "A";
            System.out.printf("  [%s] %s%n", banderas, s.resumen());
        }
        System.out.println("Banderas: N=Notificable, R=Reportable, A=Reagendable");
    }
}
