/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.TheDgtl.Stargate.listeners;

import java.io.File;
import net.TheDgtl.Stargate.Portal;
import net.TheDgtl.Stargate.Stargate;
import static net.TheDgtl.Stargate.Stargate.server;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

/**
 *
 * @author Frostalf
 */
public class StarGateWorldListener implements Listener {

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        Portal.loadAllGates(event.getWorld());
    }

    // We need to reload all gates on world unload, boo
    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        Stargate.debug("onWorldUnload", "Reloading all Stargates");
        World w = event.getWorld();
        String location = Stargate.getSaveLocation();
        File db = new File(location, w.getName() + ".db");
        if (db.exists()) {
            Portal.clearGates();
            for (World world : server.getWorlds()) {
                if (world.equals(w)) {
                    continue;
                }
                Portal.loadAllGates(world);
            }
        }
    }

}
