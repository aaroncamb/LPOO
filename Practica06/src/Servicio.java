import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Práctica 6 — Clase abstracta raiz de los servicios del gimnasio.
 *
 * Es la misma clase de P5 pero enriquecida para P6:
 *   - Atributo protected adicional: notas (libre para anotaciones).
 *   - Metodos concretos nuevos: resumen() y descripcionCompleta().
 *
 * Cumple los requisitos del entregable 1 de P6:
 *   - Clase abstracta: SI (declarada con la palabra clave abstract)
 *   - Metodos abstractos: validarCliente, emitirComprobante, tipoServicio
 *   - Metodos concretos: calcularSubtotal, aplicarDescuento,
 *     calcularImpuestos, calcularTotal, estaVigente, resumen,
 *     descripcionCompleta, procesarVenta (Template Method)
 *   - Atributos protected: nombreServicio, clienteNombre, fechaServicio,
 *     precioBase, descuentoAplicado, notas
 */
public abstract class Servicio implements Cobrable {

    protected static final double IVA = 0.16;

    protected String    nombreServicio;
    protected String    clienteNombre;
    protected LocalDate fechaServicio;
    protected double    precioBase;
    protected double    descuentoAplicado;
    protected String    notas;   // protected adicional para P6

    public Servicio(String nombreServicio, String clienteNombre,
                    LocalDate fechaServicio, double precioBase) {
        if (nombreServicio == null || nombreServicio.isBlank())
            throw new IllegalArgumentException("El nombre del servicio es obligatorio.");
        if (clienteNombre == null || clienteNombre.isBlank())
            throw new IllegalArgumentException("El cliente es obligatorio.");
        if (fechaServicio == null)
            throw new IllegalArgumentException("La fecha es obligatoria.");
        if (precioBase < 0)
            throw new IllegalArgumentException("El precio base no puede ser negativo.");

        this.nombreServicio    = nombreServicio.trim();
        this.clienteNombre     = clienteNombre.trim();
        this.fechaServicio     = fechaServicio;
        this.precioBase        = precioBase;
        this.descuentoAplicado = 0.0;
        this.notas             = "";
    }

    // -------- Implementacion de Cobrable (concretos compartidos) --------

    @Override
    public double calcularSubtotal() {
        return Math.max(0, precioBase - descuentoAplicado);
    }

    @Override
    public double aplicarDescuento(double porcentaje) {
        if (porcentaje < 0 || porcentaje > 1)
            throw new IllegalArgumentException(
                "El porcentaje debe estar entre 0 y 1. Recibido: " + porcentaje);
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

    // -------- Metodos abstractos --------

    public abstract boolean validarCliente();
    public abstract String  emitirComprobante();
    public abstract String  tipoServicio();

    // -------- Template Method (de P5) --------

    public final ResultadoVenta procesarVenta() {
        if (!validarCliente()) {
            return new ResultadoVenta(false, 0.0,
                "Cliente no valido para " + tipoServicio() + ": " + clienteNombre);
        }
        double total = calcularTotal();
        registrarEnBitacora(total);
        return new ResultadoVenta(true, total, emitirComprobante());
    }

    private void registrarEnBitacora(double total) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        System.out.printf("[%s] VENTA: %s a %s, total $%.2f%n",
                ts, tipoServicio(), clienteNombre, total);
    }

    // -------- Metodos concretos nuevos en P6 --------

    /**
     * Resumen compacto de una linea, util para listados rapidos.
     */
    public String resumen() {
        return String.format("%-25s %-20s %s  $%.2f",
                tipoServicio(), clienteNombre, fechaServicio, calcularTotal());
    }

    /**
     * Descripcion completa, multi-linea, util para mostrar en pantalla
     * o como cuerpo de notificacion.
     */
    public String descripcionCompleta() {
        StringBuilder sb = new StringBuilder();
        sb.append(tipoServicio()).append(": ").append(nombreServicio).append('\n');
        sb.append("Cliente: ").append(clienteNombre).append('\n');
        sb.append("Fecha:   ").append(fechaServicio).append('\n');
        sb.append("Total:   $").append(String.format("%.2f", calcularTotal())).append('\n');
        if (notas != null && !notas.isBlank()) {
            sb.append("Notas:   ").append(notas);
        }
        return sb.toString();
    }

    public void agregarNota(String texto) {
        if (texto == null) return;
        this.notas = notas.isEmpty() ? texto.trim() : notas + " | " + texto.trim();
    }

    public void resetearDescuento() {
        this.descuentoAplicado = 0.0;
    }

    // -------- Getters --------

    public String getNombreServicio()    { return nombreServicio; }
    public String getClienteNombre()     { return clienteNombre; }
    public LocalDate getFechaServicio()  { return fechaServicio; }
    public double getPrecioBase()        { return precioBase; }
    public double getDescuentoAplicado() { return descuentoAplicado; }
    public String getNotas()             { return notas; }

    @Override
    public String toString() {
        return resumen();
    }
}
