# Bitácora de IA — Práctica 9

Registro del uso de IA (Claude) como apoyo de pair programming durante esta práctica.

---

## Intercambio 1 — Elegir el formato alternativo al CSV

**Mi prompt:**
> El elemento de decision propia me deja escoger un formato alternativo:
> JSON manual con StringBuilder, XML basico, o reporte de texto
> alineado. ¿Cual es mas defendible para un curso, sin sobreingenierizar?

**Resumen de la respuesta:**
La IA me ayudo a pensar el caso de uso real: para QUE serviria el
formato alternativo. JSON tiene sentido si voy a integrar con una API
web; XML si trabajo con sistemas legacy; reporte alineado si lo va a
leer un humano. Para un sistema de gimnasio, el caso mas honesto es
"el gerente quiere ver el listado en pantalla o pegar en un correo",
y para eso un reporte alineado supera al CSV (que en notepad es una
sopa) y al JSON (que tampoco se lee de un vistazo).

Recomendo el reporte alineado por dos razones extra: (1) se puede
implementar con padding de strings, sin necesidad de parseo complejo;
(2) es facil de defender porque el caso de uso es obvio para cualquiera
que haya visto un reporte impreso.

**Qué hice con eso:**
Implemente ReporteAlineado con cabecera, columnas calibradas a los
datos reales del gimnasio, y pie con totales por tipo. La estructura
de "cabecera + columnas + pie" lo hace ver profesional sin esfuerzo
desmedido. Documente en el README la comparativa CSV vs Reporte
alineado en tabla para que la defensa oral tenga un anclaje claro.

**Qué aprendí:**
Que para "decision propia" hay una trampa: lo brillante puede ser
abrumador y lo simple puede ser facil. Lo MEJOR es algo que tenga
caso de uso claro y se pueda defender en una linea. JSON manual con
StringBuilder hubiera sido mas vendible para un programador senior,
pero hubiera atraido preguntas tecnicas dificiles ("¿como manejas el
escape de \n dentro de strings?"). Reporte alineado tiene caso de uso
imposible de cuestionar y la implementacion es robusta.

---

## Intercambio 2 — Cuando usar try-with-resources con Files.list()

**Mi prompt:**
> Files.list(dir) devuelve un Stream<Path>. Vi que algunos ejemplos lo
> envuelven en try-with-resources y otros no. ¿Es necesario?

**Resumen de la respuesta:**
La IA explico que SI es necesario: el Stream que devuelve Files.list()
mantiene abierto un descriptor de directorio en el SO. Aunque parece
una operacion "ligera", si no lo cierras puedes acumular descriptores
de directorios igual que con archivos comunes.

Streams en general NO requieren try-with-resources (un
list.stream().filter(...).count() es seguro porque no hay recurso
externo). Pero los obtenidos de Files.list(), Files.walk(),
Files.newDirectoryStream() SI lo requieren porque internamente
sostienen handles del SO.

La regla simple: si el Stream viene de una operacion de I/O, envuelvelo
en try-with-resources.

**Qué hice con eso:**
Lo aplique en GestorArchivos.listarArchivos:

```java
try (var stream = Files.list(p)) {
    stream.filter(Files::isRegularFile)
          .forEach(...);
}
```

Y tambien en ArchivosTest.limpiarDirectorio donde uso Files.walk().
Documente esta sutileza en la REFLEXION pregunta 3 porque es del tipo
de cosa que solo aparece en defensa oral si uno la conoce.

**Qué aprendí:**
Que "stream" en Java tiene dos sabores: los puramente funcionales
(de Collections) y los que sostienen recursos (de Files, de
BufferedReader.lines()). El compilador no los distingue, pero el SO
si. El "var stream = Files.list(p)" en un try-with-resources es la
manera segura.

---

## Intercambio 3 — Parseo CSV que respete nombres con coma

**Mi prompt:**
> Mi CSV tiene una columna "nombre" que podria contener una coma
> ("Apellido, Nombre"). Si hago split(",") se rompe. ¿Hago un parser
> que respete comillas? ¿O traigo OpenCSV?

**Resumen de la respuesta:**
La IA discutio las opciones: (1) usar OpenCSV (5 lineas, perfecto, pero
agrega dependencia externa que el curso quizas no quiere); (2) escribir
un parser basico que solo respete comillas dobles; (3) reemplazar la
coma del nombre por otro caracter antes de exportar (hack, fragil).

Recomendo la opcion 2 porque (a) cumple para el dominio (nombres con
una coma ocasional, no estructuras complicadas), (b) demuestra que
entiendo el problema y no solo importo una solucion, (c) el codigo es
~15 lineas y es comprensible.

Sugirio el algoritmo: caminar caracter por caracter; un flag
"dentroComillas" se voltea con cada `"`; los `,` dentro de comillas se
ignoran como separador.

**Qué hice con eso:**
Implemente exactamente eso en GestorArchivos.parsearLineaCSV. Tambien
agregue al escribir: si el nombre contiene `,`, lo envuelvo en
comillas dobles. Cubri esto con una prueba especifica
("pruebaCSVConNombreQueTieneComa") que demuestra que el round-trip
funciona.

Documente que NO es RFC-4180 completo (no maneja comillas dentro de
comillas escapadas como "He said ""hello"""), pero es suficiente para
los datos del gimnasio. Esa honestidad sobre los limites del parser es
defendible: muestra que pense en el caso pero decidi alcance.

**Qué aprendí:**
Que el CSV es uno de esos formatos "engañosamente simples". Parece
trivial hasta que aparece el primer nombre con coma, y de ahi sale un
problema entero. Hay una serie completa de bromas en programacion
sobre cuanto trabajo es parsear CSV bien. Saber el algoritmo de
"flag de comillas" me cubre el 95% de los casos sin importar libreria.

---

## Intercambio 4 — Cuanto ancho dejo a cada columna del reporte

**Mi prompt:**
> Para el reporte alineado, ¿como decido los anchos de columna? Si me
> quedo corto, los nombres se cortan; si me paso, el reporte queda
> ancho innecesariamente.

**Resumen de la respuesta:**
La IA propuso medir los datos reales: revisar mis 20 clientes de
prueba y ver cuanto miden los nombres mas largos, los emails mas
largos, etc. Calibrar a eso + 2-3 caracteres de holgura. Para campos
de tipo conocido (enum con 3 valores), exactamente el ancho del
valor mas largo. Para numericos, el ancho del numero mas grande
posible mas el punto y decimales.

Tambien sugirio: si un dato excede el ancho, **truncarlo** en lugar
de romper la alineacion. Mejor un nombre cortado que toda la fila
desfasada.

**Qué hice con eso:**
Calibre con mis datos reales: el nombre mas largo es "Luis Antonio
Castillo Cruz" (29 caracteres), pongo W_NOMBRE = 32. Los emails mas
largos van a 28. Las constantes quedaron documentadas como "calibradas
a los datos del gimnasio". Si manana entra alguien con nombre de 40
caracteres, se trunca pero el reporte sigue legible.

**Qué aprendí:**
Que las decisiones de presentacion (anchos, formatos) deben anclarse
a los datos reales, no a numeros redondos arbitrarios. "32" no es
mejor que "30" en abstracto, pero si tengo un nombre de 29 caracteres,
32 me deja holgura para los siguientes 2-3 sin ser excesivo.

---

## Intercambio 5 — serialVersionUID de Cliente, ¿que valor le pongo?

**Mi prompt:**
> Ya en P7 use serialVersionUID = 1L en mis excepciones. Aqui Cliente
> tambien lo necesita por serializacion. ¿Sigue siendo 1L? ¿Importa el
> valor? ¿O lo deberia generar con IntelliJ?

**Resumen de la respuesta:**
La IA explico que el valor en si no importa: lo que importa es que
sea CONSISTENTE entre la version que serializa y la version que
deserializa. Si dejo serialVersionUID = 1L en Cliente y manana cambio
la clase pero mantengo el 1L, los archivos viejos seguiran cargando
mientras la JVM pueda mapear los campos (compatibilidad relajada).

IntelliJ ofrece auto-generar un valor de 16 digitos basado en el
hash de la firma de la clase. Eso es util si quiero que CUALQUIER
cambio en la clase rompa los archivos viejos. Para mi caso prefiero
1L explicito: tengo control sobre cuando "rompo" la compatibilidad
(cambio el numero cuando hago un cambio incompatible a proposito).

**Qué hice con eso:**
Mantuve `serialVersionUID = 1L` en Cliente y documente en un
comentario que si en el futuro cambio incompatibilemente la
estructura del Cliente, debo subir el numero a 2L para que los
archivos viejos no se carguen mal silenciosamente.

**Qué aprendí:**
Que serialVersionUID es un contrato de version, no un identificador
unico de clase. Mantenerlo bajo control manual da mejor experiencia
de evolucion: tu decides cuando un cambio rompe vs cuando no, en
lugar de que el compilador lo decida por el hash. En sistemas reales
de produccion, mantener serialVersionUID conscientemente es practica
estandar.
