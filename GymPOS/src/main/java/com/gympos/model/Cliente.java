package com.gympos.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Cliente del gimnasio.
 *
 * Combina dos requisitos que parecen contradictorios:
 *   - Property de JavaFX para que la UI sea reactiva (TableView se
 *     refresca solo cuando los campos cambian).
 *   - Serializable para guardarse/cargarse del disco con
 *     ObjectOutputStream.
 *
 * Como las clases Property NO son Serializable por defecto, uso
 * writeObject/readObject personalizados que serializan solo los valores
 * primitivos y reconstruyen las Properties al deserializar.
 *
 * El campo `puntos` se usa por el sistema de recompensas (cuanto mas
 * gasta el cliente, mas puntos acumula; los puntos se pueden canjear).
 */
public class Cliente implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum TipoMembresia { BASICA, PREMIUM, VIP }

    // Las Property son transient porque NO son Serializable.
    // Reconstruimos manualmente en readObject.
    private transient IntegerProperty id;
    private transient StringProperty  nombreCompleto;
    private transient StringProperty  email;
    private transient ObjectProperty<LocalDate> fechaRegistro;
    private transient DoubleProperty  pesoKg;
    private transient ObjectProperty<TipoMembresia> tipoMembresia;
    private transient BooleanProperty activo;
    private transient IntegerProperty puntos;

    public Cliente(int id, String nombreCompleto, String email,
                   LocalDate fechaRegistro, double pesoKg,
                   TipoMembresia tipoMembresia) {
        this.id             = new SimpleIntegerProperty(id);
        this.nombreCompleto = new SimpleStringProperty(nombreCompleto);
        this.email          = new SimpleStringProperty(email);
        this.fechaRegistro  = new SimpleObjectProperty<>(fechaRegistro);
        this.pesoKg         = new SimpleDoubleProperty(pesoKg);
        this.tipoMembresia  = new SimpleObjectProperty<>(tipoMembresia);
        this.activo         = new SimpleBooleanProperty(true);
        this.puntos         = new SimpleIntegerProperty(0);
    }

    // -------- Getters / Setters / Property accessors --------

    public int getId()                       { return id.get(); }
    public void setId(int v)                 { id.set(v); }
    public IntegerProperty idProperty()      { return id; }

    public String getNombreCompleto()                { return nombreCompleto.get(); }
    public void setNombreCompleto(String v)          { nombreCompleto.set(v); }
    public StringProperty nombreCompletoProperty()   { return nombreCompleto; }

    public String getEmail()                 { return email.get(); }
    public void setEmail(String v)           { email.set(v); }
    public StringProperty emailProperty()    { return email; }

    public LocalDate getFechaRegistro()                      { return fechaRegistro.get(); }
    public void setFechaRegistro(LocalDate v)                { fechaRegistro.set(v); }
    public ObjectProperty<LocalDate> fechaRegistroProperty() { return fechaRegistro; }

    public double getPesoKg()                { return pesoKg.get(); }
    public void setPesoKg(double v)          { pesoKg.set(v); }
    public DoubleProperty pesoKgProperty()   { return pesoKg; }

    public TipoMembresia getTipoMembresia()                      { return tipoMembresia.get(); }
    public void setTipoMembresia(TipoMembresia v)                { tipoMembresia.set(v); }
    public ObjectProperty<TipoMembresia> tipoMembresiaProperty() { return tipoMembresia; }

    public boolean isActivo()                { return activo.get(); }
    public void setActivo(boolean v)         { activo.set(v); }
    public BooleanProperty activoProperty()  { return activo; }

    public int getPuntos()                   { return puntos.get(); }
    public void setPuntos(int v)             { puntos.set(v); }
    public IntegerProperty puntosProperty()  { return puntos; }

    // -------- Operaciones de dominio --------

    /** Suma puntos por una operacion (pago, renovacion). */
    public void agregarPuntos(int cantidad) {
        if (cantidad < 0) return;
        puntos.set(puntos.get() + cantidad);
    }

    public boolean canjearPuntos(int cantidad) {
        if (cantidad <= 0 || cantidad > puntos.get()) return false;
        puntos.set(puntos.get() - cantidad);
        return true;
    }

    // -------- Serializacion personalizada --------
    // Como las Property no son Serializable, escribo y leo los valores
    // primitivos manualmente. La estructura del archivo es estable y
    // robusta frente a cambios en JavaFX.

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(getId());
        out.writeUTF(getNombreCompleto());
        out.writeUTF(getEmail());
        out.writeObject(getFechaRegistro());
        out.writeDouble(getPesoKg());
        out.writeUTF(getTipoMembresia().name());
        out.writeBoolean(isActivo());
        out.writeInt(getPuntos());
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        int idVal = in.readInt();
        String nombre = in.readUTF();
        String emailVal = in.readUTF();
        LocalDate fecha = (LocalDate) in.readObject();
        double peso = in.readDouble();
        TipoMembresia tipo = TipoMembresia.valueOf(in.readUTF());
        boolean activoVal = in.readBoolean();
        int puntosVal = in.readInt();

        this.id             = new SimpleIntegerProperty(idVal);
        this.nombreCompleto = new SimpleStringProperty(nombre);
        this.email          = new SimpleStringProperty(emailVal);
        this.fechaRegistro  = new SimpleObjectProperty<>(fecha);
        this.pesoKg         = new SimpleDoubleProperty(peso);
        this.tipoMembresia  = new SimpleObjectProperty<>(tipo);
        this.activo         = new SimpleBooleanProperty(activoVal);
        this.puntos         = new SimpleIntegerProperty(puntosVal);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cliente)) return false;
        return getId() == ((Cliente) o).getId();
    }

    @Override
    public int hashCode() { return Objects.hash(getId()); }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s)", getId(), getNombreCompleto(), getEmail());
    }
}
