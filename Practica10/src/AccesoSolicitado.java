import java.time.LocalDateTime;

/**
 * Práctica 10 — Mensaje que viaja por el buffer compartido.
 *
 * Representa "un cliente que llego al gimnasio y espera ser atendido
 * por algun torniquete". Inmutable a proposito: una vez creado, no se
 * puede modificar. Esto es valioso en concurrencia porque elimina la
 * posibilidad de que dos hilos lean campos en estado inconsistente.
 *
 * Llevar la marca de tiempo en que el cliente llego (no en que se
 * proceso) nos permite medir el tiempo de espera real.
 */
public final class AccesoSolicitado {

    private final int idCliente;
    private final String nombreCliente;
    private final LocalDateTime momentoLlegada;

    public AccesoSolicitado(int idCliente, String nombreCliente) {
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.momentoLlegada = LocalDateTime.now();
    }

    public int getIdCliente()                 { return idCliente; }
    public String getNombreCliente()          { return nombreCliente; }
    public LocalDateTime getMomentoLlegada()  { return momentoLlegada; }

    @Override
    public String toString() {
        return String.format("Cliente#%d (%s)", idCliente, nombreCliente);
    }
}
