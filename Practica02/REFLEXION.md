# Reflexión — Práctica 2

## 1. ¿Cuál es la diferencia entre una clase y un objeto? Da un ejemplo con tu propio código.

Una **clase** es un molde, una definición. Describe qué atributos y qué
comportamiento van a tener todos los elementos que se construyan a partir
de ella, pero por sí misma no representa nada concreto. En mi código,
`Cliente` es una clase: describe qué es un cliente del gimnasio, qué datos
guarda y qué cosas puede hacer.

Un **objeto** es una instancia concreta de una clase. Tiene valores
específicos para cada atributo y vive en memoria. En mi `Main.java` creé
cinco objetos distintos a partir de la misma clase `Cliente`:

```java
Cliente c1 = new Cliente();              // un cliente
Cliente c2 = new Cliente(1002, ...);     // otro cliente, distinto
```

Los dos son objetos del tipo `Cliente`, pero `c1` y `c2` son entidades
separadas: si cambio el peso de `c1`, `c2` no se entera. La clase es la
plantilla; los objetos son las cosas que esa plantilla produce.

Una analogía que me ayudó: la clase es el plano arquitectónico de una casa,
los objetos son las casas que se construyen siguiendo ese plano. Todas
comparten la misma forma, pero cada una tiene su dirección, sus muebles
y a su gente viviendo dentro.

## 2. ¿Por qué usaste 3 constructores distintos? ¿Qué problema resuelve cada uno?

Cada constructor sirve a un caso de uso real diferente:

- **Constructor vacío** (`new Cliente()`): lo necesito cuando voy a llenar
  el objeto de forma incremental, por ejemplo desde un formulario donde los
  campos llegan uno por uno mientras el recepcionista teclea. Sin este
  constructor tendría que decidir todos los valores antes de instanciar,
  lo cual no siempre es posible.

- **Constructor mínimo** (`new Cliente(id, nombre, email)`): es el caso de
  alta rápida. En el mostrador, lo único que el gimnasio realmente necesita
  para registrar a un cliente nuevo son esos tres datos. La fecha de
  registro se asigna sola (hoy), y el peso se inicializa en 0 hasta que el
  cliente suba a la báscula. Este constructor evita pedir información que
  todavía no existe.

- **Constructor completo** (`new Cliente(id, nombre, email, fecha, peso)`):
  lo uso cuando reconstruyo un cliente desde un archivo o una base de datos,
  donde sí conozco todos los campos porque ya estaban guardados antes.
  Si solo tuviera el constructor mínimo, no podría restaurar la fecha de
  registro original ni el peso histórico.

Si tuviera un solo constructor con todos los parámetros, los casos donde
no tengo todos los datos me obligarían a pasar valores ficticios
(`null`, `0`), lo cual es feo y propenso a errores.

## 3. ¿Qué pasaría si no tuvieras constructores definidos? ¿Java sigue funcionando? ¿Por qué?

Sí, Java sigue funcionando. Si no escribo ningún constructor en mi clase,
el compilador me da uno gratis: el **constructor por defecto**, que es
público, no recibe parámetros y no hace nada (solo inicializa los atributos
con sus valores por defecto: `0`, `null`, `false`).

O sea que `new Cliente()` funcionaría aunque yo nunca hubiera escrito un
constructor en `Cliente.java`. Lo confirmé escribiendo una clase pequeña
sin constructores y funcionó.

**Pero hay una sutileza importante**: ese regalo solo aplica si **no
defino ningún constructor**. En cuanto escribo aunque sea uno propio (por
ejemplo el de `id, nombre, email`), Java deja de regalarme el de cero
parámetros. Por eso en mi `Cliente` tuve que escribir explícitamente el
constructor vacío, aunque su cuerpo esté en blanco: si no lo pongo,
`new Cliente()` deja de compilar en cuanto agrego cualquier otro
constructor.

Esto tiene sentido porque Java asume que si yo me tomé la molestia de
escribir un constructor con parámetros, probablemente quiero forzar a
que esos datos siempre se proporcionen, y entonces darle vida al
constructor vacío "a escondidas" sería contraintuitivo.
