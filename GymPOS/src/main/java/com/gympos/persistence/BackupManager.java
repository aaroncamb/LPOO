package com.gympos.persistence;

import com.gympos.util.Loggers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * GymPOS - Manager de backups.
 *
 * Hace copias de los archivos de datos al directorio data/backups/ con
 * un timestamp en el nombre. Reutilizado de P9.
 *
 * Diseñado para combinarse con TareaBackup (en concurrency/) que lo
 * ejecuta en background sin bloquear la UI.
 */
public class BackupManager {

    private final String directorioBackups;
    private final GestorArchivos archivos;

    public BackupManager(String directorioBackups, GestorArchivos archivos)
            throws IOException {
        this.directorioBackups = directorioBackups;
        this.archivos = archivos;
        archivos.asegurarDirectorio(directorioBackups);
    }

    /**
     * Crea un backup del archivo dado.
     * Ejemplo: clientes.dat -> clientes_2026-05-15_18-30-15.dat
     */
    public Path crearBackup(String archivoOriginal) throws IOException {
        Path origen = Path.of(archivoOriginal);
        if (!Files.exists(origen)) {
            Loggers.warn("Backup omitido (no existe): " + archivoOriginal);
            return null;
        }

        String nombreOriginal = origen.getFileName().toString();
        String nombreBackup = construirNombreBackup(nombreOriginal);
        Path destino = Path.of(directorioBackups, nombreBackup);

        Files.copy(origen, destino, StandardCopyOption.REPLACE_EXISTING);
        Loggers.info("Backup creado: " + destino);
        return destino;
    }

    /**
     * Backup masivo: respalda varios archivos a la vez.
     * Util al cerrar la aplicacion.
     */
    public int crearBackupMultiple(List<String> archivosOriginales) throws IOException {
        int creados = 0;
        for (String archivo : archivosOriginales) {
            if (crearBackup(archivo) != null) creados++;
        }
        return creados;
    }

    private String construirNombreBackup(String nombreOriginal) {
        String ts = GestorArchivos.timestampActual();
        int dot = nombreOriginal.lastIndexOf('.');
        if (dot < 0) return nombreOriginal + "_" + ts;
        return nombreOriginal.substring(0, dot) + "_" + ts + nombreOriginal.substring(dot);
    }

    public List<String> listarBackups() throws IOException {
        return archivos.listarArchivos(directorioBackups);
    }

    public void restaurarBackup(String nombreBackup, String archivoDestino) throws IOException {
        Path origen = Path.of(directorioBackups, nombreBackup);
        if (!Files.exists(origen)) {
            throw new IOException("Backup no encontrado: " + nombreBackup);
        }
        Path destino = Path.of(archivoDestino);
        Files.copy(origen, destino, StandardCopyOption.REPLACE_EXISTING);
        Loggers.info("Restaurado: " + archivoDestino + " <- " + nombreBackup);
    }

    public String getDirectorio() { return directorioBackups; }
}
