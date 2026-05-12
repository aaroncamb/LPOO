import java.time.LocalDate;

/**
 * Práctica 4 — Clase intermedia.
 *
 * Estandar representa el modelo de "membresia mensual con cobro fijo",
 * que es lo que comparten Basica y Premium. Es ABSTRACTA porque por si
 * misma no tiene sentido: "una membresia estandar" todavia no dice si
 * es Basica o Premium.
 *
 * Lo que esta clase aporta a sus hijas:
 *   - Atributo precioMensual (no esta en Membresia porque VIP no
 *     factura por mes, sino por año).
 *   - Implementacion de renovar() que suma 30 dias, compartida.
 *   - Constructor que calcula fechaFin = fechaInicio + 30 dias.
 *
 * Lo que NO implementa (lo dejan las hijas concretas):
 *   - calcularPrecio()        - cada nivel tiene precio distinto
 *   - beneficiosIncluidos()   - cada nivel tiene su lista
 *   - descuentoRenovacion()   - Basica 0%, Premium 5%
 *   - tipoLegible()           - "Basica" / "Premium"
 */
public abstract class Estandar extends Membresia {

    protected double precioMensual;

    public Estandar(String titularNombre, LocalDate fechaInicio, double precioMensual) {
        super(titularNombre, fechaInicio);
        if (precioMensual < 0) {
            throw new IllegalArgumentException("Precio mensual no puede ser negativo.");
        }
        this.precioMensual = precioMensual;
        this.fechaFin      = fechaInicio.plusDays(30);
    }

    /**
     * En Estandar, renovar significa sumar 30 dias mas.
     * Si la membresia ya estaba vencida, se reactiva.
     */
    @Override
    public void renovar() {
        LocalDate base = (fechaFin.isAfter(LocalDate.now())) ? fechaFin : LocalDate.now();
        this.fechaFin = base.plusDays(30);
        this.activa   = true;
    }

    public double getPrecioMensual() {
        return precioMensual;
    }
}
