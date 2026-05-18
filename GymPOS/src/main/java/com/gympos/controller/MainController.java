package com.gympos.controller;

import com.gympos.concurrency.TareaBackup;
import com.gympos.util.Loggers;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * GymPOS - Controlador de la ventana principal.
 *
 * Es el contenedor que ensambla todas las pestañas. Cada pestaña tiene
 * su propio controller que devuelve un Node listo para insertar.
 *
 * Se encarga de:
 *   - Construir el menu superior (Archivo, Ayuda) con atajos.
 *   - Crear las 4 pestañas: Clientes, Membresias, Clases, Reportes.
 *   - Manejar el cierre: guardar datos y opcionalmente crear backup.
 */
public class MainController {

    private final Stage stage;
    private final AppContext ctx;

    public MainController(Stage stage, AppContext ctx) {
        this.stage = stage;
        this.ctx = ctx;
    }

    public void inicializar() {
        // -------- Menu superior --------
        MenuBar menuBar = construirMenu();

        // -------- TabPane con las 4 pestañas --------
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        ClientesController clientes = new ClientesController(
                stage, ctx.getGestionClientes());
        Tab tabClientes = new Tab("Clientes", clientes.construir());

        MembresiasController membresias = new MembresiasController(
                stage, ctx.getSistemaMembresias(), ctx.getGestionClientes(),
                ctx.getProcesadorPagos(),
                ctx.getConfig().getDouble("fiscal.iva", 0.16));
        Tab tabMembresias = new Tab("Membresias", membresias.construir());

        ClasesController clases = new ClasesController(
                stage, ctx.getClasesGrupales(), ctx.getGestionClientes());
        Tab tabClases = new Tab("Clases Grupales", clases.construir());

        ReportesController reportes = new ReportesController(
                stage, ctx.getGeneradorReportes());
        Tab tabReportes = new Tab("Reportes", reportes.construir());

        tabs.getTabs().addAll(tabClientes, tabMembresias, tabClases, tabReportes);

        // -------- Layout --------
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(tabs);

        Scene scene = new Scene(root, 1024, 720);
        scene.getStylesheets().add(
                getClass().getResource("/styles.css").toExternalForm());

        stage.setTitle(ctx.getConfig().getString("gym.nombre", "GymPOS")
                + " - Punto de Venta v1.0");
        stage.setScene(scene);

        // Al cerrar la ventana: guardar datos
        stage.setOnCloseRequest(e -> {
            // Persistir
            try {
                ctx.guardarTodo();
            } catch (IOException ex) {
                Loggers.error("No se pudo guardar al cerrar: " + ex.getMessage());
            }

            // Backup automatico si esta habilitado
            if (ctx.getConfig().getBoolean("backup.automatico.al.cerrar", true)) {
                e.consume();   // pausa el cierre para esperar al backup
                ejecutarBackupYCerrar();
            }
        });
    }

    private MenuBar construirMenu() {
        Menu archivo = new Menu("Archivo");

        MenuItem guardar = new MenuItem("Guardar todo");
        guardar.setAccelerator(KeyCombination.valueOf("Ctrl+S"));
        guardar.setOnAction(e -> {
            try {
                ctx.guardarTodo();
                infoAlerta("Estado guardado correctamente.");
            } catch (IOException ex) {
                errorAlerta("No se pudo guardar: " + ex.getMessage());
            }
        });

        MenuItem backup = new MenuItem("Crear backup ahora");
        backup.setAccelerator(KeyCombination.valueOf("Ctrl+B"));
        backup.setOnAction(e -> ejecutarBackupSinCerrar());

        MenuItem salir = new MenuItem("Salir");
        salir.setAccelerator(KeyCombination.valueOf("Ctrl+Q"));
        salir.setOnAction(e -> stage.fireEvent(
                new javafx.stage.WindowEvent(stage,
                        javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST)));

        archivo.getItems().addAll(guardar, backup, new javafx.scene.control.SeparatorMenuItem(), salir);

        Menu ayuda = new Menu("Ayuda");
        MenuItem acerca = new MenuItem("Acerca de");
        acerca.setOnAction(e -> mostrarAcercaDe());
        ayuda.getItems().add(acerca);

        return new MenuBar(archivo, ayuda);
    }

    /**
     * Ejecuta backup en background mostrando barra de progreso, y al
     * terminar cierra la aplicacion.
     */
    private void ejecutarBackupYCerrar() {
        TareaBackup tarea = ctx.tareaBackupCompleto();

        Stage progresoStage = construirVentanaProgreso(tarea, "Creando backup antes de salir");
        progresoStage.show();

        tarea.setOnSucceeded(ev -> {
            progresoStage.close();
            Loggers.info("=== GymPOS cerrando (backup creado) ===");
            stage.close();
        });
        tarea.setOnFailed(ev -> {
            progresoStage.close();
            Loggers.warn("Backup fallo al cerrar: " + tarea.getException());
            stage.close();
        });

        Thread t = new Thread(tarea, "backup-al-cerrar");
        t.setDaemon(true);
        t.start();
    }

    private void ejecutarBackupSinCerrar() {
        TareaBackup tarea = ctx.tareaBackupCompleto();
        Stage progresoStage = construirVentanaProgreso(tarea, "Creando backup");
        progresoStage.show();

        tarea.setOnSucceeded(ev -> {
            progresoStage.close();
            infoAlerta(tarea.getValue() + " backups creados.");
        });
        tarea.setOnFailed(ev -> {
            progresoStage.close();
            errorAlerta("Backup fallo: " + tarea.getException());
        });

        Thread t = new Thread(tarea, "backup-manual");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Construye una ventana de progreso vinculada a un Task. Util para
     * operaciones bloqueantes que tarden segundos.
     */
    private Stage construirVentanaProgreso(javafx.concurrent.Task<?> tarea, String titulo) {
        ProgressBar barra = new ProgressBar(0);
        barra.setPrefWidth(300);
        barra.progressProperty().bind(tarea.progressProperty());

        Label mensaje = new Label();
        mensaje.textProperty().bind(tarea.messageProperty());

        VBox vb = new VBox(10, new Label(titulo), barra, mensaje);
        vb.setPadding(new javafx.geometry.Insets(20));

        Scene sc = new Scene(vb);
        sc.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        Stage st = new Stage();
        st.initOwner(stage);
        st.initModality(javafx.stage.Modality.WINDOW_MODAL);
        st.setTitle(titulo);
        st.setScene(sc);
        st.setResizable(false);
        return st;
    }

    private void mostrarAcercaDe() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.initOwner(stage);
        a.setTitle("Acerca de GymPOS");
        a.setHeaderText(ctx.getConfig().getString("gym.nombre", "GymPOS")
                + " - Sistema de Punto de Venta");
        a.setContentText(
                "Version 1.0 - Proyecto Integrador LPOO\n\n"
              + "Modulos:\n"
              + "  - Gestion de clientes (CRUD + busqueda)\n"
              + "  - Sistema de membresias (BASICA, PREMIUM, VIP)\n"
              + "  - Procesador de pagos con descuentos y puntos\n"
              + "  - Calendario de clases grupales\n"
              + "  - Generador de reportes (en background)\n"
              + "  - Control de acceso\n"
              + "  - Backups automaticos al cerrar\n\n"
              + "Atajos:\n"
              + "  Ctrl+S - Guardar todo\n"
              + "  Ctrl+B - Crear backup ahora\n"
              + "  Ctrl+Q - Salir");
        a.showAndWait();
    }

    private void infoAlerta(String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.initOwner(stage);
        a.setTitle("GymPOS");
        a.setHeaderText(null);
        a.setContentText(mensaje);
        a.showAndWait();
    }

    private void errorAlerta(String mensaje) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.initOwner(stage);
        a.setTitle("Error");
        a.setHeaderText("Ocurrio un problema");
        a.setContentText(mensaje);
        a.showAndWait();
    }
}
