package com.ghostchu.quickshop.api.serialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
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
}
