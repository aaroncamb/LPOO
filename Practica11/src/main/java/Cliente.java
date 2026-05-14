import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.time.LocalDate;

/**
 * Práctica 11 — Cliente con propiedades JavaFX.
 *
 * A diferencia de las prácticas anteriores donde los atributos eran
 * tipos Java básicos, aquí usamos "Property" (SimpleStringProperty,
 * SimpleIntegerProperty, etc) para que JavaFX pueda hacer binding
 * automatico con los controles UI.
 *
 * El TableView<Cliente> que muestra estos clientes necesita un
 * PropertyValueFactory que mire los metodos *Property() para enlazar
 * cada celda al valor. Asi cuando el modelo cambia, la tabla se
 * refresca automaticamente sin tocar la UI.
 */
public class Cliente {

    public enum TipoMembresia { BASICA, PREMIUM, VIP }

    private final IntegerProperty id;
    private final StringProperty  nombreCompleto;
    private final StringProperty  email;
    private final ObjectProperty<LocalDate> fechaRegistro;
    private final DoubleProperty  pesoKg;
    private final ObjectProperty<TipoMembresia> tipoMembresia;
    private final BooleanProperty activo;

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
    }

    // -------- Getters/Setters (estilo JavaFX) --------

    public int getId()                       { return id.get(); }
    public void setId(int v)                 { id.set(v); }
    public IntegerProperty idProperty()      { return id; }

    public String getNombreCompleto()                { return nombreCompleto.get(); }
    public void setNombreCompleto(String v)          { nombreCompleto.set(v); }
    public StringProperty nombreCompletoProperty()   { return nombreCompleto; }

    public String getEmail()                 { return email.get(); }
    public void setEmail(String v)           { email.set(v); }
    public StringProperty emailProperty()    { return email; }

    public LocalDate getFechaRegistro()                       { return fechaRegistro.get(); }
    public void setFechaRegistro(LocalDate v)                 { fechaRegistro.set(v); }
    public ObjectProperty<LocalDate> fechaRegistroProperty()  { return fechaRegistro; }

    public double getPesoKg()                { return pesoKg.get(); }
    public void setPesoKg(double v)          { pesoKg.set(v); }
    public DoubleProperty pesoKgProperty()   { return pesoKg; }

    public TipoMembresia getTipoMembresia()                       { return tipoMembresia.get(); }
    public void setTipoMembresia(TipoMembresia v)                 { tipoMembresia.set(v); }
    public ObjectProperty<TipoMembresia> tipoMembresiaProperty()  { return tipoMembresia; }

    public boolean isActivo()                { return activo.get(); }
    public void setActivo(boolean v)         { activo.set(v); }
    public BooleanProperty activoProperty()  { return activo; }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s)", getId(), getNombreCompleto(), getEmail());
    }
}
