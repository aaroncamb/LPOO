package com.gympos.service;

import com.gympos.model.Cliente;
import com.gympos.model.Membresia;
import com.gympos.model.RegistroAcceso;
import com.gympos.persistence.GestorArchivos;
import com.gympos.util.Loggers;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * GymPOS - Modulo de generacion de reportes.
 *
 * Produce reportes de texto con COLUMNAS ALINEADAS (formato de P9):
 * cabecera con datos del gimnasio + secciones + pie con totales.
 *
 * Este modulo expone metodos SINCRONICOS para generar reportes. Los
 * reportes que toman tiempo (10s+ con datasets grandes) se ejecutan
 * en background usando TareaReporte (en concurrency/) que envuelve a
 * esta clase en un javafx.concurrent.Task. Asi el FXAT no se bloquea.
 */
public class GeneradorReportes {

    private static final DateTimeFormatter TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final GestionClientes clientes;
    private final SistemaMembresias membresias;
    private final ProcesadorPagos pagos;
    private final ControlAcceso acceso;
    private final GestorArchivos archivos;
    private final String nombreGym;

    public GeneradorReportes(GestionClientes clientes, SistemaMembresias membresias,
                              ProcesadorPagos pagos, ControlAcceso acceso,
                              GestorArchivos archivos, String nombreGym) {
        this.clientes = clientes;
        this.membresias = membresias;
        this.pagos = pagos;
        this.acceso = acceso;
        this.archivos = archivos;
        this.nombreGym = nombreGym != null ? nombreGym : "Gimnasio";
    }

    // ============================================================
    //   REPORTE GENERAL
    // ============================================================

    /**
     * Genera un reporte general del estado del gimnasio:
     *   - Cantidad de clientes por tipo de membresia
     *   - Membresias por vencer (proximos 7 dias)
     *   - Membresias ya vencidas
     *   - Ingresos del periodo (tickets emitidos)
     *   - Accesos del dia
     *
     * Lo escribe a un archivo TXT y devuelve la ruta.
     */
    public String generarReporteGeneral() throws IOException {
        StringBuilder sb = new StringBuilder();

        cabecera(sb, "REPORTE GENERAL");
        seccionClientes(sb);
        seccionMembresiasPorVencer(sb);
        seccionMembresiasVencidas(sb);
        seccionIngresos(sb);
        seccionAccesos(sb);
        pie(sb);

        return guardarEnArchivo(sb.toString(), "reporte_general");
    }

    /**
     * Reporte focalizado en ingresos: lista todos los tickets emitidos
     * con sus montos.
     */
    public String generarReporteIngresos() throws IOException {
        StringBuilder sb = new StringBuilder();

        cabecera(sb, "REPORTE DE INGRESOS");

        List<ProcesadorPagos.Ticket> tickets = pagos.ticketsDelDia();
        sb.append(String.format("Total de tickets emitidos: %d%n", tickets.size()));
        sb.append(String.format("Ingreso bruto total:       $%.2f%n%n",
                pagos.ingresoBrutoTotal()));

        sb.append(formato("ID",        5)).append(' ')
          .append(formato("CONCEPTO",  25)).append(' ')
          .append(formato("SUBTOTAL",  10)).append(' ')
          .append(formato("DESCUENTO", 10)).append(' ')
          .append(formato("IVA",       10)).append(' ')
          .append(formato("TOTAL",     10)).append(' ')
          .append(formato("METODO",    13))
          .append('\n');

        sb.append("-".repeat(5)).append(' ')
          .append("-".repeat(25)).append(' ')
          .append("-".repeat(10)).append(' ')
          .append("-".repeat(10)).append(' ')
          .append("-".repeat(10)).append(' ')
          .append("-".repeat(10)).append(' ')
          .append("-".repeat(13))
          .append('\n');

        for (ProcesadorPagos.Ticket t : tickets) {
            sb.append(formato(String.valueOf(t.getIdTicket()), 5)).append(' ')
              .append(formato(t.getConcepto(), 25)).append(' ')
              .append(formato(String.format("$%.2f", t.getSubtotal()), 10)).append(' ')
              .append(formato(String.format("$%.2f", t.getDescuento()), 10)).append(' ')
              .append(formato(String.format("$%.2f", t.getImpuestos()), 10)).append(' ')
              .append(formato(String.format("$%.2f", t.getTotal()), 10)).append(' ')
              .append(formato(t.getMetodoPago(), 13))
              .append('\n');
        }

        pie(sb);
        return guardarEnArchivo(sb.toString(), "reporte_ingresos");
    }

    /**
     * Reporte de asistencia del dia: cuantos clientes entraron y salieron.
     */
    public String generarReporteAsistencia() throws IOException {
        StringBuilder sb = new StringBuilder();

        cabecera(sb, "REPORTE DE ASISTENCIA");

        List<RegistroAcceso> accesos = acceso.registrosDelDia(LocalDate.now());
        long entradas = accesos.stream()
                .filter(a -> a.getTipo() == RegistroAcceso.TipoMovimiento.ENTRADA)
                .count();
        long salidas = accesos.size() - entradas;

        sb.append(String.format("Fecha:    %s%n", LocalDate.now()));
        sb.append(String.format("Entradas: %d%n", entradas));
        sb.append(String.format("Salidas:  %d%n%n", salidas));

        sb.append(formato("ID",        6)).append(' ')
          .append(formato("HORA",      9)).append(' ')
          .append(formato("CLIENTE",   8)).append(' ')
          .append(formato("TIPO",      9)).append(' ')
          .append(formato("TORNIQUETE",15))
          .append('\n');

        sb.append("-".repeat(6)).append(' ')
          .append("-".repeat(9)).append(' ')
          .append("-".repeat(8)).append(' ')
          .append("-".repeat(9)).append(' ')
          .append("-".repeat(15))
          .append('\n');

        DateTimeFormatter horaFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        for (RegistroAcceso r : accesos) {
            sb.append(formato(String.valueOf(r.getIdRegistro()), 6)).append(' ')
              .append(formato(r.getMomento().format(horaFmt), 9)).append(' ')
              .append(formato("#" + r.getIdCliente(), 8)).append(' ')
              .append(formato(r.getTipo().toString(), 9)).append(' ')
              .append(formato(r.getNumeroTorniquete(), 15))
              .append('\n');
        }

        pie(sb);
        return guardarEnArchivo(sb.toString(), "reporte_asistencia");
    }

    // ============================================================
    //   COMPONENTES DEL FORMATO
    // ============================================================

    private void cabecera(StringBuilder sb, String tituloReporte) {
        String separador = "=".repeat(90);
        sb.append(separador).append('\n');
        sb.append("  ").append(nombreGym).append(" - ").append(tituloReporte).append('\n');
        sb.append("  Generado: ")
          .append(LocalDateTime.now().format(TS_FORMAT)).append('\n');
        sb.append(separador).append('\n').append('\n');
    }

    private void pie(StringBuilder sb) {
        sb.append('\n').append("-".repeat(90)).append('\n');
        sb.append("  Fin del reporte\n");
        sb.append("=".repeat(90)).append('\n');
    }

    private void seccionClientes(StringBuilder sb) {
        sb.append("--- CLIENTES ---\n");
        sb.append(String.format("  Total:    %d%n", clientes.total()));
        Map<Cliente.TipoMembresia, Long> conteo = clientes.conteoPorTipo();
        for (Cliente.TipoMembresia t : Cliente.TipoMembresia.values()) {
            sb.append(String.format("    %-8s %d%n", t, conteo.getOrDefault(t, 0L)));
        }
        sb.append(String.format("  Activos:  %d%n%n", clientes.filtrarActivos().size()));
    }

    private void seccionMembresiasPorVencer(StringBuilder sb) {
        List<Membresia> porVencer = membresias.porVencerEn(7);
        sb.append("--- MEMBRESIAS POR VENCER (proximos 7 dias) ---\n");
        sb.append(String.format("  Total: %d%n", porVencer.size()));
        for (Membresia m : porVencer) {
            sb.append("    ").append(m).append('\n');
        }
        sb.append('\n');
    }

    private void seccionMembresiasVencidas(StringBuilder sb) {
        List<Membresia> vencidas = membresias.vencidas();
        sb.append("--- MEMBRESIAS VENCIDAS ---\n");
        sb.append(String.format("  Total: %d%n", vencidas.size()));
        for (Membresia m : vencidas) {
            sb.append("    ").append(m).append('\n');
        }
        sb.append('\n');
    }

    private void seccionIngresos(StringBuilder sb) {
        sb.append("--- INGRESOS DEL PERIODO ---\n");
        sb.append(String.format("  Tickets emitidos: %d%n", pagos.totalTicketsEmitidos()));
        sb.append(String.format("  Ingreso bruto:    $%.2f%n%n", pagos.ingresoBrutoTotal()));
    }

    private void seccionAccesos(StringBuilder sb) {
        sb.append("--- ACCESOS DEL DIA ---\n");
        sb.append(String.format("  Entradas hoy: %d%n", acceso.totalEntradasHoy()));
        sb.append('\n');
    }

    private String formato(String texto, int ancho) {
        if (texto == null) texto = "";
        if (texto.length() >= ancho) return texto.substring(0, ancho);
        return texto + " ".repeat(ancho - texto.length());
    }

    private String guardarEnArchivo(String contenido, String prefijoNombre) throws IOException {
        String nombre = prefijoNombre + "_" + GestorArchivos.timestampActual() + ".txt";
        String ruta = "data/reportes/" + nombre;
        archivos.asegurarDirectorio("data/reportes");
        archivos.escribirTexto(ruta, contenido);
        Loggers.info("Reporte generado: " + ruta);
        return ruta;
    }
}
