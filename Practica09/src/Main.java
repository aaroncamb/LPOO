import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Práctica 9 — Programa principal con CLI (Command Line Interface).
 *
 * Se invoca como:
 *     java Main <comando> [argumentos]
 *
 * Comandos disponibles:
 *     init                 Inicializa data/ con 20 clientes de prueba
 *                          (genera CSV y binario simultaneamente)
 *     load-csv             Lee data/clientes.csv y muestra los clientes
 *     load-bin             Lee data/clientes.dat y muestra los clientes
 *     save-bin             Guarda binario a partir del CSV actual
 *     backup               Crea backup del binario con timestamp
 *     list-backups         Lista los backups disponibles
 *     restore <archivo>    Restaura el binario desde un backup
 *     report               Genera reporte alineado en data/reporte.txt
 *     log <mensaje>        Agrega una linea al log de operaciones
 *     help                 Muestra esta ayuda
 *
 * Si no se pasa comando, ejecuta `help`.
 *
 * Sin argumentos especiales: lee data/ y data/backups/ del directorio
 * de trabajo actual.
 */
public class Main {

    // Rutas estandar
    private static final String DIR_DATA    = "data";
    private static final String DIR_BACKUPS = "data/backups";
    private static final String CSV         = "data/clientes.csv";
    private static final String BIN         = "data/clientes.dat";
    private static final String REPORTE     = "data/reporte.txt";
    private static final String LOG         = "data/operaciones.log";

    public static void main(String[] args) {
        if (args.length == 0) {
            mostrarAyuda();
            return;
        }

        try {
            GestorArchivos archivos = new GestorArchivos();
            archivos.asegurarDirectorio(DIR_DATA);
            BackupManager backups = new BackupManager(DIR_BACKUPS, archivos);

            String comando = args[0].toLowerCase();

            switch (comando) {
                case "init"          -> ejecutarInit(archivos);
                case "load-csv"      -> ejecutarLoadCsv(archivos);
                case "load-bin"      -> ejecutarLoadBin(archivos);
                case "save-bin"      -> ejecutarSaveBin(archivos);
                case "backup"        -> ejecutarBackup(backups);
                case "list-backups"  -> ejecutarListBackups(backups);
                case "restore"       -> ejecutarRestore(args, backups);
                case "report"        -> ejecutarReport(archivos);
                case "log"           -> ejecutarLog(args, archivos);
                case "help", "-h", "--help" -> mostrarAyuda();
                default -> {
                    System.err.println("Comando desconocido: " + comando);
                    mostrarAyuda();
                    System.exit(1);
                }
            }
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            System.exit(1);
        }
    }

    // ---------- comandos ----------

    private static void ejecutarInit(GestorArchivos archivos) throws IOException {
        List<Cliente> datos = DatosPrueba.generar();
        archivos.escribirCSV(CSV, datos);
        archivos.guardarBinario(BIN, datos);
        System.out.println("Inicializacion OK:");
        System.out.println("  " + CSV + "  (" + datos.size() + " clientes)");
        System.out.println("  " + BIN);
    }

    private static void ejecutarLoadCsv(GestorArchivos archivos) throws IOException {
        if (!Files.exists(Path.of(CSV))) {
            System.out.println("No existe " + CSV + ". Ejecuta primero: java Main init");
            return;
        }
        List<Cliente> clientes = archivos.leerCSV(CSV);
        System.out.println("CSV cargado: " + clientes.size() + " clientes");
        for (Cliente c : clientes) System.out.println("  " + c);
    }

    private static void ejecutarLoadBin(GestorArchivos archivos)
            throws IOException, ClassNotFoundException {
        if (!Files.exists(Path.of(BIN))) {
            System.out.println("No existe " + BIN + ". Ejecuta primero: java Main init");
            return;
        }
        List<Cliente> clientes = archivos.cargarBinario(BIN);
        System.out.println("Binario cargado: " + clientes.size() + " clientes");
        for (Cliente c : clientes) System.out.println("  " + c);
    }

    private static void ejecutarSaveBin(GestorArchivos archivos) throws IOException {
        List<Cliente> clientes = archivos.leerCSV(CSV);
        archivos.guardarBinario(BIN, clientes);
        System.out.println("Guardado: " + BIN + "  (" + clientes.size() + " clientes)");
    }

    private static void ejecutarBackup(BackupManager backups) throws IOException {
        if (!Files.exists(Path.of(BIN))) {
            System.out.println("No existe " + BIN + " para respaldar.");
            return;
        }
        Path destino = backups.crearBackup(BIN);
        System.out.println("Backup creado: " + destino);
    }

    private static void ejecutarListBackups(BackupManager backups) throws IOException {
        List<String> lista = backups.listarBackups();
        if (lista.isEmpty()) {
            System.out.println("No hay backups en " + backups.getDirectorio());
        } else {
            System.out.println("Backups disponibles en " + backups.getDirectorio() + ":");
            lista.stream().sorted().forEach(b -> System.out.println("  " + b));
        }
    }

    private static void ejecutarRestore(String[] args, BackupManager backups) throws IOException {
        if (args.length < 2) {
            System.err.println("Uso: java Main restore <nombre-backup>");
            System.err.println("Usa list-backups para ver opciones disponibles.");
            return;
        }
        backups.restaurarBackup(args[1], BIN);
        System.out.println("Restaurado: " + BIN + " <- " + args[1]);
    }

    private static void ejecutarReport(GestorArchivos archivos)
            throws IOException, ClassNotFoundException {
        // Carga desde binario (es el estado actual); fallback a CSV.
        List<Cliente> clientes;
        if (Files.exists(Path.of(BIN))) {
            clientes = archivos.cargarBinario(BIN);
        } else if (Files.exists(Path.of(CSV))) {
            clientes = archivos.leerCSV(CSV);
        } else {
            System.err.println("No hay datos cargados. Ejecuta primero: java Main init");
            return;
        }

        ReporteAlineado reporte = new ReporteAlineado();
        reporte.escribirArchivo(REPORTE, clientes);
        System.out.println("Reporte generado: " + REPORTE);
        System.out.println("---- Primera vista ----");
        // Vista previa: imprimir el reporte tambien en consola
        System.out.println(reporte.generar(clientes));
    }

    private static void ejecutarLog(String[] args, GestorArchivos archivos) throws IOException {
        if (args.length < 2) {
            System.err.println("Uso: java Main log <mensaje>");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) sb.append(' ');
            sb.append(args[i]);
        }
        String linea = "[" + GestorArchivos.timestampActual() + "] " + sb;
        archivos.escribirLineaTexto(LOG, linea);
        System.out.println("Log: " + linea);
    }

    // ---------- ayuda ----------

    private static void mostrarAyuda() {
        System.out.println("Practica 9 - Sistema de gestion de archivos");
        System.out.println();
        System.out.println("Uso: java Main <comando> [argumentos]");
        System.out.println();
        System.out.println("Comandos:");
        System.out.println("  init             Inicializa CSV y binario con 20 clientes");
        System.out.println("  load-csv         Lee CSV y muestra clientes");
        System.out.println("  load-bin         Lee binario y muestra clientes");
        System.out.println("  save-bin         Convierte CSV actual -> binario");
        System.out.println("  backup           Crea backup del binario con timestamp");
        System.out.println("  list-backups     Lista los backups disponibles");
        System.out.println("  restore <name>   Restaura un backup");
        System.out.println("  report           Genera reporte alineado en data/reporte.txt");
        System.out.println("  log <mensaje>    Agrega una linea al log de operaciones");
        System.out.println("  help             Muestra esta ayuda");
        System.out.println();
        System.out.println("Flujo tipico:");
        System.out.println("  java Main init");
        System.out.println("  java Main report");
        System.out.println("  java Main backup");
        System.out.println("  java Main list-backups");
    }
}
