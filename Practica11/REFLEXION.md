# Reflexión — Práctica 11

## 1. ¿Qué es el hilo de la UI (JavaFX Application Thread) y por qué no debes hacer operaciones pesadas en él?

JavaFX es **single-threaded para la UI**. Toda creación, modificación y
renderizado de componentes (`TableView`, `Button`, `Scene`, etc) ocurre
en **un único hilo** llamado **JavaFX Application Thread** (FXAT).

El método `start(Stage)` de `Application` se ejecuta en este hilo, y
todos los `EventHandler` (clicks, teclas, cambios de propiedad) se
disparan también en él. Es como un bucle de eventos: el FXAT toma
eventos de una cola, ejecuta sus handlers, y vuelve a leer la cola.

### Por qué no hacer operaciones pesadas en él

Si dentro de un `EventHandler` (por ejemplo, un click de botón) ejecuto
una operación que tarda 3 segundos (lectura de archivo, llamada a un
servicio remoto, procesamiento de 50.000 registros), durante esos 3
segundos el FXAT está **ocupado** y no puede procesar otros eventos.

Las consecuencias visibles:
- La UI se **congela**: los botones no responden a clicks, no se
  pueden mover ventanas, no se redibujan controles que estaban
  cambiando, los textos pegados que estaban escribiendo no aparecen.
- El sistema operativo eventualmente marca la app como "no respondiendo".
  En Windows aparece el cuadro gris de "Programa no responde". En
  macOS aparece la rueda arcoiris.
- Si la operación finalmente termina, el usuario podría haber
  acumulado una cola gigante de clicks que se procesan de golpe.

### Cómo evitarlo

Operaciones pesadas se hacen en **otro hilo**, normalmente con
`javafx.concurrent.Task` o un `ExecutorService`:

```java
Task<List<Cliente>> tarea = new Task<>() {
    @Override
    protected List<Cliente> call() throws Exception {
        // hilo de fondo: aqui se puede tardar lo que sea
        return cargarDesdeBaseDeDatos();
    }
};

tarea.setOnSucceeded(e -> {
    // este handler corre DE VUELTA en el FXAT, asi que puede tocar la UI
    tabla.getItems().setAll(tarea.getValue());
});

new Thread(tarea).start();
```

**La regla**: para tocar UI, hay que estar en el FXAT. Para hacer
trabajo pesado, hay que estar en OTRO hilo. Si estás en otro hilo y
necesitas tocar la UI, usas `Platform.runLater(() -> { ... })` que
encola la acción en el FXAT.

En mi código de P11 esto no aparece porque las operaciones son
triviales (filtrar 12 clientes, mostrar un formulario). Pero si esto
fuera una app real conectada a una base de datos, la regla aplicaría
a cada query: nunca en el FXAT.

### Conexión con la P10

En P10 trabajé con `Thread`, `Runnable` y `ExecutorService`
explícitamente. Aquí en P11 el FXAT es **un hilo más en mi
aplicación**, solo que tiene un rol especial (el único que puede tocar
la UI). El conocimiento de concurrencia de P10 se aplica directamente:
en una app real, el FXAT sería el productor de queries (el usuario
hace click → genera evento) y otros hilos serían los consumidores
(ejecutan la query, devuelven el resultado al FXAT vía
`Platform.runLater`).

## 2. ¿Qué es un EventHandler? ¿Cómo conecta la acción del usuario con la lógica de tu programa?

Un **EventHandler** es una **función de callback** que se ejecuta cuando
ocurre un evento específico en un componente. En JavaFX, la interfaz
es `EventHandler<T extends Event>` con un único método:

```java
void handle(T event);
```

### Cómo se conecta

Cada control de JavaFX tiene métodos `setOnXxx(EventHandler<Xxx>)` que
registran un handler para un tipo de evento:

```java
botonGuardar.setOnAction(e -> intentarGuardar());
campoBusqueda.textProperty().addListener((obs, viejo, nuevo) -> { ... });
tabla.setOnMouseClicked(ev -> { ... });
scene.setOnKeyPressed(ev -> { ... });
```

Cuando el usuario hace algo (click, teclear, presionar tecla), JavaFX:

1. Detecta el evento del SO (un click de mouse en cierta posición).
2. Encuentra el componente JavaFX bajo el cursor.
3. Construye un objeto `Event` con detalles (tipo, coordenadas, código
   de tecla).
4. **Llama al `handle()`** del `EventHandler` registrado, pasándole
   el evento.

Esto **transfiere el control** desde el bucle de eventos del FXAT a
**mi código de negocio**. El handler hace su trabajo y devuelve;
JavaFX recupera el control y procesa el siguiente evento.

### Lambdas vs clases anónimas

Como `EventHandler` es una **interfaz funcional** (un solo método
abstracto), Java permite escribir handlers como lambdas:

```java
// Versión con lambda (lo que uso)
botonGuardar.setOnAction(e -> intentarGuardar());

// Versión equivalente con clase anónima (estilo antiguo)
botonGuardar.setOnAction(new EventHandler<ActionEvent>() {
    @Override
    public void handle(ActionEvent e) {
        intentarGuardar();
    }
});
```

Las lambdas son la forma moderna y son drásticamente más legibles
para handlers cortos.

### El "puente" entre UI y lógica

Lo importante conceptualmente: los `EventHandler` son el puente entre
"el usuario hizo algo" y "mi programa hace algo". Sin ellos, los
componentes serían cajas decorativas. Con ellos, los componentes se
vuelven interactivos: cada handler dice "cuando pase X, ejecuta Y".

En mi código, esta separación deja la lógica del dominio (validar un
email, eliminar un cliente, filtrar la tabla) **fuera** de los
componentes UI. El handler es solo una **traducción**:
```
evento UI → llamada a la lógica
```

Esto significa que mis métodos `accionNuevo()`, `accionEliminarSeleccionado()`
podrían testearse o invocarse desde otro contexto (un test, un menú,
un atajo de teclado, un script) sin acoplarse al evento que los
disparó. Es separación de responsabilidades vía callbacks.

## 3. ¿Qué diferencia hay entre un Stage, una Scene y un Node en JavaFX?

Las tres son los **bloques estructurales** de cualquier app JavaFX, y
se relacionan en jerarquía:

```
Stage   (ventana del SO)
   └── Scene   (contenido grafico de la ventana)
          └── Node   (cada control individual: button, label, etc)
                ├── Node
                └── Node ...
```

### Stage

Es **una ventana** del sistema operativo. Tiene barra de título, botones
de minimizar/maximizar/cerrar, se puede mover, redimensionar. La
ventana inicial se llama "primary stage" y la entrega el framework
en `start(Stage primaryStage)`. Se pueden crear más con
`new Stage()` para ventanas secundarias (ej. el `ClienteFormulario` es
un Stage con `initModality(WINDOW_MODAL)`).

Métodos típicos: `setTitle`, `setScene`, `show`, `close`,
`initModality`, `initOwner`.

Hay un Stage "raíz" y los demás Stages pueden ser sus hijos
(con `initOwner`), lo cual permite que el SO los agrupe y los
mueva/cierre juntos.

### Scene

Es el **contenido gráfico** de un Stage. Una Stage tiene exactamente
**una Scene** activa en cada momento (aunque se puede cambiar
dinámicamente). La Scene tiene un **árbol de nodos** (su raíz suele
ser un `BorderPane`, `VBox`, etc) y contiene también las hojas de
estilo CSS aplicables a esos nodos.

```java
Scene scene = new Scene(root, 900, 600);
scene.getStylesheets().add("/styles.css");
stage.setScene(scene);
```

La Scene actúa como **contenedor de eventos**: tiene
`setOnKeyPressed`, `getAccelerators` que aplican a TODA la ventana.

### Node

Es **cada componente individual**: un `Button`, `Label`, `TextField`,
`TableView`, `HBox`. Todos heredan de la clase abstracta `Node`.

Los Nodes se organizan en un **árbol**: hay nodos "contenedores"
(`Pane`, `HBox`, `VBox`, `GridPane`, `BorderPane`) que tienen
**hijos**, y nodos "hoja" (`Button`, `Label`) que no. El árbol entero
empieza en la raíz que se le pasa a la Scene.

Cada Node tiene propiedades comunes: posición, tamaño, estilo, listeners
de eventos, visibilidad, etc.

### En mi código

Concretamente:

- **Stage** = ventana principal de la app + ventana modal del formulario
  + ventanas de Alert.
- **Scene** = una para la ventana principal (root: `BorderPane`), otra
  para el formulario (root: `VBox` con `GridPane`).
- **Node** = cada `Button`, `Label`, `TextField`, `TableView`, `MenuBar`,
  `HBox`, `VBox`, etc. Hay docenas.

La relación es: el Stage es **el contenedor del SO**, la Scene es
**la pizarra dibujable**, y los Nodes son **los elementos pintados**
sobre la pizarra.

### Una analogía que me ayudó

Pensar en una pintura física:
- **Stage** = el marco de madera + las paredes que lo sostienen.
- **Scene** = el lienzo dentro del marco.
- **Node** = cada figura pintada en el lienzo (un rostro, un árbol).

El marco se cuelga, mueve, intercambia (Stage). El lienzo se puede
sacar del marco y poner otro (cambiar de Scene). Y dentro del lienzo
están los detalles que el observador ve y con los que interactúa
(Nodes).

Esa metáfora me hizo entender por qué los métodos están donde están:
- "Cambia el título" → al Stage (es el marco).
- "Aplica el CSS" → a la Scene (es el lienzo).
- "Maneja el click" → al Node (es la figura clickeable).
