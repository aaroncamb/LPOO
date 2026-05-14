# Reflexión — Práctica 9

## 1. ¿Qué es la serialización y cuándo es útil en comparación con guardar texto plano?

**Serialización** es el proceso de convertir un objeto en memoria en
una secuencia de bytes que se puede guardar a disco o enviar por la
red, y después recuperar para reconstruir un objeto equivalente
(**deserialización**).

En Java, una clase puede serializarse con solo declarar `implements
Serializable` y dejar que la JVM se encargue: lee los atributos del
objeto, los convierte a bytes con su propio formato binario, escribe
también la información del tipo. Mi `Cliente` lo hace con una sola
línea.

**Cuándo es útil frente a texto plano:**

- **Cuando el estado a guardar es complejo o anidado.** Un `Cliente`
  tiene atributos primitivos, un `String`, un `LocalDate`, un `enum`,
  un `boolean`. Para guardarlo como texto, tengo que decidir cómo
  representar cada cosa (¿la fecha como ISO? ¿el enum como string?), y
  para leerlo tengo que escribir un parser que invierta esas
  decisiones. Con serialización, todo eso lo hace la JVM por mí: el
  archivo `.dat` contiene "el cliente" sin que yo tenga que pensar
  qué significa cada byte.

- **Cuando quiero un snapshot del estado del sistema.** Si tengo una
  `List<Cliente>` de 20 objetos en memoria y quiero guardarla para
  reanudar después, la serialización me da una operación atómica:
  `out.writeObject(lista)` y listo. La lista, su tipo, su tamaño, y
  cada cliente con todos sus campos quedan en el archivo. Al cargar,
  obtengo de vuelta una `List<Cliente>` idéntica.

- **Cuando el archivo solo lo leerá Java.** Si la única aplicación que
  va a usar el archivo es mi propio sistema, no tiene sentido pagar el
  costo de exportar a un formato neutro como CSV.

**Cuándo no es útil:**

- **Cuando el archivo necesita leerse desde otro lenguaje o
  aplicación.** Excel no entiende `.dat` serializado de Java; ni
  Python, ni JavaScript. El formato binario es **propietario de la
  JVM**.

- **Cuando el archivo debe ser legible por humanos.** Un `.dat` abierto
  en notepad es una sopa de bytes; un `.csv` o un `.txt` se pueden
  inspeccionar a ojo.

- **Cuando la clase evoluciona y rompe la compatibilidad.** Si mañana
  agrego un atributo a `Cliente` y cambio el `serialVersionUID`, los
  archivos viejos dejan de cargar. El texto plano tiende a ser más
  robusto a cambios (puedo ignorar columnas nuevas o llenar con
  valores por defecto).

**Por qué en mi proyecto uso las dos:**

En P9 expongo ambos. El CSV es para intercambio con sistemas externos
y para que un humano pueda inspeccionar los datos. El binario es para
el snapshot rápido del estado, y es lo que respalda el `BackupManager`.
Tener las dos representaciones cubre los dos escenarios sin obligar a
escoger.

## 2. ¿Por qué usamos `BufferedReader` en lugar de leer byte a byte? ¿Qué mejora en rendimiento ofrece?

`BufferedReader` envuelve a otro `Reader` (como `FileReader`) y le
agrega un **buffer interno de ~8 KB**. La diferencia respecto a leer
byte a byte (o carácter a carácter directamente del archivo) está en
cuántas veces el programa "habla con el sistema operativo".

Cada operación de lectura del disco implica:

1. **Una llamada al sistema operativo** (`read()` syscall).
2. El SO accede al disco o al cache, lee el bloque.
3. Devuelve los bytes al programa.

Las llamadas al sistema operativo tienen un costo fijo (cambio de
contexto entre user space y kernel space). Si las hacemos por cada
byte que leemos, el archivo de 1 KB cuesta 1024 syscalls; de 1 MB,
1 millón. Esto es **órdenes de magnitud más lento** que necesario.

Con `BufferedReader`:

1. **Una sola syscall** lee 8 KB del disco al buffer en memoria.
2. Los siguientes 8.000+ caracteres que pida el programa se sirven
   **desde memoria**, sin tocar el SO.
3. Cuando el buffer se agota, otra syscall trae 8 KB más.

Para un archivo de 1 MB, esto pasa de ~1 millón de syscalls a ~128.
La diferencia se siente en archivos medianos en adelante.

**Otra ventaja**: `BufferedReader.readLine()` lee hasta el siguiente
`\n` o `\r\n` y devuelve la línea sin el separador. Si yo tuviera que
hacer eso manualmente, tendría que leer carácter por carácter mirando
si el siguiente es un salto de línea, manejar `\r`, `\n`, `\r\n` de
distintos sistemas operativos, acumular en un `StringBuilder`. Es
exactamente lo que `BufferedReader` ya hace internamente.

Lo mismo aplica a `BufferedWriter` en el otro sentido: acumula
escrituras en su buffer interno y las descarga al disco en bloques
grandes en lugar de hacer una syscall por cada `write`.

**En mi código** todos los métodos de `GestorArchivos` que tocan
archivos envuelven `FileReader`/`FileWriter` en `BufferedReader`/`BufferedWriter`,
y los streams binarios en `BufferedInputStream`/`BufferedOutputStream`.
Es la primera línea de defensa contra I/O lento.

## 3. ¿Qué riesgos tiene no cerrar un archivo después de usarlo? ¿Cómo los mitigaste?

Los riesgos prácticos son cuatro:

**1. Fugas de descriptores de archivo (file handles).** El sistema
operativo limita cuántos archivos puede tener un proceso abiertos al
mismo tiempo (típicamente 1024 en Linux). Si abro archivos en un
bucle y no los cierro, en algún momento el SO me niega abrir más y
recibo `Too many open files`. El programa truena en un lugar
aparentemente no relacionado.

**2. Pérdida de datos por buffer no descargado.** `BufferedWriter`
acumula en su buffer interno antes de escribir al disco. Si el
programa termina sin cerrar el writer, los bytes que estaban en el
buffer **no llegan al archivo**. El archivo queda incompleto y
silenciosamente. Esto es especialmente engañoso porque mis pruebas
podrían pasar en archivos chicos (cabe todo en un solo flush) y
fallar misteriosamente en producción con archivos grandes.

**3. Bloqueo del archivo en otros procesos.** En Windows, un archivo
abierto por un programa puede no ser borrable ni renombrable por
otros (lock exclusivo). Si dejo un archivo abierto, podría impedir
que un proceso externo (un antivirus, un script de backup) lo
manipule.

**4. Pérdida de información en cierres con error.** Algunos
recursos solo escriben metadatos durante el `close()` (footer del
ZIP, terminador del JSON streaming, etc). No cerrar deja el archivo
malformado.

**Cómo los mitigué en mi código:**

Uso `try-with-resources` en **toda** operación de I/O. Esto declara
el recurso en el paréntesis del `try` y Java garantiza que `close()`
se llama automáticamente al salir del bloque, sin importar si fue
con éxito, con excepción, o por `return` temprano. Ejemplo de mi
`GestorArchivos.guardarBinario`:

```java
public void guardarBinario(String archivo, List<Cliente> clientes) throws IOException {
    try (ObjectOutputStream out = new ObjectOutputStream(
            new BufferedOutputStream(new FileOutputStream(archivo)))) {
        out.writeObject(new ArrayList<>(clientes));
    }
}
```

Tres ventajas concretas de este patrón sobre `finally` manual:

1. **No se puede olvidar el `close()`.** Java lo escribe por mí.
2. **Funciona con varios recursos anidados**: cuando declaro
   `ObjectOutputStream` envolviendo `BufferedOutputStream` envolviendo
   `FileOutputStream`, Java los cierra a todos en orden inverso al
   salir del bloque.
3. **Las excepciones del cierre se guardan como suppressed** en lugar
   de "tapar" la excepción principal del cuerpo.

Adicionalmente, en `BackupManager` y `GestorArchivos.listarArchivos`
uso `Files.list()` también dentro de `try-with-resources` porque el
`Stream` que devuelve sostiene un descriptor de directorio que también
hay que cerrar. Es una sutileza fácil de olvidar; si no lo cierras, la
fuga de descriptor existe igual que con archivos comunes.

**Por qué importa en este proyecto en particular:** `GymLogger`
(P7) y aquí en P9 ambos escriben a archivos con frecuencia (cada
operación logueada, cada CSV, cada backup). Un solo método que olvide
cerrar un archivo y se ejecute en un bucle es una fuga creciente. El
patrón `try-with-resources` aplicado en todos los puntos de I/O
elimina toda esa categoría de bugs por construcción, no por
disciplina del programador.
