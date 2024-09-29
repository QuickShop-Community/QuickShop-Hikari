package com.ghostchu.quickshop.compatibility.openinv;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.shop.inventory.BukkitInventoryWrapper;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OpenInvCommand implements CommandHandler<Player> {

  private final Main plugin;

  public OpenInvCommand(final Main openinv) {

    this.plugin = openinv;
  }

  /**
   * Calling while command executed by specified sender
   *
   * @param sender       The command sender but will automatically convert to specified instance
   * @param commandLabel The command prefix (/quickshop = qs, /shop = shop)
   * @param cmdArg       The arguments (/quickshop create stone will receive stone)
   */
  @Override
  public void onCommand(final Player sender, @NotNull final String commandLabel, @NotNull final String[] cmdArg) {

    final Shop shop = getLookingShop(sender);
    if(shop == null) {
      plugin.getApi().getTextManager().of(sender, "not-looking-at-shop").send();
      return;
    }
    if(!sender.getUniqueId().equals(shop.getOwner().getUniqueId()) && !QuickShop.getPermissionManager().hasPermission(sender, "quickshop.admin")) {
      plugin.getApi().getTextManager().of(sender, "no-permission").send();
      return;
    }
    if(shop.getInventory() instanceof EnderChestWrapper) {
      shop.setInventory(new BukkitInventoryWrapper((((InventoryHolder)shop.getLocation().getBlock().getState()).getInventory())), plugin.getApi().getInventoryWrapperRegistry().get("QuickShop-Hikari"));
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.to-chest")));
    } else {
      shop.setInventory(new EnderChestWrapper(shop.getOwner().getUniqueIdIfRealPlayer().orElse(CommonUtil.getNilUniqueId()), plugin.getOpenInv(), plugin), plugin.getManager());
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.to-echest")));
    }
  }

  /**
   * Calling while sender trying to tab-complete
   *
   * @param sender       The command sender but will automatically convert to specified instance
   * @param commandLabel The command prefix (/quickshop = qs, /shop = shop)
   * @param cmdArg       The arguments (/quickshop create stone [TAB] will receive stone)
   *
   * @return Candidate list
   */
  @Override
  public @Nullable List<String> onTabComplete(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final String[] cmdArg) {

    return CommandHandler.super.onTabComplete(sender, commandLabel, cmdArg);
  }
}
