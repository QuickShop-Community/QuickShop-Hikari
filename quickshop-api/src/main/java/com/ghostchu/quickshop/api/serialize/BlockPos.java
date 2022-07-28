package com.ghostchu.quickshop.api.serialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode
@ToString
public class BlockPos {
    private int x;
    private int y;
    private int z;
    private String world;

    public BlockPos(@NotNull Location location) {
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.world = location.getWorld().getName();
    }

    public BlockPos(int x, int y, int z, String world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }
}
