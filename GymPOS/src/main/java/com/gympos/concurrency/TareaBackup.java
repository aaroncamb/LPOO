package com.gympos.concurrency;

import com.gympos.persistence.BackupManager;
import com.gympos.util.Loggers;

import javafx.concurrent.Task;

import java.util.List;

/**
 * GymPOS - Tarea concurrente para hacer backup de varios archivos.
 *
 * Igual que TareaReporte: corre en hilo de fondo, actualiza progreso
 * y mensaje de forma thread-safe, devuelve el resultado al FXAT
 * cuando termina.
 *
 * Se invoca al cerrar la aplicacion (si esta habilitado en config) y
 * desde el menu "Archivo → Crear backup ahora".
 */
public class TareaBackup extends Task<Integer> {

    private final BackupManager backupManager;
    private final List<String> archivos;

    public TareaBackup(BackupManager backupManager, List<String> archivos) {
        this.backupManager = backupManager;
        this.archivos = archivos;
        updateTitle("Creando backup de " + archivos.size() + " archivos");
    }

    /** Numero de backups exitosos (puede ser menor que archivos.size() si algunos no existian). */
    @Override
    protected Integer call() throws Exception {
        int total = archivos.size();
        int creados = 0;

        for (int i = 0; i < total; i++) {
            String archivo = archivos.get(i);
            updateMessage("Respaldando " + archivo);
            try {
                if (backupManager.crearBackup(archivo) != null) creados++;
            } catch (Exception e) {
                Loggers.warn("Backup fallo para " + archivo + ": " + e.getMessage());
            }
            updateProgress(i + 1, total);
        }

        updateMessage(creados + " backups creados");
        return creados;
    }
}
