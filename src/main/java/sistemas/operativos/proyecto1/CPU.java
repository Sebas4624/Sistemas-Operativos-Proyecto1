package sistemas.operativos.proyecto1;

import java.util.Iterator;
import java.util.NoSuchElementException;
import sistemas.operativos.proyecto1.lib.Queue;
import sistemas.operativos.proyecto1.lib.PriorityQueue;
import sistemas.operativos.proyecto1.process.Process;
import sistemas.operativos.proyecto1.process.ProcessType;

/**
 *
 * @author Sebastián
 */
public class CPU {
    private final Queue<Process> readyQueue;
    private final PriorityQueue<Process> readyPriorityQueue;
    private final Queue<Process> ioQueue;
    private Process currentProcess;
    private final Config config;
    private long simulationTime;
    
    public CPU(Config config) {
        this.readyQueue = new Queue();
        this.readyPriorityQueue = new PriorityQueue();
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
    
    // Método para crear procesos con prioridad
    public void createPriorityProcess(String name, int arrivalTime, int instructions, ProcessType type, int cyclesForException, int cyclesToSatisfy, int priority) {
        String id = java.time.LocalTime.now().toString();
        
        Process process = new Process(id, name, arrivalTime, instructions, type, cyclesForException, cyclesToSatisfy, priority);
        readyPriorityQueue.add(process);
        
        System.out.println("Proceso creado: " + name);  ///////////////////////////
    }
    
    /**
     *
     *   Política de Planificación FCFS (First-Come First-Served)
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
                System.out.println("Proceso " + currentProcess.name() +  " bloqueado.");  ///////////////////////////
                currentProcess = null;
            } else if (currentProcess.isFinished()) {
                System.out.println("¡Proceso " + currentProcess.name() + " terminado! :)");
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
     *   Política de Planificación RR (Round Robin)
     *
     */
    
    public void simulateCycleRR() {
        simulationTime++;
        
        // 1. Procesar I/O
        processIOQueue();
        
        // 2. Planificar siguiente proceso (si no hay uno actual)
        if (currentProcess == null || currentProcess.isFinished() || 
            currentProcess.isBlockedIO()) {
            scheduleNextProcess();
        }
        
        // 3. Ejecutar proceso actual
        if (currentProcess != null && (currentProcess.isReady() || currentProcess.isRunning())) {
            currentProcess.setRunning();
            boolean executed = currentProcess.executeInstruction();
            config.reduceRemainingQuantum();
            
            if (config.getRemainingQuantum() == 0) {
                readyQueue.enqueue(currentProcess);
                config.resetRemainingQuantum();
                System.out.println("Quantum del proceso " + currentProcess.name() + " terminado. Volviendo a poner en cola de listos.");  ///////////////////////////
                currentProcess = null;
            } else if (currentProcess.isBlockedIO()) {
                ioQueue.enqueue(currentProcess);
                System.out.println("Proceso " + currentProcess.name() +  " bloqueado.");  ///////////////////////////
                currentProcess = null;
            } else if (currentProcess.isFinished()) {
                System.out.println("¡Proceso " + currentProcess.name() + " terminado! :)");
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
     *   Política de Planificación PRI (Por prioridades; mayor número, mayor prioridad)
     *
     */
    
    public void simulateCyclePRI() {
        simulationTime++;
        
        // 1. Procesar I/O
        processIOPriorityQueue();
        
        // 2. Planificar siguiente proceso (si no hay uno actual)
        if (currentProcess == null || currentProcess.isFinished() || 
            currentProcess.isBlockedIO()) {
            scheduleNextPriorityProcess();
        }
        
        // 3. Ejecutar proceso actual
        if (currentProcess != null && (currentProcess.isReady() || currentProcess.isRunning())) {
            currentProcess.setRunning();
            boolean executed = currentProcess.executeInstruction();
            //System.out.println("Instrucción ejecutada");  ///////////////////////////
            
            if (currentProcess.isBlockedIO()) {
                ioQueue.enqueue(currentProcess);
                System.out.println("Proceso " + currentProcess.name() +  " bloqueado.");  ///////////////////////////
                currentProcess = null;
            } else if (currentProcess.isFinished()) {
                System.out.println("¡Proceso " + currentProcess.name() + " terminado! :)");
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
     *   Manejadores de procesos e IO
     *
     */
    
    private void processIOQueue() {
        int n = ioQueue.size();
        for (int i = 0; i < n; i++) {
            Process p = ioQueue.dequeue();                 // saco la cabeza
            boolean done = p.processIOCycle();             // avanzo 1 ciclo de E/S
            if (done) {
                p.setReady();
                readyQueue.enqueue(p);                     // vuelve a READY
                System.out.println("I/O completado para: " + p.name() + ". Poniendo en cola de listos.");
            } else {
                ioQueue.enqueue(p);                        // aún no termina: regresa al final
            }
        }
    }
    

    private void scheduleNextProcess() {
        if (!readyQueue.isEmpty()) {
            currentProcess = readyQueue.dequeue();
            currentProcess.setRunning(); // estado explícito: READY -> RUNNING
        }
    }
    
    
    private void processIOPriorityQueue() {
        int n = ioQueue.size();
        for (int i = 0; i < n; i++) {
            Process p = ioQueue.dequeue();          // saco cabeza
            boolean done = p.processIOCycle();      // avanzo 1 ciclo de E/S
            if (done) {
                p.setReady();
                readyPriorityQueue.add(p);          // vuelve a READY (con prioridad)
                System.out.println("I/O completado para: " + p.name() + ". Poniendo en cola de listos.");
            } else {
                ioQueue.enqueue(p);                 // aún no termina: regresa al final
            }
        }
    }
    
    private void scheduleNextPriorityProcess() {
        if (!readyPriorityQueue.isEmpty()) {
            try {
                currentProcess = readyPriorityQueue.poll();
            } catch (NoSuchElementException e) {
                System.out.println("Error polling the queue: " + e.toString());
            }
        }
    }
}
