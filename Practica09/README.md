# Práctica 9 — Entrada/Salida (I/O) en Java

## Objetivo

Construir un sistema de gestión de archivos que demuestre las formas
principales de persistir y recuperar datos en Java: texto plano,
formato tabular CSV, serialización binaria y operaciones con
directorios. Implementar backups automáticos con timestamp, exportar a
un formato alternativo (Elemento de Decisión Propia) y exponer todo
mediante una interfaz de línea de comandos.

## Estructura

```
Practica09/
├── README.md
├── REFLEXION.md
├── BITACORA_IA.md
└── src/
    ├── Cliente.java              (implements Serializable, con serialVersionUID)
    ├── GestorArchivos.java       (texto plano, CSV, binario, directorios)
    ├── BackupManager.java        (backup con timestamp + listar + restaurar)
    ├── ReporteAlineado.java      (decision propia: formato alternativo al CSV)
    ├── DatosPrueba.java          (20 clientes realistas)
    ├── Main.java                 (CLI con argumentos)
    └── ArchivosTest.java         (17 pruebas unitarias)
```

En tiempo de ejecución, el programa crea las siguientes carpetas:

```
data/
├── clientes.csv                  (input/output CSV)
├── clientes.dat                  (serializado binario)
├── reporte.txt                   (reporte alineado)
├── operaciones.log               (log de operaciones)
└── backups/
    └── clientes_YYYY-MM-DD_HH-MM-SS.dat
```

## Compilación y ejecución

```bash
javac -d out src/*.java

# Inicializa data/ con 20 clientes
java -cp out Main init

# Genera reporte alineado en data/reporte.txt
java -cp out Main report

# Backup con timestamp en data/backups/
java -cp out Main backup

# Ver backups disponibles
java -cp out Main list-backups

# Ver ayuda completa
java -cp out Main help

# Correr pruebas
java -cp out ArchivosTest
```

## Las tres formas de persistencia

### 1. Texto plano (`escribirLineaTexto` / `leerLineasTexto`)

Para logs y archivos de configuración simples. `BufferedWriter` con
modo append (`new FileWriter(archivo, true)`) permite agregar líneas
sin reescribir el archivo entero. `BufferedReader.readLine()` lee
línea por línea sin cargar todo el archivo en memoria.

### 2. CSV (`escribirCSV` / `leerCSV`)

Para intercambio con sistemas externos (Excel, bases de datos, otras
aplicaciones). Mi parser básico:

- Detecta nombres con coma y los envuelve en comillas dobles
  al escribir.
- Al leer, respeta las comillas dobles para no partir mal un nombre
  que las contiene.
- Salta líneas corruptas individualmente (no aborta la lectura entera)
  y reporta a stderr cuál fue.

Ejemplo del CSV generado:

```
id,nombre,email,fechaRegistro,pesoKg,tipo,activo
1001,Ana Gabriela Perez Soto,ana.perez@correo.mx,2024-11-05,62.5,PREMIUM,true
1002,Bruno Hernandez Lara,bruno.h@correo.mx,2025-01-12,78.0,BASICA,true
...
```

### 3. Binario serializado (`guardarBinario` / `cargarBinario`)

Para snapshot rápido del estado del sistema. `Cliente implements
Serializable` con `serialVersionUID = 1L`. La serialización preserva
**todo el estado** del objeto: tipos primitivos, enums, fechas, flags.
Es lo más fiel al estado en memoria, pero solo Java puede leerlo.

`BufferedOutputStream` y `BufferedInputStream` envolviendo a `FileOutputStream`/`FileInputStream`
aceleran la I/O agrupando bytes en bloques.

## Operaciones con directorios

`GestorArchivos.asegurarDirectorio()` usa `Files.createDirectories()`
del NIO moderno: crea estructuras anidadas (`data/backups/`) en una
sola llamada y es idempotente (no falla si ya existen).

`listarArchivos()` usa `Files.list()` dentro de un `try-with-resources`
(porque el `Stream` que devuelve es un recurso que también necesita
cerrarse).

## Backup automático con timestamp

```java
clientes.dat  →  clientes_2026-05-13_18-30-15.dat
```

El método `BackupManager.crearBackup()` toma el nombre original,
inserta el timestamp justo antes de la extensión, y copia con
`Files.copy(...)`. Usa `StandardCopyOption.REPLACE_EXISTING` para
manejar el caso (raro pero posible) de colisión en el mismo segundo.

Operaciones expuestas:
- `crearBackup(archivo)` — crea un backup nuevo.
- `listarBackups()` — lista todos los backups en el directorio.
- `restaurarBackup(nombre, destino)` — copia un backup sobre el archivo
  actual.

## Elemento de Decisión Propia — Reporte de texto alineado

Implementé un exportador a **reporte de texto con columnas alineadas**,
distinto del CSV. Ejemplo de la salida:

```
=============================================================================================
  GIMNASIO - REPORTE DE CLIENTES
  Generado:        2026-05-13 18:46:56
  Total registros: 20
=============================================================================================

ID    NOMBRE                           EMAIL                        TIPO     PESO    ESTADO
----- -------------------------------- ---------------------------- -------- ------- --------
1001  Ana Gabriela Perez Soto          ana.perez@correo.mx          PREMIUM   62.5   activo
1002  Bruno Hernandez Lara             bruno.h@correo.mx            BASICA    78.0   activo
...
1020  Tomas Espino Beltran             tomas.e@correo.mx            BASICA    77.0   activo

---------------------------------------------------------------------------------------------
  Totales por tipo:
    BASICA:  7
    PREMIUM: 8
    VIP:     5

  Activos:   17 / 20
=============================================================================================
```

### Caso de uso frente al CSV

| Aspecto | CSV | Reporte alineado |
|---|---|---|
| Lector objetivo | Otra aplicación (Excel, BD) | Humano (gerente, recepcionista) |
| Legibilidad en notepad | Mala (todo en una línea) | Excelente (cada cliente en su fila) |
| Importable a Excel | Sí, directo | No, requiere parseo manual |
| Conteo de totales | No incluye, hay que sumar | Incluido en el pie del reporte |
| Para imprimir/pegar en email | Inútil | Ideal |

**El CSV es para máquinas, el reporte alineado es para personas.**
Cada uno tiene su lugar:

- Cuando el gerente del gimnasio quiere ver "qué clientes tengo y de
  qué tipo" sin abrir Excel, el reporte alineado le da la respuesta
  de un vistazo.
- Cuando contabilidad necesita importar los datos a su sistema, el
  CSV es lo apropiado.

Detalles técnicos del reporte:
- Anchos de columna fijos calibrados a los datos reales.
- Padding con espacios para alinear; truncado si el campo excede.
- Cabecera con timestamp y total de registros.
- Pie con conteo por tipo de membresía y total de activos.
- Separadores con `=` y `-` para escanear visualmente.

## CLI con argumentos

`Main` se invoca con `java Main <comando> [argumentos]`. Comandos:

| Comando | Acción |
|---|---|
| `init` | Inicializa `data/` con 20 clientes (CSV + binario) |
| `load-csv` | Lee el CSV y muestra los clientes |
| `load-bin` | Lee el binario y muestra los clientes |
| `save-bin` | Convierte el CSV actual a binario |
| `backup` | Crea backup del binario con timestamp |
| `list-backups` | Lista los backups disponibles |
| `restore <nombre>` | Restaura un backup |
| `report` | Genera reporte alineado en `data/reporte.txt` |
| `log <mensaje>` | Agrega una línea al log de operaciones |
| `help` | Muestra la ayuda |

Si no se pasa comando, ejecuta `help`. Si el comando es desconocido,
imprime error a stderr, muestra ayuda y sale con código 1.

## Resultados de las pruebas

```
=== Resumen ===
Pasadas:  17
Falladas: 0
Total:    17
```

17 pruebas distribuidas en:
- Texto plano (1).
- CSV: round-trip, nombres con coma, líneas corruptas (3).
- Binario: round-trip, preservación de enums y flags (2).
- Directorios: creación anidada, listar (2).
- Backups: formato del nombre, restaurar, listar varios (3).
- Reporte alineado: cabecera, contenido completo, totales (3).
- Timestamp: formato (1).
- Casos de error implícitos (corrupción, ausencia de archivo, etc).
