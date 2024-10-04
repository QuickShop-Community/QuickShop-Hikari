package com.ghostchu.quickshop.listener;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import com.ghostchu.quickshop.shop.inventory.BukkitInventoryWrapper;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DisplayProtectionListener extends AbstractProtectionListener {

  public DisplayProtectionListener(final QuickShop plugin) {

    super(plugin);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void inventory(final InventoryOpenEvent event) {

    Util.inventoryCheck(new BukkitInventoryWrapper(event.getInventory()));
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void inventory(final InventoryPickupItemEvent event) {

    final ItemStack itemStack = event.getItem().getItemStack();
    if(!AbstractDisplayItem.checkIsGuardItemStack(itemStack)) {
      return; // We didn't care that
    }
    @Nullable final Location loc = event.getInventory().getLocation();
    @Nullable final InventoryHolder holder = event.getInventory().getHolder();
    event.setCancelled(true);
    sendAlert(
            "[DisplayGuard] Something  "
            + holder
            + " at "
            + loc
            + " trying pickup the DisplayItem,  you should teleport to that location and to check detail..");
    Util.inventoryCheck(new BukkitInventoryWrapper(event.getInventory()));
  }

  private void sendAlert(@NotNull final String msg) {

    if(!plugin.getConfig().getBoolean("send-display-item-protection-alert")) {
      return;
    }
    MsgUtil.sendGlobalAlert(msg);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void item(final ItemDespawnEvent event) {

    final ItemStack itemStack = event.getEntity().getItemStack();
    if(AbstractDisplayItem.checkIsGuardItemStack(itemStack)) {
      event.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void player(final PlayerFishEvent event) {

    if(event.getState() != State.CAUGHT_ENTITY) {
      return;
    }
    if(event.getCaught() == null) {
      return;
    }
    if(!(event.getCaught() instanceof Item item)) {
      return;
    }
    final ItemStack is = item.getItemStack();
    if(!AbstractDisplayItem.checkIsGuardItemStack(is)) {
      return;
    }
    event.getHook().remove();
    event.setCancelled(true);
    sendAlert(
            "[DisplayGuard] Player "
            + event.getPlayer().getName()
            + " trying hook item use Fishing Rod, QuickShop already removed it.");
    Util.inventoryCheck(new BukkitInventoryWrapper(event.getPlayer().getInventory()));
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void player(final PlayerArmorStandManipulateEvent event) {

    if(!AbstractDisplayItem.checkIsGuardItemStack(event.getArmorStandItem())) {
      return;
    }
    event.setCancelled(true);
    Util.inventoryCheck(new BukkitInventoryWrapper(event.getPlayer().getInventory()));
    sendAlert(
            "[DisplayGuard] Player  "
            + event.getPlayer().getName()
            + " trying mainipulate armorstand contains displayItem.");
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void portal(final EntityPortalEvent event) {

    if(!(event.getEntity() instanceof Item itemEntity)) {
      return;
    }
    if(AbstractDisplayItem.checkIsGuardItemStack(itemEntity.getItemStack())) {
      event.setCancelled(true);
      event.getEntity().remove();
      sendAlert(
              "[DisplayGuard] Somebody want dupe the display by Portal at "
              + event.getFrom()
              + " , QuickShop already cancel it.");
    }
  }
}
