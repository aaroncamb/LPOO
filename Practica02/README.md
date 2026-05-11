# Práctica 2 — Clases y Objetos

## Objetivo

Modelar el dominio del semestre con clases Java, ejercitando atributos,
constructores múltiples y métodos de instancia. Construir además una
clase contenedora que gestione una colección de objetos del dominio.

## Estructura

```
Practica02/
├── README.md
├── REFLEXION.md
├── BITACORA_IA.md
└── src/
    ├── Cliente.java          (clase principal del dominio)
    ├── GestorClientes.java   (clase contenedora)
    └── Main.java             (programa principal)
```

## Compilación y ejecución

Desde la carpeta `Practica02/`:

```bash
javac -d out src/*.java
java -cp out Main
```

En IntelliJ basta con abrir `Main.java` y usar **Run**.

## Elemento de Decisión Propia — Justificación

### Dominio elegido: gimnasio

Mantengo el dominio de **gimnasio** que iniciamos en la Práctica 1
(calculadora de IMC), y que voy a conservar todo el semestre. Las razones:

1. **Coherencia con el proyecto final.** La Práctica 12 pide construir
   GymPOS, un sistema de punto de venta para un gimnasio. Si modelo desde
   ahora `Cliente`, `Membresia`, `Pago`, etc., al llegar a la P12 ya tengo
   una mini-biblioteca de clases lista para integrar en lugar de empezar
   desde cero.

2. **Suficiente complejidad para cada tema.** El gimnasio tiene varios
   tipos de servicios (membresías, clases grupales, entrenamientos), lo
   cual da material natural para herencia (P4), polimorfismo (P5) e
   interfaces (P6) sin sentirse forzado.

3. **No es el ejemplo prohibido.** La consigna indica explícitamente "no
   uses estudiante/universidad". Gimnasio cumple.

### Decisiones de diseño dentro de la P2

**Tres constructores con propósitos distintos.**

- *Vacío*: para llenar el objeto poco a poco, por ejemplo desde un
  formulario donde los campos llegan secuencialmente.
- *Mínimo (id, nombre, email)*: el caso del alta rápida en mostrador. La
  fecha de registro se asigna automáticamente al día actual y el peso
  queda en 0 hasta que el cliente sube a la báscula. Este es el
  constructor que probablemente se usaría más en producción.
- *Completo*: para reconstruir un cliente desde un archivo o una base de
  datos, donde sí conocemos todos los campos.

**Detección de IDs duplicados en `GestorClientes.agregar()`.**

La consigna pide "métodos para agregar, buscar y mostrar" sin mayor
detalle. Decidí que `agregar` rechace IDs duplicados devolviendo `false`,
porque permitir duplicados rompería todo lo que viene después
(`buscarPorId` ambiguo, `equals` inconsistente, problemas de integridad
en P8 cuando use `HashMap`). En P3 esto se va a robustecer convirtiéndolo
en una excepción específica.

**`buscarPorId` regresa `Optional<Cliente>` en lugar de `null`.**

Aunque todavía no he visto excepciones en clase, evito retornar `null`
porque obliga al llamador a recordar comprobar `== null` antes de cada
uso. `Optional` lo hace explícito en la firma del método. Esto está
documentado en mi bitácora porque fue una de las decisiones que
discutí con la IA.

**Igualdad por id, no por referencia.**

Sobrescribí `equals` y `hashCode` para que dos `Cliente` se consideren
iguales si comparten el id. El id es la clave de negocio, no la posición
en memoria. Esta decisión es importante porque permite usar `Cliente`
correctamente como clave de un `HashMap` o elemento de un `HashSet` en
prácticas posteriores.

## Observación

Los atributos quedaron con visibilidad por defecto (package-private), no
`private`. Es intencional: el tema de modificadores de acceso y validación
pertenece a la P3, donde se hará el refactor a `private` con getters,
setters y reglas de validación. Lo dejo así para que el progreso entre
prácticas sea visible y honesto.
