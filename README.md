# SettingsLibrary

[![](https://jitpack.io/v/MikeyDevelopmentz/SettingsLibrary.svg)](https://jitpack.io/#MikeyDevelopmentz/SettingsLibrary)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**A powerful, centralized settings UI library for Minecraft Spigot/Paper plugins**

SettingsLibrary provides a unified, beautiful GUI where players can access and configure all their plugins in one place. Instead of each plugin having its own `/config` or settings command, everything is accessible through a single `/settings` menu.

---

## ✨ Features

- **🎨 Unified Interface**: One centralized menu for all plugin settings
- **📦 Easy Integration**: Simple API for plugin developers
- **🎭 Multiple Templates**: 6 pre-built GUI layouts (default, minimal, compact, grid, fancy, centered)
- **⚙️ Fully Customizable**: Complete control over GUI appearance, sounds, and animations
- **🔊 Sound Effects**: Configurable sounds for opens, clicks, and interactions
- **🎬 Animations**: Smooth border animations and visual effects
- **📍 Auto-Positioning**: Automatic plugin slot management with manual override support
- **🔌 Hot-Reload**: Reload configuration without restarting the server
- **🎯 Priority System**: Control plugin display order
- **📱 Event-Driven**: Event-based registration for better plugin compatibility

---

## 📦 Installation (Server Owners)

1. Download `SettingsLibrary.jar` from [Releases](https://github.com/MikeyDevelopmentz/SettingsLibrary/releases)
2. Place in your server's `plugins` folder
3. Restart or reload your server
4. Configure via `plugins/SettingsLibrary/config.yml`
5. Install plugins that support SettingsLibrary integration

---

## 🎮 Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/settings` | `settingslibrary.use` | Open the centralized settings menu |
| `/settings` (aliases) | - | `settingsmenu`, `pluginsettings` |
| `/settingslibrary list` | `settingslibrary.admin` | List all integrated plugins |
| `/settingslibrary info <plugin>` | `settingslibrary.admin` | Show detailed plugin information |
| `/settingslibrary reload` | `settingslibrary.reload` | Reload configuration |
| `/settingslibrary refresh` | `settingslibrary.admin` | Refresh all open menus |
| `/settingslibrary debug` | `settingslibrary.admin` | Toggle debug mode |
| `/settingslibrary help` | - | Show help menu |

### Aliases
- `/settingslibrary` → `/setlib`, `/slib`

---

## 🔑 Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `settingslibrary.use` | `true` | Access to `/settings` menu |
| `settingslibrary.admin` | `op` | Access to admin commands |
| `settingslibrary.reload` | `op` | Reload plugin configuration |

---

## ⚙️ Configuration

### GUI Settings

```yaml
gui:
  title: "&8&l✦ &b&lSettings &8&l✦"
  size: 54  # 27, 36, 45, or 54
  
  open-sound:
    enabled: true
    sound: BLOCK_NOTE_BLOCK_PLING
    volume: 0.5
    pitch: 1.2
  
  click-sound:
    enabled: true
    sound: UI_BUTTON_CLICK
    volume: 0.7
    pitch: 1.0
  
  animations:
    enabled: true
    border-interval: 10
```

### Template System

Choose from 6 pre-built templates or create your own:

```yaml
template:
  active: "default"  # default, minimal, compact, grid, fancy, centered
  auto-arrange: true
  auto-arrange-start: 10
```

**Templates Available:**
- **default**: Clean and elegant with borders
- **minimal**: Maximum plugin space, minimal decoration
- **compact**: 27-slot GUI for servers with few plugins
- **grid**: Organized grid with separators
- **fancy**: Decorative with accent highlights
- **centered**: Plugins centered with wide borders

### Decorators

Customize the appearance of GUI elements:

```yaml
decorators:
  border:
    material: BLACK_STAINED_GLASS_PANE
    name: " "
  
  filler:
    material: GRAY_STAINED_GLASS_PANE
    name: " "
  
  separator:
    material: WHITE_STAINED_GLASS_PANE
    name: " "
  
  accent:
    material: LIGHT_BLUE_STAINED_GLASS_PANE
    name: " "
```

### Navigation Items

```yaml
navigation:
  close:
    enabled: true
    slot: 49
    material: BARRIER
    name: "&c&lClose"
    lore:
      - "&7Click to close the menu"
  
  info:
    enabled: true
    slot: 4
    material: BOOK
    name: "&e&lSettings Library"
    lore:
      - "&7A unified settings menu"
```

---

## 👨‍💻 For Plugin Developers

### Maven Setup (JitPack)

Add JitPack repository and dependency to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.MikeyDevelopmentz</groupId>
        <artifactId>SettingsLibrary</artifactId>
        <version>1.0.1</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### Gradle Setup

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.MikeyDevelopmentz:SettingsLibrary:1.0.1'
}
```

---

## 🔌 Integration Guide

### 1. Add Soft Dependency

In your `plugin.yml`:

```yaml
softdepend: [SettingsLibrary]
```

### 2. Create Settings Provider

Implement the `SettingsProvider` interface:

```java
import me.mikey.settingslibrary.api.SettingsProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class MySettingsProvider implements SettingsProvider {
    
    private final MyPlugin plugin;
    
    public MySettingsProvider(MyPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public Plugin getPlugin() {
        return plugin;
    }
    
    @Override
    public String getSettingsId() {
        return "my-plugin";  // Unique identifier
    }
    
    @Override
    public String getDisplayName() {
        return "§b§lMy Plugin";
    }
    
    @Override
    public ItemStack getDisplayItem() {
        ItemStack item = new ItemStack(Material.DIAMOND);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b§lMy Plugin");
        meta.setLore(Arrays.asList(
            "§7Configure my plugin settings",
            "",
            "§e▶ Click to open"
        ));
        item.setItemMeta(meta);
        return item;
    }
    
    @Override
    public void onClick(Player player, ClickType clickType) {
        // Open your custom settings GUI
        player.sendMessage("§aOpening settings...");
        openMySettingsGUI(player);
    }
    
    @Override
    public int getPriority() {
        return 50;  // Higher = displayed first
    }
}
```

### 3. Register Your Provider

**Method A: Direct Registration (Recommended)**

```java
import me.mikey.settingslibrary.api.SettingsLibraryAPI;

public class MyPlugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        // Register with SettingsLibrary if available
        SettingsLibraryAPI.ifAvailable(api -> {
            api.registerProvider(new MySettingsProvider(this));
            getLogger().info("Registered with SettingsLibrary!");
        });
    }
}
```

**Method B: Event-Based Registration**

```java
import me.mikey.settingslibrary.api.event.SettingsLibraryReadyEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MyPlugin extends JavaPlugin implements Listener {
    
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }
    
    @EventHandler
    public void onSettingsLibraryReady(SettingsLibraryReadyEvent event) {
        event.getAPI().registerProvider(new MySettingsProvider(this));
    }
}
```

---

## 📚 API Reference

### SettingsLibraryAPI

```java
// Get API instance
SettingsLibraryAPI api = SettingsLibraryAPI.get();

// Safe access with null check
SettingsLibraryAPI.ifAvailable(api -> {
    // Use API here
});

// Register a settings provider
api.registerProvider(provider);

// Unregister a provider
api.unregisterProvider(provider);

// Open settings menu for a player
api.openSettingsMenu(player);

// Get all registered providers
Collection<SettingsProvider> providers = api.getProviders();

// Refresh all open menus
api.refreshMenus();
```

### SettingsProvider Interface

```java
public interface SettingsProvider {
    Plugin getPlugin();              // Your plugin instance
    String getSettingsId();          // Unique identifier
    String getDisplayName();         // Display name (optional)
    ItemStack getDisplayItem();      // Item shown in menu
    void onClick(Player, ClickType); // Handle click events
    int getPriority();              // Display order (optional, default 0)
}
```

### Events

**SettingsLibraryReadyEvent**
- Fired when SettingsLibrary is ready to accept registrations
- Access API via `event.getAPI()`

**SettingsProviderRegisterEvent**
- Fired when a provider is registered
- Cancellable

---

## 🎨 Template Pattern Keys

When creating custom templates in `templates.yml`:

| Key | Description |
|-----|-------------|
| `B` | Border decorator |
| `F` | Filler decorator |
| `S` | Separator decorator |
| `A` | Accent decorator |
| `P` | Plugin slot (auto-filled) |
| `I` | Info button slot |
| `C` | Close button slot |
| `X` | Empty/unused slot |

**Example Custom Template:**

```yaml
templates:
  custom:
    name: "My Custom Layout"
    description: "Custom description"
    size: 54  # Optional, defaults to 54
    pattern:
      - "B B B B I B B B B"
      - "B P P P P P P P B"
      - "B P P P P P P P B"
      - "B P P P P P P P B"
      - "B P P P P P P P B"
      - "B B B B C B B B B"
```

---

## 🔧 Advanced Features

### Manual Position Override

Store plugin positions in `positions.yml`:

```yaml
positions:
  my-plugin: 20
  another-plugin: 21
```

Positions override auto-arrangement when specified.

### Priority System

Control display order with priorities:
- Higher priority = displayed first
- Default priority: `0`
- Range: `-100` to `100`

```java
@Override
public int getPriority() {
    return 75;  // High priority, shown near the top
}
```

### Dynamic Display Items

Update your display item based on plugin state:

```java
@Override
public ItemStack getDisplayItem() {
    ItemStack item = new ItemStack(Material.BEACON);
    ItemMeta meta = item.getItemMeta();
    
    // Show current plugin state in lore
    boolean enabled = myPlugin.isFeatureEnabled();
    meta.setLore(Arrays.asList(
        "§7Status: " + (enabled ? "§aEnabled" : "§cDisabled"),
        "",
        "§e▶ Click to configure"
    ));
    
    item.setItemMeta(meta);
    return item;
}
```

---

## 📖 Example Implementations

Check out the [Example Plugin](https://github.com/MikeyDevelopmentz/SettingsLibrary-Example) for a complete working implementation demonstrating:

- ✅ Basic integration
- ✅ Custom settings GUI
- ✅ Toggle switches
- ✅ Numeric sliders
- ✅ Configuration persistence
- ✅ Back button navigation
- ✅ Sound effects

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/MikeyDevelopmentz/SettingsLibrary/issues)
- **Example Plugin**: [SettingsLibrary-Example](https://github.com/MikeyDevelopmentz/SettingsLibrary-Example)
- **Documentation**: [Wiki](https://github.com/MikeyDevelopmentz/SettingsLibrary/wiki) *(coming soon)*

---

## 🙏 Credits

**Author**: Mikey  
**Version**: 1.0.1  
**API Version**: 1.21.10

Made with ❤️ for the Minecraft plugin development community
