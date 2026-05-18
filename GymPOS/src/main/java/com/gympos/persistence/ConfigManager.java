package com.gympos.persistence;

import com.gympos.util.Loggers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * GymPOS - Manager de configuracion externa.
 *
 * Lee config.properties al iniciar la aplicacion. Si el archivo no
 * existe en el directorio de trabajo, intenta cargarlo desde los
 * recursos del classpath (util cuando se ejecuta desde el JAR).
 *
 * Expone metodos tipados (getDouble, getInt, getString) con valores
 * por defecto, asi el codigo cliente puede preguntar configuracion sin
 * preocuparse del parseo.
 */
public class ConfigManager {

    private final Properties props = new Properties();
    private boolean cargado = false;

    /**
     * Carga el archivo de configuracion. Intenta primero el directorio
     * de trabajo, luego el classpath. Si ninguno funciona, deja la
     * configuracion vacia (los getters devolveran valores por defecto).
     */
    public void cargar(String rutaArchivo) {
        // 1) Intentar desde el filesystem (directorio de trabajo)
        Path p = Path.of(rutaArchivo);
        if (Files.exists(p)) {
            try (InputStream in = Files.newInputStream(p)) {
                props.load(in);
                cargado = true;
                Loggers.info("Configuracion cargada desde " + rutaArchivo);
                return;
            } catch (IOException e) {
                Loggers.warn("No se pudo leer " + rutaArchivo + ": " + e.getMessage());
            }
        }

        // 2) Intentar desde el classpath (dentro del JAR)
        try (InputStream in = getClass().getResourceAsStream("/" + rutaArchivo)) {
            if (in != null) {
                props.load(in);
                cargado = true;
                Loggers.info("Configuracion cargada desde classpath");
                return;
            }
        } catch (IOException e) {
            Loggers.warn("No se pudo leer configuracion del classpath: " + e.getMessage());
        }

        Loggers.warn("No se encontro " + rutaArchivo + ". Usando valores por defecto.");
    }

    public boolean estaCargado() { return cargado; }

    public String getString(String clave, String porDefecto) {
        return props.getProperty(clave, porDefecto);
    }

    public int getInt(String clave, int porDefecto) {
        String v = props.getProperty(clave);
        if (v == null) return porDefecto;
        try { return Integer.parseInt(v.trim()); }
        catch (NumberFormatException e) {
            Loggers.warn("Config: '" + clave + "' no es entero: " + v);
            return porDefecto;
        }
    }

    public double getDouble(String clave, double porDefecto) {
        String v = props.getProperty(clave);
        if (v == null) return porDefecto;
        try { return Double.parseDouble(v.trim()); }
        catch (NumberFormatException e) {
            Loggers.warn("Config: '" + clave + "' no es decimal: " + v);
            return porDefecto;
        }
    }

    public boolean getBoolean(String clave, boolean porDefecto) {
        String v = props.getProperty(clave);
        if (v == null) return porDefecto;
        return Boolean.parseBoolean(v.trim());
    }
}
