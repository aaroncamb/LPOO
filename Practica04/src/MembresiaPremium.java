import java.time.LocalDate;

/**
 * Práctica 4 — Membresia Premium (concreta).
 *
 * Acceso 24/7 al gimnasio, clases grupales incluidas (yoga, spinning,
 * zumba), 1 sesion mensual con nutriologo. Renovar antes del vencimiento
 * da 5% de descuento sobre el siguiente mes (politica de fidelizacion).
 */
public class MembresiaPremium extends Estandar {

    public static final double PRECIO_BASE = 650.0;
    private static final double DESCUENTO = 0.05;

    private int clasesGrupalesAgendadasEsteMes;

    public MembresiaPremium(String titularNombre, LocalDate fechaInicio) {
        super(titularNombre, fechaInicio, PRECIO_BASE);
        this.clasesGrupalesAgendadasEsteMes = 0;
    }

    /**
     * Si esta vigente (no morosa), aplica 5% de descuento al siguiente
     * cargo. La idea: incentivar a renovar antes del vencimiento.
     */
    @Override
    public double calcularPrecio() {
        if (estaVigente()) {
            return precioMensual * (1.0 - DESCUENTO);
        }
        return precioMensual;
    }

    @Override
    public String beneficiosIncluidos() {
        return "Acceso 24/7, clases grupales ilimitadas, 1 sesion mensual con nutriologo.";
    }

    @Override
    public double descuentoRenovacion() {
        return DESCUENTO;
    }

    @Override
    public String tipoLegible() {
        return "Membresia Premium";
    }

    // -------- Funcionalidad propia de Premium --------

    public void agendarClaseGrupal() {
        clasesGrupalesAgendadasEsteMes++;
    }

    public int getClasesGrupalesAgendadasEsteMes() {
        return clasesGrupalesAgendadasEsteMes;
    }
}
