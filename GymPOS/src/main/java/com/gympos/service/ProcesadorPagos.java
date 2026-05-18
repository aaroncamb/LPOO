package com.gympos.service;

import com.gympos.exceptions.EntradaInvalidaException;
import com.gympos.exceptions.PagoRechazadoException;
import com.gympos.model.Cliente;
import com.gympos.model.Membresia;
import com.gympos.util.Loggers;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * GymPOS - Modulo de procesamiento de pagos.
 *
 * Simula el flujo completo de un cobro:
 *   1. Calcular subtotal aplicando descuentos.
 *   2. Aplicar IVA.
 *   3. "Comunicar con el banco" (simulado, 90% exito).
 *   4. Si exitoso: registrar el ticket y sumar puntos al cliente.
 *   5. Si rechazado: lanzar PagoRechazadoException con contexto rico.
 *
 * Inspirado en el Template Method de P5 (procesarVenta()) y en la
 * excepcion rica de P7.
 */
public class ProcesadorPagos {

    private final double iva;
    private final int puntosPorPesoPagado;
    private final List<Ticket> ticketsEmitidos = new ArrayList<>();
    private final Random rng = new Random();
    private int siguienteIdTicket = 1;

    /**
     * @param iva           IVA aplicado a todos los cobros (ej. 0.16)
     * @param puntosPorPesoPagado puntos que gana el cliente por cada peso
     */
    public ProcesadorPagos(double iva, int puntosPorPesoPagado) {
        this.iva = iva;
        this.puntosPorPesoPagado = puntosPorPesoPagado;
    }

    // ============================================================
    //   COBRO PRINCIPAL
    // ============================================================

    /**
     * Cobra una membresia. Es el flujo "tipo Template Method":
     * valida -> calcula -> cobra -> registra -> recompensa.
     *
     * @param cliente cliente al que se le cobra
     * @param membresia membresia que se esta pagando
     * @param descuentoPct porcentaje de descuento (0.0 a 1.0)
     * @param metodoPago "tarjeta", "efectivo", "transferencia"
     */
    public Ticket cobrarMembresia(Cliente cliente, Membresia membresia,
                                  double descuentoPct, String metodoPago)
            throws PagoRechazadoException {

        if (cliente == null)   throw new EntradaInvalidaException("cliente", null, "obligatorio");
        if (membresia == null) throw new EntradaInvalidaException("membresia", null, "obligatorio");
        if (descuentoPct < 0 || descuentoPct > 1) {
            throw new EntradaInvalidaException("descuentoPct", descuentoPct, "rango 0..1");
        }

        double subtotal = membresia.costoPeriodo();
        double descuento = subtotal * descuentoPct;
        double base = subtotal - descuento;
        double impuestos = base * iva;
        double total = base + impuestos;

        // Simular comunicacion con el banco: 90% de exito
        intentarCobro(cliente, total, metodoPago);

        // Si llegamos aqui, el cobro fue exitoso
        Ticket t = new Ticket(siguienteIdTicket++, cliente.getId(),
                "Membresia " + membresia.nombrePlan(),
                subtotal, descuento, impuestos, total, metodoPago);
        ticketsEmitidos.add(t);

        // Recompensa: puntos por el monto pagado
        int puntosGanados = (int) (total * puntosPorPesoPagado);
        cliente.agregarPuntos(puntosGanados);

        Loggers.info("Pago OK: " + t + " (+" + puntosGanados + " pts)");
        return t;
    }

    /**
     * Cobro generico para cualquier concepto (clase grupal, evaluacion,
     * recargo, etc).
     */
    public Ticket cobrarConcepto(Cliente cliente, String concepto, double monto,
                                  String metodoPago)
            throws PagoRechazadoException {
        if (cliente == null)  throw new EntradaInvalidaException("cliente", null, "obligatorio");
        if (concepto == null) throw new EntradaInvalidaException("concepto", null, "obligatorio");
        if (monto <= 0)       throw new EntradaInvalidaException("monto", monto, "positivo");

        double impuestos = monto * iva;
        double total = monto + impuestos;

        intentarCobro(cliente, total, metodoPago);

        Ticket t = new Ticket(siguienteIdTicket++, cliente.getId(),
                concepto, monto, 0, impuestos, total, metodoPago);
        ticketsEmitidos.add(t);

        int puntosGanados = (int) (total * puntosPorPesoPagado);
        cliente.agregarPuntos(puntosGanados);

        Loggers.info("Pago OK: " + t + " (+" + puntosGanados + " pts)");
        return t;
    }

    /**
     * Simula la comunicacion con el banco. 90% exito. Falla aleatoria
     * con uno de varios codigos. Lanza PagoRechazadoException con
     * contexto rico cuando falla.
     */
    private void intentarCobro(Cliente cliente, double total, String metodoPago)
            throws PagoRechazadoException {
        int dado = rng.nextInt(10);
        if (dado == 0) {
            // 10% de falla con codigo aleatorio
            String[] codigos = {"INSUF_FUNDS", "TARJETA_VENCIDA", "TIMEOUT_GATEWAY"};
            String[] mensajes = {"Fondos insuficientes", "Tarjeta vencida", "Timeout en gateway"};
            int k = rng.nextInt(codigos.length);
            PagoRechazadoException ex = new PagoRechazadoException(
                    mensajes[k] + " al cobrar a " + cliente.getNombreCompleto(),
                    total, metodoPago, codigos[k]);
            Loggers.logExcepcion(ex);
            throw ex;
        }
    }

    // ============================================================
    //   ESTADISTICAS PARA REPORTES
    // ============================================================

    public List<Ticket> ticketsDelDia() {
        return new ArrayList<>(ticketsEmitidos);
    }

    public int totalTicketsEmitidos() { return ticketsEmitidos.size(); }

    public double ingresoBrutoTotal() {
        return ticketsEmitidos.stream().mapToDouble(Ticket::getTotal).sum();
    }

    // ============================================================
    //   CLASE INTERNA: Ticket
    // ============================================================

    /** Comprobante de un cobro exitoso. Inmutable y serializable. */
    public static class Ticket implements Serializable {
        private static final long serialVersionUID = 1L;

        private final int idTicket;
        private final int idCliente;
        private final String concepto;
        private final double subtotal;
        private final double descuento;
        private final double impuestos;
        private final double total;
        private final String metodoPago;
        private final LocalDateTime momento;

        public Ticket(int idTicket, int idCliente, String concepto,
                      double subtotal, double descuento, double impuestos,
                      double total, String metodoPago) {
            this.idTicket = idTicket;
            this.idCliente = idCliente;
            this.concepto = concepto;
            this.subtotal = subtotal;
            this.descuento = descuento;
            this.impuestos = impuestos;
            this.total = total;
            this.metodoPago = metodoPago;
            this.momento = LocalDateTime.now();
        }

        public int getIdTicket()        { return idTicket; }
        public int getIdCliente()       { return idCliente; }
        public String getConcepto()     { return concepto; }
        public double getSubtotal()     { return subtotal; }
        public double getDescuento()    { return descuento; }
        public double getImpuestos()    { return impuestos; }
        public double getTotal()        { return total; }
        public String getMetodoPago()   { return metodoPago; }
        public LocalDateTime getMomento() { return momento; }

        @Override
        public String toString() {
            return String.format("Ticket#%d %s $%.2f (%s)",
                    idTicket, concepto, total, metodoPago);
        }
    }
}
