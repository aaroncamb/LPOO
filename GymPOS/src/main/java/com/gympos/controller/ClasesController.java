package com.gympos.controller;

import com.gympos.exceptions.CupoExcedidoException;
import com.gympos.model.ClaseGrupal;
import com.gympos.model.Cliente;
import com.gympos.service.GestionClientes;
import com.gympos.view.BotonAccion;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Window;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * GymPOS - Controller de la pestaña Clases Grupales.
 *
 * Muestra el calendario de clases con su instructor, horario, cupo
 * actual y disponibilidad. Permite inscribir clientes a una clase
 * (validando que tengan membresia que incluya clases grupales o
 * cobrandolas como concepto suelto).
 */
public class ClasesController {

    private static final DateTimeFormatter FMT_HORARIO =
            DateTimeFormatter.ofPattern("EEE dd/MMM HH:mm");

    private final Window owner;
    private final List<ClaseGrupal> clases;
    private final GestionClientes gestionClientes;

    private final ObservableList<ClaseGrupal> datos = FXCollections.observableArrayList();
    private final TableView<ClaseGrupal> tabla = new TableView<>();
    private final Label statusbar = new Label();

    public ClasesController(Window owner, List<ClaseGrupal> clases,
                            GestionClientes gestionClientes) {
        this.owner = owner;
        this.clases = clases;
        this.gestionClientes = gestionClientes;
    }

    public BorderPane construir() {
        datos.setAll(clases);

        construirTabla();
        HBox toolbar = construirToolbar();

        statusbar.setPadding(new Insets(5, 10, 5, 10));
        statusbar.getStyleClass().add("statusbar");
        actualizarStatusbar();

        BorderPane root = new BorderPane();
        root.setTop(toolbar);
        root.setCenter(tabla);
        root.setBottom(statusbar);
        return root;
    }

    private HBox construirToolbar() {
        BotonAccion btnInscribir = BotonAccion.primario("Inscribir cliente");
        BotonAccion btnCancelar  = BotonAccion.peligro("Cancelar inscripcion");
        btnInscribir.setOnAction(e -> accionInscribir());
        btnCancelar.setOnAction(e -> accionCancelar());

        HBox tb = new HBox(10, btnInscribir, btnCancelar);
        tb.setPadding(new Insets(10));
        tb.setAlignment(Pos.CENTER_LEFT);
        tb.getStyleClass().add("toolbar");
        return tb;
    }

    private void construirTabla() {
        TableColumn<ClaseGrupal, Number> colId = new TableColumn<>("Id");
        colId.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(
                d.getValue().getIdClase()));
        colId.setPrefWidth(40);

        TableColumn<ClaseGrupal, String> colNombre = new TableColumn<>("Clase");
        colNombre.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNombre()));
        colNombre.setPrefWidth(170);

        TableColumn<ClaseGrupal, String> colInstructor = new TableColumn<>("Instructor");
        colInstructor.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getInstructor()));
        colInstructor.setPrefWidth(180);

        TableColumn<ClaseGrupal, String> colHorario = new TableColumn<>("Horario");
        colHorario.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getHorario().format(FMT_HORARIO)));
        colHorario.setPrefWidth(140);

        TableColumn<ClaseGrupal, String> colCupo = new TableColumn<>("Cupo");
        colCupo.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNumInscritos()
                        + "/" + d.getValue().getCupoMaximo()));
        colCupo.setPrefWidth(80);

        TableColumn<ClaseGrupal, Number> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(d -> new javafx.beans.property.SimpleDoubleProperty(
                d.getValue().getPrecio()));
        colPrecio.setPrefWidth(80);

        tabla.getColumns().add(colId);
        tabla.getColumns().add(colNombre);
        tabla.getColumns().add(colInstructor);
        tabla.getColumns().add(colHorario);
        tabla.getColumns().add(colCupo);
        tabla.getColumns().add(colPrecio);

        tabla.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tabla.setItems(datos);
    }

    private void accionInscribir() {
        ClaseGrupal sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            alerta("Selecciona primero una clase.");
            return;
        }
        if (sel.estaLlena()) {
            alerta("Esta clase ya alcanzo su cupo maximo (" + sel.getCupoMaximo() + ").");
            return;
        }

        // Pedir cliente
        List<Cliente> clientes = gestionClientes.todos();
        if (clientes.isEmpty()) {
            alerta("No hay clientes registrados.");
            return;
        }
        ChoiceDialog<Cliente> selector = new ChoiceDialog<>(clientes.get(0), clientes);
        selector.initOwner(owner);
        selector.setTitle("Inscribir a clase");
        selector.setHeaderText("Inscribir a " + sel.getNombre());
        selector.setContentText("Selecciona cliente:");

        Optional<Cliente> elegido = selector.showAndWait();
        if (elegido.isEmpty()) return;

        try {
            sel.inscribir(elegido.get().getId());
            tabla.refresh();
            actualizarStatusbar();
            alerta("Inscripcion exitosa: " + elegido.get().getNombreCompleto()
                    + " en " + sel.getNombre() + " ("
                    + sel.getNumInscritos() + "/" + sel.getCupoMaximo() + ")");
        } catch (CupoExcedidoException ex) {
            alerta("No se pudo inscribir: " + ex.getMessage());
        }
    }

    private void accionCancelar() {
        ClaseGrupal sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            alerta("Selecciona primero una clase.");
            return;
        }
        if (sel.getNumInscritos() == 0) {
            alerta("Esta clase no tiene inscripciones que cancelar.");
            return;
        }

        // Listar inscritos para que el usuario elija a cual cancelar
        List<Cliente> inscritos = sel.getInscritosIds().stream()
                .map(gestionClientes::buscarPorId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        if (inscritos.isEmpty()) {
            alerta("No hay clientes inscritos validos.");
            return;
        }

        ChoiceDialog<Cliente> selector = new ChoiceDialog<>(inscritos.get(0), inscritos);
        selector.initOwner(owner);
        selector.setTitle("Cancelar inscripcion");
        selector.setHeaderText("Cancelar inscripcion en " + sel.getNombre());
        selector.setContentText("Selecciona el cliente:");

        Optional<Cliente> elegido = selector.showAndWait();
        elegido.ifPresent(c -> {
            sel.cancelarInscripcion(c.getId());
            tabla.refresh();
            actualizarStatusbar();
            alerta("Inscripcion cancelada para " + c.getNombreCompleto());
        });
    }

    private void alerta(String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.initOwner(owner);
        a.setTitle("Clases Grupales");
        a.setHeaderText(null);
        a.setContentText(mensaje);
        a.showAndWait();
    }

    private void actualizarStatusbar() {
        int totalInscripciones = datos.stream()
                .mapToInt(ClaseGrupal::getNumInscritos).sum();
        statusbar.setText(String.format(
                "Clases: %d  |  Inscripciones totales: %d",
                datos.size(), totalInscripciones));
    }
}
