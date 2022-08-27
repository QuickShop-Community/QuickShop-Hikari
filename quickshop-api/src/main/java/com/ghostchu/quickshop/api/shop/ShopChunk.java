package com.ghostchu.quickshop.api.shop;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * Shop chunk based searching
 */
public interface ShopChunk {
    String getWorld();

    int getX();

    int getZ();

    boolean isSame(@NotNull String world, int x, int z);

    boolean isSame(@NotNull World world, int x, int z);
}
