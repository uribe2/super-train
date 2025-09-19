package sim;

import java.io.*;
import java.util.*;

// Clase para representar un proceso
public class Proceso {
    private int id;
    private List<ReferenciaMemoria> referencias;
    private Map<Integer, Integer> tablaPaginas; // pagina virtual -> marco fisico
    private Set<Integer> marcosAsignados;
    private int referenciaActual;
    private int fallas;
    private int hits;
    private int accesosSwap;
    private int referenciasProcesadas; // Total references processed
    private Set<Integer> referenciasConFalta; // Track references that caused faults
    private Map<Integer, Long> tiempoAcceso; // Para LRU
    private long contadorTiempo;

    public Proceso(int id, String archivoReferencias) {
        this.id = id;
        this.referencias = new ArrayList<>();
        this.tablaPaginas = new HashMap<>();
        this.marcosAsignados = new HashSet<>();
        this.referenciaActual = 0;
        this.fallas = 0;
        this.hits = 0;
        this.accesosSwap = 0;
        this.referenciasProcesadas = 0;
        this.referenciasConFalta = new HashSet<>();
        this.tiempoAcceso = new HashMap<>();
        this.contadorTiempo = 0;

        cargarReferencias(archivoReferencias);
    }

    private void cargarReferencias(String archivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            // Leer y mostrar metadatos
            while ((linea = br.readLine()) != null) {
                if (linea.startsWith("TP=")) {
                    System.out.println("PROC " + id + " leyendo TP. Tam Paginas: " + linea.substring(3));
                } else if (linea.startsWith("NF=")) {
                    System.out.println("PROC " + id + " leyendo NF. Num Filas: " + linea.substring(3));
                } else if (linea.startsWith("NC=")) {
                    System.out.println("PROC " + id + " leyendo NC. Num Cols: " + linea.substring(3));
                } else if (linea.startsWith("NR=")) {
                    System.out.println("PROC " + id + " leyendo NR. Num Referencias: " + linea.substring(3));
                } else if (linea.startsWith("NP=")) {
                    System.out.println("PROC " + id + " leyendo NP. Num Paginas: " + linea.substring(3));
                } else if (linea.contains("[")) {
                    // Primera linea de referencias, procesarla y salir del bucle
                    procesarReferencia(linea);
                    break;
                }
            }

            // Procesar el resto de referencias
            while ((linea = br.readLine()) != null) {
                procesarReferencia(linea);
            }

        } catch (IOException e) {
            System.err.println("Error cargando referencias del proceso " + id + ": " + e.getMessage());
        }
    }

    private void procesarReferencia(String linea) {
        String[] partes = linea.split(",");
        int paginaVirtual = Integer.parseInt(partes[1]);
        int desplazamiento = Integer.parseInt(partes[2]);
        char operacion = partes[3].charAt(0);

        referencias.add(new ReferenciaMemoria(paginaVirtual, desplazamiento, operacion));
    }

    public boolean tieneReferencias() {
        return referenciaActual < referencias.size();
    }

    public ReferenciaMemoria siguienteReferencia() {
        return referencias.get(referenciaActual++);
    }

    public void decrementarReferenciaActual() {
        if (referenciaActual > 0)
            referenciaActual--;
    }

    // Metodos para manejar la tabla de paginas y estadisticas
    public boolean paginaCargada(int paginaVirtual) {
        return tablaPaginas.containsKey(paginaVirtual);
    }

    public void cargarPagina(int paginaVirtual, int marco) {
        tablaPaginas.put(paginaVirtual, marco);
        tiempoAcceso.put(marco, ++contadorTiempo);
    }

    public void actualizarAcceso(int paginaVirtual) {
        int marco = tablaPaginas.get(paginaVirtual);
        tiempoAcceso.put(marco, ++contadorTiempo);
    }

    public int obtenerMarcoLRU() {
        return tiempoAcceso.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(-1);
    }

    public void removerPaginaEnMarco(int marco) {
        // Encontrar y remover la pagina que esta en este marco
        Integer paginaARemover = null;
        for (Map.Entry<Integer, Integer> entrada : tablaPaginas.entrySet()) {
            if (entrada.getValue().equals(marco)) {
                paginaARemover = entrada.getKey();
                break;
            }
        }

        if (paginaARemover != null) {
            tablaPaginas.remove(paginaARemover);
            tiempoAcceso.remove(marco);
        }
    }

    public void incrementarFallas() {
        fallas++;
    }

    public void incrementarHits() {
        hits++;
    }

    public void incrementarSwap() {
        accesosSwap++;
    }

    public void incrementarReferenciasProcesadas() {
        referenciasProcesadas++;
    }

    public void marcarReferenciaConFalta(int indiceReferencia) {
        referenciasConFalta.add(indiceReferencia);
    }

    public boolean referenciaYaTuvoFalta(int indiceReferencia) {
        return referenciasConFalta.contains(indiceReferencia);
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getFallas() {
        return fallas;
    }

    public int getHits() {
        return hits;
    }

    public int getAccesosSwap() {
        return accesosSwap;
    }

    public int getTotalReferencias() {
        return referencias.size();
    }

    public int getReferenciasProcesadas() {
        return referenciasProcesadas;
    }

    public int getReferenciaActual() {
        return referenciaActual;
    }

    public Set<Integer> getMarcosAsignados() {
        return marcosAsignados;
    }

    public Map<Integer, Integer> getTablaPaginas() {
        return tablaPaginas;
    }

    public void imprimirEstadisticas() {
        int totalRefs = getTotalReferencias();
        double tasaFallas = (double) fallas / totalRefs;
        double tasaExito = (double) hits / totalRefs;

        System.out.println("Proceso: " + id);
        System.out.println("- Num referencias: " + totalRefs);
        System.out.println("- Fallas: " + fallas);
        System.out.println("- Hits: " + hits);
        System.out.println("- SWAP: " + accesosSwap);
        System.out.println("- Tasa fallas: " + String.format("%.4f", tasaFallas));
        System.out.println("- Tasa exito: " + String.format("%.4f", tasaExito));
    }
}
