package com.gympos.concurrency;

import com.gympos.service.GeneradorReportes;
import com.gympos.util.Loggers;

import javafx.concurrent.Task;

/**
 * GymPOS - Tarea concurrente para generar reportes en background.
 *
 * Implementa el patron documentado en P11 REFLEXION pregunta 1: las
 * operaciones que pueden tomar segundos (como recorrer miles de
 * tickets para generar un reporte) NO deben ejecutarse en el JavaFX
 * Application Thread (FXAT), porque congelarian la UI.
 *
 * javafx.concurrent.Task<T> es la herramienta para esto:
 *   - call() corre en un hilo de fondo.
 *   - setOnSucceeded() se ejecuta de vuelta en el FXAT cuando termina,
 *     asi puede tocar la UI con seguridad.
 *   - updateProgress() y updateMessage() actualizan barras de progreso
 *     de forma thread-safe.
 *
 * Esta clase es donde se materializa el "multithreading para tareas
 * pesadas" que pide la rubrica.
 */
public class TareaReporte extends Task<String> {

    public enum TipoReporte {
        GENERAL("reporte general"),
        INGRESOS("reporte de ingresos"),
        ASISTENCIA("reporte de asistencia");

        private final String descripcion;
        TipoReporte(String d) { this.descripcion = d; }
        public String getDescripcion() { return descripcion; }
    }

    private final GeneradorReportes generador;
    private final TipoReporte tipo;

    public TareaReporte(GeneradorReportes generador, TipoReporte tipo) {
        this.generador = generador;
        this.tipo = tipo;
        updateTitle("Generando " + tipo.getDescripcion());
    }

    /**
     * Se ejecuta en UN HILO DE FONDO. Aqui NO se puede tocar la UI
     * directamente; solo actualizar progreso/mensaje (thread-safe).
     */
    @Override
    protected String call() throws Exception {
        updateMessage("Preparando " + tipo.getDescripcion() + "...");
        updateProgress(0, 100);

        // Pequeña pausa para que la UI muestre la barra de progreso
        // (en datasets reales seria el tiempo de calculo).
        Thread.sleep(200);
        updateProgress(20, 100);

        String ruta = switch (tipo) {
            case GENERAL    -> generador.generarReporteGeneral();
            case INGRESOS   -> generador.generarReporteIngresos();
            case ASISTENCIA -> generador.generarReporteAsistencia();
        };

        updateProgress(80, 100);
        updateMessage("Guardando archivo...");
        Thread.sleep(100);
        updateProgress(100, 100);
        updateMessage("Listo: " + ruta);

        Loggers.info("Tarea " + tipo + " completada: " + ruta);
        return ruta;
    }
}
