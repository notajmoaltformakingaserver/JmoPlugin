package me.jmo.jmoplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;

import java.util.*;

public class ItemEditorGUI implements Listener {

    private final Map<UUID, String> awaitingInput = new HashMap<>();
    private final Map<UUID, ItemStack> editingItem = new HashMap<>();

    private static final String MAIN_TITLE = ChatColor.DARK_AQUA + "Item Editor";
    private static final String LORE_TITLE = ChatColor.DARK_AQUA + "Edit Lore";
    private static final String ENCHANT_TITLE = ChatColor.DARK_AQUA + "Enchantments";
    private static final String FLAGS_TITLE = ChatColor.DARK_AQUA + "Item Flags";

    public void openMainMenu(Player p) {
        ItemStack held = p.getInventory().getItemInMainHand();
        if (held == null || held.getType().isAir()) {
            p.sendMessage(ChatColor.RED + "Hold an item to edit!");
            return;
        }
        editingItem.put(p.getUniqueId(), held);
        Inventory gui = Bukkit.createInventory(null, 27, MAIN_TITLE);

        gui.setItem(10, btn(Material.NAME_TAG, ChatColor.AQUA + "Rename", ChatColor.GRAY + "Set the display name"));
        gui.setItem(11, btn(Material.WRITABLE_BOOK, ChatColor.AQUA + "Edit Lore", ChatColor.GRAY + "Add, remove or clear lore"));
        gui.setItem(12, btn(Material.ENCHANTING_TABLE, ChatColor.AQUA + "Enchantments", ChatColor.GRAY + "Add or remove enchantments"));
        gui.setItem(13, btn(Material.BLAZE_POWDER, ChatColor.AQUA + "Toggle Glow", ChatColor.GRAY + "Toggle enchant glow effect"));
        gui.setItem(14, btn(Material.ANVIL, ChatColor.AQUA + "Unbreakable", ChatColor.GRAY + "Toggle unbreakable"));
        gui.setItem(15, btn(Material.COMPARATOR, ChatColor.AQUA + "Item Flags", ChatColor.GRAY + "Hide attributes, enchants etc."));
        gui.setItem(16, btn(Material.COMMAND_BLOCK, ChatColor.AQUA + "Model Data", ChatColor.GRAY + "Set custom model data"));

        gui.setItem(4, preview(held));
        fill(gui, 27);
        p.openInventory(gui);
    }

    private void openLoreMenu(Player p) {
        ItemStack item = editingItem.get(p.getUniqueId());
        if (item == null) return;
        Inventory gui = Bukkit.createInventory(null, 27, LORE_TITLE);
        gui.setItem(10, btn(Material.QUILL, ChatColor.GREEN + "Add Line", ChatColor.GRAY + "Type a new line in chat"));
        gui.setItem(12, btn(Material.FLINT, ChatColor.YELLOW + "Remove Last Line", ChatColor.GRAY + "Removes the bottom lore line"));
        gui.setItem(14, btn(Material.BARRIER, ChatColor.RED + "Clear Lore", ChatColor.GRAY + "Wipes all lore lines"));
        gui.setItem(16, btn(Material.ARROW, ChatColor.GRAY + "Back", ChatColor.GRAY + "Return to main menu"));
        gui.setItem(4, preview(item));
        fill(gui, 27);
        p.openInventory(gui);
    }

    private void openEnchantMenu(Player p) {
        ItemStack item = editingItem.get(p.getUniqueId());
        if (item == null) return;
        Inventory gui = Bukkit.createInventory(null, 27, ENCHANT_TITLE);
        gui.setItem(10, btn(Material.EXPERIENCE_BOTTLE, ChatColor.GREEN + "Add Enchantment",
                ChatColor.GRAY + "Type: ENCHANT_NAME LEVEL in chat", ChatColor.GRAY + "e.g. DAMAGE_ALL 5"));
        gui.setItem(13, btn(Material.LAVA_BUCKET, ChatColor.RED + "Remove Enchantment",
                ChatColor.GRAY + "Type the enchant name in chat"));
        gui.setItem(16, btn(Material.ARROW, ChatColor.GRAY + "Back", ChatColor.GRAY + "Return to main menu"));
        gui.setItem(4, preview(item));
        fill(gui, 27);
        p.openInventory(gui);
    }

    private void openFlagsMenu(Player p) {
        ItemStack item = editingItem.get(p.getUniqueId());
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        Inventory gui = Bukkit.createInventory(null, 36, FLAGS_TITLE);

        int[] slots = {10, 11, 12, 13, 14, 15, 19, 20, 21};
        ItemFlag[] flags = ItemFlag.values();
        for (int i = 0; i < Math.min(flags.length, slots.length); i++) {
            ItemFlag flag = flags[i];
            boolean on = meta != null && meta.hasItemFlag(flag);
            String label = flag.name().replace("HIDE_", "").replace("_", " ");
            gui.setItem(slots[i], btn(
                on ? Material.LIME_DYE : Material.GRAY_DYE,
                (on ? ChatColor.GREEN : ChatColor.RED) + label,
                ChatColor.GRAY + (on ? "Click to remove" : "Click to add")
            ));
        }
        gui.setItem(31, btn(Material.ARROW, ChatColor.GRAY + "Back", ChatColor.GRAY + "Return to main menu"));
        gui.setItem(4, preview(item));
        fill(gui, 36);
        p.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();
        boolean ourGui = title.equals(MAIN_TITLE) || title.equals(LORE_TITLE)
                || title.equals(ENCHANT_TITLE) || title.equals(FLAGS_TITLE);
        if (!ourGui) return;

        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType().isAir() || !clicked.hasItemMeta()) return;

        String label = ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).trim();
        ItemStack item = editingItem.get(p.getUniqueId());
        if (item == null) return;

        if (title.equals(MAIN_TITLE)) {
            switch (label) {
                case "Rename":
                    p.closeInventory();
                    awaitingInput.put(p.getUniqueId(), "rename");
                    p.sendMessage(ChatColor.AQUA + "Type the new name in chat. Use & for colours.");
                    break;
                case "Edit Lore": openLoreMenu(p); break;
                case "Enchantments": openEnchantMenu(p); break;
                case "Toggle Glow": toggleGlow(p, item); openMainMenu(p); break;
                case "Unbreakable": toggleUnbreakable(p, item); openMainMenu(p); break;
                case "Item Flags": openFlagsMenu(p); break;
                case "Model Data":
                    p.closeInventory();
                    awaitingInput.put(p.getUniqueId(), "modeldata");
                    p.sendMessage(ChatColor.AQUA + "Type the custom model data number in chat.");
                    break;
            }
        } else if (title.equals(LORE_TITLE)) {
            switch (label) {
                case "Add Line":
                    p.closeInventory();
                    awaitingInput.put(p.getUniqueId(), "loreadd");
                    p.sendMessage(ChatColor.AQUA + "Type the lore line in chat. Use & for colours.");
                    break;
                case "Remove Last Line": removeLastLore(p, item); openLoreMenu(p); break;
                case "Clear Lore": clearLore(p, item); openLoreMenu(p); break;
                case "Back": openMainMenu(p); break;
            }
        } else if (title.equals(ENCHANT_TITLE)) {
            switch (label) {
                case "Add Enchantment":
                    p.closeInventory();
                    awaitingInput.put(p.getUniqueId(), "enchantadd");
                    p.sendMessage(ChatColor.AQUA + "Type: ENCHANT_NAME LEVEL  e.g. DAMAGE_ALL 5");
                    break;
                case "Remove Enchantment":
                    p.closeInventory();
                    awaitingInput.put(p.getUniqueId(), "enchantremove");
                    p.sendMessage(ChatColor.AQUA + "Type the enchantment name to remove.");
                    break;
                case "Back": openMainMenu(p); break;
            }
        } else if (title.equals(FLAGS_TITLE)) {
            if (label.equals("Back")) { openMainMenu(p); return; }
            String flagName = "HIDE_" + label.replace(" ", "_").toUpperCase();
            try {
                ItemFlag flag = ItemFlag.valueOf(flagName);
                ItemMeta meta = item.getItemMeta();
                if (meta.hasItemFlag(flag)) meta.removeItemFlags(flag);
                else meta.addItemFlags(flag);
                item.setItemMeta(meta);
                openFlagsMenu(p);
            } catch (IllegalArgumentException ex) {
                p.sendMessage(ChatColor.RED + "Unknown flag.");
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!awaitingInput.containsKey(p.getUniqueId())) return;
        e.setCancelled(true);

        String input = e.getMessage();
        String mode = awaitingInput.remove(p.getUniqueId());
        ItemStack item = editingItem.get(p.getUniqueId());
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        switch (mode) {
            case "rename":
                meta.setDisplayName(color(input));
                item.setItemMeta(meta);
                p.sendMessage(ChatColor.GREEN + "Display name updated!");
                break;
            case "loreadd": {
                List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add(color(input));
                meta.setLore(lore);
                item.setItemMeta(meta);
                p.sendMessage(ChatColor.GREEN + "Lore line added!");
                break;
            }
            case "enchantadd": {
                String[] parts = input.split(" ", 2);
                if (parts.length < 2) { p.sendMessage(ChatColor.RED + "Format: ENCHANT_NAME LEVEL"); break; }
                Enchantment ench = Enchantment.getByName(parts[0].toUpperCase());
                if (ench == null) { p.sendMessage(ChatColor.RED + "Unknown enchantment."); break; }
                try {
                    meta.addEnchant(ench, Integer.parseInt(parts[1]), true);
                    item.setItemMeta(meta);
                    p.sendMessage(ChatColor.GREEN + "Enchantment added!");
                } catch (NumberFormatException ex) { p.sendMessage(ChatColor.RED + "Invalid level."); }
                break;
            }
            case "enchantremove": {
                Enchantment ench = Enchantment.getByName(input.toUpperCase());
                if (ench == null) { p.sendMessage(ChatColor.RED + "Unknown enchantment."); break; }
                meta.removeEnchant(ench);
                item.setItemMeta(meta);
                p.sendMessage(ChatColor.GREEN + "Enchantment removed!");
                break;
            }
            case "modeldata":
                try {
                    meta.setCustomModelData(Integer.parseInt(input));
                    item.setItemMeta(meta);
                    p.sendMessage(ChatColor.GREEN + "Model data set!");
                } catch (NumberFormatException ex) { p.sendMessage(ChatColor.RED + "Invalid number."); }
                break;
        }

        Bukkit.getScheduler().runTask(
            Bukkit.getPluginManager().getPlugin("JmoPlugin"),
            () -> openMainMenu(p)
        );
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player p = (Player) e.getPlayer();
        if (awaitingInput.containsKey(p.getUniqueId())) return;
        String title = e.getView().getTitle();
        if (title.equals(MAIN_TITLE) || title.equals(LORE_TITLE)
                || title.equals(ENCHANT_TITLE) || title.equals(FLAGS_TITLE)) {
            editingItem.remove(p.getUniqueId());
        }
    }

    private void toggleGlow(Player p, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasEnchants()) {
            meta.addEnchant(Enchantment.LUCK, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            p.sendMessage(ChatColor.GREEN + "Glow added!");
        } else {
            new HashSet<>(meta.getEnchants().keySet()).forEach(meta::removeEnchant);
            p.sendMessage(ChatColor.GREEN + "Glow removed!");
        }
        item.setItemMeta(meta);
    }

    private void toggleUnbreakable(Player p, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.setUnbreakable(!meta.isUnbreakable());
        item.setItemMeta(meta);
        p.sendMessage(ChatColor.GREEN + "Unbreakable " + (meta.isUnbreakable() ? "ON!" : "OFF!"));
    }

    private void removeLastLore(Player p, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore() || meta.getLore().isEmpty()) {
            p.sendMessage(ChatColor.RED + "No lore to remove!");
            return;
        }
        List<String> lore = new ArrayList<>(meta.getLore());
        lore.remove(lore.size() - 1);
        meta.setLore(lore);
        item.setItemMeta(meta);
        p.sendMessage(ChatColor.GREEN + "Last lore line removed!");
    }

    private void clearLore(Player p, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.setLore(new ArrayList<>());
        item.setItemMeta(meta);
        p.sendMessage(ChatColor.GREEN + "Lore cleared!");
    }

    private ItemStack btn(Material mat, String name, String... lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(name);
        m.setLore(Arrays.asList(lore));
        m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        i.setItemMeta(m);
        return i;
    }

    private ItemStack preview(ItemStack item) {
        ItemStack clone = item.clone();
        ItemMeta m = clone.getItemMeta();
        if (m == null) return clone;
        List<String> lore = m.hasLore() ? new ArrayList<>(m.getLore()) : new ArrayList<>();
        lore.add(0, ChatColor.DARK_GRAY + "--- Currently Editing ---");
        m.setLore(lore);
        clone.setItemMeta(m);
        return clone;
    }

    private void fill(Inventory gui, int size) {
        ItemStack glass = btn(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < size; i++) {
            if (gui.getItem(i) == null) gui.setItem(i, glass);
        }
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}