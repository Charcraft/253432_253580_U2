import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Logica del caso practico: "Gestion de asientos en el aula, por dia,
 * considerar 2 dias; el alumno decide que asiento utilizar".
 *
 * Estructura lineal usada: COLA (Queue sobre LinkedList), principio FIFO.
 *  - offer()  -> encola al alumno al final de la fila virtual.
 *  - peek()   -> muestra quien sigue sin sacarlo (tarjeta de turno).
 *  - poll()   -> saca al alumno cuando ya eligio silla o se marco ausente.
 *
 * La interfaz grafica (GestionAsientosAula) solo llama a estos metodos:
 * aqui no hay nada de Swing, lo que permite probar la logica por separado.
 */
public class GestorAula {

    /** El caso practico pide exactamente 2 dias. */
    public static final int TOTAL_DIAS = 2;

    private int filas;
    private int[] sillasPorFila = new int[0];
    private Alumno[][] matriz;                 // matrizAsientos[fila][silla]

    private final Queue<Alumno> cola = new LinkedList<>();
    private final List<Alumno> ausentes = new ArrayList<>();
    private final List<Alumno> todos = new ArrayList<>();
    private final List<RegistroDia> historial = new ArrayList<>();

    // ------------------------------------------------------------------
    // Configuracion del salon
    // ------------------------------------------------------------------

    /**
     * Prepara una jornada nueva: guarda la distribucion del aula, reinicia la
     * matriz de asientos y vuelve a encolar a todos los alumnos de la lista.
     */
    public void configurar(int filas, int[] sillasPorFila, List<String> nombres) {
        this.filas = filas;
        this.sillasPorFila = sillasPorFila.clone();

        this.matriz = new Alumno[filas][maxSillas()];
        this.cola.clear();
        this.ausentes.clear();
        this.todos.clear();

        for (String nombre : nombres) {
            Alumno alumno = new Alumno(nombre);
            todos.add(alumno);
            cola.offer(alumno);          // FIFO: se encola al final
        }
    }

    public int getFilas() {
        return filas;
    }

    public int getSillasEnFila(int fila) {
        return sillasPorFila[fila];
    }

    public int[] getSillasPorFila() {
        return sillasPorFila.clone();
    }

    public int maxSillas() {
        int max = 0;
        for (int s : sillasPorFila) {
            max = Math.max(max, s);
        }
        return max;
    }

    public Alumno[][] getMatriz() {
        return matriz;
    }

    public List<Alumno> getAusentes() {
        return new ArrayList<>(ausentes);
    }

    public List<Alumno> getTodos() {
        return new ArrayList<>(todos);
    }

    public List<RegistroDia> getHistorial() {
        return new ArrayList<>(historial);
    }

    // ------------------------------------------------------------------
    // Operaciones de la cola
    // ------------------------------------------------------------------

    /** Alumno al frente de la cola (peek): el que aparece en la tarjeta. */
    public Alumno siguienteAlumno() {
        return cola.peek();
    }

    public int getPendientes() {
        return cola.size();
    }

    public int getProcesados() {
        return todos.size() - cola.size();
    }

    public int getTotalAlumnos() {
        return todos.size();
    }

    public int getOcupadas() {
        int ocupadas = 0;
        for (int f = 0; f < filas; f++) {
            for (int s = 0; s < sillasPorFila[f]; s++) {
                if (matriz[f][s] != null) {
                    ocupadas++;
                }
            }
        }
        return ocupadas;
    }

    public int getTotalSillas() {
        int total = 0;
        for (int s : sillasPorFila) {
            total += s;
        }
        return total;
    }

    public int getLibres() {
        return getTotalSillas() - getOcupadas();
    }

    /** Valida fila/silla y devuelve el alumno que ocupa ese lugar (o null). */
    public Alumno ocupante(int fila, int lugar) {
        return matriz[fila - 1][lugar - 1];
    }

    /**
     * Asigna al alumno que esta al frente de la cola el asiento elegido.
     * Se valida rango y disponibilidad antes de sacarlo de la cola, para no
     * perder al alumno si el dato es incorrecto.
     */
    public Alumno asignarAsiento(int fila, int lugar) throws ReglaNegocioException {
        Alumno actual = cola.peek();
        if (actual == null) {
            throw new ReglaNegocioException("La cola esta vacia: todos los alumnos ya fueron procesados.");
        }
        validarAsientoLibre(fila, lugar);

        cola.poll();                     // FIFO: sale el primero de la fila
        actual.setFila(fila);
        actual.setLugar(lugar);
        actual.setAsistio(true);
        matriz[fila - 1][lugar - 1] = actual;
        return actual;
    }

    /** Saca de la cola al alumno actual y lo agrega a la lista de ausentes. */
    public Alumno marcarAusente() throws ReglaNegocioException {
        Alumno actual = cola.peek();
        if (actual == null) {
            throw new ReglaNegocioException("La cola esta vacia: no hay alumnos pendientes.");
        }
        cola.poll();
        actual.setAsistio(false);
        actual.setFila(-1);
        actual.setLugar(-1);
        ausentes.add(actual);
        return actual;
    }

    /**
     * Permite corregir: un alumno marcado como ausente llega tarde y elige
     * una silla. Sale de la lista de ausentes y se marca como asistente.
     */
    public void reasignarAusente(Alumno alumno, int fila, int lugar) throws ReglaNegocioException {
        if (alumno == null) {
            throw new ReglaNegocioException("Seleccione un alumno de la lista de ausentes.");
        }
        validarAsientoLibre(fila, lugar);

        alumno.setFila(fila);
        alumno.setLugar(lugar);
        alumno.setAsistio(true);
        matriz[fila - 1][lugar - 1] = alumno;
        ausentes.remove(alumno);
    }

    /** Reglas compartidas por la asignacion normal y la reasignacion. */
    private void validarAsientoLibre(int fila, int lugar) throws ReglaNegocioException {
        if (fila < 1 || fila > filas) {
            throw new ReglaNegocioException("La fila debe estar entre 1 y " + filas + ".");
        }
        if (lugar < 1 || lugar > sillasPorFila[fila - 1]) {
            throw new ReglaNegocioException("En la fila " + fila + " la silla debe estar entre 1 y "
                    + sillasPorFila[fila - 1] + ".");
        }
        Alumno ocupante = matriz[fila - 1][lugar - 1];
        if (ocupante != null) {
            throw new ReglaNegocioException("La silla F" + fila + "-S" + lugar
                    + " ya esta ocupada por " + ocupante.getNombre() + ".");
        }
    }

    // ------------------------------------------------------------------
    // Cierre de la jornada
    // ------------------------------------------------------------------

    /**
     * Cierra el dia: los alumnos que quedaron en la cola cuentan como falta,
     * y se guarda la fotografia del dia en el historial.
     */
    public RegistroDia finalizarDia(int numeroDia, String etiqueta) {
        while (!cola.isEmpty()) {
            Alumno pendiente = cola.poll();
            pendiente.setAsistio(false);
            if (!ausentes.contains(pendiente)) {
                ausentes.add(pendiente);
            }
        }
        RegistroDia registro = new RegistroDia(numeroDia, etiqueta, filas, sillasPorFila, todos);
        historial.add(registro);
        return registro;
    }

    public boolean diaRegistrado(int numeroDia) {
        for (RegistroDia r : historial) {
            if (r.getNumeroDia() == numeroDia) {
                return true;
            }
        }
        return false;
    }

    public RegistroDia buscarDia(int numeroDia) {
        for (RegistroDia r : historial) {
            if (r.getNumeroDia() == numeroDia) {
                return r;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Persistencia
    // ------------------------------------------------------------------

    /** Lee un alumno por linea; ignora lineas vacias. */
    public static List<String> leerAlumnos(File archivo) throws IOException {
        List<String> nombres = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (!linea.isEmpty() && !linea.startsWith("#")) {
                    nombres.add(linea);
                }
            }
        }
        return nombres;
    }

    /** Lista de ejemplo usada cuando no se selecciona ningun archivo. */
    public static List<String> alumnosPorDefecto() {
        List<String> nombres = new ArrayList<>();
        nombres.add("Alarcon Valencia, Carlos Adrian");
        nombres.add("Barrientos Marina, Carlos David");
        nombres.add("Cordoba Argon, Maria");
        nombres.add("Dominguez Perez, Juan");
        nombres.add("Gomez Ruiz, Laura");
        nombres.add("Hernandez Lopez, Pedro");
        return nombres;
    }

    /**
     * Guarda el historial en un archivo TSV (se abre con Excel).
     * Una fila por alumno y por dia, con la distribucion del aula incluida.
     */
    public void guardarHistorial(File destino) throws IOException {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(destino)))) {
            out.println("# numeroDia\tetiqueta\tfilas\tsillasPorFila\tnombre\tfila\tlugar\tasistio");
            for (RegistroDia r : historial) {
                String sillas;
                int[] arreglo = r.getSillasPorFila();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < arreglo.length; i++) {
                    if (i > 0) {
                        sb.append(',');
                    }
                    sb.append(arreglo[i]);
                }
                sillas = sb.toString();

                for (Alumno a : r.getAlumnos()) {
                    out.println(r.getNumeroDia() + "\t" + r.getEtiqueta() + "\t" + r.getFilas()
                            + "\t" + sillas + "\t" + a.getNombre() + "\t" + a.getFila()
                            + "\t" + a.getLugar() + "\t" + (a.isAsistio() ? "1" : "0"));
                }
            }
        }
    }

    /** Reconstruye el historial guardado con guardarHistorial(). */
    public void cargarHistorial(File origen) throws IOException {
        if (origen == null || !origen.exists()) {
            return;
        }
        List<RegistroDia> cargados = new ArrayList<>();
        int diaActual = -1;
        String etiquetaActual = "";
        int filasActual = 0;
        int[] sillasActual = new int[0];
        List<Alumno> alumnosActual = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(origen))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty() || linea.startsWith("#")) {
                    continue;
                }
                String[] campos = linea.split("\t");
                if (campos.length < 8) {
                    continue;
                }
                int numero = Integer.parseInt(campos[0]);
                if (numero != diaActual) {
                    if (diaActual != -1) {
                        cargados.add(new RegistroDia(diaActual, etiquetaActual, filasActual, sillasActual, alumnosActual));
                    }
                    diaActual = numero;
                    etiquetaActual = campos[1];
                    filasActual = Integer.parseInt(campos[2]);
                    String[] partes = campos[3].split(",");
                    sillasActual = new int[partes.length];
                    for (int i = 0; i < partes.length; i++) {
                        sillasActual[i] = Integer.parseInt(partes[i].trim());
                    }
                    alumnosActual = new ArrayList<>();
                }
                Alumno a = new Alumno(campos[4]);
                a.setFila(Integer.parseInt(campos[5]));
                a.setLugar(Integer.parseInt(campos[6]));
                a.setAsistio("1".equals(campos[7]));
                alumnosActual.add(a);
            }
        }
        if (diaActual != -1) {
            cargados.add(new RegistroDia(diaActual, etiquetaActual, filasActual, sillasActual, alumnosActual));
        }
        historial.clear();
        historial.addAll(cargados);
    }

    // ------------------------------------------------------------------
    // Excepcion de reglas de negocio (mensajes para el usuario)
    // ------------------------------------------------------------------

    public static class ReglaNegocioException extends Exception {
        public ReglaNegocioException(String mensaje) {
            super(mensaje);
        }
    }
}
