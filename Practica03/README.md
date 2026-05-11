# Práctica 3 — Encapsulamiento y Modificadores de Acceso

## Objetivo

Refactorizar la clase `Cliente` para que todos sus atributos sean privados,
con getters/setters que apliquen reglas de validación reales. Incorporar una
segunda clase `Membresia` que tenga relación de composición con `Cliente` y
que use `protected` en sus atributos como preparación para la herencia de
la P4.

## Estructura

```
Practica03/
├── README.md
├── REFLEXION.md
├── BITACORA_IA.md
└── src/
    ├── Cliente.java       (refactorizado, todos los atributos private)
    ├── Membresia.java     (segunda clase, atributos protected)
    ├── Main.java          (demostracion + acceso publico/privado)
    └── ClienteTest.java   (16 pruebas unitarias manuales)
```

## Compilación y ejecución

Desde la carpeta `Practica03/`:

```bash
javac -d out src/*.java

# Demostracion completa de validaciones
java -cp out Main

# Pruebas unitarias
java -cp out ClienteTest
```

## Cambios respecto a la Práctica 2

| Aspecto | P2 | P3 |
|---|---|---|
| Visibilidad de atributos | package-private | `private` (Cliente), `protected` (Membresia) |
| Validaciones | ninguna | en cada setter, con `IllegalArgumentException` |
| Atributos | 5 (id, nombre, email, fecha, peso) | 7 (añade `alturaCm` y `membresia`) |
| Clases | 2 (Cliente, GestorClientes) | 2 (Cliente, Membresia) |
| Pruebas | implícitas en Main | 16 pruebas explícitas con conteo de pasadas/falladas |

`GestorClientes` no se incluye en esta práctica porque la consigna pide
"segunda clase con relación de composición y `protected`". `Membresia`
cumple ese rol mejor que un contenedor.

## Reglas de validación implementadas

La consigna pide **al menos dos reglas de validación no triviales**. Implementé
varias; estas son las dos principales por su justificación de negocio:

### Regla 1 — Formato de email

```java
if (!e.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
```

Esta expresión exige:
- Parte local con caracteres válidos (letras, dígitos, `._%+-`)
- Símbolo `@`
- Dominio con al menos un punto seguido de una extensión de 2+ letras

No es una validación RFC-5322 completa (esa requeriría una regex de cientos
de caracteres y rechazaría direcciones válidas raras). Para un POS de gimnasio
es excesivo. Esta versión atrapa los errores comunes de captura: usuarios que
olvidan el `@`, dominio sin TLD, espacios, caracteres extraños.

### Regla 2 — Peso entre 30 y 300 kg

- **Mínimo 30 kg**: por debajo de ese valor el caso es médico (anorexia
  severa, niños) y no debería procesarse como cliente común de un gimnasio
  comercial. El sistema debe rechazar el dato y obligar a revisar la captura.
- **Máximo 300 kg**: por arriba de ese valor casi seguramente es un error de
  captura: peso en libras escrito como si fueran kilos (200 lb ≈ 91 kg, pero
  alguien podría teclear 200 sin convertir), dedo resbalado en el cero (700
  en lugar de 70), etc.
- **Excepción: 0.0 se acepta** como caso especial cuando el cliente todavía
  no se ha pesado en la báscula del gimnasio. Sin este caso especial, el
  constructor mínimo no podría inicializar el campo.

Validaciones adicionales que también implementé (todas en setters):

- **Altura entre 120 y 230 cm** — mismo razonamiento.
- **Nombre completo de al menos 2 caracteres no en blanco** — el espíritu es
  rechazar la captura accidental de un solo carácter o todo espacios.
- **Fecha de registro no puede ser futura** — un cliente no puede estar
  registrado "mañana" en un sistema operativo.
- **id estrictamente positivo** — 0 y negativos no son ids válidos.
- **Tipo de membresía debe ser uno de los tres definidos** — previene typos.
- **Precio mensual no negativo**, **fecha fin posterior a inicio**, etc.

## Acceso público vs privado — demostración

En `Main.java` está documentado el caso clave:

```java
// En P2 esta linea funcionaba:
//     ana.pesoKg = -500;
//
// En P3 NO COMPILA porque pesoKg es private.
// El compilador dice: "pesoKg has private access in Cliente"
//
// La unica forma de modificar el peso desde fuera de Cliente es el setter:
ana.setPesoKg(-500);  // <-- lanza IllegalArgumentException, no acepta el dato
```

Esto es el corazón del encapsulamiento: el atributo está protegido del
exterior, y el setter actúa como guardián. La clase ya no puede entrar en un
estado inválido por accidente.

## Elemento de Decisión Propia

Más allá de las dos reglas requeridas, mi decisión propia fue **incluir un
caso especial explícito de "valor 0" para peso y altura**, en lugar de tratar
0 como un valor inválido o aceptar cualquier no-positivo.

Razonamiento: en la práctica real del gimnasio, un cliente se da de alta en
mostrador antes de subir a la báscula. Si fuerzo a poner un peso "válido"
desde el primer momento, el recepcionista va a inventar un número (poniendo
70 por defecto en todos), lo cual contamina el sistema con datos falsos peor
que la ausencia del dato. Aceptar `0.0` como "aún no medido" hace explícito
ese estado y permite que `calcularIMC()` devuelva un sentinel especial cuando
faltan datos.

Esta decisión apareció discutiéndolo con la IA durante el diseño, está
documentada en `BITACORA_IA.md`.

## Resultados de las pruebas

```
=== Resumen ===
Pruebas pasadas:  16
Pruebas falladas: 0
Total:            16
```

Cobertura: getters/setters, todas las reglas de validación de Cliente y
Membresia, composición, casos límite (peso 0), y comportamiento de la
renovación y vigencia.
