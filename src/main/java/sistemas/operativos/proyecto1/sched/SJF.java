/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemas.operativos.proyecto1.sched;
import sistemas.operativos.proyecto1.lib.LinkedList;
import sistemas.operativos.proyecto1.process.Process;

/**
 *
 * @author nicolepinto
 */
public class SJF implements Scheduler {
    
    private final LinkedList<Process> ready = new LinkedList<Process>();

    @Override public String name() { return "SJF(non-preemptive)"; }

    @Override public void onProcessArrived(Process p)   { p.setReady(); ready.addLast(p); }
    @Override public void onProcessUnblocked(Process p) { p.setReady(); ready.addLast(p); }
    @Override public void onProcessPreempted(Process p) { p.setReady(); ready.addLast(p); }

    @Override
    public Process selectNext() {
        if (ready.size() == 0) return null;
        int idxBest = 0;
        int bestRem = ready.get(0).remaining();
        for (int i = 1; i < ready.size(); i++) {
            int rem = ready.get(i).remaining();
            if (rem < bestRem) { bestRem = rem; idxBest = i; }
        }
        Process best = ready.get(idxBest);
        ready.remove(best);
        return best;
    }

    @Override public void onTick(long cycle) { /* no-op */ }
}


