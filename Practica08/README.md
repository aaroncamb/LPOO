# Práctica 8 — Colecciones en Java

## Objetivo

Construir una clase gestora que use **al menos cuatro estructuras de
colección distintas**, cada una con justificación de por qué es la
apropiada para su dato. Implementar `Comparable` y `Comparator`s,
operaciones CRUD completas, búsquedas con Streams (incluyendo una
**búsqueda compuesta** como Elemento de Decisión Propia), uso de
iteradores, menú interactivo, 15+ datos de prueba y análisis de tiempo
de ejecución.

## Estructura

```
Practica08/
├── README.md
├── REFLEXION.md
├── BITACORA_IA.md
└── src/
    ├── Cliente.java               (con Comparable, enum TipoMembresia)
    ├── Notificacion.java          (item de la cola FIFO)
    ├── GestorClientes.java        (las 4 estructuras + CRUD + Streams)
    ├── DatosPrueba.java           (20 clientes realistas)
    ├── Main.java                  (menu interactivo)
    ├── BenchmarkOperaciones.java  (analisis empirico de tiempos)
    └── GestorTest.java            (31 pruebas unitarias)
```

## Compilación y ejecución

```bash
javac -d out src/*.java

java -cp out Main                  # menu interactivo
java -cp out BenchmarkOperaciones  # analisis de tiempos
java -cp out GestorTest            # 31 pruebas, todas pasan
```

## Las 4 estructuras de colección

| Estructura | Para qué | Por qué es la apropiada |
|---|---|---|
| **`ArrayList<Cliente>`** | Listado principal en orden de inserción | Acceso por índice O(1), iteración rápida, es el caballo de batalla cuando se necesita "todos en orden" |
| **`HashMap<Integer, Cliente>`** | Lookup por id en O(1) | La búsqueda por id es la operación más frecuente; barrer la lista sería O(n). El benchmark muestra que `HashMap.get()` es 300–500× más rápido que el barrido lineal |
| **`HashSet<String>`** | Emails únicos registrados | `add()` devuelve `false` si el email ya existía; usar esto evita un barrido para validar unicidad. `contains()` es O(1) |
| **`LinkedList<Notificacion>`** | Cola FIFO de notificaciones pendientes | `offer()` agrega al final y `poll()` saca del frente, ambos O(1). Si fuera `ArrayList`, sacar del frente sería O(n) por el desplazamiento |

Las 4 estructuras conviven sincronizadas dentro del gestor: cuando se
agrega un cliente entra a la lista, al mapa y su email al set. Cuando se
elimina, sale de los tres. Mantener esa sincronía es responsabilidad de
los métodos `agregar`, `eliminarPorId` y `purgarInactivos`.

### ¿Por qué no usar `ArrayList` para todo?

Lo desarrollo en `REFLEXION.md` pregunta 1, pero el resumen empírico
está en el benchmark: buscar un id al final de una `ArrayList` de
50.000 elementos toma ~165.000 ns; el `HashMap.get()` toma ~325 ns. La
diferencia se nota a poco que crezca el sistema.

## `Comparable` y `Comparator`s

### `Comparable<Cliente>` — orden natural por nombre

```java
@Override
public int compareTo(Cliente otro) {
    return this.nombreCompleto.compareToIgnoreCase(otro.nombreCompleto);
}
```

El orden natural es por nombre porque es el caso de uso más común en
listados, reportes y la pantalla de recepción. Esto permite
`Collections.sort(lista)` y estructuras ordenadas como `TreeSet` sin
necesidad de Comparator explícito.

### Dos Comparators personalizados

```java
public static final Comparator<Cliente> POR_ANTIGUEDAD =
        Comparator.comparing(Cliente::getFechaRegistro);

public static final Comparator<Cliente> POR_INSCRIPCION_RECIENTE =
        POR_ANTIGUEDAD.reversed();
```

`POR_ANTIGUEDAD` ordena de más viejo a más nuevo (fecha de registro
ascendente). `POR_INSCRIPCION_RECIENTE` invierte ese orden. Definirlos
como constantes públicas estáticas permite reutilizarlos en distintos
lugares del código sin reescribir el lambda.

## Elemento de Decisión Propia — Búsqueda compuesta

```java
public List<Cliente> nuevosPremiumDesde(LocalDate fechaCorte, int limite) {
    return clientes.stream()
            .filter(Cliente::esActivo)
            .filter(c -> c.getTipoMembresia() == Cliente.TipoMembresia.PREMIUM
                      || c.getTipoMembresia() == Cliente.TipoMembresia.VIP)
            .filter(c -> c.getFechaRegistro().isAfter(fechaCorte))
            .sorted(POR_INSCRIPCION_RECIENTE)
            .limit(limite)
            .collect(Collectors.toList());
}
```

### Qué consulta es y por qué es relevante

**"Nuevos clientes activos con membresía Premium o VIP, registrados
después de cierta fecha, ordenados por inscripción más reciente, top N."**

Cumple **cuatro criterios simultáneos** en una sola expresión declarativa:

1. Filtro por estado: solo activos.
2. Filtro por categoría: Premium o VIP (los planes de mayor valor).
3. Filtro por fecha: registrados después de la fecha de corte.
4. Orden + límite: top N por antigüedad reciente.

### Por qué es relevante para el dominio

Es una consulta real del gimnasio: marketing necesita identificar a los
**nuevos clientes de alto valor** del último trimestre para enviarles
una campaña de fidelidad personalizada (cupones, sesiones gratis,
clases exclusivas). La consulta:

- Excluye clientes inactivos (no tiene sentido invertir en alguien
  que se dio de baja).
- Excluye clientes Básicos (campaña dirigida a planes con mayor
  margen).
- Excluye clientes viejos (ya son leales, esta campaña es para los
  nuevos).
- Limita el resultado a top N para que la campaña sea manejable.

Esta consulta es exactamente el tipo de cosa que en SQL se haría con
`WHERE ... AND ... ORDER BY ... LIMIT`. La traducción a Stream queda
sumamente legible y fácil de modificar (agregar un nuevo filtro es una
línea más).

## Operaciones CRUD completas

| Operación | Método | Estructura impactada |
|---|---|---|
| Create | `agregar(Cliente)` | Lista + Map + Set (los 3) |
| Read   | `buscarPorId(int)` | Map (O(1)) |
| Read   | `buscarPorNombre(String)` | Lista (Stream con filter) |
| Update | `cambiarEstado(id, bool)` | Map (lookup) → mutación del objeto |
| Delete | `eliminarPorId(int)` | Lista + Map + Set (los 3) |
| Delete | `purgarInactivos()` | Los 3, vía Iterator.remove() |

## Uso de iteradores

`purgarInactivos()` demuestra el uso correcto del Iterator:

```java
Iterator<Cliente> it = clientes.iterator();
while (it.hasNext()) {
    Cliente c = it.next();
    if (!c.esActivo()) {
        it.remove();   // unica forma segura de eliminar mientras se itera
        ...
    }
}
```

Si en lugar de `it.remove()` hiciera `clientes.remove(c)` dentro de un
`for-each` tradicional, Java lanzaría `ConcurrentModificationException`.
El método `iterator.remove()` es la única forma segura de modificar la
colección mientras se la recorre.

## Menú interactivo

El `Main` presenta un menú con 9 opciones que cubren todas las
operaciones del gestor. Los datos de prueba se cargan al inicio (20
clientes) para que el usuario pueda explorar sin tener que crear cada
cliente a mano.

## Análisis de tiempo de ejecución

`BenchmarkOperaciones` genera 50.000 clientes sintéticos y mide cada
operación 50 veces para reportar la mediana, descartando outliers de
JIT/GC. Los resultados típicos en mi máquina:

```
--- Busqueda por id (peor caso, al final) ---
  ArrayList lineal           165504 ns
  HashMap.get()                 323 ns
  → 'HashMap.get()' es 512x mas rapida

--- Sort de la lista completa ---
  Por nombre (Comparable)    2245573 ns
  Por fecha (Comparator)    13769609 ns
  → 'Por nombre' es 6.1x mas rapida

--- Filtrado y conteo (clientes Premium) ---
  Stream                      233573 ns
  for-each                     74933 ns
  → 'for-each' es 3.1x mas rapida
```

**Conclusiones:**

1. **HashMap.get() vs barrido lineal**: 300–500× más rápido. Justifica
   tener el índice `HashMap<Integer, Cliente>` además de la lista. Sin
   él, cada `buscarPorId` sería O(n) y con 10.000+ clientes se notaría
   en la interfaz.

2. **Sort por Comparable vs Comparator de LocalDate**: el sort por
   nombre es 6× más rápido porque `String.compareToIgnoreCase` está
   muy optimizado en JDK, mientras que `LocalDate.compareTo`
   descompone año/mes/día. Asintóticamente ambos son O(n log n); la
   diferencia es la constante por comparación.

3. **Stream vs for-each**: el `for-each` tradicional es 3× más rápido
   que el Stream para un filtro+conteo simple, debido al overhead de
   las abstracciones del pipeline. La diferencia es real pero el
   Stream gana en legibilidad y composabilidad: cuando hay 4-5 etapas
   (filter + sorted + limit + collect), el Stream se mantiene legible
   mientras el `for-each` se vuelve un anidamiento. **Para casos
   complejos como mi búsqueda compuesta, el costo extra del Stream es
   un buen precio por la claridad**.

Los números absolutos dependen del hardware; lo que importa es la
**proporción** entre operaciones, que se mantiene en cualquier sistema.

## Resultados de las pruebas

```
=== Resumen ===
Pasadas:  31
Falladas: 0
Total:    31
```

31 pruebas cubriendo:
- CRUD completo (6 pruebas).
- Las 4 estructuras (4 pruebas: lista, mapa lookup, set unicidad, cola FIFO).
- Comparable y los 2 Comparators (3 pruebas).
- Streams: filtros simples + búsqueda compuesta (4 pruebas).
- Búsqueda compuesta: que cumpla los 3 filtros y el orden, y que
  devuelva vacío cuando no hay nada (2 pruebas).
- Iteradores: contar y purgar con `iterator.remove()` (2 pruebas).
- Cola FIFO: orden correcto y cola vacía (2 pruebas).
- Estadísticas: conteo por tipo y peso promedio (2 pruebas).
- Estados, duplicados, eliminación con limpieza de las tres estructuras
  (6 pruebas).
