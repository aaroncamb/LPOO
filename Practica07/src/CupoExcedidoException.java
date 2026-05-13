/**
 * Práctica 7 — Excepción cuando se intenta inscribir a alguien en una
 * clase grupal o servicio que ya alcanzo su cupo maximo.
 *
 * Lleva tambien contexto util: cupo actual y maximo, y el nombre de la
 * clase, para que el sistema pueda sugerir alternativas
 * automaticamente ("clase llena, prueba esta otra a la misma hora").
 */
public class CupoExcedidoException extends GymException {

    private static final long serialVersionUID = 1L;

    private final String nombreClase;
    private final int cupoMaximo;
    private final int inscritosActuales;

    public CupoExcedidoException(String nombreClase, int cupoMaximo, int inscritosActuales) {
        super(String.format(
            "Cupo excedido en '%s': %d/%d inscritos.",
            nombreClase, inscritosActuales, cupoMaximo));
        this.nombreClase       = nombreClase;
        this.cupoMaximo        = cupoMaximo;
        this.inscritosActuales = inscritosActuales;
    }

    public String getNombreClase()      { return nombreClase; }
    public int getCupoMaximo()          { return cupoMaximo; }
    public int getInscritosActuales()   { return inscritosActuales; }

    @Override
    public String categoria() {
        return "CUPO_EXCEDIDO";
    }
}
