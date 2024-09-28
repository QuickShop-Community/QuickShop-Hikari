package com.ghostchu.quickshop.addon.reremakemigrator.migratecomponent;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.QuickShopBukkit;
import com.ghostchu.quickshop.addon.reremakemigrator.Main;
import com.ghostchu.quickshop.api.localization.text.Text;
import org.bukkit.command.CommandSender;

public abstract class AbstractMigrateComponent implements MigrateComponent {

  private final Main plugin;
  private final QuickShop hikari;
  private final org.maxgamer.quickshop.QuickShop reremake;
  private final CommandSender sender;

  public AbstractMigrateComponent(Main main, QuickShop hikari, org.maxgamer.quickshop.QuickShop reremake, CommandSender sender) {

    this.plugin = main;
    this.hikari = hikari;
    this.reremake = reremake;
    this.sender = sender;
  }

  @Override
  public QuickShop getHikari() {

    return this.hikari;
  }

  @Override
  public QuickShopBukkit getHikariJavaPlugin() {

    return this.hikari.getJavaPlugin();
  }

  @Override
  public org.maxgamer.quickshop.QuickShop getReremake() {

    return this.reremake;
  }

  @Override
  public Main getPlugin() {

    return plugin;
  }

  public CommandSender getSender() {

    return sender;
  }

  public Text text(String modulePath, Object... objects) {

    return getHikari().text().of(sender, "addon.reremake-migrator." + modulePath, objects);
  }
}
