# Bitácora de IA — Práctica 10

Registro del uso de IA (Claude) como apoyo de pair programming durante esta práctica.

---

## Intercambio 1 — wait/notify dentro de synchronized

**Mi prompt:**
> En mi BufferAccesos quiero que el consumidor espere cuando la cola
> esta vacia. Pensaba usar Thread.sleep en un bucle hasta que tuviera
> algo. ¿Esta bien?

**Resumen de la respuesta:**
La IA me explico que NO: si uso Thread.sleep dentro de un metodo
synchronized, el hilo mantiene el lock dormido y NINGUN PRODUCTOR
puede entrar a depositar. El consumidor despierta, ve buffer todavia
vacio, vuelve a dormir, y asi por siempre. Deadlock garantizado.

La forma correcta es Object.wait(): libera el monitor mientras espera
y permite que otros hilos entren. Cuando un productor llama
notifyAll(), el consumidor despierta, reacquiere el lock y verifica
la condicion de nuevo.

Sugirio leer el bucle clasico `while (cond) wait();` y por que
`while` (no `if`): cuando un hilo despierta de wait, debe REVERIFICAR
la condicion porque (a) puede haber spurious wakeups, (b) otro
consumidor pudo haber agarrado el elemento entre el notify y mi
readquisicion del lock.

**Qué hice con eso:**
Implemente exactamente el patron `synchronized + while (cond) wait()`
en BufferAccesos.depositar y tomar. Documente en comentarios
extensos POR QUE wait y no sleep, y POR QUE while y no if. Esto se
volvio el corazon de la REFLEXION pregunta 3.

**Qué aprendí:**
Que el deadlock "obvio" (sleep dentro de synchronized) es exactamente
el primer error que un programador hace al ver concurrencia. La
existencia de wait/notify NO es una alternativa estilistica a sleep;
es la respuesta a un problema real que sleep crea. Saber la
diferencia es lo que separa codigo concurrente funcional de codigo
que se traba aleatoriamente en produccion.

---

## Intercambio 2 — notify vs notifyAll, ¿cual usar?

**Mi prompt:**
> En el buffer puedo despertar a uno o a todos los esperan. ¿notify()
> o notifyAll()? Veo en internet que se debate.

**Resumen de la respuesta:**
La IA explico la trampa: notify() despierta a UN hilo arbitrario. Si
en mi buffer estan esperando tanto productores (cuando esta lleno)
como consumidores (cuando esta vacio), notify podria despertar al
hilo equivocado. Ejemplo concreto: buffer lleno, hay 3 productores
esperando para depositar. Un consumidor toma un elemento, libera
espacio, llama notify. Pero notify despierta a OTRO consumidor (que
estaba esperando porque el buffer estaba vacio antes). Ese consumidor
ve la cola con elementos, toma, libera espacio, etc. Los 3 productores
nunca se despiertan: deadlock.

notifyAll despierta a todos. Los que no cumplen la condicion vuelven
a wait gracias al while; los que si la cumplen continuan. Hay un
pequeño costo de "despertarse para volver a dormir" pero es la opcion
segura.

**Qué hice con eso:**
Use notifyAll en todos los puntos. Documente la decision en el
SINCRONIZACION.md y en los comentarios del codigo. La regla simple
que me quedo: si hay UN solo tipo de espera, notify es suficiente;
si hay productores Y consumidores compitiendo, notifyAll es lo
correcto.

**Qué aprendí:**
Que la "optimizacion prematura" en concurrencia mata: notify() es
mas eficiente que notifyAll() pero introduce bugs sutiles si las
condiciones son complejas. notifyAll es la opcion segura y solo se
optimiza si el profiler muestra que es un cuello.

---

## Intercambio 3 — Como demostrar la race condition de manera REPRODUCIBLE

**Mi prompt:**
> Quiero que DemoRaceCondition haga visible la race. La hice con un
> contador y 10 hilos, pero al ejecutar a veces da el resultado
> correcto y no demuestra nada. ¿Como fuerzo que la race aparezca?

**Resumen de la respuesta:**
La IA explico que las JVMs modernas optimizan agresivamente. Un
`total = total + 1` puede mantenerse en un registro y nunca tocar
memoria hasta el final del bloque, escondiendo la race. Para
exhibirla con seguridad, hay que FORZAR un cambio de contexto entre
la lectura y la escritura.

La forma estandar: Thread.yield() entre el read y el write. yield()
sugiere al scheduler "puedes pasarme a otro hilo aqui". Aumenta
drasticamente la probabilidad de que el hilo se cambie justo en
medio del incremento.

Alternativa mas brutal: Thread.sleep(0) o Thread.sleep(1). Mas
agresivo pero garantiza que la race aparezca. Sleep(0) en lugar de
yield() porque algunos JIT optimizan yield a no-op.

Advirtio que el yield NO debe estar en codigo de produccion. Es
solo un truco didactico para que la demo sea contundente.

**Qué hice con eso:**
Agregue Thread.yield() entre la lectura y escritura en
registrarSinSinc(). Documente en comentarios CLARAMENTE que el yield
esta ahi solo para hacer la race reproducible y que en codigo real
no estaria. El resultado fue dramatico: paso de 100.000 a 15.002
incrementos, 85% perdidos. Esto vale como evidencia empirica para
el SINCRONIZACION.md.

**Qué aprendí:**
Que demostrar concurrencia es como demostrar fisica: necesitas un
"experimento controlado" que aisle el fenomeno. El JIT y el cache
son enemigos de las demos: lo "obvio" sobre concurrencia se vuelve
invisible si el codigo se optimiza demasiado. Inyectar puntos de
preemption controlados es lo que permite ver el bug real.

---

## Intercambio 4 — Detener un hilo limpiamente sin Thread.stop()

**Mi prompt:**
> Mi torniquete corre en un bucle while(true). ¿Como lo detengo
> ordenadamente cuando termina la simulacion? Vi que Thread.stop()
> esta deprecated pero no se que usar en su lugar.

**Resumen de la respuesta:**
La IA explico que Thread.stop() esta deprecated porque "matar" un
hilo a la fuerza puede dejar estructuras de datos en estado
inconsistente (imagina que estaba a la mitad de sincronizar un
contador con su mapa indice y lo matas).

El patron moderno es "detencion cooperativa": el hilo verifica una
bandera periodicamente y termina ordenadamente cuando esta es true.
Dos elementos:

1. `private volatile boolean detenerse = false;` La palabra
   `volatile` es CRITICA: sin ella, el hilo podria cachear el valor
   en un registro y nunca ver el cambio cuando otro hilo lo pone
   en true.

2. `interrupt()` complementario: si el hilo esta dormido en wait() o
   sleep(), la bandera por si sola no lo despierta. interrupt() lanza
   InterruptedException al hilo dormido para sacarlo de su sueño.

**Qué hice con eso:**
Implemente Torniquete.detener() con ambas tecnicas: pone la bandera
volatile en true Y llama this.interrupt(). En run() captura
InterruptedException y sale del bucle limpiamente. Cubri con una
prueba (pruebaTorniqueteSeDetieneOrdenadamente) que verifica que un
torniquete bloqueado en wait() se detiene en <2s al recibir la
señal.

**Qué aprendí:**
Que la concurrencia tiene "patrones obligatorios" que el lenguaje
no enseña por si mismo. volatile + interrupt para detencion
cooperativa es uno de esos patrones que aparece en TODO codigo
concurrente serio y no esta documentado de forma central. Lo
aprendi por dolor en este intercambio.

---

## Intercambio 5 — ¿Sincronizar los getters tambien? Parece innecesario

**Mi prompt:**
> En mi ContadorAccesos, registar() es synchronized pero ¿getTotal()
> tambien debe serlo? Solo es una lectura, no modifico nada. Parece
> excesivo y costoso.

**Resumen de la respuesta:**
La IA explico que SI debe ser synchronized, pero por una razon
distinta a la exclusion mutua: la VISIBILIDAD. En arquitecturas
modernas (multi-core con caches separados por CPU), un hilo puede
modificar `total` en su cache local y otro hilo puede seguir leyendo
el valor viejo desde su propio cache durante un buen rato.

synchronized no solo garantiza exclusion mutua; tambien garantiza
"happens-before": cuando un hilo SALE de un bloque synchronized,
sus cambios se publican (flush a memoria principal). Cuando otro
hilo ENTRA al mismo monitor, ve esos cambios.

Sin synchronized en el getter, podria leer un valor stale durante
segundos en JVMs sin optimizaciones especiales. Con synchronized,
veo el valor mas reciente garantizado.

Alternativa: declarar `total` como `volatile`. volatile da
visibilidad sin exclusion mutua. Pero como ya tengo synchronized
en los escritores, agregarlo solo a la lectura simplifica:
synchronized en ambos lados es coherente.

**Qué hice con eso:**
Sincronice tambien getTotal, getTotalVIP, etc. en ContadorAccesos.
Documente la razon de visibilidad en un comentario al inicio de la
clase. Esto se vio en la REFLEXION pregunta 2 cuando explique las
desventajas de synchronized (mencione visibilidad como ventaja
adicional).

**Qué aprendí:**
Que la concurrencia tiene DOS dimensiones que se confunden: exclusion
mutua (un hilo a la vez) y visibilidad (los cambios de un hilo son
vistos por otros). synchronized resuelve las dos. volatile solo
resuelve visibilidad. Un sistema sin volatile NI synchronized en una
variable compartida puede dar resultados aparentemente correctos por
horas hasta que el cache no se invalida y empieza a fallar.
