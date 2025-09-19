package sim;

// Clase para representar una referencia de memoria
public class ReferenciaMemoria {
    private int paginaVirtual;
    private int desplazamiento;
    private char operacion; // 'r' para read, 'w' para write

    public ReferenciaMemoria(int paginaVirtual, int desplazamiento, char operacion) {
        this.paginaVirtual = paginaVirtual;
        this.desplazamiento = desplazamiento;
        this.operacion = operacion;
    }

    public int getPaginaVirtual() {
        return paginaVirtual;
    }

    public int getDesplazamiento() {
        return desplazamiento;
    }

    public char getOperacion() {
        return operacion;
    }
}
