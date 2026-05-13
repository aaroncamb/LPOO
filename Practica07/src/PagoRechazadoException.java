/**
 * Práctica 7 — Excepción de pago rechazado.
 *
 * ELEMENTO DE DECISION PROPIA: esta excepcion lleva INFORMACION DE
 * CONTEXTO ADICIONAL mas alla del mensaje. Cuando el equipo de soporte
 * recibe un ticket "el cliente dice que no pudo pagar", necesita poder
 * reconstruir el incidente sin tener que pedirle al cliente que repita
 * datos que ya tenia el sistema.
 *
 * Campos de contexto:
 *   - montoIntentado:     cuanto se intento cobrar
 *   - metodoPago:         "tarjeta", "efectivo", "transferencia"
 *   - codigoErrorInterno: codigo machine-readable ("INSUF_FUNDS",
 *                         "TARJETA_VENCIDA", "TIMEOUT_GATEWAY", etc)
 *   - metodoOrigen:       nombre del metodo del sistema donde se origino
 *   - referenciaTransaccion: id rastreable que el cliente puede dar
 *
 * En produccion estos datos van directo al log estructurado y permiten
 * filtrar incidentes por codigo, agruparlos en metricas y darle al
 * soporte una pista accionable sin tener que repetir el incidente.
 */
public class PagoRechazadoException extends GymException {

    private static final long serialVersionUID = 1L;

    private final double montoIntentado;
    private final String metodoPago;
    private final String codigoErrorInterno;
    private final String metodoOrigen;
    private final String referenciaTransaccion;

    public PagoRechazadoException(String mensaje,
                                   double montoIntentado,
                                   String metodoPago,
                                   String codigoErrorInterno,
                                   String metodoOrigen) {
        super(mensaje);
        this.montoIntentado        = montoIntentado;
        this.metodoPago            = metodoPago;
        this.codigoErrorInterno    = codigoErrorInterno;
        this.metodoOrigen          = metodoOrigen;
        this.referenciaTransaccion = generarReferencia();
    }

    /**
     * Genera una referencia corta y unica para que el cliente pueda darla
     * al soporte. Algo legible tipo "PAY-A3F7B2".
     */
    private static String generarReferencia() {
        long base = System.currentTimeMillis() ^ Thread.currentThread().threadId();
        return "PAY-" + Long.toHexString(base).toUpperCase().substring(0, 6);
    }

    public double getMontoIntentado()        { return montoIntentado; }
    public String getMetodoPago()            { return metodoPago; }
    public String getCodigoErrorInterno()    { return codigoErrorInterno; }
    public String getMetodoOrigen()          { return metodoOrigen; }
    public String getReferenciaTransaccion() { return referenciaTransaccion; }

    @Override
    public String categoria() {
        return "PAGO_RECHAZADO";
    }

    /**
     * Representacion estilo JSON para que sea facil pegar en un ticket de
     * soporte o consumir desde un dashboard de monitoreo.
     */
    @Override
    public String toString() {
        return String.format(
            "{ \"tipo\": \"%s\", \"timestamp\": \"%s\", \"referencia\": \"%s\", "
            + "\"monto\": %.2f, \"metodo_pago\": \"%s\", \"codigo\": \"%s\", "
            + "\"origen\": \"%s\", \"mensaje\": \"%s\" }",
            categoria(), getTimestamp(), referenciaTransaccion,
            montoIntentado, metodoPago, codigoErrorInterno,
            metodoOrigen, getMessage());
    }
}
