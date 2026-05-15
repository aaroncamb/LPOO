package com.gympos.model;

import com.gympos.exceptions.CupoExcedidoException;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Clase grupal del gimnasio (yoga, spinning, HIIT, etc).
 *
 * Almacena los IDs de clientes inscritos en un Set para validar
 * unicidad y no permitir cupos excedidos. Cada inscripcion adicional
 * verifica el cupo y lanza CupoExcedidoException si no hay lugar.
 */
public class ClaseGrupal implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idClase;
    private String nombre;
    private String instructor;
    private LocalDateTime horario;
    private int cupoMaximo;
    private double precio;
    private HashSet<Integer> inscritosIds = new HashSet<>();

    public ClaseGrupal(int idClase, String nombre, String instructor,
                       LocalDateTime horario, int cupoMaximo, double precio) {
        if (idClase <= 0) throw new IllegalArgumentException("idClase debe ser positivo.");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("nombre obligatorio.");
        if (cupoMaximo <= 0) throw new IllegalArgumentException("cupo debe ser positivo.");
        if (precio < 0) throw new IllegalArgumentException("precio no negativo.");

        this.idClase = idClase;
        this.nombre = nombre.trim();
        this.instructor = instructor != null ? instructor.trim() : "";
        this.horario = horario;
        this.cupoMaximo = cupoMaximo;
        this.precio = precio;
    }

    /**
     * Inscribe un cliente. Lanza CupoExcedidoException si la clase
     * esta llena. No-op silencioso si el cliente ya estaba inscrito.
     */
    public void inscribir(int idCliente) throws CupoExcedidoException {
        if (inscritosIds.contains(idCliente)) return;   // ya estaba
        if (inscritosIds.size() >= cupoMaximo) {
            throw new CupoExcedidoException(nombre, cupoMaximo, inscritosIds.size());
        }
        inscritosIds.add(idCliente);
    }

    public void cancelarInscripcion(int idCliente) {
        inscritosIds.remove(idCliente);
    }

    public boolean estaInscrito(int idCliente) {
        return inscritosIds.contains(idCliente);
    }

    public int lugaresDisponibles() {
        return cupoMaximo - inscritosIds.size();
    }

    public boolean estaLlena() {
        return inscritosIds.size() >= cupoMaximo;
    }

    // -------- Getters --------
    public int getIdClase()          { return idClase; }
    public String getNombre()        { return nombre; }
    public String getInstructor()    { return instructor; }
    public LocalDateTime getHorario(){ return horario; }
    public int getCupoMaximo()       { return cupoMaximo; }
    public double getPrecio()        { return precio; }
    public int getNumInscritos()     { return inscritosIds.size(); }
    public Set<Integer> getInscritosIds() { return new HashSet<>(inscritosIds); }

    @Override
    public String toString() {
        return String.format("%s con %s @ %s (%d/%d)",
                nombre, instructor, horario, inscritosIds.size(), cupoMaximo);
    }
}
