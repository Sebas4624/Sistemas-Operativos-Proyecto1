package sistemas.operativos.proyecto1;

import sistemas.operativos.proyecto1.process.ProcessType;
import sistemas.operativos.proyecto1.sched.*;
import sistemas.operativos.proyecto1.gui.*;


import javax.swing.JFrame;

/**
 * Archivo "Main" del proyecto.
 * @author Sebastián
 * @author Nicole
 */
public class Proyecto1 {

    public static void main(String[] args) {
        Stats stats = new Stats();
        //Config config = new Config(2000, 100, PlanPolicy.FCFS, 20);
        Config config = new Config(200);   // ciclos
        config.setCycleDuration(100);       // ms por ciclo
        config.setQuantum(20);
         //Config config = new Config(200, 100, PlanPolicy.RR, 20);
        //Config config = new Config(200, 100, PlanPolicy.PRI, 20);
       
        Simulator sim = new Simulator(stats, config);
        JFrame simulatorView = new MainView(sim, stats);
        simulatorView.setVisible(true);
        
        //sim.setScheduler(new FCFS());   
        //sim.setScheduler(new RR(config));   
        //sim.setScheduler(new PRI());
        //sim.setScheduler(new SJF());
        //sim.setScheduler(new HRRN());
        //sim.setScheduler(new SRTF());

        // Crear procesos de ejemplo
        // CPU Bound: 20 instrucciones, sin I/O
        sim.createProcess("Proceso_CPU_1", 0, 20, ProcessType.CPU_BOUND, 0, 0, 1);
        
        // I/O Bound: 45 instrucciones, I/O cada 5 ciclos, servicio de 15 ciclos
        sim.createProcess("Proceso_IO_1", 10, 45, ProcessType.IO_BOUND, 5, 15, 4);
        
        // I/O Bound: 25 instrucciones, I/O cada 2 ciclos, servicio de 7 ciclos
        sim.createProcess("Proceso_IO_2", 200, 30, ProcessType.IO_BOUND, 2, 7, 9);
        
        // CPU Bound: 55 instrucciones, sin I/O
        sim.createProcess("Proceso_CPU_2", 1, 55, ProcessType.CPU_BOUND, 0, 0, 10);
        
        // I/O Bound: 40 instrucciones, I/O cada 10 ciclos, servicio de 15 ciclos
        sim.createProcess("Proceso_IO_3", 4, 40, ProcessType.IO_BOUND, 10, 15, 4);
        
        // I/O Bound: 20 instrucciones, I/O cada 2 ciclos, servicio de 8 ciclos
        sim.createProcess("Proceso_IO_4", 15, 20, ProcessType.IO_BOUND, 2, 8, 5);
        
        // CPU Bound: 65 instrucciones, sin I/O
        sim.createProcess("Proceso_CPU_3", 7, 65, ProcessType.CPU_BOUND, 0, 0, 4);
        
        // CPU Bound: 35 instrucciones, sin I/O
        sim.createProcess("Proceso_CPU_4", 0, 35, ProcessType.CPU_BOUND, 0, 0, 7);
        
        // CPU Bound: 30 instrucciones, sin I/O
        sim.createProcess("Proceso_CPU_5", 9, 30, ProcessType.CPU_BOUND, 0, 0, 12);
        
        // I/O Bound: 40 instrucciones, I/O cada 5 ciclos, servicio de 7 ciclos
        sim.createProcess("Proceso_IO_5", 2, 40, ProcessType.IO_BOUND, 5, 7, 2);
        
        // CPU Bound: 40 instrucciones, sin I/O
        sim.createProcess("Proceso_CPU_6", 0, 40, ProcessType.CPU_BOUND, 0, 0, 4);
                
        // Start Simulation
        //sim.startSimulation();
    }
}
