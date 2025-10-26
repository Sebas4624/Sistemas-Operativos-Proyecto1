package sistemas.operativos.proyecto1;

import sistemas.operativos.proyecto1.lib.Queue;
import sistemas.operativos.proyecto1.lib.LinkedList;
import sistemas.operativos.proyecto1.lib.PriorityQueue;
import sistemas.operativos.proyecto1.process.Process;
import sistemas.operativos.proyecto1.process.ProcessType;
import sistemas.operativos.proyecto1.sched.SRTF;
import java.util.concurrent.Semaphore;


/**
 * Clase CPU del simulador.
 * @author Sebastián
 */
    
public class CPU {
    private final Queue<Process> readyQueue;
    private final PriorityQueue<Process> readyPriorityQueue;
    private final Queue<Process> ioQueue;
    private final Queue<Process> finishedQueue;
    private Process currentProcess;
    private final Config config;
    private final Stats stats;
    private long simulationTime;
    private long busyCycles = 0;    
    private sistemas.operativos.proyecto1.sched.Scheduler scheduler;
    private final Semaphore readyMutex = new Semaphore(1, true);
    private final Semaphore ioMutex    = new Semaphore(1, true);
    private final Semaphore cpuMutex   = new Semaphore(1, true);
    
    private final java.util.List<sistemas.operativos.proyecto1.process.Process> allProcesses = new java.util.ArrayList<>();
    
    public java.util.List<sistemas.operativos.proyecto1.process.Process> getAllProcesses() {
        return allProcesses;
    }
    
    // Si true, la E/S la hará un hilo externo (no se llama processIOQueue() desde CPU):
    private volatile boolean externalIOThread = false;
    public void enableExternalIOThread(boolean v) { this.externalIOThread = v; }
    
    public long getBusyCycles() { return busyCycles; }

    public void setScheduler(sistemas.operativos.proyecto1.sched.Scheduler s) {
        this.scheduler = s;
    }
    
        //log
    private final java.util.List<String> eventLog = new java.util.ArrayList<>();

    private void log(String fmt, Object... args) {
        String line = String.format("[%06d] ", simulationTime) + String.format(fmt, args);
        eventLog.add(line);
    }

    public java.util.List<String> getEventLog() {
        return java.util.Collections.unmodifiableList(eventLog);
    }

    /**
     * Constructor.
     * @param config Configuración del simulador. 
     * @param stats 
     */
    public CPU(Config config, Stats stats) {
        this.readyQueue = new Queue();
        this.readyPriorityQueue = new PriorityQueue();
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
        
        //log de creacion
        log("NEW %s (pri=%d, ins=%d, type=%s)", name, priority, instructions, type);

        allProcesses.add(process);
        
        process.onEnqueuedReady((int) simulationTime);
        readyMutex.acquireUninterruptibly();

        try {
            if (scheduler != null) scheduler.onProcessArrived(process);
            else readyQueue.enqueue(process);
        } finally {
            readyMutex.release();
        }
        System.out.println("Proceso creado: " + name);  
    }
    
    /**
     * Crea un proceso y lo pone en la cola de prioridad de listos.
     * @param name Nombre del proceso.
     * @param arrivalTime Tiempo de llegada del proceso.
     * @param instructions Cantidad de instrucciones del proceso.
     * @param type Tipo de proceso [CPU_BOUND - IO_BOUND].
     * @param cyclesForException Ciclos necesarios para generar una excepción.
     * @param cyclesToSatisfy Ciclos necesarios para satisfacer dicha excepción.
     * @param priority Nivel de prioridad del proceso.
     */
    public void createPriorityProcess(String name, int arrivalTime, int instructions, ProcessType type, int cyclesForException, int cyclesToSatisfy, int priority) {
        String id = java.time.LocalTime.now().toString();
        
        Process process = new Process(id, name, arrivalTime, instructions, type, cyclesForException, cyclesToSatisfy, priority);
        
        //log de creacion
        log("NEW %s (pri=%d, ins=%d, type=%s)", name, priority, instructions, type);

        allProcesses.add(process); 
        
        process.onEnqueuedReady((int) simulationTime);
        readyMutex.acquireUninterruptibly();
        
        try {
            if (scheduler != null) scheduler.onProcessArrived(process);
            else readyPriorityQueue.add(process); // fallback legacy
        } finally {
            readyMutex.release();
        }
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
            log("RUN %s (remain=%d, t=%d)", currentProcess.name(), currentProcess.remaining(), simulationTime);

            if (!currentProcess.isRunning()) stats.addLog("Proceso \"" + currentProcess.name() + "\" se ha puesto en ejecución.");
            currentProcess.setRunning();
            boolean executed = currentProcess.executeInstruction();
            if (executed) busyCycles++; 

            if (currentProcess.isBlockedIO()) {
                log("BLOCK %s -> IO (t=%d)", currentProcess.name(), simulationTime);

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
                log("FIN %s (t=%d)", currentProcess.name(), simulationTime);
                
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
        if (scheduler != null) scheduler.onTick(simulationTime); 
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

        // 1) Avanza E/S
        if (!externalIOThread) {
            processIOQueue();
        }

        // 2) Selecciona si no hay proceso ejecutando (o si terminó / se bloqueó)
        if (currentProcess == null || currentProcess.isFinished() || currentProcess.isBlockedIO()) {
            scheduleNextProcess();            // pone RUNNING internamente
        }

        // 3) Ejecuta un ciclo del proceso actual (si lo hay)
        if (currentProcess != null && (currentProcess.isReady() || currentProcess.isRunning())) {
            log("RUN %s (remain=%d, qrem=%d, t=%d)",
                currentProcess.name(), currentProcess.remaining(),
                config.getRemainingQuantum(), simulationTime);
            
            currentProcess.setRunning();
            boolean executed = currentProcess.executeInstruction();
            if (executed) busyCycles++;    
            config.reduceRemainingQuantum();

            // PRIORIDAD 1: terminó
            if (currentProcess.isFinished()) {
                log("FIN %s (t=%d)", currentProcess.name(), simulationTime);

                currentProcess.setFinishTime((int) simulationTime); //
                
                finishedQueue.enqueue(currentProcess);
                this.stats.setFinishedQueue(finishedQueue.toLinkedList());
                
                System.out.println("¡Proceso " + currentProcess.name() + " terminado! :)");
                currentProcess = null;  // el próximo ciclo selecciona otro y resetea quantum
            }
            //PRIORIDAD 2: se bloqueó por E/S 
            else if (currentProcess.isBlockedIO()) {
                log("BLOCK %s -> IO (t=%d)", currentProcess.name(), simulationTime);

                currentProcess.setBlocked();
                ioMutex.acquireUninterruptibly();
                try{
                    ioQueue.enqueue(currentProcess);
                } finally {
                    ioMutex.release();
                }
                System.out.println("Proceso " + currentProcess.name() + " bloqueado.");
                currentProcess = null;  // el próximo ciclo selecciona otro y resetea quantum
            }
            //  PRIORIDAD 3: venció el quantum (RR) 
            else if (config.getRemainingQuantum() == 0) {
                log("PREEMPT %s (quantum=0) -> READY (t=%d)", currentProcess.name(), simulationTime);

                // métrica: va a READY
                currentProcess.onEnqueuedReady((int) simulationTime);

                if (scheduler != null) {
                    scheduler.onProcessPreempted(currentProcess);
                } else {
                    currentProcess.setReady();
                    readyMutex.acquireUninterruptibly();
                    try {
                        readyQueue.enqueue(currentProcess);
                    } finally {
                        readyMutex.release();
                    }
                System.out.println("Quantum de " + currentProcess.name() + " terminado. Reencolado en READY.");
                currentProcess = null; // el próximo selectNext() hará reset del quantum
            }
        }

        // 4) Espera según duración del ciclo
        try {
            Thread.sleep(config.getCycleDuration());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        if (scheduler != null) scheduler.onTick(simulationTime);
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
        if (currentProcess == null || currentProcess.isFinished() || 
            currentProcess.isBlockedIO()) {
            scheduleNextProcess();
        }
        
        // 3. Ejecutar proceso actual
        if (currentProcess != null && (currentProcess.isReady() || currentProcess.isRunning())) {
            log("RUN %s (remain=%d, pri=%d, t=%d)",
                currentProcess.name(), currentProcess.remaining(), currentProcess.priority(), simulationTime);

            currentProcess.setRunning();
            boolean executed = currentProcess.executeInstruction();
            if (executed) busyCycles++;
            //System.out.println("Instrucción ejecutada");  ///////////////////////////
            
            if (currentProcess.isBlockedIO()) {
                log("BLOCK %s -> IO (t=%d)", currentProcess.name(), simulationTime);

                currentProcess.setBlocked();    // marca estado
                ioMutex.acquireUninterruptibly();
                try{ 
                    ioQueue.enqueue(currentProcess);
                } finally {
                    ioMutex.release();
                }
                System.out.println("Proceso " + currentProcess.name() + " bloqueado.");
                currentProcess = null;
                
            } else if (currentProcess.isFinished()) {
                log("FIN %s (t=%d)", currentProcess.name(), simulationTime);
                currentProcess.setFinishTime((int) simulationTime);
                
                finishedQueue.enqueue(currentProcess);
                this.stats.setFinishedQueue(finishedQueue.toLinkedList());
                
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
        if (scheduler != null) scheduler.onTick(simulationTime);
    }
    
        /** SRTF: Shortest Remaining Time First (EXPROPIATIVO). */
    public void simulateCycleSRTF() {
        simulationTime++;

        // 1) Avanza la E/S (esto reinyecta a READY vía scheduler.onProcessUnblocked(...))
        if (!externalIOThread) {
            processIOQueue();
        }

        // 1.5) Hook de EXPROPIACIÓN SRTF:
        // si hay un READY con menor remaining() que el RUNNING, desalojar.
        if (scheduler != null && currentProcess != null && scheduler instanceof SRTF) {
            SRTF srtf = (SRTF) scheduler;
            Process best = srtf.peekBest();                  // mira el mejor sin sacarlo
            if (best != null && best.remaining() < currentProcess.remaining()) {
                log("PREEMPT %s (better=%s, %d<%d, t=%d)",
                    currentProcess.name(), best.name(), best.remaining(), currentProcess.remaining(), simulationTime);
                currentProcess.onEnqueuedReady((int) simulationTime);
                scheduler.onProcessPreempted(currentProcess); // reencola el actual a READY
                currentProcess = null;                        // forzar nueva selección
            }
        }

        // 2) Seleccionar si no hay actual / terminó / se bloqueó / (o lo acabamos de desalojar)
        if (currentProcess == null || currentProcess.isFinished() || currentProcess.isBlockedIO()) {
            scheduleNextProcess();   // este ya hace setRunning() al elegido
        }

        // 3) Ejecutar 1 instrucción
        if (currentProcess != null && (currentProcess.isReady() || currentProcess.isRunning())) {
            log("RUN %s (remain=%d, t=%d)", currentProcess.name(), currentProcess.remaining(), simulationTime);

            currentProcess.setRunning();
            boolean executed = currentProcess.executeInstruction();
            if (executed) busyCycles++;

            if (currentProcess.isBlockedIO()) {
                log("BLOCK %s -> IO (t=%d)", currentProcess.name(), simulationTime); // ← corregido
                currentProcess.setBlocked();           // estado coherente
                ioMutex.acquireUninterruptibly();
                try {
                    ioQueue.enqueue(currentProcess);       // pasa a cola de E/S
                } finally {
                    ioMutex.release();
                }
                System.out.println("Proceso " + currentProcess.name() + " bloqueado.");
                currentProcess = null;
                
            } else if (currentProcess.isFinished()) {
                log("FIN %s (t=%d)", currentProcess.name(), simulationTime);
                currentProcess.setFinishTime((int) simulationTime);
                
                finishedQueue.enqueue(currentProcess);
                this.stats.setFinishedQueue(finishedQueue.toLinkedList());
                
                System.out.println("¡Proceso " + currentProcess.name() + " terminado! :)");
                currentProcess = null;
            }
        }

        // 4) Espera + tick
        try { Thread.sleep(config.getCycleDuration()); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        if (scheduler != null) scheduler.onTick(simulationTime);
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
                
                if (scheduler != null) {
                    scheduler.onProcessUnblocked(p);
                }
                else {
                    readyQueue.enqueue(p);
                }                   // vuelve a READY
                
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
             
                    if (scheduler != null) scheduler.onProcessUnblocked(p);
                    else readyQueue.enqueue(p);
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

    private void scheduleNextProcess() {
        cpuMutex.acquireUninterruptibly();
        readyMutex.acquireUninterruptibly();
        try {
            if (scheduler != null) {
                currentProcess = scheduler.selectNext();
            } else if (!readyQueue.isEmpty()) {
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
    
    public boolean isActive() {
        return readyQueue.isEmpty() && ioQueue.isEmpty() && readyPriorityQueue.isEmpty() && currentProcess == null;
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
