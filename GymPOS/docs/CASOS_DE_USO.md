# Casos de Uso — GymPOS

Documento formal de casos de uso del sistema. Cada caso de uso describe una interacción significativa entre un actor (usuario del sistema) y GymPOS.

---

## Actores del sistema

| Actor | Descripción |
|---|---|
| **Recepcionista** | Personal del gimnasio que opera GymPOS en el día a día: da de alta clientes, cobra membresías, inscribe a clases. |
| **Gerente** | Genera reportes para análisis y decisiones estratégicas. |
| **Sistema** | Procesos automáticos: backup al cerrar, validación de vigencia. |

En este alcance del proyecto, los actores Recepcionista y Gerente operan la misma instancia de GymPOS sin restricciones de rol. Una mejora futura sería autenticación con permisos diferenciados.

---

## CU-01: Registrar nuevo cliente

**Actor principal**: Recepcionista
**Precondición**: GymPOS abierto en la pestaña Clientes
**Postcondición**: El cliente queda registrado en el sistema con una membresía asociada

### Flujo principal

1. El Recepcionista hace click en el botón **"Nuevo"**.
2. El Sistema muestra el formulario modal de alta.
3. El Recepcionista ingresa:
   - Id único positivo.
   - Nombre completo.
   - Email con formato válido.
   - Fecha de registro (por defecto, hoy).
   - Peso en kg (30-300).
   - Tipo de membresía (BASICA, PREMIUM o VIP).
4. El Sistema valida cada campo **en tiempo real** (mientras el Recepcionista escribe).
5. Cuando todos los campos son válidos, el Sistema habilita el botón **"Guardar"**.
6. El Recepcionista hace click en **"Guardar"** (o presiona Enter).
7. El Sistema registra al cliente en `GestionClientes`.
8. El Sistema crea automáticamente una membresía del tipo elegido en `SistemaMembresias`.
9. El Sistema cierra el diálogo y resalta al nuevo cliente en la tabla.

### Flujo alternativo: ID duplicado

- En el paso 7, si el id ya existe, el Sistema muestra un Alert *"Id duplicado: ya existe un cliente con id N"* y vuelve al paso 3 sin agregar nada.

### Flujo alternativo: Email duplicado

- En el paso 7, si el email está registrado por otro cliente, el Sistema muestra un Alert similar y vuelve al paso 3.

### Flujo alternativo: Cancelación

- En cualquier momento durante los pasos 3-6, el Recepcionista puede presionar **Escape** o click en **"Cancelar"**. El Sistema descarta los datos y cierra el diálogo sin cambios.

---

## CU-02: Buscar y filtrar clientes

**Actor principal**: Recepcionista
**Precondición**: GymPOS abierto en la pestaña Clientes con al menos un cliente registrado
**Postcondición**: La tabla muestra solo los clientes que coinciden con el criterio

### Flujo principal

1. El Recepcionista escribe un texto en el campo **"Buscar"**.
2. Con cada tecla, el Sistema filtra la tabla **en tiempo real**.
3. Solo se muestran los clientes cuyo nombre completo o email contiene el texto buscado (sin distinguir mayúsculas).
4. La barra de estado se actualiza: *"Mostrando X de Y clientes"*.
5. Opcionalmente, el Recepcionista hace click en el encabezado de cualquier columna para ordenar los resultados filtrados.
6. Para restaurar la lista completa, el Recepcionista borra el campo **"Buscar"**.

### Notas técnicas

El filtrado usa `FilteredList<Cliente>` de JavaFX. El ordenamiento usa `SortedList<Cliente>` con `comparatorProperty` vinculado a la tabla. Los dos se componen para mantenerse sincronizados automáticamente.

---

## CU-03: Renovar y cobrar una membresía

**Actor principal**: Recepcionista
**Precondición**: La pestaña Membresías muestra membresías
**Postcondición**: La membresía queda renovada con nueva fecha de vencimiento y se emite un ticket de cobro

### Flujo principal

1. El Recepcionista hace doble-click sobre la fila de la membresía a renovar.
2. El Sistema abre el diálogo de cobro mostrando:
   - Datos del cliente (nombre, email).
   - Método de pago (default: tarjeta).
   - Slider de descuento (default: 0%).
   - Desglose: subtotal, descuento, IVA, total.
3. *(Opcional)* El Recepcionista elige otro método de pago.
4. *(Opcional)* El Recepcionista arrastra el slider para aplicar descuento (0-30%). El desglose se actualiza en vivo.
5. El Recepcionista hace click en **"Cobrar"**.
6. El Sistema intenta el cobro a través de `ProcesadorPagos`.
7. Si el cobro es exitoso (90% de probabilidad simulado):
   - Se crea un `Ticket` y se registra.
   - Se acumulan puntos al cliente: `total × puntos.por.peso.pagado`.
   - Se renueva la membresía en `SistemaMembresias.renovar(idCliente)`.
   - El cliente recibe 50 puntos bonus por renovación.
   - El Sistema cierra el diálogo y refresca la tabla.
   - El Sistema muestra confirmación: *"Membresía renovada exitosamente"*.

### Flujo alternativo: Cobro rechazado

- En el paso 7, si el cobro falla (10% de probabilidad), `ProcesadorPagos` lanza `PagoRechazadoException`.
- El Sistema muestra un Alert de error con:
  - Mensaje del banco.
  - Código de error interno (`INSUF_FUNDS`, `TARJETA_VENCIDA`, `TIMEOUT_GATEWAY`).
  - Referencia única de la transacción (e.g. `PAY-A3F2E8`).
  - Monto y método intentados.
- El Recepcionista puede dar la referencia al cliente para seguimiento bancario.
- La membresía NO se renueva.

### Notas técnicas

El cálculo del total es: `(subtotal × (1 - descuentoPct)) × (1 + iva)`. El IVA viene de `config.properties` (default 16%).

---

## CU-04: Inscribir cliente a clase grupal

**Actor principal**: Recepcionista
**Precondición**: La pestaña Clases Grupales muestra clases programadas
**Postcondición**: El cliente queda inscrito en la clase si había cupo disponible

### Flujo principal

1. El Recepcionista selecciona la clase en la tabla.
2. El Recepcionista hace click en **"Inscribir cliente"**.
3. El Sistema abre un `ChoiceDialog` con la lista de clientes registrados.
4. El Recepcionista elige al cliente.
5. El Sistema invoca `ClaseGrupal.inscribir(idCliente)`:
   - Si la clase tiene cupo y el cliente no está ya inscrito: se agrega al `HashSet<Integer>` interno.
   - Si el cliente ya estaba inscrito: la operación se ignora silenciosamente.
6. El Sistema actualiza la tabla mostrando el nuevo cupo: `N+1/CUPOMAX`.
7. El Sistema muestra confirmación.

### Flujo alternativo: Cupo lleno

- En el paso 5, si la clase ya alcanzó su cupo máximo, `ClaseGrupal.inscribir()` lanza `CupoExcedidoException`.
- El Sistema muestra un Alert con el mensaje: *"Cupo excedido en '<nombre clase>': X/X inscritos."*
- El cliente NO se inscribe.

---

## CU-05: Generar reporte (en hilo de fondo)

**Actor principal**: Gerente
**Precondición**: GymPOS abierto en la pestaña Reportes
**Postcondición**: Se genera un archivo TXT con timestamp en `data/reportes/`

### Flujo principal

1. El Gerente hace click en uno de los tres botones:
   - **"Reporte general"**
   - **"Reporte de ingresos"**
   - **"Reporte de asistencia"**
2. El Sistema construye una `TareaReporte` con el tipo elegido.
3. El Sistema vincula:
   - `progressBar.progressProperty()` ← `tarea.progressProperty()`
   - `label.textProperty()` ← `tarea.messageProperty()`
4. El Sistema lanza la tarea en un hilo daemon (`Thread(tarea).start()`).
5. **La UI sigue respondiendo**: el Gerente puede cambiar de pestaña, abrir formularios, etc.
6. Internamente, `TareaReporte.call()` ejecuta en hilo de fondo:
   - `updateProgress(0, 100)`.
   - Llama a `GeneradorReportes.generarReporteX()`.
   - `updateProgress(80, 100)`.
   - Guarda el archivo en `data/reportes/`.
   - `updateProgress(100, 100)`.
7. Cuando termina, `setOnSucceeded()` se ejecuta DE VUELTA en el FXAT:
   - Se desbinden las propiedades.
   - El `TextArea` carga el contenido del archivo como vista previa.
   - La barra de estado se actualiza con la ruta del último reporte.

### Flujo alternativo: Fallo de I/O

- Si en el paso 6 falla la escritura del archivo (disco lleno, permiso denegado), `Task.setOnFailed()` se dispara.
- El Sistema muestra un Alert: *"No se pudo generar el reporte. Revisa el log."*

### Flujo opcional: Abrir el archivo

- Después de generar un reporte, el Gerente puede hacer click en **"Abrir último reporte"**.
- El Sistema invoca `Desktop.getDesktop().open(File)` para lanzar el visor del SO.

### Notas técnicas

Este caso de uso demuestra el **multithreading** que pide la rúbrica. La operación de generar reportes podría tomar segundos con datasets grandes; al ejecutarse en hilo de fondo, el JavaFX Application Thread (FXAT) no se bloquea y la UI no se congela.

---

## CU-06: Cerrar la aplicación con backup automático

**Actor principal**: Recepcionista
**Precondición**: GymPOS está abierto; `backup.automatico.al.cerrar=true` en config
**Postcondición**: Los datos quedan guardados y respaldados; la aplicación se cierra limpiamente

### Flujo principal

1. El Recepcionista cierra la ventana (click en X, `Alt+F4`, o `Ctrl+Q`).
2. El Sistema captura el evento `WINDOW_CLOSE_REQUEST` en `MainController.setOnCloseRequest`.
3. El Sistema persiste todos los datos en disco (`AppContext.guardarTodo()`).
4. El Sistema consume el evento (`e.consume()`) para pausar el cierre.
5. El Sistema construye una `TareaBackup` con los 5 archivos de datos.
6. El Sistema muestra una **ventana modal de progreso** con barra y mensaje.
7. La tarea se ejecuta en un hilo daemon:
   - Para cada archivo de datos: crea una copia en `data/backups/` con timestamp.
   - Actualiza el progreso y mensaje en cada iteración.
8. Cuando termina, `setOnSucceeded()`:
   - Cierra la ventana de progreso.
   - Cierra el stage principal (`stage.close()`).
   - La aplicación termina.

### Flujo alternativo: Backup deshabilitado

- Si en el paso 1 la configuración tiene `backup.automatico.al.cerrar=false`, los pasos 4-8 se omiten. La aplicación se cierra inmediatamente después de guardar.

### Flujo alternativo: Fallo de backup

- Si la tarea falla en el paso 7, se loguea el error pero **la aplicación se cierra de todos modos**. No queremos bloquear el cierre por un backup fallido.

---

## CU-07: Validar acceso por torniquete (concepto)

**Actor principal**: Sistema (invocado por el Recepcionista cuando un cliente intenta entrar)
**Precondición**: Cliente registrado en el sistema
**Postcondición**: Se permite o niega la entrada, y se registra el evento

### Flujo principal

1. El Recepcionista identifica al cliente que intenta entrar.
2. El Recepcionista invoca `ControlAcceso.registrarEntrada(cliente, torniquete)`.
3. El Sistema invoca `SistemaMembresias.verificarVigencia(idCliente, nombre)`.
4. Si la membresía está vigente:
   - El Sistema crea un `RegistroAcceso` con tipo ENTRADA y timestamp actual.
   - El registro se agrega a la lista.
   - La operación devuelve el `RegistroAcceso` creado.
5. El Recepcionista permite la entrada.

### Flujo alternativo: Membresía vencida

- Si en el paso 3 la membresía está vencida, `verificarVigencia()` lanza `MembresiaVencidaException`.
- La excepción lleva el nombre del cliente, fecha de vencimiento y días vencida.
- El Recepcionista informa al cliente que necesita renovar para entrar.
- Se puede invocar CU-03 (cobro) para renovar en el momento.

### Notas

Este caso de uso está implementado a nivel de servicio pero no tiene UI en este alcance (la pestaña dedicada al acceso se omitió por priorización pragmática). El concepto y la API quedan listos para una próxima iteración con torniquetes físicos vía hardware.

---

## Resumen de cobertura

| Funcionalidad de la consigna | Caso de uso |
|---|---|
| Registro de nuevas suscripciones | CU-01, CU-03 |
| Renovación de membresías | CU-03 |
| Sistema de puntos/recompensas | CU-03 (acumulación automática) |
| Calendario de clases grupales | CU-04 |
| Control de inventario de equipos | UI implementada, sin CU formal por simplicidad |
| Notificaciones por vencimiento | Visual en la pestaña Membresías (estado por color) |
| Generación de reportes | CU-05 |
| Persistencia de datos | CU-06 |
| Validación de acceso | CU-07 |
