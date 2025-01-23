package com.ghostchu.quickshop.compatibility.worldedit;

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.bukkit.Location;

/**
 * Proxy class to handle WorldEdit actions
 */
public class WorldEditBlockListener extends AbstractDelegateExtent {

  private final Actor actor;
  private final World world;
  private final Extent extent;
  private final QuickShopAPI api;

  // Same Package access
  WorldEditBlockListener(final Actor actor, final World world, final Extent originalExtent, final QuickShopAPI api) {

    super(originalExtent);
    this.actor = actor;
    this.world = world;
    this.extent = originalExtent;
    this.api = api;
  }

  @Override
  public <T extends BlockStateHolder<T>> boolean setBlock(final BlockVector3 position, final T block) throws WorldEditException {

    if(!(this.world instanceof BukkitWorld)) {
      return super.setBlock(position, block);
    }
    final org.bukkit.World bukkitWorld = ((BukkitWorld)this.world).getWorld();
    final BlockState oldBlock = extent.getBlock(position);
    final BlockState newBlock = block.toImmutableState();

    final Location location = new Location(bukkitWorld, position.getBlockX(), position.getBlockY(), position.getBlockZ());

    if(extent.setBlock(position, block)) {
      // Block Changed
      if(oldBlock.getBlockType().getMaterial().hasContainer() && !newBlock.getBlockType().getMaterial().hasContainer()) {
        final Shop shop = api.getShopManager().getShop(location, true); // Because WorldEdit can only remove half of shop, so we can keep another half as shop if it is doublechest shop.
        if(shop != null) {
          Util.mainThreadRun(()->{
            api.logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "WorldEdit", false), "WorldEdit", shop.saveToInfoStorage()));
            api.getShopManager().deleteShop(shop);
          });
        }
      }
    }
    return super.setBlock(position, block);
  }
}
