package com.gympos.util;

import com.gympos.exceptions.GymException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * GymPOS - Logger central de la aplicacion.
 *
 * Escribe operaciones y errores a un archivo plano con timestamps.
 * Implementa "fail-soft": si la escritura al log falla, NO propaga la
 * excepcion (un fallo de log no debe tumbar la aplicacion).
 *
 * Diseñado como singleton lazy para que cualquier modulo pueda llamar
 * Loggers.info("...") sin pasar instancias por todos lados.
 *
 * Concurrencia: cada escritura sincroniza sobre un lock estatico, asi
 * que si el GeneradorReportes (en otro hilo) y la UI escriben al mismo
 * tiempo, las lineas no quedan mezcladas.
 */
public final class Loggers {

    private static final DateTimeFormatter TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Object LOCK = new Object();

    private static String archivoLog = "data/operaciones.log";
    private static boolean inicializado = false;

    private Loggers() { /* utilidad estatica, sin instancias */ }

    /**
     * Configura la ruta del archivo de log. Si no se llama, usa el
     * valor por defecto "data/operaciones.log".
     */
    public static void configurar(String archivo) {
        archivoLog = archivo;
        inicializado = false;
    }

    public static void info(String mensaje)   { escribir("INFO",  null, mensaje); }
    public static void warn(String mensaje)   { escribir("WARN",  null, mensaje); }
    public static void error(String mensaje)  { escribir("ERROR", null, mensaje); }

    /** Registra una excepcion del dominio con su categoria. */
    public static void logExcepcion(GymException ex) {
        if (ex == null) return;
        escribir("ERROR", ex.categoria(), ex.getMessage());
    }

    /**
     * Escribe una linea al archivo de log con timestamp.
     * Sincronizado para evitar escrituras intercaladas entre hilos.
     */
    private static void escribir(String nivel, String categoria, String mensaje) {
        if (!inicializado) inicializar();

        String linea = String.format("%s [%-5s] %s%s%n",
                LocalDateTime.now().format(TS_FORMAT),
                nivel,
                categoria == null ? "" : categoria + " - ",
                mensaje);

        synchronized (LOCK) {
            try (BufferedWriter w = new BufferedWriter(
                    new FileWriter(archivoLog, true))) {
                w.write(linea);
            } catch (IOException e) {
                // Fail-soft: no tumbar el sistema por un fallo de log.
                System.err.println("[Loggers] No se pudo escribir al log: "
                        + e.getMessage());
            }
        }
    }

    /** Crea el directorio del archivo de log si no existe. */
    private static void inicializar() {
        try {
            Path p = Path.of(archivoLog);
            Path padre = p.getParent();
            if (padre != null && !Files.exists(padre)) {
                Files.createDirectories(padre);
            }
            inicializado = true;
        } catch (IOException e) {
            System.err.println("[Loggers] No se pudo preparar directorio: "
                    + e.getMessage());
        }
    }
}
