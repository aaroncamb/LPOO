# Bitácora de IA — Práctica 1

Registro del uso de IA (Claude) como apoyo de pair programming durante esta práctica.

---

## Intercambio 1 — Decisión sobre el "Elemento de Decisión Propia"

**Mi prompt:**
> Voy a hacer un curso de POO en Java. La Práctica 1 me pide hacer un HolaMundo
> y además un segundo programa "que me parezca útil" usando solo Java estándar.
> El proyecto final del semestre es un sistema POS para gimnasio. ¿Qué programa
> pequeño tendría sentido hacer ahora que después me sirva, sin adelantarme a
> lo que pide la práctica?

**Resumen de la respuesta:**
La IA sugirió varias opciones y me recomendó una calculadora de IMC porque:
(a) los clientes de un gimnasio guardan peso y altura, así que la lógica se
reaprovecha más adelante; (b) ejercita lectura por consola, validación y
manejo básico de excepciones que la rúbrica pondera 20%; (c) no requiere
adelantarme a clases todavía.

**Qué hice con eso:**
Acepté la idea de la calculadora de IMC porque la justificación de
reutilización me convenció. Rechacé la versión inicial donde la IA proponía
una clase `Persona` con atributos: en Práctica 1 todavía no toca crear
clases de dominio, así que reduje la solución a una clase con `main` y
métodos estáticos auxiliares. La modelización del cliente la dejo para P2.

**Qué aprendí:**
A elegir el alcance de una solución por la práctica donde estoy parado, no
por lo que "se puede hacer". Sobreingeniería en la P1 sería tan mala señal
como hacer la calculadora con un `if` gigante.

---

## Intercambio 2 — Sobre la lectura robusta de números desde consola

**Mi prompt:**
> Si uso `Scanner.nextDouble()` y el usuario escribe letras, ¿qué pasa?
> ¿Cómo lo manejo sin que el programa truene?

**Resumen de la respuesta:**
Explicó que `nextDouble()` lanza `InputMismatchException` y, lo más
importante, que el token inválido **no se consume del buffer**, así que un
bucle ingenuo entra en loop infinito. Sugirió leer con `nextLine()` y parsear
con `Double.parseDouble()` dentro de un `try/catch`, porque `nextLine()` sí
consume el contenido completo de la línea.

**Qué hice con eso:**
Adopté el patrón `nextLine` + `parseDouble` y lo encapsulé en
`leerDoublePositivo(Scanner)`. Añadí dos detalles propios:
1. `replace(',', '.')` para aceptar la coma decimal que se usa en español.
2. Validación adicional de que el valor sea estrictamente positivo (un peso
   o altura ≤ 0 no tienen sentido), reintentando la lectura.

**Qué aprendí:**
Por qué `nextLine` se considera más seguro para entrada interactiva, y que
un `Scanner` deja "basura" en el buffer si fallan los `nextX` tipados. Esto
me va a salvar en prácticas futuras donde mezcle `nextInt()` con `nextLine()`.

---

## Intercambio 3 — Sobre `try-with-resources` con Scanner

**Mi prompt:**
> ¿Tiene sentido envolver `new Scanner(System.in)` en un `try-with-resources`?
> ¿No se cierra `System.in` y rompe algo?

**Resumen de la respuesta:**
Cerrar el `Scanner` *sí* cierra `System.in`, pero como el programa termina
inmediatamente después en este caso, no causa problemas. Mencionó que en
programas más largos donde necesite leer en distintos momentos, la práctica
correcta sería *no* cerrar el `Scanner` o usar uno solo de ámbito más amplio.

**Qué hice con eso:**
Lo dejé con `try-with-resources` porque para este programa de un solo flujo
no hay efecto secundario, y me sirve como evidencia temprana del patrón que
la Práctica 7 va a evaluar formalmente. Documenté en mi cabeza el matiz para
no caer en el error en programas más complejos.

**Qué aprendí:**
Que las "buenas prácticas" no son universales: cerrar recursos siempre es
bueno *en general*, pero `System.in` es un recurso compartido del proceso
y cerrarlo tiene consecuencias. Me sirvió como recordatorio de que conviene
cuestionar las recetas, no solo aplicarlas.

---

## Intercambio 4 — Formato de fecha en español

**Mi prompt:**
> Quiero imprimir la fecha actual en formato "lunes 9 de mayo de 2026, 14:30:00".
> ¿Es `SimpleDateFormat` o algo más nuevo?

**Resumen de la respuesta:**
Recomendó `java.time.LocalDateTime` + `DateTimeFormatter` (API moderna desde
Java 8) y mostró el patrón `EEEE d 'de' MMMM 'de' yyyy, HH:mm:ss` con un
`Locale("es", "MX")` para que los nombres de día y mes salgan en español.

**Qué hice con eso:**
Lo apliqué tal cual. Verifiqué que `EEEE` da el nombre completo del día y
que las comillas simples sirven para incluir literales como "de". Confirmé
ejecutando que con el locale correcto se obtiene "viernes 9 de mayo" en vez
de "Friday May 9".

**Qué aprendí:**
Que `SimpleDateFormat` es legacy y no es thread-safe. La API `java.time` es
inmutable, más limpia y es la que voy a usar todo el semestre.
