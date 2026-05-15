package com.gympos.exceptions;

/**
 * Excepcion UNCHECKED para datos invalidos pasados al sistema. A
 * diferencia de las GymException (checked, errores del mundo externo),
 * esta representa un bug del codigo que llama: monto negativo, id nulo,
 * fecha imposible.
 *
 * Es RuntimeException porque no tiene sentido obligar a try/catch un
 * bug que no deberia ocurrir; lo que se debe hacer es corregir el codigo
 * que paso los datos invalidos.
 */
public class EntradaInvalidaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String campo;
    private final transient Object valorRecibido;

    public EntradaInvalidaException(String campo, Object valorRecibido, String detalle) {
        super(String.format("Entrada invalida en '%s' (valor=%s): %s",
                campo, valorRecibido, detalle));
        this.campo = campo;
        this.valorRecibido = valorRecibido;
    }

    public String getCampo()         { return campo; }
    public Object getValorRecibido() { return valorRecibido; }
}
