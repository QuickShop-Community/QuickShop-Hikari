package com.ghostchu.quickshop.shop.inventory;

import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperManager;
import com.ghostchu.quickshop.api.serialize.BlockPos;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.performance.PerfMonitor;
import lombok.Builder;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class BukkitInventoryWrapperManager implements InventoryWrapperManager {

    @Override
    public @NotNull InventoryWrapper locate(@NotNull String symbolLink) throws IllegalArgumentException {
        try (PerfMonitor ignored = new PerfMonitor("Locate inventory wrapper")) {
            if (CommonUtil.isJson(symbolLink)) {
                Log.debug("Reading the old format symbol link: " + symbolLink);
                return locateOld(symbolLink);
            } else {
                return locateNew(symbolLink);
            }
        } catch (Exception exception) {
            throw new IllegalArgumentException(exception.getMessage());
        }
    }

    private InventoryWrapper locateNew(String symbolLink) {
        BlockPos blockPos = BlockPos.deserialize(symbolLink);
        World world = Bukkit.getWorld(blockPos.getWorld());
        if (world == null) {
            throw new IllegalArgumentException("Invalid symbol link: Invalid world name.");
        }
        BlockState state = world.getBlockAt(blockPos.getX(), blockPos.getY(), blockPos.getZ()).getState();
        if (!(state instanceof InventoryHolder holder)) {
            throw new IllegalArgumentException("Invalid symbol link: Target block not a InventoryHolder (map changed/resetted?)");
        }
        return new BukkitInventoryWrapper(holder.getInventory());
    }

    @Deprecated
    private InventoryWrapper locateOld(String symbolLink) {
        CommonHolder commonHolder = JsonUtil.standard().fromJson(symbolLink, CommonHolder.class);
        //noinspection SwitchStatementWithTooFewBranches
        switch (commonHolder.getHolder()) {
            case BLOCK -> {
                BlockHolder blockHolder = JsonUtil.standard().fromJson(commonHolder.getContent(), BlockHolder.class);
                World world = Bukkit.getWorld(blockHolder.getWorld());
                if (world == null) {
                    throw new IllegalArgumentException("Invalid symbol link: Invalid world name.");
                }
                BlockState block = world.getBlockAt(blockHolder.getX(), blockHolder.getY(), blockHolder.getZ()).getState();
                if (!(block instanceof InventoryHolder holder)) {
                    throw new IllegalArgumentException("Invalid symbol link: Target block not a Container (map changed/resetted?)");
                }
                return new BukkitInventoryWrapper(holder.getInventory());
            }
            default -> throw new IllegalArgumentException("Invalid symbol link: Invalid holder type.");
        }
    }

    @Override
    public @NotNull String mklink(@NotNull InventoryWrapper wrapper) throws IllegalArgumentException {
        try (PerfMonitor ignored = new PerfMonitor("Mklink inventory wrapper")) {
            if (wrapper.getLocation() != null) {
                Block block = wrapper.getLocation().getBlock();
                return new BlockPos(block.getLocation()).serialize();
            }
            throw new IllegalArgumentException("Target is invalid.");
        }
    }

    public @NotNull String mklink(@NotNull Location location) throws IllegalArgumentException {
        try (PerfMonitor ignored = new PerfMonitor("Mklink inventory wrapper")) {
            return new BlockPos(location).serialize();
        }
    }

    @Deprecated
    public enum HolderType {
        BLOCK("block"), UNKNOWN("unknown");
        private final String typeString;

        HolderType(String typeString) {
            this.typeString = typeString;
        }

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
    @Builder
    @Deprecated
    public static class CommonHolder {
        private HolderType holder;
        private String content;

        public CommonHolder(HolderType holder, String content) {
            this.holder = holder;
            this.content = content;
        }
    }

    @Data
    @Builder
    @Deprecated
    public static class BlockHolder {
        private String world;
        private int x;
        private int y;
        private int z;

        public BlockHolder(String world, int x, int y, int z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
