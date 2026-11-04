/**
 * Modelo de un alumno dentro del pase de lista.
 *
 * Guarda el lugar que eligio el alumno (fila y silla) y si asistio o no.
 * Cuando una jornada termina, se guarda una copia (metodo copia()) dentro
 * de RegistroDia para conservar el historial del dia sin que se modifique
 * despues.
 */
public class Alumno {

    private final String nombre;   // Apellidos, Nombre (tal como viene en el .txt)
    private int fila = -1;         // -1 significa "sin asiento asignado"
    private int lugar = -1;
    private boolean asistio = false;

    public Alumno(String nombre) {
        this.nombre = nombre.trim();
    }

    public String getNombre() {
        return nombre;
    }

    public int getFila() {
        return fila;
    }

    public void setFila(int fila) {
        this.fila = fila;
    }

    public int getLugar() {
        return lugar;
    }

    public void setLugar(int lugar) {
        this.lugar = lugar;
    }

    public boolean isAsistio() {
        return asistio;
    }

    public void setAsistio(boolean asistio) {
        this.asistio = asistio;
    }

    /** true si ya se le asigno una silla fisica en el aula. */
    public boolean tieneAsiento() {
        return fila > 0 && lugar > 0;
    }

    /** Texto corto para dibujar dentro del boton de la silla. */
    public String getApellidoCorto() {
        int coma = nombre.indexOf(',');
        String apellido = (coma > 0) ? nombre.substring(0, coma) : nombre;
        return apellido.length() > 13 ? apellido.substring(0, 12) + "." : apellido;
    }

    /** Copia independiente, usada para congelar la informacion del dia. */
    public Alumno copia() {
        Alumno c = new Alumno(nombre);
        c.fila = fila;
        c.lugar = lugar;
        c.asistio = asistio;
        return c;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
