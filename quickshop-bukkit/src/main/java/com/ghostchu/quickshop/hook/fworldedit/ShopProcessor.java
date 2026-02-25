package com.ghostchu.quickshop.hook.fworldedit;

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

import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * ShopProcessor
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class ShopProcessor implements IBatchProcessor {

  private final World world;

  public ShopProcessor(final World world) {

    this.world = world;
  }

  @Override
  public IChunkSet processSet(final IChunk chunk, final IChunkGet get, final IChunkSet set) {

    final org.bukkit.World bukkitWorld = Bukkit.getWorld(this.world.getName());
    if(bukkitWorld == null) {
      return set;
    }

    final Map<BlockVector3, FaweCompoundTag> tilesFrom = get.tiles();

    if(tilesFrom.isEmpty()) {
      return set;
    }

    for(final Map.Entry<BlockVector3, FaweCompoundTag> entry : tilesFrom.entrySet()) {

      final BlockVector3 pos = entry.getKey();

      final BlockState fromBlock = get.getBlock(pos.x() & 15, pos.y(), pos.z() & 15);
      final BlockState toBlock = set.getBlock(pos.x() & 15, pos.y(), pos.z() & 15);

      if(toBlock.getBlockType() == null || toBlock.getBlockType() == BlockTypes.__RESERVED__) {
        continue;
      }

      final Location location = new Location(bukkitWorld, pos.x(), pos.y(), pos.z());
      if(fromBlock.getBlockType().getMaterial().hasContainer() && !toBlock.getBlockType().getMaterial().hasContainer()) {

        final Shop shop = QuickShop.getInstance().getShopManager().getShop(location, true); // Because WorldEdit can only remove half of shop, so we can keep another half as shop if it is doublechest shop.
        if(shop != null) {
          Util.regionThread(shop.getLocation(), () -> {
            QuickShop.getInstance().logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "WorldEdit", false), "WorldEdit", shop.saveToInfoStorage()));
            QuickShop.getInstance().getShopManager().deleteShop(shop);
          });
        }
      }
    }
    return set;
  }

  @Override
  public @Nullable Extent construct(final Extent child) {

    return null;
  }
}