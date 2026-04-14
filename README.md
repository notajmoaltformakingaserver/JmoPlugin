# JmoPlugin<br>
## The Most SIMPLE Plugin Known To Man.<br>
adds some funni lil commands to make minecraft better and more immersive!<br>
***(check releases for more info this is just a light documentation..)*** <br>

> actual documentation - by Jmo_fr with ♥️
# JmoPlugin

A feature-rich Spigot plugin for Minecraft 1.20.4 that adds immersive commands, custom weapons, magical creatures, and an advanced item editor to enhance your server experience.
> NOTEE::::: for some reasonnn it works for 1.21.11 umm idk why but yeah...., pretty sure it works for most versions 1.21+
## ✨ Features

- **Custom Swords** - 15 unique swords with special combat abilities
- **Magical Creatures** - Spawn holy/unholy cows and evil chickens with special effects
- **Herobrine Transformation** - Become a mystical entity with special powers and a lightning wand
- **Advanced Item Editor** - Edit item display names, lore, enchantments, flags, and NBT data via in-game chat
- **Combat Enhancements** - Sword effects including healing, teleportation, status effects, knockback, and more
- **Particle Effects** - Visual effects for creatures and Herobrine transformation

## 📋 Commands

| Command | Usage | Description |
|---------|-------|-------------|
| `/hi` | `/hi` | Receive a personalized greeting |
| `/jmotest` | `/jmotest` | Test if the plugin is working correctly |
| `/holycow` | `/holycow` | Spawn a holy (or unholy) cow with particle effects |
| `/evilchicken` | `/evilchicken` | Spawn a hostile evil chicken with 40 HP |
| `/customsword list` | `/customsword list` | View all available custom swords |
| `/customsword give <player> <sword>` | `/customsword give PlayerName holyblade` | Give a custom sword to a player |
| `/herobrine` | `/herobrine` | Transform into Herobrine with special abilities |
| `/unherobrine` | `/unherobrine` | Remove Herobrine transformation |
| `/itemeditor` | `/itemeditor` | Open the interactive item editor |

## ⚔️ Custom Swords

Each sword has unique combat abilities when hitting players:

### Available Swords:
1. **Holy Blade** - Heals the wielder on hit
2. **Unholy Cleaver** - Applies Wither and Weakness effects
3. **Void Saber** - Randomly teleports enemies
4. **Flamebrand** - Sets enemies on fire
5. **Frostfang** - Slows enemies significantly
6. **Thunder Edge** - Calls lightning on hit
7. **Life Drinker** - Heals the wielder with each strike
8. **Plague Blade** - Poisons enemies
9. **Shadow Knife** - Blinds victims
10. **Earth Splitter** - Multiplies knockback
11. **Wind Cutter** - Launches enemies skyward
12. **Blood Reaver** - Extra damage against low-health enemies
13. **Starfall** - 30% chance to call lightning
14. **Doom Blade** - Applies multiple debuffs (Wither, Slowness, Weakness)
15. **Kingslayer** - Extra damage against other players
16. **Meow** - Instant kills any player/entity within a 30 billion block circle (basically whole world) from 1 strike. <br> 

## 🐮 Magical Creatures

### Holy/Unholy Cow
- **Command**: `/holycow`
- **Effects**: Floats with particle effects
- **Special**: 35% chance to spawn as "Unholy Cow"
- **Drops**: 
  - **Holy Beef** - Grants Regeneration II and Absorption II for 60 seconds
  - **Unholy Beef** - Applies Wither II and Blindness for cursed damage

### Evil Chicken
- **Command**: `/evilchicken`
- **Health**: 40 HP (2x normal)
- **Speed**: Enhanced movement
- **AI**: Aggressive - targets the summoner
- **Visual**: Particle cloud effect on spawn

## 👻 Herobrine Transformation

### Benefits:
- Permanent Night Vision (can see in dark)
- Glowing effect (visible to all players)
- Exclusive **Herobrine's Wrath** wand (Blaze Rod)
- Continuous particle effects (clouds, flame, portal)
- Monsters cannot target you

### Herobrine's Wrath Wand:
- **Right-click** to strike lightning at the block you're looking at (50-block range)
- Devastating area damage

### Commands:
- `/herobrine` - Activate transformation
- `/unherobrine` - Deactivate transformation

## 📝 Item Editor

The **Item Editor** is an advanced in-game tool to customize items held in your main hand.

### How to Use:
1. Hold an item in your main hand
2. Type `/itemeditor`
3. Choose a field to edit: `displayname`, `lore`, `enchant`, `flags`, or `nbt`
4. Type your new value

### Edit Fields:

#### Display Name
- Supports color codes with `&` prefix (e.g., `&c&lRed Bold`)
- **Example**: `&f&lMy Epic Sword`

#### Lore
- Separate lines with `|` (pipe symbol)
- Supports color codes
- **Example**: `&eFirst line|&7Second line|&4Third line`

#### Enchantments
- Format: `EnchantmentName Level` (separated by pipes)
- **Example**: `DAMAGE_ALL 5|KNOCKBACK 2|FIRE_ASPECT 1`
- Works with any vanilla enchantment

#### Item Flags
- Toggle visibility flags (pipe-separated)
- Available flags: `HIDE_ENCHANTS`, `HIDE_ATTRIBUTES`, `HIDE_UNBREAKABLE`, `HIDE_CAN_DESTROY`, `HIDE_CAN_PLACE`, `HIDE_OTHER_STUFF`, `HIDE_DYE`
- **Example**: `HIDE_ENCHANTS|HIDE_ATTRIBUTES`

#### NBT / Persistent Data Tags
- Add custom persistent data
- Format: `key=value|key2=value2`
- **Example**: `custom_id=sword_123|rarity=legendary`

## 📦 Requirements

- **Java**: 8 or higher
- **Minecraft Server**: Spigot 1.20.4
- **Bukkit API**: Compatible with 1.20.4

## 🚀 Installation

1. Download the latest JAR from [Releases](https://github.com/notajmoaltformakingaserver/JmoPlugin/releases)
2. Place the JAR in your server's `plugins/` directory
3. Restart your server
4. All commands are available immediately!

## 🔨 Building from Source

This project uses **Maven** for builds.
> this was deff not made by copilot (trust)





- i figured out that if i just dont enter teh box i cant get stuck in it >:3

```bash
# Clone the repository
git clone https://github.com/notajmoaltformakingaserver/JmoPlugin.git
cd JmoPlugin

# Build the project
mvn clean package

# JAR will be in target/ directory

