package com.ghostchu.quickshop.shop.components;

/*
 * QuickShop-Hikari
 * Copyright (C) 2026 Daniel "creatorfromhell" Vidmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.settings.type.ShopDisplayEvent;
import com.ghostchu.quickshop.api.event.settings.type.ShopItemEvent;
import com.ghostchu.quickshop.api.shop.ModernShop;
import com.ghostchu.quickshop.api.shop.builder.ShopItemBuilder;
import com.ghostchu.quickshop.api.shop.components.ShopItem;
import com.ghostchu.quickshop.api.shop.display.DisplayItem;
import com.ghostchu.quickshop.api.shop.service.result.ShopChangeType;
import com.ghostchu.quickshop.shop.builder.SimpleShopItemBuilder;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.EqualsAndHashCode;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;

/**
 * SimpleShopDisplay
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class SimpleShopItem implements ShopItem {

  @EqualsAndHashCode.Exclude
  private final ModernShop<?, ?, ?, ?> shop;

  @NotNull
  private ItemStack item;
  @NotNull
  private ItemStack originalItem;

  @Nullable
  @EqualsAndHashCode.Exclude
  private AbstractDisplayItem displayItem = null;

  private boolean disableDisplay = false;

  public SimpleShopItem(@NotNull final ModernShop<?, ?, ?, ?> shop) {
    this.shop = shop;
  }

  public SimpleShopItem(@NotNull final ModernShop<?, ?, ?, ?> shop,
                        @NotNull final ItemStack item, final boolean disableDisplay) {

    this.shop = shop;
    this.item = item;
    this.originalItem = item.clone();
    this.disableDisplay = disableDisplay;
  }

  /**
   * Get shop item's ItemStack
   *
   * @return The shop's ItemStack
   */
  @Override
  public @NotNull ItemStack getItem() {

    final ShopItemEvent event = new ShopItemEvent(Phase.RETRIEVE, shop, this.item.clone());

    return event.updated();
  }

  /**
   * @return The enchantments the shop has on its items.
   */
  public @NotNull Map<Enchantment, Integer> getEnchants() {

    final ItemStack item = getItem();

    if(item.hasItemMeta() && item.getItemMeta().hasEnchants()) {
      return Objects.requireNonNull(item.getItemMeta()).getEnchants();
    }
    return Collections.emptyMap();
  }

  /**
   * @return The ItemStack type of this shop
   */
  public @NotNull Material getMaterial() {

    return this.getItem().getType();
  }

  /**
   * Set shop item's ItemStack
   *
   * @param item ItemStack to set
   */
  @Override
  public void setItem(@NotNull final ItemStack item) {

    //Create our shop event with Pre Phase and call
    ShopItemEvent event = new ShopItemEvent(Phase.PRE, shop, this.item, item);
    event.callEvent();

    //Call our Main Phase
    event = event.clone(Phase.MAIN);
    if(event.callCancellableEvent()) {

      Log.debug("A plugin cancelled the item change event.");
      return;
    }


    this.item = event.updated().clone();
    this.originalItem = item.clone();

    //call our Post Phase
    event.clone(Phase.POST).callEvent();
  }

  /**
   * Encodes and retrieves information related to the shop's item.
   *
   * @return a string representation of the encoded item data
   */
  @Override
  public String encodedItem() {

    return QuickShop.getInstance().platform().encodeStack(this.originalItem);
  }

  /**
   * Checks whether the provided {@code ItemStack} matches the current shop item.
   *
   * @param compare The {@code ItemStack} to compare against the shop's item. The parameter can be
   *             {@code null}.
   *
   * @return {@code true} if the provided {@code ItemStack} matches the shop's item, {@code false}
   * otherwise.
   */
  @Override
  public boolean matches(final @Nullable ItemStack compare) {

    if(compare == null) {
      return false;
    }
    final ItemStack givenItem = compare.clone();
    givenItem.setAmount(1);
    final ItemStack shopItem = this.getItem();
    shopItem.setAmount(1);
    return QuickShop.getInstance().getItemMatcher().matches(shopItem, givenItem);
  }

  /**
   * Gets shop status is stacking shop
   *
   * @return The shop stacking status
   */
  @Override
  public boolean isStackingShop() {

    return QuickShop.getInstance().isAllowStack() && this.item.getAmount() > 1;
  }

  /**
   * Getting the item stacking amount of the shop.
   *
   * @return The item stacking amount of the shop.
   */
  @Override
  public int getShopStackingAmount() {

    if(isStackingShop()) {
      return this.item.getAmount();
    }
    return 1;
  }

  /**
   * Getting if this shop has been disabled the display
   *
   * @return Does display has been disabled
   */
  @Override
  public boolean isDisableDisplay() {

    final ShopDisplayEvent event = ShopDisplayEvent.RETRIEVE(shop, this.disableDisplay);
    event.callEvent();

    return event.updated();
  }

  /**
   * Set the display disable state
   *
   * @param disabled Has been disabled
   */
  @Override
  public void setDisableDisplay(final boolean disabled) {

    if(this.disableDisplay == disabled) {
      return;
    }

    this.disableDisplay = disabled;
  }

  /**
   * Get the display item
   *
   * @return The display item
   */
  @Override
  public DisplayItem getDisplayItem() {

    return displayItem;
  }

  /**
   * Determines whether a custom item name should be used.
   *
   * @return true if a custom item name is enabled, false otherwise
   */
  @Override
  public boolean useCustomItemName() {

    if(!QuickShop.getInstance().getConfig().getBoolean("shop.force-use-item-original-name")) {
      return false;
    }

    final ItemMeta itemMeta = this.item.getItemMeta();
    if(itemMeta == null) {
      return false;
    }

    try {
      if(itemMeta.hasItemName()) {
        return true;
      }
    } catch(final NoSuchMethodError ignore) {
      //old version
    }

    try {
      if(itemMeta.hasCustomName()) {
        return true;
      }
    } catch(final NoSuchMethodError ignore) {
      //old version
    }

    return itemMeta.hasDisplayName();
  }

  /**
   * Customizes and returns a Component representing an item name.
   *
   * @return a Component representing the customized item name
   */
  @Override
  public Component customItemName() {

    return Util.getItemStackName(getItem());
  }

  @Override
  public EnumSet<ShopChangeType> diff(final @Nullable ShopItem compare) {

    final EnumSet<ShopChangeType> changes = EnumSet.noneOf(ShopChangeType.class);

    if(compare == null || this.disableDisplay != compare.isDisableDisplay()) {
      changes.add(ShopChangeType.DISPLAY_TOGGLE);
    }

    if(compare == null || !this.item.equals(compare.getItem())) {
      changes.add(ShopChangeType.ITEM);
    }

    if(compare == null || this.getShopStackingAmount() != compare.getShopStackingAmount()) {
      changes.add(ShopChangeType.AMOUNT);
    }

    return changes;
  }

  @Override
  public ShopItemBuilder builder() {

    return new SimpleShopItemBuilder().item(this.item.clone()).disableDisplay(this.disableDisplay);
  }
}