package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.display.DisplayType;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This event is called before the shop display item created
 */
public class ShopDisplayItemSpawnEvent extends AbstractQSEvent implements QSCancellable {

  @NotNull
  private final DisplayType displayType;

  @NotNull
  private final ItemStack itemStack;

  @NotNull
  private final Shop shop;

  private boolean cancelled;
  private @Nullable Component cancelReason;

  /**
   * This event is called before the shop display item created
   *
   * @param shop        Target shop
   * @param displayType The displayType
   * @param itemStack   Target ItemStack
   */
  public ShopDisplayItemSpawnEvent(
          @NotNull final Shop shop, @NotNull final ItemStack itemStack, @NotNull final DisplayType displayType) {

    this.shop = shop;
    this.itemStack = itemStack;
    this.displayType = displayType;
  }

  @Override
  public @Nullable Component getCancelReason() {

    return this.cancelReason;
  }

  @Override
  public void setCancelled(final boolean cancel, @Nullable final Component reason) {

    this.cancelled = cancel;
    this.cancelReason = reason;
  }

  /**
   * Gets the current display type
   *
   * @return DisplayType
   */
  public @NotNull DisplayType getDisplayType() {

    return this.displayType;
  }

  /**
   * Gets the ItemStack used for display
   *
   * @return The display ItemStack
   */
  public @NotNull ItemStack getItemStack() {

    return this.itemStack;
  }

  /**
   * Gets the shop
   *
   * @return the shop
   */
  public @NotNull Shop getShop() {

    return this.shop;
  }

  @Override
  public boolean isCancelled() {

    return this.cancelled;
  }
}
