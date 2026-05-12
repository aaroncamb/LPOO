import java.time.LocalDate;

/**
 * Práctica 5 — Servicio de clase grupal (yoga, spinning, zumba, etc).
 *
 * Caracteristicas distintivas:
 *   - Tiene cupo limitado (asistentes inscritos vs cupo maximo).
 *   - Se valida que la fecha no sea pasada (no se cobran clases ya ocurridas).
 *   - Tres sobrecargas de aplicarDescuento (porcentual, monto fijo, cupon).
 */
public class ClaseGrupal extends Servicio {

    private final int cupoMaximo;
    private int inscritos;

    /** Tabla local de cupones validos. */
    private static final java.util.Map<String, Double> CUPONES = java.util.Map.of(
        "BIENVENIDA", 0.20,    // 20% para nuevos
        "AMIGO",      0.15,    // 15% por traer un amigo
        "TEMPORADA",  0.10     // 10% por temporada baja
    );

    public ClaseGrupal(String nombre, String cliente, LocalDate fecha,
                       double precio, int cupoMaximo) {
        super(nombre, cliente, fecha, precio);
        if (cupoMaximo <= 0) {
            throw new IllegalArgumentException("Cupo maximo debe ser positivo.");
        }
        this.cupoMaximo = cupoMaximo;
        this.inscritos  = 0;
    }

    // ---------- Implementacion de los metodos abstractos del padre ----------

    @Override
    public boolean validarCliente() {
        // Una clase grupal solo se vende si la fecha es hoy o futura y
        // todavia hay cupo disponible.
        if (fechaServicio.isBefore(LocalDate.now())) return false;
        return inscritos < cupoMaximo;
    }

    @Override
    public String emitirComprobante() {
        return String.format(
            "COMPROBANTE - Clase Grupal%n" +
            "  Cliente:    %s%n" +
            "  Clase:      %s%n" +
            "  Fecha:      %s%n" +
            "  Subtotal:   $%.2f%n" +
            "  IVA:        $%.2f%n" +
            "  TOTAL:      $%.2f",
            clienteNombre, nombreServicio, fechaServicio,
            calcularSubtotal(), calcularImpuestos(), calcularTotal());
    }

    @Override
    public String tipoServicio() {
        return "Clase Grupal";
    }

    // ---------- SOBRECARGAS de aplicarDescuento ----------
    //
    // El metodo (double porcentaje) ya viene del padre por Cobrable.
    // Aqui agrego DOS sobrecargas mas con tipos de parametro distintos
    // que la JVM diferencia por firma. La tercera sobrecarga (la del
    // padre) sigue funcionando heredada.
    //
    // Total: 3 firmas distintas de aplicarDescuento en esta clase.

    /**
     * Sobrecarga 2: descuento por monto fijo en pesos.
     * Si el descuento excede el precio, se cobra mínimo 0.
     */
    public double aplicarDescuento(int montoFijoPesos) {
        if (montoFijoPesos < 0) {
            throw new IllegalArgumentException(
                "El monto fijo no puede ser negativo. Recibido: " + montoFijoPesos);
        }
        this.descuentoAplicado = Math.min(montoFijoPesos, precioBase);
        return calcularSubtotal();
    }

    /**
     * Sobrecarga 3: descuento via codigo de cupon.
     * Si el cupon no existe, se lanza excepcion para que el cajero lo note.
     */
    public double aplicarDescuento(String codigoCupon) {
        if (codigoCupon == null) {
            throw new IllegalArgumentException("Codigo de cupon no puede ser null.");
        }
        Double porcentaje = CUPONES.get(codigoCupon.trim().toUpperCase());
        if (porcentaje == null) {
            throw new IllegalArgumentException(
                "Cupon no valido: \"" + codigoCupon + "\"");
        }
        // Reutilizo la version porcentual heredada del padre.
        return super.aplicarDescuento(porcentaje);
    }

    // ---------- Comportamiento propio de ClaseGrupal ----------

    public void inscribirAsistente() {
        if (inscritos >= cupoMaximo) {
            throw new IllegalStateException("Clase llena: " + nombreServicio);
        }
        inscritos++;
    }

    public int getInscritos()  { return inscritos; }
    public int getCupoMaximo() { return cupoMaximo; }
    public int getCupoDisponible() { return cupoMaximo - inscritos; }
}
