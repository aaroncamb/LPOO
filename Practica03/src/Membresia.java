import java.time.LocalDate;

/**
 * Práctica 3 — Segunda clase con composición y atributos protected.
 *
 * Una Membresia pertenece a un Cliente (composición: el Cliente la
 * referencia internamente). Esta clase modela el contrato de membresía
 * actual: tipo, vigencia y precio.
 *
 * Atributos protected:
 *   En P4 vamos a convertir esta clase en abstracta y crear
 *   MembresiaBasica, MembresiaPremium y MembresiaVIP como subclases.
 *   Esas subclases necesitaran acceso directo a tipo, fechas y precio
 *   para sobrescribir el comportamiento sin romper encapsulamiento.
 *   Por eso los atributos se marcan protected ahora, no private.
 */
public class Membresia {

    public static final String TIPO_BASICA  = "Basica";
    public static final String TIPO_PREMIUM = "Premium";
    public static final String TIPO_VIP     = "VIP";

    protected String    tipo;
    protected LocalDate fechaInicio;
    protected LocalDate fechaFin;
    protected double    precioMensual;
    protected boolean   activa;

    public Membresia() {
    }

    /*
     * Nota: los siguientes constructores llaman a setTipo, que es un
     * metodo de instancia. El compilador advierte "this-escape" porque
     * una eventual subclase podria sobrescribir setTipo y observar el
     * objeto a medio construir. En P4 voy a refactorizar Membresia a
     * clase abstracta y trasladar la validacion a un metodo final o
     * estatico; mientras tanto suprimo el warning de forma localizada
     * para no dejar ruido en compilacion limpia.
     */
    @SuppressWarnings("this-escape")
    public Membresia(String tipo) {
        setTipo(tipo);
        this.fechaInicio = LocalDate.now();
        this.fechaFin    = this.fechaInicio.plusDays(30);
        this.precioMensual = precioPorTipo(tipo);
        this.activa = true;
    }

    @SuppressWarnings("this-escape")
    public Membresia(String tipo, LocalDate fechaInicio, LocalDate fechaFin,
                     double precioMensual) {
        setTipo(tipo);
        setFechaInicio(fechaInicio);
        setFechaFin(fechaFin);
        setPrecioMensual(precioMensual);
        this.activa = true;
    }

    public String getTipo()             { return tipo; }
    public LocalDate getFechaInicio()   { return fechaInicio; }
    public LocalDate getFechaFin()      { return fechaFin; }
    public double getPrecioMensual()    { return precioMensual; }
    public boolean isActiva()           { return activa; }

    public void setTipo(String tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo no puede ser null.");
        }
        String t = tipo.trim();
        if (!t.equals(TIPO_BASICA) && !t.equals(TIPO_PREMIUM) && !t.equals(TIPO_VIP)) {
            throw new IllegalArgumentException(
                "Tipo de membresia invalido. Esperado: Basica, Premium o VIP. "
                + "Recibido: \"" + tipo + "\"");
        }
        this.tipo = t;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        if (fechaInicio == null) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser null.");
        }
        if (fechaFin != null && fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException(
                "La fecha de inicio no puede ser posterior a la de fin.");
        }
        this.fechaInicio = fechaInicio;
    }

    public void setFechaFin(LocalDate fechaFin) {
        if (fechaFin == null) {
            throw new IllegalArgumentException("La fecha de fin no puede ser null.");
        }
        if (fechaInicio != null && fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException(
                "La fecha de fin no puede ser anterior a la de inicio.");
        }
        this.fechaFin = fechaFin;
    }

    public void setPrecioMensual(double precioMensual) {
        if (precioMensual < 0) {
            throw new IllegalArgumentException(
                "El precio mensual no puede ser negativo. Recibido: " + precioMensual);
        }
        this.precioMensual = precioMensual;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    protected static double precioPorTipo(String tipo) {
        switch (tipo) {
            case TIPO_BASICA:  return 350.0;
            case TIPO_PREMIUM: return 650.0;
            case TIPO_VIP:     return 1200.0;
            default:           return 0.0;
        }
    }

    public boolean estaVigente() {
        if (!activa || fechaInicio == null || fechaFin == null) {
            return false;
        }
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(fechaInicio) && !hoy.isAfter(fechaFin);
    }

    public void renovar(int dias) {
        if (dias <= 0) {
            throw new IllegalArgumentException(
                "Los dias de renovacion deben ser positivos. Recibido: " + dias);
        }
        LocalDate base = (fechaFin != null && fechaFin.isAfter(LocalDate.now()))
                ? fechaFin
                : LocalDate.now();
        this.fechaFin = base.plusDays(dias);
        this.activa = true;
    }

    @Override
    public String toString() {
        return String.format("Membresia[%s, %s a %s, $%.2f/mes, %s]",
                tipo, fechaInicio, fechaFin, precioMensual,
                estaVigente() ? "vigente" : "vencida");
    }
}
