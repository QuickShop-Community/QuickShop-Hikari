package com.ghostchu.quickshop.api.event;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemPreviewComponentPopulateEvent extends AbstractQSEvent {

  private final Player player;
  private Component component;

  public ItemPreviewComponentPopulateEvent(@NotNull Component component, @Nullable Player p) {

    this.component = component;
    this.player = p;
  }

  public Component getComponent() {

    return component;
  }

  public void setComponent(Component component) {

    this.component = component;
  }

  @Nullable
  public Player getPlayer() {

    return player;
  }
}
