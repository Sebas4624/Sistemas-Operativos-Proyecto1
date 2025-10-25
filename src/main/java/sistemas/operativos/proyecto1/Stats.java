package sistemas.operativos.proyecto1;

import sistemas.operativos.proyecto1.lib.LinkedList;
import sistemas.operativos.proyecto1.process.Process;

/**
 *
 * @author Sebastián
 */
public class Stats {
    public long totalProcesses;
    public long completedProcesses;
    public double avgWait;
    public double avgResp;
    public double avgTurn;
    public double util;
    public double throughput;
    public double fairness;
    
    public Process currentProcess;
    public LinkedList<Process> readyQueue;
    public LinkedList<Process> ioQueue;
    public LinkedList<Process> finishedQueue;
    
    public long currentCycle;
    
    public Stats() {
        this.totalProcesses = 0;
        this.completedProcesses = 0;
        this.avgWait = 0;
        this.avgResp = 0;
        this.avgTurn = 0;
        this.util = 0;
        this.throughput = 0;
        this.fairness = 0;
        
        this.currentProcess = null;
        this.readyQueue = new LinkedList();
        this.ioQueue = new LinkedList();
        this.finishedQueue = new LinkedList();
        
        this.currentCycle = 0;
    }
    
    public void setTotalProcesses(long total) { totalProcesses = total; }
    public void setCompletedProcesses(long completed) { completedProcesses = completed; }
    public void setAvgWait(double avg) { avgWait = avg; }
    public void setAvgResp(double avg) { avgResp = avg; }
    public void setAvgTurn(double avg) { avgTurn = avg; }
    public void setUtil(double per) { util = per; }
    public void setThroughput(double through) { throughput = through; }
    public void setFairness(double fair) { fairness = fair; }
    public void setCurrentCycle(long c) { currentCycle = c; }
    
    public boolean isCurrentProcessAvailable() {
        return this.currentProcess != null;
    }
    public void setCurrentProcess(Process current) {
        this.currentProcess = current;
    }
    
    public void setReadyQueue(LinkedList<Process> q) { readyQueue = q; }
    public void setIoQueue(LinkedList<Process> q) { ioQueue = q; }
    public void setFinishedQueue(LinkedList<Process> q) { finishedQueue = q; }
    
    public String[] getReadyQueueList() {
        String[] res = new String[readyQueue.size()];
        
        for(int i = 0; i < res.length; i++) { 
            res[i] = readyQueue.get(i).name();
        }
        
        return res;
    }
    public String[] getIoQueueList() {
        String[] res = new String[ioQueue.size()];
        
        for(int i = 0; i < res.length; i++) { 
            res[i] = ioQueue.get(i).name();
        }
        
        return res;
    }
    public String[] getFinishedQueueList() {
        String[] res = new String[finishedQueue.size()];
        
        for(int i = 0; i < res.length; i++) { 
            res[i] = finishedQueue.get(i).name();
        }
        
        return res;
    }
}
