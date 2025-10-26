package sistemas.operativos.proyecto1;

import sistemas.operativos.proyecto1.lib.Queue;
import sistemas.operativos.proyecto1.lib.LinkedList;
import sistemas.operativos.proyecto1.process.Process;
import sistemas.operativos.proyecto1.process.ProcessType;
import java.util.concurrent.Semaphore;


/**
 * Clase CPU del simulador.
 * @author Sebastián
 */
    
public class CPU {
    private final Queue<Process> readyQueue;
    private final Queue<Process> ioQueue;
    private final Queue<Process> finishedQueue;
    private Process currentProcess;
    private final Config config;
    private final Stats stats;
    private long simulationTime;
    private long busyCycles = 0;
    private final Semaphore readyMutex = new Semaphore(1, true);
    private final Semaphore ioMutex    = new Semaphore(1, true);
    private final Semaphore cpuMutex   = new Semaphore(1, true);
    
    private final LinkedList<Process> allProcesses = new LinkedList<>();
    
    public LinkedList<Process> getAllProcesses() {
        return allProcesses;
    }
    
    // Si true, la E/S la hará un hilo externo (no se llama processIOQueue() desde CPU):
    private volatile boolean externalIOThread = false;
    public void enableExternalIOThread(boolean v) { this.externalIOThread = v; }
    
    public long getBusyCycles() { return busyCycles; }
    
        //log
    private final LinkedList<String> eventLog = new LinkedList();

    private void log(String fmt, Object... args) {
        String line = String.format("[%06d] ", simulationTime) + String.format(fmt, args);
        eventLog.add(line);
    }

    public String[] getEventLogArray() {
        int n = eventLog.size();
        String[] arr = new String[n];
        for (int i = 0; i < n; i++) {
            arr[i] = eventLog.get(i);   
        }
        return arr;
    }

    /**
     * Constructor.
     * @param config Configuración del simulador. 
     * @param stats 
     */
    public CPU(Config config, Stats stats) {
        this.readyQueue = new Queue();
        this.ioQueue = new Queue();
        this.finishedQueue = new Queue();
        this.config = config;
        this.stats = stats;
        this.simulationTime = 0;
    }
    
    public void resetCPUState() {
        
    }
    
    /**
     * Crea un proceso y lo pone en la cola de listos.
     * @param name Nombre del proceso.
     * @param arrivalTime Tiempo de llegada del proceso.
     * @param instructions Cantidad de instrucciones del proceso.
     * @param type Tipo de proceso [CPU_BOUND - IO_BOUND].
     * @param cyclesForException Ciclos necesarios para generar una excepción.
     * @param cyclesToSatisfy Ciclos necesarios para satisfacer dicha excepción.
     * @param priority Nivel de prioridad del proceso.
     */
    public void createProcess(String name, int arrivalTime, int instructions, ProcessType type, int cyclesForException, int cyclesToSatisfy, int priority) {
        String id = java.time.LocalTime.now().toString();
        
        Process process = new Process(id, name, arrivalTime, instructions, type, cyclesForException, cyclesToSatisfy, priority);
        
        allProcesses.add(process);
        
        process.onEnqueuedReady((int) simulationTime);
        readyMutex.acquireUninterruptibly();

        try {
            readyQueue.enqueue(process);
        } finally {
            readyMutex.release();
        }
        stats.addLog("Proceso \"" + process.name() + "\" ha hizo creado y se ha puesto en la cola de listos.");
        System.out.println("Proceso creado: " + name);  
    }

    /**
     * Política de planificación FCFS (First-Come First-Served)
     * 
     * Se ejecutan los procesos tal cual como se van añadiendo a la cola de listos.
     */
    public void simulateCycleFCFS() {
        simulationTime++;
        
        // 1. Procesar I/O
        if (!externalIOThread) {
            processIOQueue();
        }
        
        // 2. Planificar siguiente proceso (si no hay uno actual)
        if (currentProcess == null || currentProcess.isFinished() || currentProcess.isBlockedIO()) {
            scheduleNextProcess();
        }
        
        if (currentProcess != null && (currentProcess.isReady() || currentProcess.isRunning())) {
            if (!currentProcess.isRunning()) stats.addLog("Proceso \"" + currentProcess.name() + "\" se ha puesto en ejecución.");
            currentProcess.setRunning();
            boolean executed = currentProcess.executeInstruction();
            if (executed) busyCycles++; 

            if (currentProcess.isBlockedIO()) {
                currentProcess.setBlocked(); 
                ioMutex.acquireUninterruptibly();
                try{
                    ioQueue.enqueue(currentProcess);
                } finally {
                    ioMutex.release();
                }
                
                System.out.println("Proceso " + currentProcess.name() + " bloqueado.");
                stats.addLog("Proceso \"" + currentProcess.name() + "\" se ha bloqueado y puesto en la cola de bloqueados.");
                currentProcess = null;
                
            } else if (currentProcess.isFinished()) {
                config.resetRemainingQuantum();
                currentProcess.setFinishTime((int) simulationTime);   //guarda fin
                
                finishedQueue.enqueue(currentProcess);
                this.stats.setFinishedQueue(finishedQueue.toLinkedList());
                
                System.out.println("¡Proceso " + currentProcess.name() + " terminado! :)");
                stats.addLog("Proceso \"" + currentProcess.name() + "\" se ha terminado y puesto en la cola de terminados.");
                currentProcess = null;
            } 
        
        
            // 4. Esperar según la duración del ciclo configurada
            try {
                Thread.sleep(config.getCycleDuration());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Política de planificación RR (Round Robin)
     * 
     * Se ejecutan los procesos con un valor "Quantum" asignado a cada uno de
     * los procesos de forma equitativa, y por cada ciclo se va disminuyendo en
     * uno. Si el "Quantum" del proceso llega a 0, se expulsa del CPU y se añade
     * al final de la cola de listos.
     */
    public void simulateCycleRR() {
        simulationTime++;
        
        // 1. Procesar I/O
        if (!externalIOThread) {
            processIOQueue();
        }
        
        // 2. Planificar siguiente proceso (si no hay uno actual)
        if (currentProcess == null || currentProcess.isFinished() || currentProcess.isBlockedIO()) {
            scheduleNextProcess();
        }
        
        if (currentProcess != null && (currentProcess.isReady() || currentProcess.isRunning())) {
            if (!currentProcess.isRunning()) stats.addLog("Proceso \"" + currentProcess.name() + "\" se ha puesto en ejecución.");
            currentProcess.setRunning();
            boolean executed = currentProcess.executeInstruction();
            if (executed) busyCycles++; 
            
            if (config.getRemainingQuantum() == 0) {
                config.resetRemainingQuantum();
                readyQueue.enqueue(currentProcess);
                
                stats.addLog("Se ha terminado el quantum para el proceso \"" + currentProcess.name() + "\" y se ha puesto en la cola de listos.");
                currentProcess = null;
            } else {
                config.reduceRemainingQuantum();
            }

            if (currentProcess != null) {
                if (currentProcess.isBlockedIO()) {
                    currentProcess.setBlocked(); 
                    ioMutex.acquireUninterruptibly();
                    try{
                        ioQueue.enqueue(currentProcess);
                    } finally {
                        ioMutex.release();
                    }

                    System.out.println("Proceso " + currentProcess.name() + " bloqueado.");
                    stats.addLog("Proceso \"" + currentProcess.name() + "\" se ha bloqueado y puesto en la cola de bloqueados.");
                    currentProcess = null;

                } else if (currentProcess.isFinished()) {
                    config.resetRemainingQuantum();
                    currentProcess.setFinishTime((int) simulationTime);   //guarda fin

                    finishedQueue.enqueue(currentProcess);
                    this.stats.setFinishedQueue(finishedQueue.toLinkedList());

                    System.out.println("¡Proceso " + currentProcess.name() + " terminado! :)");
                    stats.addLog("Proceso \"" + currentProcess.name() + "\" se ha terminado y puesto en la cola de terminados.");
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
    }
    
    /**
     * Política de planificación SPN (Shortest Process Next)
     * 
     * Se ejecutan los procesos con la menor cantidad de instrucciones primero,
     * ejecutando los más pequeños primero y progresivamente ejecutando los
     * procesos con mayor cantidad de instrucciones.
     */
    public void simulateCycleSPN() {
        simulationTime++;
        
        // 1. Procesar I/O
        if (!externalIOThread) {
            processIOQueue();
        }
        
        // 2. Planificar siguiente proceso (si no hay uno actual)
        if (currentProcess == null || currentProcess.isFinished() || currentProcess.isBlockedIO()) {
            scheduleNextProcessShortestProcess();
        }
        
        if (currentProcess != null && (currentProcess.isReady() || currentProcess.isRunning())) {
            if (!currentProcess.isRunning()) stats.addLog("Proceso \"" + currentProcess.name() + "\" se ha puesto en ejecución.");
            currentProcess.setRunning();
            boolean executed = currentProcess.executeInstruction();
            if (executed) busyCycles++; 

            if (currentProcess.isBlockedIO()) {
                currentProcess.setBlocked(); 
                ioMutex.acquireUninterruptibly();
                try{
                    ioQueue.enqueue(currentProcess);
                } finally {
                    ioMutex.release();
                }
                
                System.out.println("Proceso " + currentProcess.name() + " bloqueado.");
                stats.addLog("Proceso \"" + currentProcess.name() + "\" se ha bloqueado y puesto en la cola de bloqueados.");
                currentProcess = null;
                
            } else if (currentProcess.isFinished()) {
                config.resetRemainingQuantum();
                currentProcess.setFinishTime((int) simulationTime);   //guarda fin
                
                finishedQueue.enqueue(currentProcess);
                this.stats.setFinishedQueue(finishedQueue.toLinkedList());
                
                System.out.println("¡Proceso " + currentProcess.name() + " terminado! :)");
                stats.addLog("Proceso \"" + currentProcess.name() + "\" se ha terminado y puesto en la cola de terminados.");
                currentProcess = null;
            } 
        

            // 4. Esperar según la duración del ciclo configurada
            try {
                Thread.sleep(config.getCycleDuration());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Política de planificación PRI (Por prioridades; mayor número, mayor prioridad)
     * 
     * Se ejecutan los procesos que se van sacando de la cola de prioridad de
     * listos. Conforme se van sacando procesos de la cola, van saliendo los
     * que mayor nivel de prioridad tienen, y van quedando los que menor
     * prioridad tienen.
     */
    public void simulateCyclePRI() {
        simulationTime++;
        
        // 1. Procesar I/O
        if (!externalIOThread) {
            processIOQueue();
        }
        
        // 2. Planificar siguiente proceso (si no hay uno actual)
        if (currentProcess == null || currentProcess.isFinished() || currentProcess.isBlockedIO()) {
            scheduleNextProcessPriority();
        }
        
        if (currentProcess != null && (currentProcess.isReady() || currentProcess.isRunning())) {
            if (!currentProcess.isRunning()) stats.addLog("Proceso \"" + currentProcess.name() + "\" se ha puesto en ejecución.");
            currentProcess.setRunning();
            boolean executed = currentProcess.executeInstruction();
            if (executed) busyCycles++; 

            if (currentProcess.isBlockedIO()) {
                currentProcess.setBlocked(); 
                ioMutex.acquireUninterruptibly();
                try{
                    ioQueue.enqueue(currentProcess);
                } finally {
                    ioMutex.release();
                }
                
                System.out.println("Proceso " + currentProcess.name() + " bloqueado.");
                stats.addLog("Proceso \"" + currentProcess.name() + "\" se ha bloqueado y puesto en la cola de bloqueados.");
                currentProcess = null;
                
            } else if (currentProcess.isFinished()) {
                config.resetRemainingQuantum();
                currentProcess.setFinishTime((int) simulationTime);   //guarda fin
                
                finishedQueue.enqueue(currentProcess);
                this.stats.setFinishedQueue(finishedQueue.toLinkedList());
                
                System.out.println("¡Proceso " + currentProcess.name() + " terminado! :)");
                stats.addLog("Proceso \"" + currentProcess.name() + "\" se ha terminado y puesto en la cola de terminados.");
                currentProcess = null;
            } 


            // 4. Esperar según la duración del ciclo configurada
            try {
                Thread.sleep(config.getCycleDuration());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Política de planificación MFQ (Multilevel Feedback Queue)
     * 
     * Se ejecutan los procesos con mayor nivel de prioridad y mediante un
     * valor "quantum" asignado a cada proceso, el cual va disminuyendo por
     * cada ciclo. Si el "Quantum" del proceso llega a 0, se expulsa del CPU,
     * se añade al final de la cola de listos y se le disminuye el nivel de
     * prioridad en 1 a menos de que su nivel sea igual a 1.
     */
    public void simulateCycleMFQ() {
        simulationTime++;
        
        // 1. Procesar I/O
        if (!externalIOThread) {
            processIOQueue();
        }
        
        // 2. Planificar siguiente proceso (si no hay uno actual)
        if (currentProcess == null || currentProcess.isFinished() || currentProcess.isBlockedIO()) {
            scheduleNextProcessPriority();
        }
        
        if (currentProcess != null && (currentProcess.isReady() || currentProcess.isRunning())) {
            if (!currentProcess.isRunning()) stats.addLog("Proceso \"" + currentProcess.name() + "\" se ha puesto en ejecución.");
            currentProcess.setRunning();
            boolean executed = currentProcess.executeInstruction();
            if (executed) busyCycles++; 
            
            if (config.getRemainingQuantum() == 0) {
                config.resetRemainingQuantum();
                currentProcess.reducePriority();
                readyQueue.enqueue(currentProcess);
                
                stats.addLog("Se ha reducido el nivel de prioridad del proceso \"" + currentProcess.name() + "\" a " + currentProcess.priority() + ".");
                stats.addLog("Se ha terminado el quantum para el proceso \"" + currentProcess.name() + "\" y se ha puesto en la cola de listos.");
                currentProcess = null;
            } else {
                config.reduceRemainingQuantum();
            }

            if (currentProcess != null) {
                if (currentProcess.isBlockedIO()) {
                    currentProcess.setBlocked(); 
                    ioMutex.acquireUninterruptibly();
                    try{
                        ioQueue.enqueue(currentProcess);
                    } finally {
                        ioMutex.release();
                    }

                    System.out.println("Proceso " + currentProcess.name() + " bloqueado.");
                    stats.addLog("Proceso \"" + currentProcess.name() + "\" se ha bloqueado y puesto en la cola de bloqueados.");
                    currentProcess = null;

                } else if (currentProcess.isFinished()) {
                    config.resetRemainingQuantum();
                    currentProcess.setFinishTime((int) simulationTime);   //guarda fin

                    finishedQueue.enqueue(currentProcess);
                    this.stats.setFinishedQueue(finishedQueue.toLinkedList());

                    System.out.println("¡Proceso " + currentProcess.name() + " terminado! :)");
                    stats.addLog("Proceso \"" + currentProcess.name() + "\" se ha terminado y puesto en la cola de terminados.");
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
    }
    
    /**
     *
     *   Manejadores de procesos e IO
     *
     */
    
    /**
     * Procesar la cola de IO, si no está vacía. Si se completa una petición de
     * IO, se desbloquea el proceso y se añade de vuelta a la cola de listos.
     */
    private void processIOQueue() {
        int n = ioQueue.size();
        for (int i = 0; i < n; i++) {
            Process p = ioQueue.dequeue();                 // saco la cabeza
            boolean done = p.processIOCycle();             // avanzo 1 ciclo de E/S
            if (done) {
                p.setReady();
                p.onEnqueuedReady((int) simulationTime);
                
                readyQueue.enqueue(p);               // vuelve a READY
                
                stats.addLog("I/O compledato para \"" + p.name() + "\".");
                stats.addLog("Proceso \"" + p.name() + "\" se ha puesto en cola de listos.");
                System.out.println("I/O completado para: " + p.name() + ". Poniendo en cola de listos.");
            } else {
                ioQueue.enqueue(p);                        // aún no termina: regresa al final
            }
        }
    }
    
    public void processIOCycleOneTick() {
        ioMutex.acquireUninterruptibly();
        readyMutex.acquireUninterruptibly();
        try {
            int n = ioQueue.size();
            for (int i = 0; i < n; i++) {
                Process p = ioQueue.dequeue();
                boolean done = p.processIOCycle();
                if (done) {
                    p.setReady();
                    p.onEnqueuedReady((int) simulationTime);
                    
                    log("IO-DONE %s -> READY (t=%d)", p.name(), simulationTime);
             
                    readyQueue.enqueue(p);
                    System.out.println("I/O completado para: " + p.name() + ". Poniendo en cola de listos.");
                } else {
                    ioQueue.enqueue(p);
                }
            }
        } finally {
            readyMutex.release();
            ioMutex.release();
        }
    }
    
    // Planificar procesos

    private void scheduleNextProcess() {
        cpuMutex.acquireUninterruptibly();
        readyMutex.acquireUninterruptibly();
        try {
            if (!readyQueue.isEmpty()) {
                currentProcess = readyQueue.dequeue();
                stats.addLog("Proceso \"" + currentProcess.name() + "\" se ha puesto en cola de listos.");
            } else {
                currentProcess = null;
            }

            if (currentProcess != null) {
                log("DISPATCH %s", currentProcess.name());
                // Cambia a RUNNING
                currentProcess.setRunning();
                // Métricas: tiempo de espera y de primera respuesta
                currentProcess.onDispatchedToCpu((int) simulationTime);
                if (currentProcess.startTime() == null) {
                    currentProcess.setStartTime((int) simulationTime);
                }
            }
        } finally {
            // Siempre liberar en el orden inverso
            readyMutex.release();
            cpuMutex.release();
        }
    }
    
    private void scheduleNextProcessPriority() {
        cpuMutex.acquireUninterruptibly();
        readyMutex.acquireUninterruptibly();
        try {
            if (!readyQueue.isEmpty()) {
                currentProcess = readyQueue.pollPriority();
                stats.addLog("Proceso \"" + currentProcess.name() + "\" se ha puesto en cola de listos.");
            } else {
                currentProcess = null;
            }

            if (currentProcess != null) {
                log("DISPATCH %s", currentProcess.name());
                // Cambia a RUNNING
                currentProcess.setRunning();
                // Métricas: tiempo de espera y de primera respuesta
                currentProcess.onDispatchedToCpu((int) simulationTime);
                if (currentProcess.startTime() == null) {
                    currentProcess.setStartTime((int) simulationTime);
                }
            }
        } finally {
            // Siempre liberar en el orden inverso
            readyMutex.release();
            cpuMutex.release();
        }
    }
    
    private void scheduleNextProcessShortestProcess() {
        cpuMutex.acquireUninterruptibly();
        readyMutex.acquireUninterruptibly();
        try {
            if (!readyQueue.isEmpty()) {
                currentProcess = readyQueue.pollShortestProcess();
                stats.addLog("Proceso \"" + currentProcess.name() + "\" se ha puesto en cola de listos.");
            } else {
                currentProcess = null;
            }

            if (currentProcess != null) {
                log("DISPATCH %s", currentProcess.name());
                // Cambia a RUNNING
                currentProcess.setRunning();
                // Métricas: tiempo de espera y de primera respuesta
                currentProcess.onDispatchedToCpu((int) simulationTime);
                if (currentProcess.startTime() == null) {
                    currentProcess.setStartTime((int) simulationTime);
                }
            }
        } finally {
            // Siempre liberar en el orden inverso
            readyMutex.release();
            cpuMutex.release();
        }
    }
    
    // Misceláneos
    
    public boolean isActive() {
        return readyQueue.isEmpty() && ioQueue.isEmpty() && currentProcess == null;
    }

    public Process getCurrentProcess() {
        return this.currentProcess;
    }

    public LinkedList<Process> getReadyQueue() {
        return this.readyQueue.toLinkedList();
    }

    public LinkedList<Process> getIoQueue() {
        return this.ioQueue.toLinkedList();
    }
}
