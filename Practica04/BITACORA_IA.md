# Bitácora de IA — Práctica 4

Registro del uso de IA (Claude) como apoyo de pair programming durante esta práctica.

---

## Intercambio 1 — VIP no encajaba en la jerarquía simétrica

**Mi prompt:**
> Tengo tres tipos de membresia: Basica, Premium y VIP. Mi primera idea era
> ponerlas a las tres como hijas de una clase intermedia "Estandar" o
> "MembresiaConPago". Pero al empezar a codificar VIP me di cuenta de que
> no tiene precio mensual, tiene cuota anual. ¿Como lo resuelvo? ¿Pongo un
> precioMensual ficticio (cuota anual / 12) en VIP, o repienso la jerarquia?

**Resumen de la respuesta:**
La IA me dijo que el problema que estaba detectando es un sintoma clasico
de "herencia mal aplicada". El principio de sustitucion de Liskov dice que
una subclase debe poder usar lo que hereda sin mentir. Si VIP tiene que
inventarse un precioMensual que no representa nada en el negocio, esta
violando ese principio. Las opciones que me explico:
  1. Inventar el dato ficticio (lo peor, contamina el modelo).
  2. Subir el atributo precioMensual a la clase intermedia pero hacer la
     jerarquia asimetrica: solo las que realmente tienen precio mensual
     pasan por ahi.
  3. Usar composicion en lugar de herencia para los servicios.

Recomendo la opcion 2 porque mantiene la herencia sin forzar nada, y es lo
mas limpio para la escala del ejercicio.

**Qué hice con eso:**
Adopte la opcion 2: hice la jerarquia asimetrica. Estandar (con precioMensual)
es padre solo de Basica y Premium. VIP hereda directo de Membresia. La
jerarquia perdio simetria visual pero gano honestidad: ninguna clase tiene
campos ficticios. Esto se volvio el corazon del Elemento de Decision Propia
de la practica y lo documente largo en el README.

**Qué aprendí:**
Que las jerarquias "bonitas" en el papel no siempre son las correctas en
codigo. Cuando una hija no puede usar lo que hereda sin mentir, la
jerarquia esta mal y hay que repensarla. La regla practica que me quedo:
si una subclase tiene que sobrescribir casi todo para funcionar, mejor que
sea hermana que hija.

---

## Intercambio 2 — Donde poner los métodos abstractos

**Mi prompt:**
> En Membresia (raiz) tengo varios metodos: calcularPrecio, renovar,
> beneficiosIncluidos, descuentoRenovacion. ¿Marco todos como abstractos
> en la raiz, o solo algunos? ¿O conviene que renovar() tenga una
> implementacion por defecto?

**Resumen de la respuesta:**
La IA explico la diferencia practica: un metodo abstracto **obliga** a toda
clase concreta a darle una implementacion. Un metodo concreto (con cuerpo)
**permite** a las hijas heredarlo si les sirve. La pregunta clave es: "¿hay
una implementacion razonable que TODAS las subclases podrian usar?".

Para `renovar()` la respuesta es NO: Estandar suma 30 dias, VIP suma 365.
No hay un default neutro. Por eso debe ser abstracto en Membresia y se
implementa por separado en Estandar (30 dias, heredado por Basica y
Premium) y en VIP (365 dias).

Para `calcularPrecio()` y `beneficiosIncluidos()`, igual: no hay default
universal. Abstractos.

Sugirio que `estaVigente()` y `cancelar()` SI fueran concretos en Membresia
porque la logica es la misma para todas las subclases (la fecha y el flag
activa funcionan igual sin importar el tipo).

**Qué hice con eso:**
Apliqué eso: en Membresia, `calcularPrecio`, `beneficiosIncluidos`,
`renovar`, `descuentoRenovacion` y `tipoLegible` son abstractos.
`estaVigente`, `cancelar` y los getters son concretos. La consecuencia: el
compilador me obligo a implementar los abstractos en cada clase concreta,
lo cual es exactamente lo que queria (no dejarme olvidar uno).

**Qué aprendí:**
A pensar en metodos abstractos como "contratos que la subclase debe firmar"
y en metodos concretos heredables como "regalos que la subclase puede
usar tal cual". La decision depende de si existe un default razonable.

---

## Intercambio 3 — protected en serio, no por costumbre

**Mi prompt:**
> En P3 deje los atributos de Membresia como protected pensando en P4.
> Ahora que estoy en P4, ¿realmente necesito acceso protected, o podria
> hacerlos private y usar setters?

**Resumen de la respuesta:**
La IA me dijo que el debate "protected vs private" tiene dos campos legitimos:
  - Campo A (protected esta bien): los hijos son "familia" y el codigo es
    mas limpio sin tantos getters/setters internos.
  - Campo B (private siempre): aunque sean hijos, encapsular es encapsular;
    si manana cambias la representacion interna de un atributo en el padre,
    los hijos se rompen igual que si fueran externos.

Para mi caso particular concluyo: como mi jerarquia es estable (no se va a
re-arquitecturar a mitad del semestre) y la herencia se diseno
explicitamente, protected es razonable. Si fuera codigo de produccion
de un equipo grande, lo haria private para forzar la API.

**Qué hice con eso:**
Mantuve protected en los atributos de Membresia y Estandar. Esto permitio
que en MembresiaVIP escriba directamente `this.fechaFin = fechaInicio.plusDays(365)`
sin pasar por un setter intermedio que no aporta nada. Documente esta
decision en el README de P3 (era una decision que cruzaba ambas practicas).

**Qué aprendí:**
Que la "mejor practica" en abstracto no existe: depende del tamaño del
proyecto, la estabilidad de la jerarquia y el equipo. En un ejercicio
academico controlado, protected esta bien; en un sistema con cientos de
clases y multiples desarrolladores, private+API publica probablemente
gane.

---

## Intercambio 4 — Diagrama UML: PlantUML o imagen?

**Mi prompt:**
> La consigna pide diagrama UML "imagen o texto PlantUML". ¿Cual conviene
> mas para entrega y mantenimiento?

**Resumen de la respuesta:**
La IA dijo que PlantUML es mejor por varias razones:
  1. Es texto, asi que se commitea limpio (diff legibles).
  2. Se regenera automaticamente si cambio una clase. Una imagen dibujada
     en draw.io se desactualiza en cuanto modifico el codigo.
  3. IntelliJ tiene un plugin gratuito que renderiza .puml al abrirlo, asi
     que el evaluador (si usa IntelliJ) ve la imagen sin tener que abrir
     una herramienta aparte.

Recomendo dejar AMBOS en el repo: el .puml editable y un .png renderizado
para que el evaluador pueda ver la imagen al instante sin instalar nada.

**Qué hice con eso:**
Cree `UML.puml` con la jerarquia completa, atributos, metodos abstractos
en cursiva, y notas explicativas (especialmente la que justifica por que
VIP no hereda de Estandar). Despues lo renderice a PNG. Ambos archivos
quedan en el repo.

**Qué aprendí:**
Que para artefactos que dependen del codigo (diagramas, dumps, reportes),
es mejor que vivan como texto generable que como imagenes estaticas. El
diagrama de hoy es la verdad de hoy; si manana cambia el codigo, el .puml
se actualiza con una linea, no con redibujar todo.

---

## Intercambio 5 — Pattern matching de instanceof en Java 21

**Mi prompt:**
> Para llamar a metodos especificos de Premium (agendarClaseGrupal) en mi
> Main, sé que necesito un cast. ¿Sigue siendo `if (m instanceof Premium)
> { Premium p = (Premium) m; ... }` o hay algo mas nuevo en Java 21?

**Resumen de la respuesta:**
La IA explico que desde Java 16+ existe "pattern matching for instanceof":
puedo escribir directamente `if (m instanceof MembresiaPremium prem)` y
la variable `prem` queda disponible dentro del if con el tipo ya casteado.
Esto:
  1. Es mas corto (una linea menos).
  2. Es mas seguro (el cast lo hace el compilador, no yo).
  3. Es lo que un dev Java moderno escribiria hoy.

Tambien me menciono el "switch pattern matching" (en preview/standar segun
version) que permite escribir un switch que ramifica por tipo, pero ese
es mas avanzado y no lo necesito todavia.

**Qué hice con eso:**
Use la sintaxis nueva en mi Main:

```java
if (p1 instanceof MembresiaPremium prem) {
    prem.agendarClaseGrupal();
}
```

Mucho mas limpio que el patron tradicional. Esto demuestra ademas que mi
codigo aprovecha Java 21 y no solo la sintaxis de Java 8 que viene en los
manuales viejos.

**Qué aprendí:**
Que vale la pena revisar que features se agregaron entre versiones del
lenguaje. Estoy usando JDK 21 pero por costumbre escribiria como en Java 8.
Pattern matching para instanceof es una mejora ergonomica clara que ya
puedo usar en todas las practicas restantes.
