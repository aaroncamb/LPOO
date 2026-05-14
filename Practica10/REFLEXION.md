# Reflexión — Práctica 10

## 1. ¿Qué es una race condition? Describe un escenario concreto de tu código donde podría ocurrir.

Una **race condition** es un bug que ocurre cuando dos o más hilos
acceden a un recurso compartido al mismo tiempo y el resultado final
depende del **orden de ejecución** que el scheduler decida. Como el
scheduler no es predecible, el bug aparece a veces sí y a veces no, lo
cual lo hace especialmente difícil de detectar y reproducir.

El nombre "race" viene de que los hilos están literalmente "compitiendo"
para llegar primero a la operación crítica.

### Escenario concreto en mi código

`ContadorAccesos.registrarSinSinc(...)` (la versión deliberadamente sin
sincronizar, solo para demostrar el bug):

```java
public void registrarSinSinc(String tipoMembresia) {
    int leido = total;        // paso 1
    total = leido + 1;        // paso 2
    ...
}
```

Aunque parezca atómico, `total++` se compila a **tres operaciones de
máquina**: leer `total`, sumarle 1, escribir `total`. Si dos hilos
ejecutan estos pasos intercalados, pueden perder un incremento.

Escenario paso a paso, con `total` empezando en 100:

| Tiempo | Hilo A | Hilo B | Valor de total |
|---|---|---|---|
| t1 | lee total → 100 | | 100 |
| t2 | | lee total → 100 | 100 |
| t3 | escribe 101 | | 101 |
| t4 | | escribe 101 | 101 |

Ambos hilos creían incrementar. Esperábamos 102. Quedó en 101. Un
incremento se perdió.

Y lo demuestra empíricamente mi `DemoRaceCondition`: en una corrida
típica, **10 hilos × 10.000 incrementos = 100.000 esperados** terminan
en **15.002 contados**. Se pierden ~85% de los incrementos.

Otra race que evité: en el `BufferAccesos`, si dos torniquetes
hicieran `cola.poll()` al mismo tiempo, uno podría obtener el último
elemento y el otro podría obtener `null` aunque la cola contenía
elementos al inicio de la operación. Esto causaría
`NullPointerException` cuando el segundo torniquete intente procesar.

## 2. ¿Por qué `synchronized` resuelve el problema? ¿Qué desventaja de rendimiento tiene?

### Cómo resuelve el problema

`synchronized` garantiza **exclusión mutua** sobre un monitor (un
objeto). Solo un hilo a la vez puede estar dentro del bloque
sincronizado. Mientras el primer hilo está dentro, todos los demás
quedan esperando en la entrada hasta que el primero salga.

En mi `ContadorAccesos.registrar(...)` con `synchronized` en la firma,
el monitor es `this` (la instancia del contador):

```java
public synchronized void registrar(String tipoMembresia) {
    total++;
    ...
}
```

Como solo un hilo puede ejecutar la operación a la vez, los tres pasos
(leer, sumar, escribir) ocurren sin interrupción. Ya no es posible
que dos hilos lean el mismo valor inicial.

`synchronized` también garantiza **visibilidad**: cuando un hilo sale
de un bloque sincronizado, sus cambios se "publican" para que todos
los hilos los vean. Sin esto, un hilo lector podría seguir viendo un
valor stale cacheado en su registro local.

### Las desventajas de rendimiento

**1. Contención**: si muchos hilos quieren entrar al mismo bloque, la
mayoría espera. La sección crítica se vuelve un cuello de botella.
Cuanto más larga sea la sección, peor; idealmente debe ser lo más
corta posible (solo lo estrictamente compartido).

**2. Costo de la adquisición**: tomar y soltar un monitor tiene
costo no trivial (varios cientos de nanosegundos en JVMs modernas).
Para operaciones muy frecuentes y muy cortas (como un contador), ese
costo puede ser comparable al trabajo útil.

**3. Bloqueo de hilos**: cuando un hilo no puede entrar, queda
"parado" sin hacer nada. Los hilos parados desperdician CPU si la
JVM no los reasigna a otra tarea.

**4. Riesgo de deadlock**: si tomo dos monitores en orden distinto en
distintos hilos, puede haber deadlock. `synchronized` no protege
contra esto; lo evita el cuidado del programador.

### Alternativas y cuándo no usar synchronized

Para casos puntuales hay alternativas más eficientes:

- **`AtomicInteger`** para contadores: usa CAS (compare-and-swap) del
  hardware. No bloquea hilos, no requiere monitor. Para un contador
  como el mío, sería más rápido y más simple. No lo usé porque la
  consigna pide demostrar `synchronized`.

- **`java.util.concurrent.locks.ReentrantLock`**: más flexible que
  `synchronized` (timeouts, lock entre métodos distintos, fairness).
  Más complejo de usar correctamente.

- **Estructuras concurrentes**: `ConcurrentHashMap`,
  `BlockingQueue`. Si las hubiera usado para el buffer, no habría
  necesitado `synchronized` ni `wait/notify` manuales — la propia
  estructura lo maneja.

La regla práctica: `synchronized` es la opción "simple y correcta"
por defecto. Cuando el profiler muestra que es un cuello de botella,
se piensa en alternativas. No al revés.

## 3. ¿Qué diferencia hay entre `Thread.sleep()` y `Object.wait()`? ¿Cuándo usarías cada uno?

Aunque ambos "pausan" al hilo, son **completamente distintos** en lo
que hacen con los recursos.

### `Thread.sleep(ms)`

- Es un **método estático** de `Thread`.
- Pausa al hilo actual por al menos `ms` milisegundos.
- **NO libera ningún monitor que el hilo tenga adquirido**.
- Se "despierta" solo (cuando pasa el tiempo) o por
  `interrupt()`.

### `Object.wait()`

- Es un método de **instancia** de `Object` (toda clase lo tiene).
- Solo se puede llamar **desde dentro de un bloque `synchronized`**
  sobre ese mismo objeto. Si lo llamas fuera, lanza
  `IllegalMonitorStateException`.
- **Libera el monitor que el hilo tenía sobre ese objeto** antes de
  dormirse.
- Se despierta cuando otro hilo llama `notify()` o `notifyAll()`
  sobre el mismo objeto (o por `interrupt()`).
- Al despertarse, tiene que **readquirir el monitor** antes de
  continuar.

### La diferencia que importa: el monitor

Si dentro de un bloque sincronizado el hilo hace `Thread.sleep()`,
**mantiene el lock dormido**. Ningún otro hilo puede entrar a ese
monitor. Esto **garantiza deadlock** si el escenario espera a que
otro hilo modifique el estado.

Si dentro de un bloque sincronizado el hilo hace `wait()`,
**suelta el lock**. Otros hilos pueden entrar, modificar el estado, y
eventualmente llamar `notify` o `notifyAll` cuando la condición
cambie. El hilo dormido despierta, readquiere el lock, y continúa.

### Cuándo usaría cada uno

**`Thread.sleep`**: cuando quiero pausar al hilo por un tiempo fijo
sin que dependa de ningún otro hilo. Es lo que uso en `Torniquete.run`
para simular el tiempo de validación de la tarjeta:

```java
int latencia = ThreadLocalRandom.current().nextInt(80, 250);
Thread.sleep(latencia);
```

Aquí no estoy esperando una condición ni libero monitor — el
torniquete solo está "trabajando" durante ese tiempo. `sleep` es
exactamente la herramienta correcta.

**`wait()`**: cuando estoy esperando que **una condición cambie** y
esa condición depende de otro hilo. Es lo que uso en
`BufferAccesos.tomar` cuando el buffer está vacío:

```java
synchronized (this) {
    while (cola.isEmpty()) {
        wait();   // libero el monitor, otros hilos pueden depositar
    }
    ...
}
```

Si aquí usara `Thread.sleep`, el consumidor mantendría el monitor del
buffer dormido y **ningún productor podría depositar** mientras el
consumidor duerme. El consumidor despertaría, vería el buffer
todavía vacío, y volvería a dormir. **Deadlock garantizado.** `wait()`
libera el monitor justo el tiempo necesario para que un productor
entre, deposite, llame `notifyAll`, y suelte el lock.

### Regla práctica

`sleep` para "esperar un rato".
`wait` para "esperar a que pase algo".

Si lo que espero depende de otros hilos, casi siempre quiero `wait`
+ `notify`. Si solo necesito una pausa de tiempo independiente,
`sleep`.

Una trampa común: nuevos programadores ven que `sleep` "funciona"
para esperar y lo usan en todos lados. El programa "funciona" la
mayor parte del tiempo, hasta que aparece un caso donde el orden
importa y el sistema se traba sutilmente. `wait/notify` cuesta más
de aprender pero evita esta categoría entera de bugs.
