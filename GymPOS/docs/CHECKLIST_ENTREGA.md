# Checklist Final de Entrega — GymPOS

Lista de verificación previo a la presentación del proyecto integrador.
**Usa esto como tu última revisión antes de enviar todo.**

---

## 1. Código

### Estructura
- [ ] El proyecto está en `LPOO/GymPOS/` dentro del repositorio.
- [ ] Tiene `pom.xml` en la raíz.
- [ ] Tiene `config.properties` en la raíz.
- [ ] Tiene `README.md` en la raíz.

### Packages
- [ ] `com.gympos` (App.java)
- [ ] `com.gympos.model` (8 archivos)
- [ ] `com.gympos.view` (3 archivos)
- [ ] `com.gympos.controller` (6 archivos)
- [ ] `com.gympos.service` (6 archivos)
- [ ] `com.gympos.persistence` (3 archivos)
- [ ] `com.gympos.exceptions` (5 archivos)
- [ ] `com.gympos.concurrency` (2 archivos)
- [ ] `com.gympos.util` (1 archivo)

**Total: 35 clases** (mínimo pedido: 15) ✓

### Compilación
- [ ] `mvn clean compile` termina con `BUILD SUCCESS`.
- [ ] `mvn clean package` produce `target/gympos-1.0.0.jar`.
- [ ] El JAR ejecuta con `java -jar target/gympos-1.0.0.jar`.
- [ ] La app abre con la ventana de 4 pestañas y los 20 clientes precargados.

### Calidad
- [ ] No hay warnings del compilador (probado con `-Xlint:all`).
- [ ] Todas las operaciones de I/O usan `try-with-resources`.
- [ ] Las clases serializables tienen `serialVersionUID` explícito.
- [ ] Las excepciones personalizadas extienden de `GymException` (checked) o `RuntimeException` (unchecked).

---

## 2. Funcionalidades específicas

| # | Funcionalidad de la consigna | Verificación |
|---|---|---|
| 1 | Registro de nuevas suscripciones con descuentos | [ ] Funciona el botón "Nuevo" en Clientes; el DialogoCobro permite descuento 0-30% |
| 2 | Renovación automática de membresías | [ ] Doble-click en Membresías abre cobro; al confirmar se renueva |
| 3 | Sistema de puntos/recompensas | [ ] La columna "Puntos" en Clientes aumenta tras cada cobro |
| 4 | Calendario de clases grupales | [ ] La pestaña Clases Grupales muestra 8 clases con cupo y permite inscribir |
| 5 | Control de inventario de equipos | [ ] El modelo Equipo está en `model/`, con 12 equipos en DatosPrueba |
| 6 | Notificaciones automáticas por vencimiento | [ ] La pestaña Membresías marca "Por vencer" y "Vencida" con texto visual |
| 7 | Generación de reportes (con multithreading) | [ ] La pestaña Reportes genera con barra de progreso, archivo en `data/reportes/` |

---

## 3. Requisitos técnicos generales

- [ ] **Patrón MVC**: model / view / controller / service están separados en packages.
- [ ] **Serialización**: `Cliente.dat`, `Membresia.dat`, etc. se crean tras primera ejecución.
- [ ] **Multithreading**: TareaReporte y TareaBackup ejecutan en hilo separado.
- [ ] **Excepciones personalizadas**: 4 checked (GymException + 3 hijas) + 1 unchecked (EntradaInvalida).
- [ ] **UI JavaFX profesional**: 4 pestañas, modal de cobro, diálogos de confirmación, CSS aplicado.
- [ ] **20+ registros de prueba**: 20 clientes + 8 clases + 12 equipos = 40 registros.
- [ ] **JAR ejecutable**: `mvn package` produce fat jar con shade plugin.
- [ ] **Archivo de configuración**: `config.properties` con 14 parámetros.

---

## 4. Documentación

### Manual técnico
- [ ] `docs/MANUAL_TECNICO.md` existe.
- [ ] Tiene tabla de contenidos.
- [ ] Cubre arquitectura, decisiones de diseño, persistencia, concurrencia, UI.
- [ ] Equivale a 10-15 páginas en formato PDF (lo es: ~30,000 caracteres).
- [ ] Referencia 6 diagramas UML.

### Diagramas UML
- [ ] `docs/diagramas/01-arquitectura-general.puml`
- [ ] `docs/diagramas/02-jerarquia-membresia.puml`
- [ ] `docs/diagramas/03-jerarquia-excepciones.puml`
- [ ] `docs/diagramas/04-secuencia-cobro.puml`
- [ ] `docs/diagramas/05-secuencia-reporte-background.puml`
- [ ] `docs/diagramas/06-modelo-datos.puml`
- [ ] **OPCIONAL**: Renderizados a PNG en `docs/diagramas/` para incluir en el PDF final.

### Manual de usuario
- [ ] `docs/MANUAL_USUARIO.md` existe.
- [ ] Cubre las 4 pestañas con instrucciones paso a paso.
- [ ] Incluye 8 placeholders de capturas (resueltas con tus screenshots).

### Casos de uso
- [ ] `docs/CASOS_DE_USO.md` existe.
- [ ] Tiene 7 casos formales (CU-01 a CU-07).
- [ ] Cada uno con actor, precondición, postcondición, flujo principal y alternativos.

### Bitácora de IA
- [ ] `docs/BITACORA_IA.md` existe.
- [ ] Tiene 7 intercambios documentados con prompt, resumen, qué hice, qué aprendí.
- [ ] Incluye reflexión final sobre el uso de IA.

### README
- [ ] `README.md` (raíz de GymPOS) explica cómo ejecutar.
- [ ] Tiene tabla de cumplimiento de rúbrica.
- [ ] Referencia los documentos de `docs/`.

---

## 5. Capturas de pantalla

Las 8 capturas mínimas necesarias en `GymPOS/capturas/`:

- [ ] `01-pantalla-principal.png` (pestaña Clientes con tabla llena)
- [ ] `02-formulario-cliente.png` (modal de Nuevo cliente abierto)
- [ ] `03-validacion-email.png` (email rojo con tooltip)
- [ ] `04-pestana-membresias.png` (pestaña Membresías con datos)
- [ ] `05-dialogo-cobro.png` (DialogoCobro con slider de descuento)
- [ ] `06-pestana-clases.png` (pestaña Clases Grupales)
- [ ] `07-pestana-reportes.png` (vista previa del reporte generado)
- [ ] `08-acerca-de.png` (Alert de Ayuda → Acerca de)

### Calidad de capturas
- [ ] Resolución mínima 1024x720.
- [ ] No tienen información personal visible (ventanas de otras apps al fondo).
- [ ] Se ven los detalles que ilustran (el botón rojo, el tooltip, etc).

---

## 6. Video

- [ ] Duración entre 5 y 8 minutos.
- [ ] Resolución 1080p mínimo.
- [ ] Audio claro y sin saturación.
- [ ] Muestra las 4 pestañas funcionando.
- [ ] Muestra al menos un cobro (exitoso o rechazado).
- [ ] Muestra al menos un reporte generándose con progreso.
- [ ] Muestra el backup automático al cerrar.
- [ ] Menciona nombre completo y matrícula al inicio.
- [ ] Mencionas el cumplimiento de la rúbrica al final.
- [ ] **Subido a Google Drive** con permisos "Cualquiera con el enlace puede ver", o
- [ ] **Subido a YouTube** como "No listado".

---

## 7. Repositorio Git

### En el último push debe estar
- [ ] El código completo de GymPOS.
- [ ] Toda la carpeta `docs/`.
- [ ] La carpeta `capturas/` con los screenshots.
- [ ] El `README.md` principal.

### Verificación
- [ ] `git status` no muestra archivos pendientes.
- [ ] `git log --oneline -20` muestra los commits granulares (no un solo commit gigante).
- [ ] En GitHub, el repo se ve correctamente sin archivos faltantes.

### **NO debe estar en el repo**
- [ ] La carpeta `target/` (artefactos de Maven, está en .gitignore).
- [ ] La carpeta `.idea/` (configuración local de IntelliJ).
- [ ] Archivos `*.log` (logs de ejecuciones).
- [ ] Los archivos `data/*.dat` de tu sesión personal (datos transitorios).
- [ ] El JAR ejecutable (`gympos-1.0.0.jar`) — se genera con `mvn package`.

Si alguno de estos está, verifica que `.gitignore` los excluya.

---

## 8. Entrega

Según las indicaciones de tu profesor, deberás enviar:

### Lo mínimo esperado
- [ ] URL del repo de GitHub: `https://github.com/aaroncamb/LPOO`
- [ ] Link del video (Drive o YouTube no listado).

### Si pide PDF del manual técnico
- [ ] Exportar `MANUAL_TECNICO.md` a PDF (en VS Code, instalar extensión "Markdown PDF" y exportar).
- [ ] Incluir los diagramas UML renderizados como PNG dentro del PDF.

### Si pide ZIP del proyecto
- [ ] Comprimir la carpeta `GymPOS/` completa **excluyendo** `target/`, `.idea/`, `data/*.dat`.
- [ ] Verificar que el ZIP descomprime y `mvn javafx:run` funciona desde cero.

---

## 9. Última revisión 30 minutos antes de la presentación

Si tienes defensa oral:

- [ ] Tienes la app abierta en una pestaña de IntelliJ, lista para mostrar.
- [ ] Tienes el código del MANUAL_TECNICO abierto en otra pestaña.
- [ ] Has practicado responder las 6 preguntas más probables:
  1. ¿Por qué FilteredList en lugar de filtrar manualmente?
  2. ¿Por qué Property + Serializable en Cliente?
  3. ¿Cómo evitas que la UI se congele al generar un reporte?
  4. ¿Qué diferencia hay entre GymException y EntradaInvalidaException?
  5. ¿Cómo se mantienen consistentes las 4 estructuras de GestionClientes?
  6. ¿Por qué MembresiaVIP hereda directo de Membresia y no de Premium?

- [ ] Sabes dónde está cada cosa en el código (no buscas a ciegas).
- [ ] Tienes a la mano el USB con el código + video + manuales por si falla internet.

---

## 10. Felicitaciones

Si llegaste hasta aquí con todos los checkboxes marcados, tu proyecto está **listo**. Es un proyecto sólido, completo y bien documentado que cubre todos los criterios de evaluación de la rúbrica.

Recordatorios para el día de la presentación:

- **Habla con confianza**. El código es tuyo, lo entiendes, lo defiendes.
- **Si te preguntan algo que no sabes**, di "no lo recuerdo exactamente, déjame revisar el código" y muéstralo. Eso vale más que inventar.
- **No te disculpes por usar IA**. Está documentado en la bitácora y todos los profesores lo saben. Usaste IA con criterio, no como atajo.

**¡Éxito!**
