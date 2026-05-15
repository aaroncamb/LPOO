package com.gympos.exceptions;

import java.time.LocalDateTime;

/**
 * GymPOS - Excepcion raiz de las operaciones del gimnasio.
 *
 * Es abstracta y CHECKED. Las subclases concretas representan errores
 * del mundo externo que el sistema puede y debe manejar gracilmente:
 * un pago rechazado, una clase llena, una membresia vencida.
 *
 * Reutilizada y simplificada desde la Practica 7.
 */
public abstract class GymException extends Exception {

    private static final long serialVersionUID = 1L;

    private final LocalDateTime timestamp;

    protected GymException(String mensaje) {
        super(mensaje);
        this.timestamp = LocalDateTime.now();
    }

    protected GymException(String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.timestamp = LocalDateTime.now();
    }

    public LocalDateTime getTimestamp() { return timestamp; }

    /** Categoria del problema para clasificar logs y reportes. */
    public abstract String categoria();
}
