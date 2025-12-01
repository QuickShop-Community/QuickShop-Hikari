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
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.menu.config.GuiConfig;
import com.ghostchu.quickshop.menu.shared.GuiChatAction;
import com.ghostchu.quickshop.menu.shared.QuickShopPage;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.shop.history.ShopHistory;
import com.ghostchu.quickshop.util.ShopUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import net.kyori.adventure.text.Component;
import net.tnemc.item.AbstractItemStack;
import net.tnemc.item.bukkit.BukkitItemStack;
import net.tnemc.item.providers.SkullProfile;
import net.tnemc.menu.core.builder.IconBuilder;
import net.tnemc.menu.core.callbacks.page.PageOpenCallback;
import net.tnemc.menu.core.compatibility.MenuPlayer;
import net.tnemc.menu.core.icon.action.impl.RunnableAction;
import net.tnemc.menu.core.icon.action.impl.SwitchMenuAction;
import net.tnemc.menu.core.icon.impl.StateIcon;
import net.tnemc.menu.core.manager.MenuManager;
import net.tnemc.menu.core.viewer.MenuViewer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static com.ghostchu.quickshop.menu.ShopHistoryMenu.HISTORY_RECORDS;
import static com.ghostchu.quickshop.menu.ShopHistoryMenu.HISTORY_SUMMARY;
import static com.ghostchu.quickshop.menu.ShopHistoryMenu.SHOPS_DATA;

import static com.ghostchu.quickshop.menu.ShopKeeperMenu.KEEPER_MAIN;
import static com.ghostchu.quickshop.shop.SimpleShopManager.BUYING_TYPE;
import static com.ghostchu.quickshop.shop.SimpleShopManager.FROZEN_TYPE;
import static com.ghostchu.quickshop.shop.SimpleShopManager.SELLING_TYPE;

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

        // Clear existing icons to ensure fresh data on reopen
        open.getPage().getIcons().clear();

        // Load GUI configuration
        final GuiConfig.MenuConfig menuConfig = QuickShop.getInstance().getGuiConfig().getMenuConfig("keeper");
        final GuiConfig.IconConfig borderConfig = menuConfig != null?menuConfig.getIcon("border") : null;
        final GuiConfig.IconConfig shopItemConfig = menuConfig != null?menuConfig.getIcon("shop-item") : null;
        final GuiConfig.IconConfig changePriceConfig = menuConfig != null?menuConfig.getIcon("change-price") : null;
        final GuiConfig.IconConfig modeToggleConfig = menuConfig != null?menuConfig.getIcon("mode-toggle") : null;
        final GuiConfig.IconConfig staffConfig = menuConfig != null?menuConfig.getIcon("staff") : null;
        final GuiConfig.IconConfig historyConfig = menuConfig != null?menuConfig.getIcon("history") : null;
        final GuiConfig.IconConfig removeConfig = menuConfig != null?menuConfig.getIcon("remove") : null;
        final GuiConfig.IconConfig closeConfig = menuConfig != null?menuConfig.getIcon("close") : null;

        // Set up our borders from config (gray for modern look)
        final String borderMaterial = borderConfig != null?borderConfig.getMaterial() : "GRAY_STAINED_GLASS_PANE";
        final IconBuilder borderBuilder = new IconBuilder(QuickShop.getInstance().stack().of(borderMaterial, 1));
        
        // Rows 2 and 4 for modern 4-row layout
        final List<Integer> borderRows = borderConfig != null?borderConfig.getRows() : List.of(2, 4);
        for (final int row : borderRows) {
          open.getPage().setRow(row, borderBuilder);
        }

        // Shop item preview - top center (slot 4)
        final ItemStack shopItem = shop.get().getItem();
        final int shopItemSlot = shopItemConfig != null?shopItemConfig.getSlot() : 4;
        open.getPage().addIcon(new IconBuilder(new BukkitItemStack().of(shopItem)).withSlot(shopItemSlot).build());

        // Always read price directly from shop to get the latest value
        final double currentPrice = shop.get().getPrice();

        // Change price icon from config (GOLD_NUGGET for "price")
        final String changePriceMaterial = changePriceConfig != null?changePriceConfig.getMaterial() : "GOLD_NUGGET";
        final int changePriceSlot = changePriceConfig != null?changePriceConfig.getSlot() : 19;
        
        if(shop.get().playerAuthorize(id, BuiltInShopPermission.SET_PRICE)
           || QuickShop.getInstance().perm().hasPermission(player, "quickshop.other.price")) {
          open.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(changePriceMaterial, 1)
                                                         .display(getConfigDisplay(changePriceConfig, "<bold><green>Change Price</green></bold>"))
                                                         .lore(getConfigLore(changePriceConfig, currentPrice)))
                                         .withActions(new GuiChatAction((message)->{
                                           if(!message.isEmpty()) {
                                             try {
                                               final BigDecimal price = new BigDecimal(message);
                                               // Update price and reopen menu in the same region thread to ensure price is updated before GUI shows
                                               Util.regionThread(shop.get().getLocation(), ()->{
                                                 ShopUtil.setPrice(QuickShop.getInstance(), QUserImpl.createFullFilled(player), price.doubleValue(), shop.get());
                                                 // Reopen menu after price is set
                                                 final MenuPlayer menuPlayer = QuickShop.getInstance().createMenuPlayer(player);
                                                 menuPlayer.inventory().openMenu(menuPlayer, "qs:keeper", KEEPER_MAIN);
                                               });
                                               return true;
                                             } catch(final NumberFormatException ignore) { }
                                           }
                                           return true;
                                         }, guiMessage("keeper.enter-price"), false))  // false = don't auto-reopen, we handle it manually
                                         .withSlot(changePriceSlot).build());
        }

        // Mode Toggle Icon from config (concrete for clean look)
        final GuiConfig.IconConfig sellingConfig = modeToggleConfig != null?modeToggleConfig.getSubIcon("selling") : null;
        final GuiConfig.IconConfig buyingConfig = modeToggleConfig != null?modeToggleConfig.getSubIcon("buying") : null;
        final GuiConfig.IconConfig frozenConfig = modeToggleConfig != null?modeToggleConfig.getSubIcon("frozen") : null;
        final String sellingMaterial = sellingConfig != null?sellingConfig.getMaterial() : "LIME_CONCRETE";
        final String buyingMaterial = buyingConfig != null?buyingConfig.getMaterial() : "ORANGE_CONCRETE";
        final String frozenMaterial = frozenConfig != null?frozenConfig.getMaterial() : "LIGHT_BLUE_CONCRETE";
        final int modeToggleSlot = modeToggleConfig != null?modeToggleConfig.getSlot() : 21;
        
        if(shop.get().playerAuthorize(id, BuiltInShopPermission.SET_SHOPTYPE)
           || QuickShop.getInstance().perm().hasPermission(player, "quickshop.other.freeze")
              && QuickShop.getInstance().perm().hasPermission(player, "quickshop.other.sell")
              && QuickShop.getInstance().perm().hasPermission(player, "quickshop.other.buy")) {

          final String sellingText = QuickShop.getInstance().text().of("shop-type.selling").plain();
          final String buyingText = QuickShop.getInstance().text().of("shop-type.buying").plain();
          final String frozenText = QuickShop.getInstance().text().of("shop-type.frozen").plain();

          final AbstractItemStack<?> buyingStack = QuickShop.getInstance().stack().of(sellingMaterial, 1)
                  .display(getConfigDisplay(modeToggleConfig, "<bold><green>Change Mode</green></bold>"))
                  .lore(getConfigLore(modeToggleConfig, sellingText, buyingText));

          final AbstractItemStack<?> sellingStack = QuickShop.getInstance().stack().of(buyingMaterial, 1)
                  .display(getConfigDisplay(modeToggleConfig, "<bold><green>Change Mode</green></bold>"))
                  .lore(getConfigLore(modeToggleConfig, buyingText, frozenText));

          final AbstractItemStack<?> frozenStack = QuickShop.getInstance().stack().of(frozenMaterial, 1)
                  .display(getConfigDisplay(modeToggleConfig, "<bold><green>Change Mode</green></bold>"))
                  .lore(getConfigLore(modeToggleConfig, frozenText, sellingText));

          final String modeState = shop.get().shopType().identifier().toUpperCase(Locale.ROOT);

          final StateIcon changeIcon = new StateIcon(buyingStack, null, "SHOP_TYPE", modeState, (currentState)->{
            if(currentState.toUpperCase(Locale.ROOT).equals("SELLING")) {
              Util.regionThread(shop.get().getLocation(), ()->shop.get().shopType(BUYING_TYPE));
              return "BUYING";
            } else if(currentState.toUpperCase(Locale.ROOT).equals("FROZEN")) {
              Util.regionThread(shop.get().getLocation(), ()->shop.get().shopType(SELLING_TYPE));
              return "SELLING";
            }
            Util.regionThread(shop.get().getLocation(), ()->shop.get().shopType(FROZEN_TYPE));
            return "FROZEN";
          });
          changeIcon.setSlot(modeToggleSlot);
          changeIcon.addState("SELLING", buyingStack);
          changeIcon.addState("BUYING", sellingStack);
          changeIcon.addState("FROZEN", frozenStack);
          open.getPage().addIcon(changeIcon);
        }

        // Staff Icon from config
        final String staffMaterial = staffConfig != null?staffConfig.getMaterial() : "PLAYER_HEAD";
        final int staffSlot = staffConfig != null?staffConfig.getSlot() : 23;
        
        SkullProfile profile = null;
        if(shop.get().getOwner().isRealPlayer()) {
          profile = new SkullProfile();
          profile.setUuid(shop.get().getOwner().getUniqueId());
        }

        if((!shop.get().playerAuthorize(id, BuiltInShopPermission.MANAGEMENT_PERMISSION)
            && !QuickShop.getInstance().perm().hasPermission(player, "quickshop.other.staff"))) {

          open.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(staffMaterial, 1)
                                                         .display(getConfigDisplay(staffConfig, "<gray>Shop Staff (No Permission)</gray>"))
                                                         .profile(profile))
                                         .withSlot(staffSlot).build());
        } else {

          open.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(staffMaterial, 1)
                                                         .display(getConfigDisplay(staffConfig, "<bold><aqua>Shop Staff</aqua></bold>"))
                                                         .lore(getConfigLore(staffConfig))
                                                         .profile(profile))
                                         .withSlot(staffSlot)
                                         .withActions(new SwitchMenuAction("qs:staff")).build());
        }

        // History Icon from config (NEW - quick access to history)
        final String historyMaterial = historyConfig != null?historyConfig.getMaterial() : "BOOK";
        final int historySlot = historyConfig != null?historyConfig.getSlot() : 24;
        
        if(shop.get().playerAuthorize(id, BuiltInShopPermission.VIEW_PURCHASE_LOGS)
           || QuickShop.getInstance().perm().hasPermission(player, "quickshop.other.history")) {
          open.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(historyMaterial, 1)
                  .display(getConfigDisplay(historyConfig, "<bold><light_purple>Transaction History</light_purple></bold>"))
                  .lore(getConfigLore(historyConfig)))
                  .withActions(new RunnableAction((click) -> {
                    // Close current menu and load history async
                    viewer.get().close(QuickShop.getInstance().createMenuPlayer(player));
                    
                    final List<Shop> shops = new ArrayList<>();
                    shops.add(shop.get());
                    
                    final MenuPlayer menuPlayer = QuickShop.getInstance().createMenuPlayer(player);
                    
                    Util.asyncThreadRun(() -> {
                      final ShopHistory shopHistory = new ShopHistory(QuickShop.getInstance(), shops);
                      
                      try {
                        final List<ShopHistory.ShopHistoryRecord> queryResult = shopHistory.query();
                        final ShopHistory.ShopSummary summary = shopHistory.generateSummary().join();
                        Log.debug(summary.toString());
                        
                        if(queryResult == null) {
                          return;
                        }
                        
                        final MenuViewer historyViewer = new MenuViewer(id);
                        MenuManager.instance().addViewer(historyViewer);
                        historyViewer.addData(SHOPS_DATA, shops);
                        historyViewer.addData(HISTORY_RECORDS, queryResult);
                        historyViewer.addData(HISTORY_SUMMARY, summary);
                        
                        Util.mainThreadRun(() -> {
                          MenuManager.instance().open("qs:history", 1, menuPlayer);
                        });
                        
                      } catch(final Exception e) {
                        MenuManager.instance().removeViewer(id);
                        QuickShop.getInstance().text().of(id, "internal-error", id).send();
                        QuickShop.getInstance().logger().error("Couldn't query the shop history for shops {}.", shopHistory.shops(), e);
                      }
                    });
                  }))
                  .withSlot(historySlot).build());
        }

        // Remove Icon from config (TNT for dramatic effect)
        final String removeMaterial = removeConfig != null?removeConfig.getMaterial() : "TNT";
        final int removeSlot = removeConfig != null?removeConfig.getSlot() : 25;
        
        if(shop.get().playerAuthorize(id, BuiltInShopPermission.DELETE)
           || QuickShop.getInstance().perm().hasPermission(player, "quickshop.other.destroy")) {
          open.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(removeMaterial, 1)
                                                         .display(getConfigDisplay(removeConfig, "<bold><red>Delete Shop</red></bold>"))
                                                         .lore(getConfigLore(removeConfig)))
                                         .withActions(new GuiChatAction((message)->{
                                           if(!message.isEmpty()) {
                                             if(message.equalsIgnoreCase("confirm")) {
                                               Util.regionThread(shop.get().getLocation(), ()->QuickShop.getInstance().getShopManager().deleteShop(shop.get()));
                                               QuickShop.getInstance().logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(player), "/quickshop remove command", shop.get().saveToInfoStorage()));
                                               return true;
                                             }
                                             return true;
                                           }
                                           player.sendMessage(guiMessage("keeper.confirm-delete"));
                                           return false;
                                         }, guiMessage("keeper.confirm-delete"), false))  // Don't reopen after delete
                                         .withSlot(removeSlot).build());
        }

        // Close button - OAK_DOOR for "exit" (slot 31)
        final String closeMaterial = closeConfig != null?closeConfig.getMaterial() : "OAK_DOOR";
        final int closeSlot = closeConfig != null?closeConfig.getSlot() : 31;
        open.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(closeMaterial, 1)
                .display(getConfigDisplay(closeConfig, "<red>Close</red>")))
                .withActions(new RunnableAction((click -> viewer.get().close(QuickShop.getInstance().createMenuPlayer(player)))))
                .withSlot(closeSlot).build());
      }
    }
  }
}