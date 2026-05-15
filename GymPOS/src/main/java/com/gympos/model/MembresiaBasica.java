package com.gympos.model;

import java.time.LocalDate;

/**
 * Plan basico: acceso al area de pesas y cardio, mensual.
 * NO incluye clases grupales ni entrenador personal.
 */
public class MembresiaBasica extends Membresia {

    private static final long serialVersionUID = 1L;

    public MembresiaBasica(int idMembresia, int idCliente,
                            LocalDate fechaInicio, double precioMensual) {
        super(idMembresia, idCliente, fechaInicio, precioMensual);
    }

    @Override
    public String nombrePlan() { return "BASICA"; }

    @Override
    public int duracionMeses() { return 1; }

    @Override
    public boolean incluyeClasesGrupales()      { return false; }

    @Override
    public boolean incluyeEntrenadorPersonal()  { return false; }

    @Override
    public String descripcion() {
        return "Acceso al area de pesas y cardio. Renovacion mensual.";
    }
}
