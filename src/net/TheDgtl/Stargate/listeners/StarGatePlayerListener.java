/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.TheDgtl.Stargate.listeners;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.TheDgtl.Stargate.EconomyHandler;
import net.TheDgtl.Stargate.Portal;
import net.TheDgtl.Stargate.Stargate;
import static net.TheDgtl.Stargate.Stargate.bungeeQueue;
import static net.TheDgtl.Stargate.Stargate.canAccessNetwork;
import static net.TheDgtl.Stargate.Stargate.canAccessPortal;
import static net.TheDgtl.Stargate.Stargate.canAccessServer;
import static net.TheDgtl.Stargate.Stargate.canAccessWorld;
import static net.TheDgtl.Stargate.Stargate.enableBungee;
import static net.TheDgtl.Stargate.Stargate.openPortal;
import static net.TheDgtl.Stargate.Stargate.sendMessage;
import static net.TheDgtl.Stargate.Stargate.server;
import static net.TheDgtl.Stargate.Stargate.stargate;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 *
 * @author Frostalf
 */
public class StarGatePlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!enableBungee) {
            return;
        }

        Player player = event.getPlayer();
        String destination = bungeeQueue.remove(player.getName().toLowerCase());
        if (destination == null) {
            return;
        }

        Portal portal = Portal.getBungeeGate(destination);
        if (portal == null) {
            Stargate.debug("PlayerJoin", "Error fetching destination portal: " + destination);
            return;
        }
        portal.teleport(player, portal, null);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // cancel portal and endgateway teleportation if it's from a Stargate entrance
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if (!event.isCancelled()
                && (cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL
                || cause == PlayerTeleportEvent.TeleportCause.END_GATEWAY && World.Environment.THE_END == event.getFrom().getWorld().getEnvironment())
                && Portal.getByAdjacentEntrance(event.getFrom()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled()) {
            return;
        }

        // Check to see if the player actually moved
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockY() == event.getTo().getBlockY() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        Portal portal = Portal.getByEntrance(event.getTo());
        // No portal or not open
        if (portal == null || !portal.isOpen()) {
            return;
        }

        // Not open for this player
        if (!portal.isOpenFor(player)) {
            Stargate.sendMessage(player, Stargate.getString("denyMsg"));
            portal.teleport(player, portal, event);
            return;
        }

        Portal destination = portal.getDestination(player);
        if (!portal.isBungee() && destination == null) {
            return;
        }

        boolean deny = false;
        // Check if player has access to this server for Bungee gates
        if (portal.isBungee()) {
            if (!canAccessServer(player, portal.getNetwork())) {
                deny = true;
            }
        } else {
            // Check if player has access to this network
            if (!canAccessNetwork(player, portal.getNetwork())) {
                deny = true;
            }

            // Check if player has access to destination world
            if (!canAccessWorld(player, destination.getWorld().getName())) {
                deny = true;
            }
        }

        if (!canAccessPortal(player, portal, deny)) {
            Stargate.sendMessage(player, Stargate.getString("denyMsg"));
            portal.teleport(player, portal, event);
            portal.close(false);
            return;
        }

        int cost = Stargate.getUseCost(player, portal, destination);
        if (cost > 0) {
            String target = portal.getGate().getToOwner() ? portal.getOwner() : null;
            if (!Stargate.chargePlayer(player, target, cost)) {
                // Insufficient Funds
                Stargate.sendMessage(player, "Insufficient Funds");
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

        // BungeeCord Support
        if (portal.isBungee()) {
            if (!enableBungee) {
                player.sendMessage(Stargate.getString("bungeeDisabled"));
                portal.close(false);
                return;
            }

            // Teleport the player back to this gate, for sanity's sake
            portal.teleport(player, portal, event);

            // Send the SGBungee packet first, it will be queued by BC if required
            try {
                // Build the message, format is <player>#@#<destination>
                String msg = event.getPlayer().getName() + "#@#" + portal.getDestinationName();
                // Build the message data, sent over the SGBungee bungeecord channel
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                DataOutputStream msgData = new DataOutputStream(bao);
                msgData.writeUTF("Forward");
                msgData.writeUTF(portal.getNetwork());	// Server
                msgData.writeUTF("SGBungee");			// Channel
                msgData.writeShort(msg.length()); 	// Data Length
                msgData.writeBytes(msg); 			// Data
                player.sendPluginMessage(stargate, "BungeeCord", bao.toByteArray());
            } catch (IOException ex) {
                Stargate.log.severe("[Stargate] Error sending BungeeCord teleport packet");
                ex.printStackTrace();
                return;
            }

            // Connect player to new server
            try {
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                DataOutputStream msgData = new DataOutputStream(bao);
                msgData.writeUTF("Connect");
                msgData.writeUTF(portal.getNetwork());

                player.sendPluginMessage(stargate, "BungeeCord", bao.toByteArray());
                bao.reset();
            } catch (IOException ex) {
                Stargate.log.severe("[Stargate] Error sending BungeeCord connect packet");
                ex.printStackTrace();
                return;
            }

            // Close portal if required (Should never be)
            portal.close(false);
            return;
        }

        destination.teleport(player, portal, event);
        portal.close(false);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        
        // Right click
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
        	if (block.getBlockData() instanceof WallSign) {
                Portal portal = Portal.getByBlock(block);
                if (portal == null) {
                    return;
                }
                // Cancel item use
                event.setUseItemInHand(Event.Result.DENY);
                event.setUseInteractedBlock(Event.Result.DENY);

                boolean deny = false;
                if (!Stargate.canAccessNetwork(player, portal.getNetwork())) {
                    deny = true;
                }

                if (!Stargate.canAccessPortal(player, portal, deny)) {
                    Stargate.sendMessage(player, Stargate.getString("denyMsg"));
                    return;
                }

                if ((!portal.isOpen()) && (!portal.isFixed())) {
                    portal.cycleDestination(player);
                }
                return;
            }

            // Implement right-click to toggle a stargate, gets around spawn protection problem.
            if ((block.getType() == Material.STONE_BUTTON)) {
                Portal portal = Portal.getByBlock(block);
                if (portal == null) {
                    return;
                }

                // Cancel item use
                event.setUseItemInHand(Event.Result.DENY);
                event.setUseInteractedBlock(Event.Result.DENY);

                boolean deny = false;
                if (!Stargate.canAccessNetwork(player, portal.getNetwork())) {
                    deny = true;
                }

                if (!Stargate.canAccessPortal(player, portal, deny)) {
                    Stargate.sendMessage(player, Stargate.getString("denyMsg"));
                    return;
                }

                openPortal(player, portal);
                if (portal.isOpenFor(player)) {
                    event.setUseInteractedBlock(Event.Result.ALLOW);
                }
            }
            return;
        }

        // Left click
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            // Check if we're scrolling a sign
        	if (block.getBlockData() instanceof WallSign) {
                Portal portal = Portal.getByBlock(block);
                if (portal == null) {
                    return;
                }

                event.setUseInteractedBlock(Event.Result.DENY);
                // Only cancel event in creative mode
                if (player.getGameMode().equals(GameMode.CREATIVE)) {
                    event.setCancelled(true);
                }

                boolean deny = false;
                if (!Stargate.canAccessNetwork(player, portal.getNetwork())) {
                    deny = true;
                }

                if (!Stargate.canAccessPortal(player, portal, deny)) {
                    Stargate.sendMessage(player, Stargate.getString("denyMsg"));
                    return;
                }

                if ((!portal.isOpen()) && (!portal.isFixed())) {
                    portal.cycleDestination(player, -1);
                }
            }
        }
    }
}
