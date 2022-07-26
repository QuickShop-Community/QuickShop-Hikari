package com.ghostchu.quickshop.compatibility.towny.command;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermissionGroup;
import com.ghostchu.quickshop.compatibility.towny.Main;
import com.ghostchu.quickshop.compatibility.towny.TownyShopUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.enginehub.squirrelid.Profile;
import org.jetbrains.annotations.NotNull;

public class NationCommand implements CommandHandler<Player> {
    private final Main plugin;

    public NationCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        Shop shop = getLookingShop(sender);
        if (shop == null) {
            plugin.getApi().getTextManager().of(sender, "not-looking-at-shop").send();
            return;
        }
        // Check if anybody already set it to town shop
        if (TownyShopUtil.getShopTown(shop) != null) {
            plugin.getApi().getTextManager().of(sender, "addon.towny.target-shop-already-is-town-shop").send();
            return;
        }
        Nation recordNation = TownyShopUtil.getShopNation(shop);
        if (recordNation != null) {
            if (TownyShopUtil.getShopOriginalOwner(shop).equals(sender.getUniqueId())
                    || recordNation.getKing().getUUID().equals(sender.getUniqueId())) {
                // Turn it back to a normal shop
                shop.setPlayerGroup(TownyShopUtil.getShopOriginalOwner(shop), BuiltInShopPermissionGroup.EVERYONE);
                shop.setOwner(TownyShopUtil.getShopOriginalOwner(shop));
                TownyShopUtil.setShopTown(shop, null);
                plugin.getApi().getTextManager().of(sender, "addon.towny.make-shop-no-longer-owned-by-town").send();
                return;
            } else {
                plugin.getApi().getTextManager().of(sender, "no-permission").send();
            }
        }
        // Check if anybody already set it to nation shop
        if (TownyShopUtil.getShopNation(shop) != null) {
            // Turn it back to a normal shop
            shop.setPlayerGroup(TownyShopUtil.getShopOriginalOwner(shop), BuiltInShopPermissionGroup.EVERYONE);
            shop.setOwner(TownyShopUtil.getShopOriginalOwner(shop));
            TownyShopUtil.setShopNation(shop, null);
            plugin.getApi().getTextManager().of(sender, "addon.towny.make-shop-no-longer-owned-by-nation").send();
            return;
        }
        // Set as a nation shop
        Town town = TownyAPI.getInstance().getTown(shop.getLocation());
        if (town == null) {
            plugin.getApi().getTextManager().of(sender, "addon.towny.target-shop-not-in-town-region").send();
            return;
        }
        Nation nation = TownyAPI.getInstance().getTownNationOrNull(town);
        if (nation == null) {
            plugin.getApi().getTextManager().of(sender, "addon.towny.target-shop-not-in-nation-region").send();
            return;
        }
        String vaultAccountName = nation.getAccount().getName();
        Profile profile = QuickShop.getInstance().getPlayerFinder().find(vaultAccountName);
        if (profile == null) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(vaultAccountName);
            profile = new Profile(player.getUniqueId(), vaultAccountName);
        }
        TownyShopUtil.setShopOriginalOwner(shop, shop.getOwner());
        TownyShopUtil.setShopNation(shop, nation);
        shop.setPlayerGroup(shop.getOwner(), BuiltInShopPermissionGroup.ADMINISTRATOR);
        shop.setOwner(profile.getUniqueId());
        plugin.getApi().getTextManager().of(sender, "make-shop-owned-by-nation", nation.getName()).send();
        plugin.getApi().getTextManager().of(sender, "shop-owning-changing-notice").send();
    }
}
