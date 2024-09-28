package com.ghostchu.quickshop.compatibility.towny;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class TownyMaterialPriceLimiter {

  private final Map<Material, Double> prices = new HashMap<>();
  private final double percentage;

  public TownyMaterialPriceLimiter(final ConfigurationSection section, final double percentage) {

    this.percentage = percentage;
    for(final String key : section.getKeys(false)) {
      final Material mat = Material.matchMaterial(key);
      if(mat == null) {
        JavaPlugin.getPlugin(Main.class).getLogger().warning("Invalid material in config: " + key);
      }
      final double price = section.getDouble(key);
      prices.put(mat, price);
    }
  }

  @Nullable
  public Double getPrice(@NotNull final Material material, final boolean selling) {

    final Double basePrice = prices.get(material);
    if(basePrice == null) {
      return null;
    }
    if(selling) {
      return basePrice + (basePrice * percentage);
    } else {
      return basePrice;
    }
  }

}
