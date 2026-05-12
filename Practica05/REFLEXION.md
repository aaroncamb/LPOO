# Reflexión — Práctica 5

## 1. ¿Qué diferencia hay entre sobreescritura (override) y sobrecarga (overload)? Da un ejemplo de cada una desde tu código.

**Sobreescritura (override)** ocurre cuando una clase hija proporciona su
propia implementación de un método que ya estaba definido en su clase
padre, manteniendo la **misma firma** (mismo nombre, mismos parámetros,
mismo tipo de retorno). La JVM decide en tiempo de ejecución cuál versión
ejecutar, según el tipo concreto del objeto.

Ejemplo en mi código (`EntrenamientoPersonal.java`):

```java
// Servicio (padre)
@Override
public double calcularSubtotal() {
    return Math.max(0, precioBase - descuentoAplicado);
}

// EntrenamientoPersonal (hija) - SOBREESCRITURA
@Override
public double calcularSubtotal() {
    double base = precioBase;
    if (duracionMinutos > 60) {
        base *= RECARGO_SESION_LARGA;   // recargo del 20%
    }
    return Math.max(0, base - descuentoAplicado);
}
```

Misma firma (`double calcularSubtotal()`), pero la hija agrega un recargo.
Cuando hago `Servicio s = new EntrenamientoPersonal(...)` y llamo
`s.calcularSubtotal()`, la JVM ejecuta la versión de `EntrenamientoPersonal`
porque ese es el tipo real del objeto, aunque la variable esté declarada
como `Servicio`. Esto se llama "despacho dinámico".

**Sobrecarga (overload)** ocurre cuando varias versiones del mismo método
**conviven en la misma clase**, distinguiéndose por tener **firmas
distintas** (diferente número o tipo de parámetros). La JVM elige cuál
ejecutar en **tiempo de compilación**, según los argumentos que se le
pasen en cada llamada.

Ejemplo en `ClaseGrupal.java`:

```java
// Sobrecarga 1: hereda de Cobrable (firma double)
@Override
public double aplicarDescuento(double porcentaje) { ... }

// Sobrecarga 2: misma clase, otra firma (int)
public double aplicarDescuento(int montoFijoPesos) { ... }

// Sobrecarga 3: misma clase, otra firma (String)
public double aplicarDescuento(String codigoCupon) { ... }
```

Las tres se llaman `aplicarDescuento`, pero reciben tipos distintos. Cuando
escribo `claseGrupal.aplicarDescuento(0.10)` el compilador entiende que va
la versión `double`; `aplicarDescuento(50)` resuelve a `int`; y
`aplicarDescuento("BIENVENIDA")` resuelve a `String`. **No hay ambigüedad**
porque las firmas son inequívocamente distintas.

La diferencia clave en una frase: override es **una clase reemplaza un
método de su padre**; overload es **una misma clase tiene varios métodos
con el mismo nombre pero distinta firma**. Override usa polimorfismo,
overload no.

## 2. ¿Por qué usaste `instanceof` antes de hacer un cast? ¿Qué excepción previene?

Usé `instanceof` para asegurarme de que un objeto **realmente es** del tipo
al que voy a castearlo antes de hacer el cast.

En mi `Main`, después de recorrer un `Servicio[]` polimórfico, necesito
llamar métodos específicos de cada subclase (`inscribirAsistente` solo
existe en `ClaseGrupal`, `getEntrenador` solo en `EntrenamientoPersonal`,
etc.). Como esos métodos no están en la clase base `Servicio`, tengo que
acceder al objeto por su tipo concreto, lo cual requiere un cast:

```java
for (Servicio s : servicios) {
    if (s instanceof ClaseGrupal cg) {       // verificacion + cast en una linea
        cg.inscribirAsistente();
    }
}
```

`instanceof` previene **`ClassCastException`**, que es una excepción de
runtime que ocurre cuando intentas castear un objeto al tipo equivocado.
Sin la verificación, si hago:

```java
ClaseGrupal cg = (ClaseGrupal) s;   // cast ciego
```

y resulta que `s` es en realidad una `EvaluacionFisica`, la JVM lanza
`ClassCastException` en esa línea, el programa se interrumpe, y la traza
muestra "EvaluacionFisica cannot be cast to ClaseGrupal".

Con `instanceof`, el cast solo ocurre dentro del bloque `if`, y solo
cuando estamos seguros de que el objeto es del tipo correcto. Si no lo es,
el bloque se salta y no hay cast, así que no hay excepción.

Una sutileza que aprendí: desde Java 16+, la sintaxis `instanceof Tipo
nombreVariable` hace el cast automáticamente y deja la variable lista
para usar. Antes había que escribir dos líneas:

```java
// estilo antiguo
if (s instanceof ClaseGrupal) {
    ClaseGrupal cg = (ClaseGrupal) s;   // cast manual redundante
    cg.inscribirAsistente();
}

// estilo moderno (Java 16+, lo que uso en mi codigo)
if (s instanceof ClaseGrupal cg) {
    cg.inscribirAsistente();
}
```

Lo segundo es más limpio y menos propenso a errores (no hay forma de
escribir mal el cast porque no lo escribes manualmente).

## 3. ¿Podrías instanciar tu clase abstracta directamente? ¿Por qué sí o por qué no?

No, no puedo. Si intento `new Servicio(...)` el compilador me dice:

```
Servicio is abstract; cannot be instantiated
```

Esto es así por dos razones técnicas y una conceptual:

**Técnica 1:** `Servicio` declara métodos abstractos
(`validarCliente`, `emitirComprobante`, `tipoServicio`) que **no tienen
cuerpo**. Si la JVM pudiera crear una instancia de `Servicio`, ¿qué pasaría
si alguien llamara `unServicio.validarCliente()`? No hay código que
ejecutar. Java prefiere prevenir el problema en compilación a permitir
crear objetos que rompan al usarlos.

**Técnica 2:** Aunque no tuviera métodos abstractos, marcar la clase como
`abstract` es una declaración explícita del programador: "esta clase está
incompleta o es genérica, no la instancien". El compilador respeta esa
intención.

**Conceptual:** `Servicio` representa la **idea** de un servicio del
gimnasio, no un servicio concreto. ¿Qué sería "un servicio"? No sabemos
qué incluye, qué cobra, cómo se valida. Solo `ClaseGrupal`,
`EntrenamientoPersonal` o `EvaluacionFisica` son entidades del mundo real;
`Servicio` es una abstracción para hablar de las tres a la vez. Permitir
instanciarla sería permitir que exista en el sistema un "servicio sin
tipo", lo cual no representa nada en el negocio del gimnasio.

Lo que **sí puedo hacer** y de hecho hago todo el tiempo en mi código es
usar `Servicio` como **tipo de variable** o **tipo de parámetro**:

```java
Servicio s = new ClaseGrupal(...);    // OK: variable de tipo abstracto
Servicio[] arr = { ... };              // OK: array de tipo abstracto
public void cobrar(Servicio s) { ... } // OK: parametro de tipo abstracto
```

La variable tiene tipo `Servicio` pero el objeto al que apunta es siempre
de una subclase concreta. Eso es polimorfismo: el cliente del código
trabaja con el tipo abstracto, el comportamiento real lo aporta la
subclase concreta. Esa separación entre "tipo en la firma" y "tipo del
objeto" es exactamente lo que hace útil tener clases abstractas:
ofrecen vocabulario para hablar del grupo sin comprometerse con uno
específico.
