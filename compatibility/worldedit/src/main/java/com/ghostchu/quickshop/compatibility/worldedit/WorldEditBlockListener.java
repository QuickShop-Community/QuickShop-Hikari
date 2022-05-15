/*
 *  This file is a part of project QuickShop, the name is WorldEditBlockListener.java
 *  Copyright (C) Ghost_chu and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ghostchu.quickshop.compatibility.worldedit;

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.bukkit.Location;

/**
 * Proxy class to handle WorldEdit actions
 */
public class WorldEditBlockListener extends AbstractDelegateExtent {
    private final Actor actor;
    private final World world;
    private final Extent extent;
    private final QuickShopAPI api;

    // Same Package access
    WorldEditBlockListener(Actor actor, World world, Extent originalExtent, QuickShopAPI api) {
        super(originalExtent);
        this.actor = actor;
        this.world = world;
        this.extent = originalExtent;
        this.api = api;
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(final BlockVector3 position, final T block) throws WorldEditException {
        if (!(this.world instanceof BukkitWorld)) {
            return super.setBlock(position, block);
        }
        org.bukkit.World world = ((BukkitWorld) this.world).getWorld();
        BlockState oldBlock = extent.getBlock(position);
        BlockState newBlock = block.toImmutableState();

        Location location = new Location(world, position.getBlockX(), position.getBlockY(), position.getBlockZ());

        if (extent.setBlock(position, block)) {
            // Block Changed
            if (oldBlock.getBlockType().getMaterial().hasContainer() && !newBlock.getBlockType().getMaterial().hasContainer()) {
                Shop shop = api.getShopManager().getShop(location, true); // Because WorldEdit can only remove half of shop, so we can keep another half as shop if it is doublechest shop.
                if (shop != null) {
                    Util.mainThreadRun(() -> {
                        api.logEvent(new ShopRemoveLog(actor.getUniqueId(), "WorldEdit", shop.saveToInfoStorage()));
                        shop.delete();
                    });
                }
            }
        }
        return super.setBlock(position, block);
    }
}
