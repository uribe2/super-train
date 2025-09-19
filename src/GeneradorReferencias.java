import java.io.*;

// Clase para generar las referencias de memoria
class GeneradorReferencias {
    private ConfiguracionSistema config;

    public GeneradorReferencias(ConfiguracionSistema config) {
        this.config = config;
    }

    public void generarArchivos() {
        for (int i = 0; i < config.getNumeroProcesos(); i++) {
            generarArchivoProceso(i, config.getTamanosMatrices()[i]);
        }
    }

    private void generarArchivoProceso(int numProceso, String tamanoMatriz) {
        String[] dimensiones = tamanoMatriz.split("x");
        int filas = Integer.parseInt(dimensiones[0]);
        int columnas = Integer.parseInt(dimensiones[1]);

        String nombreArchivo = "proc" + numProceso + ".txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(nombreArchivo))) {
            // Escribir metadatos
            int numReferencias = 3 * filas * columnas; // 3 matrices
            int bytesMatriz = filas * columnas * 4; // 4 bytes por entero
            int totalBytes = 3 * bytesMatriz;
            int numPaginas = (int) Math.ceil((double) totalBytes / config.getTamanoPagina());

            writer.println("TP=" + config.getTamanoPagina());
            writer.println("NF=" + filas);
            writer.println("NC=" + columnas);
            writer.println("NR=" + numReferencias);
            writer.println("NP=" + numPaginas);

            // Generar referencias para suma de matrices
            generarReferenciasSumaMatrices(writer, filas, columnas);

        } catch (IOException e) {
            System.err.println("Error escribiendo archivo " + nombreArchivo + ": " + e.getMessage());
        }
    }

    private void generarReferenciasSumaMatrices(PrintWriter writer, int filas, int columnas) {
        int tamPagina = config.getTamanoPagina();
        int bytesMatriz = filas * columnas * 4;

        // Simular el algoritmo de suma de matrices
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                // Acceso a M1[i][j] (lectura)
                int posicionM1 = (i * columnas + j) * 4;
                int paginaM1 = posicionM1 / tamPagina;
                int desplazamientoM1 = posicionM1 % tamPagina;
                writer.println("M1:[" + i + "-" + j + "]," + paginaM1 + "," + desplazamientoM1 + ",r");

                // Acceso a M2[i][j] (lectura)
                int posicionM2 = bytesMatriz + (i * columnas + j) * 4;
                int paginaM2 = posicionM2 / tamPagina;
                int desplazamientoM2 = posicionM2 % tamPagina;
                writer.println("M2:[" + i + "-" + j + "]," + paginaM2 + "," + desplazamientoM2 + ",r");

                // Acceso a M3[i][j] (escritura)
                int posicionM3 = 2 * bytesMatriz + (i * columnas + j) * 4;
                int paginaM3 = posicionM3 / tamPagina;
                int desplazamientoM3 = posicionM3 % tamPagina;
                writer.println("M3:[" + i + "-" + j + "]," + paginaM3 + "," + desplazamientoM3 + ",w");
            }
        }
    }
}
