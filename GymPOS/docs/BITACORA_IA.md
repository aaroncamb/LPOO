# Bitácora de IA — GymPOS

Registro del uso de inteligencia artificial (Claude, de Anthropic) como herramienta de apoyo durante el desarrollo del proyecto integrador GymPOS.

Este documento es complementario a las bitácoras individuales de cada práctica P1-P11. Aquí se documentan los intercambios que cubren decisiones arquitectónicas y de integración a nivel proyecto.

---

## Intercambio 1 — Alcance del proyecto

**Mi prompt:**

> La consigna del proyecto integrador pide cinco módulos funcionales (gestión clientes, membresías, pagos, reportes, control acceso), siete funcionalidades específicas (registro suscripciones, renovación, puntos, calendario clases, inventario equipos, notificaciones, reportes con multithreading), arquitectura MVC, mínimo 15 clases, JAR ejecutable, manual técnico, manual de usuario, video de 5-8 min y bitácora de IA. ¿Por dónde arranco sin perder de vista lo importante?

**Resumen de la respuesta:**

La IA propuso tres alcances posibles: "completo" (hacer absolutamente todo con PDF y notificaciones por email), "pragmático" (módulos sólidos + UI funcional + reportes TXT) y "mínimo" (lo justo para pasar la rúbrica).

Recomendó el pragmático con argumentos concretos:

1. **PDF requiere librería externa** (iText, PDFBox). Sumar dependencias agrega superficie de bugs sin agregar valor real para la rúbrica. El reporte TXT alineado de P9 ya se ve profesional.
2. **Las 5 funcionalidades específicas son negociables entre sí**. Hacer 5 cosas bien-pero-simples vale más que 5 cosas a medias.
3. **Los criterios "Arquitectura y diseño" + "Calidad de código y documentación" suman 35% de la nota.** Un manual técnico bien hecho compensa mucho.

Sugirió que el video lo grabe yo con script ensayado en lugar de intentar automatizarlo.

**Qué hice con eso:**

Adopté el alcance pragmático. Decidí:
- Reportes TXT alineados de P9, sin PDF.
- Calendario de clases simple (tabla + inscribir/cancelar), sin vista de calendario gráfica.
- Inventario de equipos como modelo serializable, sin pestaña dedicada (decisión consciente para no inflar el alcance).
- Notificaciones por vencimiento como estado visual en la tabla de Membresías, sin emails.

Esto me dejó tiempo para hacer la documentación bien (manual técnico completo, casos de uso formales, diagramas UML).

**Qué aprendí:**

Que "alcance" es una decisión estratégica, no técnica. Aprendí a leer la rúbrica buscando qué pesa más en la nota y a no añadir features "porque sería cool" si no aportan a esos criterios. También aprendí que cumplir excelente con el 80% es mejor que cumplir mal con el 100%.

---

## Intercambio 2 — Service Locator vs Inyección de Dependencias

**Mi prompt:**

> Tengo varios servicios (GestionClientes, SistemaMembresias, ProcesadorPagos, etc.) y varios controllers JavaFX que los necesitan. Si los paso por constructor a cada controller, los constructores se vuelven gigantescos. ¿Qué patrón uso? Vi que existe Spring pero parece overkill.

**Resumen de la respuesta:**

La IA confirmó que Spring sería overkill para este tamaño. Propuso un **Service Locator simple**: una clase `AppContext` que se construye una sola vez al inicio, expone los servicios como getters, y los controllers la consultan.

Las ventajas mencionadas:
- Sin reflection, sin XML, sin anotaciones.
- Single source of truth para servicios.
- Inicialización centralizada (en un solo método sé qué se carga primero, qué después).
- Fácil para inicialización con datos (cargar desde disco o sembrar) en un solo lugar.

Las desventajas mencionadas:
- Hace más difícil hacer mocks para tests unitarios.
- Si abusas, se vuelve global state disfrazado.

Para el alcance del proyecto las ventajas pesaban más. Recomendó:
- Construir el AppContext en `App.start()`.
- Exponerlo como singleton lazy (`AppContext.get()`).
- Encapsular la inicialización completa en su constructor.

**Qué hice con eso:**

Implementé `AppContext` exactamente así. El constructor hace, en orden:
1. Cargar config.properties.
2. Configurar el logger.
3. Asegurar directorios de datos.
4. Instanciar servicios en orden de dependencia (los que dependen de archivos al final).
5. Cargar datos desde disco o sembrar con DatosPrueba.

Esto me permitió que cada controller pidiera lo que necesitaba sin tener que cambiar la cadena de constructores cuando agregaba un servicio nuevo.

**Qué aprendí:**

Patrones arquitectónicos son herramientas, no dogmas. El "patrón correcto" depende del tamaño y vida útil del proyecto. Spring es excelente para proyectos enterprise donde la app vive 10 años y muta cada semana; para un proyecto académico de 35 clases, un service locator con 200 líneas es la respuesta correcta.

---

## Intercambio 3 — Property + Serializable en una misma clase

**Mi prompt:**

> Quiero que Cliente tenga propiedades JavaFX para que la tabla sea reactiva, pero también que sea Serializable para guardarlo a archivo binario. El problema es que SimpleStringProperty no implementa Serializable. ¿Cómo resuelvo?

**Resumen de la respuesta:**

La IA explicó que hay tres opciones:

1. **Dos clases separadas**: una "domain Cliente" (Serializable) y una "ui Cliente" (con Property). Convertir entre ellas. Mucho boilerplate.
2. **Mantener Property y usar JSON en lugar de binario**: Jackson maneja Property con un módulo extra. Pero JSON no es lo que pide la rúbrica (binario serializado).
3. **Property con writeObject/readObject custom**: declarar las Property como `transient`, y manualmente escribir/leer los valores primitivos en métodos especiales que Java reconoce durante la serialización.

La opción 3 es la canónica de Oracle para este caso, robusta y future-proof. La IA proporcionó el esqueleto:

```java
private transient StringProperty nombreCompleto;

private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeUTF(getNombreCompleto());
    // ...
}

private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    this.nombreCompleto = new SimpleStringProperty(in.readUTF());
    // ...
}
```

Explicó que Java reconoce estos dos métodos por nombre/firma y los invoca automáticamente. No hay que registrar nada.

**Qué hice con eso:**

Implementé Cliente con 8 propiedades transient y los dos métodos custom. Para cada campo serializo el valor primitivo (`writeInt`, `writeUTF`, `writeDouble`, etc.), y para fechas y enums uso `writeObject(LocalDate)` y `writeUTF(enum.name())`.

Documenté en el manual técnico (sección 4) por qué esta solución y por qué no las alternativas.

**Qué aprendí:**

Que `transient` no es solo "no serializar este campo": es una afirmación contractual de que **el campo se reconstruye de algún otro modo**. Si declaras transient y no lo reconstruyes en readObject, el campo queda en null silenciosamente, lo cual es peor que un error explícito. Aprendí a tratar transient como "promesa de reconstrucción", no como "ignorar".

---

## Intercambio 4 — Concurrencia en JavaFX: Task vs Thread crudo

**Mi prompt:**

> Para generar reportes y hacer backups en background, vi que JavaFX tiene una clase Task. Pero también puedo usar Thread crudo o ExecutorService como en P10. ¿Cuál es la diferencia y cuál debo usar?

**Resumen de la respuesta:**

La IA explicó tres niveles de abstracción:

1. **Thread crudo**: bajo nivel. Para tocar la UI desde el Thread necesitas `Platform.runLater()` manualmente. No tiene progress ni mensaje vinculables. Útil para casos muy ad-hoc.

2. **ExecutorService**: pool de threads reusable. Mejor cuando lanzas muchas tareas pequeñas. No resuelve el problema de comunicarse con la UI.

3. **javafx.concurrent.Task**: alto nivel. Diseñada específicamente para JavaFX. Expone `progressProperty()`, `messageProperty()`, `valueProperty()` que son **observables thread-safe** y se pueden bindear directo a controles de UI. Tiene callbacks `setOnSucceeded`, `setOnFailed`, `setOnCancelled` que se ejecutan en el FXAT automáticamente.

Para mi caso (mostrar progreso de un reporte en una ProgressBar), `Task` es la opción correcta sin comparación. Resuelve el cruce de hilos automáticamente.

La IA me mostró el patrón básico:

```java
Task<String> tarea = new Task<>() {
    @Override protected String call() throws Exception {
        updateProgress(0, 100);
        // ... trabajo pesado
        return resultado;
    }
};
barra.progressProperty().bind(tarea.progressProperty());
tarea.setOnSucceeded(e -> { /* ya en el FXAT */ });
new Thread(tarea).start();
```

**Qué hice con eso:**

Creé `TareaReporte extends Task<String>` y `TareaBackup extends Task<Integer>`. La firma del genérico es el tipo de retorno: la ruta del archivo para reportes, el número de backups creados para backups.

Implementé la ventana de progreso modal en MainController para el backup al cerrar: barra bindeada a progressProperty, label bindeado a messageProperty.

Documenté esto en el manual técnico sección 8 y en CASOS_DE_USO.md (CU-05 y CU-06).

**Qué aprendí:**

Que JavaFX tiene herramientas **diseñadas para JavaFX**, y usarlas siempre es mejor que reinventar con Thread crudo. Task encapsula tres problemas a la vez: ejecución asíncrona, comunicación de progreso a la UI, y manejo de errores. Hacer eso a mano son ~50 líneas de boilerplate por cada tarea.

---

## Intercambio 5 — Manejo del cierre de ventana con backup async

**Mi prompt:**

> Quiero que al cerrar la ventana, GymPOS haga backup automático de los archivos de datos. Pero si el backup tarda 2 segundos, no quiero que la app parezca colgada. ¿Cómo combino el evento de cierre con una tarea async?

**Resumen de la respuesta:**

La IA explicó el truco: `setOnCloseRequest` recibe un evento que se puede **consumir** con `e.consume()`, lo que **pausa el cierre**. Luego puedes lanzar la tarea async, y solo cuando termine llamas `stage.close()` manualmente.

El flujo recomendado:

```java
stage.setOnCloseRequest(e -> {
    e.consume();  // pausa el cierre
    
    TareaBackup tarea = ctx.tareaBackupCompleto();
    
    // Mostrar ventana de progreso modal
    Stage progresoStage = construirVentanaProgreso(tarea);
    progresoStage.show();
    
    tarea.setOnSucceeded(ev -> {
        progresoStage.close();
        stage.close();  // ahora sí cerramos
    });
    
    new Thread(tarea).start();
});
```

Importante: la ventana de progreso debe ser **modal** (`initModality(WINDOW_MODAL)`) para que el usuario no pueda hacer otras cosas mientras el backup ocurre.

También mencionó que si el backup falla, hay que cerrar la app **de todos modos** (no bloquear el cierre por un backup fallido), solo loguear el error.

**Qué hice con eso:**

Implementé exactamente ese patrón en `MainController.ejecutarBackupYCerrar()`. La ventana de progreso tiene una ProgressBar y un Label, ambos bindeados a las propiedades de TareaBackup.

También implementé el flujo "Crear backup ahora" (Ctrl+B) que usa la misma TareaBackup pero sin cerrar la app después.

**Qué aprendí:**

Que `Event.consume()` es una herramienta poderosa: te permite **interceptar** una acción del usuario, hacer algo en medio, y decidir si dejar que continúe. JavaFX está lleno de estos puntos de gancho (cierre de ventana, cierre de tab, click en menú). Aprendí a verlos como "oportunidades" en lugar de eventos pasivos.

---

## Intercambio 6 — Bug de Java 21: definite assignment con lambdas

**Mi prompt:**

> El compilador de Java 21 me da "variable stage might not have been initialized" en mi ClienteFormulario, en una línea que es claramente un lambda que se ejecutará después. ¿Por qué se queja?

**Resumen de la respuesta:**

La IA explicó que el compilador de Java 21 es **más estricto** con análisis de flujo. Específicamente, con la regla de "definite assignment": para que un campo `final` pueda ser usado en un lambda, el compilador necesita **demostrar estáticamente** que ha sido asignado antes de la declaración del lambda.

En mi caso, yo tenía:

```java
public ClienteFormulario(Window padre, Cliente editar) {
    // ...
    botonCancelar.setOnAction(e -> {
        resultado = null;
        stage.close();  // ← stage usado aquí
    });
    // ...
    stage = new Stage();  // ← asignado aquí, DESPUÉS
}
```

Aunque en runtime el lambda se ejecutará después de que stage sea asignado (porque setOnAction solo registra el lambda, no lo ejecuta), el compilador no puede demostrarlo. En Java 17 esto pasaba con un warning; en Java 21 es error.

La solución: **mover la asignación de stage ANTES de los listeners**.

```java
public ClienteFormulario(Window padre, Cliente editar) {
    // ...
    stage = new Stage();  // PRIMERO
    botonCancelar.setOnAction(e -> {
        resultado = null;
        stage.close();  // ahora el compilador está feliz
    });
    // ...
}
```

**Qué hice con eso:**

Lo arreglé en ClienteFormulario (P11) y verifiqué proactivamente DialogoCobro (GymPOS), donde apliqué el mismo patrón desde el inicio.

Como medida preventiva, en el código de GymPOS estructuré todos los constructores de diálogos como: (1) declarar campos, (2) **crear el stage**, (3) crear los listeners. El stage siempre se crea antes que los listeners.

**Qué aprendí:**

Que el "warning" de Java 17 a veces se vuelve "error" en Java 21. La regla más profunda: **el orden de inicialización dentro de un constructor importa cuando hay lambdas que capturan campos**. El compilador no es psíquico, no asume nada, y a veces es más exigente entre versiones.

Esto me hizo más cuidadoso con la "ergonomía de constructores": no agrupar listeners arriba y construcción abajo. El orden lógico ahora siempre es: crear lo que va a ser capturado → registrar listeners que lo capturen.

---

## Intercambio 7 — Estructura de la documentación

**Mi prompt:**

> La rúbrica pide manual técnico de 10-15 páginas, manual de usuario con casos de uso, video script, y bitácora de IA. Son varios documentos. ¿Qué pongo en cada uno para que no se repitan?

**Resumen de la respuesta:**

La IA propuso una división clara:

- **MANUAL_TECNICO.md**: para quien va a **mantener o extender** el sistema. Habla de arquitectura, packages, decisiones de diseño, dependencias, cómo compilar, limitaciones. **No** habla de cómo usar la app.

- **MANUAL_USUARIO.md**: para quien va a **operar** el sistema. Habla de qué hace cada botón, qué pasa al hacer doble-click, cómo buscar, qué significa cada color. **No** habla de código.

- **CASOS_DE_USO.md**: para evaluar **funcionalidad**. Formato formal con actores, precondiciones, flujos principales y alternativos. Es lo que da claridad a la rúbrica de qué cubre el sistema.

- **VIDEO_SCRIPT.md**: guía interna para grabar el video. No es para el evaluador; es para mí mismo durante la grabación.

- **BITACORA_IA.md**: este documento. Para transparencia con el profesor sobre cómo usé IA.

La IA insistió en que cada documento tenga **una sola audiencia** y un solo propósito. Si encuentro que estoy escribiendo lo mismo en dos lados, ese es un olor a mezcla de responsabilidades.

**Qué hice con eso:**

Implementé exactamente esa estructura. El MANUAL_TECNICO empieza con "Introducción" y "Tecnologías" y termina con "Limitaciones" y "Mejoras futuras". El MANUAL_USUARIO empieza con "Bienvenida" y "Primera ejecución" y termina con "Resolución de problemas".

Para los diagramas UML usé PlantUML porque es texto plano y va versionable en git (los .puml son legibles incluso sin renderizar). Generé 6 diagramas que cubren: arquitectura general, jerarquía de membresía, jerarquía de excepciones, secuencia de cobro, secuencia de reporte en background, modelo de datos.

**Qué aprendí:**

Que escribir documentación es un ejercicio de **empatía**: ponerse en los zapatos de cada audiencia y preguntar "¿qué necesita saber esta persona?". La regla "una audiencia por documento" es difícil de seguir cuando uno es flojo (es más fácil amontonar todo en un README gigante), pero el resultado es mucho más profesional.

---

## Reflexión final sobre el uso de IA

A lo largo de las 11 prácticas y el proyecto integrador, usé Claude como **pair programmer** y **asesor de diseño**, no como generador de código que copio sin entender. La metodología fue siempre:

1. **Yo planteo el problema** o la consigna en mis palabras.
2. **La IA propone opciones** con trade-offs explícitos.
3. **Yo decido** qué opción tomar, a veces ajustando lo propuesto.
4. **Yo implemento** y verifico que entiendo cada línea.
5. **Yo defiendo el código** en mi propio razonamiento, no en autoridad de la IA.

Este enfoque me dejó con un sistema que:

- **Entiendo línea por línea**. No hay magia, no hay "esto lo puso la IA y no sé qué hace".
- **Puedo defender oralmente**. Si me preguntan por qué uso `FilteredList` en lugar de `stream().filter()`, sé responder.
- **Puedo modificar con seguridad**. Cuando vi el bug de "definite assignment" en Java 21, supe diagnosticarlo y arreglarlo yo mismo en GymPOS antes de que apareciera.

La IA fue particularmente útil para:
- **Decisiones de alcance** (intercambio 1).
- **Patrones arquitectónicos** que no había visto en clase (intercambio 2, 4, 5).
- **Bugs sutiles de versiones de Java** (intercambio 6).
- **Organización de documentación** (intercambio 7).

La IA no fue útil (y aprendí a no pedírselo) para:
- Generar todo el código de un módulo de golpe.
- Tomar decisiones por mí.
- Escribir mi nombre en la bitácora como autor.

El código de GymPOS es **mío**, escrito con asistencia inteligente pero con criterio y comprensión propia.

---

**Total de intercambios documentados en este proyecto:** 7
**Total de intercambios documentados en P1-P11:** 35-40 aproximadamente
**Líneas de código del proyecto:** ~3,500 (sin contar documentación)
**Tiempo invertido:** ~30 horas distribuidas en 5 días.
