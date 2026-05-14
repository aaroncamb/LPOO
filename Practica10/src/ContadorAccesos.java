/**
 * Práctica 10 — Contador compartido entre todos los torniquetes.
 *
 * Lleva el total de accesos del dia y el desglose por categoria.
 * Es el ejemplo canonico de "recurso compartido" en concurrencia: si
 * dos torniquetes incrementan el contador a la vez SIN sincronizacion,
 * se pierden incrementos por race condition.
 *
 * El metodo no sincronizado `registrarSinSinc` esta a proposito: lo uso
 * en DemoRaceCondition para mostrar empiricamente que el contador
 * pierde cuentas. Los metodos correctos son `registrar` y `getTotal`
 * (ambos synchronized).
 *
 * Por que sincronizar getTotal tambien:
 *   En un sistema con caches por CPU, sin synchronized otro hilo
 *   podria leer un valor "viejo" del contador. synchronized garantiza
 *   visibilidad: todos los hilos ven el valor actualizado mas reciente.
 */
public class ContadorAccesos {

    private int total = 0;
    private int totalVIP = 0;
    private int totalPremium = 0;
    private int totalBasica = 0;

    /**
     * Version SIN sincronizar - solo para demostrar la race.
     * No usar en codigo real.
     *
     * Insertamos Thread.yield() entre la lectura y la escritura para
     * FORZAR un cambio de contexto y exhibir la race condition de
     * forma reproducible. En codigo real, este `yield` no estaria;
     * lo dejo aqui solo con fines didacticos. Sin el, las
     * optimizaciones de la JVM moderna a veces "esconden" la race en
     * ciertos hardware y la demo no es contundente.
     */
    public void registrarSinSinc(String tipoMembresia) {
        // EN UN SOLO HILO esto seria atomico. EN MULTIPLES HILOS, no.
        // El "total++" se compila en 3 instrucciones:
        //   1. leer total a un registro
        //   2. incrementar el registro
        //   3. escribir el registro de vuelta a memoria
        // Si dos hilos hacen los pasos en orden intercalado, pueden
        // leer el mismo valor inicial y escribir el mismo valor final,
        // perdiendo un incremento.
        int leido = total;
        Thread.yield();  // forzar cambio de contexto para exhibir la race
        total = leido + 1;

        switch (tipoMembresia) {
            case "VIP"     -> { int v = totalVIP;     Thread.yield(); totalVIP     = v + 1; }
            case "PREMIUM" -> { int p = totalPremium; Thread.yield(); totalPremium = p + 1; }
            case "BASICA"  -> { int b = totalBasica;  Thread.yield(); totalBasica  = b + 1; }
        }
    }

    /**
     * Version CORRECTA con synchronized. Garantiza que solo un hilo
     * a la vez pueda estar dentro del metodo. El monitor implícito
     * es la propia instancia (this).
     */
    public synchronized void registrar(String tipoMembresia) {
        total++;
        switch (tipoMembresia) {
            case "VIP"     -> totalVIP++;
            case "PREMIUM" -> totalPremium++;
            case "BASICA"  -> totalBasica++;
        }
    }

    public synchronized int getTotal()        { return total; }
    public synchronized int getTotalVIP()     { return totalVIP; }
    public synchronized int getTotalPremium() { return totalPremium; }
    public synchronized int getTotalBasica()  { return totalBasica; }

    public synchronized void reset() {
        total = 0;
        totalVIP = 0;
        totalPremium = 0;
        totalBasica = 0;
    }

    @Override
    public synchronized String toString() {
        return String.format("Accesos: total=%d (VIP=%d Premium=%d Basica=%d)",
                total, totalVIP, totalPremium, totalBasica);
    }
}
