/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemas.operativos.proyecto1.sched;
import sistemas.operativos.proyecto1.lib.LinkedList;
import sistemas.operativos.proyecto1.process.Process;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author nicolepinto
 */
public class HRRN implements Scheduler {
    private final LinkedList<Process> ready = new LinkedList<Process>();
    private final Map<String, Long> waitTicks = new HashMap<String, Long>(); // id -> espera acumulada

    @Override 
    public String name() { return "HRRN"; }

    @Override 
    public void onProcessArrived(Process p) {
        p.setReady();
        ready.addLast(p);
        waitTicks.put(p.id(), 0L);
    }

    @Override 
    public void onProcessUnblocked(Process p) {
        p.setReady();
        ready.addLast(p);
        // Resetear espera al re-entrar a READY
        waitTicks.put(p.id(), 0L);
    }

    @Override 
    public void onProcessPreempted(Process p) {
        p.setReady();
        ready.addLast(p);
        // Resetear espera al re-entrar a READY
        waitTicks.put(p.id(), 0L);
    }

    @Override
    public Process selectNext() {
        if (ready.size() == 0) return null;
        int idxBest = 0;
        double bestR = responseRatio(ready.get(0));
        for (int i = 1; i < ready.size(); i++) {
            double r = responseRatio(ready.get(i));
            if (r > bestR) { bestR = r; idxBest = i; }
        }
        Process best = ready.get(idxBest);
        ready.remove(best);
        waitTicks.remove(best.id()); // resetea su contador al despachar
        return best;
    }

    @Override
    public void onTick(long cycle) {
        // Cada ciclo, todos los READY suman 1 de espera
        for (int i = 0; i < ready.size(); i++) {
            Process p = ready.get(i);
            waitTicks.put(p.id(), waitTicks.getOrDefault(p.id(), 0L) + 1L);
        }
    }

    private double responseRatio(Process p) {
        long w = waitTicks.getOrDefault(p.id(), 0L);
        int s = Math.max(1, p.remaining()); // evita división por 0
        return (double) (w + s) / (double) s;
    }
}

