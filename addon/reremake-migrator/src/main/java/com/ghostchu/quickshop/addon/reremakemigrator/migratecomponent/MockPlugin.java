package com.ghostchu.quickshop.addon.reremakemigrator.migratecomponent;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

public class MockPlugin implements Plugin {

  private final String pluginName;

  public MockPlugin(final String pluginName) {

    this.pluginName = pluginName;
  }

  @NotNull
  @Override
  public File getDataFolder() {

    return null;
  }

  @NotNull
  @Override
  public PluginDescriptionFile getDescription() {

    return new PluginDescriptionFile(pluginName, "0.0.0", "N/A");
  }

  @NotNull
  @Override
  public FileConfiguration getConfig() {

    return null;
  }

  @Nullable
  @Override
  public InputStream getResource(@NotNull final String s) {

    return null;
  }

  @Override
  public void saveConfig() {

  }

  @Override
  public void saveDefaultConfig() {

  }

  @Override
  public void saveResource(@NotNull final String s, final boolean b) {

  }

  @Override
  public void reloadConfig() {

  }

  @NotNull
  @Override
  public PluginLoader getPluginLoader() {

    return null;
  }

  @NotNull
  @Override
  public Server getServer() {

    return null;
  }

  @Override
  public boolean isEnabled() {

    return false;
  }

  @Override
  public void onDisable() {

  }

  @Override
  public void onLoad() {

  }

  @Override
  public void onEnable() {

  }

  @Override
  public boolean isNaggable() {

    return false;
  }

  @Override
  public void setNaggable(final boolean b) {

  }

  @Nullable
  @Override
  public ChunkGenerator getDefaultWorldGenerator(@NotNull final String s, @Nullable final String s1) {

    return null;
  }

  @Nullable
  @Override
  public BiomeProvider getDefaultBiomeProvider(@NotNull final String s, @Nullable final String s1) {

    return null;
  }

  @NotNull
  @Override
  public Logger getLogger() {

    return null;
  }

  @NotNull
  @Override
  public String getName() {

    return pluginName;
  }

  @Override
  public boolean onCommand(@NotNull final CommandSender commandSender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] strings) {

    return false;
  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull final CommandSender commandSender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] strings) {

    return null;
  }
}
