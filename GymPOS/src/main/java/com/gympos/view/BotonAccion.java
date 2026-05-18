package com.gympos.view;

import javafx.scene.control.Button;

/**
 * GymPOS - Boton de accion con variantes predefinidas.
 *
 * Reutilizado tal cual de P11. Centraliza el estilo de los botones
 * en tres variantes (PRIMARIO, SECUNDARIO, PELIGRO) que vinculan a
 * clases CSS en styles.css.
 */
public class BotonAccion extends Button {

    public enum Variante {
        PRIMARIO("boton-primario"),
        SECUNDARIO("boton-secundario"),
        PELIGRO("boton-peligro");

        private final String claseCSS;
        Variante(String claseCSS) { this.claseCSS = claseCSS; }
        public String getClaseCSS() { return claseCSS; }
    }

    public BotonAccion(String texto, Variante variante) {
        super(texto);
        getStyleClass().add("boton-accion");
        getStyleClass().add(variante.getClaseCSS());
    }

    public static BotonAccion primario(String texto)   { return new BotonAccion(texto, Variante.PRIMARIO); }
    public static BotonAccion secundario(String texto) { return new BotonAccion(texto, Variante.SECUNDARIO); }
    public static BotonAccion peligro(String texto)    { return new BotonAccion(texto, Variante.PELIGRO); }
}
