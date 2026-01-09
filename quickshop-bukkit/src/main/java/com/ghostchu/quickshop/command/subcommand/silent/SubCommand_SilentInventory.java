package com.ghostchu.quickshop.command.subcommand.silent;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.listener.LockListener;
import com.ghostchu.quickshop.shop.ContainerShop;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class SubCommand_SilentInventory extends SubCommand_SilentBase {

  public SubCommand_SilentInventory(final QuickShop plugin) {

    super(plugin);
  }

  @Override
  protected void doSilentCommand(final Player sender, @NotNull final Shop shop, @NotNull final CommandParser parser) {

    if(!(shop instanceof final ContainerShop cs)) {
      plugin.text().of(sender, "not-looking-at-shop").send();
      return;
    }

    final InventoryWrapper inventory = cs.getInventory();

    if(inventory == null || inventory.getHolder() == null) {
      Log.debug("Inventory is empty! " + cs);
      return;
    }
    
    if(plugin.getConfig().getBoolean("shop.lock") && !shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.ACCESS_INVENTORY)) {
      if(plugin.perm().hasPermission(sender, "quickshop.other.open")) {
        if(LockListener.lockCoolDown.getIfPresent(sender.getUniqueId()) == null) {
          plugin.text().of(sender, "bypassing-lock").send();
          LockListener.lockCoolDown.put(sender.getUniqueId(), LockListener.EMPTY_OBJECT);
        }
        return;
      }
      plugin.text().of(sender, "that-is-locked").send();
      return;
    }

    sender.openInventory(inventory.getHolder().getInventory());
    QuickShop.inShop.add(sender.getUniqueId());
  }
}
