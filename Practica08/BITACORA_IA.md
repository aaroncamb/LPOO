# Bitácora de IA — Práctica 8

Registro del uso de IA (Claude) como apoyo de pair programming durante esta práctica.

---

## Intercambio 1 — Decidir las 4 estructuras (no caer en "ArrayList para todo")

**Mi prompt:**
> La consigna me pide 4 estructuras distintas con justificacion. Mi
> instinto es usar ArrayList para todo y agregar otras tres "porque
> hay que cumplir". Como elijo las estructuras de verdad, sin que se
> sienta forzado?

**Resumen de la respuesta:**
La IA propuso pensar en operaciones REALES del gimnasio y mapear cada
una a la estructura mas eficiente:
  - "Mostrar todos los clientes" → iterar en orden → ArrayList.
  - "Buscar cliente por id" → lookup → HashMap.
  - "¿Este email ya esta?" → contains → HashSet.
  - "Procesar notificaciones en orden de llegada" → FIFO → LinkedList.

Si las operaciones del negocio justifican cada eleccion, ya no es
"cumplir requisito", es diseño honesto. Recomendo escribir el por que
de cada estructura ANTES de codificar, para que la implementacion siga
al razonamiento.

**Qué hice con eso:**
Hice el ejercicio: liste las 5-6 operaciones mas frecuentes del gestor
y para cada una pense cual estructura era O(1) o lo mas barata posible.
Quedaron exactamente las cuatro que la IA sugirio. Esto me dio
material rico para el README (tabla con justificacion) y para la
REFLEXION pregunta 1.

**Qué aprendí:**
Que las estructuras no se eligen "porque hay que tener variedad" sino
porque cada operacion frecuente tiene un costo asintotico, y la
estructura correcta lo minimiza. Esta forma de pensar (operaciones
frecuentes → estructura optima) es lo que separa codigo amateur de
codigo profesional.

---

## Intercambio 2 — Por que no usar ArrayList para la cola de notificaciones

**Mi prompt:**
> La cola de notificaciones podria ser un ArrayList con add() al final
> y remove(0) al frente. ¿Por que LinkedList es mejor?

**Resumen de la respuesta:**
La IA explico que `ArrayList.remove(0)` es O(n) porque hay que
desplazar TODOS los elementos restantes una posicion hacia adelante.
Si la cola tiene 1.000 notificaciones y proceso una por segundo, cada
poll cuesta ~1.000 operaciones de copia. Es O(n^2) total para procesar
la cola entera.

En LinkedList, `poll()` (que internamente desconecta el primer nodo)
es O(1). Procesar la cola entera es O(n), no O(n^2).

Tambien menciono ArrayDeque como alternativa moderna mas eficiente que
LinkedList para colas (mejor cache locality), pero recomendo LinkedList
para la practica porque es la que la consigna mencionaba explicitamente.

**Qué hice con eso:**
Use LinkedList y documente la justificacion en el README. La diferencia
O(1) vs O(n) la registre tambien en el benchmark (aunque al final no
incluye este caso, lo mencione en las conclusiones). Esto me da una
respuesta solida si en defensa oral preguntan "¿por que no ArrayList
para la cola?".

**Qué aprendí:**
Que las estructuras tienen perfiles de complejidad asimetricos: algunas
operaciones son baratas, otras caras. Saberlo permite combinar las
estructuras correctas para cada caso. ArrayList es excelente para
acceso aleatorio y append; pésima para inserciones/eliminaciones en
posiciones arbitrarias. LinkedList es lo opuesto.

---

## Intercambio 3 — Como hacer el benchmark sin trampearse

**Mi prompt:**
> El entregable 7 pide "analisis de tiempo de ejecucion". Si solo mido
> con System.nanoTime una vez, los resultados van a ser inestables por
> el GC y el JIT. ¿Como hago una medicion creible?

**Resumen de la respuesta:**
La IA explico tres practicas standard de microbenchmarking:
  1. **Calentamiento (warmup):** ejecutar la operacion N veces antes de
     medir, para que el JIT optimice el codigo. Sin warmup, las
     primeras mediciones son siempre mas lentas.
  2. **Repetir y tomar mediana:** correr la operacion 50-100 veces y
     usar la mediana (no la media) para descartar outliers de pausas
     de GC o context switches del SO.
  3. **Comparar magnitudes, no valores absolutos:** los tiempos
     absolutos dependen del hardware, asi que la conclusion debe ser
     "A es 500x mas rapida que B", no "A toma 250ns".

Tambien mencionó JMH (Java Microbenchmark Harness) que es la herramienta
profesional, pero recomendo NO usarla para este caso porque agrega
complejidad innecesaria y no es del nivel del curso. nanoTime con
warmup y mediana es suficiente y se ve maduro.

**Qué hice con eso:**
Implemente el helper `medir()` con 10 iteraciones de warmup, 50
mediciones, sort y devolver la mediana. En las conclusiones documente
que los tiempos absolutos dependen del hardware y lo importante es la
proporcion. Esto suena mucho mas profesional que solo imprimir un
nanoTime crudo.

**Qué aprendí:**
Que medir performance honestamente es dificil y requiere disciplina. Si
solo corro la operacion una vez, lo que mida puede ser mas el GC que
el codigo. El warmup + mediana es un truco simple que mejora muchisimo
la calidad de las mediciones.

---

## Intercambio 4 — Tuve resultados sorprendentes en el benchmark

**Mi prompt:**
> El benchmark me dio dos sorpresas: (1) sort por Comparable es 6x mas
> rapido que por Comparator de fecha; (2) for-each es 3x mas rapido
> que stream. ¿Estoy haciendo algo mal? Pense que streams y comparators
> iban a ser similares al for-each.

**Resumen de la respuesta:**
La IA me dio explicaciones tecnicas para cada caso:

  1. **Sort por Comparable mas rapido**: `String.compareToIgnoreCase`
     compara byte a byte y termina temprano en la primera diferencia.
     `LocalDate.compareTo` descompone año, mes y dia (3 comparaciones
     internas). Por elemento la diferencia es chica, pero multiplicada
     por n*log(n) operaciones de sort, se nota. La conclusion correcta
     es que **la velocidad del sort depende del costo del comparator,
     no de si es Comparable o Comparator**.

  2. **For-each mas rapido que stream**: cierto y esperado. El stream
     tiene overhead de objetos intermedios (Spliterator, lambda,
     pipeline). Para una sola operacion simple, ese overhead se nota.
     Para pipelines complejos (filter + sort + limit + collect), el
     overhead se amortiza y la legibilidad gana.

Sugirio que NO ocultara esos resultados sorprendentes en el README,
sino que los presentara con la explicacion: muestra que entiendo lo
que mido y que no estoy maquillando resultados.

**Qué hice con eso:**
Actualice la seccion "Conclusiones" del benchmark con las
explicaciones reales (la version inicial decia algo generico tipo
"ambos son iguales"). En el README discuti los resultados con
honestidad: stream es mas lento pero gana en mantenibilidad. Esto se
ve maduro porque no oculta el trade-off.

**Qué aprendí:**
Que los benchmarks pueden sorprender, y la respuesta correcta no es
"el benchmark esta mal" sino "entender por que dio ese resultado". A
veces lo que medimos contradice nuestras intuiciones, y ahi es donde
aprendemos cosas reales sobre el lenguaje y las estructuras.

---

## Intercambio 5 — Iterator.remove() vs ConcurrentModificationException

**Mi prompt:**
> Quiero eliminar los clientes inactivos. Si hago
> `for (Cliente c : lista) if (!c.esActivo()) lista.remove(c);`
> ¿que pasa?

**Resumen de la respuesta:**
La IA dijo que eso lanza `ConcurrentModificationException` en la
SIGUIENTE iteracion. El motivo: el for-each usa internamente un
Iterator que tiene un contador de modificaciones; cuando hago
`lista.remove(c)` ese contador se incrementa y el iterator detecta
que "alguien modifico la lista por fuera" y lanza la excepcion.

La forma correcta es usar el Iterator explicitamente y llamar
`iterator.remove()`, que avisa al iterator del cambio. Otra opcion es
`list.removeIf(predicate)`, que internamente usa el iterator y es mas
limpio para casos simples.

**Qué hice con eso:**
Implemente `purgarInactivos()` con Iterator explicito porque la
consigna pide "uso de iteradores" como entregable. Documente en el
codigo POR QUE no se puede hacer con for-each + remove. Esto me da
material para defender la decision oralmente.

**Qué aprendí:**
Que la ConcurrentModificationException no es solo cosa de threads, la
puede lanzar un solo thread modificando una coleccion mientras la
itera. El nombre es engañoso. La regla simple: si necesitas modificar
una coleccion mientras la recorres, usa Iterator o removeIf.
