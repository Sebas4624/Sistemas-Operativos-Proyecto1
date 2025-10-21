package sistemas.operativos.proyecto1;

/**
 *
 * @author Sebastián
 */
public class Config {
    private int cyclesAmount;
    private long cycleDuration;
    private final String configFile = "system_config.json";
    
    public Config(int cyclesAmount) {
        this.cyclesAmount = cyclesAmount;
        this.cycleDuration = 100; // default: 100ms
    }
    
    public Config(int cyclesAmount, long initialCycleDuration) {
        this.cyclesAmount = cyclesAmount;
        this.cycleDuration = initialCycleDuration;
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
}
