/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.TheDgtl.Stargate.thread;

import java.util.Iterator;
import net.TheDgtl.Stargate.Portal;
import net.TheDgtl.Stargate.Stargate;

/**
 *
 * @author Frostalf
 */
public class StarGateThread implements Runnable {

    Stargate SG = Stargate.getInstance();

    @Override
    public void run() {
        long time = System.currentTimeMillis() / 1000;
        // Close open portals
        for (Iterator<Portal> iter = Stargate.openList.iterator(); iter.hasNext();) {
            Portal p = iter.next();
            // Skip always open gates
            if (p.isAlwaysOn()) {
                continue;
            }
            if (!p.isOpen()) {
                continue;
            }
            if (time > p.getOpenTime() + Stargate.openTime) {
                p.close(false);
                iter.remove();
            }
        }
        // Deactivate active portals
        for (Iterator<Portal> iter = Stargate.activeList.iterator(); iter.hasNext();) {
            Portal p = iter.next();
            if (!p.isActive()) {
                continue;
            }
            if (time > p.getOpenTime() + Stargate.activeTime) {
                p.deactivate();
                iter.remove();
            }
        }
    }

}
