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
    private final int version = 2;
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

    public static BlockPos deserialize(String string) {
        String[] split = string.split(";");
        return new BlockPos(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]), split[4]);
    }

    public String serialize() {
        return version + ";" + x + ";" + y + ";" + z + ";" + world;
    }
}
