# Práctica 1 — Introducción a Java y Configuración del Entorno

## Objetivo

Configurar el entorno de desarrollo Java y verificar su funcionamiento mediante
un programa de presentación, además de un segundo programa que ejercite las
construcciones básicas del lenguaje.

## Entorno usado

- **JDK:** OpenJDK 21.0.8 LTS (Temurin)
- **IDE:** IntelliJ IDEA Community Edition
- **Sistema operativo:** (anota tu SO real)

> Las capturas de pantalla del JDK instalado y del IDE configurado están en
> `capturas/` (agregarlas antes de la entrega).

## Estructura

```
Practica01/
├── README.md           (este archivo)
├── REFLEXION.md
├── BITACORA_IA.md
├── capturas/           (capturas del JDK y del IDE)
└── src/
    ├── HolaMundo.java
    └── CalculadoraIMC.java
```

## Compilación y ejecución

Desde la carpeta `Practica01/`:

```bash
# Compilar
javac -d out src/HolaMundo.java src/CalculadoraIMC.java

# Ejecutar el programa principal
java -cp out HolaMundo

# Ejecutar el elemento de decisión propia
java -cp out CalculadoraIMC
```

En IntelliJ basta con abrir cada archivo y usar **Run** sobre `main`.

## Programas incluidos

### `HolaMundo.java`

Imprime nombre completo, matrícula y la fecha actual del sistema formateada
en español (`DateTimeFormatter` con locale `es_MX`).

### `CalculadoraIMC.java` — Elemento de Decisión Propia

Lee por consola el peso en kilogramos y la altura en centímetros, calcula el
Índice de Masa Corporal y lo clasifica según los rangos de la OMS
(Bajo peso / Normal / Sobrepeso / Obesidad I-III).

**Por qué elegí este programa:**

A partir de la Práctica 2 trabajaré todo el semestre sobre el dominio de un
**gimnasio**, manteniendo coherencia hasta el proyecto final GymPOS. Una
calculadora de IMC me parece una primera utilidad pertinente porque:

1. Se conecta de forma natural con los datos que guardaremos de cada cliente
   (peso y altura) en prácticas posteriores.
2. Ejercita varios elementos básicos del lenguaje: lectura por consola,
   manejo de excepciones (`NumberFormatException`), `try-with-resources`,
   condicionales, métodos auxiliares, formateo de números con `printf`.
3. Implementa validación de entrada (rechaza valores no numéricos y no
   positivos), lo cual es una preparación honesta para el énfasis en
   validaciones que pide la rúbrica del curso (20% del peso de evaluación).

Únicamente uso clases del paquete estándar `java.util.Scanner`, sin
dependencias externas, según pide la consigna.
