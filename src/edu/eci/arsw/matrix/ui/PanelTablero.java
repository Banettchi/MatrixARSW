package edu.eci.arsw.matrix.ui;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PanelTablero extends JPanel {
    private final int tamano;
    private char[][] matriz;
    
    // Tema oscuro
    private static final Color C_BG = new Color(18, 18, 28);
    private static final Color C_CELL = new Color(38, 38, 56);
    private static final Color C_BORDER = new Color(30, 30, 46);
    
    private final Map<Character, Color> paleta;

    public PanelTablero(int tamano) {
        this.tamano = tamano;
        this.matriz = new char[tamano][tamano];
        for (int i = 0; i < tamano; i++) {
            for (int j = 0; j < tamano; j++) {
                matriz[i][j] = '.';
            }
        }
        
        paleta = new HashMap<>();
        paleta.put('N', new Color(48, 210, 108)); // Verde neón
        paleta.put('A', new Color(210, 54, 54));  // Rojo
        paleta.put('T', new Color(48, 148, 220)); // Azul
        paleta.put('#', new Color(78, 80, 96));   // Gris muros
        
        setPreferredSize(new Dimension(tamano * 60 + 20, tamano * 60 + 20));
        setBackground(C_BG);
    }

    public void actualizar(char[][] nuevaMatriz) {
        this.matriz = nuevaMatriz;
        repaint();
    }

    public int[] pixelACelda(int px, int py) {
        int w = getWidth() - 20;
        int h = getHeight() - 20;
        int cellSize = Math.min(w, h) / tamano;
        int offsetX = (getWidth() - (cellSize * tamano)) / 2;
        int offsetY = (getHeight() - (cellSize * tamano)) / 2;

        if (px < offsetX || px >= offsetX + (cellSize * tamano) || 
            py < offsetY || py >= offsetY + (cellSize * tamano)) {
            return null;
        }

        int c = (px - offsetX) / cellSize;
        int f = (py - offsetY) / cellSize;
        return new int[]{f, c};
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth() - 20;
        int h = getHeight() - 20;
        int cellSize = Math.min(w, h) / tamano;
        
        int offsetX = (getWidth() - (cellSize * tamano)) / 2;
        int offsetY = (getHeight() - (cellSize * tamano)) / 2;

        for (int i = 0; i < tamano; i++) {
            for (int j = 0; j < tamano; j++) {
                int x = offsetX + j * cellSize;
                int y = offsetY + i * cellSize;

                // Fondo celda
                g2.setColor(C_CELL);
                g2.fillRoundRect(x + 2, y + 2, cellSize - 4, cellSize - 4, 12, 12);
                
                // Borde celda
                g2.setColor(C_BORDER);
                g2.drawRoundRect(x + 2, y + 2, cellSize - 4, cellSize - 4, 12, 12);

                char elemento = matriz[i][j];
                if (elemento != '.') {
                    g2.setColor(paleta.getOrDefault(elemento, Color.WHITE));
                    int p = 8; // padding interior
                    g2.fillRoundRect(x + p, y + p, cellSize - 2*p, cellSize - 2*p, 8, 8);
                    
                    // Letra centrada
                    g2.setColor(C_BG);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 18));
                    FontMetrics fm = g2.getFontMetrics();
                    String txt = String.valueOf(elemento);
                    int txtX = x + (cellSize - fm.stringWidth(txt)) / 2;
                    int txtY = y + ((cellSize - fm.getHeight()) / 2) + fm.getAscent();
                    g2.drawString(txt, txtX, txtY);
                }
            }
        }
    }
}
