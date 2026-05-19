# GymPOS — Sistema de Punto de Venta para Gimnasio

Proyecto Integrador — Laboratorio de Programación Orientada a Objetos
**César Aarón Mendoza Benavides** · Matrícula 1904833

---

## Resumen

GymPOS es una aplicación de escritorio JavaFX para administrar un gimnasio: clientes, membresías, cobros, clases grupales, control de acceso e inventario de equipos. Integra todo el material visto en las 11 prácticas del curso en una sola aplicación coherente y funcional.

**35 clases Java** organizadas en 9 packages MVC. Compila sin warnings con `-Xlint:all`. Persistencia binaria, multithreading para reportes y backups, jerarquía completa de excepciones, JAR ejecutable empacado con shade plugin.

---

## Ejecutar la aplicación

### Opción A: Desde IntelliJ IDEA

1. Abrir el directorio raíz del proyecto.
2. Click derecho sobre `pom.xml` → **"Add as Maven Project"**.
3. Esperar a que descargue las dependencias.
4. Panel **Maven** (lateral derecho) → `gympos` → `Plugins` → `javafx` → doble click en `javafx:run`.

### Opción B: Desde la terminal

```bash
cd GymPOS
mvn clean package
java -jar target/gympos-1.0.0.jar
```

### Opción C: Desarrollo

```bash
mvn javafx:run
```

---

## Documentación

Este proyecto tiene documentación extensa en la carpeta `docs/`:

| Documento | Contenido |
|---|---|
| [MANUAL_TECNICO.md](docs/MANUAL_TECNICO.md) | Arquitectura, decisiones de diseño, 6 diagramas UML, dependencias, limitaciones |
| [MANUAL_USUARIO.md](docs/MANUAL_USUARIO.md) | Guía paso a paso de cada pestaña con capturas |
| [CASOS_DE_USO.md](docs/CASOS_DE_USO.md) | 7 casos de uso formales con flujos principales y alternativos |
| [VIDEO_SCRIPT.md](docs/VIDEO_SCRIPT.md) | Guion del video de presentación de 5-8 minutos |
| [BITACORA_IA.md](docs/BITACORA_IA.md) | Registro del uso de IA como apoyo durante el desarrollo |
| `diagramas/*.puml` | Diagramas UML en PlantUML (arquitectura, jerarquías, secuencias) |

---

## Estructura del proyecto

```
GymPOS/
├── pom.xml                      Maven + JavaFX + shade plugin
├── config.properties            Configuración (IVA, rutas, precios)
├── README.md                    Este archivo
├── data/                        Datos persistidos en runtime
├── capturas/                    Screenshots para los manuales
├── docs/                        Documentación
└── src/main/
    ├── java/com/gympos/
    │   ├── App.java             Punto de entrada (Application)
    │   ├── model/               8 clases del dominio
    │   ├── view/                3 componentes UI reutilizables
    │   ├── controller/          6 controladores
    │   ├── service/             6 servicios de negocio
    │   ├── persistence/         3 clases de I/O
    │   ├── exceptions/          5 excepciones de dominio
    │   ├── concurrency/         2 tareas concurrentes
    │   └── util/                1 utilidad (Logger)
    └── resources/
        └── styles.css           Tema visual negro/dorado
```

---

## Cumplimiento de la rúbrica

| Requisito | Cumplimiento |
|---|---|
| Mínimo 15 clases en packages | **35 clases** en 9 packages |
| Patrón MVC | Model + View + Controller + Service + Persistence + Concurrency |
| Serialización para persistencia | `ObjectOutputStream` con `writeObject/readObject` personalizados en Cliente |
| Multithreading | `TareaReporte` y `TareaBackup` extienden `javafx.concurrent.Task<>` |
| Excepciones personalizadas | 4 checked (`GymException` y 3 hijas) + 1 unchecked |
| UI JavaFX profesional | 4 pestañas, modal de cobro, tema CSS, ventanas de progreso |
| 20+ registros de prueba | 20 clientes + 8 clases + 12 equipos = 40 registros |
| JAR ejecutable | `mvn package` produce fat jar con shade plugin |
| Manual técnico 10-15 pág con UML | `docs/MANUAL_TECNICO.md` + 6 diagramas PlantUML |
| Manual de usuario con casos de uso | `docs/MANUAL_USUARIO.md` + `docs/CASOS_DE_USO.md` |
| Archivo de configuración | `config.properties` con 14 parámetros |
| Video 5-8 minutos | Script en `docs/VIDEO_SCRIPT.md` |
| Bitácora de IA | `docs/BITACORA_IA.md` con intercambios documentados |

---

## Funcionalidades específicas

| Funcionalidad pedida | Dónde se implementa |
|---|---|
| Registro de nuevas suscripciones con descuentos | `ClientesController` + `DialogoCobro` con slider de descuento |
| Renovación de membresías | `MembresiasController` doble-click → renovar y cobrar |
| Sistema de puntos/recompensas | `Cliente.agregarPuntos()`, acumulación automática en cada cobro + bonus de renovación |
| Calendario de clases grupales | `ClasesController` con `ClaseGrupal.inscribir(idCliente)` y validación de cupo |
| Control de inventario de equipos | Modelo `Equipo` con `Estado` enum (OPERATIVO, EN_REPARACION, FUERA_DE_SERVICIO) |
| Notificaciones por vencimiento | Estado visual en pestaña Membresías (Vigente, Por vencer, Vencida) |
| Generación de reportes | 3 tipos en background con `Task` + ProgressBar |

---

## Reutilización inteligente de las prácticas

Cada práctica anterior contribuye al GymPOS:

| Práctica | Aporte a GymPOS |
|---|---|
| P3 (Encapsulamiento) | Validaciones en `Cliente` (peso 30-300, email regex) |
| P4 (Herencia) | Jerarquía `Membresia → MembresiaBasica/Premium/VIP` |
| P5 (Polimorfismo) | Template Method en `ProcesadorPagos.cobrarMembresia()` |
| P6 (Interfaces) | Cuando aplica (omitido en este alcance pragmático) |
| P7 (Excepciones) | Jerarquía `GymException` con `PagoRechazadoException` rica |
| P8 (Colecciones) | `GestionClientes` con 4 estructuras sincronizadas |
| P9 (I/O) | `GestorArchivos` genérico + `BackupManager` con timestamp |
| P10 (Concurrencia) | `TareaReporte`/`TareaBackup` con `Task` y `Platform.runLater` |
| P11 (JavaFX) | Toda la capa de UI: `TableView`, `FilteredList`, componentes personalizados |

---

## Tecnologías

- **Java 21** (LTS)
- **JavaFX 21.0.2**
- **Maven 3.8+**
- **maven-shade-plugin 3.5.0** (fat JAR)
- **javafx-maven-plugin 0.0.8** (ejecución en desarrollo)

Sin frameworks adicionales: no Spring, no Hibernate, no librerías de PDF. Solo bibliotecas estándar de JavaFX.

---

## Licencia y créditos

Proyecto académico desarrollado como evaluación final del curso. Los datos de prueba son ficticios.

Asistido en desarrollo por Claude (Anthropic) como apoyo de pair programming. Ver `docs/BITACORA_IA.md` para detalle de los intercambios.
