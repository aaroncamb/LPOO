# Bitácora de IA — Práctica 7

Registro del uso de IA (Claude) como apoyo de pair programming durante esta práctica.

---

## Intercambio 1 — Checked vs unchecked, ¿cual para cada caso?

**Mi prompt:**
> Tengo que crear 4 excepciones para el gimnasio: pago rechazado, cupo
> excedido, membresia vencida, y datos invalidos. ¿Hago las 4 checked,
> las 4 unchecked, o mezclo? La consigna acepta cualquiera pero quiero
> elegir bien.

**Resumen de la respuesta:**
La IA argumento por la mezcla: las primeras tres son "vida real" (un
banco que dice no, una clase llena, un cliente que dejo expirar su
membresia) y el sistema DEBE poder reaccionar a esos casos, asi que
checked. La cuarta, "datos invalidos", representa un BUG del codigo
que llama: si paso un monto negativo, no es un caso de negocio, es un
error del programador. Forzar try/catch en todo el codigo cliente por
un caso que NUNCA deberia ocurrir solo agrega ruido. Unchecked es lo
correcto ahi.

Tambien menciono que esta decision tiene un fuerte componente de
estilo y que en proyectos modernos (Spring, frameworks reactivos) se
prefiere unchecked para todo. Pero en cursos academicos donde la
rubrica evalua manejo explicito, checked sigue siendo la opcion
tradicional.

**Qué hice con eso:**
Implemente la mezcla: GymException (abstracta, checked) como padre de
las 3 de negocio; EntradaInvalidaException afuera, heredando de
RuntimeException. Esto tambien me dio material rico para la
REFLEXION pregunta 1.

**Qué aprendí:**
Que la decision checked/unchecked no es tecnica sino de intencion:
"¿quiero obligar al llamador a pensar en este error, o asumo que es
un bug que el llamador no causaria deliberadamente?". La respuesta
depende del tipo de error y del estilo del proyecto.

---

## Intercambio 2 — Como hacer una excepcion "rica" sin abusar

**Mi prompt:**
> El elemento de decision propia pide una excepcion con info de contexto
> adicional. Estoy tentado a meterle 10 campos a PagoRechazadoException
> pero no se cuanto es demasiado. ¿Como decido que informacion incluir?

**Resumen de la respuesta:**
La IA propuso pensar en el caso de uso real: imaginar que un agente de
soporte recibe un ticket "no pude pagar" y se sienta a investigar. ¿Que
informacion necesitaria para no tener que pedirle al cliente que repita
datos? Cada campo debe responder a una pregunta concreta del soporte.

Sugirio 5 campos: monto (¿cuanto?), metodo (¿como pago?), codigo
machine-readable (¿que tipo de error?), metodo origen (¿en que parte
del sistema?), y una referencia corta (¿como rastrear?). Mas alla de
eso, ya es over-engineering: un timestamp lo aporta la superclase, y
detalles del cliente se buscan por su id.

**Qué hice con eso:**
Implemente exactamente los 5 campos sugeridos. Agregue el toString()
estilo JSON porque el caso de uso final ("pegar el error en un ticket
o consumirlo desde un dashboard") justifica el formato estructurado.
Documente cada campo en el README con su "para que" especifico.

**Qué aprendí:**
Que para que una excepcion rica sea util de verdad, hay que poder
articular EXACTAMENTE como cada campo ayudaria al soporte. Si no
puedo justificar un campo, sobra. Esta validacion de "para que sirve"
es lo que separa una excepcion bien diseñada de un objeto inflado de
metadatos.

---

## Intercambio 3 — Warning de serialVersionUID, ¿lo ignoro?

**Mi prompt:**
> Compilo con -Xlint:all y me salen warnings de "serializable class has
> no definition of serialVersionUID" en mis excepciones. ¿Importa? Las
> excepciones que vi en codigo profesional muchas veces no lo tienen.

**Resumen de la respuesta:**
La IA explico que el warning aparece porque Exception (y RuntimeException)
implementan Serializable. Java pide que toda clase serializable tenga
un serialVersionUID porque sin el, si la clase cambia entre la
serializacion y la deserializacion, se rompen los objetos serializados
silenciosamente.

En la practica, NUNCA se serializan excepciones de runtime, asi que
el riesgo es 0. Pero el warning sigue ahi para mantener consistencia.
Hay tres opciones: (1) suprimir el warning con @SuppressWarnings, (2)
agregar el serialVersionUID = 1L, (3) ignorarlo.

Recomendo la (2) porque es una linea y es lo que se ve en codigo
profesional bien mantenido (frameworks de prod como Spring siempre lo
incluyen, aunque sea ceremonial).

**Qué hice con eso:**
Agregue `private static final long serialVersionUID = 1L;` a las 4
excepciones. Tambien marque `valorRecibido` como `transient` en
EntradaInvalidaException porque era Object (no Serializable garantizado).
Esto deja compilacion limpia con -Xlint:all, lo cual es señal de
cuidado profesional.

**Qué aprendí:**
Que los warnings del compilador casi siempre apuntan a algo real,
aunque a veces sea ceremonial. Tomarse el tiempo de entender QUE
problema previene cada warning antes de suprimirlo es buena disciplina.
En este caso aprendi que Exception es serializable por diseño (para
poder pasarla entre procesos en RMI y otros mecanismos antiguos), y
serialVersionUID es la garantia de compatibilidad entre versiones.

---

## Intercambio 4 — Try-with-resources con FileWriter en modo append

**Mi prompt:**
> Quiero que el log se vaya acumulando, no que se sobreescriba en cada
> llamada. ¿Como uso try-with-resources con FileWriter en modo append?
> Y ¿que pasa si el directorio "logs/" no existe?

**Resumen de la respuesta:**
La IA mostro que FileWriter tiene un constructor de dos argumentos:
`new FileWriter(path, true)` donde el segundo es "append". Si lo
combino con BufferedWriter envolviendo, queda:

```java
try (BufferedWriter w = new BufferedWriter(new FileWriter(path, true))) {
    w.write(linea);
}
```

Sobre el directorio: si "logs/" no existe, FileWriter lanza
FileNotFoundException. Hay dos opciones: (1) asegurar el directorio en
el constructor del logger usando Files.createDirectories, o (2)
documentar que el usuario debe crear el directorio antes. Recomendo
(2) para no agregar magia, y mencionarlo en el README.

**Qué hice con eso:**
Use append=true en FileWriter para que el log crezca. En el README
dejo instrucciones claras: `mkdir -p logs` antes de ejecutar. Tambien
en el catch del logger, si la escritura falla por cualquier razon,
caigo a stderr y sigo: un fallo de logging NO debe tumbar el sistema
de negocio (esta es una regla de oro en sistemas de produccion).

**Qué aprendí:**
Que el logging defensivo (no dejar que fallos de log tumben el sistema)
es una regla importante en produccion. Si el disco esta lleno y no se
puede escribir, lo peor que puede pasar es que perdamos visibilidad
operativa; lo mejor es que el sistema siga sirviendo a los clientes.

---

## Intercambio 5 — Logger debe propagar la IOException o tragarsela?

**Mi prompt:**
> En mi GymLogger, si la escritura al archivo falla por IO, ¿propago
> la excepcion al codigo que llamo al log? ¿O la atrapo en silencio?

**Resumen de la respuesta:**
La IA dijo que es una decision importante con razonamiento claro: el
codigo que llama al logger esta tratando con la logica de negocio
(procesar un pago, inscribir un cliente). Si propago una IOException
desde el log, esa IOException llega al codigo de negocio que no tiene
manera razonable de manejarla (¿deberia rechazar el pago porque el log
fallo? eso es absurdo).

Recomendacion: el logger atrapa la IOException, la imprime a stderr
para que el operador la vea, y devuelve normalmente. El codigo de
negocio sigue funcionando. Esto se llama "fail soft" para el logging.

Hubo una distincion adicional: si el log es CRITICO para auditoria
financiera o de seguridad (no nuestro caso), la decision podria
invertirse: mejor fallar el pago que perder el registro. Pero para
nuestro caso, perder una linea de log es menos malo que rechazar un
cliente.

**Qué hice con eso:**
Adopte fail-soft: el logger atrapa la IOException, la imprime a stderr
y sigue. Esto deja el sistema robusto: incluso si el disco se llena y
no se puede escribir, los flujos de negocio siguen procesandose.
Documente esta decision en un comentario dentro del logger.

**Qué aprendí:**
Que el manejo de excepciones tiene una dimension de "criticidad": no
todas las fallas son iguales. Un fallo en el flujo de negocio amerita
propagar la excepcion; un fallo en logging amerita capturarla y
seguir. Distinguir entre estos casos es lo que separa codigo robusto
de codigo fragil.
