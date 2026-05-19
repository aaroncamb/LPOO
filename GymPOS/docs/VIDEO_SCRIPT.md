# Script del Video — GymPOS

**Duración objetivo:** 6-7 minutos (dentro del rango 5-8 pedido)
**Grabación:** OBS Studio con captura de pantalla + micrófono
**Resolución:** 1920×1080 (Full HD)
**Voz:** española neutral, tono profesional pero relajado

---

## Antes de grabar — Preparación (10 min)

### 1. Configurar OBS

- **Fuente principal**: "Display Capture" o "Captura de pantalla" del monitor donde tendrás IntelliJ y la app.
- **Fuente secundaria**: "Audio Input" para el micrófono. Habla 30 segundos como prueba y verifica el nivel (que no se sature en rojo).
- **Resolución de salida**: 1920×1080.
- **Bitrate**: 4000-6000 kbps es suficiente para captura de pantalla.
- **Formato**: MP4.

### 2. Preparar el entorno

- **Cerrar todas las aplicaciones no necesarias** (Slack, navegador con tabs, notificaciones).
- **Silenciar notificaciones de Windows** (modo "No molestar").
- **Tener abierto en la barra de tareas**:
  - IntelliJ con el proyecto LPOO.
  - El navegador con el repo de GitHub abierto en una pestaña.
  - El visor del archivo `MANUAL_TECNICO.md` (en VS Code o navegador con preview).
- **Borrar la carpeta `GymPOS/data/`** justo antes de grabar, para que la app arranque "limpia" y se siembre con datos de prueba (más impactante visualmente).

### 3. Ensayo previo

- **Lee el script completo dos veces** antes de grabar.
- **Haz una toma de prueba de 1 minuto** para verificar audio y video.
- **No tengas miedo de detener y volver a grabar** una sección si te equivocas. OBS permite grabar varias secciones y unirlas después.

---

## ESTRUCTURA DEL VIDEO

```
00:00 - 00:30   Introducción
00:30 - 01:30   Arquitectura y código
01:30 - 03:00   Demo: módulo Clientes
03:00 - 04:30   Demo: Membresías y cobro (incluyendo error)
04:30 - 05:30   Demo: Clases grupales y Reportes (multithreading)
05:30 - 06:30   Cierre con backup automático
06:30 - 07:00   Cierre del video
```

---

## GUION COMPLETO

> **Convenciones**:
> - **[ACCIÓN]** = lo que haces en pantalla (no lo dices).
> - **"Texto entre comillas"** = lo que dices en voz.
> - **(notas)** = recordatorios para ti, no se mencionan.

---

### 00:00 - 00:30 → INTRODUCCIÓN

**[ACCIÓN]** Pantalla del README.md de GymPOS abierto en VS Code o navegador.

> "Hola, soy César Aarón Mendoza Benavides, matrícula 1904833. Este es el video de presentación de **GymPOS**, mi proyecto integrador para el Laboratorio de Programación Orientada a Objetos."

> "GymPOS es un sistema de punto de venta para un gimnasio, construido en Java 21 con JavaFX. Integra todo el material visto en las once prácticas del curso: encapsulamiento, herencia, polimorfismo, interfaces, excepciones, colecciones, entrada/salida, concurrencia e interfaz gráfica."

> "El proyecto tiene 35 clases organizadas en 9 packages con arquitectura MVC, supera holgadamente el mínimo de 15 clases que pide la rúbrica."

**[ACCIÓN]** Scroll lento por el README para mostrar la tabla de cumplimiento.

---

### 00:30 - 01:30 → ARQUITECTURA Y CÓDIGO

**[ACCIÓN]** Cambiar a IntelliJ. Mostrar el árbol de carpetas del proyecto GymPOS expandido.

> "Veamos rápidamente la estructura. El package **model** contiene las clases del dominio: Cliente, la jerarquía abstracta de Membresía con sus tres hijas Básica, Premium y VIP, ClaseGrupal, Equipo y RegistroAcceso."

**[ACCIÓN]** Click en `model/Cliente.java`, mostrar las primeras líneas.

> "Cliente usa **JavaFX Property** para que la tabla sea reactiva, combinado con **serialización personalizada** porque las Property no son Serializable por defecto. Los métodos writeObject y readObject escriben y reconstruyen las Property manualmente."

**[ACCIÓN]** Scroll hasta encontrar `writeObject`, mostrarlo unos segundos.

**[ACCIÓN]** Click en `model/Membresia.java`.

> "Membresía es una clase abstracta con métodos abstractos. La VIP hereda directo de Membresía porque es **anual**, mientras que Básica y Premium son mensuales. Esto demuestra herencia y polimorfismo de la P4."

**[ACCIÓN]** Click en `exceptions/PagoRechazadoException.java`.

> "Las excepciones forman una jerarquía. **PagoRechazadoException** lleva contexto rico: monto, método de pago, código de error y una referencia única. Su método toString emite JSON, lo que la hace procesable por herramientas de monitoreo."

**[ACCIÓN]** Click en `concurrency/TareaReporte.java`.

> "Para multithreading uso `javafx.concurrent.Task`. Los reportes y backups se ejecutan en hilo de fondo, dejando la UI libre para responder. Lo vemos en acción en un momento."

---

### 01:30 - 03:00 → DEMO: MÓDULO CLIENTES

**[ACCIÓN]** En el panel Maven de IntelliJ, doble-click en `javafx:run`. Esperar a que abra la app.

> "Voy a ejecutar la aplicación con `mvn javafx:run`. La primera vez tarda unos segundos porque carga JavaFX."

**[ACCIÓN]** Cuando abre la app, mostrarla unos 2 segundos completa.

> "Esta es GymPOS. En la primera ejecución, el sistema se siembra con 20 clientes de prueba, 8 clases grupales y 12 equipos."

> "Tengo cuatro pestañas: Clientes, Membresías, Clases Grupales y Reportes."

**[ACCIÓN]** Estar en la pestaña Clientes. Click en encabezado "Nombre" para ordenar.

> "Las columnas son ordenables. Click en cualquier encabezado."

**[ACCIÓN]** Escribir "ana" en el campo de búsqueda. Mostrar el filtrado en vivo.

> "El campo de búsqueda filtra en tiempo real por nombre o email."

**[ACCIÓN]** Borrar el campo. Click en botón "Nuevo".

> "Voy a registrar un cliente nuevo."

**[ACCIÓN]** En el formulario, escribir:
- Id: `1099`
- Nombre: `Maria del Carmen Ramirez`
- Email empezando con `maria@` (sin completar todavía).

**[ACCIÓN]** Detenerse en el email mal formado para mostrar el borde rojo.

> "Observa la **validación de email en tiempo real**. El borde se pone rojo cuando el formato es inválido, y aparece un tooltip explicando el problema."

**[ACCIÓN]** Completar el email a `maria.r@correo.mx`. El borde se pone verde.

> "Cuando es válido, se pone verde."

**[ACCIÓN]** Completar el resto: peso 60, tipo PREMIUM.

> "El botón Guardar se habilita solo cuando todos los campos son válidos. Esto se logra con listeners en las propiedades de los campos."

**[ACCIÓN]** Click en Guardar. Mostrar que el cliente aparece en la tabla.

---

### 03:00 - 04:30 → MEMBRESÍAS Y COBRO (con error)

**[ACCIÓN]** Click en pestaña "Membresías".

> "Pestaña Membresías. Cada fila muestra el cliente, su plan, fechas, días restantes y el estado: vigente, por vencer o vencida."

**[ACCIÓN]** Doble-click en alguna membresía cualquiera (por ejemplo la primera).

> "Doble-click sobre una membresía abre el **diálogo de cobro**."

**[ACCIÓN]** Mostrar el diálogo de cobro. Mover el slider de descuento lentamente.

> "El diálogo muestra los datos del cliente. Selecciono método de pago y arrastro el slider de descuento. Observen el **desglose en vivo**: subtotal, descuento, IVA y total se recalculan automáticamente."

**[ACCIÓN]** Dejar el slider en ~10% de descuento. Click en "Cobrar".

> "Click en Cobrar."

**[ACCIÓN]** Si el cobro es **exitoso**: Se cierra el diálogo, aparece confirmación.

> "Cobro exitoso. La membresía se renueva con nueva fecha de vencimiento y el cliente acumula puntos por el monto pagado más 50 puntos bonus por renovación."

**[ACCIÓN]** Si el cobro es **rechazado** (1 de cada 10): Aparece el Alert de error.

> "El sistema simula un 10% de fallos aleatorios para mostrar el manejo de la **excepción PagoRechazadoException**. Observen el contexto rico: código de error, referencia única, monto y método. Esta referencia se le da al cliente si necesita aclarar con su banco."

**[ACCIÓN — IMPORTANTE]** Hagas lo que hagas en esta sección, **intenta cobrar varias veces** (3-4) hasta que te toque al menos un error. Ese error es el "momento culminante" del módulo de pagos. Si nunca toca error en la grabación, puedes mencionarlo verbalmente:

> "El sistema está diseñado para mostrar el error si el banco rechaza el cobro. Como es aleatorio, en esta toma no apareció, pero está en el código y en la documentación."

---

### 04:30 - 05:30 → CLASES GRUPALES Y REPORTES

**[ACCIÓN]** Click en pestaña "Clases Grupales".

> "Pestaña Clases Grupales. Tengo 8 clases programadas con su instructor, horario, cupo actual y precio."

**[ACCIÓN]** Seleccionar una clase. Click en "Inscribir cliente".

**[ACCIÓN]** En el selector de clientes, elegir cualquiera (María del Carmen, por ejemplo). Confirmar.

> "Inscribo a María del Carmen en Yoga matutino. El cupo se actualiza."

**[ACCIÓN]** Click en pestaña "Reportes".

> "Pestaña Reportes. Aquí está el **multithreading** funcionando."

**[ACCIÓN]** Click en "Reporte general".

> "Click en Reporte general. Observen la **barra de progreso**. El reporte se genera en un hilo de fondo, así que si fuera con miles de registros, la UI seguiría respondiendo."

**[ACCIÓN]** Cuando termine, mostrar la vista previa del TXT en el TextArea.

> "El reporte se genera con columnas alineadas, estilo P9. Incluye cabecera con el nombre del gimnasio y timestamp, secciones de clientes, membresías por vencer, vencidas, ingresos y accesos."

**[ACCIÓN]** Scroll hacia abajo en el TextArea para mostrar el contenido.

> "El archivo queda guardado en `data/reportes/` con timestamp en el nombre. Cada generación produce un archivo nuevo."

**[ACCIÓN — OPCIONAL]** Click en "Abrir último reporte" para que se abra Notepad.

> "Puedo abrirlo en Notepad para imprimirlo o compartirlo."

**[ACCIÓN]** Cerrar Notepad rápido y volver a GymPOS.

---

### 05:30 - 06:30 → CIERRE CON BACKUP AUTOMÁTICO

**[ACCIÓN]** Click en menú "Archivo" para mostrar las opciones.

> "El menú Archivo tiene Guardar todo, Crear backup ahora, y Salir, todos con atajos de teclado."

**[ACCIÓN]** Cerrar el menú. Click en el botón X de la ventana.

> "Al cerrar la aplicación, GymPOS hace **backup automático** de los archivos de datos."

**[ACCIÓN]** Aparece la ventana de progreso. Mostrar la barra avanzando.

> "Observen la ventana modal de progreso. La operación se ejecuta en un hilo daemon usando TareaBackup, que extiende javafx.concurrent.Task."

**[ACCIÓN]** Cuando termina, la app se cierra.

> "La aplicación se cierra solo después de que el backup termina."

**[ACCIÓN]** Cambiar al explorador de archivos. Navegar a `GymPOS/data/backups/`.

> "Aquí están los backups con timestamp en el nombre. Cinco archivos: clientes, membresías, accesos, clases y equipos."

**[ACCIÓN]** Mostrar los archivos con sus timestamps.

---

### 06:30 - 07:00 → CIERRE DEL VIDEO

**[ACCIÓN]** Volver a IntelliJ. Mostrar el README.md o el manual técnico.

> "GymPOS cumple con todos los entregables: arquitectura MVC con 35 clases, persistencia binaria, multithreading, jerarquía de excepciones, UI funcional, JAR ejecutable, manual técnico de 14 secciones con 6 diagramas UML, manual de usuario con casos de uso, y este video."

> "Todo el código y la documentación están en mi repositorio de GitHub. Gracias por su atención."

**[ACCIÓN]** Fundido a negro o pantalla del repo de GitHub por 2-3 segundos.

**[FIN DE GRABACIÓN]**

---

## Consejos para la grabación

### 1. Ritmo y velocidad

- **Habla un poco más lento de lo normal**. Lo que en una conversación se siente natural, en un video suena rápido.
- **Pausa 1 segundo después de cada acción visual importante**. Le das tiempo al espectador a procesar lo que ve.

### 2. Si te equivocas

- **No reinicies todo el video**. OBS te deja grabar en secciones.
- Si te trabas en una sección, **detén la grabación, respira, y graba esa sección de nuevo**. Después las unes en cualquier editor de video (Clipchamp viene con Windows 11).
- **No menciones la equivocación**. El espectador no se enterará.

### 3. Si se ve raro

- Si la app no abre por algún motivo, **no entres en pánico**. Detén la grabación, soluciona, regraba la sección.
- Si te toca el cobro exitoso 5 veces seguidas (estadística mala) y quieres mostrar el error, **ejecuta cobros varias veces antes de grabar para "preparar el terreno"**.

### 4. Cosas a evitar

- **NO menciones que usaste IA** en el video. Eso va en la BITACORA_IA.md (documento separado). El video es para mostrar EL PRODUCTO.
- **NO leas el script palabra por palabra**. Úsalo como guía pero suena natural.
- **NO uses muletillas excesivas** ("este...", "o sea...", "como que..."). Pausa en su lugar.
- **NO te disculpes en cámara** ("perdón si me equivoco", "espero que se vea bien"). Suena inseguro.

### 5. Cosas que suman puntos

- **Mostrar código brevemente** mientras explicas qué hace. Da credibilidad.
- **Usar terminología correcta**: "polimorfismo dinámico", "binding", "thread-safe", "Template Method". Demuestra que sabes.
- **Hablar con confianza**, especialmente al explicar decisiones de diseño ("opté por esto porque...").
- **Mencionar reutilización de prácticas**: "esto es el patrón de la P11", "esto reutiliza la idea de P9". Demuestra integración.

---

## Después de grabar

### Edición mínima

Si grabaste todo en una sola toma:
1. Recorta el inicio y final muertos.
2. Verifica el audio con audífonos antes de exportar.

Si grabaste por secciones:
1. Únelas en Clipchamp o cualquier editor.
2. Agrega transiciones suaves (corte simple o disolver de 0.3s).
3. Exporta a MP4 1080p.

### Subir a YouTube/Drive

- **Privacidad**: configurar como "No listado" si lo subes a YouTube (solo quien tenga el link lo ve).
- **Drive**: subir directo a tu Google Drive y compartir con el profesor con permiso de "Cualquiera con el enlace puede ver".

---

## Checklist final del video

- [ ] Duración entre 5 y 8 minutos (no más, no menos).
- [ ] Resolución 1080p o superior.
- [ ] Audio claro y sin saturación.
- [ ] Se ve claramente la pantalla (no hay zoom 50%).
- [ ] Se muestran las 4 pestañas funcionando.
- [ ] Se muestra el código brevemente (Cliente, Membresia, una excepción).
- [ ] Se muestra el cobro exitoso (o intentos hasta que aparezca el error).
- [ ] Se muestra el reporte generándose con progreso.
- [ ] Se muestra el backup automático al cerrar.
- [ ] Se menciona el cumplimiento de la rúbrica al final.
- [ ] Se da nombre completo y matrícula al inicio.
