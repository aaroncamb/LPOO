package com.gympos.view;

import com.gympos.exceptions.PagoRechazadoException;
import com.gympos.model.Cliente;
import com.gympos.model.Membresia;
import com.gympos.service.ProcesadorPagos;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Optional;

/**
 * GymPOS - Dialogo modal para cobrar una membresia o un concepto.
 *
 * Muestra el desglose en vivo:
 *   subtotal - descuento + IVA = total
 *
 * El descuento se ajusta con un Slider de 0% a 30%. El total se
 * recalcula reactivamente mientras se mueve el slider.
 *
 * Maneja PagoRechazadoException: si el cobro falla, muestra un Alert
 * con el contexto rico de la excepcion (referencia, codigo, etc).
 */
public class DialogoCobro {

    private final Stage stage;
    private final Cliente cliente;
    private final Membresia membresia;          // nullable: para cobros genericos
    private final String conceptoGenerico;       // usado si membresia es null
    private final double montoBase;
    private final ProcesadorPagos procesadorPagos;

    private final Slider sliderDescuento = new Slider(0, 30, 0);
    private final ChoiceBox<String> selectorMetodo = new ChoiceBox<>();
    private final Label etiquetaSubtotal = new Label();
    private final Label etiquetaDescuento = new Label();
    private final Label etiquetaIVA = new Label();
    private final Label etiquetaTotal = new Label();

    private ProcesadorPagos.Ticket resultado;
    private final double iva;

    /** Cobra una membresia. */
    public DialogoCobro(Window padre, Cliente cliente, Membresia membresia,
                        ProcesadorPagos procesadorPagos, double iva) {
        this.cliente = cliente;
        this.membresia = membresia;
        this.conceptoGenerico = null;
        this.montoBase = membresia.costoPeriodo();
        this.procesadorPagos = procesadorPagos;
        this.iva = iva;
        this.stage = construirDialogo(padre,
                "Cobrar membresia " + membresia.nombrePlan());
    }

    /** Cobra un concepto generico (ej. clase grupal). */
    public DialogoCobro(Window padre, Cliente cliente, String concepto, double monto,
                        ProcesadorPagos procesadorPagos, double iva) {
        this.cliente = cliente;
        this.membresia = null;
        this.conceptoGenerico = concepto;
        this.montoBase = monto;
        this.procesadorPagos = procesadorPagos;
        this.iva = iva;
        this.stage = construirDialogo(padre, "Cobrar " + concepto);
    }

    private Stage construirDialogo(Window padre, String titulo) {
        // Header con datos del cliente
        Label cabeceraCliente = new Label(
                String.format("Cliente: %s\nEmail: %s",
                        cliente.getNombreCompleto(), cliente.getEmail()));
        cabeceraCliente.getStyleClass().add("cabecera-cobro");

        // Selector de metodo de pago
        selectorMetodo.getItems().setAll("tarjeta", "efectivo", "transferencia");
        selectorMetodo.setValue("tarjeta");

        // Slider de descuento
        sliderDescuento.setShowTickLabels(true);
        sliderDescuento.setShowTickMarks(true);
        sliderDescuento.setMajorTickUnit(10);
        sliderDescuento.setBlockIncrement(1);
        sliderDescuento.valueProperty().addListener((obs, v, n) -> actualizarDesglose());

        // Layout del desglose
        GridPane desglose = new GridPane();
        desglose.setHgap(15);
        desglose.setVgap(8);
        desglose.setPadding(new Insets(10));
        desglose.getStyleClass().add("desglose-cobro");

        int fila = 0;
        desglose.add(new Label("Subtotal:"),   0, fila); desglose.add(etiquetaSubtotal,  1, fila++);
        desglose.add(new Label("Descuento:"),  0, fila); desglose.add(etiquetaDescuento, 1, fila++);
        desglose.add(new Label("IVA (16%):"),  0, fila); desglose.add(etiquetaIVA,       1, fila++);
        desglose.add(new Label("TOTAL:"),      0, fila); desglose.add(etiquetaTotal,     1, fila++);

        etiquetaTotal.getStyleClass().add("total-cobro");

        // Botones
        BotonAccion btnCobrar   = BotonAccion.primario("Cobrar");
        BotonAccion btnCancelar = BotonAccion.secundario("Cancelar");

        Stage stageLocal = new Stage();
        btnCobrar.setOnAction(e -> intentarCobrar(stageLocal));
        btnCancelar.setOnAction(e -> { resultado = null; stageLocal.close(); });

        HBox barraBotones = new HBox(10, btnCancelar, btnCobrar);
        barraBotones.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(15,
                cabeceraCliente,
                new Label("Metodo de pago:"), selectorMetodo,
                new Label("Descuento (%):"),  sliderDescuento,
                desglose,
                barraBotones);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("dialogo-cobro");

        Scene scene = new Scene(root, 480, 460);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        scene.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.ENTER) intentarCobrar(stageLocal);
            else if (ev.getCode() == KeyCode.ESCAPE) { resultado = null; stageLocal.close(); }
        });

        stageLocal.setTitle(titulo);
        stageLocal.setScene(scene);
        stageLocal.initOwner(padre);
        stageLocal.initModality(Modality.WINDOW_MODAL);
        stageLocal.setResizable(false);

        actualizarDesglose();
        return stageLocal;
    }

    /** Recalcula el desglose en vivo segun el slider de descuento. */
    private void actualizarDesglose() {
        double descuentoPct = sliderDescuento.getValue() / 100.0;
        double descuentoMonto = montoBase * descuentoPct;
        double base = montoBase - descuentoMonto;
        double impuestos = base * iva;
        double total = base + impuestos;

        etiquetaSubtotal.setText(String.format("$%.2f", montoBase));
        etiquetaDescuento.setText(String.format("-$%.2f (%.0f%%)",
                descuentoMonto, sliderDescuento.getValue()));
        etiquetaIVA.setText(String.format("+$%.2f", impuestos));
        etiquetaTotal.setText(String.format("$%.2f", total));
    }

    private void intentarCobrar(Stage stageLocal) {
        double descuentoPct = sliderDescuento.getValue() / 100.0;
        String metodo = selectorMetodo.getValue();

        try {
            if (membresia != null) {
                resultado = procesadorPagos.cobrarMembresia(
                        cliente, membresia, descuentoPct, metodo);
            } else {
                double conDescuento = montoBase * (1 - descuentoPct);
                resultado = procesadorPagos.cobrarConcepto(
                        cliente, conceptoGenerico, conDescuento, metodo);
            }
            stageLocal.close();
        } catch (PagoRechazadoException ex) {
            // Mostrar el contexto rico de la excepcion
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.initOwner(stageLocal);
            error.setTitle("Pago rechazado");
            error.setHeaderText("No se pudo procesar el cobro");
            error.setContentText(
                    "Mensaje:     " + ex.getMessage() + "\n"
                  + "Codigo:      " + ex.getCodigoErrorInterno() + "\n"
                  + "Referencia:  " + ex.getReferenciaTransaccion() + "\n"
                  + "Monto:       $" + String.format("%.2f", ex.getMontoIntentado()) + "\n"
                  + "Metodo:      " + ex.getMetodoPago() + "\n\n"
                  + "Da esta referencia a soporte si necesitas seguimiento.");
            error.showAndWait();
        }
    }

    /**
     * Muestra el dialogo y bloquea hasta que se cierre.
     * Devuelve el Ticket emitido, o Optional.empty si se cancelo o si
     * el cobro fallo.
     */
    public Optional<ProcesadorPagos.Ticket> mostrar() {
        stage.showAndWait();
        return Optional.ofNullable(resultado);
    }
}
