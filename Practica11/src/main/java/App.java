import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Práctica 11 — Punto de entrada de la aplicacion JavaFX.
 *
 * Extender Application es el patron estandar de JavaFX. El framework
 * llama a init() (opcional, antes de mostrar UI), luego a start(stage)
 * cuando la plataforma esta lista para crear la UI, y finalmente stop()
 * al cerrar.
 *
 * El metodo main() solo redirige a Application.launch() que es lo que
 * realmente arranca el toolkit grafico.
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Toda la construccion de la UI vive en MainController
        // para mantener App.java minimalista.
        MainController controller = new MainController(primaryStage);
        controller.inicializar();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
