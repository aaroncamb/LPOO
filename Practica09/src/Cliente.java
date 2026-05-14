import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Práctica 9 — Cliente serializable.
 *
 * Adaptacion del Cliente de P8: ahora implementa Serializable para
 * poder escribirse a un archivo binario via ObjectOutputStream y
 * leerse de vuelta con ObjectInputStream.
 *
 * El serialVersionUID es CRITICO: identifica la "version" de la clase.
 * Si manana agrego un atributo nuevo, deserializar un archivo viejo
 * podria fallar si el serialVersionUID cambia. Dejarlo explicito en
 * 1L me da control sobre cuando "rompe" la compatibilidad.
 */
public class Cliente implements Serializable {

    private static final long serialVersionUID = 1L;

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

    // -------- Getters --------

    public int getId()                       { return id; }
    public String getNombreCompleto()        { return nombreCompleto; }
    public String getEmail()                 { return email; }
    public LocalDate getFechaRegistro()      { return fechaRegistro; }
    public double getPesoKg()                { return pesoKg; }
    public TipoMembresia getTipoMembresia()  { return tipoMembresia; }
    public boolean esActivo()                { return activo; }

    public void desactivar() { this.activo = false; }
    public void reactivar()  { this.activo = true; }

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
        return String.format("[%d] %-30s %s %s %.1fkg",
                id, nombreCompleto, email, tipoMembresia, pesoKg);
    }
}
