import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Caso practico Unidad 2 - Estructuras de Datos.
 * "Gestion de asientos en el aula, por dia, considerar 2 dias.
 *  El alumno decide que asiento utilizar."
 *
 * Estructura lineal: COLA (FIFO) implementada con Queue/LinkedList.
 *
 * Pantallas:
 *   1. Inicio      -> jornada (dia 1 o dia 2), dia de la semana, filas, sillas,
 *                     lista de alumnos (.txt).
 *   2. Gestion     -> tarjeta del alumno al frente de la cola y mapa de sillas.
 *   3. Resumen     -> totales del dia y mapa de asientos en solo lectura.
 *   4. Comparativa -> tabla y graficas de los 2 dias.
 */
public class GestionAsientosAula extends JFrame {

    // ------------------------- Paleta y tipografias -------------------------
    private static final Color AZUL = new Color(0x1B3252);
    private static final Color AZUL_MEDIO = new Color(0x2F6FB0);
    private static final Color FONDO = new Color(0xF1F4F8);
    private static final Color VERDE = new Color(0x2E9E5B);
    private static final Color ROJO = new Color(0xD64545);
    private static final Color GRIS_TEXTO = new Color(0x5A6472);
    private static final Color TARJETA = Color.WHITE;

    private static final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FUENTE_SECCION = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FUENTE_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FUENTE_BOTON = new Font("Segoe UI", Font.BOLD, 13);

    // ----------------------------- Estado -----------------------------------
    private final GestorAula gestor = new GestorAula();
    private final File archivoHistorial = new File("historial_asientos.tsv");

    private CardLayout cardLayout;
    private JPanel contenedor;

    // Pantalla de inicio
    private JComboBox<String> cbJornada;
    private JComboBox<String> cbDiaSemana;
    private JSpinner spFilas;
    private JPanel panelSillasPorFila;
    private final List<JSpinner> spinnersSillas = new ArrayList<>();
    private JLabel lblArchivo;
    private JLabel lblPieInicio;
    private File archivoAlumnos;

    // Pantalla de gestion
    private JLabel lblTurnoNumero;
    private JLabel lblNombreAlumno;
    private JLabel lblEstadoDia;
    private JTextField txtFila;
    private JTextField txtLugar;
    private Boton btnAceptar;
    private Boton btnAusente;
    private Boton btnVerAusentes;
    private JPanel panelTarjeta;
    private JPanel panelAula;
    private JLabel lblEstadoBarra;
    private BotonSilla[][] botonesSillas;
    private JLabel lblContadorOcupadas;
    private JLabel lblContadorLibres;
    private JLabel lblContadorCola;
    private Boton btnFinalizar;

    // Pantalla de resumen
    private JPanel panelResumen;

    // Pantalla de comparativa
    private JPanel panelComparativa;

    private int diaEnCurso = 1;
    private String diaSemanaEnCurso = "Lunes";

    public GestionAsientosAula() {
        setTitle("Gestion de Asientos en el Aula - Cola (FIFO) - Unidad 2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 780);
        setMinimumSize(new Dimension(1060, 700));
        setLocationRelativeTo(null);

        cargarHistorialGuardado();

        cardLayout = new CardLayout();
        contenedor = new JPanel(cardLayout);
        contenedor.setBackground(FONDO);

        contenedor.add(crearPantallaInicio(), "inicio");
        contenedor.add(crearPantallaGestion(), "gestion");
        contenedor.add(crearPantallaResumen(), "resumen");
        contenedor.add(crearPantallaComparativa(), "comparativa");

        add(contenedor);
        mostrarInicio();
    }

    // ======================================================================
    // 1. PANTALLA DE INICIO
    // ======================================================================

    private JPanel crearPantallaInicio() {
        JPanel pantalla = new JPanel(new BorderLayout());
        pantalla.setBackground(FONDO);
        pantalla.add(crearEncabezado(), BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setBackground(FONDO);
        centro.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel tarjeta = tarjetaBlanca();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(new EmptyBorder(22, 28, 22, 28));
        tarjeta.setPreferredSize(new Dimension(820, 490));

        JLabel titulo = new JLabel("Configuracion de la jornada");
        titulo.setFont(FUENTE_SECCION);
        titulo.setForeground(AZUL);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        tarjeta.add(titulo);

        JLabel ayuda = new JLabel("El caso practico considera 2 dias: registre el Dia 1 y despues el Dia 2.");
        ayuda.setFont(FUENTE_NORMAL);
        ayuda.setForeground(GRIS_TEXTO);
        ayuda.setAlignmentX(Component.LEFT_ALIGNMENT);
        ayuda.setBorder(new EmptyBorder(4, 0, 16, 0));
        tarjeta.add(ayuda);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(TARJETA);
        formulario.setAlignmentX(Component.LEFT_ALIGNMENT);
        formulario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 4, 8, 14);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;

        // Jornada
        gbc.gridx = 0;
        gbc.gridy = 0;
        formulario.add(etiquetaAncha("Jornada a registrar:"), gbc);
        cbJornada = new JComboBox<>(new String[]{"Dia 1", "Dia 2"});
        cbJornada.setFont(FUENTE_NORMAL);
        gbc.gridx = 1;
        formulario.add(cbJornada, gbc);

        // Dia de la semana
        gbc.gridx = 0;
        gbc.gridy = 1;
        formulario.add(etiquetaAncha("Dia de la semana:"), gbc);
        cbDiaSemana = new JComboBox<>(new String[]{"Lunes", "Martes", "Miercoles",
                "Jueves", "Viernes", "Sabado"});
        cbDiaSemana.setFont(FUENTE_NORMAL);
        gbc.gridx = 1;
        formulario.add(cbDiaSemana, gbc);

        // Filas
        gbc.gridx = 0;
        gbc.gridy = 2;
        formulario.add(etiquetaAncha("Cantidad de filas:"), gbc);
        spFilas = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        spFilas.setFont(FUENTE_NORMAL);
        spFilas.setPreferredSize(new Dimension(80, 32));
        spFilas.addChangeListener(e -> construirSpinnersSillas());
        gbc.gridx = 1;
        formulario.add(spFilas, gbc);

        // Sillas por fila
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formulario.add(etiquetaAncha("Sillas por fila:"), gbc);
        panelSillasPorFila = new JPanel(new GridLayout(0, 3, 8, 6));
        panelSillasPorFila.setBackground(TARJETA);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        formulario.add(panelSillasPorFila, gbc);

        // Archivo
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formulario.add(etiquetaAncha("Lista de alumnos (.txt):"), gbc);
        JPanel filaArchivo = new JPanel(new BorderLayout(8, 0));
        filaArchivo.setBackground(TARJETA);
        Boton btnArchivo = new Boton("Seleccionar archivo...", AZUL_MEDIO);
        btnArchivo.addActionListener(e -> seleccionarArchivo());
        lblArchivo = new JLabel("Sin archivo: se usara la lista de ejemplo");
        lblArchivo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblArchivo.setForeground(GRIS_TEXTO);
        filaArchivo.add(btnArchivo, BorderLayout.WEST);
        filaArchivo.add(lblArchivo, BorderLayout.CENTER);
        gbc.gridx = 1;
        gbc.weightx = 1;
        formulario.add(filaArchivo, gbc);

        tarjeta.add(formulario);
        tarjeta.add(Box.createVerticalStrut(14));

        Boton btnComenzar = new Boton("Comenzar pase de lista", VERDE);
        btnComenzar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnComenzar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnComenzar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnComenzar.addActionListener(e -> iniciarJornada());
        tarjeta.add(btnComenzar);

        lblPieInicio = new JLabel(" ");
        lblPieInicio.setFont(FUENTE_NORMAL);
        lblPieInicio.setForeground(GRIS_TEXTO);
        lblPieInicio.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblPieInicio.setBorder(new EmptyBorder(12, 0, 0, 0));
        tarjeta.add(lblPieInicio);

        centro.add(tarjeta);
        pantalla.add(centro, BorderLayout.CENTER);

        construirSpinnersSillas();
        return pantalla;
    }

    /** Crea un spinner de sillas por cada fila del salon. */
    private void construirSpinnersSillas() {
        int filas = (Integer) spFilas.getValue();
        panelSillasPorFila.removeAll();
        spinnersSillas.clear();

        for (int i = 0; i < filas; i++) {
            JPanel caja = new JPanel(new BorderLayout(6, 0));
            caja.setBackground(TARJETA);
            JLabel etiquetaFila = new JLabel("Fila " + (i + 1) + ":");
            etiquetaFila.setFont(FUENTE_NORMAL);
            etiquetaFila.setForeground(GRIS_TEXTO);
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(4, 1, 12, 1));
            spinner.setFont(FUENTE_NORMAL);
            spinner.setPreferredSize(new Dimension(60, 30));
            caja.add(etiquetaFila, BorderLayout.WEST);
            caja.add(spinner, BorderLayout.CENTER);
            panelSillasPorFila.add(caja);
            spinnersSillas.add(spinner);
        }
        panelSillasPorFila.revalidate();
        panelSillasPorFila.repaint();
        actualizarPieInicio();
    }

    private void actualizarPieInicio() {
        int sillas = 0;
        for (JSpinner s : spinnersSillas) {
            sillas += (Integer) s.getValue();
        }
        int dias = gestor.getHistorial().size();
        lblPieInicio.setText("Aula configurada con " + spinnersSillas.size() + " filas y "
                + sillas + " sillas  |  Dias registrados: " + dias + " de " + GestorAula.TOTAL_DIAS);
    }

    private void seleccionarArchivo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecciona la lista de alumnos (.txt)");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos de texto (*.txt)", "txt"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            archivoAlumnos = chooser.getSelectedFile();
            lblArchivo.setText(" " + archivoAlumnos.getName());
        }
    }

    /** Lee el formulario y arranca la jornada elegida. */
    private void iniciarJornada() {
        int numeroDia = cbJornada.getSelectedIndex() + 1;
        String diaSemana = (String) cbDiaSemana.getSelectedItem();

        if (gestor.diaRegistrado(numeroDia)) {
            aviso("El Dia " + numeroDia + " ya fue registrado. Elija el otro dia para continuar.",
                    "Jornada ya registrada", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (numeroDia == 2 && !gestor.diaRegistrado(1)) {
            aviso("Primero debe finalizar el Dia 1 para poder registrar el Dia 2.",
                    "Orden de las jornadas", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filas = (Integer) spFilas.getValue();
        int[] sillas = new int[filas];
        for (int i = 0; i < filas; i++) {
            sillas[i] = (Integer) spinnersSillas.get(i).getValue();
        }

        List<String> nombres;
        try {
            nombres = (archivoAlumnos != null && archivoAlumnos.exists())
                    ? GestorAula.leerAlumnos(archivoAlumnos)
                    : GestorAula.alumnosPorDefecto();
        } catch (IOException ex) {
            aviso("No se pudo leer el archivo: " + ex.getMessage(), "Error de lectura",
                    JOptionPane.ERROR_MESSAGE);
            nombres = GestorAula.alumnosPorDefecto();
        }

        if (nombres.isEmpty()) {
            aviso("La lista de alumnos esta vacia.", "Lista vacia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (nombres.size() > sumar(sillas)) {
            aviso("Hay " + nombres.size() + " alumnos y solo " + sumar(sillas)
                    + " sillas. Aumente las sillas o revise la lista.", "Faltan sillas",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        diaEnCurso = numeroDia;
        diaSemanaEnCurso = diaSemana;
        gestor.configurar(filas, sillas, nombres);

        lblEstadoDia.setText("Dia " + numeroDia + " de " + GestorAula.TOTAL_DIAS
                + "  -  " + diaSemana);
        dibujarAula();
        actualizarTarjeta();
        cardLayout.show(contenedor, "gestion");
    }

    private int sumar(int[] valores) {
        int total = 0;
        for (int v : valores) {
            total += v;
        }
        return total;
    }

    // ======================================================================
    // 2. PANTALLA DE GESTION
    // ======================================================================

    private JPanel crearPantallaGestion() {
        JPanel pantalla = new JPanel(new BorderLayout());
        pantalla.setBackground(FONDO);
        pantalla.add(crearEncabezado(), BorderLayout.NORTH);

        // ---- Tarjeta del alumno en turno (izquierda) ----
        panelTarjeta = tarjetaBlanca();
        panelTarjeta.setLayout(new BoxLayout(panelTarjeta, BoxLayout.Y_AXIS));
        panelTarjeta.setBorder(new EmptyBorder(18, 18, 18, 18));
        panelTarjeta.setPreferredSize(new Dimension(330, 0));

        JLabel tituloTarjeta = new JLabel("Turno en cola (FIFO)");
        tituloTarjeta.setFont(FUENTE_SECCION);
        tituloTarjeta.setForeground(AZUL);
        tituloTarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelTarjeta.add(tituloTarjeta);

        lblTurnoNumero = new JLabel("Alumno 1 de 1");
        lblTurnoNumero.setFont(FUENTE_NORMAL);
        lblTurnoNumero.setForeground(GRIS_TEXTO);
        lblTurnoNumero.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTurnoNumero.setBorder(new EmptyBorder(2, 0, 10, 0));
        panelTarjeta.add(lblTurnoNumero);

        lblNombreAlumno = new JLabel("<html><b>Alumno:</b><br>-</html>");
        lblNombreAlumno.setFont(new Font("Segoe UI", Font.PLAIN, 19));
        lblNombreAlumno.setForeground(AZUL);
        lblNombreAlumno.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblNombreAlumno.setBorder(new EmptyBorder(0, 0, 16, 0));
        panelTarjeta.add(lblNombreAlumno);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(TARJETA);
        formulario.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formulario.add(etiqueta("Fila:"), gbc);
        txtFila = new JTextField();
        txtFila.setFont(FUENTE_NORMAL);
        txtFila.setPreferredSize(new Dimension(90, 32));
        gbc.gridx = 1;
        formulario.add(txtFila, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formulario.add(etiqueta("Silla:"), gbc);
        txtLugar = new JTextField();
        txtLugar.setFont(FUENTE_NORMAL);
        txtLugar.setPreferredSize(new Dimension(90, 32));
        gbc.gridx = 1;
        formulario.add(txtLugar, gbc);
        panelTarjeta.add(formulario);
        formulario.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                formulario.getPreferredSize().height));

        JLabel pista = new JLabel("<html>Tambien puede hacer clic en una<br>silla libre del mapa.</html>");
        pista.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        pista.setForeground(GRIS_TEXTO);
        pista.setAlignmentX(Component.LEFT_ALIGNMENT);
        pista.setBorder(new EmptyBorder(6, 0, 12, 0));
        panelTarjeta.add(pista);

        btnAceptar = new Boton("Aceptar asiento", VERDE);
        btnAceptar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAceptar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnAceptar.addActionListener(e -> asignarAsiento());
        panelTarjeta.add(btnAceptar);
        panelTarjeta.add(Box.createVerticalStrut(8));

        btnAusente = new Boton("Marcar ausente", ROJO);
        btnAusente.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAusente.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnAusente.addActionListener(e -> marcarAusente());
        panelTarjeta.add(btnAusente);
        panelTarjeta.add(Box.createVerticalStrut(8));

        btnVerAusentes = new Boton("Ver lista de ausentes", AZUL_MEDIO);
        btnVerAusentes.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnVerAusentes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnVerAusentes.addActionListener(e -> abrirAusentes());
        panelTarjeta.add(btnVerAusentes);

        panelTarjeta.add(Box.createVerticalGlue());

        // ---- Mapa del aula (centro) ----
        panelAula = new JPanel();
        panelAula.setBackground(FONDO);
        JScrollPane scroll = new JScrollPane(panelAula);
        scroll.setBorder(tarjetaBlanca().getBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(FONDO);

        JPanel centro = new JPanel(new BorderLayout(14, 0));
        centro.setBackground(FONDO);
        centro.setBorder(new EmptyBorder(12, 16, 12, 16));
        centro.add(panelTarjeta, BorderLayout.WEST);
        centro.add(scroll, BorderLayout.CENTER);

        // ---- Barra inferior ----
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(TARJETA);
        barra.setBorder(new EmptyBorder(10, 16, 10, 16));

        JPanel contadores = new JPanel(new GridLayout(1, 3, 18, 0));
        contadores.setBackground(TARJETA);
        lblContadorOcupadas = contador("Ocupadas: 0");
        lblContadorLibres = contador("Libres: 0");
        lblContadorCola = contador("En cola: 0");
        contadores.add(lblContadorOcupadas);
        contadores.add(lblContadorLibres);
        contadores.add(lblContadorCola);

        lblEstadoBarra = new JLabel(" ");
        lblEstadoBarra.setFont(FUENTE_NORMAL);
        lblEstadoBarra.setForeground(GRIS_TEXTO);
        lblEstadoBarra.setBorder(new EmptyBorder(0, 24, 0, 12));

        btnFinalizar = new Boton("Finalizar jornada", ROJO);
        btnFinalizar.setPreferredSize(new Dimension(180, 40));
        btnFinalizar.addActionListener(e -> finalizarJornada());

        barra.add(contadores, BorderLayout.WEST);
        barra.add(lblEstadoBarra, BorderLayout.CENTER);
        barra.add(btnFinalizar, BorderLayout.EAST);

        JPanel sur = new JPanel(new BorderLayout());
        sur.setBackground(TARJETA);
        JLabel dia = new JLabel("  ");
        lblEstadoDia = dia;
        dia.setFont(FUENTE_SECCION);
        dia.setForeground(AZUL);
        JPanel franja = new JPanel(new BorderLayout());
        franja.setBackground(new Color(0xE6EEF8));
        franja.setBorder(new EmptyBorder(8, 16, 8, 16));
        franja.add(dia, BorderLayout.WEST);
        sur.add(franja, BorderLayout.NORTH);
        sur.add(barra, BorderLayout.SOUTH);

        pantalla.add(centro, BorderLayout.CENTER);
        pantalla.add(sur, BorderLayout.SOUTH);
        return pantalla;
    }

    private void dibujarAula() {
        panelAula.removeAll();
        panelAula.setLayout(new BoxLayout(panelAula, BoxLayout.Y_AXIS));
        panelAula.setBorder(new EmptyBorder(14, 14, 14, 14));
        botonesSillas = new BotonSilla[gestor.getFilas()][gestor.maxSillas()];

        for (int f = 0; f < gestor.getFilas(); f++) {
            JPanel fila = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 6));
            fila.setBackground(FONDO);
            fila.setAlignmentX(Component.LEFT_ALIGNMENT);
            fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));

            JLabel etiquetaFila = new JLabel("Fila " + (f + 1));
            etiquetaFila.setFont(FUENTE_NORMAL);
            etiquetaFila.setForeground(GRIS_TEXTO);
            etiquetaFila.setPreferredSize(new Dimension(52, 30));
            fila.add(etiquetaFila);

            for (int s = 0; s < gestor.getSillasEnFila(f); s++) {
                final int numFila = f + 1;
                final int numSilla = s + 1;
                Alumno ocupante = gestor.getMatriz()[f][s];
                BotonSilla boton = new BotonSilla("F" + numFila + "-S" + numSilla,
                        ocupante == null ? "" : ocupante.getApellidoCorto());
                if (ocupante != null) {
                    boton.pintar(VERDE, Color.WHITE);
                    boton.setEnabled(false);
                }
                boton.addActionListener(e -> {
                    txtFila.setText(String.valueOf(numFila));
                    txtLugar.setText(String.valueOf(numSilla));
                });
                botonesSillas[f][s] = boton;
                fila.add(boton);
            }
            panelAula.add(fila);
            panelAula.add(Box.createVerticalStrut(6));
        }
        panelAula.revalidate();
        panelAula.repaint();
        actualizarContadores();
    }

    /** Refresca la tarjeta del alumno en turno y los contadores. */
    private void actualizarTarjeta() {
        Alumno actual = gestor.siguienteAlumno();
        int total = gestor.getTotalAlumnos();
        int procesados = gestor.getProcesados();

        if (actual != null) {
            lblTurnoNumero.setText("Alumno " + (procesados + 1) + " de " + total);
            lblNombreAlumno.setText("<html><b>Alumno en turno:</b><br>" + actual.getNombre() + "</html>");
            btnAceptar.setEnabled(true);
            btnAusente.setEnabled(true);
        } else {
            lblTurnoNumero.setText("Alumnos procesados: " + total + " de " + total);
            lblNombreAlumno.setText("<html><b>Cola vacia.</b><br>"
                    + "Ya se procesaron todos los alumnos.</html>");
            btnAceptar.setEnabled(false);
            btnAusente.setEnabled(false);
        }
        txtFila.setText("");
        txtLugar.setText("");
        btnVerAusentes.setText("Ver lista de ausentes (" + gestor.getAusentes().size() + ")");
        actualizarContadores();
        actualizarPieInicio();
    }

    private void actualizarContadores() {
        lblContadorOcupadas.setText("Ocupadas: " + gestor.getOcupadas());
        lblContadorLibres.setText("Libres: " + gestor.getLibres());
        lblContadorCola.setText("En cola: " + gestor.getPendientes());
        lblEstadoBarra.setText("Ausentes: " + gestor.getAusentes().size()
                + "   |   Sillas: " + gestor.getTotalSillas());
    }

    private void asignarAsiento() {
        try {
            int fila = Integer.parseInt(txtFila.getText().trim());
            int silla = Integer.parseInt(txtLugar.getText().trim());
            Alumno asignado = gestor.asignarAsiento(fila, silla);

            actualizarBoton(fila, silla, asignado);
            destello();
            actualizarTarjeta();
        } catch (NumberFormatException ex) {
            aviso("Escriba numeros enteros en Fila y Silla.", "Dato invalido",
                    JOptionPane.ERROR_MESSAGE);
        } catch (GestorAula.ReglaNegocioException ex) {
            aviso(ex.getMessage(), "No se pudo asignar la silla", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void marcarAusente() {
        try {
            Alumno ausente = gestor.marcarAusente();
            actualizarTarjeta();
            lblEstadoBarra.setText("Se marco como ausente a " + ausente.getNombre());
        } catch (GestorAula.ReglaNegocioException ex) {
            aviso(ex.getMessage(), "Cola vacia", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actualizarBoton(int fila, int silla, Alumno alumno) {
        BotonSilla boton = botonesSillas[fila - 1][silla - 1];
        if (boton != null) {
            boton.setTexto("F" + fila + "-S" + silla, alumno.getApellidoCorto());
            boton.pintar(VERDE, Color.WHITE);
            boton.setEnabled(false);
        }
    }

    /** Pequeno destello verde en la tarjeta al pasar al siguiente alumno. */
    private void destello() {
        Color original = panelTarjeta.getBackground();
        panelTarjeta.setBackground(new Color(0xE7F6EC));
        javax.swing.Timer timer = new javax.swing.Timer(180, e -> {
            panelTarjeta.setBackground(original);
            ((javax.swing.Timer) e.getSource()).stop();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void abrirAusentes() {
        JDialog dialog = new JDialog(this, "Alumnos ausentes - Dia " + diaEnCurso, true);
        dialog.setSize(560, 460);
        dialog.setLocationRelativeTo(this);

        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBackground(TARJETA);
        principal.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel titulo = new JLabel("Puede asignar una silla a un alumno que llego tarde");
        titulo.setFont(FUENTE_SECCION);
        titulo.setForeground(AZUL);
        principal.add(titulo, BorderLayout.NORTH);

        javax.swing.DefaultListModel<Alumno> modelo = new javax.swing.DefaultListModel<>();
        for (Alumno a : gestor.getAusentes()) {
            modelo.addElement(a);
        }
        JList<Alumno> lista = new JList<>(modelo);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setFont(FUENTE_NORMAL);
        principal.add(new JScrollPane(lista), BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(8, 8));
        pie.setBackground(TARJETA);
        JPanel campos = new JPanel(new GridLayout(1, 4, 6, 0));
        campos.setBackground(TARJETA);
        JTextField fila = new JTextField();
        JTextField silla = new JTextField();
        campos.add(etiqueta("Fila:"));
        campos.add(fila);
        campos.add(etiqueta("Silla:"));
        campos.add(silla);

        Boton btnAsignar = new Boton("Asignar silla", VERDE);
        btnAsignar.setPreferredSize(new Dimension(150, 38));
        btnAsignar.addActionListener(e -> {
            Alumno seleccionado = lista.getSelectedValue();
            try {
                int f = Integer.parseInt(fila.getText().trim());
                int s = Integer.parseInt(silla.getText().trim());
                gestor.reasignarAusente(seleccionado, f, s);
                modelo.removeElement(seleccionado);
                actualizarBoton(f, s, seleccionado);
                actualizarTarjeta();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                aviso("Escriba numeros enteros en Fila y Silla.", "Dato invalido",
                        JOptionPane.ERROR_MESSAGE);
            } catch (GestorAula.ReglaNegocioException ex) {
                aviso(ex.getMessage(), "No se pudo asignar la silla", JOptionPane.WARNING_MESSAGE);
            }
        });

        pie.add(campos, BorderLayout.CENTER);
        pie.add(btnAsignar, BorderLayout.EAST);
        principal.add(pie, BorderLayout.SOUTH);

        dialog.add(principal);
        dialog.setVisible(true);
    }

    // ======================================================================
    // 3. PANTALLA DE RESUMEN DEL DIA
    // ======================================================================

    private JPanel crearPantallaResumen() {
        JPanel pantalla = new JPanel(new BorderLayout());
        pantalla.setBackground(FONDO);
        pantalla.add(crearEncabezado(), BorderLayout.NORTH);

        panelResumen = new JPanel(new BorderLayout());
        panelResumen.setBackground(FONDO);
        panelResumen.setBorder(new EmptyBorder(16, 20, 16, 20));
        pantalla.add(panelResumen, BorderLayout.CENTER);
        return pantalla;
    }

    private void mostrarResumen(RegistroDia registro) {
        panelResumen.removeAll();

        JPanel superior = new JPanel(new BorderLayout(0, 12));
        superior.setBackground(FONDO);

        JLabel titulo = new JLabel("Jornada finalizada: " + registro.getTitulo());
        titulo.setFont(FUENTE_TITULO);
        titulo.setForeground(AZUL);
        superior.add(titulo, BorderLayout.NORTH);

        JPanel kpis = new JPanel(new GridLayout(1, 4, 12, 0));
        kpis.setBackground(FONDO);
        kpis.add(tarjetaKpi(String.valueOf(registro.getTotalAlumnos()), "Alumnos en lista", AZUL));
        kpis.add(tarjetaKpi(String.valueOf(registro.getAsistentes()), "Asistieron", VERDE));
        kpis.add(tarjetaKpi(String.valueOf(registro.getFaltas()), "Faltas", ROJO));
        kpis.add(tarjetaKpi(registro.getPorcentajeAsistencia() + "%", "Asistencia", AZUL_MEDIO));
        superior.add(kpis, BorderLayout.CENTER);
        panelResumen.add(superior, BorderLayout.NORTH);

        JPanel aula = new JPanel();
        aula.setBackground(FONDO);
        aula.setLayout(new BoxLayout(aula, BoxLayout.Y_AXIS));
        aula.setBorder(new EmptyBorder(16, 0, 0, 0));
        dibujarAulaRegistro(aula, registro);

        JScrollPane scroll = new JScrollPane(aula);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(FONDO);
        panelResumen.add(scroll, BorderLayout.CENTER);

        JPanel botones = new JPanel(new BorderLayout());
        botones.setBackground(FONDO);
        botones.setBorder(new EmptyBorder(12, 0, 0, 0));

        Boton btnComparar = new Boton("Ver comparativa de los 2 dias", AZUL_MEDIO);
        btnComparar.addActionListener(e -> mostrarComparativa());
        btnComparar.setEnabled(gestor.getHistorial().size() == GestorAula.TOTAL_DIAS);

        Boton btnInicio = new Boton("Regresar al inicio", VERDE);
        btnInicio.addActionListener(e -> mostrarInicio());

        botones.add(btnComparar, BorderLayout.WEST);
        botones.add(btnInicio, BorderLayout.EAST);
        panelResumen.add(botones, BorderLayout.SOUTH);

        panelResumen.revalidate();
        panelResumen.repaint();
    }

    /** Pinta el aula de un dia ya guardado (sin interaccion). */
    private void dibujarAulaRegistro(JPanel contenedor, RegistroDia registro) {
        Alumno[][] matriz = new Alumno[registro.getFilas()][];
        for (int f = 0; f < registro.getFilas(); f++) {
            matriz[f] = new Alumno[registro.getSillasPorFila(f)];
        }
        for (Alumno a : registro.getAlumnos()) {
            if (a.tieneAsiento() && a.isAsistio()) {
                matriz[a.getFila() - 1][a.getLugar() - 1] = a;
            }
        }

        for (int f = 0; f < registro.getFilas(); f++) {
            JPanel fila = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 6));
            fila.setBackground(FONDO);
            fila.setAlignmentX(Component.LEFT_ALIGNMENT);
            fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));

            JLabel etiquetaFila = new JLabel("Fila " + (f + 1));
            etiquetaFila.setFont(FUENTE_NORMAL);
            etiquetaFila.setForeground(GRIS_TEXTO);
            etiquetaFila.setPreferredSize(new Dimension(52, 30));
            fila.add(etiquetaFila);

            for (int s = 0; s < registro.getSillasPorFila(f); s++) {
                Alumno ocupante = matriz[f][s];
                BotonSilla boton = new BotonSilla("F" + (f + 1) + "-S" + (s + 1),
                        ocupante == null ? "" : ocupante.getApellidoCorto());
                boton.setEnabled(false);
                if (ocupante != null) {
                    boton.pintar(VERDE, Color.WHITE);
                }
                fila.add(boton);
            }
            contenedor.add(fila);
            contenedor.add(Box.createVerticalStrut(6));
        }
    }

    private JPanel tarjetaKpi(String valor, String texto, Color color) {
        JPanel tarjeta = tarjetaBlanca();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblValor.setForeground(color);
        lblValor.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTexto = new JLabel(texto);
        lblTexto.setFont(FUENTE_NORMAL);
        lblTexto.setForeground(GRIS_TEXTO);
        lblTexto.setAlignmentX(Component.LEFT_ALIGNMENT);

        tarjeta.add(lblValor);
        tarjeta.add(lblTexto);
        return tarjeta;
    }

    // ======================================================================
    // 4. PANTALLA COMPARATIVA DE LOS 2 DIAS
    // ======================================================================

    private JPanel crearPantallaComparativa() {
        JPanel pantalla = new JPanel(new BorderLayout());
        pantalla.setBackground(FONDO);
        pantalla.add(crearEncabezado(), BorderLayout.NORTH);

        panelComparativa = new JPanel(new BorderLayout(0, 14));
        panelComparativa.setBackground(FONDO);
        panelComparativa.setBorder(new EmptyBorder(16, 20, 16, 20));
        pantalla.add(panelComparativa, BorderLayout.CENTER);
        return pantalla;
    }

    private void mostrarComparativa() {
        panelComparativa.removeAll();
        RegistroDia dia1 = gestor.buscarDia(1);
        RegistroDia dia2 = gestor.buscarDia(2);

        JLabel titulo = new JLabel("Comparativa de las 2 jornadas");
        titulo.setFont(FUENTE_TITULO);
        titulo.setForeground(AZUL);
        panelComparativa.add(titulo, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.setBackground(FONDO);

        // Tarjetas resumen por dia
        JPanel filaKpis = new JPanel(new GridLayout(1, 2, 14, 0));
        filaKpis.setBackground(FONDO);
        filaKpis.add(panelDia(dia1));
        filaKpis.add(panelDia(dia2));
        centro.add(filaKpis, BorderLayout.NORTH);

        // Tabla alumno por alumno
        String[] columnas = {"Alumno", "Dia 1: asiento", "Dia 1: asistencia",
                "Dia 2: asiento", "Dia 2: asistencia"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        List<Alumno> base = dia1 != null ? dia1.getAlumnos() : new ArrayList<>();
        for (Alumno alumno : base) {
            Alumno enDia2 = dia2 != null ? dia2.buscar(alumno.getNombre()) : null;
            modelo.addRow(new Object[]{
                    alumno.getNombre(),
                    textoAsiento(alumno),
                    alumno.isAsistio() ? "Asistio" : "Falta",
                    enDia2 == null ? "-" : textoAsiento(enDia2),
                    enDia2 == null ? "-" : (enDia2.isAsistio() ? "Asistio" : "Falta")
            });
        }

        JTable tabla = new JTable(modelo);
        tabla.setFont(FUENTE_NORMAL);
        tabla.setRowHeight(28);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabla.setGridColor(new Color(0xE2E7EE));
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int c = 1; c < tabla.getColumnCount(); c++) {
            tabla.getColumnModel().getColumn(c).setCellRenderer(centrado);
        }

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Asistencia y asiento por dia"));
        centro.add(scroll, BorderLayout.CENTER);
        panelComparativa.add(centro, BorderLayout.CENTER);

        Boton btnInicio = new Boton("Regresar al inicio", VERDE);
        btnInicio.addActionListener(e -> mostrarInicio());
        JPanel pie = new JPanel(new BorderLayout());
        pie.setBackground(FONDO);
        pie.add(btnInicio, BorderLayout.EAST);
        panelComparativa.add(pie, BorderLayout.SOUTH);

        panelComparativa.revalidate();
        panelComparativa.repaint();
        cardLayout.show(contenedor, "comparativa");
    }

    private JPanel panelDia(RegistroDia dia) {
        JPanel panel = tarjetaBlanca();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel titulo = new JLabel(dia == null ? "Dia pendiente" : dia.getTitulo());
        titulo.setFont(FUENTE_SECCION);
        titulo.setForeground(dia == null ? GRIS_TEXTO : AZUL);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titulo);

        if (dia == null) {
            JLabel pendiente = new JLabel("Aun no se ha registrado esta jornada.");
            pendiente.setFont(FUENTE_NORMAL);
            pendiente.setForeground(GRIS_TEXTO);
            pendiente.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(pendiente);
            return panel;
        }

        panel.add(Box.createVerticalStrut(6));
        panel.add(linea("Asistieron: " + dia.getAsistentes() + " de " + dia.getTotalAlumnos()
                + " (" + dia.getPorcentajeAsistencia() + "%)", VERDE));
        panel.add(linea("Faltas: " + dia.getFaltas(), ROJO));
        panel.add(linea("Sillas ocupadas: " + dia.getSillasOcupadas() + " de " + dia.getTotalSillas(), AZUL_MEDIO));
        return panel;
    }

    private JLabel linea(String texto, Color color) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(FUENTE_NORMAL);
        lbl.setForeground(color);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private String textoAsiento(Alumno alumno) {
        return alumno.tieneAsiento() ? "F" + alumno.getFila() + "-S" + alumno.getLugar() : "-";
    }

    // ======================================================================
    // Navegacion y utilidades de interfaz
    // ======================================================================

    private void finalizarJornada() {
        int respuesta = JOptionPane.showConfirmDialog(this,
                "Finalizar la jornada del " + diaSemanaEnCurso + "?\n"
                        + "Los alumnos que sigan en la cola se contaran como FALTAS.",
                "Confirmar cierre del dia", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        RegistroDia registro = gestor.finalizarDia(diaEnCurso, diaSemanaEnCurso);
        try {
            gestor.guardarHistorial(archivoHistorial);
        } catch (IOException ex) {
            aviso("El resumen se mostrara, pero no se pudo guardar el historial: "
                    + ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
        }
        mostrarResumen(registro);
        cardLayout.show(contenedor, "resumen");
    }

    private void mostrarInicio() {
        actualizarPieInicio();
        String[] opciones = {"Dia 1", "Dia 2"};
        for (int i = 0; i < opciones.length; i++) {
            if (gestor.diaRegistrado(i + 1)) {
                opciones[i] = opciones[i] + " (registrado)";
            }
        }
        cbJornada.setModel(new javax.swing.DefaultComboBoxModel<>(opciones));
        cbJornada.setSelectedIndex(gestor.diaRegistrado(1) ? 1 : 0);
        cardLayout.show(contenedor, "inicio");
    }

    private void cargarHistorialGuardado() {
        try {
            gestor.cargarHistorial(archivoHistorial);
        } catch (IOException | RuntimeException ex) {
            // Si el archivo esta danado se ignora: la app debe abrir igual.
            System.err.println("No se pudo cargar el historial: " + ex.getMessage());
        }
    }

    private void aviso(String mensaje, String titulo, int tipo) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, tipo);
    }

    private JPanel crearEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, AZUL, getWidth(), 0, new Color(0x2A4C77)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        encabezado.setBorder(new EmptyBorder(14, 22, 14, 22));

        JLabel titulo = new JLabel("Gestion de Asientos en el Aula");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 21));
        titulo.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel("Estructura de datos lineal: COLA (FIFO)  |  Unidad 2 - Caso practico");
        subtitulo.setFont(FUENTE_SUBTITULO);
        subtitulo.setForeground(new Color(0xC9D8EA));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(titulo);
        textos.add(subtitulo);
        encabezado.add(textos, BorderLayout.WEST);

        Boton btnHistorial = new Boton("Ver comparativa", new Color(0x3C6E9B));
        btnHistorial.setPreferredSize(new Dimension(150, 36));
        btnHistorial.addActionListener(e -> {
            if (gestor.getHistorial().isEmpty()) {
                aviso("Todavia no hay jornadas registradas.", "Sin historial",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            mostrarComparativa();
        });
        JPanel derecha = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        derecha.setOpaque(false);
        derecha.add(btnHistorial);
        encabezado.add(derecha, BorderLayout.EAST);
        return encabezado;
    }

    private JPanel tarjetaBlanca() {
        JPanel panel = new JPanel();
        panel.setBackground(TARJETA);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E7EE)),
                new EmptyBorder(2, 2, 2, 2)));
        return panel;
    }

    private JLabel etiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(FUENTE_NORMAL);
        lbl.setForeground(GRIS_TEXTO);
        return lbl;
    }

    /** Etiqueta de ancho fijo para alinear los formularios. */
    private JLabel etiquetaAncha(String texto) {
        JLabel lbl = etiqueta(texto);
        lbl.setPreferredSize(new Dimension(190, 24));
        return lbl;
    }

    private JLabel contador(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(AZUL);
        return lbl;
    }

    /** Boton plano con color propio y efecto de hover. */
    private static class Boton extends JButton {
        private final Color color;
        private boolean encima = false;

        Boton(String texto, Color color) {
            super(texto);
            this.color = color;
            setFont(FUENTE_BOTON);
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(200, 38));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    encima = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color base = isEnabled() ? color : new Color(0xB6BEC9);
            if (encima && isEnabled()) {
                base = base.brighter();
            }
            g2.setColor(base);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.setColor(new Color(255, 255, 255, 60));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    /** Boton de una silla: dibuja el codigo F?-S? y el apellido del ocupante. */
    private static class BotonSilla extends JButton {
        private String linea1;
        private String linea2;
        private Color fondo = new Color(0xFFFFFF);
        private Color texto = new Color(0x3A4552);

        BotonSilla(String linea1, String linea2) {
            this.linea1 = linea1;
            this.linea2 = linea2;
            Dimension tamano = new Dimension(104, 58);
            setPreferredSize(tamano);
            setMinimumSize(tamano);
            setMaximumSize(tamano);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        void setTexto(String linea1, String linea2) {
            this.linea1 = linea1;
            this.linea2 = linea2;
            repaint();
        }

        void pintar(Color fondo, Color texto) {
            this.fondo = fondo;
            this.texto = texto;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fondo);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.setColor(new Color(0, 0, 0, 26));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

            g2.setColor(texto);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(linea1, (getWidth() - fm.stringWidth(linea1)) / 2, 22);

            if (linea2 != null && !linea2.isEmpty()) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                fm = g2.getFontMetrics();
                g2.drawString(linea2, (getWidth() - fm.stringWidth(linea2)) / 2, 39);
            }
            g2.dispose();
        }
    }

    // ======================================================================
    // Punto de entrada
    // ======================================================================

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.err.println("No se pudo aplicar el look and feel: " + ex.getMessage());
        }
        SwingUtilities.invokeLater(() -> new GestionAsientosAula().setVisible(true));
    }
}
