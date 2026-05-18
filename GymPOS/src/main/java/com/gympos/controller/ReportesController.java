package com.gympos.controller;

import com.gympos.concurrency.TareaReporte;
import com.gympos.service.GeneradorReportes;
import com.gympos.view.BotonAccion;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * GymPOS - Controller de la pestaña Reportes.
 *
 * Ofrece tres tipos de reportes (general, ingresos, asistencia) que
 * se generan en HILO DE FONDO usando TareaReporte (javafx.concurrent.Task).
 *
 * Esta pestaña demuestra el multithreading que pide la rubrica: la UI
 * no se congela mientras el reporte se procesa (incluso si fuera grande
 * con muchos datos), y el progreso se muestra con una ProgressBar
 * vinculada al Task.
 *
 * Tras generar el reporte, muestra una vista previa en un TextArea y
 * permite abrir el archivo en el visor del sistema.
 */
public class ReportesController {

    private final Window owner;
    private final GeneradorReportes generador;

    private final ProgressBar barraProgreso = new ProgressBar(0);
    private final Label etiquetaEstado = new Label("Listo para generar reporte.");
    private final TextArea vistaPrevia = new TextArea();
    private final Label statusbar = new Label();

    private String rutaUltimoReporte = null;

    public ReportesController(Window owner, GeneradorReportes generador) {
        this.owner = owner;
        this.generador = generador;
    }

    public BorderPane construir() {
        // Toolbar con botones para cada tipo de reporte
        BotonAccion btnGeneral    = BotonAccion.primario("Reporte general");
        BotonAccion btnIngresos   = BotonAccion.primario("Reporte de ingresos");
        BotonAccion btnAsistencia = BotonAccion.primario("Reporte de asistencia");
        BotonAccion btnAbrir      = BotonAccion.secundario("Abrir ultimo reporte");

        btnGeneral.setOnAction(e -> generar(TareaReporte.TipoReporte.GENERAL));
        btnIngresos.setOnAction(e -> generar(TareaReporte.TipoReporte.INGRESOS));
        btnAsistencia.setOnAction(e -> generar(TareaReporte.TipoReporte.ASISTENCIA));
        btnAbrir.setOnAction(e -> abrirUltimoReporte());

        HBox toolbar = new HBox(10, btnGeneral, btnIngresos, btnAsistencia, btnAbrir);
        toolbar.setPadding(new Insets(10));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("toolbar");

        // Area de progreso
        barraProgreso.setPrefWidth(Double.MAX_VALUE);
        VBox panelProgreso = new VBox(5, etiquetaEstado, barraProgreso);
        panelProgreso.setPadding(new Insets(10));

        // Vista previa
        vistaPrevia.setEditable(false);
        vistaPrevia.setFont(javafx.scene.text.Font.font("Consolas", 11));
        vistaPrevia.setText("Aqui aparecera el contenido del reporte generado.");
        vistaPrevia.getStyleClass().add("vista-previa-reporte");

        VBox centro = new VBox(panelProgreso, vistaPrevia);
        VBox.setVgrow(vistaPrevia, javafx.scene.layout.Priority.ALWAYS);

        statusbar.setPadding(new Insets(5, 10, 5, 10));
        statusbar.getStyleClass().add("statusbar");
        statusbar.setText("Reportes se guardan en data/reportes/");

        BorderPane root = new BorderPane();
        root.setTop(toolbar);
        root.setCenter(centro);
        root.setBottom(statusbar);
        return root;
    }

    /**
     * Lanza la generacion del reporte en hilo de fondo.
     * El usuario sigue pudiendo usar la UI durante la generacion.
     */
    private void generar(TareaReporte.TipoReporte tipo) {
        TareaReporte tarea = new TareaReporte(generador, tipo);

        // Bindings: la UI se actualiza automaticamente
        barraProgreso.progressProperty().bind(tarea.progressProperty());
        etiquetaEstado.textProperty().bind(tarea.messageProperty());

        // Cuando termine exitosamente
        tarea.setOnSucceeded(e -> {
            barraProgreso.progressProperty().unbind();
            etiquetaEstado.textProperty().unbind();
            barraProgreso.setProgress(1.0);

            String ruta = tarea.getValue();
            rutaUltimoReporte = ruta;
            etiquetaEstado.setText("Reporte generado: " + ruta);
            statusbar.setText("Ultimo reporte: " + ruta);

            // Cargar vista previa
            try {
                String contenido = Files.readString(Path.of(ruta));
                vistaPrevia.setText(contenido);
            } catch (IOException ex) {
                vistaPrevia.setText("(no se pudo cargar vista previa: "
                        + ex.getMessage() + ")");
            }
        });

        // Si fallo
        tarea.setOnFailed(e -> {
            barraProgreso.progressProperty().unbind();
            etiquetaEstado.textProperty().unbind();
            barraProgreso.setProgress(0);
            Throwable cause = tarea.getException();
            etiquetaEstado.setText("Error al generar: "
                    + (cause == null ? "desconocido" : cause.getMessage()));
            alerta("No se pudo generar el reporte. Revisa el log.");
        });

        // Lanzar la tarea en un Thread (daemon para que no impida cerrar la app)
        Thread t = new Thread(tarea, "tarea-reporte-" + tipo);
        t.setDaemon(true);
        t.start();
    }

    private void abrirUltimoReporte() {
        if (rutaUltimoReporte == null) {
            alerta("Aun no se ha generado ningun reporte en esta sesion.");
            return;
        }
        try {
            // Desktop.open() lanza el visor por defecto del SO para .txt
            Desktop.getDesktop().open(new File(rutaUltimoReporte));
        } catch (Exception ex) {
            alerta("No se pudo abrir el archivo: " + ex.getMessage());
        }
    }

    private void alerta(String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.initOwner(owner);
        a.setTitle("Reportes");
        a.setHeaderText(null);
        a.setContentText(mensaje);
        a.showAndWait();
    }
}
