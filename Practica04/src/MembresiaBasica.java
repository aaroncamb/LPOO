import java.time.LocalDate;

/**
 * Práctica 4 — Membresia Basica (concreta).
 *
 * Nivel mas economico. Acceso al area de pesas y cardio en horario
 * limitado. Sin clases grupales, sin entrenador, sin descuento al
 * renovar (la idea de negocio es que Basica sea precio "ancla", la
 * fidelizacion la dan los planes superiores).
 */
public class MembresiaBasica extends Estandar {

    /** Precio base de Basica segun catalogo del gimnasio. */
    public static final double PRECIO_BASE = 350.0;

    public MembresiaBasica(String titularNombre, LocalDate fechaInicio) {
        super(titularNombre, fechaInicio, PRECIO_BASE);
    }

    @Override
    public double calcularPrecio() {
        // Precio plano: lo que diga el catalogo, sin extras.
        return precioMensual;
    }

    @Override
    public String beneficiosIncluidos() {
        return "Acceso a pesas y cardio en horario 6:00-22:00. Sin clases grupales.";
    }

    @Override
    public double descuentoRenovacion() {
        return 0.0;   // Basica no tiene descuento por renovar
    }

    @Override
    public String tipoLegible() {
        return "Membresia Basica";
    }
}
