import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/**
 * Práctica 3 — Cliente con encapsulamiento real.
 *
 * Cambios respecto a la versión de P2:
 *   - Todos los atributos pasan a private.
 *   - Se añaden getters y setters.
 *   - Los setters de email, peso y altura validan con reglas de negocio
 *     y lanzan IllegalArgumentException si el dato es inválido.
 *   - Se incorpora un nuevo atributo alturaCm (necesario para que el
 *     IMC se calcule a partir de los datos del propio cliente).
 *   - El cliente ahora tiene una Membresia asociada (composición).
 */
public class Cliente {

    private int       id;
    private String    nombreCompleto;
    private String    email;
    private LocalDate fechaRegistro;
    private double    pesoKg;
    private double    alturaCm;
    private Membresia membresia;   // composición: un Cliente tiene una Membresia

    public Cliente() {
    }

    @SuppressWarnings("this-escape")
    public Cliente(int id, String nombreCompleto, String email) {
        setId(id);
        setNombreCompleto(nombreCompleto);
        setEmail(email);
        this.fechaRegistro = LocalDate.now();
    }

    @SuppressWarnings("this-escape")
    public Cliente(int id, String nombreCompleto, String email,
                   LocalDate fechaRegistro, double pesoKg, double alturaCm) {
        setId(id);
        setNombreCompleto(nombreCompleto);
        setEmail(email);
        setFechaRegistro(fechaRegistro);
        setPesoKg(pesoKg);
        setAlturaCm(alturaCm);
    }

    // -------- Getters --------

    public int getId()                    { return id; }
    public String getNombreCompleto()     { return nombreCompleto; }
    public String getEmail()              { return email; }
    public LocalDate getFechaRegistro()   { return fechaRegistro; }
    public double getPesoKg()             { return pesoKg; }
    public double getAlturaCm()           { return alturaCm; }
    public Membresia getMembresia()       { return membresia; }

    // -------- Setters con validación --------

    public void setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                "El id debe ser positivo. Recibido: " + id);
        }
        this.id = id;
    }

    public void setNombreCompleto(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().length() < 2) {
            throw new IllegalArgumentException(
                "El nombre completo debe tener al menos 2 caracteres. Recibido: \"" 
                + nombreCompleto + "\"");
        }
        this.nombreCompleto = nombreCompleto.trim();
    }

    /**
     * Regla de validación no trivial #1:
     *   El email debe tener formato válido: parte local, @, dominio con
     *   al menos un punto, y caracteres aceptados. No es una validación
     *   completa RFC-5322 (eso es excesivo para un POS de gimnasio), pero
     *   atrapa los errores comunes de captura.
     */
    public void setEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("El email no puede ser null.");
        }
        String e = email.trim();
        // Mínimo: algo@algo.algo
        if (!e.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException(
                "El email no tiene formato válido. Recibido: \"" + e + "\"");
        }
        this.email = e.toLowerCase();
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        if (fechaRegistro == null) {
            throw new IllegalArgumentException("La fecha de registro no puede ser null.");
        }
        if (fechaRegistro.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                "La fecha de registro no puede ser futura. Recibida: " + fechaRegistro);
        }
        this.fechaRegistro = fechaRegistro;
    }

    /**
     * Regla de validación no trivial #2:
     *   El peso debe estar entre 30 y 300 kg.
     *
     *   Justificación de los límites:
     *     - 30 kg: por debajo de ese valor el caso es médico (anorexia
     *       severa, niños pequeños) y no debería procesarse como cliente
     *       común de un gimnasio comercial.
     *     - 300 kg: por arriba de ese valor casi seguramente es error de
     *       captura (peso en libras escrito como si fueran kilos, dedo
     *       resbalado en el cero, etc.).
     *
     *   Se permite 0.0 como caso especial cuando el cliente todavía no
     *   se ha pesado en la báscula del gimnasio.
     */
    public void setPesoKg(double pesoKg) {
        if (pesoKg == 0.0) {
            this.pesoKg = 0.0;     // "aún no se ha registrado"
            return;
        }
        if (pesoKg < 30.0 || pesoKg > 300.0) {
            throw new IllegalArgumentException(
                "El peso debe estar entre 30 y 300 kg (o ser 0 si aún no se pesa). "
                + "Recibido: " + pesoKg);
        }
        this.pesoKg = pesoKg;
    }

    /**
     * Regla de validación adicional:
     *   La altura debe estar entre 120 y 230 cm. Bajo 120 cm es estatura
     *   infantil que un gimnasio para adultos no atiende; arriba de 230 cm
     *   es prácticamente imposible (el récord mundial documentado es 251 cm).
     *   Como con el peso, se permite 0 para "aún no se mide".
     */
    public void setAlturaCm(double alturaCm) {
        if (alturaCm == 0.0) {
            this.alturaCm = 0.0;
            return;
        }
        if (alturaCm < 120.0 || alturaCm > 230.0) {
            throw new IllegalArgumentException(
                "La altura debe estar entre 120 y 230 cm (o ser 0 si aún no se mide). "
                + "Recibida: " + alturaCm);
        }
        this.alturaCm = alturaCm;
    }

    public void setMembresia(Membresia membresia) {
        this.membresia = membresia;   // se permite null (cliente sin membresía activa)
    }

    // -------- Métodos de dominio --------

    /**
     * Calcula el IMC del cliente con base en su peso y altura.
     * Si falta alguno de los dos datos, regresa -1 como indicador.
     */
    public double calcularIMC() {
        if (pesoKg <= 0 || alturaCm <= 0) {
            return -1;
        }
        double alturaM = alturaCm / 100.0;
        return pesoKg / (alturaM * alturaM);
    }

    public int mesesActivo() {
        if (fechaRegistro == null) return 0;
        Period diferencia = Period.between(fechaRegistro, LocalDate.now());
        return diferencia.getYears() * 12 + diferencia.getMonths();
    }

    public boolean esVeterano() {
        return mesesActivo() >= 12;
    }

    public String primerNombre() {
        if (nombreCompleto == null || nombreCompleto.isBlank()) return "";
        return nombreCompleto.trim().split("\\s+")[0];
    }

    // -------- Heredados de Object --------

    @Override
    public boolean equals(Object otro) {
        if (this == otro) return true;
        if (!(otro instanceof Cliente)) return false;
        return this.id == ((Cliente) otro).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * toString personalizado: incluye estado de membresía y, si hay datos
     * suficientes, el IMC calculado. Es la representación que veré en logs
     * y al iterar el gestor.
     */
    @Override
    public String toString() {
        String memStr = (membresia == null)
                ? "sin membresía"
                : membresia.getTipo() + (membresia.estaVigente() ? " (vigente)" : " (vencida)");
        double imc = calcularIMC();
        String imcStr = (imc < 0) ? "n/d" : String.format("%.1f", imc);

        return String.format(
                "Cliente[id=%d, %s, %s, registro=%s, %.1fkg, %.0fcm, IMC=%s, %s]",
                id, nombreCompleto, email, fechaRegistro,
                pesoKg, alturaCm, imcStr, memStr);
    }
}
