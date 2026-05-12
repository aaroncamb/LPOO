# Bitácora de IA — Práctica 5

Registro del uso de IA (Claude) como apoyo de pair programming durante esta práctica.

---

## Intercambio 1 — Reusar la jerarquía de P4 o crear una nueva

**Mi prompt:**
> P5 me pide una interfaz, una clase abstracta y 3 concretas con
> sobrecargas. Ya tengo en P4 la jerarquia Membresia/Estandar/VIP. ¿La
> reuso aqui o creo algo nuevo? Reusarla parece eficiente pero quizas
> mezcla conceptos.

**Resumen de la respuesta:**
La IA recomendo crear una jerarquia nueva por tres razones: (1) P5
explicitamente pide construir interfaz + abstracta + concretas, asi que
forzarlo sobre P4 me obligaria a "estirar" Membresia con metodos que
no le pertenecen; (2) en P6 voy a necesitar 3 interfaces mas que
combinare con las concretas, y aplicarlas sobre Membresia no encaja
bien (una membresia no es lo mismo que un servicio puntual); (3) en el
proyecto final GymPOS los clientes pueden pagar servicios sueltos sin
tener membresia, asi que Servicio es una pieza valiosa por si sola.

**Qué hice con eso:**
Acepte la recomendacion y arme la jerarquia nueva: Cobrable (interfaz),
Servicio (abstracta), ClaseGrupal/EntrenamientoPersonal/EvaluacionFisica
(concretas). Documente esta decision en el README porque sé que en
defensa oral van a preguntarme "¿por que dos jerarquias en el mismo
dominio?". La respuesta corta: porque modelan cosas distintas (un
contrato mensual vs una sesion puntual).

**Qué aprendí:**
Que la coherencia de dominio NO significa "todo bajo una sola raiz".
Significa que las piezas se relacionen entre si en el contexto del
negocio. Membresias y Servicios son dos piezas del mismo gimnasio,
no estan obligadas a heredar una de la otra.

---

## Intercambio 2 — Sobrecarga vs sobreescritura en la misma palabra

**Mi prompt:**
> aplicarDescuento(double) viene de la interfaz Cobrable. Si en
> ClaseGrupal le agrego aplicarDescuento(int) y aplicarDescuento(String),
> ¿la primera es override y las otras dos overload? ¿O las tres son
> overload? Me lio.

**Resumen de la respuesta:**
La IA me aclaro la distincion: `aplicarDescuento(double)` ES un override
porque la firma `double aplicarDescuento(double)` ya existe en Cobrable
y ClaseGrupal la reimplementa (heredada de Servicio en realidad, que
es donde esta el cuerpo). Las versiones `(int)` y `(String)` son
overloads porque agregan firmas nuevas que NO existian en el padre. La
JVM diferencia entre las tres por el tipo del parametro al momento de
compilar la llamada.

Tambien me advirtio de una trampa: si declaro `aplicarDescuento(double)`
en la hija pero con nombre exacto y `@Override`, esta sobreescribiendo
de Cobrable, no sobrecargando. La sobrecarga requiere firma DISTINTA.

**Qué hice con eso:**
Refactore el codigo y la documentacion para nombrar correctamente cada
caso: en `Servicio.aplicarDescuento(double)` puse `@Override` (es override
del metodo de Cobrable). En `ClaseGrupal` NO sobrescribo ese metodo, lo
heredo del padre Servicio. Lo que agrego en ClaseGrupal son las firmas
`(int)` y `(String)`, que son overload puros (firmas nuevas). Total:
3 firmas distintas conviven en ClaseGrupal, lo cual cumple el
"minimo 3 sobrecargas" de la consigna.

Documente la distincion en el README y en la REFLEXION porque sé que
es el tipo de pregunta tramposa que puede caer en defensa oral.

**Qué aprendí:**
Que "sobrecarga" en lenguaje coloquial a veces se usa para describir
cualquier metodo con el mismo nombre, pero tecnicamente sobrecargar es
agregar firmas NUEVAS, no reimplementar. Override es una relacion
vertical (entre padre e hijo); overload es una relacion horizontal
(dentro de la misma clase).

---

## Intercambio 3 — Template Method, ¿como diseñarlo?

**Mi prompt:**
> La consigna pide aplicar Template Method como decision propia: un
> metodo concreto en la clase abstracta que llame a los abstractos. ¿Que
> deberia hacer ese metodo en mi caso? ¿Cualquier metodo cuenta o tiene
> que tener una logica especifica?

**Resumen de la respuesta:**
La IA explico que Template Method tiene una estructura tipica:
  1. Un metodo concreto en el padre define una SECUENCIA fija de pasos.
  2. Algunos pasos son comunes a todas las hijas (se implementan en el
     padre).
  3. Otros pasos varian (se declaran abstractos para que cada hija
     implemente).
  4. El padre llama a esos pasos en orden, las hijas no tienen que
     preocuparse del flujo, solo de su parte.

Sugirio modelar `procesarVenta()` con cuatro pasos: validar, calcular
total, registrar, emitir comprobante. Mencionó que era valioso marcar
el metodo `final` para evitar que una hija accidentalmente rompiera la
secuencia sobreescribiendo el flujo.

**Qué hice con eso:**
Implemente exactamente eso. `procesarVenta()` es `final` en Servicio y
llama en orden: validarCliente (abstracto), calcularTotal (concreto
heredado de Cobrable), registrarEnBitacora (privado, comun a todas),
emitirComprobante (abstracto). Las hijas solo implementan
validarCliente y emitirComprobante. Documente bien en el README por
que esto es superior a dejar que cada hija haga su propio flujo.

**Qué aprendí:**
Que Template Method no es solo "un metodo que llama otros". Es un patron
explicito sobre QUIEN controla el flujo: el padre lo controla y las
hijas rellenan piezas. Esto invierte el control (el padre llama a las
hijas, no las hijas al padre) y se le llama "principio de Hollywood":
"don't call us, we'll call you". Es una idea poderosa que aparece en
muchos frameworks (Spring, JUnit).

---

## Intercambio 4 — EvaluacionFisica exenta de IVA, ¿override del metodo concreto?

**Mi prompt:**
> Mi EvaluacionFisica no debe cobrar IVA porque es servicio de salud.
> Pero calcularImpuestos() esta IMPLEMENTADO en Servicio (devuelve
> subtotal * 0.16). ¿Sobrescribir un metodo concreto del padre es legal?
> ¿Es buena practica?

**Resumen de la respuesta:**
La IA confirmo que si, es totalmente legal sobreescribir metodos
concretos del padre, no solo los abstractos. Y mientras la sobreescritura
respete el principio de sustitucion (la hija sigue cumpliendo el
contrato), es buena practica. En mi caso EvaluacionFisica sigue siendo
Cobrable porque devuelve un numero valido (0.0); simplemente su regla
es distinta. Esto se llama "override de comportamiento por defecto"
y es uno de los usos legitimos de la herencia.

Sugirio agregar un comentario en el codigo explicando POR QUE
EvaluacionFisica tiene esa regla, para evitar que un futuro mantenedor
piense que es un bug.

**Qué hice con eso:**
Agregue el override de `calcularImpuestos()` en EvaluacionFisica con
un comentario que explica la regla del negocio (servicios de salud
exentos). Tambien lo cubri con una prueba unitaria especifica
(`pruebaEvaluacionExentaDeIVA`) para que cualquier cambio futuro que
quiebre esto se detecte.

**Qué aprendí:**
Que la herencia no es solo "agregar metodos" sino tambien "ajustar el
comportamiento heredado". Sobrescribir un metodo concreto es legitimo
cuando la hija tiene una variante razonable de la regla general. Lo
que importa es no romper el contrato (la hija sigue haciendo lo que
el padre prometia, solo con un calculo distinto).

---

## Intercambio 5 — instanceof con pattern matching, ¿esta bien usarlo?

**Mi prompt:**
> En P4 ya use `if (m instanceof MembresiaPremium prem)`. ¿Sigue siendo
> valido en P5, o el evaluador podria considerarlo "feature avanzada que
> no se ha visto"?

**Resumen de la respuesta:**
La IA dijo que pattern matching para instanceof es features ESTABLE
desde Java 16 (2021), no preview ni experimental. Cualquier Java
moderno lo soporta. La unica razon para evitarlo seria si la consigna
explicitamente pidiera la sintaxis vieja, lo cual no es el caso. Usar
la version moderna demuestra que entiendo Java actual.

Sugirio incluir AMBAS sintaxis en algun lugar del codigo (al menos en
un comentario o en la REFLEXION) para mostrar que conozco la diferencia
y eleji la moderna a proposito.

**Qué hice con eso:**
Uso el pattern matching moderno en todo el Main de P5. En la REFLEXION
incluyo un bloque con la sintaxis antigua al lado de la nueva para
contrastar, lo cual deja claro que conozco las dos. Es una pequeña
señal de control sobre el lenguaje que vale en defensa oral.

**Qué aprendí:**
Que distinguir entre "feature estable" y "feature experimental" es util
para tomar decisiones de codigo. Java 21 tiene muchas features
modernas (records, sealed classes, switch expressions, pattern matching)
y vale la pena saber cuales son estables para usarlas con confianza.
