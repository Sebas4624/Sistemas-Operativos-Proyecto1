package sistemas.operativos.proyecto1;

/**
 *
 * @author Sebastián
 */
public class Config {
    private int cyclesAmount;
    private long cycleDuration;
    private final String configFile = "system_config.json";
    private final PlanPolicy policy;
    private final int quantum;  // "Quantum" en forma de "ciclos"
    private int remainingQuantum;
    
    public Config() {
        this.cyclesAmount = 100;
        this.cycleDuration = 100; // default: 100ms
        this.policy = PlanPolicy.FCFS;
        this.quantum = 20;
        this.remainingQuantum = 20;
    }
    
    public Config(int cyclesAmount) {
        this.cyclesAmount = cyclesAmount;
        this.cycleDuration = 100; // default: 100ms
        this.policy = PlanPolicy.FCFS;
        this.quantum = 20;
        this.remainingQuantum = 20;
    }
    
    public Config(int cyclesAmount, long initialCycleDuration, PlanPolicy policy, int quantum) {
        this.cyclesAmount = cyclesAmount;
        this.cycleDuration = initialCycleDuration;
        this.policy = policy;
        this.quantum = quantum;
        this.remainingQuantum = quantum;
    }
    /*
    // Guardar configuración
    public void saveConfig() {
        try {
            JSONObject config = new JSONObject();
            config.put("cycleDuration", cycleDuration);
            
            Files.write(Paths.get(configFile), config.toString().getBytes());
        } catch (IOException e) {
            System.err.println("Error guardando configuración: " + e.getMessage());
        }
    }
    
    // Cargar configuración
    public void loadConfig() {
        try {
            String content = new String(Files.readAllBytes(Paths.get(configFile)));
            JSONObject config = new JSONObject(content);
            this.cycleDuration = config.getLong("cycleDuration");
        } catch (IOException e) {
            System.err.println("Error cargando configuración: " + e.getMessage());
        }
    }
    */
    // Getters y Setters
    public long getCycleDuration() { return cycleDuration; }
    public void setCycleDuration(long cycleDuration) { 
        this.cycleDuration = cycleDuration; 
    }
    
    public int getCyclesAmount() { return cyclesAmount; }
    public void getCyclesAmount(int cyclesAmount) {
        this.cyclesAmount = cyclesAmount;
    }
    
    public PlanPolicy getPolicy() { return policy; }
    
    public int getQuantum() { return quantum; }
    public int getRemainingQuantum() { return remainingQuantum; }
    public void reduceRemainingQuantum() { remainingQuantum--; }
    public void resetRemainingQuantum() { remainingQuantum = quantum; }
}
