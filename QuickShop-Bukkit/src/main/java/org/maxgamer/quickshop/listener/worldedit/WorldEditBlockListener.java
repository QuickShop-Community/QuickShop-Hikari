/*
 * This file is a part of project QuickShop, the name is WorldEditBlockListener.java
 *  Copyright (C) PotatoCraft Studio and contributors
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

package org.maxgamer.quickshop.listener.worldedit;

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
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.logging.container.ShopRemoveLog;

/**
 * Proxy class to handle WorldEdit actions
 */
public class WorldEditBlockListener extends AbstractDelegateExtent {
    private final Actor actor;
    private final World world;
    private final Extent extent;
    private final QuickShop plugin;

    // Same Package access
    WorldEditBlockListener(Actor actor, World world, Extent originalExtent, QuickShop plugin) {
        super(originalExtent);
        this.actor = actor;
        this.world = world;
        this.extent = originalExtent;
        this.plugin = plugin;
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
                Shop shop = plugin.getShopManager().getShop(location, true); // Because WorldEdit can only remove half of shop, so we can keep another half as shop if it is doublechest shop.
                if (shop != null) {
                    plugin.getLogger().info("Removing shop at " + location + " because removed by WorldEdit.");
                    plugin.logEvent(new ShopRemoveLog(actor.getUniqueId() != null ? actor.getUniqueId() : Util.getNilUniqueId(), "WorldEdit", shop.saveToInfoStorage()));
                    Util.mainThreadRun(shop::delete);
                }
            }
        }
        return super.setBlock(position, block);
    }
}
