# Reflexión — Práctica 1

## 1. ¿Qué diferencia hay entre JDK, JRE y JVM? ¿Por qué instalamos el JDK?

La **JVM** (Java Virtual Machine) es la pieza que ejecuta el bytecode de Java.
Es lo que hace posible el "write once, run anywhere": el mismo `.class` corre
en cualquier sistema operativo donde haya una JVM.

El **JRE** (Java Runtime Environment) es la JVM más las bibliotecas estándar
de Java. Con el JRE puedo *ejecutar* programas Java compilados, pero no
puedo *crearlos*.

El **JDK** (Java Development Kit) incluye el JRE más las herramientas de
desarrollo: el compilador `javac`, el depurador `jdb`, `javadoc`, etc.

Instalé el JDK porque necesito compilar mi propio código, no solo ejecutar
programas ya compilados. Sin `javac` no podría convertir `HolaMundo.java`
en `HolaMundo.class`.

## 2. ¿Por qué Java es considerado "write once, run anywhere"? Explica con tus propias palabras.

Cuando compilo Java no obtengo código máquina específico de mi procesador,
sino *bytecode*: un formato intermedio diseñado para una máquina virtual
abstracta (la JVM). Ese bytecode es el mismo para todos los sistemas.

Lo que cambia entre plataformas es la JVM: hay una para Windows, otra para
Linux, otra para macOS, otra para ARM, etc. Cada JVM sabe traducir el
bytecode a las instrucciones reales de su CPU/SO.

Entonces yo escribo y compilo una sola vez, y mientras el equipo destino
tenga *alguna* JVM compatible, mi `.class` corre sin recompilar. En lenguajes
como C tendría que recompilar para cada plataforma.

## 3. ¿Qué hace exactamente `System.out.println()`? ¿Qué clase y método estás usando?

`System` es una clase del paquete `java.lang`. Tiene un atributo público y
estático llamado `out` que es del tipo `java.io.PrintStream` y representa la
salida estándar del proceso (típicamente la consola).

Cuando escribo `System.out.println("hola")` estoy:

1. Accediendo al campo estático `out` de la clase `System`.
2. Llamando al método de instancia `println(String)` sobre ese `PrintStream`.

`println` escribe el argumento al stream y le añade un separador de línea
del sistema operativo. Internamente convierte el argumento a `String`
(invocando `toString()` si no lo es) y delega en `print` + un salto de línea.

Es decir, no hay "magia": es simplemente acceso a un atributo público y una
llamada a método como cualquier otra, lo cual es coherente con que Java
quiera que todo viva dentro de clases.
