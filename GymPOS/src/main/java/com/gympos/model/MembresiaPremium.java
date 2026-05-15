package com.gympos.model;

import java.time.LocalDate;

/**
 * Plan premium: incluye clases grupales (yoga, spinning, etc).
 * Mensual con precio mayor que basica.
 */
public class MembresiaPremium extends Membresia {

    private static final long serialVersionUID = 1L;

    public MembresiaPremium(int idMembresia, int idCliente,
                             LocalDate fechaInicio, double precioMensual) {
        super(idMembresia, idCliente, fechaInicio, precioMensual);
    }

    @Override
    public String nombrePlan() { return "PREMIUM"; }

    @Override
    public int duracionMeses() { return 1; }

    @Override
    public boolean incluyeClasesGrupales()      { return true; }

    @Override
    public boolean incluyeEntrenadorPersonal()  { return false; }

    @Override
    public String descripcion() {
        return "Acceso completo + clases grupales (yoga, spinning, HIIT). Mensual.";
    }
}
