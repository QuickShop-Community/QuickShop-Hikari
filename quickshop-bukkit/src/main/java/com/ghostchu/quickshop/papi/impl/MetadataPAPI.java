package com.ghostchu.quickshop.papi.impl;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.papi.PAPISubHandler;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MetadataPAPI implements PAPISubHandler {

  private final QuickShop plugin;

  public MetadataPAPI(@NotNull final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public @NotNull String getPrefix() {

    return "metadata";
  }

  @Override
  @Nullable
  public String handle0(@NotNull final OfflinePlayer player, @NotNull final String paramsTrimmed) {

    final String[] args = paramsTrimmed.split("_");
    if(args.length < 1) {
      return null;
    }
    return switch(args[0]) {
      case "fork" -> plugin.getFork();
      case "version" -> plugin.getVersion();
      default -> null;
    };
  }

}
