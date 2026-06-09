package edu.eci.arsw.matrix.ui;

import edu.eci.arsw.matrix.core.Tablero;
import edu.eci.arsw.matrix.concurrency.Neo;
import edu.eci.arsw.matrix.concurrency.Agente;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class VentanaJuego extends JFrame {

    private int tamanoActual = 8;    private static final Color C_BG = new Color(18, 18, 28);
    private static final Color C_PANEL = new Color(24, 24, 38);
    private static final Color C_TEXT = new Color(210, 210, 228);
    private static final Color C_SUB = new Color(130, 130, 158);

    private Tablero tablero;
    private PanelTablero panelTablero;
    private Neo neo;
    private Agente[] agentes;

    // Estado de setup
    private String modoColocacion = "";
    private int numAgentes = 2;
    private boolean juegoEnCurso = false;
    private boolean finMostrado = false;

    // Referencias a widgets del sidebar
    private JLabel lblEstado;
    private JButton btnIniciar;

    public VentanaJuego() {
        super("Matrix Escape");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(C_BG);

        tablero = new Tablero(tamanoActual);
        tablero.setOnCambio(() -> SwingUtilities.invokeLater(this::refrescar));
        panelTablero = new PanelTablero(tamanoActual);

        construirUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── Construcción inicial ─────────────────────────────────────────────────
    private void construirUI() {
        setLayout(new BorderLayout(8, 8));
        getRootPane().setBorder(new EmptyBorder(12, 12, 12, 12));
        getContentPane().setBackground(C_BG);

        panelTablero.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!juegoEnCurso) {
                    manejarClick(e.getX(), e.getY());
                }
            }
        });

        add(panelTablero, BorderLayout.CENTER);
        add(crearSidebarSetup(), BorderLayout.EAST);
    }

    // ── Sidebar de configuración ─────────────────────────────────────────────
    private JPanel crearSidebarSetup() {
        JPanel p = sidebar();

        agrega(p, label("Matrix Escape", 15, Font.BOLD, C_TEXT));
        agrega(p, label("Selecciona y haz click en el tablero", 11, Font.PLAIN, C_SUB));
        p.add(gap(14));

        ButtonGroup grupo = new ButtonGroup();
        String[][] items = {
            {"N", "Neo", "#30D278"},
            {"A", "Agente", "#D23C3C"},
            {"T", "Teléfono", "#3296DC"},
            {"#", "Muro", "#5A5A6E"},};
        for (String[] item : items) {
            JToggleButton btn = toggle(item[1], Color.decode(item[2]));
            grupo.add(btn);
            final String modo = item[0];
            btn.addActionListener(e -> modoColocacion = modo);
            agrega(p, btn);
            p.add(gap(5));
        }

        p.add(gap(8));
        JButton btnRandom = boton("Generar Aleatorio", new Color(45, 60, 80));
        btnRandom.addActionListener(e -> generarAleatorio());
        agrega(p, btnRandom);
        
        p.add(gap(5));
        JButton btnLimpiar = boton("Limpiar tablero", new Color(70, 35, 35));
        btnLimpiar.addActionListener(e -> limpiarTablero());
        agrega(p, btnLimpiar);
        p.add(gap(14));

        agrega(p, label("Tamaño tablero:", 11, Font.PLAIN, C_SUB));
        p.add(gap(4));

        JSpinner spinnerTamano = new JSpinner(new SpinnerNumberModel(8, 6, 20, 1));
        spinnerTamano.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        spinnerTamano.setAlignmentX(LEFT_ALIGNMENT);
        estilizarSpinner(spinnerTamano);
        spinnerTamano.addChangeListener(e -> {
            tamanoActual = (int) spinnerTamano.getValue();
            cambiarTamanoTablero();
        });
        p.add(spinnerTamano);
        p.add(gap(14));

        agrega(p, label("Número de agentes:", 11, Font.PLAIN, C_SUB));
        p.add(gap(4));

        JSpinner spinner = new JSpinner(new SpinnerNumberModel(2, 1, 4, 1));
        spinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        spinner.setAlignmentX(LEFT_ALIGNMENT);
        estilizarSpinner(spinner);
        spinner.addChangeListener(e -> {
            numAgentes = (int) spinner.getValue();
            actualizarEstado();
        });
        p.add(spinner);
        p.add(gap(14));

        lblEstado = new JLabel("<html>Neo: ✗<br>Teléfonos: 0<br>Agentes: 0</html>");
        lblEstado.setForeground(C_SUB);
        lblEstado.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblEstado.setAlignmentX(LEFT_ALIGNMENT);
        p.add(lblEstado);

        p.add(Box.createVerticalGlue());

        btnIniciar = boton("Iniciar juego", new Color(30, 110, 65));
        btnIniciar.setEnabled(false);
        btnIniciar.addActionListener(e -> iniciarJuego());
        agrega(p, btnIniciar);

        return p;
    }

    // ── Sidebar de juego ─────────────────────────────────────────────────────
    private JPanel crearSidebarJuego() {
        JPanel p = sidebar();

        agrega(p, label("En juego", 15, Font.BOLD, C_TEXT));
        p.add(gap(10));
        agrega(p, label("Neo y los agentes se mueven solos.\nGana el hilo más rápido.", 11, Font.PLAIN, C_SUB));
        p.add(gap(16));

        // Leyenda
        Object[][] leyenda = {
            {"N", new Color(48, 210, 108), "Neo"},
            {"A", new Color(210, 54, 54), "Agente"},
            {"T", new Color(48, 148, 220), "Teléfono"},
            {"#", new Color(78, 80, 96), "Muro"},};
        for (Object[] fila : leyenda) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            row.setBackground(C_PANEL);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
            row.setAlignmentX(LEFT_ALIGNMENT);
            JLabel ico = new JLabel((String) fila[0]);
            ico.setForeground((Color) fila[1]);
            ico.setFont(new Font("SansSerif", Font.BOLD, 13));
            JLabel lbl = new JLabel((String) fila[2]);
            lbl.setForeground(C_SUB);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
            row.add(ico);
            row.add(lbl);
            p.add(row);
            p.add(gap(4));
        }

        p.add(Box.createVerticalGlue());

        JButton btnRendirse = boton("Rendirse", new Color(100, 35, 35));
        btnRendirse.addActionListener(e -> tablero.finalizarJuego("Juego detenido."));
        agrega(p, btnRendirse);

        return p;
    }

    // ── Lógica de setup ──────────────────────────────────────────────────────
    private void manejarClick(int px, int py) {
        if (modoColocacion.isEmpty()) {
            return;
        }
        int[] celda = panelTablero.pixelACelda(px, py);
        if (celda == null) {
            return;
        }
        int f = celda[0], c = celda[1];
        char actual = tablero.getCell(f, c);

        switch (modoColocacion) {
            case "N":
                if (actual == 'N') {
                    tablero.limpiarCelda(f, c);
                } else if (actual == '.') {
                    quitarNeoExistente();
                    tablero.colocarNeo(f, c);
                }
                break;
            case "A":
                if (actual == 'A') {
                    tablero.limpiarCelda(f, c);
                } else if (actual == '.' && contarEn('A') < numAgentes) {
                    tablero.colocarAgente(f, c);
                }
                break;
            case "T":
                if (actual == 'T') {
                    tablero.limpiarCelda(f, c); 
                }else if (actual == '.') {
                    tablero.colocarTelefono(f, c);
                }
                break;
            case "#":
                if (actual == '#') {
                    tablero.limpiarCelda(f, c); 
                }else if (actual == '.') {
                    tablero.colocarMuro(f, c);
                }
                break;
        }
        panelTablero.actualizar(tablero.getSnapshot());
        actualizarEstado();
    }
    
    private void generarAleatorio() {
        tablero.reset();
        
        // Colocar Neo
        ponerRandom('N');
        // Colocar Teléfono
        ponerRandom('T');
        // Colocar Agentes
        for (int i = 0; i < numAgentes; i++) ponerRandom('A');
        // Colocar Muros (unos 8 muros)
        for (int i = 0; i < 8; i++) ponerRandom('#');
        
        panelTablero.actualizar(tablero.getSnapshot());
        actualizarEstado();
    }

    private void ponerRandom(char ch) {
        while (true) {
            int f = (int) (Math.random() * tamanoActual);
            int c = (int) (Math.random() * tamanoActual);
            if (tablero.getCell(f, c) == '.') {
                if (ch == 'N') tablero.colocarNeo(f, c);
                else if (ch == 'A') tablero.colocarAgente(f, c);
                else if (ch == 'T') tablero.colocarTelefono(f, c);
                else if (ch == '#') tablero.colocarMuro(f, c);
                break;
            }
        }
    }

    private void quitarNeoExistente() {
        char[][] snap = tablero.getSnapshot();
        for (int i = 0; i < tamanoActual; i++) {
            for (int j = 0; j < tamanoActual; j++) {
                if (snap[i][j] == 'N') {
                    tablero.limpiarCelda(i, j);
                    return;
                }
            }
        }
    }

    private int contarEn(char ch) {
        int n = 0;
        for (char[] row : tablero.getSnapshot()) {
            for (char c : row) {
                if (c == ch) {
                    n++;
                }
            }
        }
        return n;
    }

    private void actualizarEstado() {
        int nNeo = contarEn('N');
        int nTel = contarEn('T');
        int nAgt = contarEn('A');
        lblEstado.setText(String.format(
                "<html>Neo: %s<br>Teléfonos: %d<br>Agentes: %d / %d</html>",
                nNeo > 0 ? "✓" : "✗", nTel, nAgt, numAgentes));
        btnIniciar.setEnabled(nNeo > 0 && nTel > 0 && nAgt == numAgentes);
    }

    private void limpiarTablero() {
        tablero.reset();
        panelTablero.actualizar(tablero.getSnapshot());
        actualizarEstado();
    }

    // ── Arranque del juego ───────────────────────────────────────────────────
    private void iniciarJuego() {
        juegoEnCurso = true;
        finMostrado = false;

        // Localizar agentes en el snapshot y crear hilos
        char[][] snap = tablero.getSnapshot();
        agentes = new Agente[numAgentes];
        int id = 0;
        outer:
        for (int i = 0; i < tamanoActual; i++) {
            for (int j = 0; j < tamanoActual; j++) {
                if (snap[i][j] == 'A') {
                    agentes[id] = new Agente(tablero, id, i, j);
                    if (++id == numAgentes) {
                        break outer;
                    }
                }
            }
        }

        neo = new Neo(tablero);

        // Cambiar sidebar
        cambiarEast(crearSidebarJuego());

        neo.start();
        for (Agente a : agentes) {
            a.start();
        }
    }

    // ── Refresco de la UI ────────────────────────────────────────────────────
    private void refrescar() {
        panelTablero.actualizar(tablero.getSnapshot());
        if (!juegoEnCurso) {
            actualizarEstado();
        }
        if (tablero.juegoTerminado() && !finMostrado) {
            finMostrado = true;
            mostrarFin();
        }
    }

    private void mostrarFin() {
        juegoEnCurso = false;

        String msg = tablero.getMensajeFinal();
        int op = JOptionPane.showOptionDialog(
                this, msg, "Fin del juego",
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[]{"Jugar de nuevo", "Salir"}, "Jugar de nuevo");

        if (op == JOptionPane.YES_OPTION) {
            reiniciar(); 
        }else {
            System.exit(0);
        }
    }

    private void reiniciar() {
        if (neo != null) {
            neo.interrupt();
        }
        if (agentes != null) {
            for (Agente a : agentes) {
                a.interrupt();
            }
        }

        tablero = new Tablero(tamanoActual);
        tablero.setOnCambio(() -> SwingUtilities.invokeLater(this::refrescar));
        modoColocacion = "";
        numAgentes = 2;

        // Limpiar mouse listeners del panel y volver a agregar el de setup
        for (MouseListener ml : panelTablero.getMouseListeners()) {
            panelTablero.removeMouseListener(ml);
        }
        panelTablero.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!juegoEnCurso) {
                    manejarClick(e.getX(), e.getY());
                }
            }
        });

        panelTablero.actualizar(tablero.getSnapshot());
        cambiarEast(crearSidebarSetup());
    }

    private void cambiarTamanoTablero() {
        tablero = new Tablero(tamanoActual);
        tablero.setOnCambio(() -> SwingUtilities.invokeLater(this::refrescar));
        remove(panelTablero);
        panelTablero = new PanelTablero(tamanoActual);
        panelTablero.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!juegoEnCurso) {
                    manejarClick(e.getX(), e.getY());
                }
            }
        });
        add(panelTablero, BorderLayout.CENTER);
        pack();
        revalidate();
        repaint();
        actualizarEstado();
    }

    // ── Utilidades ───────────────────────────────────────────────────────────
    private void cambiarEast(JPanel nuevo) {
        BorderLayout bl = (BorderLayout) getContentPane().getLayout();
        Component viejo = bl.getLayoutComponent(BorderLayout.EAST);
        if (viejo != null) {
            remove(viejo);
        }
        add(nuevo, BorderLayout.EAST);
        revalidate();
        repaint();
    }

    private JPanel sidebar() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(C_PANEL);
        p.setBorder(new EmptyBorder(14, 14, 14, 14));
        p.setPreferredSize(new Dimension(195, 0));
        return p;
    }

    private void agrega(JPanel p, JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        p.add(c);
    }

    private Component gap(int h) {
        return Box.createRigidArea(new Dimension(0, h));
    }

    private JLabel label(String txt, int size, int style, Color color) {
        JLabel l = new JLabel("<html>" + txt + "</html>");
        l.setFont(new Font("SansSerif", style, size));
        l.setForeground(color);
        return l;
    }

    private JToggleButton toggle(String texto, Color acento) {
        JToggleButton btn = new JToggleButton(texto);
        btn.setBackground(new Color(38, 38, 56));
        btn.setForeground(C_TEXT);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        
        btn.addChangeListener(e -> {
            if (btn.isSelected()) btn.setBackground(acento.darker());
            else btn.setBackground(new Color(38, 38, 56));
        });
        
        return btn;
    }

    private JButton boton(String texto, Color fondo) {
        JButton btn = new JButton(texto);
        btn.setBackground(fondo);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        return btn;
    }

    private void estilizarSpinner(JSpinner s) {
        Color bg = new Color(38, 38, 56);
        s.setBackground(bg);
        JFormattedTextField tf = ((JSpinner.DefaultEditor) s.getEditor()).getTextField();
        tf.setBackground(bg);
        tf.setForeground(C_TEXT);
        tf.setCaretColor(C_TEXT);
    }
}
