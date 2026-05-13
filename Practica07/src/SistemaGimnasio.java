import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Práctica 7 — Clase de negocio que orquesta operaciones del gimnasio.
 *
 * Es donde se LANZAN y se MANEJAN las excepciones. Cada operacion
 * principal puede fallar de formas distintas y la clase decide:
 *   - Cuando lanzar la excepcion (cumpliendo el "throws" en la firma).
 *   - Cuando capturarla, registrarla en el log, y propagarla.
 *
 * Las operaciones internas (procesarPago, inscribirCliente, validarAcceso)
 * declaran "throws GymException" en su firma, lo cual es el contrato:
 * "esta operacion puede fallar de manera recuperable, este preparado".
 *
 * Los simulamos sin conexion real a un banco; el codigo de error de
 * pago se genera aleatoriamente para que las pruebas y la demo
 * muestren distintos casos.
 */
public class SistemaGimnasio {

    private final GymLogger logger;
    private final Map<String, Integer> cuposInscritos = new HashMap<>();
    private final Map<String, Integer> cuposMaximos   = new HashMap<>();
    private final Map<String, LocalDate> vencimientos = new HashMap<>();
    private final Random rng = new Random(42);   // seed fijo para reproducibilidad

    public SistemaGimnasio(GymLogger logger) {
        if (logger == null) {
            throw new EntradaInvalidaException(
                "logger", null, "el logger es obligatorio");
        }
        this.logger = logger;
    }

    // -------- Configuracion inicial (no lanza GymException) --------

    public void registrarClase(String nombre, int cupoMaximo) {
        if (nombre == null || nombre.isBlank()) {
            throw new EntradaInvalidaException(
                "nombre", nombre, "nombre de clase requerido");
        }
        if (cupoMaximo <= 0) {
            throw new EntradaInvalidaException(
                "cupoMaximo", cupoMaximo, "debe ser positivo");
        }
        cuposMaximos.put(nombre, cupoMaximo);
        cuposInscritos.put(nombre, 0);
        logger.info("Clase registrada: " + nombre + " (cupo " + cupoMaximo + ")");
    }

    public void registrarMembresia(String cliente, LocalDate fechaVencimiento) {
        if (cliente == null || cliente.isBlank()) {
            throw new EntradaInvalidaException(
                "cliente", cliente, "nombre del cliente requerido");
        }
        if (fechaVencimiento == null) {
            throw new EntradaInvalidaException(
                "fechaVencimiento", null, "fecha de vencimiento requerida");
        }
        vencimientos.put(cliente, fechaVencimiento);
        logger.info("Membresia registrada para " + cliente + " vence " + fechaVencimiento);
    }

    // -------- Operaciones de negocio (PUEDEN lanzar GymException) --------

    /**
     * Procesa un pago. Simula un sistema de tarjeta que a veces falla.
     * Lanza PagoRechazadoException con contexto rico si falla.
     */
    public void procesarPago(String cliente, double monto, String metodoPago)
            throws PagoRechazadoException {

        if (monto <= 0) {
            throw new EntradaInvalidaException(
                "monto", monto, "el monto debe ser positivo");
        }

        // Simulacion: 30% de probabilidad de rechazo con codigos distintos.
        int dado = rng.nextInt(10);
        if (dado < 3) {
            String codigo;
            String mensajeBanco;
            switch (dado) {
                case 0 -> { codigo = "INSUF_FUNDS";    mensajeBanco = "Fondos insuficientes"; }
                case 1 -> { codigo = "TARJETA_VENCIDA"; mensajeBanco = "Tarjeta vencida"; }
                default -> { codigo = "TIMEOUT_GATEWAY"; mensajeBanco = "Timeout en gateway"; }
            }
            PagoRechazadoException ex = new PagoRechazadoException(
                    mensajeBanco + " al cobrar a " + cliente,
                    monto, metodoPago, codigo, "procesarPago");
            logger.logExcepcion(ex);
            throw ex;
        }

        logger.info(String.format("Pago OK: %s pago $%.2f via %s",
                cliente, monto, metodoPago));
    }

    /**
     * Inscribe a un cliente en una clase. Lanza CupoExcedidoException si
     * la clase ya esta llena.
     */
    public void inscribirEnClase(String cliente, String nombreClase)
            throws CupoExcedidoException {

        if (!cuposMaximos.containsKey(nombreClase)) {
            throw new EntradaInvalidaException(
                "nombreClase", nombreClase, "clase no registrada");
        }

        int actuales = cuposInscritos.get(nombreClase);
        int maximo   = cuposMaximos.get(nombreClase);

        if (actuales >= maximo) {
            CupoExcedidoException ex = new CupoExcedidoException(
                    nombreClase, maximo, actuales);
            logger.logExcepcion(ex);
            throw ex;
        }

        cuposInscritos.put(nombreClase, actuales + 1);
        logger.info(String.format("Inscripcion OK: %s en '%s' (%d/%d)",
                cliente, nombreClase, actuales + 1, maximo));
    }

    /**
     * Valida que el cliente pueda entrar al gimnasio (membresia vigente).
     * Lanza MembresiaVencidaException si no.
     */
    public void validarAcceso(String cliente)
            throws MembresiaVencidaException {

        LocalDate vence = vencimientos.get(cliente);
        if (vence == null) {
            throw new EntradaInvalidaException(
                "cliente", cliente, "cliente no registrado");
        }

        if (vence.isBefore(LocalDate.now())) {
            MembresiaVencidaException ex = new MembresiaVencidaException(cliente, vence);
            logger.logExcepcion(ex);
            throw ex;
        }

        logger.info("Acceso permitido: " + cliente);
    }

    /**
     * Operacion compuesta: el cliente entra al gimnasio, paga una clase
     * adicional y se inscribe. Si cualquiera de los pasos falla, se
     * captura, se loguea y se propaga al llamador para que decida que
     * hacer (mostrar mensaje al cliente, sugerir renovacion, etc).
     */
    public boolean intentarFlujoCompleto(String cliente, String nombreClase,
                                          double monto, String metodoPago) {
        try {
            validarAcceso(cliente);
            procesarPago(cliente, monto, metodoPago);
            inscribirEnClase(cliente, nombreClase);
            logger.info("FLUJO COMPLETO OK para " + cliente);
            return true;
        } catch (MembresiaVencidaException e) {
            logger.warn("Flujo cancelado: membresia vencida (" + e.getDiasDeVencida() + " dias).");
            return false;
        } catch (PagoRechazadoException e) {
            logger.warn("Flujo cancelado: pago rechazado, referencia "
                    + e.getReferenciaTransaccion());
            return false;
        } catch (CupoExcedidoException e) {
            logger.warn("Flujo cancelado: clase llena " + e.getNombreClase());
            return false;
        }
    }

    // -------- Getters utiles para pruebas --------

    public int inscritosEn(String clase) {
        return cuposInscritos.getOrDefault(clase, 0);
    }

    public int cupoMaximoDe(String clase) {
        return cuposMaximos.getOrDefault(clase, 0);
    }
}
