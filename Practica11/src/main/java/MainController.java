import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Práctica 11 — Controlador de la ventana principal.
 *
 * Es el "cerebro" de la UI:
 *   - MenuBar (Archivo / Clientes / Ayuda) con accesos rapidos.
 *   - TextField de busqueda que filtra la tabla en tiempo real.
 *   - TableView con los clientes (datos dinamicos).
 *   - Botones de Nuevo / Editar / Eliminar.
 *   - Statusbar abajo con el conteo.
 *
 * EVENTOS implementados (mouse + teclado):
 *   - doble-click en una fila -> editar
 *   - tecla Delete -> eliminar el seleccionado
 *   - tecla Enter sobre la tabla -> editar el seleccionado
 *   - F1 -> abrir "Acerca de"
 *   - Ctrl+N -> nuevo cliente
 *   - Ctrl+E -> editar seleccionado
 *
 * El filtrado interactivo usa FilteredList<Cliente>: cada cambio del
 * campo de busqueda actualiza el Predicate y la tabla se refresca sola.
 */
public class MainController {

    private final Stage stage;

    /** Lista observable raiz. Los cambios aqui se reflejan automaticamente. */
    private final ObservableList<Cliente> datos = FXCollections.observableArrayList();

    /** Lista filtrada para el filtrado interactivo (DECISION PROPIA). */
    private final FilteredList<Cliente> datosFiltrados = new FilteredList<>(datos, p -> true);

    private final TableView<Cliente> tabla = new TableView<>();
    private final TextField campoBusqueda = new TextField();
    private final Label statusbar = new Label();

    public MainController(Stage stage) {
        this.stage = stage;
    }

    /** Construye la escena y la asigna al stage. */
    public void inicializar() {
        // -------- Cargar datos iniciales --------
        datos.addAll(DatosPrueba.generar());

        // -------- Tabla --------
        construirTabla();

        // -------- Toolbar de busqueda + botones --------
        HBox toolbar = construirToolbar();

        // -------- Menu --------
        MenuBar menuBar = construirMenu();

        // -------- Statusbar --------
        statusbar.setPadding(new Insets(5, 10, 5, 10));
        statusbar.getStyleClass().add("statusbar");
        actualizarStatusbar();

        // -------- Layout --------
        BorderPane root = new BorderPane();
        root.setTop(new VBox(menuBar, toolbar));
        root.setCenter(tabla);
        root.setBottom(statusbar);

        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(
                getClass().getResource("/styles.css").toExternalForm());

        // -------- Atajos globales --------
        scene.getAccelerators().put(
                KeyCombination.valueOf("F1"), this::accionAcercaDe);
        scene.getAccelerators().put(
                KeyCombination.valueOf("Ctrl+N"), this::accionNuevo);
        scene.getAccelerators().put(
                KeyCombination.valueOf("Ctrl+E"), this::accionEditarSeleccionado);

        stage.setTitle("GymPOS - Gestion de Clientes (P11)");
        stage.setScene(scene);
    }

    // ---------------------------------------------------------------
    //   CONSTRUCCION DE COMPONENTES
    // ---------------------------------------------------------------

    private MenuBar construirMenu() {
        Menu archivo = new Menu("Archivo");
        MenuItem salir = new MenuItem("Salir");
        salir.setAccelerator(KeyCombination.valueOf("Ctrl+Q"));
        salir.setOnAction(e -> stage.close());
        archivo.getItems().addAll(salir);

        Menu clientes = new Menu("Clientes");
        MenuItem nuevo = new MenuItem("Nuevo cliente...");
        nuevo.setAccelerator(KeyCombination.valueOf("Ctrl+N"));
        nuevo.setOnAction(e -> accionNuevo());

        MenuItem editar = new MenuItem("Editar seleccionado...");
        editar.setAccelerator(KeyCombination.valueOf("Ctrl+E"));
        editar.setOnAction(e -> accionEditarSeleccionado());

        MenuItem eliminar = new MenuItem("Eliminar seleccionado");
        eliminar.setAccelerator(KeyCombination.valueOf("Delete"));
        eliminar.setOnAction(e -> accionEliminarSeleccionado());

        clientes.getItems().addAll(nuevo, editar, eliminar);

        Menu ayuda = new Menu("Ayuda");
        MenuItem acerca = new MenuItem("Acerca de...");
        acerca.setAccelerator(KeyCombination.valueOf("F1"));
        acerca.setOnAction(e -> accionAcercaDe());
        ayuda.getItems().add(acerca);

        return new MenuBar(archivo, clientes, ayuda);
    }

    private HBox construirToolbar() {
        Label etiqueta = new Label("Buscar:");
        campoBusqueda.setPromptText("nombre o email...");
        campoBusqueda.setPrefWidth(280);
        campoBusqueda.getStyleClass().add("campo-busqueda");

        // ===========================================================
        // DECISION PROPIA: filtrado interactivo
        // ===========================================================
        // Cada cambio del texto actualiza el Predicate de la FilteredList.
        // La tabla, vinculada a la FilteredList, se refresca sola.
        campoBusqueda.textProperty().addListener((obs, viejo, nuevo) -> {
            datosFiltrados.setPredicate(cliente -> {
                if (nuevo == null || nuevo.isBlank()) return true;
                String aguja = nuevo.trim().toLowerCase();
                return cliente.getNombreCompleto().toLowerCase().contains(aguja)
                    || cliente.getEmail().toLowerCase().contains(aguja);
            });
            actualizarStatusbar();
        });

        BotonAccion btnNuevo    = BotonAccion.primario("Nuevo");
        BotonAccion btnEditar   = BotonAccion.secundario("Editar");
        BotonAccion btnEliminar = BotonAccion.peligro("Eliminar");
        btnNuevo.setOnAction(e -> accionNuevo());
        btnEditar.setOnAction(e -> accionEditarSeleccionado());
        btnEliminar.setOnAction(e -> accionEliminarSeleccionado());

        HBox toolbar = new HBox(10, etiqueta, campoBusqueda,
                btnNuevo, btnEditar, btnEliminar);
        toolbar.setPadding(new Insets(10));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("toolbar");
        return toolbar;
    }

    private void construirTabla() {
        TableColumn<Cliente, Number> colId = new TableColumn<>("Id");
        colId.setCellValueFactory(d -> d.getValue().idProperty());
        colId.setPrefWidth(60);

        TableColumn<Cliente, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(d -> d.getValue().nombreCompletoProperty());
        colNombre.setPrefWidth(220);

        TableColumn<Cliente, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(d -> d.getValue().emailProperty());
        colEmail.setPrefWidth(220);

        TableColumn<Cliente, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getTipoMembresia().toString()));
        colTipo.setPrefWidth(80);

        TableColumn<Cliente, Number> colPeso = new TableColumn<>("Peso (kg)");
        colPeso.setCellValueFactory(d -> d.getValue().pesoKgProperty());
        colPeso.setPrefWidth(80);

        TableColumn<Cliente, String> colFecha = new TableColumn<>("Registro");
        colFecha.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getFechaRegistro().toString()));
        colFecha.setPrefWidth(110);

        tabla.getColumns().add(colId);
        tabla.getColumns().add(colNombre);
        tabla.getColumns().add(colEmail);
        tabla.getColumns().add(colTipo);
        tabla.getColumns().add(colPeso);
        tabla.getColumns().add(colFecha);
        tabla.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // -------- Conectar FilteredList -> SortedList -> Tabla --------
        // SortedList permite que las columnas sean ordenables al hacer
        // click en el encabezado.
        SortedList<Cliente> ordenable = new SortedList<>(datosFiltrados);
        ordenable.comparatorProperty().bind(tabla.comparatorProperty());
        tabla.setItems(ordenable);

        // -------- EVENTOS de mouse y teclado --------
        // Doble-click en una fila: abrir editor
        tabla.setOnMouseClicked(ev -> {
            if (ev.getButton() == MouseButton.PRIMARY && ev.getClickCount() == 2) {
                accionEditarSeleccionado();
            }
        });

        // Tecla en la tabla: Delete elimina, Enter edita
        tabla.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.DELETE) {
                accionEliminarSeleccionado();
            } else if (ev.getCode() == KeyCode.ENTER) {
                accionEditarSeleccionado();
            }
        });
    }

    // ---------------------------------------------------------------
    //   ACCIONES
    // ---------------------------------------------------------------

    private void accionNuevo() {
        Optional<Cliente> r = new ClienteFormulario(stage, null).mostrar();
        r.ifPresent(c -> {
            if (existeId(c.getId())) {
                mostrarAlerta(Alert.AlertType.WARNING,
                        "Id duplicado",
                        "Ya existe un cliente con id " + c.getId() + ".");
            } else {
                datos.add(c);
                tabla.getSelectionModel().select(c);
                actualizarStatusbar();
            }
        });
    }

    private void accionEditarSeleccionado() {
        Cliente sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Sin seleccion",
                    "Selecciona primero un cliente de la tabla.");
            return;
        }
        new ClienteFormulario(stage, sel).mostrar();
        tabla.refresh();
        actualizarStatusbar();
    }

    private void accionEliminarSeleccionado() {
        Cliente sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Sin seleccion",
                    "Selecciona primero un cliente de la tabla.");
            return;
        }

        // Dialogo modal de confirmacion (entregable: dialogo modal)
        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION);
        confirmar.initOwner(stage);
        confirmar.setTitle("Confirmar eliminacion");
        confirmar.setHeaderText("Eliminar cliente?");
        confirmar.setContentText("Vas a eliminar a:\n  " + sel.getNombreCompleto()
                + "\n  (" + sel.getEmail() + ")\n\nEsta accion no se puede deshacer.");
        confirmar.getButtonTypes().setAll(ButtonType.YES, ButtonType.CANCEL);

        Optional<ButtonType> respuesta = confirmar.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.YES) {
            datos.remove(sel);
            actualizarStatusbar();
        }
    }

    private void accionAcercaDe() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.initOwner(stage);
        info.setTitle("Acerca de");
        info.setHeaderText("GymPOS - P11 JavaFX");
        info.setContentText(
                "Gestion de clientes del gimnasio.\n"
              + "LPOO Practica 11.\n\n"
              + "Atajos:\n"
              + "  Ctrl+N - Nuevo cliente\n"
              + "  Ctrl+E - Editar seleccionado\n"
              + "  Delete - Eliminar seleccionado\n"
              + "  Doble-click en fila - Editar\n"
              + "  F1 - Esta ayuda\n"
              + "  Ctrl+Q - Salir");
        info.showAndWait();
    }

    // ---------------------------------------------------------------
    //   HELPERS
    // ---------------------------------------------------------------

    private boolean existeId(int id) {
        return datos.stream().anyMatch(c -> c.getId() == id);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert a = new Alert(tipo);
        a.initOwner(stage);
        a.setTitle(titulo);
        a.setHeaderText(titulo);
        a.setContentText(contenido);
        a.showAndWait();
    }

    private void actualizarStatusbar() {
        String texto = String.format("Mostrando %d de %d clientes",
                datosFiltrados.size(), datos.size());
        statusbar.setText(texto);
    }
}
