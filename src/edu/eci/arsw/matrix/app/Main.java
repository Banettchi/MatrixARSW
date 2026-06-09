package edu.eci.arsw.matrix.app;

import edu.eci.arsw.matrix.ui.VentanaJuego;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(VentanaJuego::new);
    }
}
