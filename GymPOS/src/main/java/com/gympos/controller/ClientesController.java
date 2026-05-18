package com.gympos.controller;

import com.gympos.model.Cliente;
import com.gympos.service.GestionClientes;
import com.gympos.view.BotonAccion;
import com.gympos.view.CampoEmail;

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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.LocalDate;
import java.util.Optional;

/**
 * GymPOS - Controller de la pestaña Clientes.
 *
 * Reusa exactamente el patron de MainController de P11:
 *   - TableView<Cliente> con FilteredList -> SortedList -> binding al
 *     comparator de la tabla.
 *   - Campo de busqueda que filtra por nombre o email en tiempo real.
 *   - Modal CRUD para alta/edicion con validacion en vivo.
 *   - Doble-click para editar, Delete para eliminar.
 *
 * Devuelve un Node (BorderPane) que MainController agrega a un Tab.
 */
public class ClientesController {

    private final Window owner;
    private final GestionClientes gestion;

    private final ObservableList<Cliente> datos = FXCollections.observableArrayList();
    private final FilteredList<Cliente> datosFiltrados = new FilteredList<>(datos, p -> true);
    private final TableView<Cliente> tabla = new TableView<>();
    private final TextField campoBusqueda = new TextField();
    private final Label statusbar = new Label();

    public ClientesController(Window owner, GestionClientes gestion) {
        this.owner = owner;
        this.gestion = gestion;
    }

    public BorderPane construir() {
        // Cargar datos iniciales
        datos.setAll(gestion.todos());

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
        Label etiqueta = new Label("Buscar:");
        campoBusqueda.setPromptText("nombre o email...");
        campoBusqueda.setPrefWidth(280);
        campoBusqueda.getStyleClass().add("campo-busqueda");

        // Filtrado interactivo en tiempo real (reutilizado de P11)
        campoBusqueda.textProperty().addListener((obs, viejo, nuevo) -> {
            datosFiltrados.setPredicate(c -> {
                if (nuevo == null || nuevo.isBlank()) return true;
                String aguja = nuevo.trim().toLowerCase();
                return c.getNombreCompleto().toLowerCase().contains(aguja)
                    || c.getEmail().toLowerCase().contains(aguja);
            });
            actualizarStatusbar();
        });

        BotonAccion btnNuevo    = BotonAccion.primario("Nuevo");
        BotonAccion btnEditar   = BotonAccion.secundario("Editar");
        BotonAccion btnEliminar = BotonAccion.peligro("Eliminar");
        btnNuevo.setOnAction(e -> accionNuevo());
        btnEditar.setOnAction(e -> accionEditar());
        btnEliminar.setOnAction(e -> accionEliminar());

        HBox tb = new HBox(10, etiqueta, campoBusqueda, btnNuevo, btnEditar, btnEliminar);
        tb.setPadding(new Insets(10));
        tb.setAlignment(Pos.CENTER_LEFT);
        tb.getStyleClass().add("toolbar");
        return tb;
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

        TableColumn<Cliente, Number> colPuntos = new TableColumn<>("Puntos");
        colPuntos.setCellValueFactory(d -> d.getValue().puntosProperty());
        colPuntos.setPrefWidth(70);

        TableColumn<Cliente, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().isActivo() ? "Activo" : "Inactivo"));
        colEstado.setPrefWidth(80);

        tabla.getColumns().add(colId);
        tabla.getColumns().add(colNombre);
        tabla.getColumns().add(colEmail);
        tabla.getColumns().add(colTipo);
        tabla.getColumns().add(colPeso);
        tabla.getColumns().add(colPuntos);
        tabla.getColumns().add(colEstado);

        tabla.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        SortedList<Cliente> ordenable = new SortedList<>(datosFiltrados);
        ordenable.comparatorProperty().bind(tabla.comparatorProperty());
        tabla.setItems(ordenable);

        // Eventos
        tabla.setOnMouseClicked(ev -> {
            if (ev.getButton() == MouseButton.PRIMARY && ev.getClickCount() == 2) {
                accionEditar();
            }
        });
        tabla.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.DELETE) accionEliminar();
            else if (ev.getCode() == KeyCode.ENTER) accionEditar();
        });
    }

    // ---------- Acciones ----------

    private void accionNuevo() {
        Optional<Cliente> nuevo = new FormularioCliente(owner, null).mostrar();
        nuevo.ifPresent(c -> {
            if (gestion.agregar(c)) {
                datos.add(c);
                tabla.getSelectionModel().select(c);
                actualizarStatusbar();
            } else {
                alerta(Alert.AlertType.WARNING, "Conflicto",
                        "Id o email ya existe. No se agrego.");
            }
        });
    }

    private void accionEditar() {
        Cliente sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            alerta(Alert.AlertType.INFORMATION, "Sin seleccion",
                    "Selecciona primero un cliente.");
            return;
        }
        Optional<Cliente> modificado = new FormularioCliente(owner, sel).mostrar();
        modificado.ifPresent(c -> {
            gestion.actualizar(c);
            tabla.refresh();
        });
    }

    private void accionEliminar() {
        Cliente sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            alerta(Alert.AlertType.INFORMATION, "Sin seleccion",
                    "Selecciona primero un cliente.");
            return;
        }
        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION);
        confirmar.initOwner(owner);
        confirmar.setTitle("Confirmar eliminacion");
        confirmar.setHeaderText("Eliminar a " + sel.getNombreCompleto() + "?");
        confirmar.setContentText("Esta accion no se puede deshacer.");
        confirmar.getButtonTypes().setAll(ButtonType.YES, ButtonType.CANCEL);

        Optional<ButtonType> r = confirmar.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.YES) {
            if (gestion.eliminarPorId(sel.getId())) {
                datos.remove(sel);
                actualizarStatusbar();
            }
        }
    }

    private void alerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert a = new Alert(tipo);
        a.initOwner(owner);
        a.setTitle(titulo);
        a.setHeaderText(titulo);
        a.setContentText(contenido);
        a.showAndWait();
    }

    private void actualizarStatusbar() {
        statusbar.setText(String.format("Mostrando %d de %d clientes",
                datosFiltrados.size(), datos.size()));
    }

    // ============================================================
    //   FORMULARIO CRUD INTERNO (clase anidada para no contaminar)
    // ============================================================

    /**
     * Formulario modal de alta/edicion. Es estaticamente identico al de
     * P11 pero usa el CampoEmail y BotonAccion de view/.
     */
    private static class FormularioCliente {
        private final Stage stage;
        private Cliente resultado;
        private final Cliente original;
        private final boolean esEdicion;

        private final TextField campoId     = new TextField();
        private final TextField campoNombre = new TextField();
        private final CampoEmail campoEmail = new CampoEmail();
        private final DatePicker campoFecha = new DatePicker(LocalDate.now());
        private final TextField campoPeso   = new TextField();
        private final ComboBox<Cliente.TipoMembresia> campoTipo = new ComboBox<>();
        private final Label etiquetaError   = new Label();
        private final BotonAccion botonGuardar;
        private final BotonAccion botonCancelar;

        FormularioCliente(Window padre, Cliente editar) {
            this.original = editar;
            this.esEdicion = editar != null;

            campoTipo.getItems().setAll(Cliente.TipoMembresia.values());
            campoTipo.setValue(Cliente.TipoMembresia.BASICA);

            if (esEdicion) {
                campoId.setText(String.valueOf(editar.getId()));
                campoId.setEditable(false);
                campoNombre.setText(editar.getNombreCompleto());
                campoEmail.setText(editar.getEmail());
                campoFecha.setValue(editar.getFechaRegistro());
                campoPeso.setText(String.valueOf(editar.getPesoKg()));
                campoTipo.setValue(editar.getTipoMembresia());
            }

            etiquetaError.getStyleClass().add("etiqueta-error");
            botonGuardar = BotonAccion.primario(esEdicion ? "Actualizar" : "Guardar");
            botonCancelar = BotonAccion.secundario("Cancelar");

            GridPane grid = new GridPane();
            grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
            int f = 0;
            grid.add(new Label("Id:"), 0, f); grid.add(campoId, 1, f++);
            grid.add(new Label("Nombre:"), 0, f); grid.add(campoNombre, 1, f++);
            grid.add(new Label("Email:"), 0, f); grid.add(campoEmail, 1, f++);
            grid.add(new Label("Fecha registro:"), 0, f); grid.add(campoFecha, 1, f++);
            grid.add(new Label("Peso (kg):"), 0, f); grid.add(campoPeso, 1, f++);
            grid.add(new Label("Tipo:"), 0, f); grid.add(campoTipo, 1, f++);

            HBox botones = new HBox(10, botonCancelar, botonGuardar);
            botones.setAlignment(Pos.CENTER_RIGHT);

            VBox root = new VBox(10, grid, etiquetaError, botones);
            root.setPadding(new Insets(10));
            root.getStyleClass().add("formulario-root");

            Scene scene = new Scene(root, 440, 400);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

            stage = new Stage();
            stage.setTitle(esEdicion ? "Editar cliente" : "Nuevo cliente");
            stage.setScene(scene);
            stage.initOwner(padre);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);

            botonGuardar.setOnAction(e -> intentarGuardar());
            botonCancelar.setOnAction(e -> { resultado = null; stage.close(); });
            scene.setOnKeyPressed(ev -> {
                if (ev.getCode() == KeyCode.ENTER && !botonGuardar.isDisabled()) intentarGuardar();
                else if (ev.getCode() == KeyCode.ESCAPE) { resultado = null; stage.close(); }
            });

            Runnable revalidar = this::revalidar;
            campoId.textProperty().addListener((o, v, n) -> revalidar.run());
            campoNombre.textProperty().addListener((o, v, n) -> revalidar.run());
            campoEmail.textProperty().addListener((o, v, n) -> revalidar.run());
            campoPeso.textProperty().addListener((o, v, n) -> revalidar.run());
            revalidar.run();
        }

        private void revalidar() {
            String error = primerError();
            botonGuardar.setDisable(error != null);
            etiquetaError.setText(error == null ? "" : error);
        }

        private String primerError() {
            try {
                int id = Integer.parseInt(campoId.getText().trim());
                if (id <= 0) return "Id debe ser positivo";
            } catch (NumberFormatException e) { return "Id debe ser un numero"; }

            if (campoNombre.getText() == null || campoNombre.getText().isBlank())
                return "El nombre es obligatorio";

            if (!campoEmail.esValido()) return "Email invalido";

            if (campoFecha.getValue() == null) return "La fecha es obligatoria";

            try {
                String t = campoPeso.getText().trim().replace(',', '.');
                if (!t.isEmpty()) {
                    double p = Double.parseDouble(t);
                    if (p != 0 && (p < 30 || p > 300)) return "Peso entre 30 y 300 kg (o 0)";
                }
            } catch (NumberFormatException e) { return "Peso debe ser numero"; }

            if (campoTipo.getValue() == null) return "Selecciona tipo de membresia";
            return null;
        }

        private void intentarGuardar() {
            if (primerError() != null) return;
            int id = Integer.parseInt(campoId.getText().trim());
            String nombre = campoNombre.getText().trim();
            String email = campoEmail.getText().trim();
            LocalDate fecha = campoFecha.getValue();
            String pt = campoPeso.getText().trim().replace(',', '.');
            double peso = pt.isEmpty() ? 0 : Double.parseDouble(pt);
            Cliente.TipoMembresia tipo = campoTipo.getValue();

            if (esEdicion) {
                original.setNombreCompleto(nombre);
                original.setEmail(email);
                original.setFechaRegistro(fecha);
                original.setPesoKg(peso);
                original.setTipoMembresia(tipo);
                resultado = original;
            } else {
                resultado = new Cliente(id, nombre, email, fecha, peso, tipo);
            }
            stage.close();
        }

        Optional<Cliente> mostrar() {
            stage.showAndWait();
            return Optional.ofNullable(resultado);
        }
    }
}
