# Gestión de asientos en el aula por día (Caso práctico U2)

**Asignatura:** Estructuras de Datos
**Unidad:** 2. Estructuras de datos básicas
**Integrantes:**
- Carlos de Jesús Martínez Pineda — `253432`
- Carlos Adrián Alarcón Valencia — `253580`

**Estructura de datos lineal utilizada:** Cola (`Queue` / `LinkedList`) — principio **FIFO**.

---

## ¿Qué resuelve el programa?

El caso asignado es:

> *Gestión de asientos en el aula, por día, considerar 2 días. El alumno decide en qué asiento utilizar.*

El programa permite pasar lista durante **dos jornadas**. En cada jornada:

1. Se configura el aula: día de la semana, número de filas y sillas por fila (cada fila puede tener una cantidad distinta).
2. Se carga la lista de alumnos desde un archivo `.txt` (un alumno por línea, iniciando por apellidos). Si no se selecciona archivo, se usa una lista de ejemplo.
3. Los alumnos se atienden **en orden de llegada** (FIFO): el primero de la cola aparece en la tarjeta de turno.
4. Cada alumno **elige su asiento**: escribiendo fila y silla, o haciendo clic sobre una silla libre del mapa.
5. Si el alumno no llegó, se marca como **ausente** y puede reasignársele una silla si llega tarde.
6. Al finalizar la jornada, los alumnos que siguen en la cola cuentan como falta y se guarda el resumen del día.
7. Al terminar los 2 días, la pantalla **Comparativa** muestra la asistencia y el asiento de cada alumno en cada jornada.

## ¿Por qué una cola?

- **Principio de acceso:** el pase de lista es **FIFO** (primero en llegar, primero en ser atendido). No es LIFO (pila) ni acceso posicional (lista).
- **Relación estructura-caso:** `offer()` encola al alumno, `peek()` muestra quién sigue sin sacarlo y `poll()` lo retira al asignarle silla o marcarlo ausente. La cola mantiene el orden de llegada sin que el programa tenga que ordenar nada.
- **Descarte:** una **pila** atendería al último alumno en llegar (injusto y contrario al pase de lista). Una **lista** permitiría elegir a cualquier alumno en cualquier momento, pero el caso exige respetar un turno, y la lista no impone orden de salida.

---

## Requisitos

- JDK 17 o superior (probado con JDK 26).
- No usa librerías externas: solo `java.awt`, `javax.swing`, `java.util` y `java.io`.

## Cómo compilar y ejecutar

Desde la carpeta del proyecto:

```bash
javac -d build src/*.java
java -cp build GestionAsientosAula
```

En Windows (PowerShell):

```powershell
javac -d build src\*.java
java -cp build GestionAsientosAula
```

O simplemente hacer doble clic en **`ejecutar.bat`**: compila y abre el programa.

## Pruebas de la lógica

```bash
javac -d build src/*.java test/PruebasGestorAula.java
java -cp build PruebasGestorAula
```

Salida esperada: `Pruebas ejecutadas: 15 | Fallidas: 0`.

---

## Estructura del repositorio

```
253432_253580_U2/
├── src/
│   ├── Alumno.java                Modelo: nombre, fila, silla, asistencia.
│   ├── RegistroDia.java           Fotografía de una jornada finalizada.
│   ├── GestorAula.java            Lógica: cola FIFO, matriz de asientos, reglas y persistencia.
│   └── GestionAsientosAula.java   Interfaz Swing (4 pantallas).
├── test/
│   └── PruebasGestorAula.java     Pruebas de la lógica sin interfaz.
├── capturas/                      Evidencias de ejecución (éxito y error).
├── docs/
│   ├── 253432_253580_U2.docx      Documentación técnica (plantilla de la materia).
│   ├── 253432_253580_U2.pdf       Documentación técnica en PDF (entregable).
│   └── img/                       Diagramas e imágenes usadas en el PDF.
├── alumnos.txt                    Lista de alumnos de ejemplo.
├── ejecutar.bat                   Compila y abre el programa en Windows.
└── README.md
```

## Pantallas

| Pantalla | Qué muestra |
|---|---|
| **Inicio** | Jornada (Día 1 / Día 2), día de la semana, filas, sillas por fila y lista `.txt`. |
| **Gestión** | Alumno en turno (frente de la cola), campos de fila/silla, mapa del aula y contadores. |
| **Resumen** | Totales del día (alumnos, asistentes, faltas, % de asistencia) y mapa de asientos. |
| **Comparativa** | Tarjetas de los 2 días y tabla alumno por alumno con su asiento y asistencia. |

## Persistencia

Al finalizar cada jornada el historial se guarda en `historial_asientos.tsv` (se abre con Excel).
Al abrir el programa se vuelve a leer, por lo que los días registrados se conservan.

## Capturas

En `capturas/` están las evidencias usadas en la documentación:

| Archivo | Contenido |
|---|---|
| `01_inicio.png` | Configuración de la jornada. |
| `02_gestion_dia1.png` | Cola iniciada, aula vacía. |
| `03_asiento_asignado.png` | Tres alumnos ya colocados en sus sillas. |
| `04_error_silla_ocupada.png` | Caso de fallo: silla ocupada. |
| `05_error_fila_fuera_de_rango.png` | Caso de fallo: fila inexistente. |
| `06_error_dato_no_numerico.png` | Caso de fallo: texto en lugar de número. |
| `07_lista_ausentes.png` | Alumno marcado ausente y reasignación de silla. |
| `08_confirmar_fin_dia.png` | Confirmación de cierre de la jornada. |
| `09_resumen_dia1.png` | Resumen del Día 1. |
| `10_inicio_dia2.png` | Día 1 registrado; se habilita el Día 2. |
| `11_gestion_dia2.png` | Día 2 en curso. |
| `12_resumen_dia2.png` | Resumen del Día 2. |
| `13_comparativa.png` | Comparativa de las 2 jornadas. |
