import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Práctica 5 — Clase abstracta raiz de los servicios del gimnasio.
 *
 * Servicio define lo que comparten ClaseGrupal, EntrenamientoPersonal y
 * EvaluacionFisica: tienen un nombre, un cliente al que se le facturan,
 * una fecha, un precio base y una tasa de IVA. Implementa Cobrable
 * porque todo servicio se cobra.
 *
 * Elemento de Decision Propia: TEMPLATE METHOD
 *   El metodo procesarVenta() es CONCRETO en esta clase abstracta y
 *   orquesta una secuencia fija de pasos:
 *
 *     1. validarCliente()      [abstracto - cada hija valida distinto]
 *     2. calcularTotal()       [definido por Cobrable]
 *     3. registrarEnBitacora() [concreto - lo mismo para todas]
 *     4. emitirComprobante()   [abstracto - cada hija formatea distinto]
 *
 *   Las hijas implementan las piezas variables; el padre garantiza que
 *   la secuencia se ejecute siempre en el orden correcto. Esa es la
 *   esencia del patron Template Method.
 *
 *   La ventaja sobre dejar el flujo a cada hija: si manana cambia el
 *   protocolo de venta (por ejemplo, agregar paso de verificacion de
 *   inventario antes de cobrar), se modifica en UN solo lugar y todas
 *   las hijas heredan el cambio. Si cada hija tuviera su propio
 *   procesarVenta() copiado, habria que tocar tres archivos y rezar
 *   por no olvidar uno (y manana cuando agreguemos un cuarto servicio,
 *   cuatro archivos).
 */
public abstract class Servicio implements Cobrable {

    protected static final double IVA = 0.16;

    protected String    nombreServicio;
    protected String    clienteNombre;
    protected LocalDate fechaServicio;
    protected double    precioBase;
    protected double    descuentoAplicado;   // monto absoluto descontado del subtotal

    public Servicio(String nombreServicio, String clienteNombre,
                    LocalDate fechaServicio, double precioBase) {
        if (nombreServicio == null || nombreServicio.isBlank()) {
            throw new IllegalArgumentException("El nombre del servicio es obligatorio.");
        }
        if (clienteNombre == null || clienteNombre.isBlank()) {
            throw new IllegalArgumentException("El cliente es obligatorio.");
        }
        if (fechaServicio == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        if (precioBase < 0) {
            throw new IllegalArgumentException("El precio base no puede ser negativo.");
        }
        this.nombreServicio    = nombreServicio.trim();
        this.clienteNombre     = clienteNombre.trim();
        this.fechaServicio     = fechaServicio;
        this.precioBase        = precioBase;
        this.descuentoAplicado = 0.0;
    }

    // ============================================================
    //   IMPLEMENTACION DE Cobrable (parte concreta compartida)
    // ============================================================

    @Override
    public double calcularSubtotal() {
        return Math.max(0, precioBase - descuentoAplicado);
    }

    /**
     * Aplica un descuento PORCENTUAL al precio base.
     * Es la version que pide la interfaz Cobrable. Las hijas pueden
     * sobrecargarla con versiones que reciban int (monto fijo) o
     * String (codigo de cupon).
     */
    @Override
    public double aplicarDescuento(double porcentaje) {
        if (porcentaje < 0 || porcentaje > 1) {
            throw new IllegalArgumentException(
                "El porcentaje debe estar entre 0 y 1. Recibido: " + porcentaje);
        }
        this.descuentoAplicado = precioBase * porcentaje;
        return calcularSubtotal();
    }

    @Override
    public double calcularImpuestos() {
        return calcularSubtotal() * IVA;
    }

    @Override
    public double calcularTotal() {
        return calcularSubtotal() + calcularImpuestos();
    }

    // ============================================================
    //   METODOS ABSTRACTOS - cada hija los implementa
    // ============================================================

    /** Valida que el cliente cumpla las condiciones para este servicio. */
    public abstract boolean validarCliente();

    /** Formatea el comprobante especifico del servicio. */
    public abstract String emitirComprobante();

    /** Tipo legible para impresion ("Clase Grupal", "Entrenamiento", etc). */
    public abstract String tipoServicio();

    // ============================================================
    //   TEMPLATE METHOD - el corazon de la decision propia
    // ============================================================

    /**
     * Orquesta el proceso de venta completo. Llama a metodos abstractos
     * en orden fijo. Las hijas NO sobreescriben procesarVenta(); en su
     * lugar implementan las piezas variables.
     */
    public final ResultadoVenta procesarVenta() {
        if (!validarCliente()) {
            return new ResultadoVenta(false, 0.0,
                "Cliente no valido para " + tipoServicio() + ": " + clienteNombre);
        }

        double total = calcularTotal();
        registrarEnBitacora(total);
        String comprobante = emitirComprobante();

        return new ResultadoVenta(true, total, comprobante);
    }

    /**
     * Paso concreto compartido del Template Method. Si todas las hijas
     * deben "registrar" la venta de la misma manera, no tiene caso
     * obligarlas a implementar el metodo: lo dejo concreto aqui.
     */
    private void registrarEnBitacora(double total) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        System.out.printf("[%s] VENTA registrada: %s a %s, total $%.2f%n",
                ts, tipoServicio(), clienteNombre, total);
    }

    // ============================================================
    //   Getters
    // ============================================================

    public String getNombreServicio()    { return nombreServicio; }
    public String getClienteNombre()     { return clienteNombre; }
    public LocalDate getFechaServicio()  { return fechaServicio; }
    public double getPrecioBase()        { return precioBase; }
    public double getDescuentoAplicado() { return descuentoAplicado; }

    /** Resetea el descuento aplicado (util entre ventas). */
    public void resetearDescuento() {
        this.descuentoAplicado = 0.0;
    }

    @Override
    public String toString() {
        return String.format("%s [%s, %s, $%.2f base, $%.2f total]",
                tipoServicio(), nombreServicio, clienteNombre,
                precioBase, calcularTotal());
    }
}
