package com.ghostchu.quickshop.util;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class ComponentJoiner {

  private final Component delimiter;
  private final Component prefix;
  private final Component suffix;
  private Component mainComponent = Component.empty();
  private boolean first = true;

  public ComponentJoiner(@NotNull final Component delimiter, @NotNull final Component prefix, @NotNull final Component suffix) {

    this.delimiter = delimiter;
    this.prefix = prefix;
    this.suffix = suffix;
    mainComponent = mainComponent.append(prefix);
  }

  public ComponentJoiner(@NotNull final Component delimiter) {

    this.delimiter = delimiter;
    this.prefix = Component.empty();
    this.suffix = Component.empty();
    mainComponent = mainComponent.append(prefix);
  }

  public void append(@NotNull final Component component) {

    if(first) {
      first = false;
    } else {
      mainComponent = mainComponent.append(delimiter);
    }
    mainComponent = mainComponent.append(component);
  }

  @NotNull
  public Component toComponent() {

    return mainComponent.append(suffix);
  }
}
