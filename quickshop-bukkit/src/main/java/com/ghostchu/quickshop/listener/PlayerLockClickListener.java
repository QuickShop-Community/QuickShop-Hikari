package com.ghostchu.quickshop.listener;
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

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

/**
 * NonLockClickListener
 *
 * @author creatorfromhell
 * @since 6.2.0.9
 */
public class PlayerLockClickListener extends AbstractProtectionListener {

  public PlayerLockClickListener(final @NotNull QuickShop plugin) {

    super(plugin);
  }

  @EventHandler(ignoreCancelled = true)
  public void onClick(final PlayerInteractEvent e) {

    final Block b = e.getClickedBlock();

    if(b == null) {
      return;
    }

    if(!Util.canBeShop(b)) {
      return;
    }

    final Player p = e.getPlayer();

    if(e.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return; // Didn't right click it, we dont care.
    }

    final Shop shop = getShopPlayer(b.getLocation(), true);
    // Make sure they're not using the non-shop half of a double chest.
    if(shop == null) {
      return;
    }

    if(plugin.getConfig().getBoolean("shop.lock") && !shop.playerAuthorize(p.getUniqueId(), BuiltInShopPermission.ACCESS_INVENTORY)) {
      if(plugin.perm().hasPermission(p, "quickshop.other.open")) {
        if(LockListener.lockCoolDown.getIfPresent(p.getUniqueId()) == null) {
          plugin.text().of(p, "bypassing-lock").send();
          LockListener.lockCoolDown.put(p.getUniqueId(), LockListener.EMPTY_OBJECT);
        }
        return;
      }
      plugin.text().of(p, "that-is-locked").send();
      e.setCancelled(true);
    }

    QuickShop.inShop.add(p.getUniqueId());
  }

  /**
   * Callback for reloading
   *
   * @return Reloading success
   */
  @Override
  public ReloadResult reloadModule() {

    register();
    return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
  }
}