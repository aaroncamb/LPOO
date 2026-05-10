import java.util.Scanner;

/**
 * Práctica 1 — Elemento de Decisión Propia.
 *
 * Calculadora de IMC (Índice de Masa Corporal).
 *
 * Lee peso (kg) y altura (cm) por consola, calcula el IMC y
 * lo clasifica según los rangos de la OMS.
 *
 * Justificación de la elección:
 *   El semestre completo lo trabajaré sobre el dominio "gimnasio" para
 *   que las clases construidas se reutilicen en el proyecto final GymPOS.
 *   Empezar desde la Práctica 1 con una utilidad ligada al dominio
 *   refuerza esa coherencia y me da una primera función que volveré a
 *   usar (los clientes de un gimnasio guardan peso y altura).
 *
 * Solo usa la biblioteca estándar de Java (java.util.Scanner).
 */
public class CalculadoraIMC {

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("=== Calculadora de IMC ===");

            System.out.print("Peso (kg):    ");
            double pesoKg = leerDoublePositivo(sc);

            System.out.print("Altura (cm):  ");
            double alturaCm = leerDoublePositivo(sc);

            double alturaM = alturaCm / 100.0;
            double imc = pesoKg / (alturaM * alturaM);

            System.out.printf("%nIMC: %.2f%n", imc);
            System.out.println("Categoría: " + clasificar(imc));
        }
    }

    /**
     * Lee un double estrictamente positivo desde el Scanner.
     * Repite la lectura hasta que el valor sea válido.
     */
    private static double leerDoublePositivo(Scanner sc) {
        while (true) {
            String linea = sc.nextLine().trim().replace(',', '.');
            try {
                double valor = Double.parseDouble(linea);
                if (valor > 0) {
                    return valor;
                }
                System.out.print("  Debe ser mayor que cero. Intenta de nuevo: ");
            } catch (NumberFormatException e) {
                System.out.print("  Valor no numérico. Intenta de nuevo: ");
            }
        }
    }

    /**
     * Clasifica un IMC según los rangos estándar de la OMS.
     */
    private static String clasificar(double imc) {
        if (imc < 18.5) return "Bajo peso";
        if (imc < 25.0) return "Peso normal";
        if (imc < 30.0) return "Sobrepeso";
        if (imc < 35.0) return "Obesidad grado I";
        if (imc < 40.0) return "Obesidad grado II";
        return "Obesidad grado III";
    }
}
