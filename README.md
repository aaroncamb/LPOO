# LPOO — Laboratorio de Programación Orientada a Objetos

Repositorio personal con las prácticas individuales y el proyecto integrador del curso.

## Dominio elegido para el semestre

A lo largo de las prácticas mantengo un dominio coherente: gestión de un gimnasio.
Las clases construidas en cada práctica (Cliente, Membresía, ClaseGrupal, Pago, etc.)
se reutilizan e integran en el proyecto final GymPOS (Práctica 12). Esto permite que
cada práctica aporte piezas concretas al sistema final en lugar de ser ejercicios aislados.

La justificación detallada de esta decisión vive en cada `README.md` de cada práctica.

## Stack

- **JDK:** 21.0.8 LTS (Temurin)
- **IDE:** IntelliJ IDEA Community
- **Build:** compilación directa con `javac` (Prácticas 1–10) y Maven (Prácticas 11–12)
- **Pruebas:** JUnit 5
- **GUI:** JavaFX 21 (Prácticas 11 y 12)

## Índice de prácticas

| # | Tema |
|---|------|
| 01 | Introducción a Java y configuración del entorno |
| 02 | Clases y objetos |
| 03 | Encapsulamiento y modificadores de acceso |
| 04 | Herencia |
| 05 | Polimorfismo |
| 06 | Clases abstractas e interfaces |
| 07 | Manejo de excepciones |
| 08 | Colecciones |
| 09 | Entrada/Salida (I/O) |
| 10 | Programación concurrente |
| 11 | JavaFX para interfaces gráficas |
| 12 | Proyecto Integrador — GymPOS |

## Estructura

Cada práctica vive en su propia carpeta `Practica##/` con:

- `README.md` — instrucciones de ejecución y justificación del Elemento de Decisión Propia
- `BITACORA_IA.md` — registro del proceso de pair programming con IA
- `REFLEXION.md` — respuestas a las preguntas de reflexión
- `src/` — código fuente

## Política de IA

El uso de IA como apoyo de pair programming está documentado en cada `BITACORA_IA.md`.
Cada decisión de diseño y línea de código son comprendidas y defendibles oralmente.
