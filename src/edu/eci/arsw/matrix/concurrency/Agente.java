package edu.eci.arsw.matrix.concurrency;

import edu.eci.arsw.matrix.core.Tablero;
import java.util.List;

public class Agente extends PersonajeMovil {
    private final int id;
    private int fila;
    private int columna;

    public Agente(Tablero tablero, int id, int filaInicial, int columnaInicial) {
        super(tablero);
        this.id = id;
        this.fila = filaInicial;
        this.columna = columnaInicial;
    }

    @Override
    protected void moverUnPaso() {
        int[] posNeo = tablero.getPosicionNeo();
        // Si Neo ya no está (fue capturado o escapó), posNeo será [-1, -1]
        if (posNeo[0] == -1) return;

        char[][] snap = tablero.getSnapshot();
        List<int[]> camino = calcularRuta(snap, fila, columna, posNeo[0], posNeo[1], false);

        if (camino.size() > 1) {
            int[] sig = camino.get(1);
            if (tablero.moverAgente(id, fila, columna, sig[0], sig[1])) {
                fila = sig[0];
                columna = sig[1];
            }
        }
    }
}
