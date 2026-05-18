package com.gympos.view;

import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

import java.util.regex.Pattern;

/**
 * GymPOS - Campo de texto especializado en emails con validacion
 * visual en tiempo real.
 *
 * Reutilizado de P11. El borde se pone:
 *   - rojo cuando el email es invalido (con tooltip explicando la razon)
 *   - verde cuando es valido
 *   - neutro cuando esta vacio
 */
public class CampoEmail extends TextField {

    private static final Pattern PATRON_EMAIL =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$");

    private boolean valido = false;

    public CampoEmail() {
        super();
        getStyleClass().add("campo-email");
        setPromptText("correo@dominio.com");
        textProperty().addListener((obs, viejo, nuevo) -> revalidar());
        revalidar();
    }

    public CampoEmail(String inicial) {
        this();
        setText(inicial);
    }

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
