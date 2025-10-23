package sistemas.operativos.proyecto1;

/**
 * Políticas de planificación.
 * @author Sebastián
 */
public enum PlanPolicy {
    FCFS, // First Come, First Served
    RR, // Round Robin
    SPN, // Shortest Process Next
    SRT, // Shortest Remaining Time
    PRI, // Priority
    MFQ, // Multilevel Feedback Queue
    SRTF
}
