package com.ghostchu.quickshop.compatibility.towny;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.command.CommandContainer;
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.details.ShopOwnershipTransferEvent;
import com.ghostchu.quickshop.api.event.details.ShopPriceChangeEvent;
import com.ghostchu.quickshop.api.event.economy.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.event.economy.ShopTaxAccountChangeEvent;
import com.ghostchu.quickshop.api.event.economy.ShopTaxAccountGettingEvent;
import com.ghostchu.quickshop.api.event.modification.ShopAuthorizeCalculateEvent;
import com.ghostchu.quickshop.api.event.modification.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.modification.ShopPreCreateEvent;
import com.ghostchu.quickshop.api.event.settings.type.ShopItemEvent;
import com.ghostchu.quickshop.api.event.settings.type.ShopOwnerNameEvent;
import com.ghostchu.quickshop.api.event.settings.type.ShopTypeEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.compatibility.towny.command.NationCommand;
import com.ghostchu.quickshop.compatibility.towny.command.TownCommand;
import com.ghostchu.quickshop.compatibility.towny.compat.UuidConversion;
import com.ghostchu.quickshop.compatibility.towny.compat.essentials.EssentialsConversion;
import com.ghostchu.quickshop.compatibility.towny.compat.general.GeneralConversion;
import com.ghostchu.quickshop.compatibility.towny.compat.gringotts.towny.GringottsTownyConversion;
import com.ghostchu.quickshop.compatibility.towny.compat.tne.TNEConversion;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.PlotClearEvent;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import com.palmergames.bukkit.towny.event.executors.TownyActionEventExecutor;
import com.palmergames.bukkit.towny.event.town.TownKickEvent;
import com.palmergames.bukkit.towny.event.town.TownLeaveEvent;
import com.palmergames.bukkit.towny.event.town.TownRuinedEvent;
import com.palmergames.bukkit.towny.event.town.TownUnclaimEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.utils.ShopPlotUtil;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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
  public void onCreation(final ShopCreateEvent event) {

    if(isWorldIgnored(event.getShop().getLocation().getWorld())) {
      return;
    }
    event.getCreator().getBukkitPlayer().ifPresent(player->{
      final Optional<Component> component = checkFlags(player, event.getShop().getLocation(), this.createFlags);
      component.ifPresent(value->event.setCancelled(true, value));
    });
  }

  private boolean isWorldIgnored(final World world) {

    if(getConfig().getBoolean("ignore-disabled-worlds", false)) {
      return !TownyAPI.getInstance().isTownyWorld(world);
    }
    return false;
  }

  private Optional<Component> checkFlags(@NotNull final Player player, @NotNull final Location location, @NotNull final List<TownyFlags> flags) {

    if(isWorldIgnored(location.getWorld())) {
      return Optional.empty();
    }
    if(!whiteList) {
      return Optional.empty();
    }
    for(final TownyFlags flag : flags) {
      switch(flag) {
        case OWN:
          if(!doesPlayerOwnShopPlot(player, location)) {
            return Optional.of(getApi().getTextManager().of(player, "addon.towny.flags.own").forLocale());
          }
          break;
        case MODIFY:
          if(!TownyActionEventExecutor.canBuild(player, location, Material.DIRT)) {
            return Optional.of(getApi().getTextManager().of(player, "addon.towny.flags.modify").forLocale());
          }
          break;
        case SHOPTYPE:
          if(!ShopPlotUtil.isShopPlot(location)) {
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
                                                .description((locale)->api.getTextManager().of("addon.towny.commands.town").forLocale(locale))
                                                .executor(new TownCommand(this))
                                                .build());
    api.getCommandManager().registerCmd(CommandContainer.builder()
                                                .prefix("nation")
                                                .permission("quickshop.addon.towny.nation")
                                                .description((locale)->api.getTextManager().of("addon.towny.commands.nation").forLocale(locale))
                                                .executor(new NationCommand(this))
                                                .build());
    uuidConversion = switch(getConfig().getInt("uuid-conversion", 0)) {
      case 1 -> new EssentialsConversion();
      case 2 -> new GringottsTownyConversion();
      case 3 -> new TNEConversion();
      default -> new GeneralConversion();
    };
    reflectChanges();
  }

  private void performConfigurationUpgrade() {

    if(getConfig().getInt("config-version", 1) == 1) {
      final boolean permissionOverride = getConfig().getBoolean("allow-permission-override", true);
      getConfig().set("allow-mayor-permission-override", permissionOverride);
      getConfig().set("allow-king-permission-override", permissionOverride);
      getConfig().set("allow-permission-override", null);
      getConfig().set("config-version", 2);
      saveConfig();
    }
  }

  private void reflectChanges() {

    if(getConfig().getBoolean("bank-mode.enable")) {
      getLogger().info("Scanning and reflecting configuration changes...");
      final long startTime = System.currentTimeMillis();
      for(final Shop shop : getApi().getShopManager().getAllShops()) {
        if(TownyShopUtil.getShopNation(shop) != null || TownyShopUtil.getShopTown(shop) != null) {
          final Double price = priceLimiter.getPrice(shop.getItem().getType(), shop.isSelling());
          if(price == null) {
            getApi().getShopManager().deleteShop(shop);
            recordDeletion(null, shop, "Towny settings disallowed this item as town/nation shop anymore");
            continue;
          }
          if(shop.isStackingShop()) {
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
  public void onTownLeave(final TownLeaveEvent event) {

    if(isWorldIgnored(event.getTown().getWorld())) {
      return;
    }
    if(!getConfig().getBoolean("delete-shop-on-resident-leave", false)) {
      return;
    }
    Util.mainThreadRun(()->purgeShops(event.getTown().getTownBlocks(), event.getResident().getUUID(), null, "Resident left town", false));
  }

  @EventHandler
  public void onTownKick(final TownKickEvent event) {

    if(isWorldIgnored(event.getTown().getWorld())) {
      return;
    }
    if(!getConfig().getBoolean("delete-shop-on-resident-leave", false)) {
      return;
    }
    Util.mainThreadRun(()->purgeShops(event.getTown().getTownBlocks(), event.getKickedResident().getUUID(), null, "Town kicked a resident", false));
  }

  @EventHandler
  public void onPlayerLeave(final TownRemoveResidentEvent event) {

    if(isWorldIgnored(event.getTown().getWorld())) {
      return;
    }
    if(!getConfig().getBoolean("delete-shop-on-resident-leave", false)) {
      return;
    }
    Util.mainThreadRun(()->purgeShops(event.getTown().getTownBlocks(), event.getResident().getUUID(), null, "Town removed a resident", false));
  }

  public void purgeShops(@NotNull final Collection<TownBlock> worldCoords, @Nullable final UUID owner, @Nullable final UUID deleter, @NotNull final String reason) {

    purgeShops(worldCoords, owner, deleter, reason, false);
  }

  public void purgeShops(@NotNull final Collection<TownBlock> worldCoords, @Nullable final UUID owner, @Nullable final UUID deleter, @NotNull final String reason, final boolean overrideOwner) {

    for(final TownBlock townBlock : worldCoords) {
      purgeShops(townBlock.getWorldCoord(), owner, deleter, reason, overrideOwner);
    }
  }

  public void purgeShops(@NotNull final WorldCoord worldCoord, @Nullable final UUID owner, @Nullable final UUID deleter, @NotNull final String reason, final boolean overrideOwner) {
    //Getting all shop with world-chunk-shop mapping
    for(final Shop shop : api.getShopManager().getAllShops()) {
      if(!Objects.equals(shop.getLocation().getWorld(), worldCoord.getBukkitWorld())) {
        continue;
      }
      if(WorldCoord.parseWorldCoord(shop.getLocation()).equals(worldCoord)) {
        if(overrideOwner || owner != null && owner.equals(shop.getOwner().getUniqueId())) {
          recordDeletion(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "Towny", false), shop, reason);
          getApi().getShopManager().deleteShop(shop);
        }
      }
    }
  }

  @EventHandler
  public void onPlotClear(final PlotClearEvent event) {

    if(isWorldIgnored(event.getTownBlock().getWorldCoord().getBukkitWorld())) {
      return;
    }
    if(!getConfig().getBoolean("delete-shop-on-plot-clear", false)) {
      return;
    }
    Util.mainThreadRun(()->purgeShops(event.getTownBlock().getWorldCoord(), null, null, "Plot cleared", true));
  }

  @EventHandler
  public void onRuin(final TownRuinedEvent event) {

    if(isWorldIgnored(event.getTown().getWorld())) {
      return;
    }
    if(!getConfig().getBoolean("delete-shop-on-town-ruin")) {
      return;
    }
    Util.mainThreadRun(()->purgeShops(event.getTown().getTownBlocks(), null, null, "Town ruined", true));
  }

  @EventHandler
  public void onPlotUnclaim(final TownUnclaimEvent event) {

    if(isWorldIgnored(event.getWorldCoord().getBukkitWorld())) {
      return;
    }
    if(!getConfig().getBoolean("delete-shop-on-plot-unclaimed")) {
      return;
    }
    Util.mainThreadRun(()->purgeShops(event.getWorldCoord(), null, null, "Town Unclaimed", true));
  }

  @EventHandler(ignoreCancelled = true)
  public void onPreCreation(final ShopPreCreateEvent event) {

    if(isWorldIgnored(event.getLocation().getWorld())) {
      return;
    }
    event.getCreator().getBukkitPlayer().ifPresent(player->{
      final Optional<Component> component = checkFlags(player, event.getLocation(), this.createFlags);
      component.ifPresent(value->event.setCancelled(true, value));
    });
  }

  @EventHandler(ignoreCancelled = true)
  public void onTrading(final ShopPurchaseEvent event) {

    if(isWorldIgnored(event.getShop().getLocation().getWorld())) {
      return;
    }
    event.getPurchaser().getBukkitPlayer().ifPresent(player->{
      final Optional<Component> component = checkFlags(player, event.getShop().getLocation(), this.tradeFlags);
      component.ifPresent(value->event.setCancelled(true, value));
    });
  }

  @EventHandler(ignoreCancelled = true)
  public void ownerDisplayOverride(final ShopOwnerNameEvent event) {

    if(!event.isPhase(Phase.RETRIEVE)) {

      return;
    }

    if(!getConfig().getBoolean("allow-owner-name-override", true)) {
      return;
    }
    final Shop shop = event.shop();

    // Town name override check
    final Town town = TownyShopUtil.getShopTown(shop);
    if(town != null) {
      event.updated(LegacyComponentSerializer.legacySection().deserialize(town.getName()));
      return;
    }
    // Nation name override check
    final Nation nation = TownyShopUtil.getShopNation(shop);
    if(nation != null) {
      event.updated(LegacyComponentSerializer.legacySection().deserialize(nation.getName()));
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void permissionOverride(final ShopAuthorizeCalculateEvent event) {

    final Location shopLoc = event.getShop().getLocation();
    if(isWorldIgnored(shopLoc.getWorld())) {
      return;
    }
    final Town town = TownyAPI.getInstance().getTown(shopLoc);
    if(town == null) {
      return;
    }
    if(town.getMayor().getUUID().equals(event.getAuthorizer())) {
      if(getConfig().getBoolean("allow-mayor-permission-override", true)) {
        if(event.getNamespace().equals(QuickShop.getInstance().getJavaPlugin()) && event.getPermission().equals(BuiltInShopPermission.DELETE.getRawNode())) {
          event.setResult(true);
          return;
        }
      }
    }
    try {
      final Nation nation = town.getNation();
      if(nation.getKing().getUUID().equals(event.getAuthorizer())) {
        if(getConfig().getBoolean("allow-king-permission-override", true)) {
          if(event.getNamespace().equals(QuickShop.getInstance().getJavaPlugin()) && event.getPermission().equals(BuiltInShopPermission.DELETE.getRawNode())) {
            event.setResult(true);
          }
        }
      }
    } catch(final NotRegisteredException ignored) {

    }
  }

  @EventHandler(ignoreCancelled = true)
  public void shopItemChanged(final ShopItemEvent event) {

    if(!event.isPhase(Phase.MAIN)) {

      return;
    }

    final Shop shop = event.shop();
    if(TownyShopUtil.getShopNation(shop) != null || TownyShopUtil.getShopTown(shop) != null) {

      event.setCancelled(true, api.getTextManager().of("addon.towny.operation-disabled-due-shop-status").forLocale());
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void shopItemChanged(final ShopOwnershipTransferEvent event) {

    final Shop shop = event.getShop();
    if(TownyShopUtil.getShopNation(shop) != null || TownyShopUtil.getShopTown(shop) != null) {
      event.setCancelled(true, api.getTextManager().of("addon.towny.operation-disabled-due-shop-status").forLocale());
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void shopPriceChanged(final ShopPriceChangeEvent event) {

    final Shop shop = event.getShop();
    if(TownyShopUtil.getShopNation(shop) != null || TownyShopUtil.getShopTown(shop) != null) {
      event.setCancelled(true, api.getTextManager().of("addon.towny.operation-disabled-due-shop-status").forLocale());
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void shopTaxAccountChanged(final ShopTaxAccountChangeEvent event) {

    final Shop shop = event.getShop();
    if(TownyShopUtil.getShopNation(shop) != null || TownyShopUtil.getShopTown(shop) != null) {
      event.setCancelled(true, api.getTextManager().of("addon.towny.operation-disabled-due-shop-status").forLocale());
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void shopTypeChanged(final ShopTypeEvent event) {

    if(!event.isPhase(Phase.MAIN)) {

      return;
    }

    final Shop shop = event.shop();
    if(TownyShopUtil.getShopNation(shop) != null || TownyShopUtil.getShopTown(shop) != null) {
      event.setCancelled(true, api.getTextManager().of("addon.towny.operation-disabled-due-shop-status").forLocale());
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void taxesAccountOverride(final ShopTaxAccountGettingEvent event) {

    if(!getConfig().getBoolean("taxes-to-town", true)) {
      return;
    }
    final Shop shop = event.getShop();
    // Send tax to server if shop is a town shop or nation shop.
    if(TownyShopUtil.getShopTown(shop) != null || TownyShopUtil.getShopNation(shop) != null) {
      return;
    }
    // Modify tax account to town account if they aren't town shop or nation shop but inside town or nation
    final Town town = TownyAPI.getInstance().getTown(shop.getLocation());
    if(town != null) {
      UUID uuid = QuickShop.getInstance().getPlayerFinder().name2Uuid(town.getAccount().getName());
      if(uuid == null) {
        final OfflinePlayer player = Bukkit.getOfflinePlayer(town.getAccount().getName());
        uuid = player.getUniqueId();
      }
      final QUser taxUUID = QUserImpl.createFullFilled(uuid, town.getAccount().getName(), false);
      event.setTaxAccount(taxUUID);
      Log.debug("Tax account override: " + uuid + " = " + town.getAccount().getName());
    }
  }

  private boolean doesPlayerOwnShopPlot(final Player player, final Location location) {
    final TownBlock townBlock = TownyAPI.getInstance().getTownBlock(location);

    if(townBlock != null && townBlock.hasResident()) {
      final Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
      if(resident != null) {

        final Resident owner = townBlock.getResidentOrNull();

        return owner != null && townBlock.getResidentOrNull().equals(resident);
      }
    }

    return false;
  }
}
