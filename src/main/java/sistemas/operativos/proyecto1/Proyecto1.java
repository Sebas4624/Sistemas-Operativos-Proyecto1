package sistemas.operativos.proyecto1;

import sistemas.operativos.proyecto1.process.ProcessType;

/**
 *
 * @author Sebastián
 * @author Nicole
 */
public class Proyecto1 {

    public static void main(String[] args) {
        Config config = new Config(200, PlanPolicy.FCFS);
        Simulator sim = new Simulator(config);
        
        // Crear procesos de ejemplo
        // CPU Bound: 20 instrucciones, sin I/O
        sim.createProcess("Proceso_CPU_1", 0, 20, ProcessType.CPU_BOUND, 0, 0, 1);
        
        // I/O Bound: 30 instrucciones, I/O cada 10 ciclos, servicio de 3 ciclos
        sim.createProcess("Proceso_IO_1", 0, 30, ProcessType.IO_BOUND, 10, 3, 4);
        
        // CPU Bound: 25 instrucciones, sin I/O
        sim.createProcess("Proceso_CPU_2", 0, 25, ProcessType.CPU_BOUND, 0, 0, 7);
        
        // CPU Bound: 40 instrucciones, sin I/O
        sim.createProcess("Proceso_CPU_3", 0, 40, ProcessType.CPU_BOUND, 0, 0, 5);
        
        // I/O Bound: 20 instrucciones, I/O cada 10 ciclos, servicio de 3 ciclos
        sim.createProcess("Proceso_IO_2", 0, 30, ProcessType.IO_BOUND, 5, 5, 2);
        
        // Start Simulation
        sim.startSimulation();
    }
}
