# Reflexión — Práctica 7

## 1. ¿Cuál es la diferencia entre una excepción chequeada (checked) y una no chequeada (unchecked)?

La diferencia técnica es simple: las **checked** heredan directamente de
`Exception` (pero NO de `RuntimeException`); las **unchecked** heredan
de `RuntimeException` (que a su vez hereda de `Exception`).

Lo que cambia, y es lo importante, es **cómo te trata el compilador**:

- Si llamas a un método que lanza una excepción **checked**, el
  compilador te **obliga** a manejarla: o la atrapas con `try/catch`, o
  declaras en tu firma `throws ...` propagándola. Si no haces ni una ni
  la otra, no compila. Esto fuerza al programador a pensar en el error
  desde la firma misma.

- Si llamas a un método que lanza una **unchecked**, el compilador no
  dice nada. Puedes ignorarla por completo y el código compila. Si la
  excepción se dispara en runtime y nadie la atrapa, sube por la pila
  hasta terminar el thread (y posiblemente el programa).

**Cuándo usar cada una.** La regla práctica que apliqué en mi código:

- **Checked para errores del mundo externo que el sistema puede y debe
  reaccionar.** Un pago rechazado por el banco, una clase llena, una
  membresía vencida. Son errores legítimos del negocio: el sistema debe
  mostrar un mensaje al usuario, sugerir alternativas, registrar el
  incidente. Forzar a manejarlas es lo correcto. Mis 3 excepciones del
  negocio heredan de `GymException` que es checked.

- **Unchecked para bugs del programador.** Un monto negativo en un
  pago, un cliente con nombre vacío, una fecha imposible. Estos errores
  no se "manejan" en runtime — significan que el código que pasó esos
  datos está roto. Forzar `try/catch` por todos lados sería ruido: el
  llamador no puede "recuperarse" de un bug que no debió ocurrir. Mejor
  que el programa truene ruidosamente para que el bug se vea y se
  arregle. Mi `EntradaInvalidaException` es unchecked precisamente por
  esto.

Hay un debate en la industria sobre si las checked son una buena idea
en general (frameworks modernos como Spring y la propia API de
java.util.stream prefieren unchecked). Mi postura es la pragmática: si
es un error que el sistema puede actuar sobre él, checked; si es un
bug, unchecked.

## 2. ¿Por qué creaste una jerarquía de excepciones en lugar de usar `Exception` directamente?

Por cuatro razones, todas con consecuencias prácticas en mi código:

**Catch granular.** Si todo lanzara `Exception`, no podría distinguir
en el catch qué pasó. Tendría que hacer `if (e.getMessage().contains("pago"))`
para decidir cómo reaccionar, lo cual es frágil y propenso a errores
de typo. Con la jerarquía, puedo escribir:

```java
catch (PagoRechazadoException e) { logger.warn("ref " + e.getReferenciaTransaccion()); }
catch (CupoExcedidoException  e) { sugerirOtraClase(); }
catch (MembresiaVencidaException e) { ofrecerRenovacion(e.getDiasDeVencida()); }
```

Cada catch obtiene el tipo correcto sin casts, y el compilador valida
que estoy usando los métodos correctos para cada excepción.

**Información de contexto especializada.** Cada subclase puede llevar
campos que solo tienen sentido para ese tipo de error.
`PagoRechazadoException` lleva monto, método de pago, código del
banco; `CupoExcedidoException` lleva cupo e inscritos;
`MembresiaVencidaException` lleva fecha y días de vencida. Si todas
fueran `Exception`, esos datos tendrían que pasarse en el mensaje
embebidos como texto, lo cual rompe el principio de no parsear
mensajes humanos.

**Catch jerárquico cuando conviene.** A veces sí quiero manejar
cualquier error del gimnasio de la misma forma (loguearlo y devolver
"hubo un problema"). Con la jerarquía esto es trivial:

```java
catch (GymException e) {
    logger.error(e.categoria() + ": " + e.getMessage());
    return false;
}
```

Un solo catch atrapa cualquier subtipo. Si fueran clases independientes
sin padre común, tendría que repetir tres catch iguales.

**Documentación viva en la firma.** Cuando una función declara
`throws PagoRechazadoException`, el lector entiende inmediatamente qué
puede salir mal. Si fuera `throws Exception`, no sabría si el problema
es de red, de validación, de pago, de cualquier otra cosa.

## 3. ¿Qué ventaja tiene `try-with-resources` sobre un bloque `finally` tradicional?

Cuatro ventajas concretas:

**1. Cerrar recursos no se puede olvidar.** Con `try-with-resources`,
declaras el recurso en el paréntesis del `try` y Java garantiza
`close()` automático. Con `finally` manual, dependes de que el
programador escriba el `if (recurso != null) recurso.close()` correctamente,
y de que lo haga en todos los métodos. Estadísticamente, alguien se va
a olvidar.

**2. Manejo correcto de excepciones en `close()`.** Con `finally`, si
tanto el cuerpo del try como el `close()` lanzan excepción, la
excepción del `close()` "tapa" a la del cuerpo. La del cuerpo se pierde
sin traza. Con `try-with-resources`, Java guarda automáticamente la
segunda excepción como **suppressed exception** dentro de la primera,
accesible vía `getSuppressed()`. No se pierde información.

**3. Menos código y más legible.** Comparemos para escribir una línea
a un archivo:

```java
// con try-with-resources (lo que uso en GymLogger)
try (BufferedWriter w = new BufferedWriter(new FileWriter(path, true))) {
    w.write(linea);
} catch (IOException e) {
    System.err.println("Fallo: " + e.getMessage());
}

// con finally tradicional
BufferedWriter w = null;
try {
    w = new BufferedWriter(new FileWriter(path, true));
    w.write(linea);
} catch (IOException e) {
    System.err.println("Fallo: " + e.getMessage());
} finally {
    if (w != null) {
        try {
            w.close();
        } catch (IOException ignored) {
            // y ahora que? si lo logueo recursivamente puedo
            // generar el mismo error
        }
    }
}
```

12 líneas de boilerplate ruidoso vs 5 líneas claras. Y la versión
con `finally` tiene una decisión incómoda (qué hacer si `close()`
falla) que la versión moderna evita gracias a las suppressed exceptions.

**4. Funciona con varios recursos en orden.** Si necesito un
`BufferedReader` envolviendo un `FileReader`, puedo declarar varios en
el mismo try y Java cierra en orden inverso:

```java
try (FileReader fr = new FileReader(path);
     BufferedReader br = new BufferedReader(fr)) {
    return br.lines().count();
}
```

Si lo hiciera con `finally`, tendría que anidar try/finally para
manejar el orden correcto del cierre, lo cual es propenso a errores.

La única condición es que el recurso implemente `AutoCloseable` (o
`Closeable`, que extiende a `AutoCloseable`). Todas las clases de I/O
estándar de Java lo implementan desde Java 7, así que esto cubre el
99% de los casos donde uno antes usaba finally.

**Por qué importa en mi código.** Mi `GymLogger.escribir()` se llama
muchas veces durante el programa: en cada operación de negocio,
cada error, cada validación. Si tuviera un solo lugar donde el
`close()` se olvidara o se hiciera mal, ese archivo podría quedar
abierto, perdería buffer al terminar el programa, o bloquearía a otros
procesos que quisieran leerlo. `try-with-resources` me elimina toda
esa categoría de bugs por construcción.
