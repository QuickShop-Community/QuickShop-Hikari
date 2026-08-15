package com.ghostchu.quickshop.shop.inventory;

import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperManager;
import com.ghostchu.quickshop.api.serialize.BlockPos;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.performance.PerfMonitor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BukkitInventoryWrapperManager implements InventoryWrapperManager {

  @Override
  @NotNull
  public InventoryWrapper locate(@NotNull final String symbolLink) throws IllegalArgumentException {

    try(PerfMonitor ignored = new PerfMonitor("Locate inventory wrapper")) {
      if(CommonUtil.isJson(symbolLink)) {
        Log.debug("Reading the old format symbol link: " + symbolLink);
        return locateOld(symbolLink);
      } else {
        return locateNew(symbolLink);
      }
    } catch(final Exception exception) {
      throw new IllegalArgumentException(exception.getMessage());
    }
  }

  private InventoryWrapper locateNew(final String symbolLink) {

    final BlockPos blockPos = BlockPos.deserialize(symbolLink);
    final World world = Bukkit.getWorld(blockPos.getWorld());
    if(world == null) {
      throw new IllegalArgumentException("Invalid symbol link: Invalid world name.");
    }
    return new BukkitInventoryWrapper(fromLocation(world, blockPos.getX(), blockPos.getY(), blockPos.getZ()).getInventory());
  }

  @Deprecated
  private InventoryWrapper locateOld(final String symbolLink) {

    final CommonHolder commonHolder = JsonUtil.standard().fromJson(symbolLink, CommonHolder.class);
    //noinspection SwitchStatementWithTooFewBranches
    switch(commonHolder.getHolder()) {
      case BLOCK -> {
        final BlockHolder blockHolder = JsonUtil.standard().fromJson(commonHolder.getContent(), BlockHolder.class);
        final World world = Bukkit.getWorld(blockHolder.getWorld());
        if(world == null) {
          throw new IllegalArgumentException("Invalid symbol link: Invalid world name.");
        }
        return new BukkitInventoryWrapper(fromLocation(world, blockHolder.getX(), blockHolder.getY(), blockHolder.getZ()).getInventory());
      }
      default -> throw new IllegalArgumentException("Invalid symbol link: Invalid holder type.");
    }
  }

  public InventoryHolder fromLocation(final World world, final int x, final int y, final int z) {

    try {

      final BlockState block = world.getBlockAt(x, y, z).getState(false);
      if(!(block instanceof InventoryHolder holder)) {
        throw new IllegalArgumentException("Invalid symbol link: Target block not a Container (map changed/resetted?)");
      }
      return holder;
    } catch(final NoSuchMethodError ignore) {
      if(!(world.getBlockAt(x, y, z).getState(false) instanceof InventoryHolder holder)) {
        throw new IllegalArgumentException("Invalid symbol link: Target block not a Container (map changed/resetted?)");
      }
      return holder;
    }
  }

  @Override
  @NotNull
  public String mklink(@NotNull final InventoryWrapper wrapper) throws IllegalArgumentException {

    try(PerfMonitor ignored = new PerfMonitor("Mklink inventory wrapper")) {
      if(wrapper.getLocation() != null) {
        final Block block = wrapper.getLocation().getBlock();
        return new BlockPos(block.getLocation()).serialize();
      }
      throw new IllegalArgumentException("Target is invalid.");
    }
  }

  @NotNull
  public String mklink(@NotNull final Location location) throws IllegalArgumentException {

    try(PerfMonitor ignored = new PerfMonitor("Mklink inventory wrapper")) {
      return new BlockPos(location).serialize();
    }
  }

  @Deprecated
  public enum HolderType {
    BLOCK("block"), UNKNOWN("unknown");
    private final String typeString;

    HolderType(final String typeString) {

      this.typeString = typeString;
    }

    @NotNull
    public HolderType fromType(@NotNull final String str) {

      for(final HolderType value : values()) {
        if(value.typeString.equals(str)) {
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

  @Deprecated
  public static class CommonHolder {

    private HolderType holder;
    private String content;

    public CommonHolder(final HolderType holder, final String content) {

      this.holder = holder;
      this.content = content;
    }

    public static class CommonHolderBuilder {

      private HolderType holder;
      private String content;

      CommonHolderBuilder() {

  }

      public BukkitInventoryWrapperManager.CommonHolder.CommonHolderBuilder holder(final HolderType holder) {

        this.holder = holder;
        return this;
      }

      public BukkitInventoryWrapperManager.CommonHolder.CommonHolderBuilder content(final String content) {

        this.content = content;
        return this;
      }

      public BukkitInventoryWrapperManager.CommonHolder build() {

        return new BukkitInventoryWrapperManager.CommonHolder(this.holder, this.content);
      }

      @Override
      public String toString() {

        return "BukkitInventoryWrapperManager.CommonHolder.CommonHolderBuilder(holder=" + this.holder + ", content=" + this.content + ")";
      }
    }

    public static BukkitInventoryWrapperManager.CommonHolder.CommonHolderBuilder builder() {

      return new BukkitInventoryWrapperManager.CommonHolder.CommonHolderBuilder();
    }

    public HolderType getHolder() {

      return this.holder;
    }

    public String getContent() {

      return this.content;
    }

    public void setHolder(final HolderType holder) {

      this.holder = holder;
    }

    public void setContent(final String content) {

      this.content = content;
    }

    @Override
    public boolean equals(final Object o) {

      if(o == this) return true;
      if(!(o instanceof BukkitInventoryWrapperManager.CommonHolder)) return false;
      final BukkitInventoryWrapperManager.CommonHolder other = (BukkitInventoryWrapperManager.CommonHolder)o;
      return Objects.equals(this.getHolder(), other.getHolder())
             && Objects.equals(this.getContent(), other.getContent());
    }

    @Override
    public int hashCode() {

      return Objects.hash(this.getHolder(), this.getContent());
    }

    @Override
    public String toString() {

      return "BukkitInventoryWrapperManager.CommonHolder(holder=" + this.getHolder() + ", content=" + this.getContent() + ")";
    }
  }

  @Deprecated
  public static class BlockHolder {

    private String world;
    private int x;
    private int y;
    private int z;

    public BlockHolder(final String world, final int x, final int y, final int z) {

      this.world = world;
      this.x = x;
      this.y = y;
      this.z = z;
    }

    public static class BlockHolderBuilder {

      private String world;
      private int x;
      private int y;
      private int z;

      BlockHolderBuilder() {

      }

      public BukkitInventoryWrapperManager.BlockHolder.BlockHolderBuilder world(final String world) {

        this.world = world;
        return this;
      }

      public BukkitInventoryWrapperManager.BlockHolder.BlockHolderBuilder x(final int x) {

        this.x = x;
        return this;
      }

      public BukkitInventoryWrapperManager.BlockHolder.BlockHolderBuilder y(final int y) {

        this.y = y;
        return this;
      }

      public BukkitInventoryWrapperManager.BlockHolder.BlockHolderBuilder z(final int z) {

        this.z = z;
        return this;
      }

      public BukkitInventoryWrapperManager.BlockHolder build() {

        return new BukkitInventoryWrapperManager.BlockHolder(this.world, this.x, this.y, this.z);
      }

      @Override
      public String toString() {

        return "BukkitInventoryWrapperManager.BlockHolder.BlockHolderBuilder(world=" + this.world + ", x=" + this.x + ", y=" + this.y + ", z=" + this.z + ")";
  }
  }

    public static BukkitInventoryWrapperManager.BlockHolder.BlockHolderBuilder builder() {

      return new BukkitInventoryWrapperManager.BlockHolder.BlockHolderBuilder();
    }

    public String getWorld() {

      return this.world;
    }

    public int getX() {

      return this.x;
    }

    public int getY() {

      return this.y;
    }

    public int getZ() {

      return this.z;
    }

    public void setWorld(final String world) {

      this.world = world;
    }

    public void setX(final int x) {

      this.x = x;
    }

    public void setY(final int y) {

      this.y = y;
    }

    public void setZ(final int z) {

      this.z = z;
    }

    @Override
    public boolean equals(final Object o) {

      if(o == this) return true;
      if(!(o instanceof BukkitInventoryWrapperManager.BlockHolder)) return false;
      final BukkitInventoryWrapperManager.BlockHolder other = (BukkitInventoryWrapperManager.BlockHolder)o;
      return this.getX() == other.getX()
             && this.getY() == other.getY()
             && this.getZ() == other.getZ()
             && Objects.equals(this.getWorld(), other.getWorld());
    }

    @Override
    public int hashCode() {

      return Objects.hash(this.getX(), this.getY(), this.getZ(), this.getWorld());
    }

    @Override
    public String toString() {

      return "BukkitInventoryWrapperManager.BlockHolder(world=" + this.getWorld() + ", x=" + this.getX() + ", y=" + this.getY() + ", z=" + this.getZ() + ")";
    }
  }
}
