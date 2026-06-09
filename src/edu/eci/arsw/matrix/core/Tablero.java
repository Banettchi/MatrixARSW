package edu.eci.arsw.matrix.core;

import java.util.*;

public class Tablero {
    private char[][] matriz;
    private final int tamano;
    private int filaNeo = -1, colNeo = -1;
    private List<int[]> telefonos = new ArrayList<>();
    private boolean terminado = false;
    private String mensajeFinal = "";
    private Runnable onCambio;

    public Tablero(int tamano) {
        this.tamano = tamano;
        reset();
    }

    public synchronized void reset() {
        matriz = new char[tamano][tamano];
        telefonos = new ArrayList<>();
        terminado = false;
        mensajeFinal = "";
        filaNeo = -1; colNeo = -1;
        for (int i = 0; i < tamano; i++) Arrays.fill(matriz[i], '.');
    }

    public void setOnCambio(Runnable r) { this.onCambio = r; }

    private void notificar() {
        if (onCambio != null) onCambio.run();
    }

    public int getTamano() { return tamano; }

    public synchronized char[][] getSnapshot() {
        char[][] copia = new char[tamano][tamano];
        for (int i = 0; i < tamano; i++) copia[i] = Arrays.copyOf(matriz[i], tamano);
        return copia;
    }

    public synchronized char getCell(int f, int c) {
        return matriz[f][c];
    }

    public synchronized boolean limpiarCelda(int f, int c) {
        if (f < 0 || f >= tamano || c < 0 || c >= tamano) return false;
        char anterior = matriz[f][c];
        if (anterior == '.') return false;
        if (anterior == 'T') telefonos.removeIf(t -> t[0] == f && t[1] == c);
        if (anterior == 'N') { filaNeo = -1; colNeo = -1; }
        matriz[f][c] = '.';
        notificar();
        return true;
    }

    public synchronized boolean colocarMuro(int f, int c) {
        if (f < 0 || f >= tamano || c < 0 || c >= tamano || matriz[f][c] != '.') return false;
        matriz[f][c] = '#';
        notificar();
        return true;
    }

    public synchronized boolean colocarTelefono(int f, int c) {
        if (f < 0 || f >= tamano || c < 0 || c >= tamano || matriz[f][c] != '.') return false;
        matriz[f][c] = 'T';
        telefonos.add(new int[]{f, c});
        notificar();
        return true;
    }

    public synchronized boolean colocarNeo(int f, int c) {
        if (f < 0 || f >= tamano || c < 0 || c >= tamano || matriz[f][c] != '.') return false;
        matriz[f][c] = 'N';
        filaNeo = f; colNeo = c;
        notificar();
        return true;
    }

    public synchronized boolean colocarAgente(int f, int c) {
        if (f < 0 || f >= tamano || c < 0 || c >= tamano || matriz[f][c] != '.') return false;
        matriz[f][c] = 'A';
        notificar();
        return true;
    }

    public synchronized boolean juegoTerminado() { return terminado; }

    public synchronized String getMensajeFinal() { return mensajeFinal; }

    public synchronized void finalizarJuego(String mensaje) {
        if (terminado) return;
        terminado = true;
        mensajeFinal = mensaje;
        notificar();
    }

    public synchronized boolean moverNeo(int f, int c) {
        if (terminado) return false;
        if (f < 0 || f >= tamano || c < 0 || c >= tamano) return false;
        if (matriz[f][c] == '#') return false;

        if (matriz[f][c] == 'A') {
            finalizarJuego("Neo fue capturado por un agente.");
            return true;
        }

        if (matriz[f][c] == 'T') {
            matriz[filaNeo][colNeo] = '.';
            filaNeo = f; colNeo = c;
            matriz[filaNeo][colNeo] = 'N';
            finalizarJuego("Neo escapó por el teléfono.");
            return true;
        }

        matriz[filaNeo][colNeo] = '.';
        filaNeo = f; colNeo = c;
        matriz[filaNeo][colNeo] = 'N';
        notificar();
        return true;
    }

    public synchronized boolean moverAgente(int id, int fVieja, int cVieja, int f, int c) {
        if (terminado) return false;
        if (f < 0 || f >= tamano || c < 0 || c >= tamano) return false;
        if (matriz[f][c] == '#' || matriz[f][c] == 'T' || matriz[f][c] == 'A') return false;

        if (matriz[f][c] == 'N') {
            finalizarJuego("El agente " + id + " atrapó a Neo.");
            return true;
        }

        matriz[fVieja][cVieja] = '.';
        matriz[f][c] = 'A';
        notificar();
        return true;
    }

    /** BFS para Neo: evita muros y teléfonos, pero puede pasar por celdas con agentes. */


    public synchronized int[] getPosicionNeo() { return new int[]{filaNeo, colNeo}; }

    public synchronized List<int[]> getTelefonos() { return new ArrayList<>(telefonos); }
}
