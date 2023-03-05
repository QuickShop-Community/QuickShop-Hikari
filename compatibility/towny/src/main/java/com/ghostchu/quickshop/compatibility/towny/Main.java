package com.ghostchu.quickshop.compatibility.towny;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.command.CommandContainer;
import com.ghostchu.quickshop.api.event.*;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.compatibility.towny.command.NationCommand;
import com.ghostchu.quickshop.compatibility.towny.command.TownCommand;
import com.ghostchu.quickshop.compatibility.towny.compat.UuidConversion;
import com.ghostchu.quickshop.compatibility.towny.compat.essentials.EssentialsConversion;
import com.ghostchu.quickshop.compatibility.towny.compat.general.GeneralConversion;
import com.ghostchu.quickshop.compatibility.towny.compat.gringotts.towny.GringottsTownyConversion;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.PlotClearEvent;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import com.palmergames.bukkit.towny.event.town.TownUnclaimEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.utils.ShopPlotUtil;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class Main extends CompatibilityModule implements Listener {
    @Getter
    private QuickShopAPI api;
    private List<TownyFlags> createFlags;
    private List<TownyFlags> tradeFlags;
    private boolean whiteList;
    @Getter
    private TownyMaterialPriceLimiter priceLimiter;
    @Getter
    private UuidConversion uuidConversion;

    @EventHandler(ignoreCancelled = true)
    public void onCreation(ShopCreateEvent event) {
        if (isWorldIgnored(event.getShop().getLocation().getWorld())) {
            return;
        }
        Optional<Component> component = checkFlags(event.getPlayer(), event.getShop().getLocation(), this.createFlags);
        component.ifPresent(value -> event.setCancelled(true, value));
    }

    private boolean isWorldIgnored(World world) {
        if (getConfig().getBoolean("ignore-disabled-worlds", false)) {
            return !TownyAPI.getInstance().isTownyWorld(world);
        }
        return false;
    }

    private Optional<Component> checkFlags(@NotNull Player player, @NotNull Location location, @NotNull List<TownyFlags> flags) {
        if (isWorldIgnored(location.getWorld())) {
            return Optional.empty();
        }
        if (!whiteList) {
            return Optional.empty();
        }
        for (TownyFlags flag : flags) {
            switch (flag) {
                case OWN:
                    if (!ShopPlotUtil.doesPlayerOwnShopPlot(player, location)) {
                        return Optional.of(getApi().getTextManager().of(player, "addon.towny.flags.own").forLocale());
                    }
                    break;
                case MODIFY:
                    if (!ShopPlotUtil.doesPlayerHaveAbilityToEditShopPlot(player, location)) {
                        return Optional.of(getApi().getTextManager().of(player, "addon.towny.flags.modify").forLocale());
                    }
                    break;
                case SHOPTYPE:
                    if (!ShopPlotUtil.isShopPlot(location)) {
                        return Optional.of(getApi().getTextManager().of(player, "addon.towny.flags.shop-type").forLocale());
                    }
                default:
                    // Ignore
            }
        }
        return Optional.empty();
    }

    @Override
    public void onDisable() {
        api.getCommandManager().unregisterCmd("town");
        api.getCommandManager().unregisterCmd("nation");
        super.onDisable();
    }

    @Override
    public void init() {
        performConfigurationUpgrade();
        api = QuickShopAPI.getInstance();
        createFlags = TownyFlags.deserialize(getConfig().getStringList("create"));
        tradeFlags = TownyFlags.deserialize(getConfig().getStringList("trade"));
        whiteList = getConfig().getBoolean("whitelist-mode");
        priceLimiter = new TownyMaterialPriceLimiter(Objects.requireNonNull(getConfig().getConfigurationSection("bank-mode.item-list")), getConfig().getDouble("bank-mode.extra-percent", 0.1));
        api.getCommandManager().registerCmd(CommandContainer.builder()
                .prefix("town")
                .permission("quickshop.addon.towny.town")
                .description((locale) -> api.getTextManager().of("addon.towny.commands.town").forLocale(locale))
                .executor(new TownCommand(this))
                .build());
        api.getCommandManager().registerCmd(CommandContainer.builder()
                .prefix("nation")
                .permission("quickshop.addon.towny.nation")
                .description((locale) -> api.getTextManager().of("addon.towny.commands.nation").forLocale(locale))
                .executor(new NationCommand(this))
                .build());
        uuidConversion = switch (getConfig().getInt("uuid-conversion", 0)) {
            case 1 -> new EssentialsConversion();
            case 2 -> new GringottsTownyConversion();
            default -> new GeneralConversion();
        };
        reflectChanges();
    }

    private void performConfigurationUpgrade() {
        if (getConfig().getInt("config-version", 1) == 1) {
            boolean permissionOverride = getConfig().getBoolean("allow-permission-override", true);
            getConfig().set("allow-mayor-permission-override", permissionOverride);
            getConfig().set("allow-king-permission-override", permissionOverride);
            getConfig().set("allow-permission-override", null);
            getConfig().set("config-version", 2);
            saveConfig();
        }
    }

    private void reflectChanges() {
        if (getConfig().getBoolean("bank-mode.enable")) {
            getLogger().info("Scanning and reflecting configuration changes...");
            long startTime = System.currentTimeMillis();
            for (Shop shop : getApi().getShopManager().getAllShops()) {
                if (TownyShopUtil.getShopNation(shop) != null || TownyShopUtil.getShopTown(shop) != null) {
                    Double price = priceLimiter.getPrice(shop.getItem().getType(), shop.isSelling());
                    if (price == null) {
                        shop.delete();
                        recordDeletion(null, shop, "Towny settings disallowed this item as town/nation shop anymore");
                        continue;
                    }
                    if (shop.isStackingShop()) {
                        shop.setPrice(price * shop.getShopStackingAmount());
                    } else {
                        shop.setPrice(price);
                    }
                }
            }
            getLogger().info("Finished to scan shops, used " + (System.currentTimeMillis() - startTime) + "ms");
        }
    }

    @EventHandler
    public void onPlayerLeave(TownRemoveResidentEvent event) {
        if (isWorldIgnored(event.getTown().getWorld())) {
            return;
        }
        if (!getConfig().getBoolean("delete-shop-on-resident-leave", false)) {
            return;
        }
        Util.mainThreadRun(() -> purgeShops(event.getTown().getTownBlocks(), event.getResident().getUUID(), null, "Town removed a resident"));
    }

    public void purgeShops(@NotNull Collection<TownBlock> worldCoords, @Nullable UUID owner, @Nullable UUID deleter, @NotNull String reason) {
        for (TownBlock townBlock : worldCoords) {
            purgeShops(townBlock.getWorldCoord(), owner, deleter, reason);
        }
    }

    public void purgeShops(@NotNull WorldCoord worldCoord, @Nullable UUID owner, @Nullable UUID deleter, @NotNull String reason) {
        //Getting all shop with world-chunk-shop mapping
        for (Shop shop : api.getShopManager().getAllShops()) {
            if (!Objects.equals(shop.getLocation().getWorld(), worldCoord.getBukkitWorld())) {
                continue;
            }
            if (WorldCoord.parseWorldCoord(shop.getLocation()).equals(worldCoord)) {
                if (owner != null && shop.getOwner().equals(owner)) {
                    recordDeletion(deleter, shop, reason);
                    shop.delete();
                }
            }
        }
    }

    @EventHandler
    public void onPlotClear(PlotClearEvent event) {
        if (isWorldIgnored(event.getTownBlock().getWorldCoord().getBukkitWorld())) {
            return;
        }
        if (!getConfig().getBoolean("delete-shop-on-plot-clear", false)) {
            return;
        }
        Util.mainThreadRun(() -> purgeShops(event.getTownBlock().getWorldCoord(), null, null, "Plot cleared"));
    }

    @EventHandler
    public void onPlotUnclaim(TownUnclaimEvent event) {
        if (isWorldIgnored(event.getWorldCoord().getBukkitWorld())) {
            return;
        }
        if (!getConfig().getBoolean("delete-shop-on-plot-unclaimed")) {
            return;
        }
        Util.mainThreadRun(() -> purgeShops(event.getWorldCoord(), null, null, "Town Unclaimed"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPreCreation(ShopPreCreateEvent event) {
        if (isWorldIgnored(event.getLocation().getWorld())) {
            return;
        }
        Optional<Component> component = checkFlags(event.getPlayer(), event.getLocation(), this.createFlags);
        component.ifPresent(value -> event.setCancelled(true, value));
    }

    @EventHandler(ignoreCancelled = true)
    public void onTrading(ShopPurchaseEvent event) {
        if (isWorldIgnored(event.getShop().getLocation().getWorld())) {
            return;
        }
        Optional<Component> component = checkFlags(event.getPlayer(), event.getShop().getLocation(), this.tradeFlags);
        component.ifPresent(value -> event.setCancelled(true, value));
    }

    @EventHandler(ignoreCancelled = true)
    public void ownerDisplayOverride(ShopOwnerNameGettingEvent event) {
        if (!getConfig().getBoolean("allow-owner-name-override", true)) {
            return;
        }
        Shop shop = event.getShop();
        // Town name override check
        Town town = TownyShopUtil.getShopTown(shop);
        if (town != null) {
            event.setName(LegacyComponentSerializer.legacySection().deserialize(town.getName()));
            return;
        }
        // Nation name override check
        Nation nation = TownyShopUtil.getShopNation(shop);
        if (nation != null) {
            event.setName(LegacyComponentSerializer.legacySection().deserialize(nation.getName()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void permissionOverride(ShopAuthorizeCalculateEvent event) {
        Location shopLoc = event.getShop().getLocation();
        if (isWorldIgnored(shopLoc.getWorld())) {
            return;
        }
        Town town = TownyAPI.getInstance().getTown(shopLoc);
        if (town == null) {
            return;
        }
        if (town.getMayor().getUUID().equals(event.getAuthorizer())) {
            if (getConfig().getBoolean("allow-mayor-permission-override", true)) {
                if (event.getNamespace().equals(QuickShop.getInstance()) && event.getPermission().equals(BuiltInShopPermission.DELETE.getRawNode())) {
                    event.setResult(true);
                    return;
                }
            }
        }
        try {
            Nation nation = town.getNation();
            if (nation.getKing().getUUID().equals(event.getAuthorizer())) {
                if (getConfig().getBoolean("allow-king-permission-override", true)) {
                    if (event.getNamespace().equals(QuickShop.getInstance()) && event.getPermission().equals(BuiltInShopPermission.DELETE.getRawNode())) {
                        event.setResult(true);
                    }
                }
            }
        } catch (NotRegisteredException ignored) {

        }
    }

    @EventHandler(ignoreCancelled = true)
    public void shopItemChanged(ShopItemChangeEvent event) {
        Shop shop = event.getShop();
        if (TownyShopUtil.getShopNation(shop) != null || TownyShopUtil.getShopTown(shop) != null) {
            event.setCancelled(true, api.getTextManager().of("addon.towny.operation-disabled-due-shop-status").forLocale());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void shopItemChanged(ShopOwnershipTransferEvent event) {
        Shop shop = event.getShop();
        if (TownyShopUtil.getShopNation(shop) != null || TownyShopUtil.getShopTown(shop) != null) {
            event.setCancelled(true, api.getTextManager().of("addon.towny.operation-disabled-due-shop-status").forLocale());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void shopPriceChanged(ShopPriceChangeEvent event) {
        Shop shop = event.getShop();
        if (TownyShopUtil.getShopNation(shop) != null || TownyShopUtil.getShopTown(shop) != null) {
            event.setCancelled(true, api.getTextManager().of("addon.towny.operation-disabled-due-shop-status").forLocale());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void shopTaxAccountChanged(ShopTaxAccountChangeEvent event) {
        Shop shop = event.getShop();
        if (TownyShopUtil.getShopNation(shop) != null || TownyShopUtil.getShopTown(shop) != null) {
            event.setCancelled(true, api.getTextManager().of("addon.towny.operation-disabled-due-shop-status").forLocale());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void shopTypeChanged(ShopTypeChangeEvent event) {
        Shop shop = event.getShop();
        if (TownyShopUtil.getShopNation(shop) != null || TownyShopUtil.getShopTown(shop) != null) {
            event.setCancelled(true, api.getTextManager().of("addon.towny.operation-disabled-due-shop-status").forLocale());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void taxesAccountOverride(ShopTaxAccountGettingEvent event) {
        if (!getConfig().getBoolean("taxes-to-town", true)) {
            return;
        }
        Shop shop = event.getShop();
        // Send tax to server if shop is a town shop or nation shop.
        if (TownyShopUtil.getShopTown(shop) != null || TownyShopUtil.getShopNation(shop) != null) {
            return;
        }
        // Modify tax account to town account if they aren't town shop or nation shop but inside town or nation
        Town town = TownyAPI.getInstance().getTown(shop.getLocation());
        if (town != null) {
            UUID uuid = QuickShop.getInstance().getPlayerFinder().name2Uuid(town.getAccount().getName());
            if (uuid == null) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(town.getAccount().getName());
                uuid = player.getUniqueId();
            }
            event.setTaxAccount(uuid);
            Log.debug("Tax account override: " + uuid + " = " + town.getAccount().getName());
        }
    }
}
