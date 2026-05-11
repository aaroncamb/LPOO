# Bitácora de IA — Práctica 2

Registro del uso de IA (Claude) como apoyo de pair programming durante esta práctica.

---

## Intercambio 1 — Cómo elegir tres constructores con sentido

**Mi prompt:**
> La práctica me pide tener al menos 3 constructores diferentes en mi clase
> Cliente. ¿Tres constructores cualquiera valen, o conviene que cada uno
> sirva para algo distinto? No quiero hacer tres versiones casi iguales
> nada más por cumplir.

**Resumen de la respuesta:**
La IA me explicó que los constructores deberían reflejar los casos de uso
reales en los que se crean objetos de esa clase. En vez de inventar
variaciones, propuso pensar en cada lugar del programa donde un Cliente
nacería: alta rápida en mostrador, importación desde archivo, formulario
con campos llenados uno por uno, etc. Así cada constructor responde a un
caso concreto.

**Qué hice con eso:**
Adopté ese enfoque y diseñé los tres constructores así:
- vacío → para llenar campo por campo
- mínimo (id, nombre, email) → alta rápida; resto se autocompleta
- completo → para reconstrucción desde archivo

Documenté esa lógica en el README porque me parece la parte más fuerte de
mi diseño. La IA inicialmente había propuesto un cuarto constructor con
solo (id, nombre); lo rechacé porque no encontré un caso real donde tener
nombre sin email tuviera sentido en mi dominio.

**Qué aprendí:**
Que la sobrecarga de constructores no es para "ofrecer opciones", es para
modelar con precisión los caminos por los que un objeto entra al sistema.
Esa diferencia me cambió el modo de pensar el diseño.

---

## Intercambio 2 — Optional vs null en buscarPorId

**Mi prompt:**
> Mi método buscarPorId puede no encontrar al cliente. ¿Devuelvo null,
> o hay una forma mejor en Java moderno?

**Resumen de la respuesta:**
La IA recomendó `Optional<Cliente>`. Argumentos: en la firma del método ya
es visible que el resultado puede no existir, así que el llamador no se
puede "olvidar" de manejar el caso. Mostró cómo usar `ifPresent`,
`ifPresentOrElse` y `orElse`. Mencionó que devolver null es legal pero
acumula NullPointerException cuando el código crece.

**Qué hice con eso:**
Lo adopté para `buscarPorId`. En `buscarPorNombre` opté por devolver una
`List` vacía en lugar de `Optional<List>` porque una lista vacía ya
comunica "no hay coincidencias" sin ambigüedad: nadie va a confundir una
lista vacía con un fallo. Esa diferencia entre los dos métodos me pareció
una decisión interesante de diseño y la documenté en el README.

**Qué aprendí:**
Que `Optional` resuelve un caso ("puede haber cero o uno"), pero no es la
respuesta para todo. Para cero-o-más, una colección vacía es más natural.
Devolver `Optional<List>` sería redundante.

---

## Intercambio 3 — Por qué sobrescribir equals y hashCode juntos

**Mi prompt:**
> Sobrescribí equals para que dos clientes con el mismo id sean iguales.
> El IDE me marcó que también debería sobrescribir hashCode. ¿Por qué?

**Resumen de la respuesta:**
La IA explicó el contrato de Object: si dos objetos son `equals`, sus
`hashCode` deben coincidir. Romper esto lleva a bugs cuando el objeto se
mete en estructuras basadas en hash (HashMap, HashSet): un objeto puede
"perderse" porque cae en un bucket distinto al que estaría su gemelo
lógico. No es opcional, es contractual.

**Qué hice con eso:**
Sobrescribí `hashCode()` usando `Objects.hash(id)`, alineado con que
`equals` también se basa solo en `id`. Probé en mi `Main` que dos clientes
con el mismo id pero datos distintos son `.equals()` aunque no sean
`==`, y dejé esa demostración impresa en la salida. En P8 (Colecciones)
voy a usar `HashMap<Integer, Cliente>` y necesito que esto esté bien
desde ahora.

**Qué aprendí:**
Que en Java hay contratos no documentados en la firma del método pero
sí en la documentación de la clase Object. Romperlos compila pero rompe
estructuras de datos en runtime. Esto es un patrón general: hay
"obligaciones" en los métodos heredados que el compilador no impone, pero
que sí afectan la corrección.

---

## Intercambio 4 — Validación de peso, ¿dónde la pongo?

**Mi prompt:**
> En actualizarPeso(double nuevoPeso), ¿debería validar que el valor sea
> positivo y razonable, o eso es para más adelante? La rúbrica de P2 no
> menciona validaciones, pero la de P3 sí.

**Resumen de la respuesta:**
Sugirió no adelantar validaciones a P2. Razones: (a) la P3 trata
explícitamente de eso, así que pondría todo el material clave en una sola
práctica; (b) si valido aquí, en P3 tendría poco que mostrar; (c) el
progreso entre prácticas debe ser visible. Sí recomendó documentar en el
README que la falta de validaciones es intencional, no descuido.

**Qué hice con eso:**
Dejé `actualizarPeso` sin validación y agregué esa nota en el README de
P2. En P3 voy a refactorizar a `setPesoKg` privado con validación de
rango (mínimo 30 kg, máximo 300 kg) y un mensaje descriptivo. Esto le da
al README de P3 un contraste claro: "antes era así, ahora es así, y por
esto importa el cambio".

**Qué aprendí:**
A planear el material en función del arco completo del semestre, no solo
de la práctica de hoy. Si meto todo en P2, P3 queda hueca. La progresión
honesta también es parte del diseño.

---

## Intercambio 5 — Sobre el quinto método de dominio

**Mi prompt:**
> Tengo cuatro métodos del dominio: mesesActivo, actualizarPeso, esVeterano
> y primerNombre. Necesito un quinto. ¿equals/hashCode/toString cuentan?

**Resumen de la respuesta:**
La IA argumentó que técnicamente sí son métodos de instancia, pero
`equals`, `hashCode` y `toString` son métodos heredados de `Object` que
estoy sobrescribiendo, no operaciones del dominio. Mejor agregar un
método más que tenga sentido para el negocio del gimnasio. Sugirió varias
opciones; la que más me gustó fue `registrarCambioPeso(double delta)`
que aplica un cambio relativo y devuelve el nuevo peso, útil cuando el
sistema solo conoce la diferencia entre sesiones.

**Qué hice con eso:**
Agregué `registrarCambioPeso(double delta)` como quinto método de dominio.
Tiene sentido: en un sistema real, las báscula a veces solo reportan el
delta respecto a la última pesada. Ahora tengo 5 métodos de dominio +
3 heredados sobrescritos.

**Qué aprendí:**
A leer la consigna con cuidado: no dice "cinco métodos", dice "cinco
métodos de instancia relacionados con operaciones del dominio". La
diferencia entre cumplir literal y cumplir el espíritu de la rúbrica.
