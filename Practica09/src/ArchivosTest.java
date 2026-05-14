import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Práctica 9 — Pruebas unitarias manuales.
 *
 * Crea un directorio temporal para no contaminar data/ del usuario.
 * Verifica cada operacion: texto, CSV, binario, backup, reporte.
 */
public class ArchivosTest {

    private static int pasadas = 0;
    private static int falladas = 0;
    private static final String DIR_TEST = "test-tmp";

    public static void main(String[] args) throws Exception {
        System.out.println("=== Pruebas - P9 I/O ===\n");

        // Limpia y prepara directorio de pruebas
        limpiarDirectorio(Path.of(DIR_TEST));
        Files.createDirectories(Path.of(DIR_TEST));

        pruebaEscribirYLeerTextoPlano();
        pruebaCSVRoundTrip();
        pruebaCSVConNombreQueTieneComa();
        pruebaCSVConLineaCorruptaSeIgnora();
        pruebaBinarioRoundTrip();
        pruebaBinarioPreservaTipoYEstadoActivo();
        pruebaDirectorioSeCreaSiNoExiste();
        pruebaListarArchivos();
        pruebaBackupTieneTimestampEnElNombre();
        pruebaBackupYRestaurar();
        pruebaListarBackups();
        pruebaReporteContieneCabecera();
        pruebaReporteContieneTodosLosClientes();
        pruebaReporteContieneTotales();
        pruebaTimestampFormatoCorrecto();

        // Limpieza
        limpiarDirectorio(Path.of(DIR_TEST));

        System.out.println("\n=== Resumen ===");
        System.out.println("Pasadas:  " + pasadas);
        System.out.println("Falladas: " + falladas);
        System.out.println("Total:    " + (pasadas + falladas));

        if (falladas > 0) System.exit(1);
    }

    // ---------- texto plano ----------

    private static void pruebaEscribirYLeerTextoPlano() throws IOException {
        GestorArchivos g = new GestorArchivos();
        String archivo = DIR_TEST + "/texto.txt";
        g.escribirLineaTexto(archivo, "linea uno");
        g.escribirLineaTexto(archivo, "linea dos");
        List<String> r = g.leerLineasTexto(archivo);
        check(r.size() == 2 && r.get(0).equals("linea uno") && r.get(1).equals("linea dos"),
              "texto plano: escribir y leer dos lineas", r);
    }

    // ---------- CSV ----------

    private static void pruebaCSVRoundTrip() throws IOException {
        GestorArchivos g = new GestorArchivos();
        List<Cliente> originales = DatosPrueba.generar();
        String archivo = DIR_TEST + "/clientes.csv";
        g.escribirCSV(archivo, originales);
        List<Cliente> recuperados = g.leerCSV(archivo);
        check(recuperados.size() == originales.size(),
              "CSV round-trip: misma cantidad de clientes", recuperados.size());
        check(recuperados.get(0).getId() == originales.get(0).getId()
                && recuperados.get(0).getNombreCompleto().equals(originales.get(0).getNombreCompleto()),
              "CSV round-trip: primer cliente preservado", null);
    }

    private static void pruebaCSVConNombreQueTieneComa() throws IOException {
        GestorArchivos g = new GestorArchivos();
        Cliente c = new Cliente(99, "Apellido, Nombre", "test@x.mx",
                LocalDate.of(2024, 1, 1), 70, Cliente.TipoMembresia.BASICA);
        String archivo = DIR_TEST + "/coma.csv";
        g.escribirCSV(archivo, List.of(c));
        List<Cliente> r = g.leerCSV(archivo);
        check(r.size() == 1 && r.get(0).getNombreCompleto().equals("Apellido, Nombre"),
              "CSV preserva nombre con coma (escapado con comillas)", null);
    }

    private static void pruebaCSVConLineaCorruptaSeIgnora() throws IOException {
        GestorArchivos g = new GestorArchivos();
        String archivo = DIR_TEST + "/corrupta.csv";
        // Escribimos a mano un CSV con una linea corrupta
        g.escribirLineaTexto(archivo, "id,nombre,email,fechaRegistro,pesoKg,tipo,activo");
        g.escribirLineaTexto(archivo, "1,Valido,v@x.mx,2024-01-01,70.0,BASICA,true");
        g.escribirLineaTexto(archivo, "FILA_INCOMPLETA,muchos,errores");
        g.escribirLineaTexto(archivo, "2,Otro,o@x.mx,2024-02-02,75.0,PREMIUM,true");

        List<Cliente> r = g.leerCSV(archivo);
        check(r.size() == 2,
              "CSV con linea corrupta: ignora la mala y procesa el resto", r.size());
    }

    // ---------- binario ----------

    private static void pruebaBinarioRoundTrip() throws IOException, ClassNotFoundException {
        GestorArchivos g = new GestorArchivos();
        List<Cliente> originales = DatosPrueba.generar();
        String archivo = DIR_TEST + "/clientes.dat";
        g.guardarBinario(archivo, originales);
        List<Cliente> recuperados = g.cargarBinario(archivo);
        check(recuperados.size() == originales.size(),
              "binario round-trip: misma cantidad", recuperados.size());
    }

    private static void pruebaBinarioPreservaTipoYEstadoActivo() throws IOException, ClassNotFoundException {
        GestorArchivos g = new GestorArchivos();
        List<Cliente> originales = DatosPrueba.generar();
        // Datos de prueba ya tiene algunos desactivados
        String archivo = DIR_TEST + "/preserva.dat";
        g.guardarBinario(archivo, originales);
        List<Cliente> recuperados = g.cargarBinario(archivo);

        // Verifica que se preservaron tipos y estado activo/inactivo
        long inactivosOriginal = originales.stream().filter(c -> !c.esActivo()).count();
        long inactivosRecuperados = recuperados.stream().filter(c -> !c.esActivo()).count();
        check(inactivosOriginal == inactivosRecuperados,
              "binario preserva el flag activo (" + inactivosOriginal + " inactivos)", null);

        long vipOriginal = originales.stream()
                .filter(c -> c.getTipoMembresia() == Cliente.TipoMembresia.VIP).count();
        long vipRecuperado = recuperados.stream()
                .filter(c -> c.getTipoMembresia() == Cliente.TipoMembresia.VIP).count();
        check(vipOriginal == vipRecuperado,
              "binario preserva enum TipoMembresia (" + vipOriginal + " VIPs)", null);
    }

    // ---------- directorios ----------

    private static void pruebaDirectorioSeCreaSiNoExiste() throws IOException {
        GestorArchivos g = new GestorArchivos();
        String nuevoDir = DIR_TEST + "/nuevo/anidado";
        g.asegurarDirectorio(nuevoDir);
        check(Files.isDirectory(Path.of(nuevoDir)),
              "asegurarDirectorio crea estructura anidada", null);
    }

    private static void pruebaListarArchivos() throws IOException {
        GestorArchivos g = new GestorArchivos();
        String dir = DIR_TEST + "/listar";
        g.asegurarDirectorio(dir);
        Files.writeString(Path.of(dir, "a.txt"), "");
        Files.writeString(Path.of(dir, "b.txt"), "");
        Files.writeString(Path.of(dir, "c.csv"), "");
        List<String> r = g.listarArchivos(dir);
        check(r.size() == 3, "listarArchivos devuelve los 3", r);
    }

    // ---------- backups ----------

    private static void pruebaBackupTieneTimestampEnElNombre() throws IOException {
        GestorArchivos g = new GestorArchivos();
        String origen = DIR_TEST + "/origen.dat";
        Files.writeString(Path.of(origen), "datos de prueba");

        BackupManager bm = new BackupManager(DIR_TEST + "/backups1", g);
        Path destino = bm.crearBackup(origen);
        String nombre = destino.getFileName().toString();

        // Debe ser tipo origen_yyyy-MM-dd_HH-mm-ss.dat
        check(nombre.startsWith("origen_") && nombre.endsWith(".dat")
                && nombre.matches("origen_\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}\\.dat"),
              "backup tiene formato origen_yyyy-MM-dd_HH-mm-ss.dat", nombre);
    }

    private static void pruebaBackupYRestaurar() throws IOException {
        GestorArchivos g = new GestorArchivos();
        String origen = DIR_TEST + "/restaurable.dat";
        Files.writeString(Path.of(origen), "version original");

        BackupManager bm = new BackupManager(DIR_TEST + "/backups2", g);
        Path backup = bm.crearBackup(origen);

        // Modifico el origen
        Files.writeString(Path.of(origen), "version modificada");

        // Restauro
        bm.restaurarBackup(backup.getFileName().toString(), origen);

        String contenido = Files.readString(Path.of(origen));
        check(contenido.equals("version original"),
              "restaurar regresa el contenido original", contenido);
    }

    private static void pruebaListarBackups() throws IOException, InterruptedException {
        GestorArchivos g = new GestorArchivos();
        BackupManager bm = new BackupManager(DIR_TEST + "/backups3", g);

        String origen = DIR_TEST + "/multibackup.dat";
        Files.writeString(Path.of(origen), "v1");
        bm.crearBackup(origen);
        Thread.sleep(1100);  // asegurar timestamp distinto (segundos)
        Files.writeString(Path.of(origen), "v2");
        bm.crearBackup(origen);

        List<String> backups = bm.listarBackups();
        check(backups.size() == 2,
              "listarBackups devuelve los dos generados", backups.size());
    }

    // ---------- reporte alineado ----------

    private static void pruebaReporteContieneCabecera() {
        ReporteAlineado r = new ReporteAlineado();
        String contenido = r.generar(DatosPrueba.generar());
        check(contenido.contains("GIMNASIO - REPORTE DE CLIENTES")
                && contenido.contains("Generado:")
                && contenido.contains("Total registros: 20"),
              "reporte contiene cabecera con titulo, fecha y total", null);
    }

    private static void pruebaReporteContieneTodosLosClientes() {
        ReporteAlineado r = new ReporteAlineado();
        List<Cliente> datos = DatosPrueba.generar();
        String contenido = r.generar(datos);
        boolean todos = true;
        for (Cliente c : datos) {
            if (!contenido.contains(String.valueOf(c.getId()))) {
                todos = false; break;
            }
        }
        check(todos, "reporte contiene los 20 IDs de cliente", null);
    }

    private static void pruebaReporteContieneTotales() {
        ReporteAlineado r = new ReporteAlineado();
        String contenido = r.generar(DatosPrueba.generar());
        check(contenido.contains("Totales por tipo:")
                && contenido.contains("BASICA:")
                && contenido.contains("PREMIUM:")
                && contenido.contains("VIP:")
                && contenido.contains("Activos:"),
              "reporte contiene seccion de totales", null);
    }

    // ---------- timestamp ----------

    private static void pruebaTimestampFormatoCorrecto() {
        String ts = GestorArchivos.timestampActual();
        check(ts.matches("\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}"),
              "timestamp tiene formato yyyy-MM-dd_HH-mm-ss", ts);
    }

    // ---------- helpers ----------

    private static void check(boolean cond, String d, Object detalle) {
        if (cond) pasar(d);
        else      fallar(d, String.valueOf(detalle));
    }

    private static void pasar(String d) {
        pasadas++;
        System.out.println("  [OK] " + d);
    }

    private static void fallar(String d, String detalle) {
        falladas++;
        System.out.println("  [FAIL] " + d + ": " + detalle);
    }

    /** Borra recursivamente un directorio si existe. */
    private static void limpiarDirectorio(Path p) throws IOException {
        if (!Files.exists(p)) return;
        try (var stream = Files.walk(p)) {
            stream.sorted((a, b) -> b.toString().length() - a.toString().length())
                  .forEach(f -> {
                      try { Files.deleteIfExists(f); }
                      catch (IOException ignored) {}
                  });
        }
    }
}
