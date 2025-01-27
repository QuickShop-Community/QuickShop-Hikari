package com.ghostchu.quickshop.listener;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.economy.AbstractEconomy;
import com.ghostchu.quickshop.api.event.modification.ShopPreCreateEvent;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Info;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopAction;
import com.ghostchu.quickshop.api.shop.ShopManager;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.menu.ShopKeeperMenu;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.shop.InteractionController;
import com.ghostchu.quickshop.shop.SimpleInfo;
import com.ghostchu.quickshop.shop.datatype.ShopSignPersistentDataType;
import com.ghostchu.quickshop.shop.inventory.BukkitInventoryWrapper;
import com.ghostchu.quickshop.util.ExpiringSet;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.PackageUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import net.tnemc.menu.core.compatibility.MenuPlayer;
import net.tnemc.menu.core.manager.MenuManager;
import net.tnemc.menu.core.viewer.MenuViewer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLocaleChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerListener extends AbstractQSListener {

  private final ExpiringSet<UUID> adventureWorkaround = new ExpiringSet<>(1, TimeUnit.SECONDS);
  private final ExpiringSet<UUID> rateLimit = new ExpiringSet<>(125, TimeUnit.MILLISECONDS);

  public PlayerListener(final QuickShop plugin) {

    super(plugin);
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onAdventureClick(final PlayerAnimationEvent event) {

    if(event.getPlayer().getGameMode() != GameMode.ADVENTURE) {
      return;
    }
    // ----Adventure dupe click workaround start----
    if(adventureWorkaround.contains(event.getPlayer().getUniqueId())) {
      return;
    }
    adventureWorkaround.add(event.getPlayer().getUniqueId());
    // ----Adventure dupe click workaround end----
    final Block focused = event.getPlayer().getTargetBlockExact(5);
    if(focused != null) {
      final PlayerInteractEvent interactEvent
              = new PlayerInteractEvent(event.getPlayer(),
                                        focused.getType() == Material.AIR? Action.LEFT_CLICK_AIR : Action.LEFT_CLICK_BLOCK,
                                        event.getPlayer().getInventory().getItemInMainHand(),
                                        focused,
                                        event.getPlayer().getFacing().getOppositeFace());
      onClick(interactEvent);
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onClick(final PlayerInteractEvent e) {
    // Deprecated: Can use useInteractedBlock() == Result.DENY instead
    if(e.isCancelled() && PackageUtil.parsePackageProperly("ignoreCancelledInteractEvent").asBoolean(true)) {
      return;
    }
    if(e.getHand() != EquipmentSlot.HAND) {
      return;
    }

    // ----Adventure dupe click workaround start----
    if(e.getPlayer().getGameMode() == GameMode.ADVENTURE) {
      if(!adventureWorkaround.contains(e.getPlayer().getUniqueId())) {
        return;
      }
      adventureWorkaround.add(e.getPlayer().getUniqueId());
    }
    // ----Adventure dupe click workaround end----
    if(rateLimit.contains(e.getPlayer().getUniqueId())) {
      return;
    }
    rateLimit.add(e.getPlayer().getUniqueId());

    final Map.Entry<Shop, ClickType> shopSearched = searchShop(e.getClickedBlock(), e.getPlayer());

    if(shopSearched.getKey() == null && shopSearched.getValue() == ClickType.AIR) {
      return;
    }

    InteractionController.Interaction interaction = null;
    if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
      if(e.getPlayer().isSneaking()) {
        interaction = shopSearched.getValue() == ClickType.SIGN? InteractionController.Interaction.SNEAKING_RIGHT_CLICK_SIGN : InteractionController.Interaction.SNEAKING_RIGHT_CLICK_SHOPBLOCK;
      } else {
        interaction = shopSearched.getValue() == ClickType.SIGN? InteractionController.Interaction.STANDING_RIGHT_CLICK_SIGN : InteractionController.Interaction.STANDING_RIGHT_CLICK_SHOPBLOCK;
      }
    } else if(e.getAction() == Action.LEFT_CLICK_BLOCK) {
      if(e.getPlayer().isSneaking()) {
        interaction = shopSearched.getValue() == ClickType.SIGN? InteractionController.Interaction.SNEAKING_LEFT_CLICK_SIGN : InteractionController.Interaction.SNEAKING_LEFT_CLICK_SHOPBLOCK;
      } else {
        interaction = shopSearched.getValue() == ClickType.SIGN? InteractionController.Interaction.STANDING_LEFT_CLICK_SIGN : InteractionController.Interaction.STANDING_LEFT_CLICK_SHOPBLOCK;
      }
    }
    if(interaction == null) {
      return;
    }
    if(Util.isDevMode()) {
      Log.debug("Click: " + interaction.name());
      Log.debug("Behavior Mapping: " + plugin.getInteractionController().getBehavior(interaction).name());
    }
    switch(plugin.getInteractionController().getBehavior(interaction)) {
      case CONTROL_PANEL -> {
        if(shopSearched.getKey() != null) {
          openControlPanel(e.getPlayer(), shopSearched.getKey());
          e.setCancelled(true);
          e.setUseInteractedBlock(Event.Result.DENY);
          e.setUseItemInHand(Event.Result.DENY);
        }
      }
      case CONTROL_PANEL_UI -> {
        if(shopSearched.getKey() != null) {
          final MenuViewer viewer = new MenuViewer(e.getPlayer().getUniqueId());
          viewer.addData(ShopKeeperMenu.SHOP_DATA_ID, shopSearched.getKey().getShopId());
          MenuManager.instance().addViewer(viewer);

          final MenuPlayer menuPlayer = QuickShop.getInstance().createMenuPlayer(e.getPlayer());
          MenuManager.instance().open("qs:keeper", 1, menuPlayer);
        }
      }
      case TRADE_UI -> {
        if(shopSearched.getKey() != null) {

          if(shopSearched.getKey().isFrozen()) {
            plugin.text().of(e.getPlayer(), "shop-cannot-trade-when-freezing").send();
            return;
          }

          final MenuViewer viewer = new MenuViewer(e.getPlayer().getUniqueId());
          viewer.addData(ShopKeeperMenu.SHOP_DATA_ID, shopSearched.getKey().getShopId());
          MenuManager.instance().addViewer(viewer);

          final MenuPlayer menuPlayer = QuickShop.getInstance().createMenuPlayer(e.getPlayer());
          MenuManager.instance().open("qs:trade", 1, menuPlayer);
        }
      }
      case TRADE_INTERACTION -> {
        if(shopSearched.getKey() == null) {
          if(e.getItem() != null && createShop(e.getPlayer(), e.getClickedBlock(), e.getBlockFace(), e.getHand(), e.getItem())) {
            e.setCancelled(true);
            e.setUseInteractedBlock(Event.Result.DENY);
            e.setUseItemInHand(Event.Result.DENY);
          }
        } else {

          if(shopSearched.getKey().isFrozen()) {
            plugin.text().of(e.getPlayer(), "shop-cannot-trade-when-freezing").send();
            return;
          }

          if(shopSearched.getKey().isBuying()) {
            if(sellToShop(e.getPlayer(), shopSearched.getKey(), false, false)) {
              e.setCancelled(true);
              e.setUseInteractedBlock(Event.Result.DENY);
              e.setUseItemInHand(Event.Result.DENY);
            }
            break;
          }
          if(shopSearched.getKey().isSelling()) {
            if(buyFromShop(e.getPlayer(), shopSearched.getKey(), false, false)) {
              e.setCancelled(true);
              e.setUseInteractedBlock(Event.Result.DENY);
              e.setUseItemInHand(Event.Result.DENY);
            }
          }
        }
      }
      case TRADE_DIRECT -> {
        if(shopSearched.getKey() == null) {
          return;
        }

        if(shopSearched.getKey().isFrozen()) {
          plugin.text().of(e.getPlayer(), "shop-cannot-trade-when-freezing").send();
          return;
        }
        if(shopSearched.getKey().isBuying()) {
          if(sellToShop(e.getPlayer(), shopSearched.getKey(), true, false)) {
            e.setCancelled(true);
            e.setUseInteractedBlock(Event.Result.DENY);
            e.setUseItemInHand(Event.Result.DENY);
          }
          break;
        }
        if(shopSearched.getKey().isSelling()) {

          if(buyFromShop(e.getPlayer(), shopSearched.getKey(), true, false)) {
            e.setCancelled(true);
            e.setUseInteractedBlock(Event.Result.DENY);
            e.setUseItemInHand(Event.Result.DENY);
          }
        }
      }
      case TRADE_DIRECT_ALL -> {

        if(shopSearched.getKey().isFrozen()) {
          plugin.text().of(e.getPlayer(), "shop-cannot-trade-when-freezing").send();
          return;
        }
        if(shopSearched.getKey().isSelling()) {
          if(buyFromShop(e.getPlayer(), shopSearched.getKey(), true, true)) {
            e.setCancelled(true);
            e.setUseInteractedBlock(Event.Result.DENY);
            e.setUseItemInHand(Event.Result.DENY);
          }
          break;
        }
        if(shopSearched.getKey().isBuying()) {
          if(sellToShop(e.getPlayer(), shopSearched.getKey(), true, true)) {
            e.setCancelled(true);
            e.setUseInteractedBlock(Event.Result.DENY);
            e.setUseItemInHand(Event.Result.DENY);
          }
        }
      }
    }
  }

  @NotNull
  public Map.Entry<@Nullable Shop, @NotNull ClickType> searchShop(@Nullable final Block b, @NotNull final Player p) {

    if(b == null) {
      return new AbstractMap.SimpleEntry<>(null, ClickType.AIR);
    }
    Shop shop = plugin.getShopManager().getShop(b.getLocation());
    // If that wasn't a shop, search nearby shops
    if(shop == null) {
      final Block attached;
      if(Util.isWallSign(b.getType())) {
        attached = Util.getAttached(b);
        if(attached != null) {
          shop = plugin.getShopManager().getShop(attached.getLocation());
          return new AbstractMap.SimpleImmutableEntry<>(shop, ClickType.SIGN);
        }
      } else if(Util.isDoubleChest(b.getBlockData())) {
        attached = Util.getSecondHalf(b);
        if(attached != null) {
          final Shop secondHalfShop = plugin.getShopManager().getShop(attached.getLocation());
          if(secondHalfShop != null && !p.getUniqueId().equals(secondHalfShop.getOwner().getUniqueId())) {
            // If player not the owner of the shop, make him select the second half of the
            // shop
            // Otherwise owner will be able to create new double chest shop
            shop = secondHalfShop;
          }
        }
      }
    }
    return new AbstractMap.SimpleImmutableEntry<>(shop, ClickType.SHOPBLOCK);
  }

  private void openControlPanel(@NotNull final Player p, @NotNull final Shop shop) {

    MsgUtil.sendControlPanelInfo(p, shop);
    this.playClickSound(p);
    shop.onClick(p);
    shop.setSignText(plugin.text().findRelativeLanguages(p));
  }

  public boolean createShop(@NotNull final Player player, @Nullable final Block block, @NotNull final BlockFace blockFace, @NotNull final EquipmentSlot hand, @NotNull final ItemStack item) {

    final QUser qUser = QUserImpl.createFullFilled(player);
    if(block == null) {
      return false; // This shouldn't happen because we have checked action type.
    }
    if(player.getGameMode() != GameMode.SURVIVAL) {
      return false; // Only survival :)
    }

    final ItemStack stack = item.clone();
    if(stack.getType().isAir()) {
      return false; // Air cannot be used for trade
    }
    if(!Util.canBeShop(block)) {
      return false;
    }
    if(plugin.getConfig().getBoolean("disable-quick-create")) {
      return false;
    }
    if(plugin.getConfig().getBoolean("shop.disable-quick-create")) {
      return false;
    }

    ShopAction action = null;
    if(plugin.perm().hasPermission(player, "quickshop.create.sell")) {
      action = ShopAction.CREATE_SELL;
    } else if(plugin.perm().hasPermission(player, "quickshop.create.buy")) {
      action = ShopAction.CREATE_BUY;
    }
    if(action == null) {
      // No permission
      return false;
    }
    // Double chest creation permission check
    if(Util.isDoubleChest(block.getBlockData()) &&
       !plugin.perm().hasPermission(player, "quickshop.create.double")) {
      plugin.text().of(player, "no-double-chests").send();
      return false;
    }
    // Blacklist check
    if(plugin.getShopItemBlackList().isBlacklisted(stack)
       && !plugin.perm()
            .hasPermission(player, "quickshop.bypass." + stack.getType().name())) {
      plugin.text().of(player, "blacklisted-item").send();
      return false;
    }
    // Check if had enderchest shop creation permission
    if(block.getType() == Material.ENDER_CHEST
       && !plugin.perm().hasPermission(player, "quickshop.create.enderchest")) {
      return false;
    }
    // Check if block is a wall sign
    if(Util.isWallSign(block.getType())) {
      return false;
    }
    // Finds out where the sign should be placed for the shop
    final Block last;
    if(Util.getVerticalFacing().contains(blockFace)) {
      last = block.getRelative(blockFace);
    } else {
      final Location playerLocation = player.getLocation();
      final double x = playerLocation.getX() - block.getX();
      final double z = playerLocation.getZ() - block.getZ();
      if(Math.abs(x) > Math.abs(z)) {
        if(x > 0) {
          last = block.getRelative(BlockFace.EAST);
        } else {
          last = block.getRelative(BlockFace.WEST);
        }
      } else {
        if(z > 0) {
          last = block.getRelative(BlockFace.SOUTH);
        } else {
          last = block.getRelative(BlockFace.NORTH);
        }
      }
    }
    // Send creation menu.
    final SimpleInfo info = new SimpleInfo(block.getLocation(), action, stack, last, false);
    final ShopPreCreateEvent spce = new ShopPreCreateEvent(qUser, block.getLocation());
    if(Util.fireCancellableEvent(spce)) {
      Log.debug("ShopPreCreateEvent cancelled");
      return false;
    }
    plugin.getShopManager().getInteractiveManager().put(player.getUniqueId(), info);
    plugin.text().of(player, "how-much-to-trade-for", Util.getItemStackName(stack),
                     plugin.isAllowStack() &&
                     plugin.perm().hasPermission(player, "quickshop.create.stacks")
                     ? stack.getAmount() : 1).send();
    return false;
  }

  public boolean sellToShop(@NotNull final Player p, @Nullable final Shop shop, final boolean direct, final boolean all) {

    if(shop == null) {
      return false;
    }
    if(!shop.isBuying()) {
      return false;
    }
    if(!plugin.perm().hasPermission(p, "quickshop.use")) {
      return false;
    }
    plugin.getShopManager().sendShopInfo(p, shop);
    shop.setSignText(plugin.text().findRelativeLanguages(p));
    this.playClickSound(p);
    shop.onClick(p);
    if(shop.getRemainingSpace() == 0) {
      plugin.text().of(p, "purchase-out-of-space", shop.ownerName()).send();
      return true;
    }
    final AbstractEconomy eco = plugin.getEconomy();
    final double price = shop.getPrice();
    final Inventory playerInventory = p.getInventory();
    final String tradeAllWord = plugin.getConfig().getString("shop.word-for-trade-all-items", "all");
    final double ownerBalance = eco.getBalance(shop.getOwner(), shop.getLocation().getWorld(), shop.getCurrency());
    final int items = getPlayerCanSell(shop, ownerBalance, price, new BukkitInventoryWrapper(playerInventory));
    final ShopManager.InteractiveManager actions = plugin.getShopManager().getInteractiveManager();
    if(shop.playerAuthorize(p.getUniqueId(), BuiltInShopPermission.PURCHASE)
       || plugin.perm().hasPermission(p, "quickshop.other.use")) {
      final Info info = new SimpleInfo(shop.getLocation(), ShopAction.PURCHASE_SELL, null, null, shop, false);
      actions.put(p.getUniqueId(), info);
      if(!direct) {
        if(shop.isStackingShop()) {
          plugin.text().of(p, "how-many-sell-stack", shop.getItem().getAmount(), items, tradeAllWord).send();
        } else {
          plugin.text().of(p, "how-many-sell", items, tradeAllWord).send();
        }
      } else {
        final int arg;
        if(all) {
          arg = buyingShopAllCalc(eco, shop, p);
        } else {
          arg = shop.getShopStackingAmount();
        }
        if(arg == 0) {
          return true;
        }
        plugin.getShopManager().actionBuying(p, new BukkitInventoryWrapper(p.getInventory()), eco, info, shop, arg);
      }
    }
    return true;
  }

  public boolean buyFromShop(@NotNull final Player p, @Nullable final Shop shop, final boolean direct, final boolean all) {

    if(shop == null) {
      return false;
    }
    if(!shop.isSelling()) {
      return false;
    }

    final AbstractEconomy eco = plugin.getEconomy();
    final int arg;
    if(all) {
      arg = sellingShopAllCalc(eco, shop, p);
    } else {
      arg = shop.getShopStackingAmount();
    }

    if(arg == 0) {
      return true;
    }
    return buyFromShop(p, shop, arg, direct, all);
  }

  public boolean buyFromShop(@NotNull final Player p, @Nullable final Shop shop, final int arg, final boolean direct, final boolean all) {

    if(shop == null) {
      return false;
    }
    if(!shop.isSelling()) {
      return false;
    }
    if(!plugin.perm().hasPermission(p, "quickshop.use")) {
      return false;
    }
    plugin.getShopManager().sendShopInfo(p, shop);
    shop.setSignText(plugin.text().findRelativeLanguages(p));
    if(shop.getRemainingStock() == 0) {
      plugin.text().of(p, "purchase-out-of-stock", shop.ownerName()).send();
      return true;
    }
    this.playClickSound(p);
    shop.onClick(p);
    final AbstractEconomy eco = plugin.getEconomy();
    final double price = shop.getPrice();
    final Inventory playerInventory = p.getInventory();
    final String tradeAllWord = plugin.getConfig().getString("shop.word-for-trade-all-items", "all");
    final ShopManager.InteractiveManager actions = plugin.getShopManager().getInteractiveManager();
    final double traderBalance = eco.getBalance(QUserImpl.createFullFilled(p), shop.getLocation().getWorld(), shop.getCurrency());
    final int itemAmount = getPlayerCanBuy(shop, traderBalance, price, new BukkitInventoryWrapper(playerInventory));
    if(shop.playerAuthorize(p.getUniqueId(), BuiltInShopPermission.PURCHASE)
       || plugin.perm().hasPermission(p, "quickshop.other.use")) {
      final Info info = new SimpleInfo(shop.getLocation(), ShopAction.PURCHASE_BUY, null, null, shop, false);
      actions.put(p.getUniqueId(), info);
      if(!direct) {
        if(shop.isStackingShop()) {
          plugin.text().of(p, "how-many-buy-stack", shop.getItem().getAmount(), itemAmount, tradeAllWord).send();
        } else {
          plugin.text().of(p, "how-many-buy", itemAmount, tradeAllWord).send();
        }
      } else {
        plugin.getShopManager().actionSelling(p, new BukkitInventoryWrapper(p.getInventory()), eco, info, shop, arg);
      }
    }
    return true;
  }

  private void playClickSound(@NotNull final Player player) {

    if(plugin.getConfig().getBoolean("effect.sound.onclick")) {
      player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 80.f, 1.0f);
    }
  }

  private int getPlayerCanSell(@NotNull final Shop shop, final double ownerBalance, final double price, @NotNull final InventoryWrapper playerInventory) {

    final boolean isContainerCountingNeeded = shop.isUnlimited();
    if(shop.isFreeShop()) {
      return isContainerCountingNeeded? Util.countItems(playerInventory, shop) : Math.min(shop.getRemainingSpace(), Util.countItems(playerInventory, shop));
    }

    int items = Util.countItems(playerInventory, shop);
    final int ownerCanAfford = (int)(ownerBalance / price);
    if(!isContainerCountingNeeded) {
      // Amount check player amount and shop empty slot
      items = Math.min(items, shop.getRemainingSpace());
      // Amount check player selling item total cost and the shop owner's balance
      items = Math.min(items, ownerCanAfford);
    } else if(plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners")) {
      // even if the shop is unlimited, the config option pay-unlimited-shop-owners is set to
      // true,
      // the unlimited shop owner should have enough money.
      items = Math.min(items, ownerCanAfford);
    }
    if(items < 0) {
      items = 0;
    }
    return items;
  }

  private int buyingShopAllCalc(@NotNull final AbstractEconomy eco, @NotNull final Shop shop, @NotNull final Player p) {

    int amount;
    final int shopHaveSpaces =
            Util.countSpace(shop.getInventory(), shop);
    final int invHaveItems = Util.countItems(new BukkitInventoryWrapper(p.getInventory()), shop);
    // Check if shop owner has enough money
    final double ownerBalance = eco
            .getBalance(shop.getOwner(), shop.getLocation().getWorld(),
                        shop.getCurrency());
    final int ownerCanAfford;
    if(shop.getPrice() != 0) {
      ownerCanAfford = (int)(ownerBalance / shop.getPrice());
    } else {
      ownerCanAfford = Integer.MAX_VALUE;
    }
    if(!shop.isUnlimited()) {
      amount = Math.min(shopHaveSpaces, invHaveItems);
      amount = Math.min(amount, ownerCanAfford);
    } else {
      amount = invHaveItems;
      // even if the shop is unlimited, the config option pay-unlimited-shop-owners is set to
      // true,
      // the unlimited shop owner should have enough money.
      if(plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners")) {
        amount = Math.min(amount, ownerCanAfford);
      }
    }
    if(amount < 1) { // typed 'all' but the auto set amount is 0
      if(shopHaveSpaces == 0) {
        // when typed 'all' but the shop doesn't have any empty space
        plugin.text().of(p, "shop-has-no-space", shopHaveSpaces,
                         Util.getItemStackName(shop.getItem())).send();
        return 0;
      }
      if(ownerCanAfford == 0
         && (!shop.isUnlimited()
             || plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners"))) {
        // when typed 'all' but the shop owner doesn't have enough money to buy at least 1
        // item (and shop isn't unlimited or pay-unlimited is true)
        plugin.text().of(p, "the-owner-cant-afford-to-buy-from-you",
                         plugin.getShopManager().format(shop.getPrice(), shop.getLocation().getWorld(),
                                                        shop.getCurrency()),
                         plugin.getShopManager().format(ownerBalance, shop.getLocation().getWorld(),
                                                        shop.getCurrency())).send();
        return 0;
      }
      // when typed 'all' but player doesn't have any items to sell
      plugin.text().of(p, "you-dont-have-that-many-items", amount, Util.getItemStackName(shop.getItem())).send();
      return 0;
    }
    return amount;
  }

  private int getPlayerCanBuy(@NotNull final Shop shop, final double traderBalance, final double price, @NotNull final InventoryWrapper playerInventory) {

    final boolean isContainerCountingNeeded = shop.isUnlimited();
    if(shop.isFreeShop()) { // Free shop
      return isContainerCountingNeeded? Util.countSpace(playerInventory, shop) : Math.min(shop.getRemainingStock(), Util.countSpace(playerInventory, shop));
    }
    int itemAmount = Math.min(Util.countSpace(playerInventory, shop), (int)Math.floor(traderBalance / price));
    if(!isContainerCountingNeeded) {
      itemAmount = Math.min(itemAmount, shop.getRemainingStock());
    }
    if(itemAmount < 0) {
      itemAmount = 0;
    }
    return itemAmount;
  }

  private int sellingShopAllCalc(@NotNull final AbstractEconomy eco, @NotNull final Shop shop, @NotNull final Player p) {

    int amount;
    final int shopHaveItems = shop.getRemainingStock();
    final int invHaveSpaces = Util.countSpace(new BukkitInventoryWrapper(p.getInventory()), shop);
    if(!shop.isUnlimited()) {
      amount = Math.min(shopHaveItems, invHaveSpaces);
    } else {
      // should check not having items but having empty slots, cause player is trying to buy
      // items from the shop.
      amount = invHaveSpaces;
    }
    // typed 'all', check if player has enough money than price * amount
    final double price = shop.getPrice();
    final double balance = eco.getBalance(QUserImpl.createFullFilled(p), shop.getLocation().getWorld(),
                                          shop.getCurrency());
    amount = Math.min(amount, (int)Math.floor(balance / price));
    if(amount < 1) { // typed 'all' but the auto set amount is 0
      // when typed 'all' but player can't buy any items
      if(!shop.isUnlimited() && shopHaveItems < 1) {
        // but also the shop's stock is 0
        plugin.text().of(p, "shop-stock-too-low",
                         shop.getRemainingStock(),
                         Util.getItemStackName(shop.getItem())).send();
        return 0;
      } else {
        // when if player's inventory is full
        if(invHaveSpaces <= 0) {
          plugin.text().of(p, "not-enough-space",
                           invHaveSpaces).send();
          return 0;
        }
        plugin.text().of(p, "you-cant-afford-to-buy",
                         plugin.getShopManager().format(price, shop.getLocation().getWorld(),
                                                        shop.getCurrency()),
                         plugin.getShopManager().format(balance, shop.getLocation().getWorld(),
                                                        shop.getCurrency())).send();
      }
      return 0;
    }
    return amount;
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onDyeing(final PlayerInteractEvent e) {

    if(e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() == null || !Util.isDyes(e.getItem().getType())) {
      return;
    }
    final Block block = e.getClickedBlock();
    if(block == null || !Util.isWallSign(block.getType())) {
      return;
    }
    final Block attachedBlock = Util.getAttached(block);
    if(attachedBlock == null || plugin.getShopManager().getShopIncludeAttached(attachedBlock.getLocation()) == null) {
      return;
    }
    e.setCancelled(true);
    Log.debug("Disallow " + e.getPlayer().getName() + " dye the shop sign.");
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onSignEditing(final SignChangeEvent e) {

    final Block block = e.getBlock();
    if(!Util.isWallSign(block.getType())) {
      return;
    }
    final BlockState state = e.getBlock().getState();
    if(state instanceof final Sign sign) {
      if(sign.getPersistentDataContainer().has(Shop.SHOP_NAMESPACED_KEY, ShopSignPersistentDataType.INSTANCE)) {
        e.setCancelled(true);
        Log.debug("Disallow " + e.getPlayer().getName() + " editing the shop sign.");
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onInventoryClose(final InventoryCloseEvent e) {

    final UUID id = e.getPlayer().getUniqueId();

    if(MenuManager.instance().inMenu(id) || !QuickShop.inShop.contains(id)) {
      return;
    }

    try {
      final Location location = e.getInventory().getLocation();
      if(location == null) {
        return; /// ignored as workaround, GH-303
      }
      //Cannot tick distance manager while unloading playerchunks
      //https://github.com/pl3xgaming/Purpur/issues/631
      if(!Util.isLoaded(location)) {
        return;
      }
      final Shop shop = plugin.getShopManager().getShopIncludeAttached(location);
      if(shop != null) {
        shop.setSignText(plugin.text().findRelativeLanguages(e.getPlayer()));
      }
    } catch(final NullPointerException ignored) {
    }

    QuickShop.inShop.remove(id);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onJoin(final PlayerLocaleChangeEvent e) {

    Log.debug("Player " + e.getPlayer().getName() + " using new locale " + e.getLocale() + ": " + plugin.text().of(e.getPlayer(), "file-test").plain(e.getLocale()));
    plugin.getDatabaseHelper().updatePlayerProfile(e.getPlayer().getUniqueId(), e.getLocale(), e.getPlayer().getName())
            .exceptionally(throwable->{
              Log.debug("Failed to set player locale: " + throwable.getMessage());
              return null;
            });
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onJoin(final PlayerJoinEvent e) {

    plugin.getPlayerFinder().cache(e.getPlayer().getUniqueId(), e.getPlayer().getName());
    // Notify the player any messages they were sent
    if(plugin.getConfig().getBoolean("shop.auto-fetch-shop-messages")) {
      final long delay = PackageUtil.parsePackageProperly("flushTransactionDelay").asLong(60);
      QuickShop.folia().getImpl().runLaterAsync(()->MsgUtil.flush(e.getPlayer()), delay);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onJoinEasterEgg(final PlayerJoinEvent e) {

    if(plugin.perm().hasPermission(e.getPlayer(), "quickshop.alerts")) {
      final Date date = new Date();
      final LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      if((localDate.getMonthValue() == 4 && localDate.getDayOfMonth() == 1) || PackageUtil.parsePackageProperly("april-rickandroll").asBoolean()) {
        QuickShop.folia().getImpl().runLater((()->plugin.text().of(e.getPlayer(), "april-rick-and-roll-easter-egg").send()), 80L);
      }
    }
  }


  /*
   * Cancels the menu for broken shop block
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerBreakShopCreationChest(final BlockBreakEvent event) {

    @Nullable final Player player = event.getPlayer();
    if(player == null) {
      return;
    }
    final ShopManager.InteractiveManager actionMap = plugin.getShopManager().getInteractiveManager();
    final Info info = actionMap.get(player.getUniqueId());
    if(info != null && info.getLocation().equals(event.getBlock().getLocation())) {
      actionMap.remove(player.getUniqueId());
      if(info.getAction().isTrading()) {
        plugin.text().of(player, "shop-purchase-cancelled").send();
      } else if(info.getAction().isCreating()) {
        plugin.text().of(player, "shop-creation-cancelled").send();
      }
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerQuit(final PlayerQuitEvent e) {
    // Remove them from the menu
    plugin.getShopManager().getInteractiveManager().remove(e.getPlayer().getUniqueId());
    plugin.getDatabaseHelper().updatePlayerProfile(e.getPlayer().getUniqueId(), e.getPlayer().getLocale(), e.getPlayer().getName())
            .exceptionally(throwable->{
              Log.debug("Failed to set player locale: " + throwable.getMessage());
              return null;
            });
  }

  @EventHandler(ignoreCancelled = true)
  public void onTeleport(final PlayerTeleportEvent e) {

    onMove(new PlayerMoveEvent(e.getPlayer(), e.getFrom(), e.getTo()));
  }

  /*
   * Waits for a player to move too far from a shop, then cancels the menu.
   */
  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onMove(final PlayerMoveEvent e) {

    final Info info = plugin.getShopManager().getInteractiveManager().get(e.getPlayer().getUniqueId());
    if(info == null) {
      return;
    }
    final Player p = e.getPlayer();
    final Location loc1 = info.getLocation();
    final Location loc2 = p.getLocation();
    if(loc1.getWorld() != loc2.getWorld() || loc1.distanceSquared(loc2) > 25) {
      if(info.getAction().isTrading()) {
        plugin.text().of(p, "shop-purchase-cancelled").send();
      } else if(info.getAction().isCreating()) {
        plugin.text().of(p, "shop-creation-cancelled").send();
      }
      plugin.getShopManager().getInteractiveManager().remove(p.getUniqueId());
    }
  }

  /**
   * Callback for reloading
   *
   * @return Reloading success
   */
  @Override
  public ReloadResult reloadModule() {

    return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
  }

  enum ClickType {
    SHOPBLOCK,
    SIGN,
    AIR
  }
}
