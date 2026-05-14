import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

import java.util.regex.Pattern;

/**
 * Práctica 11 — Componente personalizado #2.
 *
 * Extiende TextField con validacion de email EN TIEMPO REAL:
 *   - Mientras el usuario escribe, el campo cambia de color: rojo si
 *     el email no es valido, verde si si.
 *   - Un tooltip aparece sobre el campo explicando el problema
 *     ("falta @", "vacio", etc).
 *   - Expone un metodo esValido() que el formulario puede consultar
 *     antes de habilitar el boton Guardar.
 *
 * El listener vive sobre textProperty(): cada cambio del texto dispara
 * la revalidacion. Es el mismo mecanismo que se usa en el filtrado
 * interactivo (decision propia) pero aplicado a un solo campo.
 */
public class CampoEmail extends TextField {

    /**
     * Regex pragmatico de email. No es el RFC-5322 completo (que tiene
     * cientos de caracteres y validacion compleja), pero cubre el 99%
     * de los emails reales: usuario@dominio.tld.
     */
    private static final Pattern PATRON_EMAIL =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$");

    private boolean valido = false;

    public CampoEmail() {
        super();
        getStyleClass().add("campo-email");
        setPromptText("correo@dominio.com");

        // Listener en tiempo real: cada cambio del texto revalida.
        textProperty().addListener((obs, viejo, nuevo) -> revalidar());
        revalidar();   // estado inicial
    }

    public CampoEmail(String inicial) {
        this();
        setText(inicial);
    }

    /**
     * Revalida el contenido del campo y actualiza las clases CSS y el
     * tooltip. La logica esta separada en estados claros:
     *   - vacio: estilo neutro, sin tooltip.
     *   - invalido: clase "email-invalido", tooltip con razon.
     *   - valido: clase "email-valido".
     */
    private void revalidar() {
        String texto = getText();
        getStyleClass().removeAll("email-invalido", "email-valido");

        if (texto == null || texto.isBlank()) {
            valido = false;
            setTooltip(null);
            return;
        }

        if (PATRON_EMAIL.matcher(texto).matches()) {
            valido = true;
            getStyleClass().add("email-valido");
            setTooltip(null);
        } else {
            valido = false;
            getStyleClass().add("email-invalido");
            setTooltip(new Tooltip(motivoInvalido(texto)));
        }
    }

    private String motivoInvalido(String texto) {
        if (!texto.contains("@"))  return "Falta el @";
        if (texto.indexOf('@') == 0) return "Falta el nombre antes del @";
        if (texto.indexOf('@') == texto.length() - 1) return "Falta el dominio despues del @";
        if (!texto.substring(texto.indexOf('@')).contains(".")) return "Falta el punto en el dominio";
        return "Email invalido";
    }

    public boolean esValido() { return valido; }
}
