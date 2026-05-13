/**
 * Práctica 5 — Interfaz que define el contrato de "cosa que se puede cobrar".
 *
 * Cualquier clase que implemente Cobrable promete saber calcular su
 * subtotal, aplicarse un descuento porcentual, calcular sus impuestos
 * y devolver un total final.
 *
 * Nota: la version (double porcentaje) de aplicarDescuento esta aqui
 * en la interfaz porque es el caso comun. Las clases concretas pueden
 * SOBRECARGARLA con versiones que reciban int (monto fijo) o String
 * (codigo de cupon). Ese es el ejercicio de sobrecarga de P5.
 */
public interface Cobrable {

    /** Subtotal del servicio antes de descuentos e impuestos. */
    double calcularSubtotal();

    /**
     * Aplica un descuento porcentual al subtotal.
     * @param porcentaje en formato 0.0 a 1.0 (0.15 = 15% de descuento)
     * @return el nuevo monto despues del descuento (subtotal * (1 - %))
     */
    double aplicarDescuento(double porcentaje);

    /** IVA u otro impuesto aplicable al subtotal. */
    double calcularImpuestos();

    /** Monto final a cobrar al cliente. */
    double calcularTotal();
}
