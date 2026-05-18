package com.gympos.controller;

import com.gympos.concurrency.TareaBackup;
import com.gympos.persistence.BackupManager;
import com.gympos.persistence.ConfigManager;
import com.gympos.persistence.GestorArchivos;
import com.gympos.service.ControlAcceso;
import com.gympos.service.DatosPrueba;
import com.gympos.service.GeneradorReportes;
import com.gympos.service.GestionClientes;
import com.gympos.service.ProcesadorPagos;
import com.gympos.service.SistemaMembresias;
import com.gympos.util.Loggers;
import com.gympos.model.ClaseGrupal;
import com.gympos.model.Cliente;
import com.gympos.model.Equipo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * GymPOS - Contexto de la aplicacion.
 *
 * Centraliza la inicializacion y el acceso a TODOS los servicios.
 * Funciona como un "service locator" sencillo: los controllers piden
 * `AppContext.get().getGestionClientes()` y obtienen la instancia
 * compartida.
 *
 * No es un singleton clasico para evitar problemas de inicializacion:
 * App.java lo construye explicitamente al inicio y lo expone con
 * AppContext.set(...).
 *
 * Carga los datos al iniciar y hace backup al cerrar.
 */
public class AppContext {

    private static AppContext instancia;

    private final ConfigManager config;
    private final GestorArchivos archivos;
    private final BackupManager backupManager;
    private final GestionClientes gestionClientes;
    private final SistemaMembresias sistemaMembresias;
    private final ProcesadorPagos procesadorPagos;
    private final ControlAcceso controlAcceso;
    private final GeneradorReportes generadorReportes;

    // Listas en memoria (no servicios; los expongo directo)
    private final List<ClaseGrupal> clasesGrupales = new ArrayList<>();
    private final List<Equipo> equipos = new ArrayList<>();

    // ============================================================
    //   CONSTRUCCION
    // ============================================================

    private AppContext() throws IOException, ClassNotFoundException {
        // 1) Configuracion
        this.config = new ConfigManager();
        this.config.cargar("config.properties");

        // 2) Logger configurado segun config
        Loggers.configurar(config.getString("ruta.log", "data/operaciones.log"));
        Loggers.info("=== GymPOS iniciando ===");

        // 3) Persistencia base
        this.archivos = new GestorArchivos();
        archivos.asegurarDirectorio(config.getString("ruta.datos", "data"));
        archivos.asegurarDirectorio(config.getString("ruta.backups", "data/backups"));
        archivos.asegurarDirectorio(config.getString("ruta.reportes", "data/reportes"));

        this.backupManager = new BackupManager(
                config.getString("ruta.backups", "data/backups"), archivos);

        // 4) Servicios de negocio
        this.gestionClientes = new GestionClientes(archivos);
        this.sistemaMembresias = new SistemaMembresias(archivos);

        this.procesadorPagos = new ProcesadorPagos(
                config.getDouble("fiscal.iva", 0.16),
                config.getInt("puntos.por.peso.pagado", 1));

        this.controlAcceso = new ControlAcceso(sistemaMembresias, archivos);

        this.generadorReportes = new GeneradorReportes(
                gestionClientes, sistemaMembresias, procesadorPagos,
                controlAcceso, archivos,
                config.getString("gym.nombre", "Gimnasio"));

        // 5) Cargar datos persistidos (o sembrar con datos de prueba)
        cargarDatos();
    }

    /**
     * Si los archivos de datos existen, carga lo guardado. Si no
     * (primera ejecucion), siembra el sistema con DatosPrueba para
     * que el usuario tenga contenido inmediatamente.
     */
    private void cargarDatos() throws IOException, ClassNotFoundException {
        if (archivos.existe("data/clientes.dat")) {
            gestionClientes.cargarDesdeDisco();
            sistemaMembresias.cargarDesdeDisco();
            controlAcceso.cargarDesdeDisco();
            // Clases y equipos se cargan de sus respectivos archivos
            cargarClases();
            cargarEquipos();
            Loggers.info("Datos cargados desde disco.");
        } else {
            sembrarDatosPrueba();
            Loggers.info("Primera ejecucion: sistema sembrado con datos de prueba.");
        }
    }

    private void cargarClases() throws IOException, ClassNotFoundException {
        if (archivos.existe("data/clases.dat")) {
            List<ClaseGrupal> c = archivos.cargarLista("data/clases.dat");
            clasesGrupales.addAll(c);
        } else {
            clasesGrupales.addAll(DatosPrueba.generarClasesGrupales());
        }
    }

    private void cargarEquipos() throws IOException, ClassNotFoundException {
        if (archivos.existe("data/equipos.dat")) {
            List<Equipo> e = archivos.cargarLista("data/equipos.dat");
            equipos.addAll(e);
        } else {
            equipos.addAll(DatosPrueba.generarEquipos());
        }
    }

    /**
     * Siembra el sistema con los datos de prueba en primera ejecucion.
     * Tambien crea automaticamente una membresia por cada cliente.
     */
    private void sembrarDatosPrueba() throws IOException {
        // Clientes
        List<Cliente> semillaClientes = DatosPrueba.generarClientes();
        for (Cliente c : semillaClientes) {
            gestionClientes.agregar(c);
        }

        // Membresia por cada cliente, usando precios de config
        double precioBasica = config.getDouble("membresia.basica.precio", 400);
        double precioPremium = config.getDouble("membresia.premium.precio", 750);
        double precioAnualVIP = config.getDouble("membresia.vip.precio.anual", 8000);

        for (Cliente c : semillaClientes) {
            double precioMensual = switch (c.getTipoMembresia()) {
                case BASICA  -> precioBasica;
                case PREMIUM -> precioPremium;
                case VIP     -> precioAnualVIP;   // ignorado por VIP, usa precioAnual
            };
            sistemaMembresias.crear(c.getId(), c.getTipoMembresia(),
                    precioMensual, precioAnualVIP);
        }

        // Clases y equipos
        clasesGrupales.addAll(DatosPrueba.generarClasesGrupales());
        equipos.addAll(DatosPrueba.generarEquipos());

        // Persistir todo
        guardarTodo();
    }

    /** Persiste todos los modulos. */
    public void guardarTodo() throws IOException {
        gestionClientes.guardarEnDisco();
        sistemaMembresias.guardarEnDisco();
        controlAcceso.guardarEnDisco();
        archivos.guardarLista("data/clases.dat", clasesGrupales);
        archivos.guardarLista("data/equipos.dat", equipos);
        Loggers.info("Estado completo persistido.");
    }

    /** Construye una tarea de backup de todos los archivos de datos. */
    public TareaBackup tareaBackupCompleto() {
        return new TareaBackup(backupManager, List.of(
                "data/clientes.dat",
                "data/membresias.dat",
                "data/accesos.dat",
                "data/clases.dat",
                "data/equipos.dat"
        ));
    }

    // ============================================================
    //   ACCESORES
    // ============================================================

    public static AppContext get() {
        if (instancia == null) {
            throw new IllegalStateException(
                    "AppContext no inicializado. Llama a AppContext.crear() primero.");
        }
        return instancia;
    }

    /** Inicializa el contexto. Debe llamarse antes de cualquier get(). */
    public static synchronized AppContext crear()
            throws IOException, ClassNotFoundException {
        if (instancia == null) {
            instancia = new AppContext();
        }
        return instancia;
    }

    public ConfigManager getConfig()             { return config; }
    public GestorArchivos getArchivos()          { return archivos; }
    public BackupManager getBackupManager()      { return backupManager; }
    public GestionClientes getGestionClientes()  { return gestionClientes; }
    public SistemaMembresias getSistemaMembresias() { return sistemaMembresias; }
    public ProcesadorPagos getProcesadorPagos()  { return procesadorPagos; }
    public ControlAcceso getControlAcceso()      { return controlAcceso; }
    public GeneradorReportes getGeneradorReportes() { return generadorReportes; }
    public List<ClaseGrupal> getClasesGrupales() { return clasesGrupales; }
    public List<Equipo> getEquipos()             { return equipos; }
}
