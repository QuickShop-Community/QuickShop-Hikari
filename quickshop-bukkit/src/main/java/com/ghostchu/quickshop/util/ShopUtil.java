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
import com.ghostchu.quickshop.api.event.details.ShopOwnershipTransferEvent;
import com.ghostchu.quickshop.api.event.details.ShopPriceChangeEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.PriceLimiter;
import com.ghostchu.quickshop.api.shop.PriceLimiterCheckResult;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.economy.SimpleEconomyTransaction;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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

    final ShopPriceChangeEvent event = new ShopPriceChangeEvent(shop, shop.getPrice(), price);
    if(Util.fireCancellableEvent(event)) {
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
    shop.setPrice(price);
    shop.setSignText(plugin.text().findRelativeLanguages(user, false));
    plugin.text().of(user,
                     "price-is-now", plugin.getShopManager().format(shop.getPrice(), shop)).send();
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



        final ShopOwnershipTransferEvent event = new ShopOwnershipTransferEvent(shop, shop.getOwner(), to);
        if(event.callCancellableEvent()) {
          continue;
        }
        shop.setOwner(to);
      }
      if(sendMessage) {
        QuickShop.getInstance().text().of(from, "transfer-accepted-fromside", to).send();
        QuickShop.getInstance().text().of(to, "transfer-accepted-toside", from).send();
      }
    }
  }
}