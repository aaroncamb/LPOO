package com.gympos.persistence;

import com.gympos.util.Loggers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * GymPOS - Operaciones de archivo serializadas.
 *
 * Reutiliza el diseño de P9 pero generalizado: en lugar de
 * guardarBinario(List<Cliente>), expone metodos genericos para
 * cualquier List<T extends Serializable>. Esto permite reusar la
 * misma clase para Clientes, Membresias, Equipos, etc.
 *
 * Todas las operaciones usan try-with-resources para garantizar el
 * cierre correcto de los streams (P9 Reflexion 3).
 */
public class GestorArchivos {

    private static final DateTimeFormatter TS_FECHA_HORA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    // ============================================================
    //   DIRECTORIOS
    // ============================================================

    public void asegurarDirectorio(String ruta) throws IOException {
        Path p = Path.of(ruta);
        if (!Files.exists(p)) {
            Files.createDirectories(p);
            Loggers.info("Directorio creado: " + ruta);
        } else if (!Files.isDirectory(p)) {
            throw new IOException("La ruta existe pero no es directorio: " + ruta);
        }
    }

    public List<String> listarArchivos(String ruta) throws IOException {
        Path p = Path.of(ruta);
        if (!Files.isDirectory(p)) return List.of();
        List<String> archivos = new ArrayList<>();
        try (var stream = Files.list(p)) {
            stream.filter(Files::isRegularFile)
                  .forEach(f -> archivos.add(f.getFileName().toString()));
        }
        return archivos;
    }

    public boolean existe(String archivo) {
        return Files.exists(Path.of(archivo));
    }

    // ============================================================
    //   BINARIO GENERICO
    // ============================================================

    /**
     * Guarda una lista de objetos serializables al archivo dado.
     * Genérico: sirve para List<Cliente>, List<Equipo>, etc.
     */
    public <T> void guardarLista(String archivo, List<T> objetos) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(archivo)))) {
            out.writeObject(new ArrayList<>(objetos));
            Loggers.info("Guardado: " + archivo + " (" + objetos.size() + " items)");
        }
    }

    /**
     * Carga una lista de objetos serializables desde el archivo dado.
     * Si el archivo no existe, devuelve lista vacia (uso comun en
     * primera ejecucion).
     *
     * @SuppressWarnings("unchecked") porque readObject() devuelve Object;
     * el cast a List<T> no se puede verificar en runtime (type erasure).
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> cargarLista(String archivo) throws IOException, ClassNotFoundException {
        if (!Files.exists(Path.of(archivo))) {
            Loggers.info("Archivo no existe (carga vacia): " + archivo);
            return new ArrayList<>();
        }
        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(archivo)))) {
            List<T> lista = (List<T>) in.readObject();
            Loggers.info("Cargado: " + archivo + " (" + lista.size() + " items)");
            return lista;
        }
    }

    // ============================================================
    //   TEXTO PLANO (para reportes y logs)
    // ============================================================

    public void escribirTexto(String archivo, String contenido) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(archivo))) {
            w.write(contenido);
            Loggers.info("Texto escrito a " + archivo);
        }
    }

    // ============================================================
    //   UTILIDADES
    // ============================================================

    public static String timestampActual() {
        return LocalDateTime.now().format(TS_FECHA_HORA);
    }
}
