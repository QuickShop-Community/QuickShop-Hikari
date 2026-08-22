package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.api.shop.Info;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopAction;
import com.ghostchu.quickshop.common.util.JsonUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A class contains shop's infomations
 */
public class SimpleInfo implements Info {

  private final Block last;
  private final Location loc;
  private final boolean dirty;
  private final boolean bypass;
  private ShopAction action;
  private ItemStack item;
  private Shop shop;
  private String shopData;

  public SimpleInfo(
          @NotNull final Location loc,
          @NotNull final ShopAction action,
          @Nullable final ItemStack item,
          @Nullable final Block last,
          final boolean bypass) {

    this.loc = loc;
    this.action = action;
    this.last = last;
    this.bypass = bypass;
    if(item != null) {
      this.item = item.clone();
    }
    this.dirty = true;
  }

  public SimpleInfo(
          @NotNull final Location loc,
          @NotNull final ShopAction action,
          @Nullable final ItemStack item,
          @Nullable final Block last,
          @Nullable final Shop shop,
          final boolean bypass) {

    this.loc = loc;
    this.action = action;
    this.last = last;
    this.bypass = bypass;
    if(item != null) {
      this.item = item.clone();
    }
    if(shop != null) {
      this.shop = shop;
      this.shopData = JsonUtil.getGson().toJson(shop.saveToInfoStorage());
      this.dirty = shop.isDirty();
    } else {
      this.dirty = true;
    }
  }

  /**
   * @return ShopAction action, Get shop action.
   */
  @Override
  @NotNull
  public ShopAction getAction() {

    return this.action;
  }

  @Override
  public void setAction(@NotNull final ShopAction action) {

    this.action = action;
  }

  /**
   * @return ItemStack iStack, Get Shop's selling/buying item's ItemStack.
   */
  @Override
  @NotNull
  public ItemStack getItem() {

    return this.item;
  }


  /**
   * @return Location loc, Get shop's location,
   */
  @Override
  @NotNull
  public Location getLocation() {

    return this.loc;
  }

  /**
   * @return Block signBlock, Get block of shop's sign, may return the null.
   */
  @Override
  @Nullable
  public Block getSignBlock() {

    return this.last;
  }

  /**
   * Get shop is or not has changed.
   *
   * @param shop, The need checked with this shop.
   *
   * @return hasChanged
   */
  @Override
  public boolean hasChanged(@NotNull final Shop shop) {

    return !this.shopData.equals(JsonUtil.getGson().toJson(shop.saveToInfoStorage()));
  }

  @Override
  public boolean isBypassed() {

    return bypass;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof SimpleInfo)) return false;
    final SimpleInfo other = (SimpleInfo)o;
    return this.dirty == other.dirty
           && this.bypass == other.bypass
           && Objects.equals(this.last, other.last)
           && Objects.equals(this.loc, other.loc)
           && Objects.equals(this.getAction(), other.getAction())
           && Objects.equals(this.getItem(), other.getItem())
           && Objects.equals(this.shop, other.shop)
           && Objects.equals(this.shopData, other.shopData);
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.dirty, this.bypass, this.last, this.loc, this.getAction(), this.getItem(), this.shop, this.shopData);
  }

  @Override
  public String toString() {

    return "SimpleInfo(last=" + this.last + ", loc=" + this.loc + ", dirty=" + this.dirty + ", bypass=" + this.bypass + ", action=" + this.getAction() + ", item=" + this.getItem() + ", shop=" + this.shop + ", shopData=" + this.shopData + ")";
  }
}
