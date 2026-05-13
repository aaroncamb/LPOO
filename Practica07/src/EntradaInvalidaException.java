/**
 * Práctica 7 — Excepción para datos invalidos pasados al sistema.
 *
 * IMPORTANTE: esta excepcion extiende RuntimeException, NO GymException.
 * Es UNCHECKED por decision de diseño.
 *
 * Por que unchecked y no checked como las demas:
 *
 *   - Las GymException (checked) representan errores del MUNDO EXTERNO
 *     que el llamador puede y debe manejar. Un pago rechazado es vida
 *     real, no un bug.
 *
 *   - EntradaInvalidaException representa un BUG DEL PROGRAMADOR: alguien
 *     paso un dato que NUNCA deberia haberse pasado (peso negativo, id
 *     nulo, fecha futura para un registro historico). Estos errores no
 *     son "manejables" en runtime: lo unico que el sistema puede hacer es
 *     fallar ruidosamente para que se note y se arregle el codigo.
 *
 *   - Forzar al codigo cliente a hacer try/catch de
 *     EntradaInvalidaException seria ruidoso y no aporta valor: el codigo
 *     que recibe un peso negativo no puede "recuperarse" del bug, solo
 *     puede propagarlo. Por eso unchecked.
 *
 * Esta distincion checked/unchecked se documenta en la pregunta 1 de
 * REFLEXION.md.
 */
public class EntradaInvalidaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String campo;
    // transient porque Object no garantiza ser Serializable, y esta clase
    // hereda de RuntimeException (que SI es Serializable). En la practica
    // nunca se serializa una excepcion de runtime, pero el compilador lo pide.
    private final transient Object valorRecibido;

    public EntradaInvalidaException(String campo, Object valorRecibido, String detalle) {
        super(String.format("Entrada invalida en '%s' (valor=%s): %s",
                campo, valorRecibido, detalle));
        this.campo         = campo;
        this.valorRecibido = valorRecibido;
    }

    public String getCampo()         { return campo; }
    public Object getValorRecibido() { return valorRecibido; }
}
