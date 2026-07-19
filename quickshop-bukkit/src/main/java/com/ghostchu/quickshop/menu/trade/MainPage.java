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
import com.ghostchu.quickshop.api.economy.EconomyProvider;
import com.ghostchu.quickshop.api.event.display.ItemPreviewComponentPrePopulateEvent;
import com.ghostchu.quickshop.api.shop.Info;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopAction;
import com.ghostchu.quickshop.config.GuiConfig;
import com.ghostchu.quickshop.menu.browse.MarketUtils;
import com.ghostchu.quickshop.menu.shared.GuiChatAction;
import com.ghostchu.quickshop.menu.shared.PageSwitchWithCloseAction;
import com.ghostchu.quickshop.menu.shared.QuickShopPage;
import com.ghostchu.quickshop.shop.SimpleInfo;
import com.ghostchu.quickshop.shop.inventory.BukkitInventoryWrapper;
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.Component;
import net.tnemc.item.AbstractItemStack;
import net.tnemc.item.providers.SkullProfile;
import net.tnemc.menu.core.builder.IconBuilder;
import net.tnemc.menu.core.callbacks.page.PageOpenCallback;
import net.tnemc.menu.core.icon.action.impl.RunnableAction;
import net.tnemc.menu.core.viewer.MenuViewer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ghostchu.quickshop.menu.ShopKeeperMenu.SHOP_STOCK_ID;

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

        final EconomyProvider eco = QuickShop.getInstance().getEconomyManager().provider();

        // Load GUI configuration
        final GuiConfig.MenuConfig menuConfig = QuickShop.getInstance().getGuiConfig().getMenuConfig("trade");
        final GuiConfig.IconConfig borderConfig = (menuConfig != null)? menuConfig.getIcon("border") : null;
        final GuiConfig.IconConfig customAmountConfig = (menuConfig != null)? menuConfig.getIcon("custom-amount") : null;
        final GuiConfig.IconConfig quantityConfig = (menuConfig != null)? menuConfig.getIcon("quantity-buttons") : null;
        final GuiConfig.IconConfig shopItemConfig = (menuConfig != null)? menuConfig.getIcon("shop-item") : null;
        final GuiConfig.IconConfig infoStockConfig = (menuConfig != null)? menuConfig.getIcon("info-stock") : null;
        final GuiConfig.IconConfig infoPriceConfig = (menuConfig != null)? menuConfig.getIcon("info-price") : null;
        final GuiConfig.IconConfig infoSellerConfig = (menuConfig != null)? menuConfig.getIcon("info-seller") : null;
        final GuiConfig.IconConfig closeConfig = (menuConfig != null)? menuConfig.getIcon("close") : null;

        // Set up our borders from config (gray for modern look)
        final String borderMaterial = (borderConfig != null)? borderConfig.getMaterial() : "GRAY_STAINED_GLASS_PANE";
        final IconBuilder borderBuilder = new IconBuilder(QuickShop.getInstance().stack().of(borderMaterial, 1));
        // Get border rows from config or use defaults (rows 1, 4, 6 for modern 6-row layout)
        final List<Integer> borderRows = (borderConfig != null)? borderConfig.getRows() : List.of(1, 4, 6);
        for(final int row : borderRows) {
          open.getPage().setRow(row, borderBuilder);
        }

        final ItemStack shopItem = shop.get().getItem();
        final int amount = shopItem.getAmount();

        final int remainingStock = (int)viewer.get().dataOrDefault(SHOP_STOCK_ID, MarketUtils.getStockFromCache(shop.get()));
        final QuickShop plugin = QuickShop.getInstance();
        final String baseKey = (shop.get().isStackingShop())? shop.get().shopType().stackTradingTranslationKey()
                                                            : shop.get().shopType().tradingTranslationKey();
        final String finalKey = (shop.get().shopState().overrideShopTypeText())? shop.get().shopState().translationKey() : baseKey;

        final Component stockComponent = switch (remainingStock) {

          case -1 -> plugin.text().of(player, finalKey, plugin.text().of(player, "signs.unlimited").forLocale()).forLocale();

          case 0 -> (shop.get().shopState().overrideShopTypeText())? plugin.text().of(player, shop.get().shopState().translationKey()).forLocale()
                                                                   : plugin.text().of(player, shop.get().shopType().outOfStockTranslationKey()).forLocale();

          default -> Component.text(remainingStock);
        };
        final String priceFormatted = shop.get().format(shop.get().bukkitLocation().getWorld().getName(), shop.get().getCurrency());

        // Shop item display slot from config (centered in row 2)
        final int shopItemSlot = (shopItemConfig != null)? shopItemConfig.getSlot() : 13;

        final ItemPreviewComponentPrePopulateEvent previewComponentPrePopulateEvent = new ItemPreviewComponentPrePopulateEvent(shopItem, player);
        previewComponentPrePopulateEvent.callEvent();
        final AbstractItemStack<ItemStack> shopItemStack = QuickShop.getInstance().stack(previewComponentPrePopulateEvent.getItemStack());

        open.getPage().addIcon(new IconBuilder(shopItemStack).withSlot(shopItemSlot).build());

        // Info icons row (row 3) - Stock info
        final String infoStockMaterial = (infoStockConfig != null)? infoStockConfig.getMaterial() : "CHEST";
        final int infoStockSlot = (infoStockConfig != null)? infoStockConfig.getSlot() : 21;
        open.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(infoStockMaterial, 1)
                                                       .customName(getConfigDisplay(id, infoStockConfig, "<yellow>Stock Information</yellow>"))
                                                       .lore(getConfigLore(id, infoStockConfig, stockComponent)))
                                       .withSlot(infoStockSlot).build());

        // Info icons row - Price info
        final String infoPriceMaterial = infoPriceConfig != null? infoPriceConfig.getMaterial() : "GOLD_INGOT";
        final int infoPriceSlot = infoPriceConfig != null? infoPriceConfig.getSlot() : 22;
        open.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(infoPriceMaterial, 1)
                                                       .customName(getConfigDisplay(id, infoPriceConfig, "<gold>Price Information</gold>"))
                                                       .lore(getConfigLore(id, infoPriceConfig, priceFormatted, amount)))
                                       .withSlot(infoPriceSlot).build());

        // Info icons row - Seller info
        final String infoSellerMaterial = infoSellerConfig != null? infoSellerConfig.getMaterial() : "PLAYER_HEAD";
        final int infoSellerSlot = infoSellerConfig != null? infoSellerConfig.getSlot() : 23;
        SkullProfile sellerProfile = null;
        if(shop.get().getOwner().isRealPlayer()) {
          sellerProfile = new SkullProfile();
          sellerProfile.uuid(shop.get().getOwner().getUniqueId());
        }
        final String ownerName = shop.get().getOwner().getDisplay();
        open.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(infoSellerMaterial, 1)
                                                       .customName(getConfigDisplay(id, infoSellerConfig, "<aqua>Seller Information</aqua>"))
                                                       .lore(getConfigLore(id, infoSellerConfig, ownerName))
                                                       .profile(sellerProfile))
                                       .withSlot(infoSellerSlot).build());

        // Custom amount button from config (NAME_TAG for intuitive "enter amount")
        final String customAmountMaterial = customAmountConfig != null? customAmountConfig.getMaterial() : "NAME_TAG";
        final int customAmountSlot = customAmountConfig != null? customAmountConfig.getSlot() : 43;

        final String enterPath = (shop.get().isSelling())? "trade.enter-buy" : "trade.enter-sell";
        open.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(customAmountMaterial, 1)
                                                       .customName(getConfigDisplay(id, customAmountConfig, "<bold><blue>Custom Order</blue></bold>"))
                                                       .lore(getConfigLore(id, customAmountConfig, amount, stockComponent)))
                                       .withActions(new GuiChatAction((message)->{
                                         if(!message.isEmpty()) {
                                           try {
                                             final int quantity = Integer.parseInt(message);

                                             if(quantity == 0) {
                                               return true;
                                             }

                                             if((quantity % amount) > 0) {
                                               player.sendMessage(guiMessage("trade.invalid-multiple", amount));
                                               return true;
                                             }

                                             final int normalizedQuantity = quantity / amount;

                                             if(!shop.get().isUnlimited() && normalizedQuantity > remainingStock && remainingStock > -1) {
                                               player.sendMessage(guiMessage("trade.invalid-stock"));
                                               return true;
                                             }

                                             if(shop.get().isBuying()) {
                                               final Info info = new SimpleInfo(shop.get().bukkitLocation(), ShopAction.PURCHASE_SELL, null, null, shop.get(), false);
                                               Util.regionThread(shop.get().bukkitLocation(), ()->QuickShop.getInstance().getShopManager().actionBuying(player, new BukkitInventoryWrapper(player.getInventory()), eco, info, shop.get(), normalizedQuantity));
                                             } else {
                                               final Info info = new SimpleInfo(shop.get().bukkitLocation(), ShopAction.PURCHASE_BUY, null, null, shop.get(), false);
                                               Util.regionThread(shop.get().bukkitLocation(), ()->QuickShop.getInstance().getShopManager().actionSelling(player, new BukkitInventoryWrapper(player.getInventory()), eco, info, shop.get(), normalizedQuantity));
                                             }
                                             return true;
                                           } catch(final NumberFormatException ignore) { }
                                         }
                                         player.sendMessage(guiMessage(enterPath, amount));
                                         return true;
                                       }, guiMessage(enterPath, amount), false))  // Don't reopen after trade
                                       .withSlot(customAmountSlot).build());

        // Quantity buttons from config - color-coded (LIME_CONCRETE for buy, ORANGE_CONCRETE for sell)
        final String quantityMaterialBuy = quantityConfig != null && quantityConfig.section() != null
                                           ? quantityConfig.section().getString("material-buy", "LIME_CONCRETE") : "LIME_CONCRETE";
        final String quantityMaterialSell = quantityConfig != null && quantityConfig.section() != null
                                            ? quantityConfig.section().getString("material-sell", "ORANGE_CONCRETE") : "ORANGE_CONCRETE";
        final String quantityMaterial = shop.get().isSelling()? quantityMaterialBuy : quantityMaterialSell;

        final List<Integer> configQuantities = quantityConfig != null? quantityConfig.getQuantities() : List.of(1, 2, 4, 8, 16, 64);
        final List<Integer> configSlots = quantityConfig != null? quantityConfig.getSlots() : List.of(37, 38, 39, 40, 41, 42);

        for(int i = 0; i < configQuantities.size() && i < configSlots.size(); i++) {

          final int quantity = configQuantities.get(i);
          final int slot = configSlots.get(i);
          final int adjustedAmount = (amount * quantity);
          final String totalPrice = shop.get().format(shop.get().bukkitLocation().getWorld().getName(),
                                                      shop.get().getCurrency(), quantity);

          final Component displayComponent = (shop.get().isSelling())? getConfigDisplay(id, quantityConfig, "display-buy", "<green>Buy x" + adjustedAmount + "</green>", adjustedAmount)
                                                                     : getConfigDisplay(id, quantityConfig, "display-sell", "<gold>Sell x" + adjustedAmount + "</gold>", adjustedAmount);

          final boolean closeAfter = quantityConfig.section() != null && quantityConfig.section().getBoolean("close-after", true);
          final int page = (closeAfter)? -1 : 1;

          open.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(quantityMaterial, Math.min(adjustedAmount, 64))
                                                         .customName(displayComponent)
                                                         .lore(getConfigLore(id, quantityConfig, totalPrice)))
                                         .withActions(new RunnableAction((click->{
                                           if(shop.get().isBuying()) {

                                             final Info info = new SimpleInfo(shop.get().bukkitLocation(), ShopAction.PURCHASE_SELL, null, null, shop.get(), false);
                                             Util.regionThread(shop.get().bukkitLocation(), ()->QuickShop.getInstance().getShopManager().actionBuying(player, new BukkitInventoryWrapper(player.getInventory()), eco, info, shop.get(), quantity));
                                           } else {

                                             final Info info = new SimpleInfo(shop.get().bukkitLocation(), ShopAction.PURCHASE_BUY, null, null, shop.get(), false);
                                             Util.regionThread(shop.get().bukkitLocation(), ()->QuickShop.getInstance().getShopManager().actionSelling(player, new BukkitInventoryWrapper(player.getInventory()), eco, info, shop.get(), quantity));
                                           }

                                           if (closeAfter) {
                                             viewer.get().close(QuickShop.getInstance().createMenuPlayer(player));
                                           }
                                         })))
                                         .withActions(new PageSwitchWithCloseAction("qs:trade", page))
                                         .withSlot(slot).build());
        }

        // Close button - centered at bottom (slot 49)
        final String closeMaterial = closeConfig != null? closeConfig.getMaterial() : "BARRIER";
        final int closeSlot = closeConfig != null? closeConfig.getSlot() : 49;
        open.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(closeMaterial, 1)
                                                       .customName(getConfigDisplay(id, closeConfig, "<red>Close</red>")))
                                       .withActions(new RunnableAction((click->viewer.get().close(QuickShop.getInstance().createMenuPlayer(player)))))
                                       .withSlot(closeSlot).build());
      }
    }
  }
}