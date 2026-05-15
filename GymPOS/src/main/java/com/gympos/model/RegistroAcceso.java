package com.gympos.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Registro de un acceso al gimnasio: cuando un cliente entra o sale,
 * se genera uno de estos. Sirve para reportes de asistencia, deteccion
 * de horas pico, etc.
 *
 * Inmutable: una vez creado un registro, no se modifica. Los hechos
 * historicos no cambian.
 */
public class RegistroAcceso implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum TipoMovimiento { ENTRADA, SALIDA }

    private final int idRegistro;
    private final int idCliente;
    private final LocalDateTime momento;
    private final TipoMovimiento tipo;
    private final String numeroTorniquete;   // referencia opcional al torniquete

    public RegistroAcceso(int idRegistro, int idCliente,
                          TipoMovimiento tipo, String numeroTorniquete) {
        if (idRegistro <= 0) throw new IllegalArgumentException("idRegistro debe ser positivo.");
        if (idCliente <= 0)  throw new IllegalArgumentException("idCliente debe ser positivo.");
        if (tipo == null)    throw new IllegalArgumentException("tipo obligatorio.");

        this.idRegistro = idRegistro;
        this.idCliente = idCliente;
        this.momento = LocalDateTime.now();
        this.tipo = tipo;
        this.numeroTorniquete = numeroTorniquete != null ? numeroTorniquete : "manual";
    }

    public int getIdRegistro()           { return idRegistro; }
    public int getIdCliente()            { return idCliente; }
    public LocalDateTime getMomento()    { return momento; }
    public TipoMovimiento getTipo()      { return tipo; }
    public String getNumeroTorniquete()  { return numeroTorniquete; }

    @Override
    public String toString() {
        return String.format("[%s] cliente#%d %s en %s",
                momento, idCliente, tipo, numeroTorniquete);
    }
}
