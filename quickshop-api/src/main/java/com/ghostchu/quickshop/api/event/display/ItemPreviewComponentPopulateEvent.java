package com.ghostchu.quickshop.api.event.display;

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemPreviewComponentPopulateEvent extends AbstractQSEvent {

  private final Player player;
  private Component component;

  public ItemPreviewComponentPopulateEvent(@NotNull final Component component, @Nullable final Player p) {

    this.component = component;
    this.player = p;
  }

  public Component getComponent() {

    return component;
  }

  public void setComponent(final Component component) {

    this.component = component;
  }

  @Nullable
  public Player getPlayer() {

    return player;
  }
}
