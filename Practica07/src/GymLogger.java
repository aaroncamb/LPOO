import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Práctica 7 — Logger sencillo para registrar incidentes del gimnasio.
 *
 * Usa try-with-resources al escribir al archivo: garantiza que el
 * BufferedWriter se cierre aunque se lance excepcion al escribir,
 * sin necesidad de un finally explicito.
 *
 * Formato: cada entrada en una linea, prefijada con timestamp y nivel.
 *   2026-05-12T17:30:15 [WARN] CUPO_EXCEDIDO - mensaje detallado
 *
 * Si la escritura al archivo falla (disco lleno, permisos, etc) el
 * logger NO PROPAGA la excepcion: cae a stderr y sigue. Un fallo de
 * logging no debe tumbar el sistema de negocio.
 */
public class GymLogger {

    private static final DateTimeFormatter TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final String archivoLog;

    public GymLogger(String archivoLog) {
        if (archivoLog == null || archivoLog.isBlank()) {
            throw new EntradaInvalidaException(
                "archivoLog", archivoLog, "ruta de log requerida");
        }
        this.archivoLog = archivoLog;
    }

    public void info(String mensaje)  { escribir("INFO",  null, mensaje); }
    public void warn(String mensaje)  { escribir("WARN",  null, mensaje); }
    public void error(String mensaje) { escribir("ERROR", null, mensaje); }

    /**
     * Registra una excepcion del dominio con su categoria.
     * Si es PagoRechazadoException, dumpea su toString() rico (formato JSON).
     */
    public void logExcepcion(GymException ex) {
        if (ex instanceof PagoRechazadoException pr) {
            escribir("ERROR", pr.categoria(),
                    "Pago rechazado: " + pr.getMessage() + " | contexto: " + pr.toString());
        } else {
            escribir("ERROR", ex.categoria(), ex.getMessage());
        }
    }

    /**
     * Escribe una linea al archivo de log.
     *
     * USO DE TRY-WITH-RESOURCES: el BufferedWriter se declara en el
     * parentesis del try, y Java garantiza que close() se llame
     * automaticamente al salir del bloque, exitoso o con excepcion.
     *
     * Esto reemplaza al patron antiguo:
     *
     *   BufferedWriter w = null;
     *   try {
     *     w = new BufferedWriter(...);
     *     w.write(...);
     *   } finally {
     *     if (w != null) try { w.close(); } catch (IOException e) {}
     *   }
     *
     * que era propenso a olvidos y a tragar excepciones del cierre.
     */
    private void escribir(String nivel, String categoria, String mensaje) {
        String linea = String.format("%s [%-5s] %s%s%n",
                LocalDateTime.now().format(TS_FORMAT),
                nivel,
                categoria == null ? "" : categoria + " - ",
                mensaje);

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(archivoLog, true))) {   // append = true
            writer.write(linea);
        } catch (IOException e) {
            // Fallo de logging: no tumbar el sistema, solo avisar a stderr.
            System.err.println("[GymLogger] No se pudo escribir al log: "
                    + e.getMessage());
        }
    }

    public String getArchivoLog() {
        return archivoLog;
    }
}
