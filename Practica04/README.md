# Práctica 4 — Herencia

## Objetivo

Construir una jerarquía de al menos 3 niveles modelando los tipos de
membresía de un gimnasio, usando herencia para compartir comportamiento
y polimorfismo para tratar a todas las membresías de forma uniforme
desde un gestor.

## Estructura

```
Practica04/
├── README.md
├── REFLEXION.md
├── BITACORA_IA.md
├── UML.puml                       (diagrama PlantUML, editable)
├── jerarquia-membresias.png       (diagrama renderizado, vista rapida)
└── src/
    ├── Membresia.java             (raiz abstracta - nivel 1)
    ├── Estandar.java              (intermedia abstracta - nivel 2)
    ├── MembresiaBasica.java       (concreta - nivel 3)
    ├── MembresiaPremium.java      (concreta - nivel 3)
    ├── MembresiaVIP.java          (concreta directa - nivel 2)
    ├── GestorMembresias.java      (clase contenedora)
    ├── Main.java                  (demostracion)
    └── MembresiaTest.java         (15 pruebas unitarias)
```

## Compilación y ejecución

```bash
javac -d out src/*.java

java -cp out Main             # demostracion completa
java -cp out MembresiaTest    # 15 pruebas unitarias manuales
```

## Jerarquía implementada

```
Membresia (abstracta)
   |
   +-- Estandar (abstracta, intermedia)
   |     |
   |     +-- MembresiaBasica   (concreta)
   |     +-- MembresiaPremium  (concreta)
   |
   +-- MembresiaVIP            (concreta, hereda directo)
```

Profundidad: 3 niveles. Hijas concretas: 3 (Basica, Premium, VIP).

Ver `UML.puml` o `jerarquia-membresias.png` para el diagrama completo
con atributos y métodos.

## Por qué la jerarquía es así

### Estandar es intermedia abstracta

Básica y Premium comparten un modelo de negocio idéntico en lo estructural:
**cuota mensual fija, renovación cada 30 días, contrato continuo mes a mes**.
Lo único que cambia entre ellas es el monto y los beneficios. Tiene sentido
agrupar lo común en `Estandar`:

- El atributo `precioMensual`.
- La implementación de `renovar()` que suma 30 días.
- El cálculo de `fechaFin = fechaInicio + 30 días` en el constructor.

Si pusiera todo eso en `Membresia` directamente, VIP heredaría conceptos
que no le aplican (su modelo es anual). Si lo pusiera duplicado en Básica
y Premium, repetiría código y abriría la puerta a que se desincronizaran.

### VIP hereda directo de Membresia, NO de Estandar

Esta es la decisión más importante de diseño en esta práctica, y la documento
arriba en `MembresiaVIP.java`:

> Estandar modela un contrato mensual con precio mensual fijo. VIP
> funciona con un esquema completamente distinto: cuota anual, renovación
> de 365 días, no se cobra mensual. Forzar a VIP a heredar de Estandar
> implicaría inventar un `precioMensual` ficticio (cuota anual ÷ 12, que
> nadie firma como contrato) o sobrescribir `renovar()` ignorando la
> implementación del padre. Ambas opciones son humo de herencia mal
> aplicada.

La regla práctica que apliqué: **una hija debe poder usar lo que su madre
ofrece sin tener que rechazarlo**. Cuando una subclase tiene que sobrescribir
casi todo lo que hereda, la herencia probablemente está mal.

## Métodos sobrescritos por clase hija

La consigna pide al menos 3 métodos sobrescritos por hija. Cada clase
concreta sobrescribe **5 métodos abstractos** declarados en `Membresia`:

| Método | Basica | Premium | VIP |
|---|---|---|---|
| `calcularPrecio()` | $350 plano | $650, -5% si vigente | $14,400 anual, -10% si vigente |
| `beneficiosIncluidos()` | "Solo pesas y cardio" | "+ clases grupales, nutriólogo" | "+ entrenador, spa, parking" |
| `renovar()` | (heredado de Estandar, +30 días) | (heredado de Estandar, +30 días) | sobrescrito: +365 días |
| `descuentoRenovacion()` | 0% | 5% | 10% |
| `tipoLegible()` | "Membresia Basica" | "Membresia Premium" | "Membresia VIP" |

Más operaciones específicas no heredadas (también pruebas demostraron el polimorfismo):
- `MembresiaPremium.agendarClaseGrupal()`
- `MembresiaVIP.registrarAccesoSpa()`, `isEntrenadorPersonalIncluido()`

## Uso de `super()`

`super()` aparece en cada constructor de las hijas para invocar la
inicialización del padre antes de hacer lo propio:

```java
public MembresiaBasica(String titularNombre, LocalDate fechaInicio) {
    super(titularNombre, fechaInicio, PRECIO_BASE);  // llama a Estandar
}

public Estandar(String titular, LocalDate fechaInicio, double precio) {
    super(titular, fechaInicio);   // llama a Membresia
    ...
}
```

Es una cadena: `MembresiaBasica` → `Estandar` → `Membresia`. Cada paso
agrega su parte del estado.

## Elemento de Decisión Propia

La consigna pide identificar una situación donde la herencia me causó (o
podría causar) un problema de diseño, y cómo lo resolví.

**Situación detectada:** al diseñar la jerarquía pensé inicialmente en poner
las tres concretas (Básica, Premium, VIP) bajo una clase intermedia única
llamada `MembresiaConPago`. Eso me daba un árbol simétrico bonito. Pero al
empezar a codificar, noté que VIP tenía un `precioMensual` que en realidad
no existía: VIP cobra cuota anual, no mensual. Para que VIP encajara, iba
a tener que:

1. Almacenar un `precioMensual` ficticio (cuota anual ÷ 12), o
2. Dejar `precioMensual = 0` en VIP y romper el invariante de la clase padre,
   o
3. Sobrescribir `calcularPrecio()` ignorando completamente `precioMensual`.

Cualquiera de las tres es síntoma de herencia mal aplicada (la subclase
tiene que mentir o pelear con el padre para funcionar).

**Cómo lo resolví:** dividí la jerarquía. `Estandar` solo lo extienden las
hijas que realmente comparten el modelo mensual (Básica y Premium). VIP
sube un nivel y hereda directo de `Membresia`, que solo modela lo
verdaderamente compartido (titular, fechas, vigencia, cancelación). El
árbol queda asimétrico pero **honesto**: cada nodo aporta algo real, nadie
hereda lo que no le sirve.

**Aprendizaje:** la elegancia visual de una jerarquía simétrica no debe
imponer datos ficticios. Si una subclase no puede usar lo que hereda sin
rechazarlo, hay que repensar el árbol o usar composición en su lugar.

## Resultados de las pruebas

```
=== Resumen ===
Pasadas:  15
Falladas: 0
Total:    15
```

Cobertura por clase:
- `MembresiaBasica`: precio base, descuento 0, renovación 30 días.
- `MembresiaPremium`: descuento 5% si vigente, precio completo si vencida, agendar clases.
- `MembresiaVIP`: renovación 365 días, cuota anual, registro de spa.
- `GestorMembresias`: ingresos polimórficos, filtrado por tipo, renovación masiva.
- Heredados (`cancelar`, `estaVigente`, validación del padre).
