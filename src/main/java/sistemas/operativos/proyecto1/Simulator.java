package sistemas.operativos.proyecto1;

import sistemas.operativos.proyecto1.process.Process;
import sistemas.operativos.proyecto1.process.ProcessType;
import sistemas.operativos.proyecto1.lib.LinkedList;

/**
 * Clase simulador del proyecto.
 * @author Sebastián
 */
public class Simulator {
    private final Config config;
    private final CPU cpu;
    private final Stats stats;
    private Thread cpuThread, ioThread;
    private volatile boolean running = false;
    /**
     * Constructor.
     * @param stats
     */
    public Simulator(Stats stats) {
        this.config = new Config(100);
        this.cpu = new CPU(config, stats);
        this.stats = stats;
    }
    
    /**
     * Constructor.
     * @param stats
     * @param config Configuración del simulador.
     */
    public Simulator(Stats stats, Config config) {
        this.config = config;
        this.cpu = new CPU(config, stats);
        this.stats = stats;
    }
    
    public void setScheduler(sistemas.operativos.proyecto1.sched.Scheduler s) {
        cpu.setScheduler(s);
    }
    
    public void resetState() {
        
    }

    /**
     * Inicia la simulación, ejecutando la función de ejecución de ciclo
     * adecuada, dependiendo de la política de planificación actual.
     */
    public void startSimulation() {
        switch(config.getPolicy()) {
            case PlanPolicy.FCFS -> {
                System.out.println("First-Come, First-Served");  
                for (int i = 1; i < config.getCyclesAmount() + 1; i++) {
                    if(Thread.currentThread().isInterrupted()) {
                        stats.addLog("Simulación pausada.");
                        return;
                    }
                    
                    stats.setCurrentCycle();
                    cpu.simulateCycleFCFS();
                    updateReport();
                    if(cpu.isActive()) {
                        stats.addLog("Simulación finalizada.");
                        return;
                    }
                }
            }
            case RR -> {
                System.out.println("Round Robin");  
                for (int i = 1; i < config.getCyclesAmount() + 1; i++) {
                    if(Thread.currentThread().isInterrupted()) {
                        stats.addLog("Simulación pausada.");
                        return;
                    }
                    
                    stats.setCurrentCycle();
                    cpu.simulateCycleRR();
                    if(cpu.isActive()) {
                        stats.addLog("Simulación finalizada.");
                        return;
                    }
                }
            }
            case SRTF -> {
                System.out.println("Shortest Remaining Time First");
                for (int i = 1; i <= config.getCyclesAmount(); i++) {
                    if(Thread.currentThread().isInterrupted()) {
                        stats.addLog("Simulación pausada.");
                        return;
                    }
                    
                    stats.setCurrentCycle();
                    cpu.simulateCycleSRTF();
                    if(cpu.isActive()) {
                        stats.addLog("Simulación finalizada.");
                        return;
                    }
                }
            }

            case PRI -> {
                System.out.println("Cola por prioridad");  
                for (int i = 1; i < config.getCyclesAmount() + 1; i++) {
                    if(Thread.currentThread().isInterrupted()) {
                        stats.addLog("Simulación pausada.");
                        return;
                    }
                    
                    stats.setCurrentCycle();
                    cpu.simulateCyclePRI();
                    if(cpu.isActive()) {
                        stats.addLog("Simulación finalizada.");
                        return;
                    }
                }
            }
            case MFQ -> {
            }
            default -> throw new AssertionError(config.getPolicy().name());
        }
        printReport();
        dumpLogToFile();
    }
    
    /**
     * Crea un proceso dentro del simulador para ser usado y ejecutado dentro
     * del procesador.
     * @param name Nombre del proceso.
     * @param arrivalTime Tiempo de llegada del proceso.
     * @param instructions Cantidad de instrucciones del proceso.
     * @param type Tipo de proceso [CPU_BOUND - IO_BOUND].
     * @param cyclesForException Ciclos necesarios para generar una excepción.
     * @param cyclesToSatisfy Ciclos necesarios para satisfacer dicha excepción.
     * @param priority Nivel de prioridad del proceso.
     */
    public void createProcess(String name, int arrivalTime, int instructions, ProcessType type, int cyclesForException, int cyclesToSatisfy, int priority) {
        switch(config.getPolicy()) {
            case PRI -> cpu.createPriorityProcess(name, arrivalTime, instructions, type, cyclesForException, cyclesToSatisfy, priority);
            default -> cpu.createProcess(name, arrivalTime, instructions, type, cyclesForException, cyclesToSatisfy, priority);
        }
    }
    
    public void printReport() {
        LinkedList<Process> procs = cpu.getAllProcesses();
        int n = procs.size();
        if (n == 0) {
            System.out.println("No hay procesos para reportar.");
            stats.setTotalProcesses(0);
            stats.setCompletedProcesses(0);
            stats.setAvgWait(0);
            stats.setAvgResp(0);
            stats.setAvgTurn(0);
            stats.setUtil(0);
            stats.setThroughput(0);
            stats.setFairness(1.0);
            stats.setCurrentProcess(null);
            return;
        }

        long completed = 0;
        long sumWait = 0;
        long sumResp = 0;
        long sumTurn = 0;
        
        int respCount = 0;
        int turnCount = 0;

        // Para “equidad” (fairness), usaremos Jain sobre el tiempo de espera:
        // J = ( (Σw)^2 ) / ( n * Σ(w^2) ),  0< J ≤ 1 (1 es perfectamente justo)
        double sumW = 0.0;
        double sumW2 = 0.0;

        System.out.println("\n===== REPORTE =====");
        for (int i = 0; i < n; i++) {
            var p = procs.get(i);
            
            Integer start  = p.firstRun();
            Integer finish = p.finishTime();
            long wait      = p.totalWait();
            Integer arr    = p.arrival();

            Integer resp   = (start == null || arr == null)  ? null : (start - arr);
            Integer turn   = (finish == null || arr == null) ? null : (finish - arr);

            if (finish != null) completed++;

            sumWait += wait;
             if (resp != null) { sumResp += resp; respCount++; }
             if (turn != null) { sumTurn += turn; turnCount++; }
             
            sumW  += wait;
            sumW2 += ((double) wait) * wait;

            System.out.printf(
                "%s  arr=%d  start=%s  finish=%s  wait=%d  resp=%s  turn=%s  pc=%d%n",
                p.name(), arr,
                String.valueOf(start), String.valueOf(finish),
                wait,
                String.valueOf(resp), String.valueOf(turn),
                p.pc()
            );
        }

        // Promedios (se calculan sobre los que tienen valor)
        double avgWait = (n == 0) ? 0 : (double) sumWait / n;
        double avgResp = (respCount == 0) ? 0 : (double) sumResp / respCount;
        double avgTurn = (turnCount == 0) ? 0 : (double) sumTurn / turnCount;

        // Utilización de CPU
        long busy = cpu.getBusyCycles();
        long totalCycles = config.getCyclesAmount();
        double util = (totalCycles == 0) ? 0 : (100.0 * busy / totalCycles);

        // Throughput = procesos completados / tiempo total simulado (en ciclos)
        double throughput = (totalCycles == 0) ? 0 : ((double) completed / totalCycles);

        // Fairness (Jain) sobre los tiempos de espera
        double fairness  = (n == 0 || sumW2 == 0.0) ? 1.0 : ((sumW * sumW) / (n * sumW2));

        System.out.println("----- Agregados -----");
        System.out.printf("Completados: %d de %d%n", completed, n);
        System.out.printf("Promedio WAIT: %.2f  | RESP: %.2f  | TURN: %.2f%n", avgWait, avgResp, avgTurn);
        System.out.printf("CPU Utilization: %.1f%%%n", util);
        System.out.printf("Throughput (proc/ciclo): %.4f%n", throughput);
        System.out.printf("Fairness (Jain sobre WAIT): %.3f%n", fairness);
        System.out.println("====================\n");
        
        stats.setTotalProcesses(n);
        stats.setCompletedProcesses(completed);
        stats.setAvgWait(avgWait);
        stats.setAvgResp(avgResp);
        stats.setAvgTurn(avgTurn);
        stats.setUtil(util);
        stats.setThroughput(throughput);
        stats.setFairness(fairness);
        
        stats.setCurrentProcess(this.cpu.getCurrentProcess());
        stats.setReadyQueue(this.cpu.getReadyQueue());
        stats.setIoQueue(this.cpu.getIoQueue());
    }
    
    public void updateReport() {
        LinkedList<Process> procs = cpu.getAllProcesses();
        int n = procs.size();
        if (n == 0) {
            System.out.println("No hay procesos para reportar.");
            stats.setTotalProcesses(0);
            stats.setCompletedProcesses(0);
            stats.setAvgWait(0);
            stats.setAvgResp(0);
            stats.setAvgTurn(0);
            stats.setUtil(0);
            stats.setThroughput(0);
            stats.setFairness(1.0);
            stats.setCurrentProcess(null);
            return;
        }

        long completed = 0;
        long sumWait = 0;
        long sumResp = 0;
        long sumTurn = 0;
        
        int respCount = 0;
        int turnCount = 0;

        // Para “equidad” (fairness), usaremos Jain sobre el tiempo de espera:
        // J = ( (Σw)^2 ) / ( n * Σ(w^2) ),  0< J ≤ 1 (1 es perfectamente justo)
        double sumW = 0.0;
        double sumW2 = 0.0;

        System.out.println("\n===== REPORTE =====");
        for (int i = 0; i < n; i++) {
            var p = procs.get(i);
            
            Integer start  = p.firstRun();
            Integer finish = p.finishTime();
            long wait      = p.totalWait();
            Integer arr    = p.arrival();

            Integer resp   = (start == null || arr == null)  ? null : (start - arr);
            Integer turn   = (finish == null || arr == null) ? null : (finish - arr);

            if (finish != null) completed++;

            sumWait += wait;
             if (resp != null) { sumResp += resp; respCount++; }
             if (turn != null) { sumTurn += turn; turnCount++; }
             
            sumW  += wait;
            sumW2 += ((double) wait) * wait;

            System.out.printf(
                "%s  arr=%d  start=%s  finish=%s  wait=%d  resp=%s  turn=%s  pc=%d%n",
                p.name(), arr,
                String.valueOf(start), String.valueOf(finish),
                wait,
                String.valueOf(resp), String.valueOf(turn),
                p.pc()
            );
        }

        // Promedios (se calculan sobre los que tienen valor)
        double avgWait = (n == 0) ? 0 : (double) sumWait / n;
        double avgResp = (respCount == 0) ? 0 : (double) sumResp / respCount;
        double avgTurn = (turnCount == 0) ? 0 : (double) sumTurn / turnCount;

        // Utilización de CPU
        long busy = cpu.getBusyCycles();
        long totalCycles = config.getCyclesAmount();
        double util = (totalCycles == 0) ? 0 : (100.0 * busy / totalCycles);

        // Throughput = procesos completados / tiempo total simulado (en ciclos)
        double throughput = (totalCycles == 0) ? 0 : ((double) completed / totalCycles);

        // Fairness (Jain) sobre los tiempos de espera
        double fairness  = (n == 0 || sumW2 == 0.0) ? 1.0 : ((sumW * sumW) / (n * sumW2));
        
        stats.setTotalProcesses(n);
        stats.setCompletedProcesses(completed);
        stats.setAvgWait(avgWait);
        stats.setAvgResp(avgResp);
        stats.setAvgTurn(avgTurn);
        stats.setUtil(util);
        stats.setThroughput(throughput);
        stats.setFairness(fairness);
        
        stats.setCurrentProcess(this.cpu.getCurrentProcess());
        stats.setReadyQueue(this.cpu.getReadyQueue());
        stats.setIoQueue(this.cpu.getIoQueue());
    }
    
    private void dumpLogToFile() {
        try {
            java.nio.file.Path out = java.nio.file.Paths.get("events.log");
            String[] lines = cpu.getEventLogArray();  // ← de CPU
            String body = String.join(System.lineSeparator(), lines);
            java.nio.file.Files.writeString(out, body, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("No se pudo escribir events.log: " + e.getMessage());
        }
    }


    public void startSimulationAsync() {
        running = true;
        cpu.enableExternalIOThread(true);

        cpuThread = new Thread(() -> {
            while (running) {
                switch (config.getPolicy()) {
                    case PlanPolicy.FCFS -> cpu.simulateCycleFCFS();
                    case PlanPolicy.RR   -> cpu.simulateCycleRR();
                    case PlanPolicy.PRI  -> cpu.simulateCyclePRI();
                    case PlanPolicy.SRTF -> cpu.simulateCycleSRTF();
                    case PlanPolicy.SJF  -> cpu.simulateCycleFCFS();   
                    case PlanPolicy.HRRN -> cpu.simulateCycleFCFS();   
                    default -> throw new AssertionError(config.getPolicy().name());
                }
            }
        }, "CPU-Thread");

        ioThread = new Thread(() -> {
            while (running) {
                cpu.processIOCycleOneTick();
                try { Thread.sleep(config.getCycleDuration()); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            }
        }, "IO-Thread");

        cpuThread.start();
        ioThread.start();
     }

    public void stopSimulation() {
        running = false;
        if (cpuThread != null) cpuThread.interrupt();
        if (ioThread  != null) ioThread.interrupt();
        cpu.enableExternalIOThread(false);
        printReport();
        dumpLogToFile();
    }
    
    public long getCyclesDuration() { return config.getCycleDuration(); }
    public int getCyclesAmount() { return config.getCyclesAmount(); }
    public int getCyclesQuantum() { return config.getQuantum(); }
    
    public void setCyclesDuration(long n) { this.config.setCycleDuration(n); }
    public void setCyclesAmount(int n) { this.config.setCyclesAmount(n); }
    public void setCyclesQuantum(int n) { this.config.setQuantum(n); }
    
    public void updateReadyQueue() {
        stats.setReadyQueue(this.cpu.getReadyQueue());
    }
}
