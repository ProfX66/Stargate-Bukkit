/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.TheDgtl.Stargate.listeners;

import net.TheDgtl.Stargate.EconomyHandler;
import net.TheDgtl.Stargate.Stargate;
import static net.TheDgtl.Stargate.Stargate.log;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

/**
 *
 * @author Frostalf
 */
public class StarGateServerListener implements Listener {

    private Stargate plugin = Stargate.getInstance();

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (EconomyHandler.setupEconomy(plugin.getServer().getPluginManager())) {
            log.info("[Stargate] Vault v" + EconomyHandler.vault.getDescription().getVersion() + " found");
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().equals(EconomyHandler.vault)) {
            log.info("[Stargate] Vault plugin lost.");
        }
    }

}
