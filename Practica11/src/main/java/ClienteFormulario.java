import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Práctica 11 — Formulario CRUD modal de cliente.
 *
 * Es un dialogo modal: cuando esta abierto, la ventana padre no recibe
 * eventos (initModality(WINDOW_MODAL)). Sirve tanto para crear cliente
 * nuevo como para editar existente.
 *
 * Caracteristicas didacticas:
 *   - Modalidad: WINDOW_MODAL bloquea la ventana padre mientras abre.
 *   - Validacion en tiempo real: el boton "Guardar" solo se habilita
 *     cuando todos los campos son validos.
 *   - Eventos de teclado: Enter guarda (si valido), Escape cancela.
 *   - Uso de componentes propios: CampoEmail con validacion visual,
 *     BotonAccion con variantes.
 *   - Modo doble: misma clase para "crear" y "editar". Si recibe
 *     cliente en el constructor, edita; si null, crea uno nuevo.
 *
 * El flujo de uso es:
 *   Optional<Cliente> r = new ClienteFormulario(padre, null).mostrar();
 *   r.ifPresent(c -> ...); // si el usuario cancelo, vacio.
 */
public class ClienteFormulario {

    private final Stage stage;
    private Cliente resultado;   // null si el usuario cancela

    // Campos de UI
    private final TextField  campoId;
    private final TextField  campoNombre;
    private final CampoEmail campoEmail;       // componente personalizado
    private final DatePicker campoFecha;
    private final TextField  campoPeso;
    private final ComboBox<Cliente.TipoMembresia> campoTipo;
    private final BotonAccion botonGuardar;    // componente personalizado
    private final BotonAccion botonCancelar;
    private final Label etiquetaError;

    private final boolean esEdicion;
    private final Cliente clienteOriginal;

    /**
     * @param padre  ventana que invoca este dialogo
     * @param editar null para nuevo, o un cliente existente para editarlo
     */
    public ClienteFormulario(Window padre, Cliente editar) {
        this.esEdicion = editar != null;
        this.clienteOriginal = editar;

        // -------- Construir los campos --------
        campoId      = new TextField();
        campoNombre  = new TextField();
        campoEmail   = new CampoEmail();
        campoFecha   = new DatePicker(LocalDate.now());
        campoPeso    = new TextField();
        campoTipo    = new ComboBox<>();
        campoTipo.getItems().setAll(Cliente.TipoMembresia.values());
        campoTipo.setValue(Cliente.TipoMembresia.BASICA);

        campoNombre.setPromptText("Nombre completo");
        campoPeso.setPromptText("70.0");
        campoId.setPromptText("1234");

        // Si es edicion, prellenar y bloquear el id (no debe cambiar)
        if (esEdicion) {
            campoId.setText(String.valueOf(editar.getId()));
            campoId.setEditable(false);
            campoNombre.setText(editar.getNombreCompleto());
            campoEmail.setText(editar.getEmail());
            campoFecha.setValue(editar.getFechaRegistro());
            campoPeso.setText(String.valueOf(editar.getPesoKg()));
            campoTipo.setValue(editar.getTipoMembresia());
        }

        etiquetaError = new Label();
        etiquetaError.getStyleClass().add("etiqueta-error");

        // -------- Botones --------
        botonGuardar = BotonAccion.primario(esEdicion ? "Actualizar" : "Guardar");
        botonCancelar = BotonAccion.secundario("Cancelar");

        botonGuardar.setOnAction(e -> intentarGuardar());
        botonCancelar.setOnAction(e -> {
            resultado = null;
            stage.close();
        });

        // -------- Listeners de validacion en tiempo real --------
        Runnable revalidar = this::actualizarBotonGuardar;
        campoId.textProperty().addListener((o, v, n) -> revalidar.run());
        campoNombre.textProperty().addListener((o, v, n) -> revalidar.run());
        campoEmail.textProperty().addListener((o, v, n) -> revalidar.run());
        campoPeso.textProperty().addListener((o, v, n) -> revalidar.run());
        revalidar.run();

        // -------- Layout --------
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        int fila = 0;
        grid.add(new Label("Id:"), 0, fila);          grid.add(campoId, 1, fila++);
        grid.add(new Label("Nombre:"), 0, fila);      grid.add(campoNombre, 1, fila++);
        grid.add(new Label("Email:"), 0, fila);       grid.add(campoEmail, 1, fila++);
        grid.add(new Label("Fecha registro:"), 0, fila); grid.add(campoFecha, 1, fila++);
        grid.add(new Label("Peso (kg):"), 0, fila);   grid.add(campoPeso, 1, fila++);
        grid.add(new Label("Tipo membresia:"), 0, fila); grid.add(campoTipo, 1, fila++);

        HBox botones = new HBox(10, botonCancelar, botonGuardar);
        botones.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(10, grid, etiquetaError, botones);
        root.setPadding(new Insets(10));
        root.getStyleClass().add("formulario-root");

        Scene scene = new Scene(root, 420, 380);
        scene.getStylesheets().add(
                getClass().getResource("/styles.css").toExternalForm());

        // Atajo de teclado: Enter para guardar, Escape para cancelar.
        scene.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.ENTER && !botonGuardar.isDisabled()) {
                intentarGuardar();
            } else if (ev.getCode() == KeyCode.ESCAPE) {
                resultado = null;
                stage.close();
            }
        });

        stage = new Stage();
        stage.setTitle(esEdicion ? "Editar cliente" : "Nuevo cliente");
        stage.setScene(scene);
        stage.initOwner(padre);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(false);
    }

    /**
     * Muestra el dialogo y bloquea hasta que el usuario lo cierre.
     * Devuelve el cliente creado/editado o Optional.empty() si cancelo.
     */
    public Optional<Cliente> mostrar() {
        stage.showAndWait();
        return Optional.ofNullable(resultado);
    }

    // ---------------------------------------------------------------
    //   VALIDACION + GUARDADO
    // ---------------------------------------------------------------

    private void actualizarBotonGuardar() {
        String error = primerError();
        if (error == null) {
            botonGuardar.setDisable(false);
            etiquetaError.setText("");
        } else {
            botonGuardar.setDisable(true);
            etiquetaError.setText(error);
        }
    }

    /**
     * Revisa cada campo en orden y devuelve el primer error, o null si
     * todos son validos. La logica esta separada para que cada regla
     * sea facil de leer y modificar.
     */
    private String primerError() {
        // Id
        try {
            int id = Integer.parseInt(campoId.getText().trim());
            if (id <= 0) return "El id debe ser positivo";
        } catch (NumberFormatException e) {
            return "El id debe ser un numero";
        }
        // Nombre
        if (campoNombre.getText() == null || campoNombre.getText().isBlank()) {
            return "El nombre es obligatorio";
        }
        // Email - delegamos al componente personalizado
        if (!campoEmail.esValido()) {
            return "Email invalido";
        }
        // Fecha
        if (campoFecha.getValue() == null) {
            return "La fecha es obligatoria";
        }
        // Peso (0 = no medido es valido)
        try {
            String pTxt = campoPeso.getText().trim().replace(',', '.');
            if (!pTxt.isEmpty()) {
                double peso = Double.parseDouble(pTxt);
                if (peso != 0 && (peso < 30 || peso > 300)) {
                    return "El peso debe estar entre 30 y 300 kg (o 0)";
                }
            }
        } catch (NumberFormatException e) {
            return "El peso debe ser un numero";
        }
        // Tipo
        if (campoTipo.getValue() == null) {
            return "Selecciona un tipo de membresia";
        }
        return null;
    }

    /**
     * Crea o actualiza el cliente y cierra el dialogo. Solo se llega
     * aqui si la validacion ya paso (el boton estaba habilitado).
     */
    private void intentarGuardar() {
        if (primerError() != null) return;   // doble defensa

        int id = Integer.parseInt(campoId.getText().trim());
        String nombre = campoNombre.getText().trim();
        String email = campoEmail.getText().trim();
        LocalDate fecha = campoFecha.getValue();
        String pTxt = campoPeso.getText().trim().replace(',', '.');
        double peso = pTxt.isEmpty() ? 0 : Double.parseDouble(pTxt);
        Cliente.TipoMembresia tipo = campoTipo.getValue();

        if (esEdicion) {
            // Mutar el original
            clienteOriginal.setNombreCompleto(nombre);
            clienteOriginal.setEmail(email);
            clienteOriginal.setFechaRegistro(fecha);
            clienteOriginal.setPesoKg(peso);
            clienteOriginal.setTipoMembresia(tipo);
            resultado = clienteOriginal;
        } else {
            resultado = new Cliente(id, nombre, email, fecha, peso, tipo);
        }
        stage.close();
    }
}
