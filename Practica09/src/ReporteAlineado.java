import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Práctica 9 — Elemento de Decision Propia.
 *
 * Exporta una lista de clientes a un reporte de texto con COLUMNAS
 * ALINEADAS, pensado para que lo lea una persona y no una maquina.
 *
 * Caso de uso vs CSV:
 *   - CSV es para INTERCAMBIO con sistemas (Excel, BD, otra app).
 *     Un humano abriendo un CSV en notepad ve una sopa ilegible.
 *   - Reporte alineado es para PERSONAS: el gerente lo imprime, lo
 *     pega en un email, lo revisa de un vistazo. Las columnas
 *     alineadas y el encabezado con totales hacen los datos
 *     escaneables a simple vista.
 *
 * Detalles del formato:
 *   - Cabecera con nombre del gimnasio, timestamp y conteo.
 *   - Encabezado de columnas + linea separadora.
 *   - Filas con padding fijo para alinear.
 *   - Pie con totales por categoria (Premium / Basica / VIP).
 *
 * Justificacion en el README.md.
 */
public class ReporteAlineado {

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Anchos de columna fijos. Calibrados a los datos del gimnasio.
    private static final int W_ID     = 5;
    private static final int W_NOMBRE = 32;
    private static final int W_EMAIL  = 28;
    private static final int W_TIPO   = 8;
    private static final int W_PESO   = 7;
    private static final int W_ESTADO = 8;
    private static final int ANCHO_TOTAL =
            W_ID + W_NOMBRE + W_EMAIL + W_TIPO + W_PESO + W_ESTADO + 5; // separadores

    /**
     * Genera el reporte como String. Se separa de la escritura al
     * archivo para que tambien se pueda usar en consola o en pruebas.
     */
    public String generar(List<Cliente> clientes) {
        StringBuilder sb = new StringBuilder();

        // -------- Cabecera --------
        sb.append("=".repeat(ANCHO_TOTAL)).append('\n');
        sb.append("  GIMNASIO - REPORTE DE CLIENTES\n");
        sb.append("  Generado:        ").append(LocalDateTime.now().format(TS)).append('\n');
        sb.append("  Total registros: ").append(clientes.size()).append('\n');
        sb.append("=".repeat(ANCHO_TOTAL)).append('\n');
        sb.append('\n');

        // -------- Encabezado de columnas --------
        sb.append(formato("ID", W_ID))
          .append(' ')
          .append(formato("NOMBRE", W_NOMBRE))
          .append(' ')
          .append(formato("EMAIL", W_EMAIL))
          .append(' ')
          .append(formato("TIPO", W_TIPO))
          .append(' ')
          .append(formato("PESO", W_PESO))
          .append(' ')
          .append(formato("ESTADO", W_ESTADO))
          .append('\n');

        sb.append("-".repeat(W_ID)).append(' ')
          .append("-".repeat(W_NOMBRE)).append(' ')
          .append("-".repeat(W_EMAIL)).append(' ')
          .append("-".repeat(W_TIPO)).append(' ')
          .append("-".repeat(W_PESO)).append(' ')
          .append("-".repeat(W_ESTADO))
          .append('\n');

        // -------- Filas --------
        for (Cliente c : clientes) {
            sb.append(formato(String.valueOf(c.getId()), W_ID)).append(' ')
              .append(formato(c.getNombreCompleto(), W_NOMBRE)).append(' ')
              .append(formato(c.getEmail(), W_EMAIL)).append(' ')
              .append(formato(c.getTipoMembresia().toString(), W_TIPO)).append(' ')
              .append(formato(String.format("%5.1f", c.getPesoKg()), W_PESO)).append(' ')
              .append(formato(c.esActivo() ? "activo" : "inactivo", W_ESTADO))
              .append('\n');
        }

        // -------- Pie con totales --------
        sb.append('\n');
        sb.append("-".repeat(ANCHO_TOTAL)).append('\n');
        sb.append("  Totales por tipo:\n");
        long basicos = clientes.stream()
                .filter(c -> c.getTipoMembresia() == Cliente.TipoMembresia.BASICA).count();
        long premium = clientes.stream()
                .filter(c -> c.getTipoMembresia() == Cliente.TipoMembresia.PREMIUM).count();
        long vip = clientes.stream()
                .filter(c -> c.getTipoMembresia() == Cliente.TipoMembresia.VIP).count();
        long activos = clientes.stream().filter(Cliente::esActivo).count();

        sb.append(String.format("    BASICA:  %d%n", basicos));
        sb.append(String.format("    PREMIUM: %d%n", premium));
        sb.append(String.format("    VIP:     %d%n", vip));
        sb.append('\n');
        sb.append(String.format("  Activos:   %d / %d%n", activos, clientes.size()));
        sb.append("=".repeat(ANCHO_TOTAL)).append('\n');

        return sb.toString();
    }

    /**
     * Escribe el reporte directamente al archivo. Usa try-with-resources
     * para garantizar el cierre.
     */
    public void escribirArchivo(String ruta, List<Cliente> clientes) throws IOException {
        String contenido = generar(clientes);
        try (BufferedWriter w = new BufferedWriter(new FileWriter(ruta))) {
            w.write(contenido);
        }
    }

    /**
     * Trunca o rellena con espacios un texto para que ocupe exactamente
     * `ancho` caracteres. Justifica a la izquierda (texto humano).
     */
    private static String formato(String texto, int ancho) {
        if (texto == null) texto = "";
        if (texto.length() >= ancho) {
            return texto.substring(0, ancho);
        }
        return texto + " ".repeat(ancho - texto.length());
    }
}
