import gen.*;
import sim.*;

// Clase principal del simulador
public class MemoriaVirtual {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso:");
            System.out.println("Opcion 1: java -cp bin MemoriaVirtual config.txt");
            System.out.println("Opcion 2: java -cp bin MemoriaVirtual <marcos> <procesos> >> output.txt");
            return;
        }

        if (args.length == 1) {
            // Opcion 1: Generar referencias
            generarReferencias(args[0]);
        } else if (args.length == 2) {
            // Opcion 2: Simular ejecucion
            int marcos = Integer.parseInt(args[0]);
            int procesos = Integer.parseInt(args[1]);
            simularEjecucion(marcos, procesos);
        }
    }

    private static void generarReferencias(String archivoConfig) {
        ConfiguracionSistema config = new ConfiguracionSistema(archivoConfig);
        GeneradorReferencias generador = new GeneradorReferencias(config);
        generador.generarArchivos();
    }

    private static void simularEjecucion(int marcos, int procesos) {
        SimuladorMemoria simulador = new SimuladorMemoria(marcos, procesos);
        simulador.ejecutar();
    }
}
