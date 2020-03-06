/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.TheDgtl.Stargate.listeners;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author Frostalf
 */
public class StarGateListener {

    public StarGateListener(Plugin plugin) {
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(new StarGatePlayerListener(), plugin);
        pm.registerEvents(new StarGateBlockListener(), plugin);

        pm.registerEvents(new StarGateEntityListener(), plugin);
        pm.registerEvents(new StarGateWorldListener(), plugin);
        pm.registerEvents(new StarGateVehicleListener(), plugin);
        pm.registerEvents(new StarGateServerListener(), plugin);
    }
}
