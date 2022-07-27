package com.ghostchu.quickshop.shop.datatype;

import com.google.gson.annotations.Expose;
import lombok.Getter;

//TODO
@Getter
public class ShopPersistentData {
    @Expose
    private final String world;
    @Expose
    private final int x;
    @Expose
    private final int y;
    @Expose
    private final int z;
    private final boolean setup;

    public ShopPersistentData(int x, int y, int z, String world, boolean setup) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.setup = setup;
    }
}
