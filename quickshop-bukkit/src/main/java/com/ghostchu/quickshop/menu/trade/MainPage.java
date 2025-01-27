package com.ghostchu.quickshop.menu.trade;
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
import com.ghostchu.quickshop.api.shop.Info;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopAction;
import com.ghostchu.quickshop.menu.shared.PageSwitchWithCloseAction;
import com.ghostchu.quickshop.menu.shared.QuickShopPage;
import com.ghostchu.quickshop.shop.SimpleInfo;
import com.ghostchu.quickshop.shop.inventory.BukkitInventoryWrapper;
import com.ghostchu.quickshop.util.Util;
import net.tnemc.item.bukkit.BukkitItemStack;
import net.tnemc.menu.core.builder.IconBuilder;
import net.tnemc.menu.core.callbacks.page.PageOpenCallback;
import net.tnemc.menu.core.icon.action.impl.ChatAction;
import net.tnemc.menu.core.icon.action.impl.RunnableAction;
import net.tnemc.menu.core.viewer.MenuViewer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.UUID;

/**
 * MainPage
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class MainPage extends QuickShopPage {

  public MainPage() {

    super(1);

    setOpen(this::handle);
  }

  public void handle(final PageOpenCallback open) {

    open.getPage().getIcons().clear();

    final UUID id = open.getPlayer().identifier();

    final Optional<MenuViewer> viewer = open.getPlayer().viewer();
    if(viewer.isPresent()) {

      final Optional<Shop> shop = getShop(viewer.get());
      final Player player = Bukkit.getPlayer(id);
      if(shop.isPresent() && player != null) {

        final AbstractEconomy eco = QuickShop.getInstance().getEconomy();

        //Set up our borders
        final IconBuilder borderBuilder = new IconBuilder(QuickShop.getInstance().stack().of("WHITE_STAINED_GLASS_PANE", 1));
        open.getPage().setRow(1, borderBuilder);
        open.getPage().setRow(5, borderBuilder);

        final ItemStack shopItem = shop.get().getItem();
        final int amount = shopItem.getAmount();
        final int stock = (shop.get().isBuying())? -1 : shop.get().getRemainingStock();
        final String stockString = (shop.get().isUnlimited())? "Unlimited" : stock + "";

        open.getPage().addIcon(new IconBuilder(new BukkitItemStack().of(shopItem)).withSlot(13).build());

        final String lore = (shop.get().isSelling())? "gui.trade.custom.lore-buy" : "gui.trade.custom.lore-sell";
        final String enter = (shop.get().isSelling())? "gui.trade.custom.enter-buy" : "gui.trade.custom.enter-sell";
        open.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("PAPER", 1)
                                                       .display(get(id, "gui.trade.custom.display"))
                                                       .lore(getList(id, lore, amount, stockString)))
                                       .withActions(new ChatAction((message)->{

                                         if(!message.getMessage().isEmpty()) {

                                           try {

                                             final int quantity = Integer.parseInt(message.getMessage());

                                             if(quantity == 0) {
                                               return true;
                                             }

                                             if(!shop.get().isUnlimited() && quantity > stock && stock > -1) {
                                               message.getPlayer().message(legacy(id, "gui.trade.custom.stock"));
                                               return false;
                                             }

                                             if((quantity % amount) > 0) {
                                               message.getPlayer().message(legacy(id, "gui.trade.custom.multiple", amount));
                                               return false;
                                             }
                                             if(shop.get().isBuying()) {

                                               final Info info = new SimpleInfo(shop.get().getLocation(), ShopAction.PURCHASE_SELL, null, null, shop.get(), false);
                                               Util.mainThreadRun(()->QuickShop.getInstance().getShopManager().actionBuying(player, new BukkitInventoryWrapper(player.getInventory()), eco, info, shop.get(), quantity));
                                               viewer.get().close(QuickShop.getInstance().createMenuPlayer(player));
                                             } else {

                                               final Info info = new SimpleInfo(shop.get().getLocation(), ShopAction.PURCHASE_BUY, null, null, shop.get(), false);
                                               Util.mainThreadRun(()->QuickShop.getInstance().getShopManager().actionSelling(player, new BukkitInventoryWrapper(player.getInventory()), eco, info, shop.get(), quantity));
                                               viewer.get().close(QuickShop.getInstance().createMenuPlayer(player));
                                             }
                                             return true;

                                           } catch(final NumberFormatException ignore) { }
                                         }
                                         message.getPlayer().message(legacy(id, enter, amount));

                                         return false;
                                       }), new RunnableAction(click->click.player().message(legacy(id, enter, amount))))
                                       .withSlot(35).build());

        //64, 16, 8, 4, 2, 1
        final int[] quantities = new int[]{
                1, 2, 4, 8, 16, 64
        };

        final int[] quantitySlots = new int[]{
                27, 28, 29, 30, 31, 32
        };

        final String display = (shop.get().isSelling())? "gui.trade.quantity.display-buy" : "gui.trade.quantity.display-sell";

        for(int i = 0; i < quantities.length; i++) {

          final int quantity = quantities[i];
          final int slot = quantitySlots[i];
          final int adjustedAmount = (amount * quantity);

          open.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("GREEN_WOOL", adjustedAmount)
                                                         .display(get(id, display, "x" + adjustedAmount))
                                                         .lore(getList(id, "gui.trade.quantity.lore", eco.format(shop.get().getPrice(),
                                                                                                                 shop.get().getLocation().getWorld(),
                                                                                                                 shop.get().getCurrency()),
                                                                       amount,
                                                                       eco.format((adjustedAmount * shop.get().getPrice()),
                                                                                  shop.get().getLocation().getWorld(),
                                                                                  shop.get().getCurrency()))))
                                         .withActions(new RunnableAction((click->{
                                           if(shop.get().isBuying()) {

                                             final Info info = new SimpleInfo(shop.get().getLocation(), ShopAction.PURCHASE_SELL, null, null, shop.get(), false);
                                             Util.mainThreadRun(()->QuickShop.getInstance().getShopManager().actionBuying(player, new BukkitInventoryWrapper(player.getInventory()), eco, info, shop.get(), quantity));
                                             viewer.get().close(QuickShop.getInstance().createMenuPlayer(player));
                                           } else {

                                             final Info info = new SimpleInfo(shop.get().getLocation(), ShopAction.PURCHASE_BUY, null, null, shop.get(), false);
                                             Util.mainThreadRun(()->QuickShop.getInstance().getShopManager().actionSelling(player, new BukkitInventoryWrapper(player.getInventory()), eco, info, shop.get(), quantity));
                                             viewer.get().close(QuickShop.getInstance().createMenuPlayer(player));
                                           }
                                         })))
                                         .withActions(new PageSwitchWithCloseAction("qs:trade", -1))
                                         .withSlot(slot).build());
        }
      }
    }
  }
}