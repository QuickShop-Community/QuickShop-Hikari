package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.ShopAction;
import com.ghostchu.quickshop.shop.SimpleInfo;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class SubCommand_Create implements CommandHandler<Player> {

  private final QuickShop plugin;


  public SubCommand_Create(@NotNull final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    final BlockIterator bIt = new BlockIterator(sender, 10);
    ItemStack item;
    if(parser.getArgs().isEmpty()) {
      plugin.text().of(sender, "command.wrong-args").send();
      return;
    } else if(parser.getArgs().size() == 1) {
      item = sender.getInventory().getItemInMainHand();
      if(item.getType().isAir()) {
        plugin.text().of(sender, "no-anythings-in-your-hand").send();
        return;
      }
    } else {
      final String matName = parser.getArgs().get(1);
      final Material material = matchMaterial(matName);
      if(material == null) {
        plugin.text().of(sender, "item-not-exist", matName).send();
        return;
      }
      if(parser.getArgs().size() > 2 && plugin.perm().hasPermission(sender, "quickshop.create.stack") && plugin.isAllowStack()) {
        try {
          int amount = Integer.parseInt(parser.getArgs().get(2));
          if(amount < 1) {
            amount = 1;
          }
          item = new ItemStack(material, amount);
        } catch(NumberFormatException e) {
          item = new ItemStack(material, 1);
        }
      } else {
        item = new ItemStack(material, 1);
      }
    }
    Log.debug("Pending task for material: " + item);

    final String price = parser.getArgs().get(0);

    while(bIt.hasNext()) {
      final Block b = bIt.next();
      if(!Util.canBeShop(b)) {
        continue;
      }
      // Send creation menu.
      plugin.getShopManager().getInteractiveManager().put(sender.getUniqueId(),
                                                          new SimpleInfo(b.getLocation(), ShopAction.CREATE_SELL, item, b.getRelative(sender.getFacing().getOppositeFace()), false));
      plugin.getShopManager().handleChat(sender, price);
      return;
    }
    plugin.text().of(sender, "not-looking-at-valid-shop-block").send();
  }

  @Nullable
  private Material matchMaterial(String itemName) {

    itemName = itemName.toUpperCase();
    itemName = itemName.replace(" ", "_");
    final Material material = Material.matchMaterial(itemName);
    if(isValidMaterial(material)) {
      return material;
    }
    return null;
  }

  private boolean isValidMaterial(@Nullable final Material material) {

    return material != null && !material.isAir();
  }

  @NotNull
  @Override
  public List<String> onTabComplete(
          @NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().size() == 1) {
      return Collections.singletonList(plugin.text().of(sender, "tabcomplete.price").plain());
    }
    if(sender.getInventory().getItemInMainHand().getType().isAir()) {
      if(parser.getArgs().size() == 2) {
        return Collections.singletonList(plugin.text().of(sender, "tabcomplete.item").plain());
      }
      if(parser.getArgs().size() == 3) {
        return Collections.singletonList(plugin.text().of(sender, "tabcomplete.amount").plain());
      }
    }
    return Collections.emptyList();
  }

}
