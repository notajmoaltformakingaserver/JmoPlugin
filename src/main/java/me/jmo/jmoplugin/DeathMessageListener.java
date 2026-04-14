package me.jmo.jmoplugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathMessageListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        String message = e.getDeathMessage();
        if (message == null) return;

        // Cancel the default broadcast
        e.setDeathMessage(null);

        String deadWorld = e.getEntity().getWorld().getName();

        // Only send the death message to players in the same world
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getWorld().getName().equals(deadWorld)) {
                online.sendMessage(message);
            }
        }
    }
}
