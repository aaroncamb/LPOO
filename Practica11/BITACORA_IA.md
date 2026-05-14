# Bitácora de IA — Práctica 11

Registro del uso de IA (Claude) como apoyo de pair programming durante esta práctica.

---

## Intercambio 1 — Maven vs --module-path para arrancar JavaFX

**Mi prompt:**
> Necesito ejecutar JavaFX en mi IntelliJ. Vi dos enfoques: (1) bajar
> manualmente los JAR y pasarlos con --module-path, (2) usar Maven con
> el javafx-maven-plugin. ¿Cual recomiendas?

**Resumen de la respuesta:**
La IA argumento por Maven con javafx-maven-plugin:

1. **Reproducibilidad**: el pom.xml declara las versiones exactas;
   cualquiera que abra el proyecto obtiene las mismas dependencias.
2. **Limpio en el repo**: no hay que subir 30 MB de JARs binarios.
3. **JAR ejecutable facil**: maven-shade-plugin produce un fat jar de
   un solo comando.
4. **IntelliJ amigable**: solo abrir el pom.xml y todo se autoconfigura.

La alternativa --module-path manual exige documentar 7-8 pasos para
cada nuevo desarrollador, copiar JARs a cada repo, y queda fragil ante
cambios de version de JDK.

Recomendo el javafx-maven-plugin oficial (0.0.8 al momento de hacer
la practica) que provee el goal `mvn javafx:run` para desarrollo y
maneja el module-path internamente.

**Qué hice con eso:**
Construi pom.xml con tres plugins:
- maven-compiler-plugin (compila a Java 21)
- javafx-maven-plugin (ejecutar en dev)
- maven-shade-plugin (empaquetar JAR ejecutable)

Documente los tres comandos clave en README y MANUAL_USUARIO. Esto
deja el proyecto autocontenido: clonar, abrir, ejecutar.

**Qué aprendí:**
Que para tecnologias con dependencias nativas (como JavaFX que
incluye libs C++ para cada plataforma), el manejo de Maven es
ENORMEMENTE mejor que JARs sueltos. Cada plataforma tiene clasificador
diferente (win, linux, mac, mac-aarch64) y maven los selecciona
automaticamente segun el sistema donde se compila.

---

## Intercambio 2 — Property en el modelo vs Cliente "plano" + listeners

**Mi prompt:**
> Reuso Cliente de practicas anteriores donde los atributos eran
> String, int, etc. Para JavaFX vi codigo que usa "SimpleStringProperty"
> en lugar de String. ¿Vale la pena? ¿Para que?

**Resumen de la respuesta:**
La IA explico la diferencia con un ejemplo concreto. Si Cliente tiene
`String nombreCompleto`, JavaFX no puede saber CUANDO cambia su valor.
Si la tabla muestra ese nombre y el modelo se modifica desde afuera,
la tabla no se actualiza hasta que tu llames tabla.refresh()
manualmente.

Con `SimpleStringProperty nombreCompleto`, JavaFX expone
`nombreCompletoProperty()` que es **observable**. La tabla se "suscribe"
a esa property; cuando se modifica con setNombreCompleto, la property
dispara un evento que la tabla escucha y se redibuja sola.

Mencionó el costo: el codigo se vuelve un poco mas verboso (cada
campo necesita 3 metodos: getter, setter, propertyGetter). Pero la
ganancia en interactividad es enorme.

**Qué hice con eso:**
Use Property en todos los campos de Cliente para esta practica. En
las prácticas anteriores Cliente tenia atributos planos; para P11 lo
reescribi con `SimpleIntegerProperty`, `SimpleStringProperty`, etc.
Esto deja la tabla AUTO-REACTIVA: cuando el formulario edita un
cliente y modifica sus campos, la tabla se redibuja sin necesidad de
llamar refresh.

**Qué aprendí:**
Que JavaFX no es un toolkit "anatomico" como Swing — esta diseñado
sobre el patron observer. Las Property no son solo wrappers; son la
forma DE COMUNICACION entre modelo y UI. Saltarse las Property hace
que JavaFX se sienta "torpe"; usarlas hace que todo fluya con
listeners implícitos.

---

## Intercambio 3 — FilteredList y SortedList combinadas

**Mi prompt:**
> Para el filtrado interactivo (decision propia), quiero que el usuario
> teclee y la tabla se filtre. Tambien quiero que pueda hacer click en
> los encabezados para ordenar. ¿Como combino ambas?

**Resumen de la respuesta:**
La IA explico que JavaFX provee tres clases que componen exactamente
para este caso:

```
ObservableList (base) → FilteredList (filtra) → SortedList (ordena) → TableView
```

Si usaras solo `setItems(filteredList)`, perderias el ordenamiento por
columnas. Si usaras solo `setItems(sortedList)` sin filter, no podrias
buscar. La combinacion da AMBAS funciones simultaneamente.

El truco clave: vincular el comparator del SortedList con el de la
tabla:
```java
ordenable.comparatorProperty().bind(tabla.comparatorProperty());
```

Asi, cuando el usuario hace click en una columna, la tabla actualiza
su comparator interno, el SortedList lo capta por el bind, y se
reordena automaticamente.

**Qué hice con eso:**
Implemente exactamente la composicion sugerida. El listener del campo
de busqueda solo modifica el Predicate del FilteredList; el ordenamiento
lo maneja JavaFX sin tocar codigo adicional. El statusbar abajo
muestra "X de Y clientes" actualizandose solo (gracias a las Property).

**Qué aprendí:**
Que JavaFX tiene un patron de "vistas componibles" muy elegante. En
lugar de copiar listas para cada operacion (filtrar, ordenar), envuelves
una lista en wrappers que aplican una transformacion EN VIVO. Es similar
a los Stream pero persistente: la vista se mantiene sincronizada con
la fuente automaticamente.

---

## Intercambio 4 — Validacion en tiempo real sin bloquear el FXAT

**Mi prompt:**
> Quiero que el boton Guardar se habilite solo cuando todos los campos
> sean validos. La validacion incluye regex de email. ¿No es caro
> revalidar en cada tecla?

**Resumen de la respuesta:**
La IA explico que para validaciones simples (regex de un email tipico,
parsear un entero), la revalidacion en cada tecla es **trivialmente
rapida**: ~microsegundos. Si el usuario teclea 60 caracteres por
minuto, son 60 revalidaciones por minuto, ~0.1% del tiempo de CPU.

La regla "no bloquear el FXAT" se refiere a operaciones que tardan
>100ms (lecturas de archivo, queries de BD, llamadas de red). Para
operaciones de microsegundos, revalidacion sincrona esta bien.

Si la validacion fuera CARA (consultar BD para ver si el email ya
existe), entonces si: hay que (1) hacer la consulta en un Task, (2)
mostrar feedback de "verificando..." en el campo mientras tanto, (3)
recibir el resultado en el FXAT con setOnSucceeded.

**Qué hice con eso:**
Implemente la validacion sincrona en cada keystroke con
textProperty().addListener. Cada cambio re-evalua los 5 campos y
habilita/deshabilita el boton Guardar. La etiquetaError muestra el
PRIMER error encontrado (en orden de prioridad), no todos juntos
para no abrumar.

Documente en REFLEXION pregunta 1 la regla "ops <100ms en el FXAT,
mas pesadas en otro hilo".

**Qué aprendí:**
Que las advertencias sobre "no bloquees el FXAT" se aplican a
operaciones REALMENTE caras, no a cualquier codigo. Una validacion
de regex es libre. Una consulta a BD no. Esta distincion ayuda a no
sobre-ingenierizar: muchos tutoriales meten Tasks por reflejo en
casos donde una operacion sincrona es perfectamente apropiada.

---

## Intercambio 5 — Por que las dialogos de Alert no toman el CSS del padre

**Mi prompt:**
> Mi tema CSS funciona bien para la ventana principal pero los Alert
> de confirmacion salen con estilo default de javafx. ¿Como hago que
> hereden el tema?

**Resumen de la respuesta:**
La IA explico que cada Alert es su propia Scene con su propio
stylesheet. NO hereda el CSS del padre automaticamente. Hay dos
opciones:

1. **Aplicar el CSS al dialogPane:**
   ```java
   alert.getDialogPane().getStylesheets().add(
           getClass().getResource("/styles.css").toExternalForm());
   ```

2. **No estilizar Alerts** y aceptar el aspecto default.

La opcion 1 es mas profesional pero requiere consistencia: aplicarla
en CADA Alert. La opcion 2 es practica si el manual del proyecto no
exige estilo en alerts.

Para esta practica, recomendo agregar reglas CSS para `.dialog-pane`
y `.dialog-pane .label` en styles.css por si se aplica al dialogo, pero
NO aplicar el stylesheet a cada Alert explicitamente para no
sobrecargar el codigo.

**Qué hice con eso:**
Agregue reglas CSS para `.dialog-pane` en styles.css (fondo oscuro,
texto claro). En el codigo NO aplico el stylesheet a cada Alert; los
Alerts toman estilo default. Esto da una experiencia consistente
"casi siempre" sin tener que repetir codigo en cada uno.

Si en el futuro queremos estilizar todos, agregamos un helper
`estilizarDialogo(Dialog)` que aplique los stylesheets y lo invocamos
en cada Alert.

**Qué aprendí:**
Que JavaFX tiene "scopes de estilo": cada Stage con Scene tiene su
propia coleccion de stylesheets. Es como CSS isolation en componentes
web. La ventaja es predictibilidad; la desventaja es que el estilo
no se "viraliza" entre ventanas. Hay que aplicarlo donde quieres
verlo.
