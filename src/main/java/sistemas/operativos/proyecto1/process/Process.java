package sistemas.operativos.proyecto1.process;

/**
 *
 * @author Sebastián
 */
public class Process {
    private final String id;
    private final String name;
    private final int arrivalTime;
    private final int instructions;
    private int remainingInstructions;
    private ProcessState currentState;
    private final ProcessType type;
    private final int cyclesForException;
    private final int cyclesToSatisfy;
    private Integer startTime = null;
    private Integer finishTime = null;
    private Integer cyclesInIO;

    public Process(String id, String name, int arrivalTime, int instructions, ProcessType type, int cyclesForException, int cyclesToSatisfy) {
        this.id = id;
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.instructions = instructions;
        this.remainingInstructions = instructions;
        this.currentState = ProcessState.READY;
        this.type = type;
        this.cyclesForException = cyclesForException;
        this.cyclesToSatisfy = cyclesToSatisfy;
        this.cyclesInIO = 0;
    }

    public String id() { return id; }
    public String name() { return name; }
    public int arrival() { return arrivalTime; }
    public int instructions() { return instructions; }
    public int remaining() { return remainingInstructions; }

    public boolean executeInstruction() {
        if (remainingInstructions > 0 && currentState == ProcessState.RUNNING) {
            remainingInstructions--;
            
            // Verificar si genera excepción I/O (solo para procesos I/O bound)
            if (type == ProcessType.IO_BOUND && 
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
    
    public ProcessState currentState() { return currentState; }
    public boolean isFinished() {
        return currentState == ProcessState.FINISHED;
    }
    
    public boolean isBlockedIO() {
        return currentState == ProcessState.BLOCKED;
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
    public String toString() {
        return String.format("P%s(nam=%s,arr=%d,ins=%d,rem=%d)", id, name, arrivalTime, instructions, remainingInstructions);
    }
}
