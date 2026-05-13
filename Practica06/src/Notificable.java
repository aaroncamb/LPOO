/**
 * Práctica 6 — Interfaz Notificable.
 *
 * Responsabilidad: cosas que pueden ENVIAR avisos a alguien (cliente o
 * grupo). El contrato minimo es saber a quien notificar y como.
 *
 * Tiene un metodo default `notificarMultiplesCanales` que llama a
 * `enviarEmail` y `enviarSMS`. Esto permite que las implementaciones
 * obtengan gratis la funcion compuesta, sin tener que reescribir la
 * coordinacion en cada clase.
 */
public interface Notificable {

    /** A quien va dirigida la notificacion (nombre del cliente o grupo). */
    String destinatario();

    /** Envia notificacion por email. Devuelve true si "se envio bien". */
    boolean enviarEmail(String asunto, String cuerpo);

    /** Envia notificacion por SMS. Devuelve true si "se envio bien". */
    boolean enviarSMS(String mensaje);

    /**
     * Metodo DEFAULT: notifica por ambos canales en una sola llamada.
     * Si tanto email como SMS reportan exito, devuelve true.
     *
     * Las clases que implementen Notificable NO tienen que reescribir
     * esta coordinacion: la heredan de la interfaz misma. Esto es una
     * de las ventajas de los metodos default agregados en Java 8: la
     * interfaz puede crecer sin romper a sus implementadores.
     */
    default boolean notificarMultiplesCanales(String asunto, String mensaje) {
        boolean emailOk = enviarEmail(asunto, mensaje);
        boolean smsOk   = enviarSMS(mensaje);
        return emailOk && smsOk;
    }
}
