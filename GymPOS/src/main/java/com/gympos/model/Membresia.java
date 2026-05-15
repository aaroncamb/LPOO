package com.gympos.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Membresia del gimnasio - clase abstracta raiz de la jerarquia.
 *
 * Reutiliza el diseño de P4: clase abstracta con campos protected y
 * metodos abstractos que cada subclase implementa segun su politica.
 *
 * Subclases en GymPOS:
 *   MembresiaBasica   - mensual, precio fijo, sin clases grupales
 *   MembresiaPremium  - mensual, precio mayor, incluye clases grupales
 *   MembresiaVIP      - anual, incluye todo, entrenador asignado
 */
public abstract class Membresia implements Serializable {

    private static final long serialVersionUID = 1L;

    protected int idMembresia;
    protected int idCliente;
    protected LocalDate fechaInicio;
    protected LocalDate fechaVencimiento;
    protected double precioMensual;
    protected boolean activa;

    @SuppressWarnings("this-escape")
    protected Membresia(int idMembresia, int idCliente,
                         LocalDate fechaInicio, double precioMensual) {
        if (idMembresia <= 0) throw new IllegalArgumentException("idMembresia debe ser positivo.");
        if (idCliente <= 0)   throw new IllegalArgumentException("idCliente debe ser positivo.");
        if (fechaInicio == null) throw new IllegalArgumentException("fechaInicio obligatoria.");
        if (precioMensual < 0) throw new IllegalArgumentException("precio no puede ser negativo.");

        this.idMembresia = idMembresia;
        this.idCliente = idCliente;
        this.fechaInicio = fechaInicio;
        this.precioMensual = precioMensual;
        this.activa = true;
        // OK: las subclases solo definen constantes (duracionMeses retorna
        // numero fijo), no requieren campos inicializados para responder.
        this.fechaVencimiento = calcularFechaVencimiento(fechaInicio);
    }

    // -------- Metodos abstractos: cada subclase los implementa --------

    /** Nombre comercial del plan ("BASICA", "PREMIUM", "VIP"). */
    public abstract String nombrePlan();

    /** Duracion del plan en meses (1 para mensual, 12 para anual). */
    public abstract int duracionMeses();

    /** Si el plan incluye acceso a clases grupales. */
    public abstract boolean incluyeClasesGrupales();

    /** Si el plan incluye entrenador personal asignado. */
    public abstract boolean incluyeEntrenadorPersonal();

    /** Descripcion textual del plan para mostrar en UI. */
    public abstract String descripcion();

    // -------- Metodos concretos compartidos --------

    /** Calcula la fecha de vencimiento sumando duracionMeses(). */
    protected LocalDate calcularFechaVencimiento(LocalDate inicio) {
        return inicio.plusMonths(duracionMeses());
    }

    /** True si la membresia esta vigente HOY. */
    public boolean estaVigente() {
        return activa && !LocalDate.now().isAfter(fechaVencimiento);
    }

    /** Dias para que venza (negativo si ya vencio). */
    public long diasParaVencer() {
        return ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
    }

    /** Renueva la membresia desde hoy por la duracion del plan. */
    public void renovar() {
        this.fechaInicio = LocalDate.now();
        this.fechaVencimiento = calcularFechaVencimiento(this.fechaInicio);
        this.activa = true;
    }

    public void desactivar() { this.activa = false; }

    /** Costo total del periodo de esta membresia (precio mensual x duracion). */
    public double costoPeriodo() {
        return precioMensual * duracionMeses();
    }

    // -------- Getters --------

    public int getIdMembresia()             { return idMembresia; }
    public int getIdCliente()               { return idCliente; }
    public LocalDate getFechaInicio()       { return fechaInicio; }
    public LocalDate getFechaVencimiento()  { return fechaVencimiento; }
    public double getPrecioMensual()        { return precioMensual; }
    public boolean isActiva()               { return activa; }

    @Override
    public String toString() {
        return String.format("Membresia#%d %s (cliente %d) vence %s",
                idMembresia, nombrePlan(), idCliente, fechaVencimiento);
    }
}
