import java.time.LocalDate;

/**
 * Práctica 4 — Membresia VIP (concreta, hereda DIRECTO de Membresia).
 *
 * Por que VIP NO hereda de Estandar:
 *   Estandar modela un contrato mensual con precio mensual fijo. VIP
 *   funciona con un esquema completamente distinto:
 *     - Cuota ANUAL (no mensual).
 *     - Renovacion suma 365 dias (no 30).
 *     - El "precio mensual" no aplica: se factura una vez al año.
 *   Forzar a VIP a heredar de Estandar me obligaria a inventar un
 *   precioMensual que no representa nada en el mundo real (cuota anual
 *   dividida entre 12, lo cual nadie firmaria como contrato), o a tener
 *   un metodo "renovar" sobrescrito que ignora la implementacion del
 *   padre. Cualquiera de las dos opciones huele a herencia mal aplicada.
 *
 *   La solucion correcta: VIP es hermana de Estandar, no hija de ella.
 *   Ambas heredan de Membresia, que solo define lo verdaderamente
 *   compartido (titular, fechas, vigencia).
 *
 * Esta decision la documento en el README como "situacion donde la
 * herencia me hubiera causado un problema", que es justo lo que pide el
 * Elemento de Decision Propia de P4.
 */
public class MembresiaVIP extends Membresia {

    public static final double CUOTA_ANUAL = 14_400.0;
    private static final double DESCUENTO = 0.10;
    private static final int DIAS_VIGENCIA = 365;

    private boolean entrenadorPersonalIncluido;
    private int     accesosSpaConsumidosEsteAnio;

    public MembresiaVIP(String titularNombre, LocalDate fechaInicio) {
        super(titularNombre, fechaInicio);
        this.fechaFin = fechaInicio.plusDays(DIAS_VIGENCIA);
        this.entrenadorPersonalIncluido = true;
        this.accesosSpaConsumidosEsteAnio = 0;
    }

    @Override
    public double calcularPrecio() {
        // VIP cobra cuota anual completa. Si renovo antes de vencer,
        // recibe 10% de descuento en la siguiente anualidad.
        if (estaVigente()) {
            return CUOTA_ANUAL * (1.0 - DESCUENTO);
        }
        return CUOTA_ANUAL;
    }

    @Override
    public String beneficiosIncluidos() {
        return "Acceso 24/7, entrenador personal incluido, spa, "
             + "lockers privados, estacionamiento reservado.";
    }

    @Override
    public void renovar() {
        LocalDate base = (fechaFin.isAfter(LocalDate.now())) ? fechaFin : LocalDate.now();
        this.fechaFin = base.plusDays(DIAS_VIGENCIA);
        this.activa   = true;
    }

    @Override
    public double descuentoRenovacion() {
        return DESCUENTO;
    }

    @Override
    public String tipoLegible() {
        return "Membresia VIP";
    }

    // -------- Funcionalidad propia de VIP --------

    public void registrarAccesoSpa() {
        accesosSpaConsumidosEsteAnio++;
    }

    public int getAccesosSpaConsumidosEsteAnio() {
        return accesosSpaConsumidosEsteAnio;
    }

    public boolean isEntrenadorPersonalIncluido() {
        return entrenadorPersonalIncluido;
    }
}
