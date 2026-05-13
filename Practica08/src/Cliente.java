import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/**
 * Práctica 8 — Cliente con Comparable.
 *
 * Evolucion del Cliente de P3: ahora implementa Comparable<Cliente>
 * para tener un orden natural (por nombre completo). Esto permite que
 * Collections.sort(lista) y estructuras ordenadas como TreeSet funcionen
 * sin necesidad de pasarle un Comparator explicito.
 *
 * El orden natural elegido es por nombre porque es el caso de uso mas
 * comun en listados, reportes y la pantalla de recepcion. Los otros
 * criterios (antiguedad, peso) se exponen via Comparators (P8 entrega 3).
 */
public class Cliente implements Comparable<Cliente> {

    public enum TipoMembresia { BASICA, PREMIUM, VIP }

    private int id;
    private String nombreCompleto;
    private String email;
    private LocalDate fechaRegistro;
    private double pesoKg;
    private TipoMembresia tipoMembresia;
    private boolean activo;

    public Cliente(int id, String nombreCompleto, String email,
                   LocalDate fechaRegistro, double pesoKg,
                   TipoMembresia tipoMembresia) {
        if (id <= 0) throw new IllegalArgumentException("id debe ser positivo.");
        if (nombreCompleto == null || nombreCompleto.isBlank())
            throw new IllegalArgumentException("nombre obligatorio.");
        if (email == null || !email.contains("@"))
            throw new IllegalArgumentException("email invalido.");
        if (fechaRegistro == null)
            throw new IllegalArgumentException("fecha registro obligatoria.");
        if (pesoKg != 0 && (pesoKg < 30 || pesoKg > 300))
            throw new IllegalArgumentException("peso fuera de rango.");
        if (tipoMembresia == null)
            throw new IllegalArgumentException("tipo membresia obligatorio.");

        this.id = id;
        this.nombreCompleto = nombreCompleto.trim();
        this.email = email.trim().toLowerCase();
        this.fechaRegistro = fechaRegistro;
        this.pesoKg = pesoKg;
        this.tipoMembresia = tipoMembresia;
        this.activo = true;
    }

    // -------- Comparable --------

    /**
     * Orden natural por nombre completo, sin distinguir mayusculas y
     * respetando acentos (Collator default del sistema).
     */
    @Override
    public int compareTo(Cliente otro) {
        return this.nombreCompleto.compareToIgnoreCase(otro.nombreCompleto);
    }

    // -------- Operaciones de dominio --------

    public int mesesActivo() {
        Period d = Period.between(fechaRegistro, LocalDate.now());
        return d.getYears() * 12 + d.getMonths();
    }

    public boolean esActivo() { return activo; }

    public void desactivar() { this.activo = false; }
    public void reactivar()  { this.activo = true; }

    // -------- Getters --------

    public int getId()                       { return id; }
    public String getNombreCompleto()        { return nombreCompleto; }
    public String getEmail()                 { return email; }
    public LocalDate getFechaRegistro()      { return fechaRegistro; }
    public double getPesoKg()                { return pesoKg; }
    public TipoMembresia getTipoMembresia()  { return tipoMembresia; }

    // -------- Object --------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cliente)) return false;
        return id == ((Cliente) o).id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return String.format("[%d] %-30s %-30s %s %s %.1fkg %s",
                id, nombreCompleto, email, fechaRegistro,
                tipoMembresia, pesoKg, activo ? "activo" : "inactivo");
    }
}
