package com.gympos.service;

import com.gympos.exceptions.EntradaInvalidaException;
import com.gympos.exceptions.MembresiaVencidaException;
import com.gympos.model.Membresia;
import com.gympos.model.MembresiaBasica;
import com.gympos.model.MembresiaPremium;
import com.gympos.model.MembresiaVIP;
import com.gympos.persistence.GestorArchivos;
import com.gympos.util.Loggers;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * GymPOS - Modulo de gestion de membresias.
 *
 * Mantiene las membresias activas del gimnasio organizadas por:
 *   - Lista general para iteracion.
 *   - Mapa idMembresia -> Membresia (lookup directo).
 *   - Mapa idCliente -> Membresia (un cliente tiene una membresia activa).
 *
 * Expone factory methods para crear membresias del tipo correcto sin
 * que el codigo cliente tenga que conocer las subclases.
 */
public class SistemaMembresias {

    private static final String ARCHIVO_DATOS = "data/membresias.dat";

    private final List<Membresia> membresias = new ArrayList<>();
    private final Map<Integer, Membresia> porId = new HashMap<>();
    private final Map<Integer, Membresia> porCliente = new HashMap<>();
    private int siguienteId = 1;
    private final GestorArchivos archivos;

    public SistemaMembresias(GestorArchivos archivos) {
        this.archivos = archivos;
    }

    // ============================================================
    //   CARGA / PERSISTENCIA
    // ============================================================

    public void cargarDesdeDisco() throws IOException, ClassNotFoundException {
        List<Membresia> cargadas = archivos.cargarLista(ARCHIVO_DATOS);
        for (Membresia m : cargadas) {
            membresias.add(m);
            porId.put(m.getIdMembresia(), m);
            // Si el cliente ya tiene una membresia previa, prevalece la mas nueva.
            Membresia existente = porCliente.get(m.getIdCliente());
            if (existente == null
                    || m.getFechaInicio().isAfter(existente.getFechaInicio())) {
                porCliente.put(m.getIdCliente(), m);
            }
            if (m.getIdMembresia() >= siguienteId) {
                siguienteId = m.getIdMembresia() + 1;
            }
        }
        Loggers.info("SistemaMembresias: " + membresias.size() + " membresias cargadas.");
    }

    public void guardarEnDisco() throws IOException {
        archivos.guardarLista(ARCHIVO_DATOS, membresias);
    }

    // ============================================================
    //   FACTORY METHODS
    // ============================================================

    /**
     * Crea una membresia para un cliente segun el tipo. El precio
     * mensual lo define el llamador (puede venir de config.properties).
     */
    public Membresia crear(int idCliente,
                           com.gympos.model.Cliente.TipoMembresia tipo,
                           double precioMensual,
                           double precioAnualVIP) {
        if (idCliente <= 0) {
            throw new EntradaInvalidaException("idCliente", idCliente, "debe ser positivo");
        }
        if (tipo == null) {
            throw new EntradaInvalidaException("tipo", null, "tipo obligatorio");
        }

        Membresia m = switch (tipo) {
            case BASICA  -> new MembresiaBasica(siguienteId, idCliente,
                                                 LocalDate.now(), precioMensual);
            case PREMIUM -> new MembresiaPremium(siguienteId, idCliente,
                                                  LocalDate.now(), precioMensual);
            case VIP     -> new MembresiaVIP(siguienteId, idCliente,
                                              LocalDate.now(), precioAnualVIP);
        };
        siguienteId++;
        membresias.add(m);
        porId.put(m.getIdMembresia(), m);
        porCliente.put(idCliente, m);
        Loggers.info("Membresia creada: " + m);
        return m;
    }

    // ============================================================
    //   CONSULTA
    // ============================================================

    public Optional<Membresia> buscarPorId(int id) {
        return Optional.ofNullable(porId.get(id));
    }

    public Optional<Membresia> membresiaActivaDe(int idCliente) {
        return Optional.ofNullable(porCliente.get(idCliente));
    }

    public List<Membresia> todas() { return new ArrayList<>(membresias); }

    public int total() { return membresias.size(); }

    // ============================================================
    //   OPERACIONES DE NEGOCIO
    // ============================================================

    /**
     * Renueva la membresia de un cliente. Si esta vencida, primero la
     * informa lanzando excepcion (el llamador puede decidir si renovar
     * o cobrar penalizacion).
     */
    public void renovar(int idCliente) throws MembresiaVencidaException {
        Membresia m = porCliente.get(idCliente);
        if (m == null) {
            throw new EntradaInvalidaException(
                    "idCliente", idCliente, "no tiene membresia registrada");
        }

        if (!m.estaVigente()) {
            // Avisamos pero seguimos renovando: la decision la deja al UI.
            Loggers.warn("Renovando membresia VENCIDA: cliente " + idCliente);
        }
        m.renovar();
        Loggers.info("Membresia renovada: " + m);
    }

    /**
     * Verifica vigencia y lanza MembresiaVencidaException si no esta
     * vigente. Llamado por ControlAcceso antes de permitir entrada.
     */
    public void verificarVigencia(int idCliente, String nombreCliente)
            throws MembresiaVencidaException {
        Membresia m = porCliente.get(idCliente);
        if (m == null) {
            throw new EntradaInvalidaException(
                    "idCliente", idCliente, "no tiene membresia");
        }
        if (!m.estaVigente()) {
            throw new MembresiaVencidaException(nombreCliente, m.getFechaVencimiento());
        }
    }

    /** Lista de membresias por vencer en los proximos N dias. */
    public List<Membresia> porVencerEn(int dias) {
        List<Membresia> resultado = new ArrayList<>();
        for (Membresia m : membresias) {
            if (m.isActiva()) {
                long dv = m.diasParaVencer();
                if (dv >= 0 && dv <= dias) resultado.add(m);
            }
        }
        return resultado;
    }

    /** Lista de membresias YA vencidas, util para reportes. */
    public List<Membresia> vencidas() {
        List<Membresia> resultado = new ArrayList<>();
        for (Membresia m : membresias) {
            if (m.isActiva() && m.diasParaVencer() < 0) resultado.add(m);
        }
        return resultado;
    }
}
