package com.gympos.exceptions;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Excepcion lanzada cuando un cliente intenta operar con una membresia
 * expirada.
 */
public class MembresiaVencidaException extends GymException {

    private static final long serialVersionUID = 1L;

    private final String clienteNombre;
    private final LocalDate fechaVencimiento;
    private final long diasDeVencida;

    public MembresiaVencidaException(String clienteNombre, LocalDate fechaVencimiento) {
        super(String.format("La membresia de '%s' vencio el %s (hace %d dias).",
                clienteNombre, fechaVencimiento,
                ChronoUnit.DAYS.between(fechaVencimiento, LocalDate.now())));
        this.clienteNombre = clienteNombre;
        this.fechaVencimiento = fechaVencimiento;
        this.diasDeVencida = ChronoUnit.DAYS.between(fechaVencimiento, LocalDate.now());
    }

    public String getClienteNombre()       { return clienteNombre; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public long getDiasDeVencida()         { return diasDeVencida; }

    @Override
    public String categoria() { return "MEMBRESIA_VENCIDA"; }
}
