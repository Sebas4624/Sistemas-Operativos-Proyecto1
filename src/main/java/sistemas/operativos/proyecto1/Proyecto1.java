package sistemas.operativos.proyecto1;

import sistemas.operativos.proyecto1.process.ProcessType;

/**
 *
 * @author Sebastián
 * @author Nicole
 */
public class Proyecto1 {

    public static void main(String[] args) {
        Simulator sim = new Simulator();
        
        // Crear procesos de ejemplo
        // CPU Bound: 100 instrucciones, sin I/O
        sim.createProcess("Proceso_CPU", 0, 20, ProcessType.CPU_BOUND, 0, 0);
        
        // I/O Bound: 50 instrucciones, I/O cada 10 ciclos, servicio de 3 ciclos
        sim.createProcess("Proceso_IO", 0, 30, ProcessType.IO_BOUND, 10, 3);
        
        // Start Simulation
        sim.startSimulation();
    }
}
