package com.github.quickshopcommunity.dominion;
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

import cn.lunadeer.dominion.api.DominionAPI;
import cn.lunadeer.dominion.api.dtos.CuboidDTO;
import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.MemberDTO;
import cn.lunadeer.dominion.api.dtos.flag.Flags;
import cn.lunadeer.dominion.events.dominion.DominionDeleteEvent;
import cn.lunadeer.dominion.events.member.MemberRemovedEvent;
import cn.lunadeer.dominion.events.member.MemberSetFlagEvent;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.economy.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.event.management.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.management.ShopPermissionCheckEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopChunk;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.Util;
import com.github.quickshopcommunity.dominion.util.ChunkBounds;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.checkerframework.checker.units.qual.C;

import java.util.Map;
import java.util.UUID;

/**
 * Main
 *
 * @author creatorfromhell
 * @since 6.2.0.10
 */
public class Main extends CompatibilityModule {

  private DominionAPI api;

  private boolean enabled = false;

  //event-related variables
  private boolean whitelist;
  private boolean deleteWhenLosePermission;
  private boolean deleteWhenLandDeleted;

  @Override
  public void init() {

    try {
      this.api = DominionAPI.getInstance();


    } catch(final Exception ignore) {

      return;
    }

    this.enabled = DominionAPI.isDominionEnabled();
    whitelist = getConfig().getBoolean("whitelist-mode");
    deleteWhenLosePermission = getConfig().getBoolean("delete-on-lose-permission");
    deleteWhenLosePermission = getConfig().getBoolean("delete-on-lose-permission");
    deleteWhenLandDeleted = getConfig().getBoolean("delete-shops-in-dominion-when-dominion-deleted");
  }

  @EventHandler(ignoreCancelled = true)
  public void onPreCreation(final ShopCreateEvent event) {

    final Location loc = event.location();
    final UUID uuid = event.user().getUniqueId();
    if(!event.phase().cancellable() || loc.getWorld() == null || uuid == null) {

      return;
    }

    final DominionDTO dominion = api.getDominion(loc);
    if(dominion == null) {
      return;
    }

    if(dominion.getOwner().equals(uuid)) {
      return;
    }

    if(dominion.getGuestFlagValue(Flags.PLACE)) {
      return;
    }

    final MemberDTO member = api.getMember(dominion, uuid);
    if(member != null && member.getFlagValue(Flags.PLACE)) {
      return;
    }

    event.setCancelled(true, getApi().getTextManager().of(event.user(), "addon.dominion.creation-denied").forLocale());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onDominionMember(final MemberRemovedEvent event) {

    if(!deleteWhenLosePermission) {
      return;
    }
    deleteShopInDominion(event.getDominion(), event.getMember().getPlayerUUID());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onDominionMemberFlag(final MemberSetFlagEvent event) {

    if(!deleteWhenLosePermission) {
      return;
    }

    if(event.getFlag().equals(Flags.PLACE) && !event.getNewValue()) {

      deleteShopInDominion(event.getDominion(), event.getMember().getPlayerUUID());
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onLandsDeleted(final DominionDeleteEvent event) {

    if(!deleteWhenLandDeleted) {
      return;
    }
    deleteShopInDominion(event.getDominion(), event.getDominion().getOwner());
  }

  @EventHandler(ignoreCancelled = true)
  public void onTrading(final ShopPurchaseEvent event) {

    final Location loc = event.getShop().getLocation();
    if(loc == null) {
      return;
    }

    final DominionDTO dominion = api.getDominion(loc);
    if(dominion == null && whitelist) {
      event.setCancelled(true, getApi().getTextManager().of(event.getPurchaser(), "addon.dominion.trade-denied").forLocale());
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void permissionOverride(final ShopPermissionCheckEvent event) {

    if(event.shop().isEmpty()) {
      return;
    }

    final Location loc = event.shop().get().getLocation();
    if(loc.getWorld() == null) {
      return;
    }

    final DominionDTO dominion = api.getDominion(loc);
    if(dominion == null) {
      return;
    }

    if(!dominion.getOwner().equals(event.playerUUID())) {
      return;
    }

    if(event.pluginNamespace().equals(QuickShop.getInstance().getJavaPlugin().getName())
       && event.permissionNode().equals(BuiltInShopPermission.DELETE.getRawNode())) {

      event.hasPermission(true);
    }
  }

  private void deleteShopInDominion(final DominionDTO dominion, final UUID target) {
    //Getting all shop with world-chunk-shop mapping
    for(final Map.Entry<String, Map<ShopChunk, Map<Location, Shop>>> entry : getApi().getShopManager().getShops().entrySet()) {

      //Matching world
      final World world = getServer().getWorld(entry.getKey());
      if(world != null) {

        //Matching chunk
        for(final Map.Entry<ShopChunk, Map<Location, Shop>> chunkedShopEntry : entry.getValue().entrySet()) {

          final ShopChunk shopChunk = chunkedShopEntry.getKey();

          final ChunkBounds bounds = new ChunkBounds(Bukkit.getWorld(shopChunk.getWorld()), shopChunk.getX(), shopChunk.getZ());
          if(dominion.getCuboid().intersectWith(new CuboidDTO(bounds.min(), bounds.max()))) {

            //Matching Owner and delete it
            final Map<Location, Shop> shops = chunkedShopEntry.getValue();
            for(final Shop shop : shops.values()) {

              final UUID owner = shop.getOwner().getUniqueIdIfRealPlayer().orElse(null);
              if(owner == null) {

                continue;
              }

              if(target.equals(owner)) {

                recordDeletion(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "Dominion", false), shop, "Dominion: shop deleted because owner lost permission");
                Util.mainThreadRun(()->getApi().getShopManager().deleteShop(shop));
              }
            }
          }
        }
      }
    }
  }
}