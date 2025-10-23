package sistemas.operativos.proyecto1;

import java.util.Iterator;
import java.util.NoSuchElementException;
import sistemas.operativos.proyecto1.lib.Queue;
import sistemas.operativos.proyecto1.lib.PriorityQueue;
import sistemas.operativos.proyecto1.process.Process;
import sistemas.operativos.proyecto1.process.ProcessType;
import sistemas.operativos.proyecto1.sched.Scheduler;
import sistemas.operativos.proyecto1.sched.FCFS;
import sistemas.operativos.proyecto1.sched.SRTF;

/**
 * Clase CPU del simulador.
 * @author Sebastián
 */
    
public class CPU {
    private final Queue<Process> readyQueue;
    private final PriorityQueue<Process> readyPriorityQueue;
    private final Queue<Process> ioQueue;
    private Process currentProcess;
    private final Config config;
    private long simulationTime;
    
    private sistemas.operativos.proyecto1.sched.Scheduler scheduler;

    public void setScheduler(sistemas.operativos.proyecto1.sched.Scheduler s) {
        this.scheduler = s;
    }
    /**
     * Constructor.
     * @param config Configuración del simulador. 
     */
    public CPU(Config config) {
        this.readyQueue = new Queue();
        this.readyPriorityQueue = new PriorityQueue();
        this.ioQueue = new Queue();
        this.config = config;
        this.simulationTime = 0;
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
        if (scheduler != null) scheduler.onProcessArrived(process);
           else readyQueue.enqueue(process);
        
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
        if (scheduler != null) {
            scheduler.onProcessArrived(process);
        } else {
            readyPriorityQueue.add(process);
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
        if (scheduler != null) scheduler.onTick(simulationTime); 
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
        processIOQueue();

        // 2) Selecciona si no hay proceso ejecutando (o si terminó / se bloqueó)
        if (currentProcess == null || currentProcess.isFinished() || currentProcess.isBlockedIO()) {
            scheduleNextProcess();            // pone RUNNING internamente
        }

        // 3) Ejecuta un ciclo del proceso actual (si lo hay)
        if (currentProcess != null && (currentProcess.isReady() || currentProcess.isRunning())) {
            currentProcess.setRunning();
            currentProcess.executeInstruction();   // 1 instrucción por ciclo
            config.reduceRemainingQuantum();

            // *** PRIORIDAD 1: terminó ***
            if (currentProcess.isFinished()) {
                System.out.println("¡Proceso " + currentProcess.name() + " terminado! :)");
                currentProcess = null;  // el próximo ciclo selecciona otro y resetea quantum
            }
            // *** PRIORIDAD 2: se bloqueó por E/S ***
            else if (currentProcess.isBlockedIO()) {
                currentProcess.setBlocked();
                ioQueue.enqueue(currentProcess);
                System.out.println("Proceso " + currentProcess.name() + " bloqueado.");
                currentProcess = null;  // el próximo ciclo selecciona otro y resetea quantum
            }
            // *** PRIORIDAD 3: venció el quantum (RR) ***
            else if (config.getRemainingQuantum() == 0) {
                if (scheduler != null) {
                    scheduler.onProcessPreempted(currentProcess);  // ★ reencola vía scheduler
                } else {
                    currentProcess.setReady();
                    readyQueue.enqueue(currentProcess);            // fallback sin scheduler
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
        processIOQueue(); 
        
        // 2. Planificar siguiente proceso (si no hay uno actual)
        if (currentProcess == null || currentProcess.isFinished() || 
            currentProcess.isBlockedIO()) {
            scheduleNextProcess();
        }
        
        // 3. Ejecutar proceso actual
        if (currentProcess != null && (currentProcess.isReady() || currentProcess.isRunning())) {
            currentProcess.setRunning();
            currentProcess.executeInstruction();
            //System.out.println("Instrucción ejecutada");  ///////////////////////////
            
            if (currentProcess.isBlockedIO()) {
                currentProcess.setBlocked();    // marca estado
                ioQueue.enqueue(currentProcess);
                System.out.println("Proceso " + currentProcess.name() + " bloqueado.");
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
        if (scheduler != null) scheduler.onTick(simulationTime);
    }
    
        /** SRTF: Shortest Remaining Time First (EXPROPIATIVO). */
    public void simulateCycleSRTF() {
        simulationTime++;

        // 1) Avanza la E/S (esto reinyecta a READY vía scheduler.onProcessUnblocked(...))
        processIOQueue();

        // 1.5) Hook de EXPROPIACIÓN SRTF:
        // si hay un READY con menor remaining() que el RUNNING, desalojar.
        if (scheduler != null && currentProcess != null && scheduler instanceof SRTF) {
            SRTF srtf = (SRTF) scheduler;
            Process best = srtf.peekBest();                  // mira el mejor sin sacarlo
            if (best != null && best.remaining() < currentProcess.remaining()) {
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
            currentProcess.setRunning();
            currentProcess.executeInstruction();

            if (currentProcess.isBlockedIO()) {
                currentProcess.setBlocked();           // estado coherente
                ioQueue.enqueue(currentProcess);       // pasa a cola de E/S
                System.out.println("Proceso " + currentProcess.name() + " bloqueado.");
                currentProcess = null;
            } else if (currentProcess.isFinished()) {
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
                if (scheduler != null) scheduler.onProcessUnblocked(p);
                else readyQueue.enqueue(p);                   // vuelve a READY
                System.out.println("I/O completado para: " + p.name() + ". Poniendo en cola de listos.");
            } else {
                ioQueue.enqueue(p);                        // aún no termina: regresa al final
            }
        }
    }
    

    private void scheduleNextProcess() {
        if (scheduler != null) {
            currentProcess = scheduler.selectNext();
        } else if (!readyQueue.isEmpty()) {
            currentProcess = readyQueue.dequeue(); 
        } else {
            currentProcess = null;
        }

        if (currentProcess != null) {
            currentProcess.setRunning();             
         
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
    /**
     * Planificar y poner en cola de prioridad el siguiente proceso que venga
     * en cola, si hay alguno disponible.
     */
    private void scheduleNextPriorityProcess() {
        if (!readyPriorityQueue.isEmpty()) {
            try {
                currentProcess = readyPriorityQueue.poll();
                currentProcess.setRunning();
            } catch (NoSuchElementException e) {
                System.out.println("Error polling the queue: " + e);
            }
        }
    }
}
