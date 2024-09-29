package com.ghostchu.quickshop.addon.reremakemigrator.migratecomponent;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.QuickShopBukkit;
import com.ghostchu.quickshop.addon.reremakemigrator.Main;

public interface MigrateComponent {

  QuickShop getHikari();

  QuickShopBukkit getHikariJavaPlugin();

  org.maxgamer.quickshop.QuickShop getReremake();

  boolean migrate();

  Main getPlugin();
}
