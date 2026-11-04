import java.util.ArrayList;
import java.util.List;

/**
 * Fotografia (snapshot) de una jornada ya finalizada.
 *
 * Se guarda por cada uno de los 2 dias del caso practico:
 * numero de dia, dia de la semana, distribucion del aula y el estado
 * final de cada alumno (fila, lugar y asistencia).
 *
 * Es inmutable hacia afuera: recibe copias de los alumnos, por lo que
 * empezar el dia siguiente no altera los registros anteriores.
 */
public class RegistroDia {

    private final int numeroDia;          // 1 o 2
    private final String etiqueta;        // "Lunes", "Martes", ...
    private final int filas;
    private final int[] sillasPorFila;
    private final List<Alumno> alumnos;   // copias con su asiento y asistencia
    private final int asistentes;
    private final int faltas;

    public RegistroDia(int numeroDia, String etiqueta, int filas,
                       int[] sillasPorFila, List<Alumno> alumnosDelDia) {
        this.numeroDia = numeroDia;
        this.etiqueta = etiqueta;
        this.filas = filas;
        this.sillasPorFila = sillasPorFila.clone();

        this.alumnos = new ArrayList<>();
        int asistieron = 0;
        for (Alumno a : alumnosDelDia) {
            Alumno copia = a.copia();
            alumnos.add(copia);
            if (copia.isAsistio()) {
                asistieron++;
            }
        }
        this.asistentes = asistieron;
        this.faltas = alumnosDelDia.size() - asistieron;
    }

    public int getNumeroDia() {
        return numeroDia;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public int getFilas() {
        return filas;
    }

    public int getSillasPorFila(int fila) {
        return sillasPorFila[fila];
    }

    public int[] getSillasPorFila() {
        return sillasPorFila.clone();
    }

    public List<Alumno> getAlumnos() {
        return new ArrayList<>(alumnos);
    }

    public int getTotalAlumnos() {
        return alumnos.size();
    }

    public int getAsistentes() {
        return asistentes;
    }

    public int getFaltas() {
        return faltas;
    }

    /** Porcentaje de asistencia redondeado, para las tarjetas del resumen. */
    public int getPorcentajeAsistencia() {
        if (alumnos.isEmpty()) {
            return 0;
        }
        return (int) Math.round(asistentes * 100.0 / alumnos.size());
    }

    public int getTotalSillas() {
        int total = 0;
        for (int s : sillasPorFila) {
            total += s;
        }
        return total;
    }

    /** Sillas realmente ocupadas (alumnos que asistieron y eligieron lugar). */
    public int getSillasOcupadas() {
        int ocupadas = 0;
        for (Alumno a : alumnos) {
            if (a.tieneAsiento() && a.isAsistio()) {
                ocupadas++;
            }
        }
        return ocupadas;
    }

    /** Busca a un alumno por nombre para armar la tabla comparativa. */
    public Alumno buscar(String nombre) {
        for (Alumno a : alumnos) {
            if (a.getNombre().equalsIgnoreCase(nombre)) {
                return a;
            }
        }
        return null;
    }

    public String getTitulo() {
        return "Dia " + numeroDia + " - " + etiqueta;
    }
}
