package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.database.bean.DataRecord;
import com.ghostchu.quickshop.api.shop.ModernShop;
import com.ghostchu.quickshop.api.shop.ShopInfoStorage;
import com.ghostchu.quickshop.api.shop.builder.ShopBuilder;
import com.ghostchu.quickshop.api.shop.components.ShopInteraction;
import com.ghostchu.quickshop.api.shop.components.ShopItem;
import com.ghostchu.quickshop.api.shop.components.ShopLifecycle;
import com.ghostchu.quickshop.api.shop.components.ShopMeta;
import com.ghostchu.quickshop.api.shop.components.ShopPermission;
import com.ghostchu.quickshop.api.shop.components.ShopPrice;
import com.ghostchu.quickshop.api.shop.components.ShopTrading;
import com.ghostchu.quickshop.api.shop.service.result.ShopChangeType;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.database.bean.SimpleDataRecord;
import com.ghostchu.quickshop.shop.components.SimpleShopInteraction;
import com.ghostchu.quickshop.api.shop.ShopSignStorage;
import com.ghostchu.quickshop.shop.components.SimpleShopItem;
import com.ghostchu.quickshop.shop.components.SimpleShopLifecycle;
import com.ghostchu.quickshop.shop.components.SimpleShopMeta;
import com.ghostchu.quickshop.shop.components.SimpleShopPermission;
import com.ghostchu.quickshop.shop.components.SimpleShopPrice;
import com.ghostchu.quickshop.shop.components.SimpleShopTrading;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.Reloadable;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings({"deprecation", "removal"})
public class ModernContainerShop implements ModernShop<Double, Location, Player, InventoryPreview>, Reloadable {

  private final UUID runtimeRandomUniqueId = UUID.randomUUID();
  protected final Location location;

  @NotNull
  protected String symbolLink;
  
  
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

  @Override
  public @NotNull UUID getRuntimeRandomUniqueId() {

    return this.runtimeRandomUniqueId;
  }

  @Override
  public ShopItem item() {

    return item;
  }

  @Override
  public ShopInteraction<Player, InventoryPreview> interaction() {

    return interaction;
  }

  @Override
  public ShopLifecycle lifecycle() {

    return lifecycle;
  }

  @Override
  public ShopMeta meta() {

    return meta;
  }

  @Override
  public ShopPermission permission() {

    return permission;
  }

  @Override
  public ShopPrice<Double> price() {

    return price;
  }

  @Override
  public ShopTrading trading() {

    return trading;
  }

  @Override
  public Location getLocation() {

    return location;
  }

  @Override
  public Location bukkitLocation() {

    return location;
  }

  @Override
  public ShopSignStorage asShopSignStorage() {

    return new ShopSignStorage(this.bukkitLocation().getWorld().getName(),
                               this.bukkitLocation().getBlockX(),
                               this.bukkitLocation().getBlockY(),
                               this.bukkitLocation().getBlockZ());
  }

  @Override
  public @NotNull DataRecord asDataRecord() {

    return new SimpleDataRecord(
            meta.getOwner(),
            item.encodedItem(),
            item.encodedItem(),
            meta.getShopName(),
            meta.shopType().id(),
            meta.shopState().identifier(),
            price.getCurrency(),
            price.price(),
            meta.isUnlimited(),
            item.isDisableDisplay(),
            meta.getTaxAccount(),
            JsonUtil.getGson().toJson(permission.getPermissionAudiences()),
            meta.saveExtraToYaml(),
            meta.getInventoryWrapperProvider(),
            asSymbolLink(),
            new Date(),
            meta.getShopBenefit().serialize()
    );
  }

  /**
   * Getting ShopInfoStorage that you can use for storage the shop data
   *
   * @return ShopInfoStorage
   */
  @Override
  public ShopInfoStorage asInfoStorage() {

    return ShopInfoStorage.fromShop(this);
  }

  /**
   * Gets the symbol link that created by InventoryWrapperManager
   *
   * @return InventoryWrapper
   */
  @Override
  public @NotNull String asSymbolLink() {

    return symbolLink;
  }

  /**
   * Compares the current {@code ModernShop} instance with another provided instance and determines
   * the set of differences between them. These differences are represented as a set of
   * {@code ShopChangeType} values, where each type corresponds to a category of change (e.g., item,
   * price, owner, etc.).
   *
   * @param compare The {@code ModernShop} instance to compare against. If {@code null}, the method
   *                assumes comparison with a non-existent or empty shop.
   *
   * @return An {@code EnumSet} of {@code ShopChangeType} values that represent the changes detected
   * between the current shop instance and the provided shop. If no changes are detected, an empty
   * set is returned.
   */
  @Override
  public EnumSet<ShopChangeType> diff(final @Nullable ModernShop<Double, Location, Player, InventoryPreview> compare) {
    
    final EnumSet<ShopChangeType> changes = EnumSet.noneOf(ShopChangeType.class);
    changes.addAll(compare.item().diff((compare == null)? null : compare.item()));
    changes.addAll(compare.meta().diff((compare == null)? null : compare.meta()));
    changes.addAll(compare.permission().diff((compare == null)? null : compare.permission()));
    changes.addAll(compare.price().diff((compare == null)? null : compare.price()));

    if(!Objects.equals(compare.asSymbolLink(), this.asSymbolLink())) {
      changes.add(ShopChangeType.SYMBOL_LINK);
    }

    if(!this.location.equals(compare.getLocation())) {
      changes.add(ShopChangeType.LOCATION);
    }

    if(!Objects.equals(compare.getRuntimeRandomUniqueId(), this.getRuntimeRandomUniqueId())) {
      changes.add(ShopChangeType.RUNTIME_ID);
    }

    return changes;
  }

  @Override
  public ShopBuilder<Double, Location, Player, InventoryPreview> builder() {

    return null;
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
  public boolean equals(final Object o) {

    if(!(o instanceof final ModernContainerShop that)) return false;
    return Objects.equals(location, that.location) && Objects.equals(symbolLink, that.symbolLink)
           && Objects.equals(item, that.item) && Objects.equals(interaction, that.interaction)
           && Objects.equals(lifecycle, that.lifecycle) && Objects.equals(meta, that.meta)
           && Objects.equals(permission, that.permission) && Objects.equals(price, that.price)
           && Objects.equals(trading, that.trading);
  }

  @Override
  public int hashCode() {

    return Objects.hash(location, symbolLink, item, interaction, lifecycle, meta, permission, price, trading);
  }

  @Override
  public String toString() {

    return "ContainerShop{" +
           "location=" + location +
           ", plugin=" + QuickShop.getInstance() +
           ", runtimeRandomUniqueId=" + runtimeRandomUniqueId +
           ", playerGroup=" + permission.getPermissionAudiences() +
           ", isDeleted=" + lifecycle.isDeleted() +
           ", extra=" + meta.getExtra(QuickShop.getInstance().getJavaPlugin()) +
           ", shopId=" + meta.getShopId() +
           ", owner=" + meta.getOwner() +
           ", price=" + price +
           ", shopType=" + meta.shopType() +
           ", shopState=" + meta.shopState() +
           ", unlimited=" + meta.isUnlimited() +
           ", item=" + item.getItem() +
           ", originalItem=" + item.getItem() +
           ", displayItem=" + item.getDisplayItem() +
           ", isLoaded=" + lifecycle.isLoaded() +
           ", createBackup=" + createBackup +
           ", inventoryPreview=" + inventoryPreview +
           ", dirty=" + lifecycle.isDirty() +
           ", updating=" + updating +
           ", currency='" + price.getCurrency() + '\'' +
           ", disableDisplay=" + item.isDisableDisplay() +
           ", taxAccount=" + meta.getTaxAccount() +
           ", inventoryWrapperProvider='" + meta.getInventoryWrapperProvider() + '\'' +
           ", symbolLink='" + symbolLink + '\'' +
           ", shopName='" + meta.getShopName() + '\'' +
           ", benefit=" + meta.getShopBenefit() +
           '}';
  }
}
