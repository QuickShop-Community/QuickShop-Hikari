/*
 *  This file is a part of project QuickShop, the name is BukkitInventoryWrapperManager.java
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

package com.ghostchu.quickshop.shop.inventory;

import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperManager;
import com.ghostchu.quickshop.util.JsonUtil;
import io.papermc.lib.PaperLib;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.jetbrains.annotations.NotNull;

public class BukkitInventoryWrapperManager implements InventoryWrapperManager {
    @Override
    public @NotNull String mklink(@NotNull InventoryWrapper wrapper) throws IllegalArgumentException {
        if (wrapper.getLocation() != null) {
            Block block = wrapper.getLocation().getBlock();
            BlockState state = PaperLib.getBlockState(wrapper.getLocation().getBlock(), false).getState();
            if (!(state instanceof Container)) {
                throw new IllegalArgumentException("Target reporting it self not a valid Container.");
            }
            String holder = JsonUtil.standard().toJson(new BlockHolder(block.getWorld().getName(), block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ()));
            return JsonUtil.standard().toJson(new CommonHolder(HolderType.BLOCK, holder));
        }
        throw new IllegalArgumentException("Target is invalid.");
    }

    @Override
    public @NotNull InventoryWrapper locate(@NotNull String symbolLink) throws IllegalArgumentException {
        try {
            CommonHolder commonHolder = JsonUtil.standard().fromJson(symbolLink, CommonHolder.class);
            //noinspection SwitchStatementWithTooFewBranches
            switch (commonHolder.getHolder()) {
                case BLOCK -> {
                    BlockHolder blockHolder = JsonUtil.standard().fromJson(commonHolder.getContent(), BlockHolder.class);
                    World world = Bukkit.getWorld(blockHolder.getWorld());
                    if (world == null) {
                        throw new IllegalArgumentException("Invalid symbol link: invalid world name.");
                    }
                    BlockState block = PaperLib.getBlockState(world.getBlockAt(blockHolder.getX(), blockHolder.getY(), blockHolder.getZ()), false).getState();
                    if (!(block instanceof Container)) {
                        throw new IllegalArgumentException("Invalid symbol link: target block not a Container");
                    }
                    return new BukkitInventoryWrapper(((Container) block).getInventory());
                }
                default -> throw new IllegalArgumentException("Invalid symbol link: invalid holder type.");
            }
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid symbol link: " + exception.getMessage());
        }
    }

    @AllArgsConstructor
    public
    enum HolderType {
        BLOCK("block"), UNKNOWN("unknown");
        private final String typeString;

        @NotNull
        public HolderType fromType(@NotNull String str) {
            for (HolderType value : values()) {
                if (value.typeString.equals(str)) {
                    return value;
                }
            }
            return UNKNOWN;
        }

        @NotNull
        public String toType() {
            return this.typeString;
        }
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class CommonHolder {
        private HolderType holder;
        private String content;
    }


    @Data
    @AllArgsConstructor
    @Builder
    public static class BlockHolder {
        private String world;
        private int x;
        private int y;
        private int z;
    }
}
