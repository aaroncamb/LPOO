# Práctica 7 — Manejo de Excepciones

## Objetivo

Diseñar una jerarquía de excepciones personalizadas para el dominio del
gimnasio, usarla en una clase de negocio que lance y maneje cada tipo,
implementar logging con `try-with-resources`, y construir una excepción
rica con información de contexto suficiente para diagnosticar
incidentes en producción.

## Estructura

```
Practica07/
├── README.md
├── REFLEXION.md
├── BITACORA_IA.md
├── logs/                              (generado al ejecutar)
│   ├── gym.log                        (log de Main)
│   └── gym-test.log                   (log de pruebas)
└── src/
    ├── GymException.java              (raiz abstracta, CHECKED)
    ├── PagoRechazadoException.java    (CHECKED, con contexto rico)
    ├── CupoExcedidoException.java     (CHECKED)
    ├── MembresiaVencidaException.java (CHECKED)
    ├── EntradaInvalidaException.java  (UNCHECKED, fuera de la jerarquia)
    ├── GymLogger.java                 (try-with-resources)
    ├── SistemaGimnasio.java           (clase de negocio)
    ├── Main.java                      (demostracion)
    └── ExcepcionesTest.java           (18 pruebas)
```

## Compilación y ejecución

```bash
mkdir -p logs
javac -d out src/*.java

java -cp out Main                # demostracion completa
java -cp out ExcepcionesTest     # 18 pruebas, todas pasan
```

El archivo `logs/gym.log` se genera y crece con cada ejecución. Para
limpiarlo: `rm logs/gym.log`.

## Jerarquía de excepciones

```
java.lang.Exception
    └── GymException (abstracta)       ← CHECKED, padre comun
          ├── PagoRechazadoException   ← con contexto rico
          ├── CupoExcedidoException
          └── MembresiaVencidaException

java.lang.RuntimeException
    └── EntradaInvalidaException        ← UNCHECKED, fuera de la jerarquia
```

### Por qué la mezcla checked + unchecked

Las 3 excepciones de negocio son **checked** (heredan de `Exception`):
representan errores del mundo externo que el sistema debe poder manejar
gracilmente (un pago rechazado, una clase llena, una membresía vencida).
El compilador obliga a `try/catch` o `throws`, lo cual es deseable en
flujos críticos.

`EntradaInvalidaException` es **unchecked** (hereda de `RuntimeException`)
porque representa un **bug del programador**: alguien pasó un monto
negativo, un nombre nulo, una fecha imposible. Estos errores no son
"manejables" en runtime — lo único que se puede hacer es fallar
ruidosamente para que se note y se corrija el código que generó la
basura. Obligar a `try/catch` en todos lados que se llamen sería ruido
sin valor.

Esta distinción se desarrolla en `REFLEXION.md` pregunta 1.

### Por qué `GymException` es abstracta

Para que nadie pueda lanzar un "error genérico de gimnasio" sin
precisar. Lanzar `throw new GymException("algo malo")` daría logs
pobres y catch poco discriminados. Marcarla abstracta fuerza a usar
siempre la subclase específica. El compilador es nuestro aliado.

## Elemento de Decisión Propia — Contexto rico en PagoRechazadoException

La consigna pide al menos una excepción con información adicional más
allá del mensaje. `PagoRechazadoException` lleva **cinco campos de
contexto**:

| Campo | Tipo | Ejemplo | Para qué |
|---|---|---|---|
| `montoIntentado` | `double` | `350.0` | Saber cuánto se quiso cobrar sin tener que recuperar el ticket |
| `metodoPago` | `String` | `"tarjeta"` | Distinguir si fue tarjeta, efectivo, transferencia |
| `codigoErrorInterno` | `String` | `"INSUF_FUNDS"` | Código machine-readable para filtrar/agrupar incidentes |
| `metodoOrigen` | `String` | `"procesarPago"` | Saber en qué método del sistema se generó |
| `referenciaTransaccion` | `String` | `"PAY-A3F7B2"` | Identificador corto que el cliente puede dar al soporte |
| `timestamp` | `LocalDateTime` | (auto) | Heredado de `GymException` |

Además, su `toString()` produce un **dump JSON-like** listo para pegar
en un ticket de soporte o consumir desde un dashboard:

```json
{
  "tipo": "PAGO_RECHAZADO",
  "timestamp": "2026-05-12T20:16:46.669294375",
  "referencia": "PAY-19E1DD",
  "monto": 350.00,
  "metodo_pago": "tarjeta",
  "codigo": "INSUF_FUNDS",
  "origen": "procesarPago",
  "mensaje": "Fondos insuficientes al cobrar a Ana Perez"
}
```

### Cómo se usaría en un sistema real

1. **Diagnóstico inmediato:** el cliente llama a soporte y dice
   "no pude pagar". El agente le pide la referencia (PAY-19E1DD). El
   sistema busca esa referencia en los logs y ve INSTANTÁNEAMENTE el
   monto, método, código del banco y método del sistema involucrado.
   No hay que pedirle al cliente que recuerde datos.

2. **Agrupación de incidentes:** si en un día se generan 200
   `PagoRechazadoException`, el equipo de operaciones agrupa por
   `codigoErrorInterno` y descubre que el 80% son `TIMEOUT_GATEWAY`,
   lo cual apunta a un problema con el proveedor de pagos, no con
   clientes. Sin el código separado del mensaje, esto se vería como
   "200 fallas variadas" y nadie investigaría.

3. **Métricas y alertas:** se puede configurar una alerta tipo
   "si en 5 minutos hay >10 errores con `codigoErrorInterno = INSUF_FUNDS`,
   abrir caso de cobranza". Tener los campos estructurados, no
   embebidos en mensajes de texto, hace eso trivial.

4. **Auditoría:** `metodoOrigen` permite responder "¿qué versión del
   código generó este error?" cuando uno refactoriza. Si `procesarPago`
   se renombra a `cobrar`, los errores nuevos ya no llevan el nombre
   viejo, así sabemos cuándo cambió.

`CupoExcedidoException` y `MembresiaVencidaException` también llevan
contexto adicional (cupo actual/máximo y días de vencimiento), aunque
más modesto. La idea es que ningún catch tenga que "deducir" o
"reconsultar" datos que ya tenía la excepción.

## Try-with-resources

En `GymLogger.escribir()`:

```java
try (BufferedWriter writer = new BufferedWriter(
        new FileWriter(archivoLog, true))) {
    writer.write(linea);
} catch (IOException e) {
    System.err.println("[GymLogger] No se pudo escribir al log: "
            + e.getMessage());
}
```

`BufferedWriter` implementa `AutoCloseable`, así que Java garantiza
que `close()` se llamará automáticamente al salir del bloque (con éxito
o por excepción), sin necesidad de `finally`. Esto reemplaza al patrón
antiguo:

```java
// patron antiguo, propenso a errores
BufferedWriter w = null;
try {
    w = new BufferedWriter(...);
    w.write(linea);
} catch (IOException e) {
    ...
} finally {
    if (w != null) {
        try { w.close(); } catch (IOException ignored) {}
    }
}
```

Las ventajas se desarrollan en `REFLEXION.md` pregunta 3.

## Manejo en la clase de negocio

`SistemaGimnasio` muestra dos estilos de manejo:

**Estilo 1 — Lanzar al llamador (declarando en la firma):**

```java
public void procesarPago(String cliente, double monto, String metodoPago)
        throws PagoRechazadoException {
    ...
    if (rechazado) {
        throw new PagoRechazadoException(...);
    }
}
```

El llamador decide qué hacer. El método solo informa.

**Estilo 2 — Capturar y reaccionar (flujo compuesto):**

```java
public boolean intentarFlujoCompleto(...) {
    try {
        validarAcceso(cliente);
        procesarPago(cliente, monto, metodoPago);
        inscribirEnClase(cliente, nombreClase);
        return true;
    } catch (MembresiaVencidaException e) {
        logger.warn("Flujo cancelado: membresia vencida...");
        return false;
    } catch (PagoRechazadoException e) {
        logger.warn("Flujo cancelado: pago rechazado, ref " +
                e.getReferenciaTransaccion());
        return false;
    } catch (CupoExcedidoException e) {
        logger.warn("Flujo cancelado: clase llena " + e.getNombreClase());
        return false;
    }
}
```

Aquí captura cada tipo por separado para dar logs específicos. Cada
catch usa contexto distinto de la excepción correspondiente.

## Archivo de log

Se genera en `logs/gym.log`. Formato:

```
2026-05-12T20:16:46 [INFO ] Clase registrada: Yoga matutino (cupo 2)
2026-05-12T20:16:46 [INFO ] Membresia registrada para Ana Perez vence 2026-06-11
2026-05-12T20:16:46 [ERROR] PAGO_RECHAZADO - Pago rechazado: ... | contexto: {...JSON...}
2026-05-12T20:16:46 [ERROR] CUPO_EXCEDIDO - Cupo excedido en 'Yoga matutino': 2/2 inscritos.
2026-05-12T20:16:46 [INFO ] FLUJO COMPLETO OK para Ana Perez
```

Cada línea tiene: timestamp, nivel (`INFO`/`WARN`/`ERROR`), categoría
opcional, mensaje. El `PagoRechazadoException` deja el dump JSON
embebido para facilitar grep y parsing posterior.

## Resultados de las pruebas

```
=== Resumen ===
Pasadas:  18
Falladas: 0
Total:    18
```

18 pruebas distribuidas en:
- 5 sobre la jerarquía (abstracta, herencia, checked vs unchecked, timestamp).
- 5 sobre contexto rico (5 campos, toString JSON, referencia única).
- 4 sobre manejo en `SistemaGimnasio` (lanzar, catch jerárquico, recuperación).
- 2 sobre `try-with-resources` y archivo de log generado.
