# Práctica 5 — Polimorfismo

## Objetivo

Construir una interfaz y una jerarquía abstracta de servicios del
gimnasio, donde tres clases concretas implementan la misma API pero con
comportamiento distinto. Demostrar polimorfismo via colecciones,
sobrecarga de métodos, casting con `instanceof`, y aplicar el patrón
Template Method como Elemento de Decisión Propia.

## Estructura

```
Practica05/
├── README.md
├── REFLEXION.md
├── BITACORA_IA.md
└── src/
    ├── Cobrable.java               (interfaz con 4 metodos)
    ├── Servicio.java               (clase abstracta, raiz)
    ├── ResultadoVenta.java         (helper inmutable)
    ├── ClaseGrupal.java            (concreta 1)
    ├── EntrenamientoPersonal.java  (concreta 2)
    ├── EvaluacionFisica.java       (concreta 3)
    ├── CajaRegistradora.java       (opera sobre List<Cobrable>)
    ├── Main.java                   (demostracion completa)
    └── ServiciosTest.java          (19 pruebas unitarias)
```

## Compilación y ejecución

```bash
javac -d out src/*.java
java -cp out Main             # demostracion
java -cp out ServiciosTest    # 19 pruebas, todas pasan
```

## Decisión de dominio

P5 pide construir una interfaz, una nueva clase abstracta, y al menos 3
clases concretas. **Decidí no reutilizar la jerarquía `Membresia` de P4**
sino abrir una nueva: los **Servicios del gimnasio**.

Razones:

1. La consigna de P5 está formulada como ejercicio independiente: pide
   interfaz + abstracta + 3 concretas + sobrecargas. Forzarlo sobre la
   jerarquía de P4 me obligaría a mezclar conceptos (membresía mensual y
   sesión puntual no tienen las mismas operaciones).
2. **Coherencia con P6 y el proyecto final.** En P6 voy a necesitar 3
   interfaces más (`Notificable`, `Renovable`, `Reportable`); van a caer
   naturalmente sobre `ClaseGrupal`, `EntrenamientoPersonal` y
   `EvaluacionFisica`. En el proyecto final GymPOS, un cliente puede pagar
   una clase suelta sin tener membresía, así que `Servicio` es una pieza
   que voy a reutilizar.
3. La rúbrica premia la coherencia narrativa del dominio. Tener dos
   jerarquías en el mismo dominio (membresías Y servicios) es más rico
   que tener una sola estirada de más.

## La interfaz `Cobrable`

```java
public interface Cobrable {
    double calcularSubtotal();
    double aplicarDescuento(double porcentaje);
    double calcularImpuestos();
    double calcularTotal();
}
```

Modela el contrato mínimo de "algo que se puede cobrar". Cualquier clase
que la implemente promete saber calcular su subtotal, aplicar un
descuento porcentual, calcular sus impuestos y devolver un total.

La versión `aplicarDescuento(double porcentaje)` está en la interfaz
porque es el caso común. Las clases concretas pueden **sobrecargarla**
con versiones que acepten `int`, `boolean` o `String` — y de hecho lo
hacen, como exige la consigna.

## Sobrecargas implementadas

Cada clase concreta tiene **3 firmas distintas** de `aplicarDescuento`:

### ClaseGrupal

| Firma | Significado |
|---|---|
| `aplicarDescuento(double pct)` | Heredada de `Cobrable`: porcentaje 0.0–1.0 |
| `aplicarDescuento(int pesos)` | Descuento absoluto en pesos |
| `aplicarDescuento(String cupon)` | Busca el cupón en tabla local (BIENVENIDA, AMIGO, TEMPORADA) |

### EntrenamientoPersonal

| Firma | Significado |
|---|---|
| `aplicarDescuento(double pct)` | Heredada: porcentaje genérico |
| `aplicarDescuento(boolean valle)` | Si la sesión es entre 6–9 am, aplica 25% |
| `aplicarDescuento(int sesiones)` | Paquete: 2+ → 5%, 4+ → 15%, 10+ → 20% |

### EvaluacionFisica

| Firma | Significado |
|---|---|
| `aplicarDescuento(double pct)` | Heredada: porcentaje genérico |
| `aplicarDescuento(boolean edad)` | Joven (14–17) → 30%, Senior (60+) → 40% |
| `aplicarDescuento(String motivo)` | Solo `"primera"`: 100% si es primera evaluación |

**Punto interesante:** la versión `(double)` técnicamente es sobreescrita
(viene de la interfaz) y las otras dos son sobrecargadas. La distinción
está documentada en el código y en `REFLEXION.md`.

## Elemento de Decisión Propia — Template Method

La clase abstracta `Servicio` define un método **concreto y final**
llamado `procesarVenta()`:

```java
public final ResultadoVenta procesarVenta() {
    if (!validarCliente()) {
        return new ResultadoVenta(false, 0.0, "Cliente no valido...");
    }
    double total = calcularTotal();
    registrarEnBitacora(total);
    String comprobante = emitirComprobante();
    return new ResultadoVenta(true, total, comprobante);
}
```

Este método **orquesta** una secuencia fija de pasos llamando a métodos
abstractos (`validarCliente`, `emitirComprobante`) que cada subclase
implementa según su lógica. Esto es el patrón **Template Method**.

### Por qué lo diseñé así

El proceso de venta tiene una estructura idéntica para todos los servicios:
validar al cliente, calcular el total, registrar la operación, emitir un
comprobante. Lo único que cambia entre tipos de servicio es:

- **Qué significa "cliente válido"** (en `ClaseGrupal` es tener cupo, en
  `EvaluacionFisica` es tener edad suficiente).
- **Cómo se formatea el comprobante** (distintos campos por tipo).

Si dejara que cada hija implementara su propio `procesarVenta()`, el
orden de los pasos podría desincronizarse: una hija podría olvidarse de
registrar la venta, otra podría llamar a `emitirComprobante` antes de
validar, etc. Con Template Method, el padre **garantiza** la secuencia.

### Ventaja sobre delegar a cada hija

1. **Una sola fuente de verdad para el flujo**: si mañana el negocio decide
   que antes de calcular el total hay que verificar inventario, agrego un
   paso en `procesarVenta()` del padre y las tres hijas lo respetan
   automáticamente. Si cada hija tuviera su propio flujo, habría que tocar
   tres archivos (y un cuarto cuando llegue un nuevo tipo).

2. **Imposible romper la secuencia por accidente**: declaré
   `procesarVenta()` como `final` para que ninguna hija pueda
   sobreescribirla. Lo único que pueden hacer las hijas es completar las
   piezas variables.

3. **El padre delega solo lo que es realmente distinto**: el método
   `registrarEnBitacora` es `private` en `Servicio` porque su lógica es
   idéntica para todas las hijas — no tiene sentido permitir que lo
   sobrescriban.

4. **Lectura del código mejor**: alguien que abre `ClaseGrupal.java` ve
   solo `validarCliente` y `emitirComprobante`. El "qué pasa cuando se
   procesa una venta" se lee en un único lugar, en `Servicio.java`.

## Polimorfismo en acción

### Array polimórfico (entregable #7)

```java
Servicio[] servicios = {
    new ClaseGrupal(...),
    new EntrenamientoPersonal(...),
    new EvaluacionFisica(...),
    new ClaseGrupal(...),
    new EntrenamientoPersonal(...),
};

for (Servicio s : servicios) {
    System.out.println(s.tipoServicio() + " total $" + s.calcularTotal());
}
```

Una misma línea (`s.calcularTotal()`) ejecuta lógica distinta según el
tipo concreto. Notable: `EvaluacionFisica` devuelve total **sin IVA**
(está exenta), mientras las otras dos sí cobran el 16%.

### CajaRegistradora con `List<Cobrable>`

`CajaRegistradora` no conoce los tipos concretos. Solo confía en que cada
elemento es `Cobrable`. Las operaciones polimórficas:

- `totalEnCaja()` suma `calcularTotal()` de cada elemento.
- `aplicarDescuentoGlobal(0.05)` aplica el descuento a todos en una línea.
- `cerrarCaja()` llama al Template Method `procesarVenta()` cuando el
  elemento es un `Servicio`, y cae a cobro directo si no.
- `contarPorTipo(Class)` usa `instanceof` via reflexión (`tipo.isInstance(c)`).

## Casting e `instanceof`

En el `Main`, después de recorrer un `Servicio[]` polimórfico, llamo a
operaciones específicas de cada subclase. Para eso uso el **pattern
matching de `instanceof`** introducido en Java 16:

```java
for (Servicio s : servicios) {
    if (s instanceof ClaseGrupal cg) {        // cg ya está casteada
        cg.inscribirAsistente();
    } else if (s instanceof EntrenamientoPersonal ep) {
        System.out.println(ep.getEntrenador());
    } else if (s instanceof EvaluacionFisica ef) {
        System.out.println("edad " + ef.getEdadCliente());
    }
}
```

`instanceof` previene la `ClassCastException` que ocurriría si intentara
castear ciegamente. Hacer `((EntrenamientoPersonal) s).getEntrenador()`
sobre una `ClaseGrupal` truena en runtime; con `instanceof` el cast solo
ocurre cuando es seguro.

## Resultados de las pruebas

```
=== Resumen ===
Pasadas:  19
Falladas: 0
Total:    19
```

19 pruebas distribuidas en:
- 6 cálculos básicos (subtotal, IVA, total, recargo, exención).
- 7 sobrecargas (las 3 firmas de cada clase + cupón inválido).
- 3 Template Method (éxito, aborto por fecha, aborto por edad).
- 3 polimorfismo en caja (suma, conteo, descuento global).
