package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.ModernShop;
import com.ghostchu.quickshop.api.shop.ShopWorldAdapter;
import com.ghostchu.quickshop.api.shop.builder.ShopBuilder;
import com.ghostchu.quickshop.api.shop.components.ShopInteraction;
import com.ghostchu.quickshop.api.shop.components.ShopItem;
import com.ghostchu.quickshop.api.shop.components.ShopLifecycle;
import com.ghostchu.quickshop.api.shop.components.ShopMeta;
import com.ghostchu.quickshop.api.shop.components.ShopPermission;
import com.ghostchu.quickshop.api.shop.components.ShopPrice;
import com.ghostchu.quickshop.api.shop.components.ShopTrading;
import com.ghostchu.quickshop.shop.components.SimpleShopInteraction;
import com.ghostchu.quickshop.shop.components.SimpleShopItem;
import com.ghostchu.quickshop.shop.components.SimpleShopLifecycle;
import com.ghostchu.quickshop.shop.components.SimpleShopMeta;
import com.ghostchu.quickshop.shop.components.SimpleShopPermission;
import com.ghostchu.quickshop.shop.components.SimpleShopPrice;
import com.ghostchu.quickshop.shop.components.SimpleShopTrading;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.Reloadable;
import lombok.EqualsAndHashCode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@EqualsAndHashCode
public class ModernContainerShop implements ModernShop<Double, Location, Player, InventoryPreview>, Reloadable {

  protected final Location location;
  protected SimpleShopItem item;
  protected SimpleShopInteraction interaction;
  protected SimpleShopLifecycle lifecycle;
  protected SimpleShopMeta meta;
  protected SimpleShopPermission permission;
  protected SimpleShopPrice price;
  protected SimpleShopTrading trading;

  public ModernContainerShop(final Location location) {

    this.location = location;
  }

  @EqualsAndHashCode.Exclude
  private final UUID runtimeRandomUniqueId = UUID.randomUUID();

  @Override
  public @NotNull UUID getRuntimeRandomUniqueId() {

    return this.runtimeRandomUniqueId;
  }

  @Override
  public ShopItem item() {

    return null;
  }

  @Override
  public ShopInteraction<Player, InventoryPreview> interaction() {

    return null;
  }

  @Override
  public ShopLifecycle lifecycle() {

    return null;
  }

  @Override
  public ShopMeta meta() {

    return null;
  }

  @Override
  public ShopPermission permission() {

    return null;
  }

  @Override
  public ShopPrice<Double> price() {

    return null;
  }

  @Override
  public ShopTrading trading() {

    return null;
  }

  @Override
  public ShopBuilder<Double, Location, Player, InventoryPreview> asBuilder() {

    return null;
  }

  @Override
  public Location getLocation() {

    return location;
  }

  @Override
  public Location bukkitLocation() {

    return location;
  }

  protected ShopSignStorage asShopSignStorage() {

    return new ShopSignStorage(this.bukkitLocation().getWorld().getName(),
                               this.bukkitLocation().getBlockX(),
                               this.bukkitLocation().getBlockY(),
                               this.bukkitLocation().getBlockZ());
  }

  @Override
  public ReloadResult reloadModule() throws Exception {

    if(!QuickShop.getInstance().isAllowStack()) {
      this.item.setAmount(1);
    } else {
      this.item.setAmount(this.originalItem.getAmount());
    }
    return Reloadable.super.reloadModule();
  }

  @Override
  public String toString() {

    return "ContainerShop{" +
           "location=" + location +
           ", plugin=" + QuickShop.getPlugin() +
           ", runtimeRandomUniqueId=" + runtimeRandomUniqueId +
           ", playerGroup=" + playerGroup +
           ", isDeleted=" + isDeleted +
           ", extra=" + extra +
           ", shopId=" + shopId +
           ", owner=" + owner +
           ", price=" + price +
           ", shopType=" + shopType +
           ", unlimited=" + unlimited +
           ", item=" + item +
           ", originalItem=" + originalItem +
           ", displayItem=" + displayItem +
           ", isLoaded=" + isLoaded +
           ", createBackup=" + createBackup +
           ", inventoryPreview=" + inventoryPreview +
           ", dirty=" + dirty +
           ", updating=" + updating +
           ", currency='" + currency + '\'' +
           ", disableDisplay=" + disableDisplay +
           ", taxAccount=" + taxAccount +
           ", inventoryWrapperProvider='" + inventoryWrapperProvider + '\'' +
           ", symbolLink='" + symbolLink + '\'' +
           ", shopName='" + shopName + '\'' +
           ", benefit=" + benefit +
           '}';
  }
}
