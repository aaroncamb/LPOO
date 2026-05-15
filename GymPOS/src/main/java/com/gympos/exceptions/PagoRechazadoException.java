package com.gympos.exceptions;

/**
 * Excepcion lanzada cuando un cobro falla. Lleva informacion de
 * contexto suficiente para que soporte tecnico pueda diagnosticar
 * el incidente sin pedirle al cliente que repita datos.
 */
public class PagoRechazadoException extends GymException {

    private static final long serialVersionUID = 1L;

    private final double montoIntentado;
    private final String metodoPago;
    private final String codigoErrorInterno;
    private final String referenciaTransaccion;

    public PagoRechazadoException(String mensaje,
                                  double montoIntentado,
                                  String metodoPago,
                                  String codigoErrorInterno) {
        super(mensaje);
        this.montoIntentado = montoIntentado;
        this.metodoPago = metodoPago;
        this.codigoErrorInterno = codigoErrorInterno;
        this.referenciaTransaccion = generarReferencia();
    }

    private static String generarReferencia() {
        long base = System.currentTimeMillis() ^ Thread.currentThread().threadId();
        return "PAY-" + Long.toHexString(base).toUpperCase().substring(0, 6);
    }

    public double getMontoIntentado()        { return montoIntentado; }
    public String getMetodoPago()            { return metodoPago; }
    public String getCodigoErrorInterno()    { return codigoErrorInterno; }
    public String getReferenciaTransaccion() { return referenciaTransaccion; }

    @Override
    public String categoria() {
        return "PAGO_RECHAZADO";
    }

    @Override
    public String toString() {
        return String.format(
            "{ \"tipo\": \"%s\", \"timestamp\": \"%s\", \"referencia\": \"%s\", "
            + "\"monto\": %.2f, \"metodo_pago\": \"%s\", \"codigo\": \"%s\", "
            + "\"mensaje\": \"%s\" }",
            categoria(), getTimestamp(), referenciaTransaccion,
            montoIntentado, metodoPago, codigoErrorInterno, getMessage());
    }
}
