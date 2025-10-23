/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemas.operativos.proyecto1.sched;



public interface Scheduler {
    String name();
    void onProcessArrived(sistemas.operativos.proyecto1.process.Process p);
    void onProcessUnblocked(sistemas.operativos.proyecto1.process.Process p);
    void onProcessPreempted(sistemas.operativos.proyecto1.process.Process running);
    sistemas.operativos.proyecto1.process.Process selectNext();
    void onTick(long cycle);
}



