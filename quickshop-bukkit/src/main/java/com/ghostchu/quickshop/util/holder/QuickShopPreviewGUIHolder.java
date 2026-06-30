package com.ghostchu.quickshop.util.holder;

import com.google.common.base.Preconditions;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class QuickShopPreviewGUIHolder implements InventoryHolder {
  private Inventory inventory;

  @Override
  public @NotNull Inventory getInventory() {

    return this.inventory;
  }

  public void setInventory(final @NotNull Inventory inventory) {

    Preconditions.checkState(this.inventory == null);
    this.inventory = inventory;
  }

}
