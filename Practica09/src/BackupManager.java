import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Práctica 9 — Manager de backups con timestamp.
 *
 * Toma el archivo de datos actual y lo copia al directorio de backups
 * con un nombre que incluye la fecha y hora. Asi puedo tener un
 * historial de versiones del estado del sistema.
 *
 * Tambien expone listar y restaurar.
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
     * Crea un backup del archivo dado. El nombre del backup combina:
     *   - el nombre original sin extension
     *   - timestamp en formato yyyy-MM-dd_HH-mm-ss
     *   - la extension original
     *
     * Ejemplo: clientes.dat -> clientes_2026-05-13_18-30-15.dat
     *
     * @return la ruta al backup creado.
     */
    public Path crearBackup(String archivoOriginal) throws IOException {
        Path origen = Path.of(archivoOriginal);
        if (!Files.exists(origen)) {
            throw new IOException("El archivo a respaldar no existe: " + archivoOriginal);
        }

        String nombreOriginal = origen.getFileName().toString();
        String nombreBackup = construirNombreBackup(nombreOriginal);
        Path destino = Path.of(directorioBackups, nombreBackup);

        // Files.copy con REPLACE_EXISTING evita errores si por colision
        // de timestamp (mismo segundo) ya existiera. En la practica esto
        // casi nunca pasa pero conviene cubrirlo.
        Files.copy(origen, destino, StandardCopyOption.REPLACE_EXISTING);
        return destino;
    }

    /**
     * Construye el nombre del backup combinando el nombre original con
     * un timestamp justo antes de la extension.
     */
    private String construirNombreBackup(String nombreOriginal) {
        String ts = GestorArchivos.timestampActual();
        int dot = nombreOriginal.lastIndexOf('.');
        if (dot < 0) {
            // sin extension
            return nombreOriginal + "_" + ts;
        }
        String base = nombreOriginal.substring(0, dot);
        String ext  = nombreOriginal.substring(dot);
        return base + "_" + ts + ext;
    }

    /** Lista todos los backups disponibles. */
    public List<String> listarBackups() throws IOException {
        return archivos.listarArchivos(directorioBackups);
    }

    /**
     * Restaura un backup sobre el archivo dado, reemplazandolo.
     * El nombreBackup debe ser uno de los listados por listarBackups().
     */
    public void restaurarBackup(String nombreBackup, String archivoDestino) throws IOException {
        Path origen = Path.of(directorioBackups, nombreBackup);
        if (!Files.exists(origen)) {
            throw new IOException("Backup no encontrado: " + nombreBackup);
        }
        Path destino = Path.of(archivoDestino);
        Files.copy(origen, destino, StandardCopyOption.REPLACE_EXISTING);
    }

    public String getDirectorio() {
        return directorioBackups;
    }
}
