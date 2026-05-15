package com.gympos.model;

import java.time.LocalDate;

/**
 * Plan VIP: anual, incluye TODO + entrenador personal asignado.
 * Por su naturaleza (compromiso anual vs mensual) es la unica que
 * tiene duracionMeses = 12.
 *
 * El precio que recibe el constructor es ANUAL (no mensual); se
 * mantiene el campo precioMensual internamente como "precio efectivo
 * mensual prorrateado" para que la API sea consistente con las otras.
 */
public class MembresiaVIP extends Membresia {

    private static final long serialVersionUID = 1L;

    /**
     * @param precioAnual precio total del año. Se prorratea a mensual
     *                    para mantener consistencia con las otras
     *                    membresias.
     */
    public MembresiaVIP(int idMembresia, int idCliente,
                         LocalDate fechaInicio, double precioAnual) {
        super(idMembresia, idCliente, fechaInicio, precioAnual / 12.0);
    }

    @Override
    public String nombrePlan() { return "VIP"; }

    @Override
    public int duracionMeses() { return 12; }

    @Override
    public boolean incluyeClasesGrupales()      { return true; }

    @Override
    public boolean incluyeEntrenadorPersonal()  { return true; }

    @Override
    public String descripcion() {
        return "Acceso premium + entrenador personal asignado + clases ilimitadas. ANUAL.";
    }
}
