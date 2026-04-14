package me.jmo.jmoplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.block.Block;

import java.util.*;

public class JmoPlugin extends JavaPlugin implements Listener {

    private final Set<UUID> herobrinePlayers = new HashSet<>();
    private final Map<UUID, ItemStack> herobrineWand = new HashMap<>();
    private final Map<UUID, BukkitTask> herobrineParticleTasks = new HashMap<>(); // FIX: track tasks so we can cancel them
    private final Map<UUID, Boolean> itemEditorActive = new HashMap<>();
    private final Map<UUID, String> editingFieldMap = new HashMap<>();

    private ItemEditorGUI itemEditor;

    @Override
    public void onEnable() {
        itemEditor = new ItemEditorGUI(this);
        Bukkit.getPluginManager().registerEvents(itemEditor, this);
        // Register listeners
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new MobListener(this), this); // FIX: was never registered
        Bukkit.getPluginManager().registerEvents(new DeathMessageListener(), this); // NEW: world-scoped death messages

        if (getCommand("itemeditor") != null) {
            getCommand("itemeditor").setExecutor(new ItemEditorCommand(itemEditor));
        }

        getLogger().info("JmoPlugin has been enabled!, hiya console!");
    }

    @Override
    public void onDisable() {
        getLogger().info("JmoPlugin disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player p = (Player) sender;

        switch (cmd.getName().toLowerCase()) {
            case "hi":
                if (!checkCommandPermission(p, "jmo.hi", "You do not have permission to use /hi.")) return true;
                p.sendMessage("hi " + p.getName() + "!");
                return true;
            case "jmotest":
                if (!checkCommandPermission(p, "jmo.jmotest", "You do not have permission to use /jmotest.")) return true;
                p.sendMessage(ChatColor.GREEN + "The Plugin Is Working Correctly");
                return true;
            case "holycow":
                if (!checkCommandPermission(p, "jmo.holycow", "You do not have permission to use /holycow.")) return true;
                spawnHolyCow(p);
                return true;
            case "evilchicken":
                if (!checkCommandPermission(p, "jmo.evilchicken", "You do not have permission to use /evilchicken.")) return true;
                spawnEvilChicken(p);
                return true;
            case "customsword":
                if (!checkCommandPermission(p, "jmo.customsword", "You do not have permission to use /customsword.")) return true;
                return handleCustomSword(p, args);
            case "herobrine":
                if (!checkCommandPermission(p, "jmo.herobrine", "You do not have permission to use /herobrine.")) return true;
                applyHerobrine(p);
                return true;
            case "unherobrine":
                if (!checkCommandPermission(p, "jmo.unherobrine", "You do not have permission to use /unherobrine.")) return true;
                removeHerobrine(p);
                return true;
            default:
                return false;
        }
    }

    // ─── Custom Swords ───────────────────────────────────────────────────────────

    private boolean handleCustomSword(Player p, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            p.sendMessage(ChatColor.GOLD + "Custom Swords:");
            String[] swords = {"holyblade", "unholycleaver", "voidsaber", "flamebrand", "frostfang",
                    "thunderedge", "lifedrinker", "plagueblade", "shadowknife", "earthsplitter",
                    "windcutter", "bloodreaver", "starfall", "doomblade", "kingslayer", "meow"};
            for (String s : swords) p.sendMessage(" - " + s);
            return true;
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                p.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
            ItemStack sword = createCustomSword(args[2]);
            if (sword == null) {
                p.sendMessage(ChatColor.RED + "Unknown sword.");
                return true;
            }
            target.getInventory().addItem(sword);
            p.sendMessage(ChatColor.GREEN + "Gave " + args[2] + " to " + target.getName());
            return true;
        } else {
            p.sendMessage(ChatColor.RED + "Usage:");
            p.sendMessage("/customsword list");
            p.sendMessage("/customsword give <player> <sword>");
            return true;
        }
    }

    private boolean checkCommandPermission(CommandSender sender, String permission, String message) {
        if (sender.hasPermission(permission)) return true;
        sender.sendMessage(ChatColor.RED + message);
        return false;
    }

    private ItemStack createCustomSword(String id) {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = sword.getItemMeta();
        meta.addEnchant(Enchantment.SHARPNESS, 5, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        switch (id.toLowerCase()) {
            case "holyblade":
                meta.setDisplayName("§f§lHoly Blade");
                meta.setLore(Arrays.asList("§eForged in sacred light", "§7Heals the wielder on hit"));
                break;
            case "unholycleaver":
                meta.setDisplayName("§c§lUnholy Cleaver");
                meta.setLore(Arrays.asList("§eSoaked in forbidden blood", "§7Weakens enemies with each strike"));
                break;
            case "voidsaber":
                meta.setDisplayName("§5§lVoid Saber");
                meta.setLore(Arrays.asList("§dCarved from fractured reality", "§7Teleports enemies randomly"));
                break;
            case "flamebrand":
                meta.setDisplayName("§6§lFlamebrand");
                meta.setLore(Arrays.asList("§cA blade that never cools", "§7Ignites foes"));
                break;
            case "frostfang":
                meta.setDisplayName("§b§lFrostfang");
                meta.setLore(Arrays.asList("§7Forged beneath eternal ice", "§7Slows enemies"));
                break;
            case "thunderedge":
                meta.setDisplayName("§e§lThunder Edge");
                meta.setLore(Arrays.asList("§7Blessed by storm gods", "§7Lightning strikes struck enemies"));
                break;
            case "lifedrinker":
                meta.setDisplayName("§d§lLife Drinker");
                meta.setLore(Arrays.asList("§7Feeds on enemies' vitality", "§7Heals wielder"));
                break;
            case "plagueblade":
                meta.setDisplayName("§2§lPlague Blade");
                meta.setLore(Arrays.asList("§7Born in rot and decay", "§7Poisons foes"));
                break;
            case "shadowknife":
                meta.setDisplayName("§8§lShadow Knife");
                meta.setLore(Arrays.asList("§7Forged where light dies", "§7Blinds victims"));
                break;
            case "earthsplitter":
                meta.setDisplayName("§a§lEarth Splitter");
                meta.setLore(Arrays.asList("§7Heavy as the world", "§7Knocks enemies back"));
                break;
            case "windcutter":
                meta.setDisplayName("§f§lWind Cutter");
                meta.setLore(Arrays.asList("§7Light as air", "§7Launches foes skyward"));
                break;
            case "bloodreaver":
                meta.setDisplayName("§4§lBlood Reaver");
                meta.setLore(Arrays.asList("§7Stronger against weak foes", "§7Finish the wounded"));
                break;
            case "starfall":
                meta.setDisplayName("§9§lStarfall");
                meta.setLore(Arrays.asList("§7Forged beneath falling stars", "§7Chance to call lightning"));
                break;
            case "doomblade":
                meta.setDisplayName("§0§lDoom Blade");
                meta.setLore(Arrays.asList("§7Relic of the end times", "§7Applies multiple debuffs"));
                break;
            case "kingslayer":
                meta.setDisplayName("§6§lKingslayer");
                meta.setLore(Arrays.asList("§7Once ended dynasties", "§7Extra damage to players"));
                break;
            case "meow":
                meta.setDisplayName("§d§lMeow");
                meta.setLore(Arrays.asList("§7...", "§7meow"));
                break;
            default:
                return null;
        }

        sword.setItemMeta(meta);
        return sword;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;

        Player p = (Player) e.getDamager();
        LivingEntity v = (LivingEntity) e.getEntity();
        ItemStack i = p.getInventory().getItemInMainHand();
        if (!i.hasItemMeta()) return;

        String n = i.getItemMeta().getDisplayName();

        switch (n) {
            case "§f§lHoly Blade":
                p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + 2.0));
                break;
            case "§c§lUnholy Cleaver":
                v.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 120, 1));
                v.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 120, 1));
                break;
            case "§5§lVoid Saber":
                if (Math.random() < 0.25)
                    v.teleport(v.getLocation().add((Math.random() - 0.5) * 8, 0, (Math.random() - 0.5) * 8));
                break;
            case "§6§lFlamebrand":
                v.setFireTicks(120);
                break;
            case "§b§lFrostfang":
                v.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 2));
                break;
            case "§e§lThunder Edge":
                v.getWorld().strikeLightningEffect(v.getLocation());
                break;
            case "§d§lLife Drinker":
                p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + 1.0));
                break;
            case "§2§lPlague Blade":
                v.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 120, 1));
                break;
            case "§8§lShadow Knife":
                v.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
                break;
            case "§a§lEarth Splitter":
                v.setVelocity(v.getVelocity().multiply(1.4));
                break;
            case "§f§lWind Cutter":
                v.setVelocity(new Vector(0, 1.2, 0));
                break;
            case "§4§lBlood Reaver":
                if (v.getHealth() < 8.0) e.setDamage(e.getDamage() + 4.0);
                break;
            case "§9§lStarfall":
                if (Math.random() < 0.3) v.getWorld().strikeLightningEffect(v.getLocation());
                break;
            case "§0§lDoom Blade":
                v.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 0));
                v.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 0));
                v.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0));
                break;
            case "§6§lKingslayer":
                e.setDamage(e.getDamage() + 3.0);
                break;
            case "§d§lMeow":
                // instant kill everything in the world because why not
                for (Entity ent : v.getWorld().getEntities()) {
                    if (ent instanceof LivingEntity && !ent.equals(p)) {
                        ((LivingEntity) ent).setHealth(0);
                    }
                }
                break;
        }
    }

    // ─── Holy / Unholy Cow ───────────────────────────────────────────────────────

    private void spawnHolyCow(Player p) {
        Cow cow = p.getWorld().spawn(p.getLocation(), Cow.class);
        boolean unholy = Math.random() < 0.35;
        // FIX: use distinct names that don't overlap so contains() check works correctly
        cow.setCustomName(unholy ? "§c§lUNHOLY COW" : "§f§lHOLY COW ONLY");
        cow.setCustomNameVisible(true);
        cow.setGravity(false);
        cow.setVelocity(new Vector(0, 0.1, 0));
        cow.getWorld().spawnParticle(Particle.CLOUD, cow.getLocation(), 30);
    }

    @EventHandler
    public void onCowDeath(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof Cow)) return;
        Cow cow = (Cow) e.getEntity();
        if (cow.getCustomName() == null) return;

        ItemStack beef = new ItemStack(Material.COOKED_BEEF);
        ItemMeta meta = beef.getItemMeta();

        // FIX: check UNHOLY first (specific), then HOLY (general) — avoids overlap
        if (cow.getCustomName().contains("UNHOLY")) {
            meta.setDisplayName("§c§lUnholy Beef");
            meta.setLore(Arrays.asList("§7It pulses with dark energy"));
        } else if (cow.getCustomName().contains("HOLY")) {
            meta.setDisplayName("§f§lHoly Beef");
            meta.setLore(Arrays.asList("§7Blessed by the divine"));
        } else {
            return;
        }

        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        beef.setItemMeta(meta);
        e.getDrops().clear();
        e.getDrops().add(beef);
    }

    @EventHandler
    public void onEat(PlayerItemConsumeEvent e) {
        if (!e.getItem().hasItemMeta()) return;
        String n = e.getItem().getItemMeta().getDisplayName();
        Player p = e.getPlayer();

        if (n.equals("§f§lHoly Beef")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 1200, 2));
            p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 1200, 2));
        } else if (n.equals("§c§lUnholy Beef")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 600, 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0));
        }
    }

    // ─── Evil Chicken ────────────────────────────────────────────────────────────

    private void spawnEvilChicken(Player p) {
        Chicken c = p.getWorld().spawn(p.getLocation(), Chicken.class);
        c.setCustomName("§4§lEVIL CHICKEN");
        c.setCustomNameVisible(true);
        if (c.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null)
            c.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40.0);
        c.setHealth(40.0);
        if (c.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null)
            c.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.35);
        c.setTarget(p);
        c.getWorld().spawnParticle(Particle.CLOUD, c.getLocation(), 30);
    }

    // ─── Herobrine ───────────────────────────────────────────────────────────────

    private void applyHerobrine(Player p) {
        if (herobrinePlayers.contains(p.getUniqueId())) {
            p.sendMessage(ChatColor.RED + "You are already Herobrine!");
            return;
        }

        herobrinePlayers.add(p.getUniqueId());
        p.sendMessage(ChatColor.DARK_RED + "You have become Herobrine!");
        p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false));

        // FIX: store the task so we can cancel it on /unherobrine
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (herobrinePlayers.contains(p.getUniqueId())) {
                Location loc = p.getLocation();
                p.getWorld().spawnParticle(Particle.CLOUD, loc, 10, 0.5, 1.0, 0.5, 0.01);
                p.getWorld().spawnParticle(Particle.FLAME, loc, 10, 0.5, 1.0, 0.5, 0.01);
                p.getWorld().spawnParticle(Particle.PORTAL, loc, 5, 0.5, 1.0, 0.5, 0.01);
            }
        }, 0L, 5L);
        herobrineParticleTasks.put(p.getUniqueId(), task);

        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName("§c§lHerobrine's Wrath");
        meta.setLore(Arrays.asList("§7Right-click to strike lightning where you look", "§7Unleash Herobrine's wrath"));
        wand.setItemMeta(meta);
        p.getInventory().addItem(wand);
        herobrineWand.put(p.getUniqueId(), wand);
    }

    private void removeHerobrine(Player p) {
        if (!herobrinePlayers.contains(p.getUniqueId())) {
            p.sendMessage(ChatColor.RED + "You are not Herobrine!");
            return;
        }

        p.removePotionEffect(PotionEffectType.NIGHT_VISION);
        p.removePotionEffect(PotionEffectType.GLOWING);
        p.getInventory().remove(herobrineWand.get(p.getUniqueId()));
        herobrinePlayers.remove(p.getUniqueId());

        // FIX: actually cancel the particle task
        BukkitTask task = herobrineParticleTasks.remove(p.getUniqueId());
        if (task != null) task.cancel();

        herobrineWand.remove(p.getUniqueId());
        p.sendMessage(ChatColor.GRAY + "You are no longer Herobrine.");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!herobrinePlayers.contains(p.getUniqueId())) return;
        ItemStack i = e.getItem();
        if (i == null || !i.hasItemMeta()) return;
        if (!i.getItemMeta().getDisplayName().equals("§c§lHerobrine's Wrath")) return;

        Block target = p.getTargetBlockExact(50);
        if (target != null) target.getWorld().strikeLightning(target.getLocation());
    }

    @EventHandler
    public void onTarget(EntityTargetEvent e) {
        if (!(e.getEntity() instanceof Monster)) return;
        if (!(e.getTarget() instanceof Player)) return;
        if (herobrinePlayers.contains(((Player) e.getTarget()).getUniqueId()))
            e.setCancelled(true);
    }

    // ─── Old chat-based item editor (kept for backward compat, /itemeditor now uses ItemEditorCommand) ───

    @EventHandler
    public void onChatEdit(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!itemEditorActive.containsKey(p.getUniqueId())) return;

        e.setCancelled(true);
        String input = e.getMessage();

        if (!editingFieldMap.containsKey(p.getUniqueId())) {
            editingFieldMap.put(p.getUniqueId(), input.toLowerCase());
            p.sendMessage(ChatColor.GREEN + "Editing " + input + ". Type the value now!");
            return;
        }

        String field = editingFieldMap.get(p.getUniqueId());
        ItemStack item = p.getInventory().getItemInMainHand();

        if (item == null || item.getType() == Material.AIR) {
            p.sendMessage(ChatColor.RED + "Hold an item to edit!");
            itemEditorActive.remove(p.getUniqueId());
            editingFieldMap.remove(p.getUniqueId());
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) meta = Bukkit.getItemFactory().getItemMeta(item.getType());

        switch (field) {
            case "displayname":
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', input));
                p.sendMessage(ChatColor.GREEN + "Display name updated!");
                break;
            case "lore":
                List<String> lore = new ArrayList<>();
                for (String line : input.split("\\|"))
                    lore.add(ChatColor.translateAlternateColorCodes('&', line));
                meta.setLore(lore);
                p.sendMessage(ChatColor.GREEN + "Lore updated!");
                break;
            case "enchant":
                for (String eLine : input.split("\\|")) {
                    String[] parts = eLine.split(" ");
                    if (parts.length == 2) {
                        Enchantment ench = Enchantment.getByName(parts[0].toUpperCase());
                        try {
                            int level = Integer.parseInt(parts[1]);
                            if (ench != null) meta.addEnchant(ench, level, true);
                        } catch (Exception ignored) {}
                    }
                }
                p.sendMessage(ChatColor.GREEN + "Enchantments updated!");
                break;
            case "flags":
                for (String f : input.split("\\|")) {
                    try {
                        ItemFlag flag = ItemFlag.valueOf(f.toUpperCase());
                        if (meta.hasItemFlag(flag)) meta.removeItemFlags(flag);
                        else meta.addItemFlags(flag);
                    } catch (Exception ignored) {}
                }
                p.sendMessage(ChatColor.GREEN + "Item flags toggled!");
                break;
            case "nbt":
                for (String tag : input.split("\\|")) {
                    String[] kv = tag.split("=", 2);
                    if (kv.length == 2)
                        meta.getPersistentDataContainer().set(new NamespacedKey(this, kv[0]), PersistentDataType.STRING, kv[1]);
                }
                p.sendMessage(ChatColor.GREEN + "NBT / Data Tags updated!");
                break;
        }

        item.setItemMeta(meta);
        itemEditorActive.remove(p.getUniqueId());
        editingFieldMap.remove(p.getUniqueId());
    }
}