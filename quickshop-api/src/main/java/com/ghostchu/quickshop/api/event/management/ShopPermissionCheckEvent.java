package com.ghostchu.quickshop.api.event.management;
/*
 * QuickShop-Hikari
 * Copyright (C) 2025 Daniel "creatorfromhell" Vidmar
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

import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.shop.Shop;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * ShopPermissionEvent
 *
 * @author creatorfromhell
 * @since 6.2.0.9
 */
public class ShopPermissionCheckEvent extends ShopEvent {

  private final UUID playerUUID;
  private final String pluginNamespace;
  private final String permissionNode;
  private boolean hasPermission;

  public ShopPermissionCheckEvent(final @NotNull Shop shop, final @NotNull UUID playerUUID,
                                  final @NotNull String pluginNamespace, final @NotNull String permissionNode,
                                  final boolean hasPermission) {

    super(Phase.MAIN, shop);

    this.playerUUID = playerUUID;
    this.pluginNamespace = pluginNamespace;
    this.permissionNode = permissionNode;
    this.hasPermission = hasPermission;
  }

  public ShopPermissionCheckEvent(final Phase phase, final @NotNull Shop shop, final UUID playerUUID,
                                  final String pluginNamespace, final String permissionNode,
                                  final boolean hasPermission) {

    super(phase, shop);
    this.playerUUID = playerUUID;
    this.pluginNamespace = pluginNamespace;
    this.permissionNode = permissionNode;
    this.hasPermission = hasPermission;
  }

  /**
   * Creates a new instance of PhasedEvent with the specified newPhase.
   *
   * @param newPhase The new Phase for the cloned PhasedEvent
   *
   * @return A new instance of PhasedEvent with the specified newPhase
   */
  @Override
  public ShopPermissionCheckEvent clone(final Phase newPhase) {

    return new ShopPermissionCheckEvent(newPhase, this.shop, this.playerUUID, this.pluginNamespace,
                                        this.permissionNode, this.hasPermission);
  }

  public UUID playerUUID() {

    return playerUUID;
  }

  public String pluginNamespace() {

    return pluginNamespace;
  }

  public String permissionNode() {

    return permissionNode;
  }

  public boolean hasPermission() {

    return hasPermission;
  }

  public void hasPermission(final boolean hasPermission) {

    this.hasPermission = hasPermission;
  }
}
