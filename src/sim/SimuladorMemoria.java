package sim;

import java.util.*;

// Clase principal para la simulacion de memoria
public class SimuladorMemoria {
    private int totalMarcos;
    private int numeroProcesos;
    private List<Proceso> procesos;
    private Queue<Proceso> colaProcesos;

    public SimuladorMemoria(int totalMarcos, int numeroProcesos) {
        this.totalMarcos = totalMarcos;
        this.numeroProcesos = numeroProcesos;
        this.procesos = new ArrayList<>();
        this.colaProcesos = new LinkedList<>();
    }

    public void ejecutar() {
        // Fase de inicializacion
        inicializarProcesos();
        asignarMarcosIniciales();

        // Fase de simulacion
        simularEjecucion();

        // Mostrar estadisticas finales
        mostrarEstadisticas();
    }

    private void inicializarProcesos() {
        for (int i = 0; i < numeroProcesos; i++) {
            String archivo = "proc" + i + ".txt";
            System.out.println("PROC " + i + " == Leyendo archivo de configuracion ==");

            Proceso proceso = new Proceso(i, archivo);
            procesos.add(proceso);
            colaProcesos.offer(proceso);

            System.out.println("PROC " + i + " == Termino de leer archivo de configuracion ==");
        }
    }

    private void asignarMarcosIniciales() {
        int marcosPorProceso = totalMarcos / numeroProcesos;
        int marcoActual = 0;

        for (Proceso proceso : procesos) {
            for (int i = 0; i < marcosPorProceso; i++) {
                proceso.getMarcosAsignados().add(marcoActual);
                System.out.println("Proceso " + proceso.getId() + ": recibe marco " + marcoActual);
                marcoActual++;
            }
        }
    }

    private void simularEjecucion() {
        int numeroLinea = 0;

        while (!colaProcesos.isEmpty()) {
            Proceso procesoActual = colaProcesos.poll();

            if (!procesoActual.tieneReferencias()) {
                finalizarProceso(procesoActual);
                continue;
            }

            System.out.println("Turno proc: " + procesoActual.getId());

            int referenciaIndex = procesoActual.getReferenciaActual();
            ReferenciaMemoria referencia = procesoActual.siguienteReferencia();
            int paginaVirtual = referencia.getPaginaVirtual();

            System.out.println("PROC " + procesoActual.getId() + " analizando linea_: " + numeroLinea++);

            if (procesoActual.paginaCargada(paginaVirtual)) {
                // Hit de pagina - only count as hit if this reference never caused a fault
                if (!procesoActual.referenciaYaTuvoFalta(referenciaIndex)) {
                    procesoActual.incrementarHits();
                }
                procesoActual.actualizarAcceso(paginaVirtual);
                System.out.println("PROC " + procesoActual.getId() + " hits: " + procesoActual.getHits());
            } else {
                // Falla de pagina - mark this reference as having caused a fault
                procesoActual.marcarReferenciaConFalta(referenciaIndex);
                manejarFallaPagina(procesoActual, paginaVirtual);

                // El proceso debe esperar un turno despues de una falla
                procesoActual.decrementarReferenciaActual();
            }

            // Envejecimiento (actualizar contadores LRU)
            System.out.println("PROC " + procesoActual.getId() + " envejecimiento");

            // Re-encolar el proceso si aun tiene referencias
            if (procesoActual.tieneReferencias()) {
                colaProcesos.offer(procesoActual);
            } else {
                finalizarProceso(procesoActual);
            }
        }
    }

    private void manejarFallaPagina(Proceso proceso, int paginaVirtual) {
        proceso.incrementarFallas();
        System.out.println("PROC " + proceso.getId() + " falla de pag: " + paginaVirtual);

        Set<Integer> marcosDelProceso = proceso.getMarcosAsignados();

        // Buscar un marco libre asignado al proceso
        int marcoLibre = -1;
        for (int marco : marcosDelProceso) {
            if (!proceso.getTablaPaginas().containsValue(marco)) {
                marcoLibre = marco;
                break;
            }
        }

        if (marcoLibre != -1) {
            // Marco libre disponible - sin reemplazo
            proceso.cargarPagina(paginaVirtual, marcoLibre);
            proceso.incrementarSwap(); // Un acceso a SWAP
        } else {
            // Todos los marcos estan ocupados - necesario reemplazo
            int marcoVictima = proceso.obtenerMarcoLRU();

            // Remover pagina victima
            proceso.removerPaginaEnMarco(marcoVictima);

            // Cargar nueva pagina
            proceso.cargarPagina(paginaVirtual, marcoVictima);
            proceso.incrementarSwap(); // Dos accesos a SWAP (escribir victima y leer nueva)
            proceso.incrementarSwap();
        }
    }

    private void finalizarProceso(Proceso proceso) {
        System.out.println("========================");
        System.out.println("Termino proc: " + proceso.getId());
        System.out.println("========================");

        // Liberar marcos del proceso y reasignar al proceso con mas fallas
        if (!colaProcesos.isEmpty()) {
            Proceso procesoConMasFallas = encontrarProcesoConMasFallas();
            if (procesoConMasFallas != null) {
                for (int marco : proceso.getMarcosAsignados()) {
                    System.out.println("PROC " + proceso.getId() + " removiendo marco: " + marco);
                    procesoConMasFallas.getMarcosAsignados().add(marco);
                    System.out.println("PROC " + procesoConMasFallas.getId() +
                            " asignando marco nuevo " + marco);
                }
            }
        } else {
            // Es el ultimo proceso, solo mostrar que se liberan los marcos
            for (int marco : proceso.getMarcosAsignados()) {
                System.out.println("PROC " + proceso.getId() + " removiendo marco: " + marco);
            }
        }
    }

    private Proceso encontrarProcesoConMasFallas() {
        Proceso procesoConMasFallas = null;
        int maxFallas = -1;

        for (Proceso proceso : colaProcesos) {
            if (proceso.getFallas() > maxFallas) {
                maxFallas = proceso.getFallas();
                procesoConMasFallas = proceso;
            }
        }

        return procesoConMasFallas;
    }

    private void mostrarEstadisticas() {
        System.out.println("\n=== DATOS SALIDA ===");
        for (Proceso proceso : procesos) {
            proceso.imprimirEstadisticas();
            System.out.println();
        }
    }
}