package com.ghostchu.quickshop.listener;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import lombok.Getter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public abstract class AbstractProtectionListener extends AbstractQSListener {

  protected AbstractProtectionListener(@NotNull QuickShop plugin) {

    super(plugin);
    plugin.getReloadManager().register(this);
  }

  public QuickShop getPlugin() {

    return plugin;
  }

  /**
   * Get shop for nature events, may will caching but usually it doesn't will cached. Because nature
   * events usually won't check same block twice in shore time.
   *
   * @param location        The block location
   * @param includeAttached whether to include the attached shop
   *
   * @return The shop object
   */
  @Nullable
  public Shop getShopNature(@NotNull Location location, boolean includeAttached) {

    return includeAttached? plugin.getShopManager().getShopIncludeAttached(location) : plugin.getShopManager().getShop(location);
  }

  /**
   * Get shop for player events, won't be caching
   *
   * @param location        The block location
   * @param includeAttached whether to include the attached shop
   *
   * @return The shop object
   */
  @Nullable
  public Shop getShopPlayer(@NotNull Location location, boolean includeAttached) {

    return includeAttached? plugin.getShopManager().getShopIncludeAttached(location) : plugin.getShopManager().getShop(location);
  }

  /**
   * Get shop for redstone events, will caching if caching enabled
   *
   * @param location        The block location
   * @param includeAttached whether to include the attached shop
   *
   * @return The shop object
   */
  @Nullable
  public Shop getShopRedstone(@NotNull Location location, boolean includeAttached) {

    if(includeAttached) {
      return plugin.getShopManager().getShopIncludeAttachedViaCache(location);
    } else {
      return plugin.getShopManager().getShopViaCache(location);
    }

  }

}
