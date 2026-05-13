# Reflexión — Práctica 8

## 1. ¿Por qué usaste cada estructura de colección para cada tipo de dato? ¿Qué pasaría si usaras `ArrayList` para todo?

Mis cuatro estructuras y la razón concreta de cada una:

**`ArrayList<Cliente>` para el listado principal.** Necesito iterar en
orden de inserción frecuentemente (cuando muestro la lista, cuando
recorro con stream para filtrar, cuando hago reportes). `ArrayList` es
imbatible para iteración secuencial y acceso por índice. La inserción
al final también es O(1) amortizado, que es como crece mi lista.

**`HashMap<Integer, Cliente>` como índice por id.** La búsqueda por id
es la operación más frecuente en el sistema (cada vez que el
recepcionista teclea un id, cada vez que llega un pago para un
cliente). `HashMap.get()` es O(1) promedio; sin el mapa tendría que
recorrer la lista entera, O(n). Mi benchmark mostró que para 50.000
clientes, esto es **500× más lento**: 165.000 ns vs 325 ns. A pequeña
escala da igual; a escala real, hace la diferencia entre "instantáneo"
y "el sistema se siente lento".

**`HashSet<String>` para emails únicos.** El gimnasio no admite
duplicados de email (sería un cliente que se hace pasar por otro). Sin
el set tendría que recorrer la lista cada vez que alguien intenta
registrarse, preguntando "¿existe ya este email?". Con el set, basta
`if (!emailsRegistrados.add(email))` y la respuesta es O(1). Bonus:
`add()` devuelve `false` si ya existía, así que la inserción y la
validación son la misma operación.

**`LinkedList<Notificacion>` como cola FIFO.** Las notificaciones se
agregan al final (un evento nuevo) y se procesan por el frente (la más
vieja primero). `LinkedList` permite ambas operaciones en O(1). Si
usara `ArrayList`, sacar del frente sería O(n) porque hay que
desplazar todos los elementos restantes; con 1.000 notificaciones
pendientes y procesando una por segundo, eso se nota.

**¿Qué pasaría si usara `ArrayList` para todo?**

El sistema seguiría **funcionando correctamente** — esa es una verdad
incómoda. `ArrayList` es la estructura "buena para todo a pequeña
escala". Lo que cambiaría es la **velocidad asintótica**:

- Buscar por id pasaría de O(1) a O(n). Con 100 clientes ni se nota;
  con 50.000, cada búsqueda dura cientos de microsegundos.
- Validar email único pasaría de O(1) a O(n). Lo mismo.
- Procesar la cola FIFO desde el frente pasaría de O(1) a O(n) por
  cada `remove(0)`. Una cola de 1.000 elementos procesada secuencialmente
  pasaría de 1.000 operaciones O(1) a 1.000 operaciones O(n), es decir,
  O(n²) total. Eso es de los antipatterns clásicos.

La lección: para un proyecto de juguete, `ArrayList` funciona y nadie
se da cuenta. Pero a los pocos miles de registros, las decisiones de
estructura empiezan a importar. Y elegir bien desde el principio cuesta
exactamente lo mismo que elegir mal. Es de los lugares donde "hacer lo
correcto" no tiene desventaja, así que conviene hacerlo de entrada.

## 2. ¿Qué diferencia hay entre `Comparable` y `Comparator`? ¿Cuándo usarías cada uno?

**`Comparable`** define el **orden natural** de una clase. Lo
implementa la propia clase y declara "esta es la forma 'oficial' de
ordenar mis instancias". En mi código, `Cliente` implementa
`Comparable<Cliente>` con `compareTo` por nombre. Eso significa que
cualquier cosa que ordene Clientes "por defecto" (un `TreeSet`,
`Collections.sort(lista)` sin segundo argumento) lo hace por nombre.

**`Comparator`** define una **regla de ordenamiento alternativa**, vive
fuera de la clase, y permite tener varias formas distintas de ordenar
los mismos objetos. En mi código, `POR_ANTIGUEDAD` y
`POR_INSCRIPCION_RECIENTE` son dos `Comparator<Cliente>` que ordenan
de manera distinta al orden natural.

La diferencia clave: una clase tiene **un único `compareTo`**, así que
solo puede tener un orden natural. Pero puede tener **muchos
Comparators**, cada uno definido aparte.

**Cuándo uso cada uno:**

Uso **`Comparable`** cuando hay un orden que es "obviamente el
correcto" para una clase, el que el 80% de los usos esperarían. En
Cliente eso es alfabético por nombre (porque es como aparece en
listados de recepción, reportes, etc.). Si no hay un orden tan
"obvio", la regla cambia: mejor no implementar `Comparable` (la clase
queda sin orden natural y siempre hay que pasar Comparator
explícito). Eso comunica honestamente que no hay un orden privilegiado.

Uso **`Comparator`** para todos los demás criterios. En mi código son
por antigüedad y por inscripción reciente. Si mañana necesito ordenar
por peso, agrego un Comparator más sin tocar Cliente. Esa
extensibilidad sin modificar la clase es la principal ventaja:

```java
public static final Comparator<Cliente> POR_PESO_DESC =
        Comparator.comparingDouble(Cliente::getPesoKg).reversed();
```

Hubo un caso donde dudé: ¿cuál es el "orden natural" de una membresía?
¿Por precio? ¿Por nivel (Básica < Premium < VIP)? No estaba claro, así
que en P4 no implementé Comparable en Membresia y solo expongo
Comparators cuando hace falta. Esa duda fue la pista de que no había
un "orden natural" honesto.

**Truco que aprendí:** los Comparators componen.
`POR_ANTIGUEDAD.reversed()` me da el orden inverso gratis. Y
`Comparator.comparing(X).thenComparing(Y)` permite ordenar por X y
desempatar por Y. Esto vuelve a los Comparators más poderosos que el
`compareTo` manual, que tendría que codificar el orden compuesto a
mano con muchos `if`.

## 3. Explica con tus palabras qué hace una operación Stream. ¿Por qué es más legible que un bucle `for`?

Un Stream es una **secuencia de elementos que viaja por una tubería de
operaciones**. La idea es que en lugar de programar el "cómo"
(declarar un acumulador, iterar con índices, ir agregando manualmente),
declaro el "qué": qué transformaciones quiero, en qué orden, y dejo
que el lenguaje arme el bucle por mí.

Una operación de Stream típica tiene tres partes:

1. **Origen** — de dónde sale la secuencia (una lista, un array, etc).
   En mi código casi siempre `clientes.stream()`.

2. **Operaciones intermedias** — transformaciones que producen otro
   Stream. Cada una "envuelve" al anterior sin ejecutar nada todavía.
   Las más comunes que uso: `filter` (descarta elementos que no cumplen
   un predicado), `sorted` (ordena), `limit` (toma los primeros N),
   `map` (transforma cada elemento).

3. **Operación terminal** — algo que consume el stream y produce un
   resultado concreto: `collect(toList())`, `count()`, `findFirst()`,
   `forEach`. Hasta que aparece la terminal, el stream no ha procesado
   nada (es **lazy**).

**Por qué es más legible que un `for`.**

Comparemos mi búsqueda compuesta en las dos formas:

```java
// Con Stream — declarativo
List<Cliente> nuevosPremium = clientes.stream()
    .filter(Cliente::esActivo)
    .filter(c -> c.getTipoMembresia() == PREMIUM || c.getTipoMembresia() == VIP)
    .filter(c -> c.getFechaRegistro().isAfter(fechaCorte))
    .sorted(POR_INSCRIPCION_RECIENTE)
    .limit(5)
    .collect(Collectors.toList());
```

```java
// Con for tradicional — imperativo
List<Cliente> filtrados = new ArrayList<>();
for (Cliente c : clientes) {
    if (!c.esActivo()) continue;
    if (c.getTipoMembresia() != PREMIUM && c.getTipoMembresia() != VIP) continue;
    if (!c.getFechaRegistro().isAfter(fechaCorte)) continue;
    filtrados.add(c);
}
filtrados.sort(POR_INSCRIPCION_RECIENTE);
if (filtrados.size() > 5) {
    filtrados = filtrados.subList(0, 5);
}
```

El stream se **lee de arriba abajo como una receta**: filtra esto,
filtra esto otro, ordena, toma 5, recolecta. Cada paso es independiente
y nombrado. El nivel de detalle es exactamente el del problema de
negocio.

El for, en cambio, **mezcla la lógica de negocio con la mecánica del
bucle**: la lista temporal, los `continue` para saltar, el `subList` al
final para limitar. Si quiero agregar un filtro nuevo, en el stream
añado una línea; en el for tengo que pensar dónde insertar otro `if`.

Además, el stream **expone la intención**: cuando alguien lee "filter,
filter, sorted, limit", entiende inmediatamente que es una búsqueda
con varios criterios. El for podría estar haciendo eso, o podría estar
mutando variables, o teniendo efectos colaterales en cada iteración.
No puedo saberlo sin leer todo el cuerpo.

**Cuándo el for sigue siendo mejor.** Cuando hay efectos secundarios
(mutar variables externas, hacer logs, escribir archivos), un for
tradicional es más claro porque el stream no fue diseñado para eso
(usar `forEach` con efectos secundarios funciona pero rompe el
paradigma funcional). Y como mostró mi benchmark, el for tradicional
es 3× más rápido para operaciones simples. Para casos hot (que se
ejecutan millones de veces), o casos triviales (un solo filtro y
contar), prefiero el for. Para casos complejos con varias
transformaciones, el stream gana en mantenibilidad.

La regla práctica: si la pipeline tiene 3+ etapas o incluye sort/limit
con composición, va el stream. Si es una transformación trivial, el
for también está bien.
