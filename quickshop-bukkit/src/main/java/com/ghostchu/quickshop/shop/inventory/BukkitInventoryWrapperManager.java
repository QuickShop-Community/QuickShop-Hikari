package com.ghostchu.quickshop.shop.inventory;

import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperManager;
import com.ghostchu.quickshop.util.JsonUtil;
import io.papermc.lib.PaperLib;
import lombok.Builder;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.jetbrains.annotations.NotNull;

public class BukkitInventoryWrapperManager implements InventoryWrapperManager {
    @Override
    public @NotNull InventoryWrapper locate(@NotNull String symbolLink) throws IllegalArgumentException {
        try {
            CommonHolder commonHolder = JsonUtil.standard().fromJson(symbolLink, CommonHolder.class);
            //noinspection SwitchStatementWithTooFewBranches
            switch (commonHolder.getHolder()) {
                case BLOCK -> {
                    BlockHolder blockHolder = JsonUtil.standard().fromJson(commonHolder.getContent(), BlockHolder.class);
                    World world = Bukkit.getWorld(blockHolder.getWorld());
                    if (world == null) {
                        throw new IllegalArgumentException("Invalid symbol link: Invalid world name.");
                    }
                    BlockState block = PaperLib.getBlockState(world.getBlockAt(blockHolder.getX(), blockHolder.getY(), blockHolder.getZ()), false).getState();
                    if (!(block instanceof Container)) {
                        throw new IllegalArgumentException("Invalid symbol link: Target block not a Container (map changed/resetted?)");
                    }
                    return new BukkitInventoryWrapper(((Container) block).getInventory());
                }
                default -> throw new IllegalArgumentException("Invalid symbol link: Invalid holder type.");
            }
        } catch (Exception exception) {
            throw new IllegalArgumentException(exception.getMessage());
        }
    }

    @Override
    public @NotNull String mklink(@NotNull InventoryWrapper wrapper) throws IllegalArgumentException {
        if (wrapper.getLocation() != null) {
            Block block = wrapper.getLocation().getBlock();
            BlockState state = PaperLib.getBlockState(wrapper.getLocation().getBlock(), false).getState();
            if (!(state instanceof Container)) {
                throw new IllegalArgumentException("Target reporting it self not a valid Container.");
            }
            String holder = JsonUtil.standard().toJson(new BlockHolder(block.getWorld().getName(), block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ()));
            return JsonUtil.standard().toJson(new CommonHolder(HolderType.BLOCK, holder));
        }
        throw new IllegalArgumentException("Target is invalid.");
    }

    @Data
    @Builder
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


    public
    enum HolderType {
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
}
