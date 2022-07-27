package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.api.shop.ShopChunk;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class SimpleShopChunk implements ShopChunk {
    @NotNull
    private final String world;

    private final int x;

    private final int z;

    @Override
    public boolean isSame(@NotNull World world, int x, int z) {
        return isSame(world.getName(), x, z);
    }

    @Override
    public boolean isSame(@NotNull String world, int x, int z) {
        return this.x == x && this.z == z && this.world.equals(world);
    }

    @Override
    public @NotNull String getWorld() {
        return world;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getZ() {
        return z;
    }
}
