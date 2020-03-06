/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.TheDgtl.Stargate.thread;

import net.TheDgtl.Stargate.BloxPopulator;
import net.TheDgtl.Stargate.Stargate;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.EndGateway;
import org.bukkit.block.data.Orientable;

/**
 *
 * @author Frostalf
 */
public class BlockPopulatorThread implements Runnable {

    @Override
    public void run() {
        long sTime = System.nanoTime();
        while (System.nanoTime() - sTime < 25000000) {
            BloxPopulator b = Stargate.blockPopulatorQueue.poll();
            if (b == null) {
                return;
            }
            Block blk = b.getBlox().getBlock();
            blk.setType(b.getMat(), false);
            if (b.getMat() == Material.END_GATEWAY && blk.getWorld().getEnvironment() == World.Environment.THE_END) {
                // force a location to prevent exit gateway generation
                EndGateway gateway = (EndGateway) blk.getState();
                gateway.setExitLocation(blk.getWorld().getSpawnLocation());
                gateway.setExactTeleport(true);
                gateway.update(false, false);
            } else if (b.getAxis() != null) {
                Orientable orientable = (Orientable) blk.getBlockData();
                orientable.setAxis(b.getAxis());
                blk.setBlockData(orientable);
            }
        }
    }

}
