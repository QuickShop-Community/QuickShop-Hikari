package com.ghostchu.quickshop.util.lookup;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.lookup.parser.DirectBukkitParser;
import com.ghostchu.quickshop.util.lookup.parser.ItemLookupParser;
import com.ghostchu.quickshop.util.lookup.parser.ReferenceParser;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.Reloadable;
import com.google.common.collect.MapMaker;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class ItemLookupManager implements Reloadable {
    private final QuickShop plugin;
    private final Map<String, ItemStack> lookupList = new MapMaker().makeMap();
    private List<ItemLookupParser> parsers = new ArrayList<>();
    private final Pattern itemNamePattern = Pattern.compile("\\W"); // [^a-zA-Z0-9_]

    public ItemLookupManager(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        init();
        plugin.getReloadManager().register(this);
    }

    @Nullable
    public ItemStack createItemStack(@NotNull String itemString) {
        for (ItemLookupParser parser : parsers) {
            if (parser.isHandled(itemString)) {
                return parser.create(itemString);
            }
            Predicate
        }
        return null;
    }

    @Nullable
    public ItemStack lookup(@NotNull String name) {
        return lookupList.get(name);
    }

    public void add(@NotNull String name, @NotNull ItemStack stack) throws IllegalNameException, AlreadyExistsException {
        if (!itemNamePattern.matcher(name).matches()) {
            throw new IllegalNameException();
        }
        if (lookupList.containsKey(name)) {
            throw new AlreadyExistsException();
        }
        lookupList.put(name, stack);
        save();
    }

    private void init() {
        this.parsers.clear();
        this.lookupList.clear();
        this.parsers = List.of(new ReferenceParser(this), new DirectBukkitParser(this));
        File file = new File(plugin.getDataFolder(), "itemlookup.yml");
        if (!file.exists()) {
            initDefaultConfiguration(file);
        }
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection itemSection = yamlConfiguration.getConfigurationSection("items");
        if (itemSection == null) {
            itemSection = yamlConfiguration.createSection("items");
        }
        for (String itemName : itemSection.getKeys(false)) {
            if (itemSection.isItemStack(itemName)) {
                ItemStack stack = itemSection.getItemStack(itemName);
                if (stack != null) {
                    this.lookupList.put(itemName, stack);
                    Log.debug("Item " + itemName + " added to lookup list: " + stack.getType().name());
                } else {
                    Log.debug("Item " + itemName + " skip load to lookup list: null");
                }
            }
        }
    }

    private void save() {
        File file = new File(plugin.getDataFolder(), "itemlookup.yml");
        if (!file.exists()) {
            initDefaultConfiguration(file);
        }
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        yamlConfiguration.set("items", null);
        ConfigurationSection itemSection = yamlConfiguration.createSection("items");
        for (Map.Entry<String, ItemStack> map : this.lookupList.entrySet()) {
            itemSection.set(map.getKey(), map.getValue());
        }
        try {
            yamlConfiguration.save(file);
        } catch (Exception e) {
            Log.debug(Level.SEVERE, "Failed to save item lookup file: " + e.getMessage());
        }
    }

    private void initDefaultConfiguration(@NotNull File file) {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        yamlConfiguration.set("version", 1);
        try {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            yamlConfiguration.save(file);
        } catch (Exception e) {
            Log.permission(Level.SEVERE, "Failed to create default item lookup configuration file");
            plugin.getLogger().log(Level.SEVERE, "Failed to create default item lookup configuration", e);
        }
    }


    @Override
    public ReloadResult reloadModule() throws Exception {
        init();
        return Reloadable.super.reloadModule();
    }

    static class AlreadyExistsException extends Exception {
    }

    static class IllegalNameException extends Exception {
    }
}
