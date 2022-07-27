package com.ghostchu.quickshop.shop;

import lombok.Builder;
import lombok.Data;

import java.util.Objects;

/**
 * TODO This class used for storage the shop sign
 */
@Data
@Builder
public class ShopSignStorage {
    private static final boolean SHOP_SIGN = true;
    private String world;
    private int x;
    private int y;
    private int z;

    public ShopSignStorage(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean equals(String world, int x, int y, int z) {
        return Objects.equals(this.world, world) && this.x == x && this.y == y && this.z == z;
    }
}
