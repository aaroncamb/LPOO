package com.gympos.service;

import com.gympos.exceptions.EntradaInvalidaException;
import com.gympos.exceptions.MembresiaVencidaException;
import com.gympos.model.Cliente;
import com.gympos.model.RegistroAcceso;
import com.gympos.persistence.GestorArchivos;
import com.gympos.util.Loggers;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * GymPOS - Modulo de control de acceso.
 *
 * Registra cada entrada y salida del gimnasio. Antes de permitir
 * entrada, consulta a SistemaMembresias.verificarVigencia() para
 * rechazar a quienes tengan membresia vencida.
 *
 * El "torniquete" en GymPOS es ARQUITECTONICAMENTE el mismo concepto
 * de P10 (consumidor), pero aqui simplificado: las entradas se
 * registran sincronicamente cuando un recepcionista escanea al cliente
 * en la UI. En un sistema real, los torniquetes correrian como hilos
 * separados como en P10.
 */
public class ControlAcceso {

    private static final String ARCHIVO_DATOS = "data/accesos.dat";

    private final List<RegistroAcceso> registros = new ArrayList<>();
    private final SistemaMembresias sistemaMembresias;
    private final GestorArchivos archivos;
    private int siguienteIdRegistro = 1;

    public ControlAcceso(SistemaMembresias sistemaMembresias, GestorArchivos archivos) {
        this.sistemaMembresias = sistemaMembresias;
        this.archivos = archivos;
    }

    // ============================================================
    //   CARGA / PERSISTENCIA
    // ============================================================

    public void cargarDesdeDisco() throws IOException, ClassNotFoundException {
        List<RegistroAcceso> cargados = archivos.cargarLista(ARCHIVO_DATOS);
        registros.addAll(cargados);
        for (RegistroAcceso r : cargados) {
            if (r.getIdRegistro() >= siguienteIdRegistro) {
                siguienteIdRegistro = r.getIdRegistro() + 1;
            }
        }
        Loggers.info("ControlAcceso: " + registros.size() + " accesos cargados.");
    }

    public void guardarEnDisco() throws IOException {
        archivos.guardarLista(ARCHIVO_DATOS, registros);
    }

    // ============================================================
    //   OPERACIONES
    // ============================================================

    /**
     * Registra una entrada. Antes valida que el cliente tenga membresia
     * vigente; si no, lanza MembresiaVencidaException.
     */
    public RegistroAcceso registrarEntrada(Cliente cliente, String torniquete)
            throws MembresiaVencidaException {
        if (cliente == null) {
            throw new EntradaInvalidaException("cliente", null, "obligatorio");
        }
        sistemaMembresias.verificarVigencia(cliente.getId(), cliente.getNombreCompleto());

        RegistroAcceso r = new RegistroAcceso(siguienteIdRegistro++,
                cliente.getId(),
                RegistroAcceso.TipoMovimiento.ENTRADA,
                torniquete);
        registros.add(r);
        Loggers.info("Entrada: " + r);
        return r;
    }

    /**
     * Registra una salida. No requiere verificar membresia (el cliente
     * ya esta adentro; lo dejamos salir igual).
     */
    public RegistroAcceso registrarSalida(int idCliente, String torniquete) {
        if (idCliente <= 0) {
            throw new EntradaInvalidaException("idCliente", idCliente, "positivo");
        }
        RegistroAcceso r = new RegistroAcceso(siguienteIdRegistro++,
                idCliente,
                RegistroAcceso.TipoMovimiento.SALIDA,
                torniquete);
        registros.add(r);
        Loggers.info("Salida: " + r);
        return r;
    }

    // ============================================================
    //   CONSULTAS
    // ============================================================

    public List<RegistroAcceso> registrosDelDia(LocalDate fecha) {
        List<RegistroAcceso> resultado = new ArrayList<>();
        for (RegistroAcceso r : registros) {
            if (r.getMomento().toLocalDate().equals(fecha)) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    public List<RegistroAcceso> registrosDeCliente(int idCliente) {
        List<RegistroAcceso> resultado = new ArrayList<>();
        for (RegistroAcceso r : registros) {
            if (r.getIdCliente() == idCliente) resultado.add(r);
        }
        return resultado;
    }

    public Optional<RegistroAcceso> ultimoRegistroDeCliente(int idCliente) {
        RegistroAcceso ultimo = null;
        for (RegistroAcceso r : registros) {
            if (r.getIdCliente() == idCliente) {
                if (ultimo == null || r.getMomento().isAfter(ultimo.getMomento())) {
                    ultimo = r;
                }
            }
        }
        return Optional.ofNullable(ultimo);
    }

    public int totalEntradasHoy() {
        LocalDate hoy = LocalDate.now();
        return (int) registros.stream()
                .filter(r -> r.getTipo() == RegistroAcceso.TipoMovimiento.ENTRADA)
                .filter(r -> r.getMomento().toLocalDate().equals(hoy))
                .count();
    }

    public List<RegistroAcceso> todos() {
        return new ArrayList<>(registros);
    }
}
