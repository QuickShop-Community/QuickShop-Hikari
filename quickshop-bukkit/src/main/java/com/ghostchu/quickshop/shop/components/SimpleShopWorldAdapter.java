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

import com.ghostchu.quickshop.ServiceInjector;
import com.ghostchu.quickshop.api.localization.text.ProxiedLocale;
import com.ghostchu.quickshop.api.shop.ModernShop;
import com.ghostchu.quickshop.api.shop.components.ShopWorldAdapter;
import com.ghostchu.quickshop.api.shop.display.DisplayType;
import com.ghostchu.quickshop.shop.DisplayProvider;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import com.ghostchu.quickshop.util.Util;
import lombok.EqualsAndHashCode;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Sign;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * SimpleShopWorldAdapter
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class SimpleShopWorldAdapter implements ShopWorldAdapter {

  private final ModernShop<?, ?, ?, ?> shop;

  @EqualsAndHashCode.Exclude
  private final boolean isDeleted = false;

  public SimpleShopWorldAdapter(final ModernShop<?, ?, ?, ?> shop) {
    this.shop = shop;
  }

  /**
   * Whether Shop is valid
   *
   * @return status
   */
  @Override
  public boolean isValid() {

    Util.ensureThread(false);
    if(this.isDeleted) {
      return false;
    }
    return Util.canBeShop(this.shop.bukkitLocation().getBlock());
  }

  /**
   * Check the display location, and teleport, respawn if needs.
   */
  @Override
  public void checkDisplay() {

    Util.ensureThread(false);
    final boolean displayStatus = plugin.isDisplayEnabled() && !isDisableDisplay() && this.isLoaded() && !this.isDeleted();

    if(!displayStatus) {
      if(this.displayItem != null) {
        this.displayItem.remove(false);
      }
      return;
    }
    if(this.displayItem == null) {
      try {
        final DisplayProvider provider = ServiceInjector.getInjectedService(DisplayProvider.class, null);
        if(provider == null && AbstractDisplayItem.getNowUsing() == DisplayType.VIRTUALITEM && plugin.getVirtualDisplayItemManager() == null) {
          plugin.logger().warn("Invalid display provider! " +
                               "No compatible display backend found. " +
                               "This may occur if ProtocolLib or PacketEvents is missing, outdated, or incompatible with your Minecraft version, " +
                               "or if this QuickShop-Hikari build does not yet support the current server version. " +
                               "Shops will function normally, but displays above containers are disabled.");
          return;
        }

        if(provider != null) {
          this.displayItem = provider.provide(this);
        } else {

          if(AbstractDisplayItem.getNowUsing() == DisplayType.VIRTUALITEM) {

            if(plugin.getVirtualDisplayItemManager() != null) {
              this.displayItem = plugin.getVirtualDisplayItemManager().createVirtualDisplayItem(this);
            }
          }
        }

        if(this.displayItem == null) {
          plugin.logger().warn("Invalid display provider! " +
                               "No compatible display backend found. " +
                               "This may occur if ProtocolLib or PacketEvents is missing, outdated, or incompatible with your Minecraft version, " +
                               "or if this QuickShop-Hikari build does not yet support the current server version. " +
                               "Shops will function normally, but displays above containers are disabled.");
          return;
        }
      } catch(final Throwable anyError) {
        plugin.logger().warn("Failed to init the displayItem for shop {}, the display now disabled for this shop. Do you have ProtocolLib or packetevents installed?", this, anyError);
        return;
      }
    }
    if(this.displayItem != null) {
      if(!this.displayItem.isSpawned()) {
        /* Not spawned yet. */
        this.displayItem.spawn();
      } else {
        /* If not spawned, we didn't need check these, only check them when we need. */
        if(this.displayItem.checkDisplayNeedRegen()) {
          this.displayItem.fixDisplayNeedRegen();
        } else {
          /* If display was regened, we didn't need check it moved, performance! */
          if(this.displayItem.checkDisplayIsMoved()) {
            this.displayItem.fixDisplayMoved();
          }
        }
      }
      /* Dupe is always need check, if enabled display */
      this.displayItem.removeDupe();
    }
  }

  /**
   * Claim a sign as shop sign (modern method)
   *
   * @param sign The shop sign
   */
  @Override
  public void claimShopSign(@NotNull final Sign sign) {

  }

  /**
   * Get shop signs, may have multi signs
   *
   * @return Signs for the shop
   */
  @Override
  public @NotNull List<Sign> getSigns() {

    return List.of();
  }

  /**
   * Checks if a Sign is a ShopSign
   *
   * @param sign Target {@link Sign}
   *
   * @return Is shop info sign
   */
  @Override
  public boolean isShopSign(@NotNull final Sign sign) {

    return false;
  }

  /**
   * Generate new sign texts on shop's sign.
   */
  @Override
  public void setSignText() {

  }

  /**
   * Set texts on shop's sign
   *
   * @param paramArrayOfString The texts you want set
   */
  @Override
  public void setSignText(@NotNull final List<Component> paramArrayOfString) {

  }

  @Override
  public void setSignText(@NotNull final ProxiedLocale locale) {

  }
}
