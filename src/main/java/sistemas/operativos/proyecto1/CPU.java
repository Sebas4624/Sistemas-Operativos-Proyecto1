package sistemas.operativos.proyecto1;

import java.util.Iterator;
import sistemas.operativos.proyecto1.lib.Queue;
import sistemas.operativos.proyecto1.process.Process;
import sistemas.operativos.proyecto1.process.ProcessType;

/**
 *
 * @author Sebastián
 */
public class CPU {
    private final Queue<Process> readyQueue;
    private final Queue<Process> ioQueue;
    private Process currentProcess;
    private final Config config;
    private long simulationTime;
    
    public CPU(Config config) {
        this.readyQueue = new Queue();
        this.ioQueue = new Queue();
        this.config = config;
        this.simulationTime = 0;
    }
    
    // Método para crear procesos
    public void createProcess(String name, int arrivalTime, int instructions, ProcessType type, int cyclesForException, int cyclesToSatisfy, int priority) {
        String id = java.time.LocalTime.now().toString();
        
        Process process = new Process(id, name, arrivalTime, instructions, type, cyclesForException, cyclesToSatisfy, priority);
        readyQueue.enqueue(process);
        
        System.out.println("Proceso creado: " + name);  ///////////////////////////
    }
    
    /**
     *
     *   Política de Planificación FCFS
     *
     */
    
    public void simulateCycleFCFS() {
        simulationTime++;
        
        // 1. Procesar I/O
        processIOQueue();
        
        //System.out.println(currentProcess);  ///////////////////////////
        //if (currentProcess != null) System.out.println(currentProcess.currentState());  ///////////////////////////
        //if (currentProcess != null) System.out.println(currentProcess.remaining());  ///////////////////////////
        //if (currentProcess != null) System.out.println(currentProcess.toString());  ///////////////////////////
        
        // 2. Planificar siguiente proceso (si no hay uno actual)
        if (currentProcess == null || currentProcess.isFinished() || 
            currentProcess.isBlockedIO()) {
            scheduleNextProcess();
        }
        
        // 3. Ejecutar proceso actual
        if (currentProcess != null && (currentProcess.isReady() || currentProcess.isRunning())) {
            currentProcess.setRunning();
            boolean executed = currentProcess.executeInstruction();
            //System.out.println("Instrucción ejecutada");  ///////////////////////////
            
            if (currentProcess.isBlockedIO()) {
                ioQueue.enqueue(currentProcess);
                currentProcess = null;
                //System.out.println("Proceso bloqueado");  ///////////////////////////
            } else if (currentProcess.isFinished()) {
                System.out.println("Proceso " + currentProcess.name() + " terminado.");
                currentProcess = null;
            }
        }
        
        // 4. Esperar según la duración del ciclo configurada
        try {
            Thread.sleep(config.getCycleDuration());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     *
     *   Política de Planificación FCFS
     *
     */
    
    public void simulateCycleRR() {
        
    }
    
    /**
     *
     *   Funciones para IO y planificar nuevos procesos
     *
     */
    
    private void processIOQueue() {
        Iterator<Process> iterator = ioQueue.iterator();
        while (iterator.hasNext()) {
            Process process = iterator.next();
            boolean ioCompleted = process.processIOCycle();
            
            if (ioCompleted) {
                // iterator.remove();
                ioQueue.dequeue();
                readyQueue.enqueue(process);
                System.out.println("I/O completado para: " + process.name());
            }
        }
    }
    
    private void scheduleNextProcess() {
        if (!readyQueue.isEmpty()) {
            currentProcess = readyQueue.dequeue();
        }
    }
}
