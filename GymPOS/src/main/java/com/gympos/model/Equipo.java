package com.gympos.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Equipo del gimnasio (caminadora, mancuerna, barra, etc).
 *
 * El estado del equipo se modela como un enum simple. Cuando un equipo
 * pasa a "EN_REPARACION" o "FUERA_DE_SERVICIO" no se puede asignar.
 */
public class Equipo implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Estado {
        OPERATIVO("Operativo"),
        EN_REPARACION("En reparacion"),
        FUERA_DE_SERVICIO("Fuera de servicio");

        private final String descripcion;
        Estado(String d) { this.descripcion = d; }
        public String getDescripcion() { return descripcion; }
    }

    private int idEquipo;
    private String nombre;
    private String categoria;     // "Cardio", "Fuerza", "Funcional"
    private LocalDate fechaAdquisicion;
    private Estado estado;

    public Equipo(int idEquipo, String nombre, String categoria,
                  LocalDate fechaAdquisicion) {
        if (idEquipo <= 0) throw new IllegalArgumentException("idEquipo debe ser positivo.");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("nombre obligatorio.");
        if (categoria == null) categoria = "General";

        this.idEquipo = idEquipo;
        this.nombre = nombre.trim();
        this.categoria = categoria.trim();
        this.fechaAdquisicion = fechaAdquisicion != null ? fechaAdquisicion : LocalDate.now();
        this.estado = Estado.OPERATIVO;
    }

    public void cambiarEstado(Estado nuevoEstado) {
        if (nuevoEstado == null) throw new IllegalArgumentException("estado obligatorio");
        this.estado = nuevoEstado;
    }

    public boolean estaOperativo() {
        return estado == Estado.OPERATIVO;
    }

    public int getIdEquipo()              { return idEquipo; }
    public String getNombre()             { return nombre; }
    public String getCategoria()          { return categoria; }
    public LocalDate getFechaAdquisicion(){ return fechaAdquisicion; }
    public Estado getEstado()             { return estado; }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("nombre obligatorio.");
        this.nombre = nombre.trim();
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria != null ? categoria.trim() : "General";
    }

    @Override
    public String toString() {
        return String.format("Equipo#%d %s [%s] %s",
                idEquipo, nombre, categoria, estado.getDescripcion());
    }
}
