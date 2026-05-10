import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Práctica 1 — Programa de presentación.
 * Muestra los datos del autor y la fecha/hora actual del sistema.
 */
public class HolaMundo {

    // TODO: reemplazar con tus datos reales
    private static final String NOMBRE_COMPLETO = "César Aarón Mendoza Benavides";
    private static final String MATRICULA       = "1904833";

    public static void main(String[] args) {
        DateTimeFormatter formato = DateTimeFormatter
                .ofPattern("EEEE d 'de' MMMM 'de' yyyy, HH:mm:ss", Locale.of("es", "MX"));
        String fechaActual = LocalDateTime.now().format(formato);

        System.out.println("============================================");
        System.out.println("  Práctica 1 - LPOO");
        System.out.println("============================================");
        System.out.println("  Nombre:     " + NOMBRE_COMPLETO);
        System.out.println("  Matrícula:  " + MATRICULA);
        System.out.println("  Fecha:      " + fechaActual);
        System.out.println("============================================");
    }
}
