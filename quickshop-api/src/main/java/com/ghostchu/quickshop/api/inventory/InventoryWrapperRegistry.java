package com.ghostchu.quickshop.api.inventory;

import com.google.common.collect.MapMaker;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class InventoryWrapperRegistry {

  private final Map<String, InventoryWrapperManager> registry = new MapMaker().makeMap();

  @Nullable
  public String find(final InventoryWrapperManager manager) {

    for(final Map.Entry<String, InventoryWrapperManager> entry : registry.entrySet()) {
      if(entry.getValue() == manager || entry.getValue().equals(manager)) {
        return entry.getKey();
      }
    }
    return null;
  }

  @Nullable
  public InventoryWrapperManager get(final String pluginName) {

    return registry.get(pluginName);
  }

  public void register(@NotNull final Plugin plugin, @NotNull final InventoryWrapperManager manager) {

    if(registry.containsKey(plugin.getName())) {
      plugin.getLogger().warning("Nag Author: Plugin " + plugin.getName() + " already have a registered InventoryWrapperManager: "
                                 + registry.get(plugin.getName()).getClass().getName() +
                                 " but trying register another new manager: " + manager.getClass().getName() +
                                 ". This may cause unexpected behavior! Replacing with new instance...");
    }
    registry.put(plugin.getName(), manager);
  }

  public void unregister(@NotNull final Plugin plugin) {

    registry.remove(plugin.getName());
  }

}
