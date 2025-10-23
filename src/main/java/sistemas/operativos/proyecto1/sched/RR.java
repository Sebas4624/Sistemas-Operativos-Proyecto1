/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemas.operativos.proyecto1.sched;

import sistemas.operativos.proyecto1.Config;
import sistemas.operativos.proyecto1.lib.Queue;
import sistemas.operativos.proyecto1.process.Process;

/**
 *
 * @author nicolepinto
 */
public class RR implements Scheduler {
    private final Queue<Process> ready = new Queue<Process>();
    private final Config config;

    public RR(Config config) { this.config = config; }

    @Override public String name() { return "RR(q=" + config.getQuantum() + ")"; }

    @Override public void onProcessArrived(Process p)   { p.setReady(); ready.enqueue(p); }
    @Override public void onProcessUnblocked(Process p) { p.setReady(); ready.enqueue(p); }
    @Override public void onProcessPreempted(Process p) { p.setReady(); ready.enqueue(p); }

    @Override public Process selectNext() {
        config.resetRemainingQuantum();                 // reset al despachar
        return ready.isEmpty() ? null : ready.dequeue();
    }

    @Override public void onTick(long cycle) { /* no-op */ }
}


