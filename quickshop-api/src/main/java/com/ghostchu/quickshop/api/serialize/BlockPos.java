package com.ghostchu.quickshop.api.serialize;

import lombok.EqualsAndHashCode;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
@EqualsAndHashCode
public final class BlockPos {
    private final int version = 2;
    private final int x;
    private final int y;
    private final int z;
    private final String world;

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

    public int getX() {
        return x;
    }


    public int getY() {
        return y;
    }


    public int getZ() {
        return z;
    }

    public String getWorld() {
        return world;
    }

    @Override
    public String toString() {
        return "BlockPos{" +
                "version=" + version +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", world='" + world + '\'' +
                '}';
    }

    public static BlockPos deserialize(String string) {
        String[] split = string.split(";");
        return new BlockPos(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]), split[4]);
    }

    public String serialize() {
        return version + ";" + x + ";" + y + ";" + z + ";" + world;
    }
}
