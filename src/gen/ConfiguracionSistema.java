package gen;

import java.io.*;

// Clase para manejar la configuracion del sistema
public class ConfiguracionSistema {
    private int tamanoPagina;
    private int numeroProcesos;
    private String[] tamanosMatrices;

    public ConfiguracionSistema(String archivo) {
        leerConfiguracion(archivo);
    }

    private void leerConfiguracion(String archivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.startsWith("TP=")) {
                    tamanoPagina = Integer.parseInt(linea.substring(3));
                } else if (linea.startsWith("NPROC=")) {
                    numeroProcesos = Integer.parseInt(linea.substring(6));
                } else if (linea.startsWith("TAMS=")) {
                    tamanosMatrices = linea.substring(5).split(",");
                }
            }
        } catch (IOException e) {
            System.err.println("Error leyendo archivo de configuracion: " + e.getMessage());
        }
    }

    // Getters
    public int getTamanoPagina() {
        return tamanoPagina;
    }

    public int getNumeroProcesos() {
        return numeroProcesos;
    }

    public String[] getTamanosMatrices() {
        return tamanosMatrices;
    }
}