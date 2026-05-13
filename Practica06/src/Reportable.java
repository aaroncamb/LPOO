import java.time.LocalDate;

/**
 * Práctica 6 — Interfaz Reportable.
 *
 * Responsabilidad: cosas que aportan datos a reportes gerenciales del
 * gimnasio. El contrato pide los campos minimos que el sistema de
 * reportes necesita: nombre, fecha, monto, y una clave de categoria.
 *
 * Metodo default: toCsvLine(). Permite que cualquier reportable se
 * vuelque a CSV sin que cada clase reescriba el formato; el formato
 * vive en la interfaz misma. Si manana cambiamos el orden de columnas,
 * se ajusta en un solo lugar.
 */
public interface Reportable {

    /** Etiqueta legible del item para el reporte (ej. nombre de la clase). */
    String tituloReporte();

    /** Fecha asociada al item, para agrupar por dia/semana/mes. */
    LocalDate fechaParaReporte();

    /** Monto facturado de este item (para totales en el reporte). */
    double montoFacturado();

    /** Clave de categoria (ej. "yoga", "spinning", "eval-anual"). */
    String categoriaReporte();

    /**
     * Metodo DEFAULT: formatea el item como una linea CSV.
     * Las clases NO redefinen esto; basta con que respondan a los
     * cuatro metodos anteriores y obtienen el CSV gratis.
     *
     * Si una clase quiere cambiar el formato puede sobrescribirlo,
     * pero por ahora el formato unificado es lo que el sistema de
     * reportes consume.
     */
    default String toCsvLine() {
        return String.format("%s,%s,%.2f,%s",
                fechaParaReporte(),
                tituloReporte().replace(",", " "),   // sanitiza la coma
                montoFacturado(),
                categoriaReporte());
    }

    /** Encabezado CSV que combina con toCsvLine. Util para reportes. */
    static String csvHeader() {
        return "fecha,titulo,monto,categoria";
    }
}
