# Práctica 10 — Programación Concurrente

## Objetivo

Construir una simulación con múltiples hilos que demuestre los
mecanismos de concurrencia de Java: `Thread`, `Runnable`,
`ExecutorService`, `synchronized`, `wait()`/`notifyAll()`, y el
patrón productor-consumidor. Documentar las race conditions
encontradas y las estrategias de sincronización aplicadas.

## Estructura

```
Practica10/
├── README.md
├── REFLEXION.md
├── BITACORA_IA.md
├── SINCRONIZACION.md              ← documento clave de la entrega
├── logs/                          (creado en runtime)
│   └── accesos.log                (log de la simulacion)
└── src/
    ├── AccesoSolicitado.java      (mensaje inmutable del buffer)
    ├── BufferAccesos.java         (buffer compartido con wait/notify)
    ├── ContadorAccesos.java       (recurso compartido + version SIN sync)
    ├── Torniquete.java            (extends Thread)
    ├── ClienteEnFila.java         (implements Runnable)
    ├── DemoRaceCondition.java     (demuestra race empiricamente)
    ├── Main.java                  (simulacion completa)
    └── ConcurrenciaTest.java      (9 pruebas de invariantes)
```

## Compilación y ejecución

```bash
mkdir -p logs
javac -d out src/*.java

# Simulacion completa (25 clientes, 3 torniquetes, buffer cap 5)
java -cp out Main

# Demo de race condition (con vs sin synchronized)
java -cp out DemoRaceCondition

# 9 pruebas de invariantes concurrentes
java -cp out ConcurrenciaTest
```

## Elemento de Decisión Propia — Dominio elegido

**Simulación de torniquetes del gimnasio.**

### Por qué este dominio es un buen ejemplo de concurrencia

Tres razones concretas:

1. **Hay un recurso compartido obvio**: el buffer de clientes
   esperando ser atendidos, y el contador de accesos del día. Si dos
   torniquetes manipulan el mismo cliente o el mismo contador a la
   vez sin sincronización, el sistema produce datos inconsistentes
   (clientes que "entran dos veces", contador con menos accesos de
   los reales).

2. **Es un caso real, no académico**: cualquier gimnasio con varias
   entradas tiene exactamente este patrón. Las cadenas grandes
   (Smart Fit, Sports World) procesan miles de check-ins en horas
   pico desde múltiples torniquetes contra la misma base de datos.
   Las estructuras y técnicas de mi simulación son las que se usan
   en ese sistema.

3. **Productores y consumidores claros**:
   - **Productores**: clientes que llegan en oleadas (un grupo entra
     al gimnasio al mismo tiempo, llamémoslo "9:00 AM").
   - **Consumidores**: los N torniquetes procesando en paralelo.
   - **Buffer**: cola finita entre ambos. Si llegan más rápido de lo
     que los torniquetes procesan, los clientes esperan en fila.
     Si los torniquetes están vacíos y nadie llega, esperan.

### Qué problema concreto resuelve la sincronización

El problema observable que la sincronización resuelve es **pérdida
de accesos**. Sin sincronización:

- Dos torniquetes leen el contador `total = 100`, ambos lo
  incrementan a 101 y escriben. Aunque ambos atendieron a un cliente
  cada uno, el contador queda en 101 en lugar de 102. Un cliente
  "desaparece" de las estadísticas.

- Dos torniquetes ven la cola con `[cliente A]`, ambos llaman
  `poll()`, ambos creen haber recibido al cliente A. El cliente A
  pasa dos veces (factura doble) o uno de los dos recibe `null` y
  lanza `NullPointerException` cerrando el torniquete.

Con sincronización (`synchronized`, `wait`, `notifyAll`), estas
condiciones son imposibles por construcción. La demo
`DemoRaceCondition` muestra empíricamente que **84.998 incrementos
se pierden sobre 100.000** sin sincronizar, y exactamente 0 con
sincronizar.

## Mapeo entregables → archivos

| Entregable | Implementación |
|---|---|
| Clase que extienda `Thread` | `Torniquete` |
| Clase que implemente `Runnable` | `ClienteEnFila` |
| Sincronización de recurso compartido | `BufferAccesos`, `ContadorAccesos` |
| Productor-Consumidor con buffer | `BufferAccesos` con `wait/notifyAll` |
| `ExecutorService` con pool | `Main` usa `Executors.newFixedThreadPool(8)` |
| `synchronized`, `wait()`, `notify()` | `BufferAccesos` (wait/notifyAll), `ContadorAccesos` (synchronized) |
| Programa principal multi-hilo | `Main` |
| Logs de ejecución | Consola + archivo `logs/accesos.log` |
| Documento SINCRONIZACION.md | `SINCRONIZACION.md` (raíz de la práctica) |

## Por qué `Torniquete extends Thread` y `ClienteEnFila implements Runnable`

Las dos formas son legales en Java. Mi criterio:

- **`Torniquete` es una entidad con identidad**: tiene id, nombre,
  estadísticas (`procesados`), un ciclo de vida largo. Modelarlo
  como una clase que ES un hilo se siente natural.

- **`ClienteEnFila` es una tarea**: el cliente no es un hilo, es
  algo que un hilo hace. La idea de "ejecutar al cliente" como
  acción la captura mejor `Runnable`. Además permite que el
  `ExecutorService` lo gestione: 25 clientes pueden compartir 8
  hilos del pool, reutilizados.

En código profesional moderno se prefiere `Runnable` (composición
sobre herencia, no se "consume" la herencia única de Java). Aquí
uso las dos formas a propósito porque la consigna pide demostrar
ambas.

## Por qué `ExecutorService` y no crear hilos a mano

El `Main` usa `Executors.newFixedThreadPool(8)` para procesar los
clientes. Las ventajas frente a `new Thread(runnable).start()` x 25:

1. **Reutilización**: 25 clientes se procesan con 8 hilos. Sin pool,
   crearíamos 25 hilos, cada uno con su pila de memoria.
2. **Control de carga**: el pool nunca supera 8 hilos
   simultáneamente. Sin pool, 1000 clientes generarían 1000 hilos y
   probablemente tumbarían la JVM.
3. **Cierre limpio**: `pool.shutdown()` + `awaitTermination()` da un
   protocolo controlado para terminar. Sin pool, hay que coordinar
   cada hilo manualmente.

## Logs de la ejecución

Los logs se generan en dos lugares:

**Consola** (verbosos, para ver el comportamiento concurrente):

```
[21:13:36.723][T3] Torniquete-3 proceso Cliente#1 (Ana) [BASICA, espera=135ms]
[21:13:36.732][T2] Torniquete-2 proceso Cliente#3 (Carolina) [BASICA, espera=85ms]
[21:13:36.781][T1] Torniquete-1 proceso Cliente#2 (Bruno) [BASICA, espera=174ms]
[Torniquete-1][buffer] DEPOSITO Cliente#5 (Elena) (cola=2/5)
[Torniquete-2][buffer] TOMA Cliente#5 (Elena) (cola=1/5)
...
```

**Archivo** `logs/accesos.log` (concisos, para análisis posterior):

```
[21:13:36.723] Torniquete-3 proceso Cliente#1 (Ana) [BASICA, espera=135ms]
[21:13:36.732] Torniquete-2 proceso Cliente#3 (Carolina) [BASICA, espera=85ms]
...
```

Observaciones que se ven en los logs:

- Los torniquetes alternan procesando, no van en orden.
- Los timestamps muestran solapamiento real (varios eventos en el
  mismo milisegundo).
- Los tiempos de espera crecen cuando llegan ráfagas y bajan cuando
  los torniquetes alcanzan.

## Resultados de las pruebas

```
=== Resumen ===
Pasadas:  9
Falladas: 0
Total:    9
```

9 pruebas validando **invariantes concurrentes**:

1. Contador `synchronized`: 10.000 incrementos llegan completos.
2. Contador SIN sync: pierde incrementos (demostrado empíricamente).
3. Productor-Consumidor: producido = consumido, ningún mensaje
   perdido ni duplicado.
4. Productor-Consumidor: total producido == total consumido en el
   buffer.
5. Buffer nunca supera capacidad máxima (observado mientras corre).
6. Torniquete bloqueado en `wait()` se detiene en <2s al llamar
   `detener()`.
7. Múltiples torniquetes procesan todos los accesos exactamente
   una vez.
8. Suma de procesados por cada torniquete == total clientes enviados.
9. `ExecutorService` ejecuta 50 Runnables en pool de 3 hilos sin
   pérdidas.

## Resultados del Main

Simulación con 25 clientes, 3 torniquetes, buffer capacidad 5:

```
=== Resumen de la simulacion ===
Accesos: total=25 (VIP=5 Premium=7 Basica=13)

Procesado por cada torniquete:
  Torniquete-1: 8 accesos
  Torniquete-2: 9 accesos
  Torniquete-3: 8 accesos

Total procesado por torniquetes: 25
Total en contador:               25

--- Invariantes ---
  [OK  ] Total contador == clientes enviados
  [OK  ] Total procesado torniquetes == total contador
  [OK  ] Buffer vacio al final
  [OK  ] Total producido == total consumido en buffer
```

Distribución equitativa de carga entre torniquetes (8-9-8) sin
necesidad de balanceo explícito — emerge naturalmente porque cada
torniquete toma del buffer cuando está libre.
