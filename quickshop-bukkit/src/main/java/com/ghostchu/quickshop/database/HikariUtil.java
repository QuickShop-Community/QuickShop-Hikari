package com.ghostchu.quickshop.database;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.logger.Log;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.configuration.ConfigurationSection;

public class HikariUtil {

  private HikariUtil() {

  }

  public static cc.carm.lib.easysql.hikari.HikariConfig createHikariConfig() {

    final cc.carm.lib.easysql.hikari.HikariConfig config = new cc.carm.lib.easysql.hikari.HikariConfig();
    Section section = QuickShop.getInstance().getConfig().getSection("database");
    if(section == null) {
      throw new IllegalArgumentException("database section in configuration not found");
    }
    section = section.getSection("properties");
    if(section == null) {
      throw new IllegalArgumentException("database.properties section in configuration not found");
    }
    for(final Object keyObj : section.getKeys()) {

      final String key = String.valueOf(keyObj);
      config.addDataSourceProperty(key, String.valueOf(section.get(key)));
    }
    Log.debug("HikariCP Config created with properties: " + config.getDataSourceProperties());
    return config;
  }
}
