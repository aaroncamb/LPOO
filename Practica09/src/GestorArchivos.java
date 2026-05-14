import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Práctica 9 — Operaciones de archivos del sistema.
 *
 * Centraliza las tres formas de persistir y recuperar datos:
 *   1. TEXTO PLANO: para logs simples (linea por linea).
 *   2. CSV:         para intercambio con sistemas externos (Excel, BD).
 *   3. BINARIO:     para snapshot rapido del estado del sistema
 *                   (serializacion nativa de Java).
 *
 * Tambien expone operaciones con directorios (crear, listar).
 *
 * REGLA DE ORO DE LA CLASE: cada operacion de I/O usa try-with-resources.
 * Esto garantiza que los archivos se cierren correctamente aun cuando se
 * lance excepcion, sin tener que escribir finally manuales. Es la
 * mitigacion central del riesgo discutido en REFLEXION.md pregunta 3.
 */
public class GestorArchivos {

    /** Formateador para timestamps usados en logs y backups. */
    private static final DateTimeFormatter TS_FECHA_HORA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    // ============================================================
    //   DIRECTORIOS
    // ============================================================

    /**
     * Garantiza que el directorio existe. Si no, lo crea con todos
     * los padres necesarios. Lanza IOException si no se puede crear.
     */
    public void asegurarDirectorio(String ruta) throws IOException {
        Path p = Path.of(ruta);
        if (!Files.exists(p)) {
            Files.createDirectories(p);
        } else if (!Files.isDirectory(p)) {
            throw new IOException("La ruta existe pero no es directorio: " + ruta);
        }
    }

    /** Lista los archivos de un directorio (no recursivo). */
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

    // ============================================================
    //   TEXTO PLANO
    // ============================================================

    /**
     * Escribe una linea al final del archivo de texto. Si el archivo no
     * existe lo crea. Usa BufferedWriter para que escrituras frecuentes
     * (un log) no golpeen el disco linea por linea.
     */
    public void escribirLineaTexto(String archivo, String linea) throws IOException {
        try (BufferedWriter w = new BufferedWriter(
                new FileWriter(archivo, true))) {   // true = append
            w.write(linea);
            w.newLine();
        }
    }

    /**
     * Lee todas las lineas de un archivo de texto. BufferedReader llena
     * un buffer interno (~8KB) antes de devolver lineas, evitando una
     * lectura de disco por cada linea.
     */
    public List<String> leerLineasTexto(String archivo) throws IOException {
        List<String> lineas = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = r.readLine()) != null) {
                lineas.add(linea);
            }
        }
        return lineas;
    }

    // ============================================================
    //   CSV
    // ============================================================

    /**
     * Escribe una lista de clientes a un archivo CSV. Incluye encabezado.
     * Formato: id,nombre,email,fechaRegistro,pesoKg,tipo,activo
     *
     * NOTA: el nombre puede contener comas; por eso lo escapamos
     * envolviendolo en comillas dobles si la deteccion lo amerita. En
     * un sistema mas serio usariamos una libreria (OpenCSV), pero el
     * algoritmo basico cumple para los datos del gimnasio.
     */
    public void escribirCSV(String archivo, List<Cliente> clientes) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(archivo))) {
            // encabezado
            w.write("id,nombre,email,fechaRegistro,pesoKg,tipo,activo");
            w.newLine();
            for (Cliente c : clientes) {
                w.write(toCsvRow(c));
                w.newLine();
            }
        }
    }

    /**
     * Lee un archivo CSV y devuelve la lista de clientes.
     * Asume que la primera linea es encabezado y la salta.
     * Maneja nombres entre comillas (que pueden contener comas).
     */
    public List<Cliente> leerCSV(String archivo) throws IOException {
        List<Cliente> clientes = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new FileReader(archivo))) {
            String linea;
            boolean esEncabezado = true;
            int numeroLinea = 0;
            while ((linea = r.readLine()) != null) {
                numeroLinea++;
                if (esEncabezado) {
                    esEncabezado = false;
                    continue;
                }
                if (linea.isBlank()) continue;
                try {
                    clientes.add(fromCsvRow(linea));
                } catch (Exception e) {
                    // Linea corrupta: la saltamos pero avisamos.
                    System.err.println("[CSV] Linea " + numeroLinea
                            + " ignorada: " + e.getMessage());
                }
            }
        }
        return clientes;
    }

    private String toCsvRow(Cliente c) {
        // Si el nombre contiene comas, lo envolvemos en comillas.
        String nombre = c.getNombreCompleto().contains(",")
                ? "\"" + c.getNombreCompleto() + "\""
                : c.getNombreCompleto();
        return String.format("%d,%s,%s,%s,%.1f,%s,%s",
                c.getId(),
                nombre,
                c.getEmail(),
                c.getFechaRegistro(),
                c.getPesoKg(),
                c.getTipoMembresia(),
                c.esActivo());
    }

    private Cliente fromCsvRow(String linea) {
        // Parseo simple: split por coma respetando comillas dobles.
        List<String> campos = parsearLineaCSV(linea);
        if (campos.size() < 7) {
            throw new IllegalArgumentException(
                "se esperaban 7 campos, se encontraron " + campos.size());
        }
        int id           = Integer.parseInt(campos.get(0).trim());
        String nombre    = campos.get(1).trim();
        String email     = campos.get(2).trim();
        LocalDate fecha  = LocalDate.parse(campos.get(3).trim());
        double peso      = Double.parseDouble(campos.get(4).trim().replace(',', '.'));
        Cliente.TipoMembresia tipo =
                Cliente.TipoMembresia.valueOf(campos.get(5).trim());
        boolean activo   = Boolean.parseBoolean(campos.get(6).trim());

        Cliente c = new Cliente(id, nombre, email, fecha, peso, tipo);
        if (!activo) c.desactivar();
        return c;
    }

    /**
     * Parseo basico de CSV que respeta comillas dobles. No es completo
     * RFC-4180 pero cubre los casos del gimnasio (un nombre con coma).
     */
    private List<String> parsearLineaCSV(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean dentroComillas = false;

        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == '"') {
                dentroComillas = !dentroComillas;
            } else if (c == ',' && !dentroComillas) {
                campos.add(actual.toString());
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        campos.add(actual.toString());
        return campos;
    }

    // ============================================================
    //   BINARIO (Serializable)
    // ============================================================

    /**
     * Guarda una lista de clientes como objeto serializado.
     * BufferedOutputStream entre ObjectOutputStream y FileOutputStream
     * acelera la escritura agrupando bytes en bloques.
     */
    public void guardarBinario(String archivo, List<Cliente> clientes) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(archivo)))) {
            out.writeObject(new ArrayList<>(clientes));  // copia defensiva
        }
    }

    /**
     * Carga una lista de clientes desde un archivo binario serializado.
     *
     * El @SuppressWarnings("unchecked") es necesario porque readObject()
     * devuelve Object; el cast no se puede verificar genericamente en
     * runtime (type erasure). Si el archivo no contiene una List, la
     * ClassCastException avisara al instante.
     */
    @SuppressWarnings("unchecked")
    public List<Cliente> cargarBinario(String archivo)
            throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(archivo)))) {
            return (List<Cliente>) in.readObject();
        }
    }

    // ============================================================
    //   UTILIDADES
    // ============================================================

    /** Devuelve un timestamp en formato "yyyy-MM-dd_HH-mm-ss". */
    public static String timestampActual() {
        return LocalDateTime.now().format(TS_FECHA_HORA);
    }
}
