import java.time.LocalDate;

/**
 * Práctica 4 — Clase raíz de la jerarquía de membresías.
 *
 * Membresia es ABSTRACTA: no tiene sentido instanciar "una membresía
 * genérica" sin tipo. Las clases concretas son MembresiaBasica,
 * MembresiaPremium (vía Estandar) y MembresiaVIP (directa).
 *
 * Jerarquía resultante (3 niveles):
 *   Membresia                                 (nivel 1, abstracta)
 *     ├── Estandar                            (nivel 2, abstracta)
 *     │     ├── MembresiaBasica               (nivel 3, concreta)
 *     │     └── MembresiaPremium              (nivel 3, concreta)
 *     └── MembresiaVIP                        (nivel 2, concreta)
 *
 * Atributos protected: la idea es que las subclases puedan modificar
 * el estado directamente sin pasar por getters, ya que son "familia".
 */
public abstract class Membresia {

    protected String    titularNombre;     // a quien pertenece la membresia
    protected LocalDate fechaInicio;
    protected LocalDate fechaFin;
    protected boolean   activa;

    public Membresia(String titularNombre, LocalDate fechaInicio) {
        if (titularNombre == null || titularNombre.isBlank()) {
            throw new IllegalArgumentException("El titular no puede estar vacio.");
        }
        if (fechaInicio == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria.");
        }
        this.titularNombre = titularNombre.trim();
        this.fechaInicio   = fechaInicio;
        this.activa        = true;
        // fechaFin la calcula cada subclase segun su periodicidad
    }

    // -------- Metodos abstractos: cada hija los implementa --------

    /** Precio total a cobrar (mensual en Estandar, anual en VIP). */
    public abstract double calcularPrecio();

    /** Descripcion de beneficios incluidos en este nivel. */
    public abstract String beneficiosIncluidos();

    /**
     * Renueva la membresia. Cada hija decide cuantos dias suma
     * (30 dias en Estandar, 365 en VIP).
     */
    public abstract void renovar();

    /**
     * Porcentaje de descuento aplicado al renovar antes de vencer.
     * Cero en Basica, 5% en Premium, 10% en VIP.
     */
    public abstract double descuentoRenovacion();

    // -------- Metodos concretos compartidos --------

    public boolean estaVigente() {
        if (!activa || fechaInicio == null || fechaFin == null) return false;
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(fechaInicio) && !hoy.isAfter(fechaFin);
    }

    public void cancelar() {
        this.activa = false;
    }

    // -------- Getters --------

    public String getTitularNombre()   { return titularNombre; }
    public LocalDate getFechaInicio()  { return fechaInicio; }
    public LocalDate getFechaFin()     { return fechaFin; }
    public boolean isActiva()          { return activa; }

    /** Nombre legible del tipo (para impresion). */
    public abstract String tipoLegible();

    @Override
    public String toString() {
        return String.format("%s de %s [%s a %s, %s]",
                tipoLegible(), titularNombre,
                fechaInicio, fechaFin,
                estaVigente() ? "vigente" : "no vigente");
    }
}
