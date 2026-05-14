# Manual de usuario — GymPOS Clientes (P11)

Aplicación de escritorio para administrar el padrón de clientes de un
gimnasio: dar de alta, consultar, editar, eliminar, buscar y ordenar.

## 1. Instalación y primera ejecución

### Requisitos
- JDK 21 (LTS) instalado y en el PATH.
- 100 MB de espacio libre.
- Maven 3.8+ (solo para compilar; el JAR final no lo requiere).

### Pasos
1. Descomprimir el ZIP en una carpeta.
2. Abrir una terminal en esa carpeta.
3. Ejecutar:
   ```bash
   mvn clean package
   ```
4. La primera vez Maven descarga JavaFX (~30 MB). Esperar a que
   termine ("BUILD SUCCESS").
5. Ejecutar la aplicación:
   ```bash
   java -jar target/practica11-1.0.0.jar
   ```

## 2. Pantalla principal

Al abrir la aplicación verás una ventana dividida en tres zonas:

- **Barra de menú** arriba: Archivo / Clientes / Ayuda.
- **Toolbar de búsqueda** debajo del menú: campo "Buscar" + botones
  Nuevo / Editar / Eliminar.
- **Tabla central** con la lista de clientes (precarga 12 clientes de
  prueba al iniciar).
- **Barra de estado** abajo: muestra "Mostrando X de Y clientes".

![Pantalla principal](capturas/01-pantalla-principal.png)

*[Reemplazar con captura real una vez ejecutada]*

## 3. Casos de uso

### 3.1 Buscar / filtrar (interactivo)

Escribe en el campo "Buscar" de la toolbar. La tabla se actualiza
**en tiempo real**: solo se muestran los clientes cuyo nombre o
email contenga el texto buscado.

Por ejemplo:
- Escribir `ana` → muestra Ana Gabriela Perez Soto.
- Escribir `vip` → no funciona porque solo busca en nombre/email.
- Escribir `@correo.mx` → muestra todos.

Borrar el campo restaura la lista completa.

![Filtrado interactivo](capturas/04-filtrado.png)

### 3.2 Ordenar columnas

Click en cualquier encabezado de columna ordena la tabla por esa
columna (ascendente). Click otra vez invierte el orden.

Funciona también cuando hay un filtro activo: solo se ordenan los
resultados filtrados.

![Ordenar columnas](capturas/06-ordenar-columnas.png)

### 3.3 Crear nuevo cliente

Tres formas equivalentes:
- Click en botón "Nuevo" de la toolbar.
- Menú **Clientes → Nuevo cliente...**.
- Atajo **Ctrl+N**.

Se abre un diálogo modal con los campos. **El botón Guardar está
deshabilitado hasta que todos los campos sean válidos**:

- Id: debe ser un entero positivo único.
- Nombre: no puede estar vacío.
- Email: debe tener formato válido (el campo se pone **verde** cuando
  es correcto, **rojo** cuando no lo es, con un tooltip que explica
  el problema).
- Fecha de registro: obligatoria.
- Peso (kg): entre 30 y 300, o 0 si aún no se ha medido.
- Tipo de membresía: BASICA, PREMIUM o VIP.

![Formulario nuevo](capturas/02-formulario-nuevo.png)

![Validación de email](capturas/03-validacion-email.png)

Tras presionar Guardar (o **Enter** si todo es válido), el cliente
aparece en la tabla y queda seleccionado. Si presionas **Escape** o
"Cancelar", se descarta sin guardar.

### 3.4 Editar cliente existente

Cuatro formas:
- Doble-click sobre la fila del cliente.
- Seleccionar el cliente y presionar **Enter** sobre la tabla.
- Seleccionar y click en "Editar".
- Seleccionar y **Ctrl+E**.

El diálogo se abre **pre-llenado** con los datos actuales del cliente.
El campo Id queda bloqueado (no se debe cambiar el identificador de
un cliente existente). Modifica lo que necesites y Guarda; los cambios
se reflejan inmediatamente en la tabla.

### 3.5 Eliminar cliente

Tres formas:
- Seleccionar y presionar tecla **Delete**.
- Seleccionar y click en "Eliminar".
- Seleccionar y menú **Clientes → Eliminar seleccionado**.

Aparece un **diálogo de confirmación** con el nombre y email del
cliente a eliminar. Solo se elimina si confirmas con "Sí". El diálogo
se puede cancelar con "Cancelar" o Escape.

![Confirmar eliminación](capturas/05-confirmar-eliminar.png)

## 4. Atajos de teclado completos

| Atajo | Acción |
|---|---|
| **Ctrl+N** | Nuevo cliente |
| **Ctrl+E** | Editar seleccionado |
| **Delete** | Eliminar seleccionado |
| **F1** | Mostrar ayuda "Acerca de" |
| **Ctrl+Q** | Salir de la aplicación |
| Doble-click | Editar el cliente sobre el que se hizo click |
| **Enter** (en tabla) | Editar seleccionado |
| **Enter** (en formulario) | Guardar (si los campos son válidos) |
| **Escape** (en formulario) | Cancelar |

## 5. Resolución de problemas

### "JavaFX runtime components are missing"
Significa que se está ejecutando con `java` directo sin las
dependencias de JavaFX. Soluciones:
- Compilar con `mvn clean package` y usar el JAR generado.
- En IntelliJ, ejecutar con "Maven → javafx:run" del panel lateral.

### "Could not find or load main class App"
El classpath está mal. Asegurarse de:
- Estar en la carpeta `Practica11/`.
- Haber ejecutado `mvn clean package` antes.
- Usar la sintaxis exacta: `java -jar target/practica11-1.0.0.jar`.

### Los datos no persisten al cerrar
Esto es esperado: en P11 los datos viven solo en memoria. La
persistencia se integra en el proyecto final GymPOS reusando el
`GestorArchivos` de la P9.

## 6. Créditos

- **LPOO Práctica 11 — JavaFX**
- Stack: Java 21, JavaFX 21.0.2, Maven 3.8.
- Estilo visual: tema negro/dorado del gimnasio (definido en `styles.css`).
