package me.jmo.jmoplugin;

import org.bukkit.entity.Cow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class MobListener implements Listener {

    private final JmoPlugin plugin;

    public MobListener(JmoPlugin plugin) {
        this.plugin = plugin;
    }

    // FIX: removed duplicate onCowDeath that was here before — it's handled in JmoPlugin.java
    // This class is kept for future mob-related listeners that don't belong in the main class
}
