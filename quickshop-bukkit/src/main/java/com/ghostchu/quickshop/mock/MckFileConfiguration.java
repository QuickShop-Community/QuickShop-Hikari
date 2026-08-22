package com.ghostchu.quickshop.mock;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class MckFileConfiguration extends FileConfiguration {

  @NotNull
  @Override
  public String saveToString() {

    return "";
  }

  @Override
  public void loadFromString(@NotNull final String s) {

  }

  @NotNull
  @Override
  protected String buildHeader() {

    return "";
  }

  @Override
  public String toString() {

    return "MckFileConfiguration()";
  }
}
