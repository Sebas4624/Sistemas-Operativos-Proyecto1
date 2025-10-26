package sistemas.operativos.proyecto1;

import sistemas.operativos.proyecto1.process.ProcessType;
import sistemas.operativos.proyecto1.gui.*;

import javax.swing.JFrame;

/**
 * Archivo "Main" del proyecto.
 * @author Sebastián
 * @author Nicole
 */
public class Proyecto1 {

    public static void main(String[] args) {
        Stats stats = new Stats();
        Config config = new Config();
        Simulator sim = new Simulator(stats, config);

        JFrame simulatorView = new MainView(sim, stats);
        simulatorView.setVisible(true);
        
        stats.addLog("Programa iniciado.");
    }
}
