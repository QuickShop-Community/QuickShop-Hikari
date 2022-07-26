package com.ghostchu.quickshop.compatibility.towny.command;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermissionGroup;
import com.ghostchu.quickshop.compatibility.towny.Main;
import com.ghostchu.quickshop.compatibility.towny.TownyShopUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;
import org.enginehub.squirrelid.Profile;
import org.jetbrains.annotations.NotNull;

public class TownCommand implements CommandHandler<Player> {
    private final Main plugin;

    public TownCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        Shop shop = getLookingShop(sender);
        if (shop == null) {
            plugin.getApi().getTextManager().of(sender, "not-looking-at-shop").send();
            return;
        }
        // Check if anybody already set it to nation shop
        if (TownyShopUtil.getShopNation(shop) != null) {
            plugin.getApi().getTextManager().of(sender, "addon.towny.target-shop-already-is-nation-shop").send();
            return;
        }

        // Player permission check

        // Check if anybody already set it to town shop
        Town recordTown = TownyShopUtil.getShopTown(shop);
        if (recordTown != null) {
            if (TownyShopUtil.getShopOriginalOwner(shop).equals(sender.getUniqueId())
                    || recordTown.getMayor().getUUID().equals(sender.getUniqueId())
                    || recordTown.getTrustedResidents().stream().map(Resident::getUUID)
                    .anyMatch(uuid -> uuid.equals(sender.getUniqueId()))) {
                // Turn it back to a normal shop
                shop.setPlayerGroup(TownyShopUtil.getShopOriginalOwner(shop), BuiltInShopPermissionGroup.EVERYONE);
                shop.setOwner(TownyShopUtil.getShopOriginalOwner(shop));
                TownyShopUtil.setShopTown(shop, null);
                plugin.getApi().getTextManager().of(sender, "addon.towny.make-shop-no-longer-owned-by-town").send();
                return;
            } else {
                plugin.getApi().getTextManager().of(sender, "no-permission").send();
                return;
            }
        }
        // Set as a town shop
        Town town = TownyAPI.getInstance().getTown(shop.getLocation());
        if (town == null) {
            plugin.getApi().getTextManager().of(sender, "addon.towny.target-shop-not-in-town-region").send();
            return;
        }
        String vaultAccountName = Main.processTownyAccount(town.getAccount().getName());
        Profile profile = QuickShop.getInstance().getPlayerFinder().find(vaultAccountName);
        // Check if item and type are allowed
        if (plugin.getConfig().getBoolean("bank-mode.enable")) {
            Double price = plugin.getPriceLimiter().getPrice(shop.getItem().getType(), shop.isSelling());
            if (price == null) {
                plugin.getApi().getTextManager().of(sender, "item-not-allowed").send();
                return;
            }
            if (shop.isStackingShop()) {
                shop.setPrice(price * shop.getShopStackingAmount());
            }
        }
        TownyShopUtil.setShopOriginalOwner(shop, shop.getOwner());
        TownyShopUtil.setShopTown(shop, town);
        shop.setPlayerGroup(shop.getOwner(), BuiltInShopPermissionGroup.ADMINISTRATOR);
        //noinspection ConstantConditions
        shop.setOwner(profile.getUniqueId());
        plugin.getApi().getTextManager().of(sender, "make-shop-owned-by-town", town.getName()).send();
        plugin.getApi().getTextManager().of(sender, "shop-owning-changing-notice").send();
    }
}
