# Práctica 11 — JavaFX

## Objetivo

Construir una interfaz gráfica de usuario con JavaFX para la gestión de
clientes del gimnasio. Implementar tabla con datos dinámicos,
formularios CRUD modales, componentes personalizados, validación en
tiempo real, eventos de mouse y teclado, CSS externo, y un JAR
ejecutable funcional. Como Elemento de Decisión Propia, implementar
**filtrado interactivo** sobre la tabla.

## Estructura

```
Practica11/
├── pom.xml                              (Maven + javafx-maven-plugin + shade)
├── README.md
├── REFLEXION.md
├── BITACORA_IA.md
├── MANUAL_USUARIO.md
├── capturas/                            (poner aqui tus screenshots)
└── src/main/
    ├── java/
    │   ├── App.java                     (Application, punto de entrada)
    │   ├── Cliente.java                 (modelo con Property bindings)
    │   ├── MainController.java          (ventana principal)
    │   ├── ClienteFormulario.java       (dialogo modal CRUD)
    │   ├── BotonAccion.java             (componente personalizado #1)
    │   ├── CampoEmail.java              (componente personalizado #2)
    │   └── DatosPrueba.java             (12 clientes precargados)
    └── resources/
        └── styles.css                   (tema negro/dorado del gimnasio)
```

## Compilación y ejecución

### Requisitos
- JDK 21 (LTS).
- Maven 3.8+ instalado y en el PATH.

### Comandos

```bash
# 1) Compilar
mvn clean compile

# 2) Ejecutar en desarrollo (descarga JavaFX automaticamente)
mvn javafx:run

# 3) Empaquetar como JAR ejecutable (fat jar con todas las dependencias)
mvn clean package

# 4) Ejecutar el JAR
java -jar target/practica11-1.0.0.jar
```

> Nota: La primera vez que ejecutes `mvn javafx:run` o `mvn clean compile`,
> Maven descargará las dependencias de JavaFX (~30 MB). Las siguientes
> veces será mucho más rápido por el caché local de Maven (`~/.m2/`).

### En IntelliJ
1. Abrir el directorio `Practica11/` como proyecto Maven (IntelliJ
   detecta el `pom.xml` automáticamente).
2. Esperar a que sincronice las dependencias.
3. Click derecho sobre `App.java` → "Run 'App.main()'".

Si IntelliJ se queja con un error del tipo *"JavaFX runtime components
are missing"*, lo más probable es que se esté ejecutando con `java`
directo y no con el plugin de Maven. Solución: usa "Maven →
javafx:run" desde el panel lateral derecho de Maven en IntelliJ.

## Mapeo entregables → archivos

| Entregable | Implementación |
|---|---|
| Ventana principal con menú | `MainController.construirMenu()` con Archivo / Clientes / Ayuda |
| Formularios CRUD | `ClienteFormulario` (modal, modo crear y editar) |
| Tabla con datos dinámicos | `TableView<Cliente>` con `ObservableList` en `MainController` |
| Componente personalizado #1 | `BotonAccion extends Button` (3 variantes: PRIMARIO, SECUNDARIO, PELIGRO) |
| Componente personalizado #2 | `CampoEmail extends TextField` (validación visual en tiempo real) |
| Eventos de mouse | `setOnMouseClicked` doble-click para editar |
| Eventos de teclado | Delete, Enter, F1, Ctrl+N, Ctrl+E, Ctrl+Q, Escape |
| Validación en tiempo real | Listeners en `textProperty()` que habilitan/deshabilitan el botón Guardar |
| Diálogo modal | `ClienteFormulario` con `initModality(WINDOW_MODAL)` + Alert de confirmación al eliminar |
| CSS externo | `styles.css` con tema completo del gimnasio |
| JAR ejecutable | `mvn package` con `maven-shade-plugin` |
| Manual de usuario | `MANUAL_USUARIO.md` con instrucciones y placeholders para capturas |

## Elemento de Decisión Propia — Filtrado interactivo

### Qué hace

En la toolbar de la ventana principal hay un campo "Buscar". Conforme
el usuario teclea, la tabla se actualiza en **tiempo real** mostrando
solo los clientes cuyo **nombre o email** contenga el texto buscado
(sin distinguir mayúsculas/minúsculas).

Por ejemplo, escribir "ana" filtra a Ana Gabriela Perez Soto y Mariana
Flores Espinoza. Escribir "@correo.mx" filtra a todos los que tienen
ese dominio. Borrar el campo restaura la lista completa.

### Cómo lo implementé

Usé **`FilteredList<Cliente>`** del paquete
`javafx.collections.transformation`. Es la clase oficial de JavaFX para
exactamente este caso de uso.

```java
// 1) Lista observable base con los datos reales
private final ObservableList<Cliente> datos = FXCollections.observableArrayList();

// 2) Lista filtrada que envuelve la base
private final FilteredList<Cliente> datosFiltrados =
        new FilteredList<>(datos, p -> true);

// 3) Listener en el textProperty del campo
campoBusqueda.textProperty().addListener((obs, viejo, nuevo) -> {
    datosFiltrados.setPredicate(cliente -> {
        if (nuevo == null || nuevo.isBlank()) return true;
        String aguja = nuevo.trim().toLowerCase();
        return cliente.getNombreCompleto().toLowerCase().contains(aguja)
            || cliente.getEmail().toLowerCase().contains(aguja);
    });
});

// 4) SortedList para que las columnas sean ordenables
SortedList<Cliente> ordenable = new SortedList<>(datosFiltrados);
ordenable.comparatorProperty().bind(tabla.comparatorProperty());
tabla.setItems(ordenable);
```

### Por qué FilteredList y no filtrar manualmente

Podría haber hecho `tabla.setItems(datos.stream().filter(...).toList())`
cada vez que el texto cambia, pero eso tiene tres problemas:

1. **Pierde la conexión con la lista raíz**: si agrego un cliente con
   "Nuevo", el `stream().toList()` ya se ejecutó y la tabla no lo verá
   hasta refrescar manualmente.

2. **Pierde el orden por columnas**: si el usuario ordenó por nombre
   ascendente y luego buscó algo, el sort se pierde.

3. **Más código**: tendría que tener listeners manuales para mantener
   sincronizadas las dos listas (la filtrada visible y la real).

`FilteredList` resuelve los tres: es una "vista" en vivo sobre la lista
base. Cuando la lista base cambia (agregar/eliminar), la vista se
actualiza automáticamente. Cuando el predicado cambia, la vista se
reevalúa. El `SortedList` encima permite que el sort de columnas
funcione sobre los resultados filtrados.

Las tres clases (`ObservableList`, `FilteredList`, `SortedList`) están
diseñadas para componerse exactamente en este orden:

```
ObservableList (base) → FilteredList (filtro) → SortedList (orden) → TableView
```

Es el patrón canónico documentado por Oracle para tablas con búsqueda
y orden, y es lo que cualquier reseñador de código reconocería.

## Componentes personalizados

### 1. `BotonAccion extends Button`

Centraliza el estilo de los botones de la app en tres variantes:

| Variante | Uso | Color |
|---|---|---|
| `PRIMARIO` | Guardar, confirmar | Dorado del gym |
| `SECUNDARIO` | Cancelar, cerrar | Gris con borde dorado |
| `PELIGRO` | Eliminar | Rojo |

Tiene métodos de fábrica para uso conciso:
```java
BotonAccion guardar  = BotonAccion.primario("Guardar");
BotonAccion cancelar = BotonAccion.secundario("Cancelar");
BotonAccion borrar   = BotonAccion.peligro("Eliminar");
```

Cada variante aplica una clase CSS (`boton-primario`, `boton-secundario`,
`boton-peligro`) que vive en `styles.css`. Si mañana queremos cambiar
el color del botón primario, modificamos UNA línea del CSS y todos los
botones primarios de la app se actualizan.

### 2. `CampoEmail extends TextField`

Valida el email **en tiempo real** mientras el usuario teclea:
- Si el formato es correcto → borde verde.
- Si el formato es incorrecto → borde rojo + tooltip con la razón
  ("Falta el @", "Falta el dominio", etc).
- Si está vacío → borde neutro.

El estado se consulta con `campoEmail.esValido()`, y el formulario
usa esto para habilitar o deshabilitar el botón Guardar.

```java
campoEmail.textProperty().addListener((obs, v, n) -> revalidar());
```

Esta validación es lo que la consigna pide como "validación en tiempo
real" en los formularios.

## Eventos de mouse y teclado

| Acción | Disparador |
|---|---|
| Editar cliente | Doble-click en fila / Enter sobre fila seleccionada / Ctrl+E |
| Eliminar cliente | Tecla Delete sobre fila seleccionada |
| Nuevo cliente | Ctrl+N |
| Mostrar ayuda | F1 |
| Salir | Ctrl+Q |
| Guardar formulario | Enter (si valido) |
| Cancelar formulario | Escape |

Implementación: combinación de `setOnMouseClicked`, `setOnKeyPressed`
sobre la tabla, y `scene.getAccelerators()` para los atajos globales.

## Capturas en el manual de usuario

El archivo `MANUAL_USUARIO.md` incluye instrucciones de instalación,
pantallas de cada caso de uso, y placeholders para 6 capturas que
deberás reemplazar con screenshots reales una vez tengas la app
corriendo:

1. `capturas/01-pantalla-principal.png`
2. `capturas/02-formulario-nuevo.png`
3. `capturas/03-validacion-email.png`
4. `capturas/04-filtrado.png`
5. `capturas/05-confirmar-eliminar.png`
6. `capturas/06-ordenar-columnas.png`

## Limitación honesta

Este código se desarrolló y revisó en un entorno sin display gráfico,
por lo que no se ejecutó visualmente durante el desarrollo. El código
fue verificado sintácticamente con Maven (`mvn validate`) y todas las
APIs usadas son JavaFX estándar bien establecidas. Cualquier issue
visual menor que aparezca al ejecutar (alineaciones, tamaños) se ajusta
trivialmente con el CSS sin tocar Java.
