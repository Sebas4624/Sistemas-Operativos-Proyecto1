package sistemas.operativos.proyecto1.process;

/**
 * Clase proceso del simulador.
 * @author Sebastián
 */
public class Process implements Comparable<Process> {
    private final String id;
    private final String name;
    private final int arrivalTime;
    private final int instructions;
    private int remainingInstructions;
    private ProcessState currentState = ProcessState.READY;
    private final ProcessType type;
    private final int cyclesForException;
    private final int cyclesToSatisfy;
    private final int priority;
    private Integer startTime = null;
    private Integer finishTime = null;
    private Integer cyclesInIO;

    private int pc = 0;
    private int mar = 0;
    
    private Integer firstRunCycle = null; // primer ciclo en RUNNING
    private Integer lastReadyEnqueue = null;
    private long totalWaitCycles = 0;
    
    public int pc()  { return pc; }
    public int mar() { return mar; }   
    
    public void onEnqueuedReady(int cycle) { 
    lastReadyEnqueue = cycle; 
    }

    public void onDispatchedToCpu(int cycle) {
        if (lastReadyEnqueue != null) totalWaitCycles += (cycle - lastReadyEnqueue);
        if (firstRunCycle == null) firstRunCycle = cycle; // para response time
    }

    public long totalWait()        { return totalWaitCycles; }
    public Integer firstRun()      { return firstRunCycle; }

    /**
     * Constructor.
     * @param id Identificador del proceso.
     * @param name Nombre del proceso.
     * @param arrivalTime Tiempo de llegada del proceso.
     * @param instructions Cantidad de instrucciones del proceso.
     * @param type Tipo de proceso [CPU_BOUND - IO_BOUND].
     * @param cyclesForException Ciclos necesarios para generar una excepción.
     * @param cyclesToSatisfy Ciclos necesarios para satisfacer dicha excepción.
     * @param priority Nivel de prioridad del proceso.
     */
    public Process(String id, String name, int arrivalTime, int instructions, ProcessType type, int cyclesForException, int cyclesToSatisfy, int priority) {
        this.id = id;
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.instructions = instructions;
        this.remainingInstructions = instructions;
        this.type = type;
        this.cyclesForException = cyclesForException;
        this.cyclesToSatisfy = cyclesToSatisfy;
        this.priority = priority;
        this.cyclesInIO = 0;
    }
    
    public String id() { return id; }
    public String name() { return name; }
    public int arrival() { return arrivalTime; }
    public int instructions() { return instructions; }
    public int remaining() { return remainingInstructions; }
    public int priority() { return priority; }

    /**
     * Ejecuta una instrucción del proceso. Se disminuye la cantidad de
     * instrucciones restantes en uno.
     * @return Booleano determinando si se pudo ejecutar la instrucción.
     */
    public boolean executeInstruction() {
        if (remainingInstructions > 0 && currentState == ProcessState.RUNNING) {
            pc++;
            mar++;
            remainingInstructions--;
            
            // Verificar si genera excepción I/O (solo para procesos I/O bound)
            if (type == ProcessType.IO_BOUND && cyclesForException > 0 &&
                (instructions - remainingInstructions) % cyclesForException == 0) {
                currentState = ProcessState.BLOCKED;
                cyclesInIO = 0;
                return true; // Se ejecutó pero ahora está bloqueado
            }
            
            // Verificar si terminó
            if (remainingInstructions <= 0) {
                currentState = ProcessState.FINISHED;
            }
            
            return true;
        }
        return false;
    }
    
    /**
     * Contabiliza los ciclos para las excepciones y las satisfacciones.
     * @return Booleano determinando si se logró satisfacer la excepción.
     */
    public boolean processIOCycle() {
        if (currentState == ProcessState.BLOCKED) {
            mar++;
            cyclesInIO++;
            if (cyclesToSatisfy > 0 && cyclesInIO >= cyclesToSatisfy) {
                currentState = ProcessState.READY;
                return true;
            }
        }
        return false;
    }
    
    public ProcessState currentState() { return currentState; }
    public boolean isFinished() {
        return currentState == ProcessState.FINISHED || remainingInstructions <= 0;
    }
    
    public boolean isBlockedIO() {
        return currentState == ProcessState.BLOCKED;
    }
    
    public boolean isRunning() {
        return currentState == ProcessState.RUNNING;
    }
    
    public boolean isReady() {
        return currentState == ProcessState.READY;
    }
    
    public void setFinished() {
        this.currentState = ProcessState.FINISHED;
    }
    
    public void setBlocked() {
        this.currentState = ProcessState.BLOCKED;
    }
    
    public void setRunning() {
        this.currentState = ProcessState.RUNNING;
    }
    
    public void setReady() {
        this.currentState = ProcessState.READY;
    }

    public Integer startTime() { return startTime; }
    public void setStartTime(int t) { if (startTime == null) startTime = t; }

    public Integer finishTime() { return finishTime; }
    public void setFinishTime(int t) { finishTime = t; }

    public boolean finished() { return remainingInstructions == 0; }

    @Override
    public int compareTo(Process o) {
        //return Integer.compare(this.priority, o.priority());
        return Integer.compare(this.priority, o.priority());
    }

    @Override
    public String toString() {
        return String.format("P%s(nam=%s,arr=%d,ins=%d,rem=%d)", id, name, arrivalTime, instructions, remainingInstructions);
    }
}
