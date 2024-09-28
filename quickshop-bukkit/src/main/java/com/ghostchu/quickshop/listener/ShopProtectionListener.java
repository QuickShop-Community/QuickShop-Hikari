package com.ghostchu.quickshop.listener;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.shop.datatype.HopperPersistentData;
import com.ghostchu.quickshop.shop.datatype.HopperPersistentDataType;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShopProtectionListener extends AbstractProtectionListener {

  private final NamespacedKey hopperKey = new NamespacedKey(QuickShop.getInstance().getJavaPlugin(), "hopper-persistent-data");
  private boolean hopperProtect;
  private boolean hopperOwnerExclude;

  public ShopProtectionListener(@NotNull final QuickShop plugin) {

    super(plugin);
    init();
  }

  private void init() {

    this.hopperProtect = plugin.getConfig().getBoolean("protect.hopper", true);
    this.hopperOwnerExclude = plugin.getConfig().getBoolean("protect.hopper-owner-exclude", false);
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBlockExplode(final BlockExplodeEvent e) {

    for(int i = 0, a = e.blockList().size(); i < a; i++) {
      final Block b = e.blockList().get(i);
      Shop shop = getShopNature(b.getLocation(), true);
      if(shop == null) {
        shop = getShopNextTo(b.getLocation());
      }
      if(shop != null) {
        if(plugin.getConfig().getBoolean("protect.explode")) {
          e.setCancelled(true);
        } else {
          plugin.logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "Exploding", false), "BlockBreak(explode)", shop.saveToInfoStorage()));
          plugin.getShopManager().deleteShop(shop);
        }
      }
    }
  }

  /**
   * Gets the shop a sign is attached to
   *
   * @param loc The location of the sign
   *
   * @return The shop
   */
  @Nullable
  private Shop getShopNextTo(@NotNull final Location loc) {

    final Block b = Util.getAttached(loc.getBlock());
    // Util.getAttached(b)
    if(b == null) {
      return null;
    }

    return getShopNature(b.getLocation(), false);
  }

  /*
   * Handles shops breaking through entity changes (like Wither etc.)
   */
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onEntityBlockChange(final EntityChangeBlockEvent e) {

    if(!plugin.getConfig().getBoolean("protect.entity", true)) {
      return;
    }
    if(getShopNature(e.getBlock().getLocation(), true) != null) {
      e.setCancelled(true);
    }
  }

  /*
   * Handles shops breaking through explosions
   */
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onExplode(final EntityExplodeEvent e) {

    for(int i = 0, a = e.blockList().size(); i < a; i++) {
      final Block b = e.blockList().get(i);
      Shop shop = getShopNature(b.getLocation(), true);
      if(shop == null) {
        shop = getShopNextTo(b.getLocation());
      }
      if(shop == null) {
        continue;
      }

      if(plugin.getConfig().getBoolean("protect.explode")) {
        e.setCancelled(true);
      } else {
        plugin.logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "EntityExploding", false), "BlockBreak(explode)", shop.saveToInfoStorage()));
        plugin.getShopManager().deleteShop(shop);
      }
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onInventoryMove(final InventoryMoveItemEvent event) {

    if(!this.hopperProtect) {
      return;
    }
    final Location loc = event.getSource().getLocation();

    if(loc == null) {
      return;
    }
    final Shop shop = getShopRedstone(loc, true);

    if(shop == null) {
      return;
    }
    if(this.hopperOwnerExclude) {
      if(event.getDestination().getHolder() instanceof Hopper hopper) {
        final HopperPersistentData hopperPersistentData = hopper.getPersistentDataContainer().get(hopperKey, HopperPersistentDataType.INSTANCE);
        if(hopperPersistentData != null) {
          if(shop.playerAuthorize(hopperPersistentData.getPlayer(), BuiltInShopPermission.ACCESS_INVENTORY)) {
            return;
          }
        }
      }
    }
    event.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onPlaceHopper(final BlockPlaceEvent e) {

    if(e.getBlockPlaced().getState() instanceof Hopper hopper) {
      hopper.getPersistentDataContainer().set(hopperKey, HopperPersistentDataType.INSTANCE, new HopperPersistentData(e.getPlayer().getUniqueId()));
      hopper.update();
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onStructureGrow(final StructureGrowEvent e) {

    for(final BlockState block : e.getBlocks()) {
      if(getShopNature(block.getLocation(), true) != null) {
        e.setCancelled(true);
      }
    }
  }

  @Override
  public ReloadResult reloadModule() {

    init();
    return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
  }
}
