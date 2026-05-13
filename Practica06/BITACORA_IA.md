# Bitácora de IA — Práctica 6

Registro del uso de IA (Claude) como apoyo de pair programming durante esta práctica.

---

## Intercambio 1 — Reusar Servicio de P5 o hacer una clase abstracta nueva

**Mi prompt:**
> P6 me pide clase abstracta + 3 interfaces + 3 concretas que combinen
> interfaces de forma asimetrica. Ya tengo Servicio en P5. ¿La reuso o
> creo una nueva clase abstracta? La consigna no es clara.

**Resumen de la respuesta:**
La IA argumento que reusar es lo correcto por dos razones: (1) la
consigna no pide "clase abstracta nueva", solo "una clase abstracta
con metodos abstractos y concretos y atributos protected", y Servicio
ya cumple; (2) la coherencia narrativa del dominio se debilita si
empiezo a fabricar clases abstractas en cada practica.

Sugirio enriquecer Servicio para esta practica con uno o dos metodos
concretos extras y un atributo protected mas, para que se note que la
abstracta esta "viva" y crece practica con practica.

**Qué hice con eso:**
Reuse Servicio y le agregue: el atributo `notas` (protected), los
metodos concretos `resumen()`, `descripcionCompleta()`, `agregarNota()`.
Esto deja a la abstracta con 6 metodos concretos heredados + 3 abstractos
+ 6 atributos protected, mas que suficiente para el entregable.

**Qué aprendí:**
Que la coherencia entre practicas vale mas que fabricar clases nuevas
cada vez. Cuando una practica te pide algo y ya lo tienes parcialmente,
extenderlo es mejor que duplicar; ademas demuestra que entiendes
acumular complejidad sobre piezas existentes en lugar de empezar de
cero todo el tiempo.

---

## Intercambio 2 — Como hacer las combinaciones asimetricas defensibles

**Mi prompt:**
> La consigna pide que cada clase implemente combinaciones DIFERENTES
> de interfaces, y que cada combinacion tenga sentido. No quiero que
> mis decisiones se vean arbitrarias. ¿Como hago que cada combinacion
> tenga razon real de negocio?

**Resumen de la respuesta:**
La IA propuso pensar al reves: en lugar de elegir interfaces y luego
asignarlas, empezar preguntando "¿que cosas REALMENTE puede hacer cada
servicio en el mundo real?". Despues filtrar:

  - ClaseGrupal es horario publico → no se reagenda individualmente
    (afectaria al grupo). → NO Reagendable.
  - EntrenamientoPersonal es servicio premium privado → no aparece en
    reportes operacionales agregados (sus metricas son irrelevantes
    a nivel macro). → NO Reportable.
  - EvaluacionFisica es cita individual + datos agregables + comunicacion
    → las tres aplican.

Recomendo escribir la justificacion ANTES de tocar codigo, para que la
implementacion siga al razonamiento y no al reves.

**Qué hice con eso:**
Escribi primero el bloque de justificaciones (la tabla con N/R/A y los
parrafos explicativos), y solo despues implementé las clases. Esto me
ahorro reescrituras: si hubiera codificado primero, probablemente
habria hecho que las tres implementaran las tres por inercia, y
despues tendria que justificar lo que ya estaba.

**Qué aprendí:**
Que el orden importa: la justificacion antes del codigo lleva a mejor
codigo; el codigo antes de la justificacion lleva a justificaciones
forzadas. Esta es una lección general de diseño que no es solo para
esta práctica.

---

## Intercambio 3 — Para que sirven los metodos default

**Mi prompt:**
> Veo que la pregunta 3 de reflexion habla de metodos default. ¿Vale
> la pena meterlos en mis interfaces, o son cosa de teoria? ¿Qué
> aportarian a mi codigo real?

**Resumen de la respuesta:**
La IA dio tres ejemplos concretos donde aportan:

  1. Composición de metodos: si las clases que implementan Notificable
     ya tienen enviarEmail y enviarSMS, un metodo default
     notificarMultiplesCanales que los combine en uno solo es "gratis"
     y evita que cada clase reescriba la coordinacion.

  2. Formato unificado: si todas las clases Reportable comparten el
     formato CSV, vivir el formato en la interfaz (no en cada clase)
     hace que un cambio futuro sea de un solo archivo.

  3. Calculos compartidos: en Reagendable, la validacion de
     "anticipacion minima" es la misma logica (diferencia de dias);
     ponerla en un default ahorra duplicacion en cada reagendar().

Tambien menciono el beneficio mas importante: agregar metodos default
en el futuro permite evolucionar la interfaz sin romper a los
implementadores existentes. Eso es por lo que existen.

**Qué hice con eso:**
Implemente los tres ejemplos. notificarMultiplesCanales, toCsvLine,
fechaRespetaAnticipacion son defaults usados activamente en el codigo.
En la REFLEXION (pregunta 3) explico ademas el caso de "agregar un
metodo nuevo a la interfaz sin romper nada", que era exactamente lo
que la pregunta pedia.

**Qué aprendí:**
Que los metodos default no son solo una feature elegante, son la
respuesta concreta a un problema practico: como mantener una interfaz
estable en un proyecto que crece. Java 8 los agrego por una razon
muy real (querian extender la interfaz Collection sin romper millones
de implementaciones existentes en la industria).

---

## Intercambio 4 — El cast en instanceof con interfaces es igual que con clases?

**Mi prompt:**
> Uso `if (s instanceof Notificable n)` para hacer cast a interfaz.
> Pero Notificable no es una clase, es una interfaz. ¿Esto funciona
> de la misma manera? ¿Es buena practica?

**Resumen de la respuesta:**
La IA confirmo que sí, el operador instanceof funciona identico para
clases e interfaces. Cuando dice `s instanceof Notificable`, Java
chequea si el objeto referenciado por s implementa esa interfaz (o
extiende una clase que la implemente). La sintaxis de pattern matching
`Notificable n` declara una variable cast a esa interfaz, lista para
usar dentro del if.

Mencionó que es una de las formas mas limpias de hacer polimorfismo
basado en capacidades: en lugar de preguntar "¿que tipo es?",
preguntas "¿que puede hacer?". Esa pregunta es generalmente mas util.

**Qué hice con eso:**
Use el patron en todo CentroOperaciones y en Main:

```java
for (Servicio s : servicios) {
    if (s instanceof Notificable n) {
        n.notificarMultiplesCanales(...);
    }
}
```

Esto deja el codigo limpio y poco acoplado: no menciona ClaseGrupal,
EntrenamientoPersonal ni EvaluacionFisica por nombre. Solo pregunta
"¿es Notificable?" y actua.

**Qué aprendí:**
Que el polimorfismo no es solo "tratar objetos por su clase padre"
sino tambien "tratar objetos por las capacidades que prometen". Las
interfaces hacen este segundo tipo posible: el codigo cliente no
necesita saber QUE clase es, solo QUE puede hacer.

---

## Intercambio 5 — Como organizar Servicio.notas: cuando un setter no basta

**Mi prompt:**
> Mi atributo protected `notas` (String) lo uso para anotar cuando se
> reagenda un servicio. Pero acumular notas requiere concatenar el
> texto anterior + el nuevo. Pense en hacer setNotas y manejar la
> concatenacion fuera, pero parece ruidoso. ¿Hay mejor opcion?

**Resumen de la respuesta:**
La IA sugirio agregar un metodo `agregarNota(String)` en la clase
abstracta, que se encarga de la concatenacion. Esto encapsula la
logica (separar con " | ", saltar nulls) y deja a las hijas con una
interfaz simple: solo llaman `agregarNota("texto")` y no piensan en
como se compone. Si manana cambio el separador, lo cambio en un solo
lugar.

Menciono que esto es un patron general: cuando un atributo tiene
logica de actualizacion no trivial, conviene tener un metodo dedicado
en lugar de un setter genérico.

**Qué hice con eso:**
Implementé `agregarNota(String)` en Servicio. EntrenamientoPersonal y
EvaluacionFisica lo usan dentro de su reagendar():

```java
public boolean reagendar(LocalDate nuevaFecha) {
    if (!fechaRespetaAnticipacion(nuevaFecha)) return false;
    this.fechaServicio = nuevaFecha;
    agregarNota("Reagendado a " + nuevaFecha);
    return true;
}
```

Limpio, una sola linea, y el padre decide como se almacena.

**Qué aprendí:**
Que getters y setters no son la unica forma de exponer acceso a un
atributo. A veces el metodo "operacion sobre el atributo" es mas util
y mas claro que el setter generico. Esto se relaciona con tell-don't-ask:
en lugar de pedirle al objeto sus datos para procesarlos afuera, le
dices al objeto que se procese a si mismo.
