import java.time.LocalDate;

/**
 * Práctica 6 — Interfaz Reagendable.
 *
 * Responsabilidad: cosas que se pueden mover a otra fecha sin cancelar
 * la operacion (es decir, el cobro y el compromiso siguen vigentes,
 * solo cambia el dia).
 *
 * Aplica a servicios individuales (un entrenamiento personal, una
 * evaluacion fisica) pero NO a horarios publicos del gimnasio (clase
 * grupal de yoga del martes 7am: si un cliente no puede ir, los
 * otros 14 inscritos si van, no se mueve la clase).
 */
public interface Reagendable {

    /**
     * Cambia la fecha del servicio. Implementaciones deben validar que
     * la nueva fecha sea futura y respetar las reglas del negocio
     * (anticipacion minima, ventanas disponibles, etc).
     *
     * @return true si pudo reagendarse, false si la nueva fecha no es valida.
     */
    boolean reagendar(LocalDate nuevaFecha);

    /** Cuantos dias minimos de anticipacion exige este servicio. */
    int diasAnticipacionMinima();

    /**
     * Metodo DEFAULT: valida si la fecha propuesta CUMPLE la regla de
     * anticipacion minima. Lo aprovecho dentro de las implementaciones
     * de reagendar() en cada clase, asi no duplicamos el calculo.
     */
    default boolean fechaRespetaAnticipacion(LocalDate fechaPropuesta) {
        if (fechaPropuesta == null) return false;
        long dias = java.time.temporal.ChronoUnit.DAYS.between(
                LocalDate.now(), fechaPropuesta);
        return dias >= diasAnticipacionMinima();
    }
}
