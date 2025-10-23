package sistemas.operativos.proyecto1;

/**
 * Clase configuración para el simulador.
 * @author Sebastián
 */
public class Config {
    //Parámetros de simulación 
    private int  cyclesAmount;             // cantidad de ciclos a simular (si aplicas tope)
    private volatile long cycleDuration;   // ms por ciclo (cambia en caliente desde GUI)

    // --- Política y quantum ---
    private PlanPolicy policy;             // debe poder cambiarse en ejecución
    private int  quantum;                  // quantum en ciclos (RR)
    private int  remainingQuantum;

    private final String configFile = "system_config.json"; // (reservado para persistencia)

    // Constructores
    public Config() {
        this(100, 100L, PlanPolicy.FCFS, 20);
    }

    public Config(int cyclesAmount) {
        this(cyclesAmount, 100L, PlanPolicy.FCFS, 20);
    }

    public Config(int cyclesAmount, long initialCycleDuration, PlanPolicy policy, int quantum) {
        this.cyclesAmount     = Math.max(1,  cyclesAmount);
        this.cycleDuration    = Math.max(1L, initialCycleDuration);
        this.policy           = (policy != null) ? policy : PlanPolicy.FCFS;
        this.quantum          = Math.max(1, quantum);
        this.remainingQuantum = this.quantum;
    }

    // Getters / Setters 
    public synchronized int getCyclesAmount() { return cyclesAmount; }
    public synchronized void setCyclesAmount(int cyclesAmount) {
        this.cyclesAmount = Math.max(1, cyclesAmount);
    }

    public long getCycleDuration() { return cycleDuration; }
    public void setCycleDuration(long cycleDuration) {
        this.cycleDuration = Math.max(1L, cycleDuration);
    }

    public synchronized PlanPolicy getPolicy() { return policy; }
    public synchronized void setPolicy(PlanPolicy newPolicy) {
        if (newPolicy != null && newPolicy != this.policy) {
            this.policy = newPolicy;
            // si cambias a RR, arranca rebanada nueva
            if (newPolicy == PlanPolicy.RR) {
                this.remainingQuantum = this.quantum;
            }
        }
    }

    public synchronized int getQuantum() { return quantum; }
    public synchronized void setQuantum(int quantum) {
        this.quantum = Math.max(1, quantum);
        this.remainingQuantum = this.quantum; // reset al cambiar quantum
    }

    public synchronized int getRemainingQuantum() { return remainingQuantum; }
    public synchronized void reduceRemainingQuantum() {
        if (remainingQuantum > 0) remainingQuantum--;
    }
    public synchronized void resetRemainingQuantum() { remainingQuantum = quantum; }
}

