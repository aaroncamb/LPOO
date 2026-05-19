# Manual Técnico — GymPOS

**Sistema de Punto de Venta para Gimnasio**
Proyecto Integrador — Laboratorio de Programación Orientada a Objetos
César Aarón Mendoza Benavides — Matrícula 1904833

---

## Tabla de contenidos

1. Introducción
2. Tecnologías y dependencias
3. Arquitectura general
4. Modelo de dominio
5. Capa de servicios
6. Persistencia
7. Excepciones del dominio
8. Concurrencia
9. Capa de presentación (JavaFX)
10. Decisiones de diseño relevantes
11. Cómo compilar y ejecutar
12. Estructura de archivos
13. Limitaciones conocidas
14. Posibles mejoras futuras

---

## 1. Introducción

GymPOS es un sistema de gestión integral para un gimnasio que cubre cinco áreas operativas: clientes, membresías, pagos, clases grupales y control de acceso. Está construido como un único módulo Java 21 con interfaz JavaFX, persistencia en archivos binarios, multithreading para tareas pesadas y un esquema robusto de excepciones de dominio.

El proyecto integra y consolida todo el material visto en las once prácticas del curso: encapsulamiento (P3), herencia y polimorfismo (P4, P5), clases abstractas e interfaces (P6), excepciones personalizadas (P7), colecciones (P8), entrada/salida (P9), concurrencia (P10) e interfaz gráfica con JavaFX (P11). Cada componente que se reutiliza de prácticas anteriores está documentado en el código y en este manual.

El alcance fue definido como **pragmático**: se priorizó solidez de los módulos principales y una UI realmente funcional, sobre tener más funcionalidades a medias. El sistema cubre las cinco funcionalidades específicas pedidas por la consigna (registro de suscripciones, renovación, sistema de puntos, calendario de clases e inventario de equipos) y cumple con holgura el requisito de mínimo 15 clases organizadas en packages MVC.

---

## 2. Tecnologías y dependencias

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 21 (LTS) | Lenguaje base |
| JavaFX | 21.0.2 | Interfaz gráfica |
| Maven | 3.8+ | Build y gestión de dependencias |
| maven-shade-plugin | 3.5.0 | JAR ejecutable con todas las dependencias |
| javafx-maven-plugin | 0.0.8 | Ejecutar JavaFX en desarrollo |

El proyecto no usa frameworks adicionales (sin Spring, sin Hibernate, sin librerías de PDF). Todas las dependencias son estándar de JavaFX. Esto mantiene el proyecto autocontenido, fácil de auditar, y elimina riesgos de versiones incompatibles entre librerías.

El archivo `pom.xml` declara las dependencias de JavaFX, configura el compilador a Java 21, y registra dos plugins: el de JavaFX para ejecutar la app en desarrollo con `mvn javafx:run`, y el shade-plugin para empaquetar un fat JAR ejecutable con `mvn package`.

---

## 3. Arquitectura general

GymPOS sigue el patrón **MVC** (Modelo-Vista-Controlador), enriquecido con capas adicionales para servicios y persistencia. El código está organizado en nueve packages, cada uno con una responsabilidad clara:

```
com.gympos
├── App                         (clase Application de JavaFX)
├── model                       (clases del dominio)
├── view                        (componentes UI reutilizables)
├── controller                  (controladores JavaFX)
├── service                     (lógica de negocio)
├── persistence                 (I/O de archivos)
├── exceptions                  (jerarquía de excepciones)
├── concurrency                 (tareas en background)
└── util                        (utilidades: logger)
```

El diagrama general de paquetes se muestra en `diagramas/01-arquitectura-general.puml`. El flujo típico de una operación va de **controller → service → model → persistence**, atravesando potencialmente la capa de concurrencia cuando la operación es costosa.

### Service Locator: AppContext

El punto crítico de la arquitectura es la clase `AppContext`, que actúa como **service locator** centralizado. En lugar de pasar referencias de servicios por todos los constructores, los controladores piden lo que necesitan a `AppContext.get()`. Esto simplifica enormemente la composición sin introducir un framework de inyección de dependencias.

`AppContext` se crea una sola vez al iniciar la aplicación (en `App.start()`) y se mantiene vivo durante toda la sesión. Su constructor:

1. Lee `config.properties`.
2. Configura el logger.
3. Asegura que los directorios `data/`, `data/backups/` y `data/reportes/` existan.
4. Instancia los servicios en orden de dependencia.
5. Carga los datos desde disco, o siembra el sistema con datos de prueba en la primera ejecución.

Si cualquiera de estos pasos falla, `App.start()` muestra un Alert de error crítico y termina la aplicación, evitando que la UI quede en estado inconsistente.

---

## 4. Modelo de dominio

El package `com.gympos.model` contiene ocho clases que representan las entidades del negocio. El diagrama de clases se encuentra en `diagramas/06-modelo-datos.puml`.

### Cliente

`Cliente` es la clase central del sistema. Combina dos requisitos que parecen contradictorios:

- **JavaFX bindings**: para que la UI sea reactiva, los campos deben ser `Property` (e.g. `SimpleStringProperty`, `SimpleIntegerProperty`).
- **Serialización**: para guardarse y cargarse del disco con `ObjectOutputStream`.

El problema es que las clases `Property` de JavaFX NO son `Serializable`. La solución implementada es:

```java
private transient StringProperty nombreCompleto;

private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeUTF(getNombreCompleto());
    // ... otros campos
}

private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    String nombre = in.readUTF();
    this.nombreCompleto = new SimpleStringProperty(nombre);
    // ... otros campos
}
```

Los métodos `writeObject` y `readObject` son ganchos especiales que Java reconoce automáticamente durante la serialización. Marcamos los campos `Property` como `transient` (no se serializan automáticamente), y manualmente escribimos los valores primitivos al stream. Al deserializar, reconstruimos las `Property`. Esto produce archivos `.dat` estables, independientes de cambios futuros en la API de JavaFX.

El campo `puntos` implementa el **sistema de recompensas** que pide la consigna. Cada operación de cobro acumula puntos en función del monto pagado (configurable en `config.properties`), y el cliente puede canjearlos en operaciones futuras.

### Jerarquía Membresia

La jerarquía de membresías (`diagramas/02-jerarquia-membresia.puml`) ilustra los conceptos de herencia y polimorfismo. `Membresia` es una **clase abstracta** con métodos abstractos que cada subclase implementa según su política comercial:

| Plan | Duración | Clases grupales | Entrenador personal |
|---|---|---|---|
| BASICA | 1 mes | no | no |
| PREMIUM | 1 mes | sí | no |
| VIP | 12 meses | sí | sí |

`MembresiaVIP` es interesante por ser **anual**: hereda directamente de `Membresia` (no de Premium) porque su modelo comercial es diferente (compromiso de un año vs renovación mensual). Su constructor recibe el precio ANUAL y lo prorratea internamente a mensual, manteniendo así la consistencia de la API (`costoPeriodo()` sigue funcionando igual para todos los tipos).

El constructor de `Membresia` llama a `calcularFechaVencimiento()`, que a su vez llama al método abstracto `duracionMeses()`. Esto es un **caso clásico de polimorfismo durante construcción**: el JVM despacha al `duracionMeses()` de la subclase. Esto provoca un warning de compilador (`this-escape`), pero es seguro aquí porque `duracionMeses()` solo retorna una constante, sin acceder a estado no inicializado. El warning está localmente suprimido con `@SuppressWarnings("this-escape")`.

### ClaseGrupal, Equipo, RegistroAcceso

`ClaseGrupal` modela una clase del calendario (yoga, spinning, etc) con un cupo máximo. Mantiene los IDs de los clientes inscritos en un `HashSet<Integer>` para garantizar unicidad y operaciones O(1). El método `inscribir(idCliente)` lanza `CupoExcedidoException` si la clase está llena.

`Equipo` representa una pieza de inventario con un enum `Estado` (OPERATIVO, EN_REPARACION, FUERA_DE_SERVICIO). El estado se modifica con `cambiarEstado()`, validando que el nuevo estado no sea null.

`RegistroAcceso` es **inmutable**: todos sus campos son `final` y no hay setters. Esto refleja la naturaleza del dominio — los hechos históricos no se modifican. Cada vez que un cliente entra o sale del gimnasio se genera un nuevo `RegistroAcceso`.

---

## 5. Capa de servicios

El package `com.gympos.service` contiene los cinco módulos funcionales que pide la consigna, más el `DatosPrueba` para sembrar el sistema en primera ejecución.

### GestionClientes (módulo 1)

Reutiliza el patrón de la Práctica 8: **cuatro estructuras de colección sincronizadas** que se mantienen consistentes en todas las operaciones CRUD:

- `ArrayList<Cliente>` — listado en orden de inserción.
- `HashMap<Integer, Cliente>` — índice por id para lookups O(1).
- `HashSet<String>` — emails registrados para validar unicidad.
- Tres `Comparator<Cliente>` reutilizables: `POR_NOMBRE`, `POR_ANTIGUEDAD`, `POR_PUNTOS_DESC`.

El método `agregar(Cliente)` valida que el id no esté duplicado, que el email no esté usado, y actualiza las tres estructuras atómicamente. El método `eliminarPorId(int)` revierte la operación: elimina del map (que devuelve el cliente), lo busca en la lista, y libera el email del set.

El método `nuevosPremiumDesde(LocalDate, int)` es la **consulta compuesta** que P8 pidió como decisión propia: combina filtros sobre estado, tipo y fecha, ordena por antigüedad reversa, y limita el resultado. Se implementa con Stream pipeline.

### SistemaMembresias (módulo 2)

Gestiona la jerarquía de planes con factory methods que ocultan las subclases. El método `crear(idCliente, tipo, ...)` usa `switch` con expresiones de Java 21 para construir la instancia correcta:

```java
Membresia m = switch (tipo) {
    case BASICA  -> new MembresiaBasica(...);
    case PREMIUM -> new MembresiaPremium(...);
    case VIP     -> new MembresiaVIP(...);
};
```

La **renovación de membresía** que pide la consigna está implementada en `renovar(idCliente)`. Si la membresía está vencida, se loguea como warning pero se procede (la decisión la toma el usuario en la UI). El método `verificarVigencia()` se llama desde `ControlAcceso` antes de permitir una entrada, lanzando `MembresiaVencidaException` si está expirada.

Los métodos `porVencerEn(int dias)` y `vencidas()` alimentan los reportes y las notificaciones automáticas.

### ProcesadorPagos (módulo 3)

Encapsula la lógica de cobros. Cada cobro sigue un flujo similar al **Template Method de P5**:

1. Validar entrada (lanza `EntradaInvalidaException` si hay bug).
2. Calcular: subtotal × (1 - descuento) → base; base × IVA → impuestos; total.
3. Simular llamada al banco (90% de éxito; 10% falla con código aleatorio).
4. Si éxito: registrar Ticket y sumar puntos al cliente.
5. Si fallo: lanzar `PagoRechazadoException` con contexto rico.

La excepción `PagoRechazadoException` lleva cinco campos de contexto (monto, método, código de error, referencia única, timestamp). Su método `toString()` emite JSON, lo que permite que el log sea procesable por herramientas de monitoreo. La referencia única se genera mezclando `currentTimeMillis()` con `Thread.threadId()`, lo que garantiza unicidad incluso ante cobros concurrentes.

`Ticket` es una clase interna estática de `ProcesadorPagos`, también serializable, con todos los campos `final` (es un comprobante histórico, no se modifica).

### GeneradorReportes (módulo 4)

Produce tres tipos de reportes en formato **TXT con columnas alineadas** (estilo P9):

- **Reporte general**: conteo por tipo de cliente, membresías por vencer, vencidas, ingresos del periodo, accesos del día.
- **Reporte de ingresos**: lista de tickets emitidos con subtotal, descuento, IVA y total.
- **Reporte de asistencia**: entradas y salidas del día con torniquete y hora.

Cada reporte tiene una cabecera con el nombre del gimnasio y timestamp de generación, secciones temáticas, y un pie de cierre. El formato se logra con el método helper `formato(String texto, int ancho)` que rellena o trunca cada celda al ancho deseado, usando `String.repeat(' ', n)` de Java 11+.

Los reportes se guardan en `data/reportes/` con un nombre que incluye timestamp (e.g. `reporte_general_2026-05-15_22-30-15.txt`), de modo que cada generación produce un archivo nuevo sin sobrescribir los anteriores.

### ControlAcceso (módulo 5)

Maneja entradas y salidas. Antes de registrar una entrada, consulta a `SistemaMembresias.verificarVigencia()`: si la membresía está vencida, se rechaza la entrada lanzando `MembresiaVencidaException`. Las salidas no requieren validación (el cliente ya está adentro).

Mantiene una `ArrayList<RegistroAcceso>` y métodos de consulta para reportes (`registrosDelDia`, `totalEntradasHoy`, etc).

### DatosPrueba

Genera datos iniciales para la primera ejecución: **20 clientes, 8 clases grupales y 12 equipos**, cubriendo los 20+ registros que pide la consigna. Los clientes incluyen variedad de tipos de membresía, fechas de registro distribuidas entre 2022 y 2025, dos clientes desactivados, y cinco con puntos pre-acumulados para mostrar el sistema de recompensas funcionando desde el inicio.

---

## 6. Persistencia

El package `com.gympos.persistence` reutiliza el diseño de la Práctica 9 con tres clases.

### GestorArchivos

Es **genérico**: en lugar de tener métodos `guardarClientes(List<Cliente>)`, expone `<T> guardarLista(String archivo, List<T> objetos)`. Esto permite usar la misma clase para guardar clientes, membresías, accesos, clases y equipos. La generalización tiene un costo menor: `cargarLista` requiere un cast `@SuppressWarnings("unchecked")` porque `ObjectInputStream.readObject()` devuelve `Object` (el type erasure de Java impide verificación en runtime).

Toda operación de I/O usa **try-with-resources** para garantizar el cierre de streams, incluso ante excepciones. Esta es la recomendación 3 de la Reflexión de P9.

### BackupManager

Crea copias de los archivos `.dat` en `data/backups/` con timestamp en el nombre (e.g. `clientes_2026-05-15_22-30-15.dat`). El método `crearBackup(String archivo)` no falla si el archivo origen no existe (solo loguea un warning), lo que permite invocarlo seguramente para los cinco archivos del sistema aunque algunos aún no se hayan creado.

`crearBackupMultiple(List<String>)` es la versión que se invoca al cerrar la aplicación, dentro de una `TareaBackup` que la ejecuta en hilo de fondo (ver sección 8).

### ConfigManager

Lee `config.properties` desde el directorio de trabajo. Si no lo encuentra, intenta leerlo del classpath (útil cuando la app se ejecuta desde el fat JAR). Expone getters tipados (`getDouble`, `getInt`, `getBoolean`) con valores por defecto, así el código cliente nunca lidia con `null` o parsing.

Las claves se documentan en el propio `config.properties` con comentarios. Los valores se cargan al iniciar y se mantienen en memoria durante toda la sesión (no hay recarga en caliente, lo que sería innecesario para este alcance).

---

## 7. Excepciones del dominio

La jerarquía completa se ilustra en `diagramas/03-jerarquia-excepciones.puml`. El package `com.gympos.exceptions` tiene cinco clases.

`GymException` es una clase abstracta **checked** que extiende `Exception`. Todos sus hijos representan errores del mundo externo: un pago rechazado por el banco, una clase llena, una membresía vencida. El compilador exige `try/catch` o `throws` para forzar al programador a manejar conscientemente cada uno.

Cada hija lleva información de contexto específica al error. `PagoRechazadoException` es la más rica: monto, método de pago, código de error interno, referencia única, timestamp. El `DialogoCobro` muestra todos estos campos al usuario en caso de fallo, lo que permite al recepcionista dar seguimiento al cliente sin pedirle que repita los datos.

`EntradaInvalidaException` es diferente: hereda de `RuntimeException` (es **unchecked**) porque representa un **bug del programador**, no un evento del mundo. Validar que un id sea positivo o que una fecha no sea null es responsabilidad del código que llama; si pasa algo inválido, no tiene sentido obligar a `try/catch`, lo correcto es arreglar el código que pasó el dato inválido.

Esta distinción checked/unchecked es uno de los conceptos centrales que P7 buscaba enseñar, y aquí se aplica de forma sistemática.

---

## 8. Concurrencia

El package `com.gympos.concurrency` materializa el requisito de la consigna: **multithreading para tareas pesadas**.

### Por qué multithreading aquí

JavaFX es single-threaded para la UI: todo evento, todo binding y todo update de propiedad ocurre en el **JavaFX Application Thread (FXAT)**. Si una operación tarda más de ~100 ms ejecutándose en el FXAT, la UI se congela: los botones no responden, la ventana no se redibuja, el sistema operativo eventualmente marca la app como "no responde".

Las operaciones de GymPOS que pueden ser costosas son: generar reportes (recorrer cientos o miles de registros, formatear, escribir a disco) y crear backups (copiar varios archivos). Ambas se ejecutan en hilos de fondo, dejando el FXAT libre para seguir respondiendo a clicks.

### TareaReporte y TareaBackup

Ambas extienden `javafx.concurrent.Task<T>`, la clase oficial de JavaFX para tareas en background. El patrón es:

1. **Constructor**: recibe los servicios que va a invocar.
2. **`call()`**: se ejecuta en hilo de fondo. Aquí NO se puede tocar la UI directamente; solo `updateProgress()` y `updateMessage()` (que son thread-safe).
3. **Bindings desde el FXAT**: el controller hace `barraProgreso.progressProperty().bind(tarea.progressProperty())`. JavaFX se encarga de cruzar el límite entre hilos de forma segura.
4. **`setOnSucceeded()`**: callback que se ejecuta DE VUELTA en el FXAT cuando la tarea termina. Aquí sí se puede tocar la UI: cambiar texto, mostrar diálogos.

El diagrama de secuencia `diagramas/05-secuencia-reporte-background.puml` muestra el flujo completo de una generación de reporte.

### Manejo del cierre con backup

`MainController.setOnCloseRequest` intercepta el cierre de la ventana. Si la configuración tiene `backup.automatico.al.cerrar=true`, se ejecuta la siguiente secuencia:

1. `e.consume()` pausa el cierre.
2. Se construye una ventana de progreso modal mostrando una `ProgressBar`.
3. Se lanza `TareaBackup` en un hilo daemon.
4. Cuando termina (success o fail), se cierra la ventana de progreso y se cierra el stage principal.

Esto da una experiencia profesional: el usuario ve que el sistema está respaldando antes de cerrar, no se queda esperando una pantalla congelada.

---

## 9. Capa de presentación (JavaFX)

El package `com.gympos.controller` contiene seis clases que arman la UI. Cada controlador corresponde a una pestaña o al contenedor principal.

### MainController

Construye la ventana principal: un `BorderPane` con menú arriba (`MenuBar`) y un `TabPane` en el centro. Las pestañas son: Clientes, Membresías, Clases Grupales y Reportes. Cada pestaña se construye delegando a su controller específico, que devuelve un `Node` listo para insertar.

El menú tiene tres acciones: **Guardar todo** (`Ctrl+S`), **Crear backup ahora** (`Ctrl+B`), y **Salir** (`Ctrl+Q`). El backup manual y el backup automático al cerrar usan la misma `TareaBackup`, con diferentes callbacks.

### ClientesController

Reutiliza casi completamente el patrón de la Práctica 11: TableView con `FilteredList → SortedList → bind comparator`, campo de búsqueda con filtrado en tiempo real, doble-click para editar, tecla Delete para eliminar.

El **formulario CRUD** está implementado como clase anidada estática `FormularioCliente`. Es un diálogo modal con validación en vivo: el botón "Guardar" se deshabilita mientras haya campos inválidos, y una etiqueta debajo del formulario muestra el primer error encontrado. La validación incluye id positivo único, email con formato (delegado al componente `CampoEmail`), peso entre 30 y 300 kg.

### MembresiasController

Muestra todas las membresías con su cliente asociado, fechas, días restantes y estado (Vigente / Por vencer / Vencida). El doble-click sobre una fila abre `DialogoCobro` para renovar y cobrar en un solo flujo. Cuando el pago es exitoso, la membresía se renueva y el cliente recibe 50 puntos bonus (configurable).

La clase interna `FilaMembresia` empareja una `Membresia` con su `Cliente` para mostrarlos juntos en la tabla, sin acoplar las clases del modelo.

### ClasesController

Muestra el calendario con 8 clases grupales. Los botones "Inscribir cliente" y "Cancelar inscripción" usan `ChoiceDialog<Cliente>` para elegir al cliente con un selector estándar de JavaFX. Si la clase está llena, `inscribir()` lanza `CupoExcedidoException` que se muestra como Alert.

### ReportesController

Es donde más se ve la concurrencia. Tiene tres botones (uno por tipo de reporte) y un `Abrir último reporte`. Al hacer click, lanza una `TareaReporte` y vincula su `progressProperty` a una `ProgressBar`. La UI sigue respondiendo durante la generación. Al terminar, carga el contenido del archivo en un `TextArea` para vista previa.

El botón "Abrir último reporte" usa `Desktop.getDesktop().open(file)` para lanzar el visor del sistema operativo (Notepad en Windows, TextEdit en macOS, gedit en Linux).

### view/

Tres componentes reutilizables:

- **`BotonAccion`**: extiende `Button` con tres variantes (PRIMARIO, SECUNDARIO, PELIGRO) que aplican clases CSS. Centraliza el estilo: cambiar el color del botón primario es modificar una línea de CSS, no buscar todas las llamadas a `setStyle` en la app.

- **`CampoEmail`**: extiende `TextField` con validación visual en tiempo real. Borde verde si es válido, rojo si es inválido (con tooltip explicando el problema). Expone `esValido()` para que el formulario lo consulte.

- **`DialogoCobro`**: modal para cobrar una membresía o concepto. Muestra el desglose en vivo (subtotal − descuento + IVA = total) que se actualiza al mover el slider de descuento. Maneja `PagoRechazadoException` mostrando todos los campos de contexto al usuario.

### styles.css

Tema **negro y dorado**, inspirado en la estética de gimnasios premium. Los colores principales son `#1a1a1a` (fondo negro grafito), `#d4a017` (acento dorado), `#f0f0f0` (texto claro), `#27ae60` (verde válido), `#c0392b` (rojo error). Las reglas cubren todos los componentes usados: TabPane, TableView, MenuBar, ProgressBar, DialogPane, Slider, etc.

---

## 10. Decisiones de diseño relevantes

### TXT alineado en lugar de PDF

La consigna no especifica formato de reportes. Generar PDF requeriría una librería externa (iText, Apache PDFBox), aumentando el riesgo de errores y la superficie de mantenimiento. El reporte TXT alineado de P9 ya da una salida profesional, fácilmente exportable, abrible en cualquier visor, y completamente bajo control del código. Esta es una decisión consciente de simplicidad sobre vistosidad.

### Service Locator en lugar de inyección de dependencias

Para un proyecto de este tamaño (35 clases), un framework de DI como Spring sería overkill. `AppContext` provee el 80% del beneficio (centralización, single source of truth para servicios) con el 20% del costo (sin reflection, sin XML, sin anotaciones).

### Property + writeObject/readObject en Cliente

Como se explicó en sección 4, la combinación de bindings JavaFX con serialización binaria es el caso clásico donde dos requisitos chocan. La solución (transient + métodos custom) es la documentada por Oracle, robusta y future-proof.

### `FilteredList → SortedList → TableView` en lugar de filtrar manualmente

JavaFX provee este patrón canónico para tablas con búsqueda y ordenamiento. Filtrar manualmente con `tabla.setItems(streams.filter().toList())` rompería las conexiones reactivas: agregar un cliente nuevo no se reflejaría, y el sort por columna se perdería. El patrón compuesto mantiene todo sincronizado automáticamente.

### Multithreading conservador

Solo se usa multithreading donde tiene impacto real (reportes y backups). Operaciones simples como agregar un cliente o calcular el descuento del slider son síncronas, porque son del orden de microsegundos. Sobre-ingenierizar con Tasks en todo lado solo agrega complejidad sin beneficio. Esta es la enseñanza de P10 aplicada con criterio.

---

## 11. Cómo compilar y ejecutar

### Requisitos

- JDK 21 (LTS) en el PATH.
- Maven 3.8+ (opcional si se usa IntelliJ, que trae uno embebido).

### Desde la terminal

```bash
cd GymPOS
mvn clean package
java -jar target/gympos-1.0.0.jar
```

### Desde IntelliJ IDEA

1. Abrir IntelliJ.
2. Click derecho sobre `GymPOS/pom.xml` → **"Add as Maven Project"**.
3. Esperar a que IntelliJ descargue las dependencias.
4. En el panel **Maven** (lado derecho), expandir `gympos → Plugins → javafx`.
5. Doble click en `javafx:run`.

### Desarrollo (sin empaquetar JAR)

```bash
mvn javafx:run
```

Este goal compila lo necesario y ejecuta la app directamente, manejando internamente el `--module-path` que JavaFX requiere.

---

## 12. Estructura de archivos

```
GymPOS/
├── pom.xml                              Maven + JavaFX + shade plugin
├── config.properties                    Configuración externa (IVA, rutas, precios)
├── README.md
├── data/
│   ├── clientes.dat                     Binario serializado
│   ├── membresias.dat
│   ├── accesos.dat
│   ├── clases.dat
│   ├── equipos.dat
│   ├── operaciones.log                  Log central de la app
│   ├── backups/                         Copias con timestamp
│   └── reportes/                        Reportes TXT generados
├── capturas/                            Screenshots para los manuales
├── docs/
│   ├── MANUAL_TECNICO.md                Este documento
│   ├── MANUAL_USUARIO.md
│   ├── CASOS_DE_USO.md
│   ├── VIDEO_SCRIPT.md
│   ├── BITACORA_IA.md
│   └── diagramas/                       Diagramas UML en PlantUML
│       ├── 01-arquitectura-general.puml
│       ├── 02-jerarquia-membresia.puml
│       ├── 03-jerarquia-excepciones.puml
│       ├── 04-secuencia-cobro.puml
│       ├── 05-secuencia-reporte-background.puml
│       └── 06-modelo-datos.puml
└── src/main/
    ├── java/com/gympos/
    │   ├── App.java
    │   ├── controller/                  6 archivos
    │   ├── view/                        3 archivos
    │   ├── service/                     6 archivos
    │   ├── model/                       8 archivos
    │   ├── persistence/                 3 archivos
    │   ├── exceptions/                  5 archivos
    │   ├── concurrency/                 2 archivos
    │   └── util/                        1 archivo
    └── resources/
        └── styles.css
```

**Total: 35 clases Java** (la consigna pide mínimo 15).

---

## 13. Limitaciones conocidas

1. **Sin autenticación de usuarios**: cualquier persona con acceso a la máquina puede operar la app. En un sistema real habría roles (recepcionista, gerente, admin).

2. **Datos en archivos planos, no en base de datos**: para volúmenes muy grandes (>100K clientes) el rendimiento de la serialización binaria empieza a degradarse. La consigna pide serialización, así que esto está dentro del alcance.

3. **Cobros son simulados**: el 90% de éxito está hardcodeado. Una integración real requeriría una pasarela de pago (Stripe, Conekta) y manejo de tokens, fuera del alcance del curso.

4. **No hay tests unitarios automatizados**: se hicieron pruebas manuales sistemáticas. JUnit no es parte del temario del curso; agregarlo sería extender el alcance sin razón.

5. **Sin internacionalización**: todos los textos están en español hardcodeados. Para múltiples idiomas habría que usar `ResourceBundle` y archivos `.properties` por idioma.

6. **Notificaciones por vencimiento son visuales, no por email**: la app marca las membresías por vencer y vencidas en su pestaña con código de color, pero no envía emails ni SMS. Una integración real requeriría JavaMail o un servicio externo.

---

## 14. Posibles mejoras futuras

- **Migrar a base de datos** (PostgreSQL o SQLite) con JDBC o JPA.
- **Pasarela de pagos real** integrando Stripe.
- **API REST** para que los torniquetes físicos consulten vigencia automáticamente.
- **Aplicación móvil** complementaria para que los clientes vean su estado, renueven y reserven clases.
- **Dashboard de métricas** con gráficos (ingresos por mes, asistencia por hora, retención).
- **Sistema de notificaciones por email** con JavaMail.
- **Tests automatizados** con JUnit 5 cubriendo la capa de servicios.
- **Internacionalización** con ResourceBundle.
