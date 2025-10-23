/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemas.operativos.proyecto1.sched;
import sistemas.operativos.proyecto1.lib.PriorityQueue;
import sistemas.operativos.proyecto1.process.Process;

/**
 *
 * @author nicolepinto
 */
public class PRI implements Scheduler {
   
    private final PriorityQueue<Process> ready = new PriorityQueue<Process>();

    @Override public String name() { return "PRIORITY(max)"; }  // mayor número = mayor prioridad

    @Override public void onProcessArrived(Process p)   { p.setReady(); ready.add(p); }
    @Override public void onProcessUnblocked(Process p) { p.setReady(); ready.add(p); }
    @Override public void onProcessPreempted(Process p) { p.setReady(); ready.add(p); }

    @Override public Process selectNext() { return ready.isEmpty() ? null : ready.poll(); }

    @Override public void onTick(long cycle) { /* no-op */ }
}


