import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/**
 * Práctica 2 — Clases y Objetos.
 *
 * Modela a un cliente del gimnasio.
 *
 * Nota sobre el encapsulamiento:
 *   En esta práctica los atributos quedan con visibilidad por defecto
 *   (package-private) porque el tema de modificadores de acceso y
 *   validación pertenece a la Práctica 3. Allí los marcaré como private
 *   y agregaré getters/setters con reglas de validación.
 */
public class Cliente {

    int     id;
    String  nombreCompleto;
    String  email;
    LocalDate fechaRegistro;
    double  pesoKg;

    /**
     * Constructor vacío.
     * Útil cuando se va a llenar el objeto poco a poco (por ejemplo al
     * leer desde un formulario donde los campos llegan de a uno).
     */
    public Cliente() {
    }

    /**
     * Constructor mínimo: el caso común al dar de alta a un cliente nuevo.
     * Solo se piden los datos imprescindibles; el peso se inicializa en 0
     * (lo registra después la báscula del gimnasio) y la fecha de registro
     * se asigna automáticamente al día de hoy.
     */
    public Cliente(int id, String nombreCompleto, String email) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.fechaRegistro = LocalDate.now();
        this.pesoKg = 0.0;
    }

    /**
     * Constructor completo: útil para reconstruir un cliente desde un
     * archivo o una base de datos, donde sí conocemos todos los campos.
     */
    public Cliente(int id, String nombreCompleto, String email,
                   LocalDate fechaRegistro, double pesoKg) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.fechaRegistro = fechaRegistro;
        this.pesoKg = pesoKg;
    }

    // -------- Métodos de instancia --------

    /**
     * Cuenta cuántos meses completos lleva el cliente registrado en el gimnasio.
     * Útil para reportes de antigüedad y para activar promociones de fidelidad.
     */
    public int mesesActivo() {
        if (fechaRegistro == null) {
            return 0;
        }
        Period diferencia = Period.between(fechaRegistro, LocalDate.now());
        return diferencia.getYears() * 12 + diferencia.getMonths();
    }

    /**
     * Actualiza el peso del cliente. En esta práctica no hay validación;
     * eso es responsabilidad de la Práctica 3 (encapsulamiento + setters).
     */
    public void actualizarPeso(double nuevoPesoKg) {
        this.pesoKg = nuevoPesoKg;
    }

    /**
     * Indica si el cliente cumple criterio de "veterano" (más de 12 meses).
     * Esta regla la tomamos como negocio del gimnasio: clientes con más
     * de un año reciben prioridad en clases grupales.
     */
    public boolean esVeterano() {
        return mesesActivo() >= 12;
    }

    /**
     * Devuelve el primer nombre del cliente, útil para saludos en notificaciones.
     * Si el nombre es null o vacío, regresa una cadena vacía.
     */
    public String primerNombre() {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            return "";
        }
        return nombreCompleto.trim().split("\\s+")[0];
    }

    /**
     * Aplica un cambio de peso relativo (positivo o negativo) y devuelve
     * el nuevo peso. Útil cuando el gimnasio registra el avance entre
     * sesiones sin tener que calcular el total fuera de la clase.
     */
    public double registrarCambioPeso(double deltaKg) {
        this.pesoKg += deltaKg;
        return this.pesoKg;
    }

    // -------- Métodos heredados sobrescritos --------

    /**
     * Dos clientes son iguales si tienen el mismo id.
     * El id es la clave de negocio, así que es lo correcto para identidad.
     */
    @Override
    public boolean equals(Object otro) {
        if (this == otro) return true;
        if (!(otro instanceof Cliente)) return false;
        Cliente c = (Cliente) otro;
        return this.id == c.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(
                "Cliente[id=%d, nombre=%s, email=%s, registro=%s, peso=%.1fkg, meses=%d]",
                id, nombreCompleto, email, fechaRegistro, pesoKg, mesesActivo());
    }
}
