package com.ghostchu.quickshop.compatibility.craftengine;

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

import com.ghostchu.quickshop.api.event.QSConfigurationReloadEvent;
import com.ghostchu.quickshop.api.event.packet.handler.PacketHandlerAddedEvent;
import com.ghostchu.quickshop.api.shop.display.PacketHandler;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.shop.display.virtual.VirtualDisplayItemManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

/**
 * Main
 *
 * <p>Makes QuickShop's virtual shop display items use the CraftEngine client bound item, so shops
 * which sell CraftEngine items display the matching custom item model, instead of the plain
 * underlying item or a missing model.</p>
 *
 * <p>QuickShop builds the display item packet by itself, so CraftEngine never sees it and can not
 * apply its per-player, version dependent item conversion. This module registers an own
 * {@link com.ghostchu.quickshop.api.shop.display.PacketHandler} into QuickShop's display manager,
 * which wraps the packet handler QuickShop picked (PacketEvents or ProtocolLib) and only replaces
 * the display item metadata packet, so the conversion runs while the packet is sent to each
 * player.</p>
 *
 * @author YuanYuanOwO
 * @since 6.3.0.3
 */
public final class Main extends CompatibilityModule {

  private CraftEngineItemConverter converter;
  private CraftEnginePacketHandler packetHandler;
  private VirtualDisplayItemManager installedManager;
  private boolean selfInstalling;

  @Override
  public void init() {

    if(this.converter == null) {

      this.converter = new CraftEngineItemConverter(getLogger());
    }
    install();
  }

  /**
   * Attaches this module to the current display manager, wrapping the packet handler QuickShop uses.
   *
   * <p>Safe to call multiple times, an already active installation is detected and left untouched.</p>
   */
  private void install() {

    final VirtualDisplayItemManager manager = VirtualDisplayItemManager.instance();
    if(manager == null || manager.packetHandler() == null) {

      getLogger().info("QuickShop is not using virtual display items (shop.display-type must be 2), "
                               + "there is no display item to convert.");
      return;
    }
    //Never wrap our own handler, always wrap the packet handler QuickShop itself uses
    final PacketHandler<?> current = manager.packetHandler();
    final PacketHandler<?> wrapped = (current instanceof final CraftEnginePacketHandler adapter)
            ? adapter.delegate() : current;
    if(this.packetHandler == null || this.packetHandler.delegate() != wrapped) {
      //Also covers switching the display protocol (PacketEvents <-> ProtocolLib) between two reloads
      this.packetHandler = new CraftEnginePacketHandler(getLogger(), this.converter, wrapped);
      this.installedManager = null;
    }
    if(manager == this.installedManager && manager.packetHandler() == this.packetHandler) {
      //Already active
      return;
    }

    this.selfInstalling = true;
    try {

      //Unload the currently used packet factory, so that its chunk listeners are not registered twice
      manager.unload();
      //Replaces the entry of the wrapped handler, this adapter reuses its identifier
      manager.addHandler(this.packetHandler);
      manager.setHandler();
      this.packetHandler.initialize();
      manager.load();
    } finally {

      this.selfInstalling = false;
    }

    if(manager.packetHandler() == this.packetHandler) {

      this.installedManager = manager;
      getLogger().info("Shop displays are now converted with the CraftEngine client bound item.");
    } else {

      //Another plugin cancelled the registration of our handler
      this.installedManager = null;
      getLogger().warning("Could not take over the display packet handler, shop displays are not converted.");
    }
  }

  /**
   * QuickShop rebuilds its virtual display manager while reloading the configuration, which registers
   * its own packet handlers again. This module has to be registered into the new manager as well, the
   * manager selects, initializes and loads its handlers right after all of them were added.
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPacketHandlerAdded(final PacketHandlerAddedEvent event) {

    if(this.packetHandler == null || this.selfInstalling) return;
    if(event.packetHandler() == this.packetHandler) return;
    final VirtualDisplayItemManager manager = VirtualDisplayItemManager.instance();
    if(manager == null || manager == this.installedManager) return;

    this.installedManager = manager;
    manager.addHandler(this.packetHandler);
    getLogger().info("Re-attached to the rebuilt QuickShop virtual display manager.");
  }

  /**
   * Re-checks the installation after the configuration was reloaded, this also re-wraps the packet
   * handler QuickShop uses in case the display protocol was switched.
   */
  @Override
  public void onQuickShopReload(final QSConfigurationReloadEvent event) {

    super.onQuickShopReload(event);
  }

  @Override
  public void onDisable() {
    super.onDisable();
  }
}
