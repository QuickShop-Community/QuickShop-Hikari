package org.maxgamer.quickshop.shop.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.api.shop.inventory.InventoryWrapper;
import org.maxgamer.quickshop.api.shop.inventory.InventoryWrapperManager;
import org.maxgamer.quickshop.util.JsonUtil;

public class BukkitInventoryWrapperManager implements InventoryWrapperManager {
    @Override
    public @NotNull String mklink(@NotNull InventoryWrapper wrapper) throws IllegalArgumentException {
        if (wrapper.getLocation() != null) {
            Block block = wrapper.getLocation().getBlock();
            if ((block instanceof Container))
                throw new IllegalArgumentException("Target reporting it self not a valid Container.");
            String holder = JsonUtil.standard().toJson(new BlockHolder(block.getWorld().getName(), block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ()));
            return JsonUtil.standard().toJson(new CommonHolder(HolderType.BLOCK,holder));
        }
        throw new IllegalArgumentException("Target is invalid.");
    }

    @Override
    public @NotNull InventoryWrapper locate(@NotNull String symbolLink) throws IllegalArgumentException {
        try {
            CommonHolder commonHolder = JsonUtil.standard().fromJson(symbolLink,CommonHolder.class);
            //noinspection SwitchStatementWithTooFewBranches
            switch (commonHolder.getHolder()){
                case BLOCK:
                    BlockHolder blockHolder = JsonUtil.standard().fromJson(commonHolder.getContent(),BlockHolder.class);
                    World world = Bukkit.getWorld(blockHolder.getWorld());
                    if(world == null)
                        throw new IllegalArgumentException("Invalid symbol link: invalid world name.");
                    BlockState block = world.getBlockAt(blockHolder.getX(),blockHolder.getY(),blockHolder.getZ()).getState();
                    if(!(block instanceof Container))
                        throw new IllegalArgumentException("Invalid symbol link: target block not a Container");
                    return new BukkitInventoryWrapper(((Container) block).getInventory());
                default:
                    throw new IllegalArgumentException("Invalid symbol link: invalid holder type.");
            }
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid symbol link: " + exception.getMessage());
        }
    }

    @AllArgsConstructor
    enum HolderType {
        BLOCK("block"), UNKNOWN("unknown");
        private final String typeString;

        @NotNull
        public HolderType fromType(@NotNull String str) {
            for (HolderType value : values()) {
                if (value.typeString.equals(str))
                    return value;
            }
            return UNKNOWN;
        }
        @NotNull
        public String toType(){
            return this.typeString;
        }
    }

    @Data
    @AllArgsConstructor
    @Builder
    static class CommonHolder {
        private HolderType holder;
        private String content;
    }


    @Data
    @AllArgsConstructor
    @Builder
    static class BlockHolder {
        private String world;
        private int x;
        int y;
        int z;
    }
}
