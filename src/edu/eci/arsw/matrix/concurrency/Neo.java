package edu.eci.arsw.matrix.concurrency;

import edu.eci.arsw.matrix.core.Tablero;
import java.util.ArrayList;
import java.util.List;

public class Neo extends PersonajeMovil {

    public Neo(Tablero tablero) {
        super(tablero);
    }

    @Override
    protected void moverUnPaso() {
        int[] pos = tablero.getPosicionNeo();
        if (pos[0] == -1) return;

        List<int[]> camino = caminoAltelefonoCercano(pos[0], pos[1]);

        if (camino.size() > 1) {
            int[] sig = camino.get(1);
            tablero.moverNeo(sig[0], sig[1]);
        }
    }

    /** Encuentra el camino más corto al teléfono más cercano. */
    private List<int[]> caminoAltelefonoCercano(int f, int c) {
        List<int[]> telefonos = tablero.getTelefonos();
        List<int[]> mejor = null;
        int minLen = Integer.MAX_VALUE;

        for (int[] tel : telefonos) {
            char[][] snap = tablero.getSnapshot();
            List<int[]> camino = calcularRuta(snap, f, c, tel[0], tel[1], true);
            if (!camino.isEmpty() && camino.size() < minLen) {
                minLen = camino.size();
                mejor = camino;
            }
        }
        return mejor != null ? mejor : new ArrayList<>();
    }
}
