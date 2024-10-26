package com.ghostchu.quickshop.api.event.display;

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemPreviewComponentPrePopulateEvent extends AbstractQSEvent {

  private final Player player;
  private ItemStack itemStack;

  public ItemPreviewComponentPrePopulateEvent(@NotNull final ItemStack itemStack, @Nullable final Player player) {

    this.itemStack = itemStack;
    this.player = player;
  }

  public ItemStack getItemStack() {

    return itemStack;
  }

  public void setItemStack(final ItemStack itemStack) {

    this.itemStack = itemStack;
  }

  @Nullable
  public Player getPlayer() {

    return player;
  }
}
