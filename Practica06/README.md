# Práctica 6 — Clases Abstractas e Interfaces

## Objetivo

Combinar una clase abstracta con varias interfaces, donde las clases
concretas implementan combinaciones **distintas** de interfaces según
lo que el dominio dicte. Demostrar el uso de métodos `default` para
extender interfaces sin romper a sus implementadores.

## Estructura

```
Practica06/
├── README.md
├── REFLEXION.md
├── BITACORA_IA.md
└── src/
    ├── Cobrable.java               (interfaz heredada de P5)
    ├── Servicio.java               (clase abstracta, enriquecida)
    ├── ResultadoVenta.java         (helper inmutable)
    ├── Notificable.java            (interfaz nueva con default)
    ├── Reportable.java             (interfaz nueva con default + static)
    ├── Reagendable.java            (interfaz nueva con default)
    ├── ClaseGrupal.java            (concreta: Notif + Report)
    ├── EntrenamientoPersonal.java  (concreta: Notif + Reag)
    ├── EvaluacionFisica.java       (concreta: las 3)
    ├── CentroOperaciones.java      (clase gestora)
    ├── Main.java                   (demostracion completa)
    └── ServiciosTest.java          (23 pruebas)
```

## Compilación y ejecución

```bash
javac -d out src/*.java
java -cp out Main              # demostracion
java -cp out ServiciosTest     # 23 pruebas
```

## Decisión de continuidad con P5

P6 **reusa la jerarquía `Servicio`** de P5 en lugar de crear una nueva.
Razones:

1. La consigna no pide jerarquía nueva, solo "clase abstracta con métodos
   abstractos y concretos y atributos protegidos". `Servicio` ya cumple
   eso y se enriquece para esta práctica.
2. **Coherencia narrativa del dominio**: el sistema del gimnasio se va
   construyendo. P5 modela cómo se cobran los servicios; P6 modela cómo
   se operan (notificar, reportar, reagendar). Son responsabilidades
   complementarias sobre los mismos objetos.
3. En el proyecto final GymPOS, todas estas piezas conviven: la caja
   registra los cobros, el centro de operaciones agenda y notifica.

### Qué se agregó a `Servicio` para esta práctica

- **Atributo `protected` adicional**: `notas` (anotaciones libres).
- **Métodos concretos nuevos**: `resumen()` (línea compacta) y
  `descripcionCompleta()` (multi-línea, para notificaciones).
- **Método `agregarNota(String)`**: utilidad usada por las implementaciones
  de `Reagendable` para dejar registro de cuándo se movió la fecha.

El Template Method `procesarVenta()` y los métodos de Cobrable siguen ahí
sin cambios.

## Las tres interfaces nuevas

### `Notificable` — envío de avisos al cliente

```java
interface Notificable {
    String  destinatario();
    boolean enviarEmail(String asunto, String cuerpo);
    boolean enviarSMS(String mensaje);
    default boolean notificarMultiplesCanales(String asunto, String mensaje);
}
```

El método `default` coordina email + SMS en una sola llamada, **sin que
las clases tengan que reescribirlo**. Si en el futuro queremos agregar
push notifications, modificamos el default y todas las implementaciones
heredan el cambio automáticamente.

### `Reportable` — datos para reportes gerenciales

```java
interface Reportable {
    String     tituloReporte();
    LocalDate  fechaParaReporte();
    double     montoFacturado();
    String     categoriaReporte();
    default String toCsvLine();         // formato unificado
    static  String csvHeader();         // encabezado del CSV
}
```

Métodos `default` y `static` juntos: el formato CSV vive en la interfaz,
no en cada clase. Si cambia el orden de columnas, se ajusta en un lugar.

### `Reagendable` — mover una cita a otra fecha

```java
interface Reagendable {
    boolean reagendar(LocalDate nuevaFecha);
    int     diasAnticipacionMinima();
    default boolean fechaRespetaAnticipacion(LocalDate fechaPropuesta);
}
```

El `default` `fechaRespetaAnticipacion` factoriza la validación. Cada
clase concreta lo usa dentro de su `reagendar` para evitar duplicar el
cálculo de días.

## Elemento de Decisión Propia — Combinaciones asimétricas

La consigna pide explícitamente que **no todas las clases implementen las
mismas interfaces**, y que cada combinación tenga razón de negocio.
Las combinaciones son las siguientes:

| Clase | Notificable | Reportable | Reagendable |
|---|:---:|:---:|:---:|
| `ClaseGrupal`           | ✅ | ✅ | ❌ |
| `EntrenamientoPersonal` | ✅ | ❌ | ✅ |
| `EvaluacionFisica`      | ✅ | ✅ | ✅ |

### Por qué cada combinación

**`ClaseGrupal` — Notificable + Reportable, NO Reagendable**

Una clase grupal de yoga del martes 7am es un **horario público del
gimnasio**. Si un cliente no puede ir, los otros 14 inscritos sí. No
tiene sentido que un cliente individual "reagende" la clase grupal —
afectaría al resto. Lo que se hace en la realidad es cancelar la
inscripción y reinscribirse en otra clase distinta.

Si implementáramos `Reagendable` solo por simetría, la implementación
tendría que mentir: o no haría nada (devolver true sin mover nada), o
movería la clase afectando al grupo. Ambas violan el contrato.

Sí es Notificable (avisar al grupo de cambios) y sí es Reportable
(asistencia, popularidad de horarios → decisiones gerenciales).

**`EntrenamientoPersonal` — Notificable + Reagendable, NO Reportable**

Servicio 1-a-1 hecho a medida. Reagendar es trivial: cliente y
entrenador acuerdan otro día. No incluir Reportable es **una decisión
de diseño**, no un olvido: los reportes operacionales del gimnasio
agregan métricas comparables (¿cuántos asistieron a yoga este mes?
¿cuánto ingresó por evaluaciones?). Inundar ese reporte con cientos de
filas individuales de entrenamientos premium ahogaría las métricas
realmente accionables. Las métricas de entrenamiento viven en otro
canal (el CRM del cliente, no el dashboard operacional).

**`EvaluacionFisica` — las tres**

- Notificable: el cliente necesita recibir sus resultados (IMC, grasa).
- Reportable: datos agregados para análisis poblacional y campañas
  de salud.
- Reagendable: es cita individual, perfectamente movible.

Esta es la única clase con las tres. Eso no la hace "mejor" — la hace
**multifuncional** porque su naturaleza lo justifica.

### Lo que NO hicimos y por qué importa

Hubiera sido fácil hacer que las tres clases implementaran las tres
interfaces "para cumplir". Es exactamente lo que la consigna pide evitar.
Tener implementaciones que no encajan con el contrato (por ejemplo, una
`ClaseGrupal.reagendar(...)` que en realidad no reagenda nada) es
**peor que no implementarlas**: rompe la confianza de quien usa la
interfaz. El compilador no lo detecta, los bugs aparecen en producción.

## Clase gestora — `CentroOperaciones`

Lo interesante de esta clase es que **opera por interfaz, no por subclase**.
Sus métodos genéricos:

```java
public int notificarTodos(...)        // solo a los Notificable
public String generarReporteCSV()     // solo de los Reportable
public int reagendarTodosNDias(...)   // solo a los Reagendable
public List<Servicio> filtrarPorInterfaz(Class<T>)
```

`CentroOperaciones` no necesita saber si tiene `ClaseGrupal` o
`EvaluacionFisica`. Solo pregunta `if (s instanceof Notificable n)` y
actúa. Eso es polimorfismo via interfaces en estado puro: el código
genérico crece sin acoplarse a las clases concretas. Mañana podemos
agregar `ConsultaNutricion` que implemente solo `Notificable` y el
`CentroOperaciones` la procesará correctamente sin tocar una línea.

## Resultados

```
=== Resumen ===
Pasadas:  23
Falladas: 0
Total:    23
```

23 pruebas distribuidas en:
- 5 verificaciones de combinaciones de interfaces (qué implementa cada clase).
- 4 métodos `default` y `static`.
- 2 comportamiento de Reagendable (anticipación).
- 5 CentroOperaciones (notificar, reportar, reagendar, filtrar, sumar).
- 2 Servicio abstracto (métodos concretos y atributos protected).
- Banderas inferidas en cada prueba (ej. ClaseGrupal NO Reagendable).
