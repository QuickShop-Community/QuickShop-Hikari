package com.ghostchu.quickshop.menu.keeper;
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
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopType;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.menu.shared.QuickShopPage;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.ShopUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import net.tnemc.item.AbstractItemStack;
import net.tnemc.item.providers.SkullProfile;
import net.tnemc.menu.core.builder.IconBuilder;
import net.tnemc.menu.core.callbacks.page.PageOpenCallback;
import net.tnemc.menu.core.icon.action.impl.ChatAction;
import net.tnemc.menu.core.icon.action.impl.RunnableAction;
import net.tnemc.menu.core.icon.action.impl.SwitchMenuAction;
import net.tnemc.menu.core.icon.impl.StateIcon;
import net.tnemc.menu.core.viewer.MenuViewer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static com.ghostchu.quickshop.menu.ShopKeeperMenu.KEEPER_MAIN;

/**
 * MainPage
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class MainPage extends QuickShopPage {

  public MainPage() {

    super(KEEPER_MAIN);

    setOpen(this::open);
  }

  public void open(final PageOpenCallback open) {

    final UUID id = open.getPlayer().identifier();

    final Optional<MenuViewer> viewer = open.getPlayer().viewer();
    if(viewer.isPresent()) {

      final Optional<Shop> shop = getShop(viewer.get());
      final Player player = Bukkit.getPlayer(id);
      if(shop.isPresent() && player != null) {

        //Set up our borders
        final IconBuilder borderBuilder = new IconBuilder(QuickShop.getInstance().stack().of("WHITE_STAINED_GLASS_PANE", 1));
        open.getPage().setRow(1, borderBuilder);
        open.getPage().setRow(3, borderBuilder);

        final Object priceObj = viewer.get().dataOrDefault("SHOP_PRICE", shop.get().getPrice());

        //change icon - done(needs tested)
        open.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("PAPER", 1)
                                                       .display(get(id, "gui.keeper.change-icon.display"))
                                                       .lore(getList(id, "gui.keeper.change-icon.lore", priceObj)))
                                       .withActions(new ChatAction((message->{

                                         if(!message.getMessage().isEmpty()) {

                                           try {

                                             final BigDecimal price = new BigDecimal(message.getMessage());

                                             viewer.get().addData("SHOP_PRICE", price.doubleValue());

                                             Util.mainThreadRun(()->ShopUtil.setPrice(QuickShop.getInstance(), QUserImpl.createFullFilled(player), price.doubleValue(), shop.get()));
                                             return true;

                                           } catch(final NumberFormatException ignore) { }
                                         }
                                         message.getPlayer().message(legacy(id, "gui.keeper.change-icon.enter"));
                                         return false;
                                       })), new RunnableAction((run)->run.player().message(legacy(id, "gui.keeper.change-icon.enter"))))
                                       .withSlot(10).build());

        //Mode Toggle Icon
        final AbstractItemStack<?> buyingStack = QuickShop.getInstance().stack().of("GREEN_WOOL", 1)
                .display(get(id, "gui.keeper.mode-icon.display"))
                .lore(getList(id, "gui.keeper.mode-icon.lore", "SELLING", "BUYING"));

        final AbstractItemStack<?> sellingStack = QuickShop.getInstance().stack().of("RED_WOOL", 1)
                .display(get(id, "gui.keeper.mode-icon.display"))
                .lore(getList(id, "gui.keeper.mode-icon.lore", "BUYING", "FROZEN"));

        final AbstractItemStack<?> frozenStack = QuickShop.getInstance().stack().of("RED_WOOL", 1)
                .display(get(id, "gui.keeper.mode-icon.display"))
                .lore(getList(id, "gui.keeper.mode-icon.lore", "FROZEN", "SELLING"));

        String modeState = (shop.get().getShopType().equals(ShopType.BUYING))? "BUYING" : "SELLING";
        if(shop.get().getShopType().equals(ShopType.FROZEN)) {
          modeState = "FROZEN";
        }
        final StateIcon changeIcon = new StateIcon(buyingStack, null, "SHOP_TYPE", modeState, (currentState)->{
          if(currentState.toUpperCase(Locale.ROOT).equals("SELLING")) {
            Util.mainThreadRun(()->shop.get().setShopType(ShopType.BUYING));
            return "BUYING";
          } else if(currentState.toUpperCase(Locale.ROOT).equals("FROZEN")) {
            Util.mainThreadRun(()->shop.get().setShopType(ShopType.SELLING));
            return "SELLING";
          }
          Util.mainThreadRun(()->shop.get().setShopType(ShopType.FROZEN));
          return "FROZEN";
        });
        changeIcon.setSlot(12);
        changeIcon.addState("SELLING", buyingStack);
        changeIcon.addState("BUYING", sellingStack);
        changeIcon.addState("FROZEN", frozenStack);
        open.getPage().addIcon(changeIcon);

        //Staff Icon - done(needs tested/needs other menu completed)
        SkullProfile profile = null;
        if(shop.get().getOwner().isRealPlayer()) {
          profile = new SkullProfile();
          profile.setUuid(shop.get().getOwner().getUniqueId());
        }

        if((!shop.get().playerAuthorize(id, BuiltInShopPermission.MANAGEMENT_PERMISSION)
            && !QuickShop.getInstance().perm().hasPermission(player, "quickshop.other.staff"))) {

          open.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("PLAYER_HEAD", 1)
                                                         .display(get(id, "gui.keeper.staff-icon.no-permission"))
                                                         .profile(profile))
                                         .withSlot(14)
                                         .withActions(new SwitchMenuAction("qs:staff")).build());
        } else {

          open.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("PLAYER_HEAD", 1)
                                                         .display(get(id, "gui.keeper.staff-icon.display"))
                                                         .lore(getList(id, "gui.keeper.staff-icon.lore"))
                                                         .profile(profile))
                                         .withSlot(14)
                                         .withActions(new SwitchMenuAction("qs:staff")).build());
        }

        //Remove Icon - done(needs tested)
        open.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("BARRIER", 1)
                                                       .display(get(id, "gui.keeper.remove-icon.display"))
                                                       .lore(getList(id, "gui.keeper.remove-icon.lore")))
                                       .withActions(new ChatAction((message->{

                                         if(!message.getMessage().isEmpty()) {

                                           if(message.getMessage().equalsIgnoreCase("confirm")) {
                                             if(shop.get().playerAuthorize(id, BuiltInShopPermission.DELETE)
                                                || QuickShop.getInstance().perm().hasPermission(player, "quickshop.other.destroy")) {

                                               Util.mainThreadRun(()->QuickShop.getInstance().getShopManager().deleteShop(shop.get()));
                                               QuickShop.getInstance().logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(player), "/quickshop remove command", shop.get().saveToInfoStorage()));
                                             } else {

                                               QuickShop.getInstance().text().of(player, "no-permission").send();
                                             }
                                             viewer.get().close(QuickShop.getInstance().createMenuPlayer(player));
                                             return true;
                                           }
                                           return true;
                                         }
                                         message.getPlayer().message(legacy(id, "gui.keeper.remove-icon.confirm"));
                                         return false;
                                       })), new RunnableAction((run)->run.player().message(legacy(id, "gui.keeper.remove-icon.confirm"))))
                                       .withSlot(16).build());
      }
    }
  }
}