# Reflexión — Práctica 6

## 1. ¿Cuándo preferirías una clase abstracta sobre una interfaz? ¿Y al revés?

Las dos sirven para definir contratos que otras clases deben cumplir,
pero responden a necesidades distintas.

**Prefiero clase abstracta cuando:**

- **Necesito compartir estado.** Una interfaz no puede tener atributos
  de instancia, solo constantes. Mi `Servicio` tiene `nombreServicio`,
  `clienteNombre`, `fechaServicio`, `precioBase`, `descuentoAplicado`,
  `notas`. Eso solo puede vivir en una clase abstracta. Si solo tuviera
  interfaz, cada implementador tendría que reescribir los campos.

- **Quiero implementación compartida que la mayoría heredará.** Mi
  `calcularSubtotal()`, `calcularTotal()`, `calcularImpuestos()` están
  implementados en `Servicio` y las tres hijas los heredan sin tocar.
  Si solo tuviera la interfaz `Cobrable`, cada subclase tendría que
  copiar la fórmula `subtotal + subtotal * IVA`.

- **Necesito Template Method.** `procesarVenta()` orquesta una secuencia
  llamando a métodos abstractos. La interfaz puede declarar la
  secuencia (vía métodos default), pero no puede declarar **atributos
  intermedios** que esa secuencia podría necesitar.

- **Quiero impedir la instanciación sin sentido.** Marcar la clase
  como `abstract` deja claro: "no es para instanciar directamente,
  solo para extender". El compilador lo refuerza.

**Prefiero interfaz cuando:**

- **Quiero permitir que clases ya creadas adopten el contrato.** Una
  clase Java solo puede extender UNA clase (no hay herencia múltiple).
  Pero puede implementar MUCHAS interfaces. Mis `ClaseGrupal`,
  `EntrenamientoPersonal` y `EvaluacionFisica` ya extienden `Servicio`;
  no podrían extender una segunda clase abstracta `Notificable`. Por
  eso `Notificable`, `Reportable` y `Reagendable` son interfaces:
  cada clase puede tomar las que necesite, sin perder su herencia.

- **El contrato es ortogonal al tipo.** "Ser notificable" no es lo
  mismo que "ser servicio". Una membresía podría ser notificable también,
  sin tener nada que ver con mi jerarquía `Servicio`. Las interfaces son
  ideales para responsabilidades cruzadas que pueden aparecer en clases
  no relacionadas.

- **Quiero combinaciones flexibles.** En P6 hago tres combinaciones
  distintas con tres interfaces, exactamente lo que las interfaces
  permiten. Si fueran clases abstractas, no podría combinarlas: cada
  clase solo elegiría una.

**Regla práctica que me quedó:** clase abstracta cuando comparten **lo que
son** y necesitan estado común; interfaz cuando comparten **lo que pueden
hacer** y la habilidad es ortogonal a su identidad. `Servicio` es lo que
**son**, `Notificable` es lo que **pueden hacer**. Por eso uno es clase
abstracta y los otros son interfaces.

## 2. ¿Una clase puede implementar varias interfaces? ¿Por qué Java permite eso pero no herencia múltiple de clases?

Sí, una clase Java puede implementar tantas interfaces como necesite. Mi
`EvaluacionFisica` lo demuestra:

```java
public class EvaluacionFisica extends Servicio
        implements Notificable, Reportable, Reagendable { ... }
```

Una sola superclase (`Servicio`), tres interfaces. Compila y funciona
sin problema.

Java **no permite herencia múltiple de clases** por una razón llamada
**el problema del diamante**. Imagina dos clases padre `A` y `B` que
ambas implementen un método `m()`. Si una clase `C` pudiera heredar de
las dos, ¿cuál `m()` ejecuta cuando alguien llame `c.m()`? La ambigüedad
no tiene respuesta razonable, y los lenguajes que la permiten (como C++)
tienen mecanismos complicados para resolverla.

Las interfaces **no caen en el problema del diamante** porque
históricamente solo declaraban firmas, sin implementación. Si dos
interfaces declaran `void m()`, ambas piden lo mismo: que la clase
proporcione **una** implementación. Como solo hay una implementación
(la de la clase concreta), no hay ambigüedad.

A partir de Java 8 las interfaces sí pueden tener métodos `default` (con
implementación). Pero el problema del diamante sigue sin existir porque
si dos interfaces tienen un default con el mismo nombre, el compilador
**obliga a la clase a sobrescribir y elegir** explícitamente cuál usar.
La ambigüedad se resuelve forzando a quien la causa a decidir.

En mi código no llegué a este caso, pero podría pasar: si tanto
`Notificable` como `Reportable` tuvieran un `default String identificar()`,
mi `ClaseGrupal` no compilaría hasta que yo implementara `identificar()`
explícitamente.

## 3. Si agregas un método nuevo a una de tus interfaces, ¿qué clases se ven afectadas? ¿Cómo lo resolverías con un método `default`?

Si agrego un método **abstracto** (sin cuerpo) a una interfaz, **todas
las clases que la implementan dejan de compilar**. El compilador dice
"esta clase no implementa todos los métodos de la interfaz".

Ejemplo: si mañana agrego a `Notificable`:

```java
interface Notificable {
    ...
    boolean enviarPushNotification(String mensaje);   // NUEVO, abstracto
}
```

Las tres clases que lo implementan (`ClaseGrupal`, `EntrenamientoPersonal`,
`EvaluacionFisica`) se rompen al compilar. Hay que ir clase por clase y
agregar el método. Si el proyecto es grande y hay decenas de
implementadores, esto es un dolor enorme: tocas 50 archivos para agregar
un método "nuevo".

**Cómo lo resuelvo con `default`:**

Le doy al método una **implementación por defecto** dentro de la interfaz:

```java
interface Notificable {
    ...
    default boolean enviarPushNotification(String mensaje) {
        // Implementacion provisional: caer a SMS si no se sobrescribe.
        return enviarSMS(mensaje);
    }
}
```

Ahora las tres clases que ya implementaban `Notificable` **siguen
compilando sin tocarles una línea**. Heredan el `default` automáticamente
y se comportan razonablemente (en este caso, mandando SMS). Las clases
que **quieran** un comportamiento específico de push notification pueden
sobrescribir el método; las que no, siguen funcionando como antes.

Esta es exactamente la razón por la que existen los métodos `default`,
agregados en Java 8: permiten **evolucionar interfaces sin romper código
existente**. Antes de Java 8, agregar un método a una interfaz era una
ruptura mayor. Después de Java 8, es una operación segura siempre que
proveas un default razonable.

En mi código uso esto a propósito:

- `Notificable.notificarMultiplesCanales()` es `default` y compone
  `enviarEmail` + `enviarSMS`. Si mañana agrego una cuarta clase que
  implemente `Notificable`, no tengo que reescribir `notificarMultiplesCanales`:
  la hereda del default y funciona inmediatamente.

- `Reportable.toCsvLine()` es `default`. Si cambio el orden de columnas
  del CSV, lo cambio en un solo lugar y las cuatro o cinco clases que
  implementan `Reportable` reportan en el nuevo formato sin tocarlas.

- `Reagendable.fechaRespetaAnticipacion()` es `default`. La validación
  vive en la interfaz misma; las implementaciones la usan dentro de
  `reagendar()` para no duplicar el cálculo.

Los `default` no son la solución para todo: si el nuevo método **no
tiene** una implementación razonable común (por ejemplo, depende
fuertemente del tipo concreto), entonces hay que dejarlo abstracto y
asumir el costo de tocar a los implementadores. Pero cuando sí hay un
default razonable, son una herramienta valiosa para evolucionar interfaces
con bajo costo.
