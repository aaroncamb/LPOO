import javafx.scene.control.Button;

/**
 * Práctica 11 — Componente personalizado #1.
 *
 * Extiende Button con tres "variantes" de estilo predefinidas
 * (PRIMARIO, SECUNDARIO, PELIGRO). Cada variante aplica una clase CSS
 * que vive en styles.css.
 *
 * Por que vale la pena tener este componente:
 *   - Centraliza el estilo de los botones; cambiar el aspecto del
 *     boton primario es modificar una clase CSS, no buscar todas las
 *     llamadas a setStyle en la app.
 *   - Hace el codigo de la UI mas legible: en lugar de aplicar 3
 *     setStyle's a cada boton, simplemente declaro la variante en el
 *     constructor.
 *   - Garantiza consistencia visual: imposible que un dia un boton
 *     primario salga con otro color por descuido.
 */
public class BotonAccion extends Button {

    /** Variantes visuales del boton. */
    public enum Variante {
        /** Accion principal: guardar, confirmar. Color dorado del gym. */
        PRIMARIO("boton-primario"),
        /** Accion secundaria: cancelar, cerrar. */
        SECUNDARIO("boton-secundario"),
        /** Accion destructiva: eliminar. Color rojo. */
        PELIGRO("boton-peligro");

        private final String claseCSS;
        Variante(String claseCSS) { this.claseCSS = claseCSS; }
        public String getClaseCSS() { return claseCSS; }
    }

    private final Variante variante;

    public BotonAccion(String texto, Variante variante) {
        super(texto);
        this.variante = variante;
        getStyleClass().add("boton-accion");          // estilo base
        getStyleClass().add(variante.getClaseCSS());   // variante especifica
    }

    /**
     * Atajo: crea un boton primario con el texto dado.
     */
    public static BotonAccion primario(String texto) {
        return new BotonAccion(texto, Variante.PRIMARIO);
    }

    public static BotonAccion secundario(String texto) {
        return new BotonAccion(texto, Variante.SECUNDARIO);
    }

    public static BotonAccion peligro(String texto) {
        return new BotonAccion(texto, Variante.PELIGRO);
    }

    public Variante getVariante() { return variante; }
}
