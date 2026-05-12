# Reflexión — Práctica 4

## 1. ¿Qué ventaja concreta te dio la herencia en este ejercicio? ¿Qué código evitaste repetir?

La ventaja más clara la veo en `Estandar`: Básica y Premium comparten un
modelo de negocio idéntico en lo estructural (cuota mensual fija, renovación
cada 30 días). Sin herencia, las dos clases tendrían que repetir:

- El atributo `precioMensual`.
- El cálculo de `fechaFin = fechaInicio + 30 días` en el constructor.
- La implementación de `renovar()` que suma 30 días.
- El getter `getPrecioMensual()`.

Eso son varias líneas duplicadas en cada clase, y peor que la duplicación
es que si mañana el gimnasio decide cambiar la duración de "un mes" a 31
días en lugar de 30 (porque alguien se queja de que febrero le sale más
barato), tendría que cambiar dos lugares y rezar por no olvidar uno.

Al subir esa lógica común a `Estandar`, Básica y Premium se reducen casi a
nada: solo declaran su precio base y sobrescriben los métodos donde el
comportamiento sí difiere (descuento, beneficios). El cambio de "30 a 31
días" se hace en un solo lugar.

El otro beneficio, menos obvio pero más potente, es que el `GestorMembresias`
puede tratar a cualquier subtipo por la interfaz común. La línea:

```java
for (Membresia m : membresias) {
    total += m.calcularPrecio();
}
```

funciona igual sin importar si hay 5 Básicas, 3 Premiums y 2 VIPs, o 100
de un solo tipo. El gestor no necesita un `if/else` gigante por tipo. Eso es
posible solo porque las tres concretas comparten un ancestro común. Sin
herencia tendría que usar tres listas separadas o `Object` con casts.

## 2. ¿Cuándo es apropiado usar `super()` y cuándo no es necesario?

`super()` es apropiado y a veces obligatorio en dos casos:

**En el constructor de la subclase, cuando el padre tiene constructor con
parámetros y no tiene constructor sin parámetros.** Esto es lo que pasa en
toda mi jerarquía: `Membresia` solo tiene constructor con parámetros (no
hay constructor vacío), entonces `Estandar` está obligada a llamar
explícitamente `super(titular, fechaInicio)` en su constructor. Si no lo
hiciera, el compilador me marca error. Lo mismo aplica entre `Estandar` y
sus hijas: `MembresiaBasica` debe llamar `super(titular, fechaInicio,
PRECIO_BASE)` para inicializar el `precioMensual` del padre intermedio.

**En métodos sobrescritos donde quiero extender, no reemplazar, el
comportamiento del padre.** Yo no lo uso en esta práctica, pero un caso
típico sería sobrescribir `toString()` y querer empezar por la versión del
padre:

```java
@Override
public String toString() {
    return super.toString() + " [extra: ...]";
}
```

Eso evita reescribir desde cero lo que el padre ya hacía bien.

`super()` **no es necesario** cuando:

- El padre tiene un constructor sin parámetros y el compilador lo llama
  automáticamente al inicio del constructor de la hija. No es obligatorio
  escribir `super()` explícitamente en este caso.
- El método sobrescrito reemplaza completamente al del padre y no hay nada
  del padre que rescatar. Ahí solo se escribe la nueva lógica.

En general, en mi jerarquía `super()` aparece en todos los constructores de
subclases porque diseñé los padres exigiendo parámetros (titular y fecha
son obligatorios). Y NO aparece en los métodos sobrescritos porque cada
hija reemplaza completamente el cálculo (no extiende al padre, lo redefine).

## 3. ¿Qué pasa si una clase hija no sobrescribe un método de la clase padre? ¿Cuál versión se ejecuta?

Si el método no es abstracto en el padre, la hija hereda la implementación
del padre tal cual. Cuando llame al método sobre una instancia de la hija,
se ejecuta la versión del padre.

Lo confirmé con mi propia jerarquía. `MembresiaBasica` no sobrescribe el
método `renovar()`, lo recibe heredado de `Estandar`. Si hago:

```java
MembresiaBasica b = new MembresiaBasica("Ana", LocalDate.now());
b.renovar();   // ejecuta Estandar.renovar() (suma 30 dias)
```

la JVM busca el método `renovar()` empezando por la clase concreta del
objeto. Como `MembresiaBasica` no lo tiene, sube a su padre `Estandar` y
ahí lo encuentra. Esto se llama "búsqueda dinámica de métodos" o
"despacho dinámico".

Lo mismo le pasa a `MembresiaPremium`: usa el `renovar()` de `Estandar`.
Pero `MembresiaVIP` sí sobrescribe `renovar()` (suma 365 en lugar de 30),
así que cuando llamo `vip.renovar()`, ejecuta la versión de VIP, no la de
`Membresia` (que es abstracta y ni siquiera tiene cuerpo).

El caso interesante es cuando el método **es abstracto** en el padre, como
mis `calcularPrecio()` o `beneficiosIncluidos()`. Si una clase concreta no
los sobrescribe, **no compila**. El compilador exige que toda clase
concreta tenga implementación para los métodos abstractos heredados, ya
sea propia o de una clase intermedia.

Eso me obligó a darle a VIP su propio `renovar()` (porque no hereda de
Estandar y entonces hereda el `renovar()` abstracto de `Membresia`). En
cambio Básica y Premium se libran porque Estandar les da `renovar()` ya
implementado. Esa sutileza me cambió el orden en que escribí las clases:
primero definí qué métodos eran abstractos en el padre, después decidí
cuáles podía bajar a la intermedia, y solo entonces las concretas tuvieron
exactamente lo justo que faltaba.
