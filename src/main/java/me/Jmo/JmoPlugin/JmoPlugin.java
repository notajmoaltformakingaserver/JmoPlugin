package me.jmo.jmoplugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class JmoPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("JmoPlugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("JmoPlugin disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("hi")) {
            player.sendMessage("hi " + player.getName() + "!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("jmotest")) {
            player.sendMessage(ChatColor.GREEN + "The Plugin Is Working Correctly");
            return true;
        }

        return false;
    }
}
