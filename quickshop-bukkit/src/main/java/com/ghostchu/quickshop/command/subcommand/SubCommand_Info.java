package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopChunk;
import com.ghostchu.quickshop.util.MsgUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SubCommand_Info implements CommandHandler<CommandSender> {

  private final QuickShop plugin;

  public SubCommand_Info(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final CommandSender sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    int buying = 0;
    int selling = 0;
    int chunks = 0;
    int worlds = 0;
    int nostock = 0;

    for(final Map<ShopChunk, Map<Location, Shop>> inWorld :
            plugin.getShopManager().getShops().values()) {
      worlds++;

      for(final Map<Location, Shop> inChunk : inWorld.values()) {
        chunks++;
        for(final Shop shop : inChunk.values()) {
          if(shop.isBuying()) {
            buying++;
          } else if(shop.isSelling()) {
            selling++;
          }
          if(shop.isSelling() && shop.isLoaded() && shop.getRemainingStock() == 0) {
            nostock++;
          }
        }
      }
    }

    MsgUtil.sendDirectMessage(sender, Component.text("QuickShop Statistics...").color(NamedTextColor.GOLD));
    MsgUtil.sendDirectMessage(sender, Component.text("Server UniqueId: " + plugin.getServerUniqueID()).color(NamedTextColor.GREEN));
    MsgUtil.sendDirectMessage(sender, Component.text((buying + selling)
                                                     + " shops in "
                                                     + chunks
                                                     + " chunks spread over "
                                                     + worlds
                                                     + " worlds.").color(NamedTextColor.GREEN));
    MsgUtil.sendDirectMessage(sender, Component.text(nostock
                                                     + " out-of-stock loaded shops (excluding doubles) which will be removed by /quickshop clean.").color(NamedTextColor.GREEN));
    MsgUtil.sendDirectMessage(sender, Component.text("QuickShop " + QuickShop.getInstance().getVersion()).color(NamedTextColor.GREEN));
  }

}
