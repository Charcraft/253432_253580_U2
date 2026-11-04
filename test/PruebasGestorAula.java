import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Pruebas de la logica (sin interfaz grafica).
 * Se ejecutan con: java -cp build PruebasGestorAula
 */
public class PruebasGestorAula {

    private static int pruebas = 0;
    private static int fallidas = 0;

    public static void main(String[] args) throws Exception {
        probarColaFifo();
        probarSillaOcupada();
        probarFueraDeRango();
        probarCierreDeDia();
        probarDosDiasYPersistencia();
        System.out.println("\nPruebas ejecutadas: " + pruebas + "  |  Fallidas: " + fallidas);
        if (fallidas > 0) {
            System.exit(1);
        }
    }

    private static void probarColaFifo() throws Exception {
        GestorAula g = nuevoGestor(2, new int[]{3, 3});
        Alumno primero = g.siguienteAlumno();
        g.asignarAsiento(1, 1);
        Alumno segundo = g.siguienteAlumno();
        verificar("FIFO: el primero en entrar es el primero en salir",
                primero.getNombre().startsWith("Alarcon") && segundo.getNombre().startsWith("Barrientos"));
        verificar("Quedan 5 alumnos en cola", g.getPendientes() == 5);
        verificar("La silla F1-S1 quedo ocupada", g.getMatriz()[0][0] == primero);
    }

    private static void probarSillaOcupada() throws Exception {
        GestorAula g = nuevoGestor(2, new int[]{3, 3});
        g.asignarAsiento(1, 1);
        try {
            g.asignarAsiento(1, 1);
            verificar("Silla ocupada debe lanzar error", false);
        } catch (GestorAula.ReglaNegocioException ex) {
            verificar("Silla ocupada lanza error y conserva al alumno en la cola",
                    g.getPendientes() == 5);
        }
    }

    private static void probarFueraDeRango() throws Exception {
        GestorAula g = nuevoGestor(2, new int[]{3, 3});
        try {
            g.asignarAsiento(9, 1);
            verificar("Fila fuera de rango debe lanzar error", false);
        } catch (GestorAula.ReglaNegocioException ex) {
            verificar("Fila fuera de rango lanza error", true);
        }
        try {
            g.asignarAsiento(2, 4);
            verificar("Silla fuera de rango debe lanzar error", false);
        } catch (GestorAula.ReglaNegocioException ex) {
            verificar("Silla fuera de rango lanza error", true);
        }
    }

    private static void probarCierreDeDia() throws Exception {
        GestorAula g = nuevoGestor(3, new int[]{2, 2, 2});
        g.asignarAsiento(1, 1);
        g.asignarAsiento(1, 2);
        g.marcarAusente();                 // 1 falta explicita
        RegistroDia registro = g.finalizarDia(1, "Lunes");
        verificar("Cierre: asistentes = 2", registro.getAsistentes() == 2);
        verificar("Cierre: faltas = 4 (1 ausente + 3 pendientes)", registro.getFaltas() == 4);
        verificar("Cierre: cola vacia", g.getPendientes() == 0);
        verificar("Cierre: dia 1 registrado", g.diaRegistrado(1));
    }

    private static void probarDosDiasYPersistencia() throws Exception {
        GestorAula g = nuevoGestor(2, new int[]{3, 3});
        g.asignarAsiento(1, 1);
        g.asignarAsiento(2, 2);
        g.marcarAusente();
        g.finalizarDia(1, "Lunes");

        g.configurar(2, new int[]{3, 3}, GestorAula.alumnosPorDefecto());
        g.asignarAsiento(2, 1);
        g.finalizarDia(2, "Martes");

        verificar("Se guardaron los 2 dias", g.getHistorial().size() == 2);
        verificar("Dia 1 conserva su mapa de asientos", g.buscarDia(1).getSillasOcupadas() == 2);
        verificar("Dia 2 conserva su mapa de asientos", g.buscarDia(2).getSillasOcupadas() == 1);

        File archivo = new File("build/prueba_historial.tsv");
        g.guardarHistorial(archivo);

        GestorAula otro = new GestorAula();
        otro.cargarHistorial(archivo);
        verificar("Persistencia: se releen 2 jornadas", otro.getHistorial().size() == 2);
        verificar("Persistencia: dia 2 con etiqueta correcta",
                "Martes".equals(otro.buscarDia(2).getEtiqueta()));
        archivo.delete();
    }

    private static GestorAula nuevoGestor(int filas, int[] sillas) {
        GestorAula g = new GestorAula();
        g.configurar(filas, sillas, GestorAula.alumnosPorDefecto());
        return g;
    }

    private static void verificar(String descripcion, boolean condicion) {
        pruebas++;
        if (condicion) {
            System.out.println("[OK]   " + descripcion);
        } else {
            fallidas++;
            System.out.println("[FALLA] " + descripcion);
        }
    }
}
