/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.TheDgtl.Stargate.listeners;

import net.TheDgtl.Stargate.Portal;
import net.TheDgtl.Stargate.Stargate;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 *
 * @author Frostalf
 */
public class StarGateEntityListener implements Listener {

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        for (Block b : event.blockList()) {
            Portal portal = Portal.getByBlock(b);
            if (portal == null) {
                continue;
            }
            if (Stargate.destroyExplosion) {
                portal.unregister(true);
            } else {
                event.setCancelled(true);
                break;
            }
        }
    }

}
