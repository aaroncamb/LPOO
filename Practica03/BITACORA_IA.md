# Bitácora de IA — Práctica 3

Registro del uso de IA (Claude) como apoyo de pair programming durante esta práctica.

---

## Intercambio 1 — IllegalArgumentException vs devolver booleano

**Mi prompt:**
> Cuando un setter recibe un valor invalido, ¿lanzo una excepcion, devuelvo
> false, o dejo el objeto como estaba en silencio? Las tres opciones se
> pueden, pero no me queda claro cual es la mejor en Java.

**Resumen de la respuesta:**
La IA recomendó lanzar `IllegalArgumentException`. Argumentos: (a) un setter
es semánticamente una operación de asignación que asume éxito; si falla,
falla con ruido para que se note; (b) devolver `boolean` obliga al llamador
a recordar revisarlo, y el día que se olvide tendrá un bug silencioso donde
el objeto parece haberse modificado pero no; (c) silencio total es lo peor,
porque oculta bugs; (d) las excepciones cargan el mensaje descriptivo con
el valor exacto recibido, lo cual es invaluable para depurar logs.

**Qué hice con eso:**
Adopté `IllegalArgumentException` con mensajes que incluyen el valor real
recibido (`"Recibido: -500.0"`). Rechacé la sugerencia inicial de la IA de
crear una `ClienteException` propia: P3 todavía no toca excepciones
personalizadas, eso es de P7. Mantener `IllegalArgumentException` me dice
que el error es "argumento ilegal a un metodo", que es exactamente lo que
pasa, y no obliga al llamador a hacer `throws` por todos lados (es una
RuntimeException).

**Qué aprendí:**
A distinguir errores del programador (bug en el código que llama: pasar
basura a un setter) de errores del entorno (un archivo no existe, una
conexión se cayó). Para los primeros, RuntimeException; para los segundos,
Exception checked. Esta distinción me sirve para todo lo que viene.

---

## Intercambio 2 — Caso especial del peso 0

**Mi prompt:**
> Mi regla es que el peso debe estar entre 30 y 300 kg. Pero un cliente
> recien dado de alta no tiene peso aun (se pesa despues, cuando llega al
> gimnasio). ¿Acepto 0 como caso especial o pongo un valor inicial razonable?

**Resumen de la respuesta:**
La IA analizó las opciones: (1) poner un default tipo 70 kg, (2) hacer el
campo `Double` (wrapper, puede ser null), (3) aceptar 0 como sentinel.
Recomendó la (3) porque (1) contamina el sistema con datos falsos que no
se distinguen de los reales, y (2) introduce null checks en todos lados.
El sentinel 0 funciona porque ningún peso real será 0, y el constructor
mínimo puede inicializar el campo sin pelearse con la validación.

**Qué hice con eso:**
Lo implementé tal cual: si `setPesoKg(0)` llega, el setter acepta y
asigna sin disparar el rango. Para que esto se note en el comportamiento
del resto del sistema, `calcularIMC()` devuelve -1 cuando faltan datos, y
en `toString()` aparece como "n/d". Esto se volvió mi elemento de decision
propia para esta práctica porque combina una regla de validacion con un
caso de negocio real.

**Qué aprendí:**
Que las validaciones tipo "rango duro" a veces necesitan una "puerta de
escape" para representar estados legítimos del mundo real. La trampa de
los novatos es validar tan estricto que el objeto queda con un valor
inventado para satisfacer la validacion, cuando lo correcto es que el
sistema represente honestamente "no sé".

---

## Intercambio 3 — protected vs private para Membresia

**Mi prompt:**
> En P4 voy a heredar de Membresia con tres subclases. ¿Marco los atributos
> private (los hijos usan getters) o protected (los hijos acceden directo)?
> En el libro vi las dos formas pero no me queda claro la diferencia
> practica.

**Resumen de la respuesta:**
La IA dijo que en teoría private+getters es más "puro" porque encapsula
mejor incluso frente a las hijas, pero en la práctica `protected` es lo
correcto cuando hay intención explícita de herencia y los atributos son
parte del contrato de extensión. Las hijas son "familia", no "extraños",
así que pueden ver el estado interno. Lo malo de `protected` aparece cuando
se abusa y cualquier clase del mismo paquete (no solo las hijas) lo puede
ver. En proyectos con un solo paquete eso es un riesgo.

**Qué hice con eso:**
Opté por `protected` en `Membresia` porque: (a) la herencia en P4 está
diseñada explícitamente; (b) las subclases necesitan modificar precio y
duración según el tipo, lo cual sería un río de getters/setters; (c) en
mi proyecto del semestre todo va a estar en paquetes separados pronto, así
que el problema de "todos en el mismo paquete" se va a mitigar. Documenté
esta decisión en el README porque sé que en defensa oral van a preguntarme
por qué `Cliente` tiene private y `Membresia` tiene protected.

**Qué aprendí:**
Que los modificadores de acceso no son reglas matemáticas, son decisiones
de diseño que comunican intención. `private` dice "esto es mío y de nadie
más"; `protected` dice "esto es mío y de mis hijas"; `public` dice "esto
es el contrato hacia afuera". El día que necesite reorganizar paquetes,
estos modificadores afectan qué se rompe.

---

## Intercambio 4 — Regex para email, ¿qué tan estricta?

**Mi prompt:**
> Para validar email, ¿uso una regex completa RFC 5322 o algo mas simple?
> Encontre en internet regex de cientos de caracteres pero parecen excesivas.

**Resumen de la respuesta:**
La IA argumentó que validar RFC-5322 completo es contraproducente: la regex
real ocupa una página, es ilegible, y rechaza emails raros pero válidos
(con comillas, espacios escapados, etc.). Para un sistema de gimnasio,
basta con atrapar los errores comunes de captura. Propuso una regex
intermedia: parte local con caracteres comunes, `@`, dominio con punto,
TLD de 2+ letras. Mencionó que la validacion "real" del email es enviar
un correo de confirmación y ver si el usuario hace clic.

**Qué hice con eso:**
Adopté la regex sugerida:
`^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$`
Y en el README documenté que es deliberadamente menos estricta que RFC y
por qué. Probé en mi ClienteTest que rechaza los casos comunes (sin @, sin
punto, vacío) pero acepta direcciones normales. No me complique con la
validacion perfecta porque no aporta al objetivo.

**Qué aprendí:**
Que "validar más" no siempre es "validar mejor". Hay un punto óptimo donde
atrapas los errores reales sin ahogarte en casos imposibles. Esto es
contraintuitivo: uno cree que mientras más estricto, mejor.

---

## Intercambio 5 — Warning de "this-escape" en Java 21

**Mi prompt:**
> Al compilar con -Xlint:all me sale "possible 'this' escape before subclass
> is fully initialized" cuando mi constructor llama a setTipo(). El programa
> funciona bien, pero ¿que significa el warning y deberia preocuparme?

**Resumen de la respuesta:**
La IA explicó que el problema es teórico pero real: si una subclase
sobrescribe `setTipo()`, ese metodo puede ejecutarse antes de que la
subclase haya inicializado sus propios campos, viendo un objeto a medio
construir. En Java 21 agregaron este warning porque el bug es sutil y
silencioso. Las soluciones: (a) marcar el setter como `final` para que
nadie lo sobrescriba, (b) usar metodos `private` para la validacion,
o (c) suprimir el warning con `@SuppressWarnings("this-escape")` si la
clase no se va a heredar de forma que afecte ese setter.

**Qué hice con eso:**
En `Cliente` puse `@SuppressWarnings("this-escape")` porque no planeo
heredar de Cliente. En `Membresia` también lo suprimí pero con un
comentario explicando que en P4, cuando refactorice a abstracta, voy a
mover la validacion a un metodo `final` o estatico. Esto deja constancia
de que conozco el warning, no es que lo ignore por flojera. Compilar con
`-Xlint:all` sin warnings es una señal de cuidado que sé que la rúbrica
mira ("Limpieza, indentación, comentarios").

**Qué aprendí:**
Que los warnings nuevos del compilador suelen anunciar trampas reales que
la comunidad descubrió con los años. Vale la pena prestarles atención
aunque el programa "funcione". También aprendí que `@SuppressWarnings`
debe usarse localizado (en el método específico) y siempre con un
comentario que diga por qué; suprimir todo globalmente es exactamente lo
que el warning quería evitar.
