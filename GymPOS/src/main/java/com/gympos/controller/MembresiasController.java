package com.gympos.controller;

import com.gympos.exceptions.MembresiaVencidaException;
import com.gympos.model.Cliente;
import com.gympos.model.Membresia;
import com.gympos.service.GestionClientes;
import com.gympos.service.ProcesadorPagos;
import com.gympos.service.SistemaMembresias;
import com.gympos.view.BotonAccion;
import com.gympos.view.DialogoCobro;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Window;

import java.util.Optional;

/**
 * GymPOS - Controller de la pestaña Membresias.
 *
 * Muestra todas las membresias del sistema con sus fechas de
 * vencimiento, estado (vigente/vencida/por vencer), cliente asociado.
 * Permite:
 *   - Renovar (abre DialogoCobro automaticamente)
 *   - Ver detalle del cliente
 *   - Filtrar por estado: todos / vigentes / por vencer / vencidas
 */
public class MembresiasController {

    private final Window owner;
    private final SistemaMembresias sistemaMembresias;
    private final GestionClientes gestionClientes;
    private final ProcesadorPagos procesadorPagos;
    private final double iva;

    private final ObservableList<FilaMembresia> datos = FXCollections.observableArrayList();
    private final TableView<FilaMembresia> tabla = new TableView<>();
    private final Label statusbar = new Label();

    public MembresiasController(Window owner, SistemaMembresias sistemaMembresias,
                                GestionClientes gestionClientes,
                                ProcesadorPagos procesadorPagos, double iva) {
        this.owner = owner;
        this.sistemaMembresias = sistemaMembresias;
        this.gestionClientes = gestionClientes;
        this.procesadorPagos = procesadorPagos;
        this.iva = iva;
    }

    public BorderPane construir() {
        recargarDatos();

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
        BotonAccion btnRenovar  = BotonAccion.primario("Renovar y cobrar");
        BotonAccion btnRefrescar= BotonAccion.secundario("Refrescar");
        btnRenovar.setOnAction(e -> accionRenovar());
        btnRefrescar.setOnAction(e -> { recargarDatos(); actualizarStatusbar(); });

        Label info = new Label("Doble-click sobre una fila para renovar");
        info.getStyleClass().add("info-label");

        HBox tb = new HBox(10, btnRenovar, btnRefrescar, info);
        tb.setPadding(new Insets(10));
        tb.setAlignment(Pos.CENTER_LEFT);
        tb.getStyleClass().add("toolbar");
        return tb;
    }

    private void construirTabla() {
        TableColumn<FilaMembresia, Number> colId = new TableColumn<>("Id");
        colId.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(
                d.getValue().getMembresia().getIdMembresia()));
        colId.setPrefWidth(50);

        TableColumn<FilaMembresia, String> colCliente = new TableColumn<>("Cliente");
        colCliente.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNombreCliente()));
        colCliente.setPrefWidth(220);

        TableColumn<FilaMembresia, String> colPlan = new TableColumn<>("Plan");
        colPlan.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getMembresia().nombrePlan()));
        colPlan.setPrefWidth(90);

        TableColumn<FilaMembresia, String> colInicio = new TableColumn<>("Inicio");
        colInicio.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getMembresia().getFechaInicio().toString()));
        colInicio.setPrefWidth(95);

        TableColumn<FilaMembresia, String> colVence = new TableColumn<>("Vence");
        colVence.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getMembresia().getFechaVencimiento().toString()));
        colVence.setPrefWidth(95);

        TableColumn<FilaMembresia, String> colDias = new TableColumn<>("Dias rest.");
        colDias.setCellValueFactory(d -> {
            long dias = d.getValue().getMembresia().diasParaVencer();
            return new SimpleStringProperty(dias < 0 ? "(vencida " + (-dias) + "d)" : dias + "d");
        });
        colDias.setPrefWidth(100);

        TableColumn<FilaMembresia, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado()));
        colEstado.setPrefWidth(100);

        tabla.getColumns().add(colId);
        tabla.getColumns().add(colCliente);
        tabla.getColumns().add(colPlan);
        tabla.getColumns().add(colInicio);
        tabla.getColumns().add(colVence);
        tabla.getColumns().add(colDias);
        tabla.getColumns().add(colEstado);

        tabla.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tabla.setItems(datos);

        // Doble-click sobre fila: renovar
        tabla.setRowFactory(tv -> {
            javafx.scene.control.TableRow<FilaMembresia> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !row.isEmpty()) {
                    tabla.getSelectionModel().select(row.getItem());
                    accionRenovar();
                }
            });
            return row;
        });
    }

    private void recargarDatos() {
        datos.clear();
        for (Membresia m : sistemaMembresias.todas()) {
            Optional<Cliente> c = gestionClientes.buscarPorId(m.getIdCliente());
            if (c.isEmpty()) continue;   // cliente borrado, omitimos
            datos.add(new FilaMembresia(m, c.get()));
        }
    }

    private void accionRenovar() {
        FilaMembresia sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            alerta("Selecciona primero una membresia.");
            return;
        }

        // Abrir dialogo de cobro: si paga, se renueva.
        DialogoCobro dc = new DialogoCobro(owner, sel.getCliente(),
                sel.getMembresia(), procesadorPagos, iva);
        Optional<ProcesadorPagos.Ticket> resultado = dc.mostrar();

        if (resultado.isPresent()) {
            // Pago exitoso -> renovar la membresia
            try {
                sistemaMembresias.renovar(sel.getCliente().getId());
                // Bonus de puntos por renovacion
                sel.getCliente().agregarPuntos(50);
                alerta("Membresia renovada exitosamente. +50 puntos bonus.");
            } catch (MembresiaVencidaException ex) {
                // No deberia llegar aqui: renovar() solo loguea pero no lanza
                alerta("Advertencia: " + ex.getMessage());
            }
            recargarDatos();
            tabla.refresh();
            actualizarStatusbar();
        }
    }

    private void alerta(String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.initOwner(owner);
        a.setTitle("GymPOS");
        a.setHeaderText(null);
        a.setContentText(mensaje);
        a.showAndWait();
    }

    private void actualizarStatusbar() {
        long vigentes = datos.stream().filter(f -> f.getMembresia().estaVigente()).count();
        long vencidas = datos.stream().filter(f -> !f.getMembresia().estaVigente()).count();
        statusbar.setText(String.format(
                "Total: %d  |  Vigentes: %d  |  Vencidas: %d",
                datos.size(), vigentes, vencidas));
    }

    // ============================================================
    //   CLASE INTERNA - fila combinada para la tabla
    // ============================================================

    /** Empareja una Membresia con su Cliente para mostrar en la tabla. */
    private static class FilaMembresia {
        private final Membresia membresia;
        private final Cliente cliente;

        FilaMembresia(Membresia membresia, Cliente cliente) {
            this.membresia = membresia;
            this.cliente = cliente;
        }

        Membresia getMembresia() { return membresia; }
        Cliente getCliente()     { return cliente; }
        String getNombreCliente() { return cliente.getNombreCompleto(); }

        String getEstado() {
            long dias = membresia.diasParaVencer();
            if (dias < 0) return "Vencida";
            if (dias <= 7) return "Por vencer";
            return "Vigente";
        }
    }
}
