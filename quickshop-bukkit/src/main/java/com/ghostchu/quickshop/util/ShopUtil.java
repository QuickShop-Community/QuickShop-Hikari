package com.ghostchu.quickshop.util;
/*
 * QuickShop-Hikari
 * Copyright (C) 2024 Daniel "creatorfromhell" Vidmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.economy.AbstractEconomy;
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.settings.type.ShopOwnerEvent;
import com.ghostchu.quickshop.api.event.settings.type.ShopPriceEvent;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Info;
import com.ghostchu.quickshop.api.shop.PriceLimiter;
import com.ghostchu.quickshop.api.shop.PriceLimiterCheckResult;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopAction;
import com.ghostchu.quickshop.api.shop.ShopManager;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.economy.SimpleEconomyTransaction;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.shop.SimpleInfo;
import com.ghostchu.quickshop.shop.inventory.BukkitInventoryWrapper;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.ghostchu.quickshop.QuickShop.taskCache;

/**
 * ShopUtil
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class ShopUtil {

  public static boolean allowed(final Shop shop, final ItemStack itemStack) {

    if(shop.getLocation().getWorld() != null) {

      final Block block = shop.getLocation().getBlock();
      return allowed(block, itemStack);
    }

    return true;
  }

  public static boolean allowed(final Block shopBlock, final ItemStack itemStack) {

    if(shopBlock.getState() instanceof ShulkerBox
       && itemStack.getItemMeta() instanceof final BlockStateMeta blockMeta
       && blockMeta.getBlockState() instanceof ShulkerBox) {

      return false;
    }

    return true;
  }

  public static void transferRequest(@NotNull final UUID sender, @NotNull final UUID uuid, @NotNull final String name, @NotNull final Shop shop) {

    transferRequest(sender, uuid, name, List.of(shop));
  }

  public static void transferRequest(@NotNull final UUID sender, @NotNull final UUID uuid, @NotNull final String name, @NotNull final List<Shop> shopsToTransfer) {

    final Player player = Bukkit.getPlayer(sender);
    final Player receiver = Bukkit.getPlayer(uuid);
    if(receiver == null || player == null) {
      QuickShop.getInstance().text().of(sender, "player-offline", name).send();
      return;
    }
    if(sender.equals(uuid)) {
      QuickShop.getInstance().text().of(sender, "transfer-no-self", name).send();
      return;
    }
    final QUser senderQUser = QUserImpl.createFullFilled(player);
    final QUser receiverQUser = QUserImpl.createFullFilled(receiver);

    final ShopUtil.PendingTransferTask task = new ShopUtil.PendingTransferTask(senderQUser, receiverQUser, shopsToTransfer);
    taskCache.put(uuid, task);
    QuickShop.getInstance().text().of(sender, "transfer-sent", name).send();
    QuickShop.getInstance().text().of(receiver, "transfer-single-request", player.getName()).send();
    QuickShop.getInstance().text().of(receiver, "transfer-single-ask", 60).send();
  }

  public static void setPrice(final QuickShop plugin, @NotNull final QUser user, final double price, @NotNull final Shop shop) {

    if(user.getUniqueId() == null || user.getBukkitPlayer().isEmpty()) {
      return;
    }

    final boolean format = plugin.getConfig().getBoolean("use-decimal-format");

    double fee = 0;

    if(plugin.isPriceChangeRequiresFee()) {
      fee = plugin.getConfig().getDouble("shop.fee-for-price-change");
    }

    final PriceLimiter limiter = plugin.getShopManager().getPriceLimiter();

    if(!shop.playerAuthorize(user.getUniqueId(), BuiltInShopPermission.SET_PRICE)
       && !plugin.perm().hasPermission(user, "quickshop.other.price")) {
      plugin.text().of(user.getUniqueId(), "not-managed-shop").send();
      return;
    }

    if(shop.getPrice() == price) {
      // Stop here if there isn't a price change
      plugin.text().of(user.getUniqueId(), "no-price-change").send();
      return;
    }

    final PriceLimiterCheckResult checkResult = limiter.check(user, shop.getItem(), plugin.getCurrency(), price);

    switch(checkResult.getStatus()) {
      case PRICE_RESTRICTED -> {
        plugin.text().of(user.getUniqueId(), "restricted-prices", Util.getItemStackName(shop.getItem()),
                         Component.text(checkResult.getMin()),
                         Component.text(checkResult.getMax())).send();
        return;
      }
      case REACHED_PRICE_MIN_LIMIT -> {
        plugin.text().of(user, "price-too-cheap", (format)? MsgUtil.decimalFormat(checkResult.getMin()) : Double.toString(checkResult.getMin())).send();
        return;
      }
      case REACHED_PRICE_MAX_LIMIT -> {
        plugin.text().of(user, "price-too-high", (format)? MsgUtil.decimalFormat(checkResult.getMax()) : Double.toString(checkResult.getMax())).send();
        return;
      }
      case NOT_A_WHOLE_NUMBER -> {
        plugin.text().of(user, "not-a-integer", price).send();
        return;
      }
    }

    ShopPriceEvent event = new ShopPriceEvent(Phase.PRE, shop, shop.getPrice(), price);
    event.callEvent();

    event = event.clone(Phase.MAIN);

    if(event.callCancellableEvent()) {
      Log.debug("A plugin cancelled the price change event.");
      return;
    }

    if(fee > 0) {
      final SimpleEconomyTransaction transaction = SimpleEconomyTransaction.builder()
              .core(plugin.getEconomy())
              .from(QUserImpl.createFullFilled(user.getBukkitPlayer().get()))
              .amount(fee)
              .world(Objects.requireNonNull(shop.getLocation().getWorld()))
              .currency(plugin.getCurrency())
              .build();
      if(!transaction.checkBalance()) {
        plugin.text().of(user, "you-cant-afford-to-change-price", plugin.getShopManager().format(fee, shop)).send();
        return;
      }
      if(!transaction.failSafeCommit()) {
        plugin.text().of(user, "economy-transaction-failed", transaction.getLastError()).send();
        return;
      }
    }

    if(plugin.isPriceChangeRequiresFee()) {
      plugin.text().of(user,
                       "fee-charged-for-price-change", plugin.getShopManager().format(fee, shop)).send();
    }

    // Update the shop
    shop.setPrice(event.updated());
    plugin.text().of(user,
                     "price-is-now", plugin.getShopManager().format(event.updated(), shop)).send();

    event = event.clone(Phase.POST);
    event.callEvent();
  }

  public static boolean sellToShop(@NotNull final Player p, @Nullable final Shop shop, final boolean direct, final boolean all) {

    if(shop == null) {
      return false;
    }
    if(!shop.isBuying()) {
      return false;
    }
    if(!QuickShop.getInstance().perm().hasPermission(p, "quickshop.use")) {
      return false;
    }
    QuickShop.getInstance().getShopManager().sendShopInfo(p, shop);
    shop.setSignText(QuickShop.getInstance().text().findRelativeLanguages(p));
    Util.playClickSound(p);
    shop.onClick(p);
    if(shop.getRemainingSpace() == 0) {
      QuickShop.getInstance().text().of(p, "purchase-out-of-space", shop.ownerName()).send();
      return true;
    }
    final AbstractEconomy eco = QuickShop.getInstance().getEconomy();
    final double price = shop.getPrice();
    final Inventory playerInventory = p.getInventory();
    final String tradeAllWord = QuickShop.getInstance().getConfig().getString("shop.word-for-trade-all-items", "all");
    final double ownerBalance = eco.getBalance(shop.getOwner(), shop.getLocation().getWorld(), shop.getCurrency());
    final int items = getPlayerCanSell(shop, ownerBalance, price, new BukkitInventoryWrapper(playerInventory));
    final ShopManager.InteractiveManager actions = QuickShop.getInstance().getShopManager().getInteractiveManager();
    if(shop.playerAuthorize(p.getUniqueId(), BuiltInShopPermission.PURCHASE)
       || QuickShop.getInstance().perm().hasPermission(p, "quickshop.other.use")) {
      final Info info = new SimpleInfo(shop.getLocation(), ShopAction.PURCHASE_SELL, null, null, shop, false);
      actions.put(p.getUniqueId(), info);
      if(!direct) {
        if(shop.isStackingShop()) {
          QuickShop.getInstance().text().of(p, "how-many-sell-stack", shop.getItem().getAmount(), items, tradeAllWord).send();
        } else {
          QuickShop.getInstance().text().of(p, "how-many-sell", items, tradeAllWord).send();
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
        QuickShop.getInstance().getShopManager().actionBuying(p, new BukkitInventoryWrapper(p.getInventory()), eco, info, shop, arg);
      }
    }
    return true;
  }

  public static boolean buyFromShop(@NotNull final Player p, @Nullable final Shop shop, final boolean direct, final boolean all) {

    if(shop == null) {
      return false;
    }
    if(!shop.isSelling()) {
      return false;
    }

    final AbstractEconomy eco = QuickShop.getInstance().getEconomy();
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

  public static boolean buyFromShop(@NotNull final Player p, @Nullable final Shop shop, final int arg, final boolean direct, final boolean all) {

    if(shop == null) {
      return false;
    }
    if(!shop.isSelling()) {
      return false;
    }
    if(!QuickShop.getInstance().perm().hasPermission(p, "quickshop.use")) {
      return false;
    }
    QuickShop.getInstance().getShopManager().sendShopInfo(p, shop);
    shop.setSignText(QuickShop.getInstance().text().findRelativeLanguages(p));
    if(shop.getRemainingStock() == 0) {
      QuickShop.getInstance().text().of(p, "purchase-out-of-stock", shop.ownerName()).send();
      return true;
    }
    Util.playClickSound(p);
    shop.onClick(p);
    final AbstractEconomy eco = QuickShop.getInstance().getEconomy();
    final double price = shop.getPrice();
    final Inventory playerInventory = p.getInventory();
    final String tradeAllWord = QuickShop.getInstance().getConfig().getString("shop.word-for-trade-all-items", "all");
    final ShopManager.InteractiveManager actions = QuickShop.getInstance().getShopManager().getInteractiveManager();
    final double traderBalance = eco.getBalance(QUserImpl.createFullFilled(p), shop.getLocation().getWorld(), shop.getCurrency());
    final int itemAmount = getPlayerCanBuy(shop, traderBalance, price, new BukkitInventoryWrapper(playerInventory));
    if(shop.playerAuthorize(p.getUniqueId(), BuiltInShopPermission.PURCHASE)
       || QuickShop.getInstance().perm().hasPermission(p, "quickshop.other.use")) {
      final Info info = new SimpleInfo(shop.getLocation(), ShopAction.PURCHASE_BUY, null, null, shop, false);
      actions.put(p.getUniqueId(), info);
      if(!direct) {
        if(shop.isStackingShop()) {
          QuickShop.getInstance().text().of(p, "how-many-buy-stack", shop.getItem().getAmount(), itemAmount, tradeAllWord).send();
        } else {
          QuickShop.getInstance().text().of(p, "how-many-buy", itemAmount, tradeAllWord).send();
        }
      } else {
        QuickShop.getInstance().getShopManager().actionSelling(p, new BukkitInventoryWrapper(p.getInventory()), eco, info, shop, arg);
      }
    }
    return true;
  }

  private static int getPlayerCanSell(@NotNull final Shop shop, final double ownerBalance, final double price, @NotNull final InventoryWrapper playerInventory) {

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
    } else if(QuickShop.getInstance().getConfig().getBoolean("shop.pay-unlimited-shop-owners")) {
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

  private static int buyingShopAllCalc(@NotNull final AbstractEconomy eco, @NotNull final Shop shop, @NotNull final Player p) {

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
      if(QuickShop.getInstance().getConfig().getBoolean("shop.pay-unlimited-shop-owners")) {
        amount = Math.min(amount, ownerCanAfford);
      }
    }
    if(amount < 1) { // typed 'all' but the auto set amount is 0
      if(shopHaveSpaces == 0) {
        // when typed 'all' but the shop doesn't have any empty space
        QuickShop.getInstance().text().of(p, "shop-has-no-space", shopHaveSpaces,
                                          Util.getItemStackName(shop.getItem())).send();
        return 0;
      }
      if(ownerCanAfford == 0
         && (!shop.isUnlimited()
             || QuickShop.getInstance().getConfig().getBoolean("shop.pay-unlimited-shop-owners"))) {
        // when typed 'all' but the shop owner doesn't have enough money to buy at least 1
        // item (and shop isn't unlimited or pay-unlimited is true)
        QuickShop.getInstance().text().of(p, "the-owner-cant-afford-to-buy-from-you",
                                          QuickShop.getInstance().getShopManager().format(shop.getPrice(), shop.getLocation().getWorld(),
                                                                                          shop.getCurrency()),
                                          QuickShop.getInstance().getShopManager().format(ownerBalance, shop.getLocation().getWorld(),
                                                                                          shop.getCurrency())).send();
        return 0;
      }
      // when typed 'all' but player doesn't have any items to sell
      QuickShop.getInstance().text().of(p, "you-dont-have-that-many-items", amount, Util.getItemStackName(shop.getItem())).send();
      return 0;
    }
    return amount;
  }

  private static int getPlayerCanBuy(@NotNull final Shop shop, final double traderBalance, final double price, @NotNull final InventoryWrapper playerInventory) {

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

  private static int sellingShopAllCalc(@NotNull final AbstractEconomy eco, @NotNull final Shop shop, @NotNull final Player p) {

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
        QuickShop.getInstance().text().of(p, "shop-stock-too-low",
                                          shop.getRemainingStock(),
                                          Util.getItemStackName(shop.getItem())).send();
        return 0;
      } else {
        // when if player's inventory is full
        if(invHaveSpaces <= 0) {
          QuickShop.getInstance().text().of(p, "not-enough-space",
                                            invHaveSpaces).send();
          return 0;
        }
        QuickShop.getInstance().text().of(p, "you-cant-afford-to-buy",
                                          QuickShop.getInstance().getShopManager().format(price, shop.getLocation().getWorld(),
                                                                                          shop.getCurrency()),
                                          QuickShop.getInstance().getShopManager().format(balance, shop.getLocation().getWorld(),
                                                                                          shop.getCurrency())).send();
      }
      return 0;
    }
    return amount;
  }

  @Data
  public static class PendingTransferTask {

    private final QUser from;
    private final QUser to;
    private final List<Shop> shops;

    public PendingTransferTask(final QUser from, final QUser to, final List<Shop> shops) {

      this.from = from;
      this.to = to;
      this.shops = shops;
    }

    public void cancel(final boolean sendMessage) {

      if(sendMessage) {
        QuickShop.getInstance().text().of(from, "transfer-rejected-fromside", to).send();
        QuickShop.getInstance().text().of(to, "transfer-rejected-toside", from).send();
      }
    }

    public void commit(final boolean sendMessage) {

      for(final Shop shop : shops) {

        ShopOwnerEvent event = new ShopOwnerEvent(Phase.PRE, shop, shop.getOwner(), to);
        event.callEvent();

        event = event.clone(Phase.MAIN);
        if(event.callCancellableEvent()) {
          continue;
        }
        shop.setOwner(event.updated());

        event = event.clone(Phase.POST);
        event.callEvent();


        if(sendMessage) {
          QuickShop.getInstance().text().of(from, "transfer-accepted-fromside", event.updated()).send();
          QuickShop.getInstance().text().of(event.updated(), "transfer-accepted-toside", from).send();
        }
      }
    }
  }
}