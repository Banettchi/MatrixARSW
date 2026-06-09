package edu.eci.arsw.matrix.concurrency;

import edu.eci.arsw.matrix.core.Tablero;

/**
 * Clase base para todos los personajes del juego.
 * Todos comparten el mismo run(): dormir, verificar estado, calcular paso, moverse.
 * El Tablero es la memoria compartida abierta: todos ven la posición de todos.
 */
public abstract class PersonajeMovil extends Thread {
    protected final Tablero tablero;

    public PersonajeMovil(Tablero tablero) {
        this.tablero = tablero;
    }

    @Override
    public void run() {
        while (!tablero.juegoTerminado()) {
            try {
                Thread.sleep(1000 + (long)(Math.random() * 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            if (tablero.juegoTerminado()) break;
            moverUnPaso();
        }
    }

    /** Cada subclase decide hacia dónde se mueve. */
    protected abstract void moverUnPaso();

    /** 
     * Algoritmo de búsqueda BFS genérico para todos los personajes. 
     * Calcula la ruta más corta sobre una foto actual del tablero sin bloquear a los demás hilos.
     */
    protected java.util.List<int[]> calcularRuta(char[][] snap, int inicioF, int inicioC, int finF, int finC, boolean esNeo) {
        int tamano = snap.length;
        boolean[][] visto = new boolean[tamano][tamano];
        int[][] padreF = new int[tamano][tamano];
        int[][] padreC = new int[tamano][tamano];
        for (int i = 0; i < tamano; i++) { java.util.Arrays.fill(padreF[i], -1); java.util.Arrays.fill(padreC[i], -1); }

        java.util.Queue<int[]> cola = new java.util.LinkedList<>();
        cola.add(new int[]{inicioF, inicioC});
        visto[inicioF][inicioC] = true;
        int[][] dirs = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};

        while (!cola.isEmpty()) {
            int[] actual = cola.poll();
            int f = actual[0], c = actual[1];
            
            if (f == finF && c == finC) {
                java.util.List<int[]> camino = new java.util.ArrayList<>();
                int rf = f, rc = c;
                while (rf != -1 && rc != -1) {
                    camino.add(0, new int[]{rf, rc});
                    int tf = padreF[rf][rc], tc = padreC[rf][rc];
                    rf = tf; rc = tc;
                }
                return camino;
            }

            for (int[] d : dirs) {
                int nf = f + d[0], nc = c + d[1];
                if (nf >= 0 && nf < tamano && nc >= 0 && nc < tamano && !visto[nf][nc]) {
                    boolean esDestino = (nf == finF && nc == finC);
                    boolean transitable;
                    
                    if (esNeo) {
                        // Neo evita muros (y teléfonos intermedios, aunque a él no le importan los agentes)
                        transitable = (snap[nf][nc] != '#');
                    } else {
                        // Agentes evitan muros, teléfonos y a otros agentes
                        transitable = (snap[nf][nc] != '#' && snap[nf][nc] != 'T' && snap[nf][nc] != 'A');
                    }
                    
                    if (transitable || esDestino) {
                        visto[nf][nc] = true;
                        padreF[nf][nc] = f; padreC[nf][nc] = c;
                        cola.add(new int[]{nf, nc});
                    }
                }
            }
        }
        return new java.util.ArrayList<>();
    }
}
