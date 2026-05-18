package com.gympos;

import com.gympos.controller.AppContext;
import com.gympos.controller.MainController;
import com.gympos.util.Loggers;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * GymPOS - Punto de entrada de la aplicacion.
 *
 * Flujo:
 *   1. JavaFX llama a start(Stage).
 *   2. Inicializamos AppContext (carga config, abre archivos, etc).
 *   3. Construimos MainController con el contexto.
 *   4. Mostramos la ventana principal.
 *
 * Si ocurre un error CRITICO durante la inicializacion (config corrupto,
 * archivos ilegibles), mostramos un dialogo de error y abortamos sin
 * dejar la app en estado a medias.
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1) Inicializar el contexto (incluye cargar todos los datos)
            AppContext ctx = AppContext.crear();

            // 2) Armar y mostrar la ventana principal
            MainController controller = new MainController(primaryStage, ctx);
            controller.inicializar();

            primaryStage.show();
            Loggers.info("=== GymPOS listo (UI mostrada) ===");

        } catch (Exception e) {
            // Si la inicializacion falla, mostramos un error claro y
            // salimos. No queremos llegar a la UI con servicios nulos.
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Error al iniciar GymPOS");
            a.setHeaderText("La aplicacion no pudo iniciarse");
            a.setContentText(
                    "Causa: " + e.getClass().getSimpleName() + "\n"
                  + "Detalle: " + e.getMessage() + "\n\n"
                  + "Revisa data/operaciones.log para mas informacion.");
            a.showAndWait();
            javafx.application.Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
