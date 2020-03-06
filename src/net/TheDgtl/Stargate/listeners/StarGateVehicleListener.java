/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.TheDgtl.Stargate.listeners;

import net.TheDgtl.Stargate.EconomyHandler;
import net.TheDgtl.Stargate.Portal;
import net.TheDgtl.Stargate.Stargate;
import static net.TheDgtl.Stargate.Stargate.canAccessNetwork;
import static net.TheDgtl.Stargate.Stargate.canAccessPortal;
import static net.TheDgtl.Stargate.Stargate.canAccessWorld;
import static net.TheDgtl.Stargate.Stargate.handleVehicles;
import static net.TheDgtl.Stargate.Stargate.sendMessage;
import static net.TheDgtl.Stargate.Stargate.server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

/**
 *
 * @author Frostalf
 */
public class StarGateVehicleListener implements Listener {

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!handleVehicles) {
            return;
        }
        Entity passenger = event.getVehicle().getPassenger();
        Vehicle vehicle = event.getVehicle();

        Portal portal = Portal.getByEntrance(event.getTo());
        if (portal == null || !portal.isOpen()) {
            return;
        }

        // We don't support vehicles in Bungee portals
        if (portal.isBungee()) {
            return;
        }

        if (passenger instanceof Player) {
            Player player = (Player) passenger;
            if (!portal.isOpenFor(player)) {
                Stargate.sendMessage(player, Stargate.getString("denyMsg"));
                return;
            }

            Portal dest = portal.getDestination(player);
            if (dest == null) {
                return;
            }
            boolean deny = false;
            // Check if player has access to this network
            if (!canAccessNetwork(player, portal.getNetwork())) {
                deny = true;
            }

            // Check if player has access to destination world
            if (!canAccessWorld(player, dest.getWorld().getName())) {
                deny = true;
            }

            if (!canAccessPortal(player, portal, deny)) {
                Stargate.sendMessage(player, Stargate.getString("denyMsg"));
                portal.close(false);
                return;
            }

            int cost = Stargate.getUseCost(player, portal, dest);
            if (cost > 0) {
                String target = portal.getGate().getToOwner() ? portal.getOwner() : null;
                if (!Stargate.chargePlayer(player, target, cost)) {
                    // Insufficient Funds
                    Stargate.sendMessage(player, Stargate.getString("inFunds"));
                    portal.close(false);
                    return;
                }
                String deductMsg = Stargate.getString("ecoDeduct");
                deductMsg = Stargate.replaceVars(deductMsg, new String[]{"%cost%", "%portal%"}, new String[]{EconomyHandler.format(cost), portal.getName()});
                sendMessage(player, deductMsg, false);
                if (target != null) {
                    Player p = server.getPlayer(target);
                    if (p != null) {
                        String obtainedMsg = Stargate.getString("ecoObtain");
                        obtainedMsg = Stargate.replaceVars(obtainedMsg, new String[]{"%cost%", "%portal%"}, new String[]{EconomyHandler.format(cost), portal.getName()});
                        Stargate.sendMessage(p, obtainedMsg, false);
                    }
                }
            }

            Stargate.sendMessage(player, Stargate.getString("teleportMsg"), false);
            dest.teleport(vehicle);
            portal.close(false);
        } else {
            Portal dest = portal.getDestination();
            if (dest == null) {
                return;
            }
            dest.teleport(vehicle);
        }
    }

}
