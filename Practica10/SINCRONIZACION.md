# Estrategias de sincronización — Práctica 10

Este documento explica las estrategias de sincronización usadas en la
simulación de torniquetes del gimnasio, las race conditions que
identifiqué y cómo las resolví.

## Resumen rápido

Tres puntos del sistema requieren sincronización:

1. **`BufferAccesos`** (productor-consumidor): bloque sincronizado con
   `synchronized` + `wait()` / `notifyAll()`.
2. **`ContadorAccesos`** (contador compartido): métodos sincronizados
   con la palabra clave `synchronized` en la firma.
3. **Escritura al archivo de log**: bloque sincronizado sobre un
   monitor estático compartido entre torniquetes.

Cada uno responde a un patrón distinto. Los detallo a continuación.

## 1. Buffer compartido (productor-consumidor)

### El problema

Los clientes llegan a un buffer compartido (`LinkedList<AccesoSolicitado>`).
Los torniquetes los toman para procesarlos. Dos races posibles:

- **Race en la modificación**: si dos torniquetes hacen `cola.poll()` al
  mismo tiempo, ambos podrían obtener el mismo elemento o uno podría
  obtener `null` aunque haya elementos.
- **Race en la condición**: un torniquete revisa "¿está vacía?", ve que
  no, pero antes de hacer `poll` otro torniquete saca el último
  elemento. El primer torniquete intenta sacar de una cola ya vacía.

### La estrategia

**Métodos `synchronized` + `wait/notifyAll`**:

```java
public synchronized void depositar(AccesoSolicitado a) throws InterruptedException {
    while (cola.size() >= capacidadMaxima) {
        wait();   // libera el monitor; vuelve a competir cuando despierte
    }
    cola.offer(a);
    notifyAll();
}

public synchronized AccesoSolicitado tomar() throws InterruptedException {
    while (cola.isEmpty()) {
        wait();
    }
    AccesoSolicitado a = cola.poll();
    notifyAll();
    return a;
}
```

### Por qué cada decisión

**`synchronized` en el método entero**: garantiza que `cola.poll()` y
`cola.offer()` sean atómicos. Solo un hilo a la vez puede estar dentro.

**`while` en lugar de `if`**: cuando un hilo se despierta de `wait()`,
debe **reverificar la condición**. Razón: otro hilo pudo haber
consumido el elemento entre el `notifyAll` y el momento en que este
hilo readquiere el monitor. Además, existen los "spurious wakeups"
(despertares espontáneos sin notify) en algunas JVMs.

**`wait()` en lugar de `Thread.sleep()`**: `wait()` libera el monitor;
`sleep()` no. Si un consumidor hace `Thread.sleep()` cuando el buffer
está vacío, mantiene el lock y todos los productores también se
quedan bloqueados esperando entrar — deadlock garantizado. `wait()`
suelta el lock para que otros entren y eventualmente lo notifiquen.

**`notifyAll()` en lugar de `notify()`**: en nuestro buffer pueden
estar esperando tanto productores (cuando está lleno) como
consumidores (cuando está vacío). `notify()` despierta a **uno
arbitrario**: podría despertar a un productor cuando lo correcto era
despertar a un consumidor, dejando al consumidor dormido para siempre.
`notifyAll()` despierta a todos; los que no cumplen la condición
vuelven a `wait` (gracias al `while`), pero al menos el correcto sí
sigue su camino.

## 2. Contador compartido (ejemplo canónico de race)

### El problema

Cada torniquete incrementa el contador total de accesos del día.
La operación `total++` parece atómica pero se compila a tres pasos:

1. Leer `total` a un registro.
2. Incrementar el registro.
3. Escribir el registro de vuelta a `total`.

Si dos hilos hacen estos pasos intercalados, pueden leer el mismo
valor inicial y escribir el mismo valor final, perdiendo un
incremento.

### Demostración empírica

Ejecutando `DemoRaceCondition` (10 hilos x 10.000 incrementos = 100.000
esperado):

```
--- SIN synchronized ---
  Total contado: 15002      <-- perdimos 85% de los incrementos

--- CON synchronized ---
  Total contado: 100000     <-- exacto
```

Sin sincronización, **se perdieron 84.998 incrementos**. La race
condition no es teoría; es medible en cualquier corrida del programa.

(Nota técnica: agregué `Thread.yield()` entre la lectura y la escritura
del incremento sin sincronizar para forzar el cambio de contexto que
expone la race de manera reproducible. Sin el yield, las
optimizaciones del JIT pueden "esconder" la race en ciertas
arquitecturas, dando la falsa impresión de que no existe.)

### La estrategia

**Método entero `synchronized`**:

```java
public synchronized void registrar(String tipoMembresia) {
    total++;
    switch (tipoMembresia) {
        case "VIP"     -> totalVIP++;
        case "PREMIUM" -> totalPremium++;
        case "BASICA"  -> totalBasica++;
    }
}
```

### Por qué sincronicé también los getters

```java
public synchronized int getTotal() { return total; }
```

Sin `synchronized` en el getter, un hilo lector podría leer un valor
**stale** (en caché de CPU). `synchronized` no solo garantiza
exclusión mutua; también garantiza **visibilidad**: cuando un hilo
sale de un bloque `synchronized`, sus cambios se "publican" para que
todos los hilos los vean. Sin esto, el lector podría seguir viendo el
valor inicial aun después de incrementos.

### Alternativa más moderna

`AtomicInteger` resuelve este caso específico de forma más eficiente
(usa CAS — compare-and-swap — del hardware, no requiere `synchronized`).
No la usé porque la consigna pide demostrar explícitamente
`synchronized`/`wait`/`notify`. En código real para un contador
simple, `AtomicInteger` es preferible.

## 3. Escritura al archivo de log

### El problema

Tres torniquetes intentan escribir al mismo archivo. Si no
sincronizo, dos escrituras pueden mezclarse y producir líneas
corruptas tipo `[hora] [hora] mensaje1mensaje2`.

### La estrategia

**Lock estático compartido entre todos los torniquetes**:

```java
private static final Object LOCK_ARCHIVO = new Object();

private void escribirLog(String mensaje) {
    synchronized (LOCK_ARCHIVO) {
        try (BufferedWriter w = new BufferedWriter(
                new FileWriter(archivoLog, true))) {
            w.write(...);
        }
    }
}
```

### Por qué un lock externo y no `synchronized` en el método

Si pongo `synchronized` en el método, el lock es **`this`** — la
instancia del torniquete. Pero hay **3 torniquetes distintos** y cada
uno tiene su propio `this`. Sincronizar sobre `this` no impide que
torniquetes distintos escriban a la vez (solo impide que el mismo
torniquete se escriba sobre sí mismo, lo cual nunca pasa).

La solución correcta es un lock **compartido entre todos los
torniquetes**: una variable `static final` del tipo Object. Todos los
torniquetes lo sincronizan; el lock asegura que solo uno escriba al
archivo a la vez, sin importar cuál torniquete sea.

## Race conditions identificadas y resueltas

| # | Race | Dónde estaba | Solución |
|---|------|------|----------|
| 1 | `cola.poll()` concurrente con cola vacía | `BufferAccesos.tomar()` | `synchronized` + `wait()` con `while` |
| 2 | `cola.offer()` concurrente con cola llena | `BufferAccesos.depositar()` | `synchronized` + `wait()` con `while` |
| 3 | `total++` no atómico | `ContadorAccesos.registrar` | `synchronized` en método |
| 4 | Visibilidad de `total` desde otro hilo | `ContadorAccesos.getTotal` | `synchronized` en getter |
| 5 | Escritura concurrente al log | `Torniquete.escribirLog` | `synchronized` sobre lock estático compartido |
| 6 | Detención de hilo en `wait()` | `Torniquete.detener()` | `volatile boolean` + `interrupt()` |

## Detención cooperativa de hilos (bonus)

`Thread.stop()` está deprecated por una razón: detener un hilo
forzadamente puede dejar estructuras de datos en estado inconsistente.
La manera moderna es **cooperativa**:

```java
private volatile boolean detenerse = false;

public void detener() {
    this.detenerse = true;
    this.interrupt();   // despierta si estaba en wait()/sleep()
}
```

El campo es `volatile` para que el cambio sea **visible
inmediatamente** desde otros hilos (sin volatile, podría quedar
cacheado en un registro de CPU). El `interrupt()` complementa: si el
torniquete está bloqueado en `buffer.tomar()` (en `wait()`), el
interrupt lo despierta con `InterruptedException` para que pueda
salir de su bucle.

## Cómo verifico que funciona

Las pruebas en `ConcurrenciaTest.java` validan **invariantes**:

- Buffer producido = buffer consumido (ningún mensaje se pierde ni se
  duplica).
- Buffer nunca supera la capacidad máxima.
- Contador con `synchronized` = exactamente el número de incrementos.
- Suma de procesados por torniquete = total enviado al buffer.
- Torniquete bloqueado en `wait()` se detiene en menos de 2 segundos
  al llamar `detener()`.

Estas invariantes son cosas que deben cumplirse **siempre**,
independiente del scheduling. Si alguna falla aunque sea una vez en
mil corridas, hay un bug de concurrencia que hay que arreglar.
