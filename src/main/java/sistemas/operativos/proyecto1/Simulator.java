package sistemas.operativos.proyecto1;

import sistemas.operativos.proyecto1.process.ProcessType;

/**
 * Clase simulador del proyecto.
 * @author Sebastián
 */
public class Simulator {
    private Config config;
    private CPU cpu = new CPU(config);
    
    /**
     * Constructor.
     */
    public Simulator() {
        this.config = new Config(100);
        this.cpu = new CPU(config);
    }
    
    /**
     * Constructor.
     * @param config Configuración del simulador.
     */
    public Simulator(Config config) {
        this.config = config;
        this.cpu = new CPU(config);
    }
    
    /**
     * Inicia la simulación, ejecutando la función de ejecución de ciclo
     * adecuada, dependiendo de la política de planificación actual.
     */
    public void startSimulation() {
        switch(config.getPolicy()) {
            case PlanPolicy.FCFS -> {
                System.out.println("First-Come, First-Served");  
                for (int i = 1; i < config.getCyclesAmount() + 1; i++) {
                    cpu.simulateCycleFCFS();
                    System.out.print("Ciclo: ");  
                    System.out.println(i);  
                }
            }
            case RR -> {
                System.out.println("Round Robin");  
                for (int i = 1; i < config.getCyclesAmount() + 1; i++) {
                    cpu.simulateCycleRR();
                    System.out.print("Ciclo: ");  
                    System.out.println(i);  
                }
            }
            case SPN -> {
            }
            case SRT -> {
            }
            case PRI -> {
                System.out.println("Cola por prioridad");  
                for (int i = 1; i < config.getCyclesAmount() + 1; i++) {
                    cpu.simulateCyclePRI();
                    System.out.print("Ciclo: ");  
                    System.out.println(i);  
                }
            }
            case MFQ -> {
            }
            default -> throw new AssertionError(config.getPolicy().name());
        }
    }
    
    /**
     * Crea un proceso dentro del simulador para ser usado y ejecutado dentro
     * del procesador.
     * @param name Nombre del proceso.
     * @param arrivalTime Tiempo de llegada del proceso.
     * @param instructions Cantidad de instrucciones del proceso.
     * @param type Tipo de proceso [CPU_BOUND - IO_BOUND].
     * @param cyclesForException Ciclos necesarios para generar una excepción.
     * @param cyclesToSatisfy Ciclos necesarios para satisfacer dicha excepción.
     * @param priority Nivel de prioridad del proceso.
     */
    public void createProcess(String name, int arrivalTime, int instructions, ProcessType type, int cyclesForException, int cyclesToSatisfy, int priority) {
        switch(config.getPolicy()) {
            case PRI -> cpu.createPriorityProcess(name, arrivalTime, instructions, type, cyclesForException, cyclesToSatisfy, priority);
            default -> cpu.createProcess(name, arrivalTime, instructions, type, cyclesForException, cyclesToSatisfy, priority);
        }
    }
}
