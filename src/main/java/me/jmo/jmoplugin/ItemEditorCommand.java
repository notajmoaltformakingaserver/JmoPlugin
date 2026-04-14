package me.jmo.jmoplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemEditorCommand implements CommandExecutor {

    private final ItemEditorGUI editor;

    public ItemEditorCommand(ItemEditorGUI editor) {
        this.editor = editor;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player p = (Player) sender;
        if (!p.hasPermission("jmo.itemeditor")) {
            p.sendMessage(ChatColor.RED + "You do not have permission to use the item editor.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("open") || args[0].equalsIgnoreCase("gui")) {
            editor.openMainMenu(p);
            return true;
        }

        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            p.sendMessage(ChatColor.RED + "Hold an item to edit!");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "name":
            case "rename":
                if (!checkPerm(p, "jmo.itemeditor.rename", "rename items")) return true;
                String newName = join(args, 1);
                if (newName.isBlank()) {
                    p.sendMessage(ChatColor.RED + "Usage: /itemeditor name <display name>");
                    return true;
                }
                ItemMeta meta = ensureMeta(item);
                meta.setDisplayName(color(newName));
                item.setItemMeta(meta);
                p.sendMessage(ChatColor.GREEN + "Display name updated!");
                return true;
            case "loreadd":
                if (!checkPerm(p, "jmo.itemeditor.lore", "edit item lore")) return true;
                String loreLine = join(args, 1);
                if (loreLine.isBlank()) {
                    p.sendMessage(ChatColor.RED + "Usage: /itemeditor loreadd <line>");
                    return true;
                }
                meta = ensureMeta(item);
                List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add(color(loreLine));
                meta.setLore(lore);
                item.setItemMeta(meta);
                p.sendMessage(ChatColor.GREEN + "Lore line added!");
                return true;
            case "loreclear":
                if (!checkPerm(p, "jmo.itemeditor.lore", "clear lore")) return true;
                meta = ensureMeta(item);
                meta.setLore(new ArrayList<>());
                item.setItemMeta(meta);
                p.sendMessage(ChatColor.GREEN + "Lore cleared!");
                return true;
            case "enchant":
                if (!checkPerm(p, "jmo.itemeditor.enchant", "add enchantments")) return true;
                if (args.length < 3) {
                    p.sendMessage(ChatColor.RED + "Usage: /itemeditor enchant <ENCHANT_NAME> <level>");
                    return true;
                }
                Enchantment enchant = Enchantment.getByName(args[1].toUpperCase());
                if (enchant == null) {
                    p.sendMessage(ChatColor.RED + "Unknown enchantment.");
                    return true;
                }
                try {
                    int level = Integer.parseInt(args[2]);
                    meta = ensureMeta(item);
                    meta.addEnchant(enchant, level, true);
                    item.setItemMeta(meta);
                    p.sendMessage(ChatColor.GREEN + "Enchantment added!");
                } catch (NumberFormatException ex) {
                    p.sendMessage(ChatColor.RED + "Invalid enchantment level.");
                }
                return true;
            case "delenchant":
                if (!checkPerm(p, "jmo.itemeditor.enchant", "remove enchantments")) return true;
                if (args.length < 2) {
                    p.sendMessage(ChatColor.RED + "Usage: /itemeditor delenchant <ENCHANT_NAME>");
                    return true;
                }
                Enchantment remove = Enchantment.getByName(args[1].toUpperCase());
                if (remove == null) {
                    p.sendMessage(ChatColor.RED + "Unknown enchantment.");
                    return true;
                }
                meta = ensureMeta(item);
                meta.removeEnchant(remove);
                item.setItemMeta(meta);
                p.sendMessage(ChatColor.GREEN + "Enchantment removed!");
                return true;
            case "unbreakable":
                if (!checkPerm(p, "jmo.itemeditor.unbreakable", "toggle unbreakable")) return true;
                meta = ensureMeta(item);
                meta.setUnbreakable(!meta.isUnbreakable());
                item.setItemMeta(meta);
                p.sendMessage(ChatColor.GREEN + "Unbreakable " + (meta.isUnbreakable() ? "ON!" : "OFF!"));
                return true;
            case "model":
                if (!checkPerm(p, "jmo.itemeditor.modeldata", "set custom model data")) return true;
                if (args.length < 2) {
                    p.sendMessage(ChatColor.RED + "Usage: /itemeditor model <number>");
                    return true;
                }
                try {
                    meta = ensureMeta(item);
                    meta.setCustomModelData(Integer.parseInt(args[1]));
                    item.setItemMeta(meta);
                    p.sendMessage(ChatColor.GREEN + "Model data set!");
                } catch (NumberFormatException ex) {
                    p.sendMessage(ChatColor.RED + "Invalid number.");
                }
                return true;
            case "flag":
                if (!checkPerm(p, "jmo.itemeditor.flags", "toggle item flags")) return true;
                if (args.length < 2) {
                    p.sendMessage(ChatColor.RED + "Usage: /itemeditor flag <FLAG_NAME>");
                    return true;
                }
                toggleFlag(p, item, args[1], true);
                return true;
            case "unflag":
                if (!checkPerm(p, "jmo.itemeditor.flags", "toggle item flags")) return true;
                if (args.length < 2) {
                    p.sendMessage(ChatColor.RED + "Usage: /itemeditor unflag <FLAG_NAME>");
                    return true;
                }
                toggleFlag(p, item, args[1], false);
                return true;
            case "glow":
                if (!checkPerm(p, "jmo.itemeditor.glow", "toggle glow")) return true;
                editor.toggleGlow(p, item);
                return true;
            default:
                sendHelp(p);
                return true;
        }
    }

    private void toggleFlag(Player p, ItemStack item, String rawFlag, boolean add) {
        String flagName = rawFlag.toUpperCase().replace(" ", "_");
        try {
            ItemFlag flag = ItemFlag.valueOf(flagName);
            ItemMeta meta = ensureMeta(item);
            if (add) meta.addItemFlags(flag);
            else meta.removeItemFlags(flag);
            item.setItemMeta(meta);
            p.sendMessage(ChatColor.GREEN + "Item flag " + (add ? "added" : "removed") + ": " + flag.name());
        } catch (IllegalArgumentException ex) {
            p.sendMessage(ChatColor.RED + "Unknown item flag.");
        }
    }

    private void sendHelp(Player p) {
        p.sendMessage(ChatColor.YELLOW + "Item Editor Commands:");
        p.sendMessage(ChatColor.GRAY + "/itemeditor open" + ChatColor.WHITE + " - Open the editor GUI");
        p.sendMessage(ChatColor.GRAY + "/itemeditor name <name>" + ChatColor.WHITE + " - Rename the held item");
        p.sendMessage(ChatColor.GRAY + "/itemeditor loreadd <line>" + ChatColor.WHITE + " - Add lore");
        p.sendMessage(ChatColor.GRAY + "/itemeditor loreclear" + ChatColor.WHITE + " - Clear lore");
        p.sendMessage(ChatColor.GRAY + "/itemeditor enchant <ENCHANT> <level>" + ChatColor.WHITE + " - Add enchant");
        p.sendMessage(ChatColor.GRAY + "/itemeditor delenchant <ENCHANT>" + ChatColor.WHITE + " - Remove enchant");
        p.sendMessage(ChatColor.GRAY + "/itemeditor unbreakable" + ChatColor.WHITE + " - Toggle unbreakable");
        p.sendMessage(ChatColor.GRAY + "/itemeditor model <number>" + ChatColor.WHITE + " - Set model data");
        p.sendMessage(ChatColor.GRAY + "/itemeditor flag <FLAG>" + ChatColor.WHITE + " - Add item flag");
        p.sendMessage(ChatColor.GRAY + "/itemeditor unflag <FLAG>" + ChatColor.WHITE + " - Remove item flag");
        p.sendMessage(ChatColor.GRAY + "/itemeditor glow" + ChatColor.WHITE + " - Toggle glow effect");
    }

    private ItemMeta ensureMeta(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) meta = Bukkit.getItemFactory().getItemMeta(item.getType());
        return meta;
    }

    private boolean checkPerm(Player p, String node, String action) {
        if (p.hasPermission(node)) return true;
        p.sendMessage(ChatColor.RED + "You do not have permission to " + action + ".");
        return false;
    }

    private String join(String[] args, int start) {
        if (args.length <= start) return "";
        return String.join(" ", Arrays.copyOfRange(args, start, args.length));
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
