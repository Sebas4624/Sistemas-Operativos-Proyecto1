package sistemas.operativos.proyecto1;

import sistemas.operativos.proyecto1.process.ProcessType;

/**
 *
 * @author Sebastián
 */
public class Simulator {
    private Config config;
    private CPU cpu = new CPU(config);
    
    public Simulator() {
        this.config = new Config(100);
        this.cpu = new CPU(config);
    }
    
    public Simulator(Config config) {
        this.config = config;
        this.cpu = new CPU(config);
    }
    
    public void startSimulation() {
        for (int i = 1; i < config.getCyclesAmount() + 1; i++) {
            cpu.simulateCycle();
            System.out.print("Cycle: ");  ///////////////////////////
            System.out.println(i);  ///////////////////////////
        }
    }
    
    public void createProcess(String name, int arrivalTime, int instructions, ProcessType type, int cyclesForException, int cyclesToSatisfy, int priority) {
        cpu.createProcess(name, arrivalTime, instructions, type, cyclesForException, cyclesToSatisfy, priority);
    }
}
