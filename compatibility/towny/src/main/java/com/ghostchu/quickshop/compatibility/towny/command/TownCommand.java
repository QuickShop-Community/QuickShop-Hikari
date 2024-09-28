package com.ghostchu.quickshop.compatibility.towny.command;

import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermissionGroup;
import com.ghostchu.quickshop.common.util.CalculateUtil;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.compatibility.towny.Main;
import com.ghostchu.quickshop.compatibility.towny.TownyShopUtil;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TownCommand implements CommandHandler<Player> {

  private final Main plugin;

  public TownCommand(final Main plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(final Player sender, @NotNull final String commandLabel, @NotNull final String[] cmdArg) {

    final Shop shop = getLookingShop(sender);
    if(shop == null) {
      plugin.getApi().getTextManager().of(sender, "not-looking-at-shop").send();
      return;
    }
    // Check if anybody already set it to nation shop
    if(TownyShopUtil.getShopNation(shop) != null) {
      plugin.getApi().getTextManager().of(sender, "addon.towny.target-shop-already-is-nation-shop").send();
      return;
    }
    // Player permission check
    // Check if anybody already set it to town shop
    final Town recordTown = TownyShopUtil.getShopTown(shop);
    if(recordTown != null) {
      if(TownyShopUtil.getShopOriginalOwner(shop).equals(sender.getUniqueId())
         || recordTown.getMayor().getUUID().equals(sender.getUniqueId())
         || recordTown.getTrustedResidents().stream().map(Resident::getUUID)
                 .anyMatch(uuid->uuid.equals(sender.getUniqueId()))) {
        // Turn it back to a normal shop
        shop.setPlayerGroup(TownyShopUtil.getShopOriginalOwner(shop), BuiltInShopPermissionGroup.EVERYONE);
        shop.setOwner(QUserImpl.createSync(plugin.getApi().getPlayerFinder(), TownyShopUtil.getShopOriginalOwner(shop)));
        TownyShopUtil.setShopTown(shop, null);
        plugin.getApi().getTextManager().of(sender, "addon.towny.make-shop-no-longer-owned-by-town").send();
        return;
      } else {
        plugin.getApi().getTextManager().of(sender, "no-permission").send();
        return;
      }
    }
    // Set as a town shop
    final Town town = TownyAPI.getInstance().getTown(shop.getLocation());
    if(town == null) {
      plugin.getApi().getTextManager().of(sender, "addon.towny.target-shop-not-in-town-region").send();
      return;
    }
    if(plugin.getConfig().getBoolean("bank-mode.bank-plot-only", false)) {
      final TownBlock townBlock = TownyAPI.getInstance().getTownBlock(shop.getLocation());
      if(townBlock == null) {
        plugin.getApi().getTextManager().of(sender, "addon.towny.target-shop-not-in-town-region").send();
        plugin.getLogger().warning("Failed to get townBlock at " + shop.getLocation() + " maybe a bug?");
        return;
      }
      if(townBlock.getType() != TownBlockType.BANK) {
        plugin.getApi().getTextManager().of(sender, "addon.towny.plot-type-disallowed").send();
        return;
      }
    }
    final UUID uuid = plugin.getUuidConversion().convertTownyAccount(town);
    // Check if item and type are allowed
    if(plugin.getConfig().getBoolean("bank-mode.enable")) {
      final Double price = plugin.getPriceLimiter().getPrice(shop.getItem().getType(), shop.isSelling());
      if(price == null) {
        plugin.getApi().getTextManager().of(sender, "addon.towny.item-not-allowed").send();
        return;
      }
      if(shop.isStackingShop()) {
        shop.setPrice(CalculateUtil.multiply(price, shop.getShopStackingAmount()));
      } else {
        shop.setPrice(price);
      }
    }

    final UUID shopOwnerUUID = shop.getOwner().getUniqueIdIfRealPlayer().orElse(CommonUtil.getNilUniqueId());
    TownyShopUtil.setShopOriginalOwner(shop, shopOwnerUUID);
    TownyShopUtil.setShopTown(shop, town);
    shop.setPlayerGroup(shopOwnerUUID, BuiltInShopPermissionGroup.ADMINISTRATOR);
    shop.setOwner(QUserImpl.createSync(plugin.getApi().getPlayerFinder(), uuid));
    plugin.getApi().getTextManager().of(sender, "addon.towny.make-shop-owned-by-town", town.getName()).send();
    plugin.getApi().getTextManager().of(sender, "addon.towny.shop-owning-changing-notice").send();
  }
}
