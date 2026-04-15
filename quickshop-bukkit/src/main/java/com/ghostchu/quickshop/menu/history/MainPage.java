package com.ghostchu.quickshop.menu.history;
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
import com.ghostchu.quickshop.api.database.bean.DataRecord;
import com.ghostchu.quickshop.api.localization.text.ProxiedLocale;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.config.GuiConfig;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.shop.history.ShopHistory;
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.Component;
import net.tnemc.item.AbstractItemStack;
import net.tnemc.item.bukkit.BukkitItemStack;
import net.tnemc.item.providers.SkullProfile;
import net.tnemc.menu.core.builder.IconBuilder;
import net.tnemc.menu.core.callbacks.page.PageOpenCallback;
import net.tnemc.menu.core.icon.action.IconAction;
import net.tnemc.menu.core.icon.action.impl.DataAction;
import net.tnemc.menu.core.icon.action.impl.SwitchPageAction;
import net.tnemc.menu.core.viewer.MenuViewer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.ghostchu.quickshop.menu.ShopHistoryMenu.HISTORY_RECORDS;
import static com.ghostchu.quickshop.menu.ShopHistoryMenu.HISTORY_SUMMARY;
import static com.ghostchu.quickshop.menu.ShopHistoryMenu.SHOPS_DATA;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.get;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.getConfigDisplay;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.getConfigLore;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.getPlayer;

/**
 * MainPage
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class MainPage {

  protected final String returnMenu;
  protected final String menuName;
  protected final int menuPage;
  protected final int returnPage;
  protected final String staffPageID;
  protected final int menuRows;
  protected final String iconLore;
  protected final IconAction[] actions;

  public MainPage(final String returnMenu, final String menuName,
                  final int menuPage, final int returnPage, final String staffPageID,
                  final int menuRows, final String iconLore, final IconAction... actions) {

    this.returnMenu = returnMenu;
    this.menuName = menuName;
    this.menuPage = menuPage;
    this.returnPage = returnPage;
    this.staffPageID = staffPageID;
    this.iconLore = iconLore;
    this.actions = actions;

    //we need a controller row and then at least one row for items.
    this.menuRows = (menuRows <= 1)? 2 : menuRows;
  }

  public void handle(final PageOpenCallback callback) {

    final Optional<MenuViewer> viewer = callback.getPlayer().viewer();
    if(viewer.isPresent()) {

      final Optional<Object> shopsData = viewer.get().findData(SHOPS_DATA);
      final Optional<Object> historyData = viewer.get().findData(HISTORY_RECORDS);
      final Optional<Object> summaryData = viewer.get().findData(HISTORY_SUMMARY);
      final Player player = Bukkit.getPlayer(viewer.get().uuid());
      if(shopsData.isPresent() && historyData.isPresent() && summaryData.isPresent() && player != null) {

        final ProxiedLocale locale = QuickShop.getInstance().getTextManager().findRelativeLanguages(player);

        callback.getPage().getIcons().clear();
        final UUID id = viewer.get().uuid();

        // Load GUI configuration
        final GuiConfig.MenuConfig menuConfig = QuickShop.getInstance().getGuiConfig().getMenuConfig("history");
        final GuiConfig.IconConfig borderConfig = (menuConfig != null)? menuConfig.getIcon("border") : null;
        final GuiConfig.IconConfig entryConfig = (menuConfig != null)? menuConfig.getIcon("entry") : null;
        final GuiConfig.IconConfig summaryConfig = (menuConfig != null)? menuConfig.getIcon("summary") : null;
        final GuiConfig.IconConfig shopInfoConfig = (menuConfig != null)? menuConfig.getIcon("shop-info") : null;
        final GuiConfig.IconConfig multiShopConfig = (menuConfig != null)? menuConfig.getIcon("multi-shop-info") : null;
        final GuiConfig.IconConfig topCustomersConfig = (menuConfig != null)? menuConfig.getIcon("top-customers") : null;
        final GuiConfig.IconConfig prevPageConfig = (menuConfig != null)? menuConfig.getIcon("previous-page") : null;
        final GuiConfig.IconConfig nextPageConfig = (menuConfig != null)? menuConfig.getIcon("next-page") : null;
        final GuiConfig.IconConfig backConfig = (menuConfig != null)? menuConfig.getIcon("back") : null;

        final int listStartSlot = (menuConfig != null)? menuConfig.getSection().getInt("list-start-slot", 9) : 9;

        final int offset = 9;
        final int page = (Integer)viewer.get().dataOrDefault(staffPageID, 1);
        final int items = (menuRows - 1) * offset;
        final int start = ((page - 1) * offset);

        final List<Shop> shops = (ArrayList<Shop>)shopsData.get();
        final List<ShopHistory.ShopHistoryRecord> queryResult = (List<ShopHistory.ShopHistoryRecord>)historyData.get();
        final ShopHistory.ShopSummary summary = (ShopHistory.ShopSummary)summaryData.get();

        final int maxPages = (queryResult.size() / items) + (((queryResult.size() % items) > 0)? 1 : 0);

        final int prev = (page <= 1)? maxPages : page - 1;
        final int next = (page >= maxPages)? 1 : page + 1;

        // Set up borders from config
        final String borderMaterial = (borderConfig != null)? borderConfig.getMaterial() : "GRAY_STAINED_GLASS_PANE";
        final IconBuilder borderBuilder = new IconBuilder(QuickShop.getInstance().stack().of(borderMaterial, 1));
        final List<Integer> borderRows = (borderConfig != null)? borderConfig.getRows() : List.of(1, 6);
        for(final int row : borderRows) {
          callback.getPage().setRow(row, borderBuilder);
        }

        //header icon
        final Shop shop = shops.getFirst();
        final String world = (shop.bukkitLocation().getWorld() != null)? shop.bukkitLocation().getWorld().getName() : "World";

        final Component shopName;
        if(shop.getShopName() != null) {

          shopName = QuickShop.getInstance().text().of("history.shop.header-icon-shop-name", shop.getShopName()).forLocale();
        } else {
          shopName = QuickShop.getInstance().text().of("history.shop.header-icon-shop-empty-name", world, shop.bukkitLocation().getBlockX(), shop.bukkitLocation().getBlockY(), shop.bukkitLocation().getBlockZ()).forLocale();
        }

        final Component shopType = QuickShop.getInstance().text().of(shop.shopType().translationKey()).forLocale();

        // Shop info icon from config
        final String shopInfoMaterial = (shopInfoConfig != null)? shopInfoConfig.getMaterial() : "PLAYER_HEAD";
        final int shopInfoSlot = (shopInfoConfig != null)? shopInfoConfig.getSlot() : 4;
        final String multiShopMaterial = (multiShopConfig != null)? multiShopConfig.getMaterial() : "CHEST";
        final int multiShopSlot = (multiShopConfig != null)? multiShopConfig.getSlot() : 4;

        if(shops.size() == 1) {

          final QUser owner = shop.getOwner();
          SkullProfile ownerProfile = null;
          if(owner.isRealPlayer() && owner.getUniqueId() != null) {

            ownerProfile = new SkullProfile();
            ownerProfile.setUuid(owner.getUniqueId());
          }

          callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(shopInfoMaterial, 1)
                                                             .display(shopName)
                                                             .lore(getConfigLore(id, shopInfoConfig,
                                                                                 shopType,
                                                                                 shop.getOwner().getDisplay(),
                                                                                 Util.getItemStackName(shop.getItem()),
                                                                                 shop.price(), shop.getShopStackingAmount(),
                                                                                 shop.bukkitLocation().getWorld().getName() + " " + shop.bukkitLocation().getBlockX()
                                                                                 + ", " + shop.bukkitLocation().getBlockY() + ", "
                                                                                 + shop.bukkitLocation().getBlockZ()))
                                                             .profile(ownerProfile))
                                             .withActions(new SwitchPageAction(returnMenu, returnPage))
                                             .withSlot(shopInfoSlot)
                                             .build());
        } else {

          callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(multiShopMaterial, 1)
                                                             .display(getConfigDisplay(id, multiShopConfig, "<yellow>Multiple Shops ({0})</yellow>", shops.size())))
                                             .withSlot(multiShopSlot)
                                             .build());
        }

        // Summary icon from config
        final String summaryMaterial = (summaryConfig != null)? summaryConfig.getMaterial() : "OAK_SIGN";
        final int summarySlot = (summaryConfig != null)? summaryConfig.getSlot() : 0;

        callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(summaryMaterial, 1)
                                                           .display(getConfigDisplay(id, summaryConfig, "<yellow>Summary</yellow>"))
                                                           .lore(getConfigLore(id, summaryConfig, locale.getNumberFormat().format(summary.totalPurchases()),
                                                                               locale.getNumberFormat().format(summary.uniquePurchasers()),
                                                                               hours(id, 24), locale.getNumberFormat().format(summary.recentPurchases24h()),
                                                                               days(id, 3), locale.getNumberFormat().format(summary.recentPurchases3d()),
                                                                               days(id, 7), locale.getNumberFormat().format(summary.recentPurchases7d()),
                                                                               days(id, 30), locale.getNumberFormat().format(summary.recentPurchases30d()),
                                                                               locale.getNumberFormat().format(summary.totalPurchases()),
                                                                               hours(id, 24), locale.getNumberFormat().format(summary.recentPurchasesBalance24h()),
                                                                               days(id, 3), locale.getNumberFormat().format(summary.recentPurchasesBalance3d()),
                                                                               days(id, 7), locale.getNumberFormat().format(summary.recentPurchasesBalance7d()),
                                                                               days(id, 30), locale.getNumberFormat().format(summary.recentPurchasesBalance30d()),
                                                                               locale.getNumberFormat().format(summary.totalBalance())
                                                                               )))
                                           .withSlot(summarySlot)
                                           .build());

        // Top customers icon from config
        final String topCustomersMaterial = (topCustomersConfig != null)? topCustomersConfig.getMaterial() : "DIAMOND";
        final int topCustomersSlot = (topCustomersConfig != null)? topCustomersConfig.getSlot() : 8;

        final List<Component> valuableDescription = new ArrayList<>(summary.valuableCustomers().size());
        for(final Map.Entry<UUID, Long> entry : summary.valuableCustomers().entrySet()) {
          valuableDescription.addAll(getConfigLore(id, topCustomersConfig, QUserImpl.createSync(QuickShop.getInstance().getPlayerFinder(),
                                                                                         entry.getKey()).getDisplay(), entry.getValue()));
        }

        callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(topCustomersMaterial, 1)
                                                           .display(getConfigDisplay(id, topCustomersConfig, "<aqua>Top Customers ({0})</aqua>", summary.valuableCustomers().size()))
                                                           .lore(valuableDescription)).withSlot(topCustomersSlot).build());

        // Pagination icons from config
        final String prevPageMaterial = (prevPageConfig != null)? prevPageConfig.getMaterial() : "ARROW";
        final int prevPageSlot = (prevPageConfig != null)? prevPageConfig.getSlot() : 3;
        final String nextPageMaterial = (nextPageConfig != null)? nextPageConfig.getMaterial() : "ARROW";
        final int nextPageSlot = (nextPageConfig != null)? nextPageConfig.getSlot() : 5;

        if(maxPages > 1) {

          callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(prevPageMaterial, 1)
                                                             .display(getConfigDisplay(id, prevPageConfig, "<white><< Previous Page</white>"))
                                                             .lore(getConfigLore(id, prevPageConfig, page)))
                                             .withActions(new DataAction(staffPageID, prev), new SwitchPageAction(menuName, menuPage))
                                             .withSlot(prevPageSlot)
                                             .build());

          callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(nextPageMaterial, 1)
                                                             .display(getConfigDisplay(id, nextPageConfig, "<white>Next Page >></white>"))
                                                             .lore(getConfigLore(id, nextPageConfig, page)))
                                             .withActions(new DataAction(staffPageID, next), new SwitchPageAction(menuName, menuPage))
                                             .withSlot(nextPageSlot)
                                             .build());
        }

        int i = 0;
        for(final ShopHistory.ShopHistoryRecord record : queryResult) {
          final String userName = QUserImpl.createSync(QuickShop.getInstance().getPlayerFinder(), record.buyer()).getDisplay();
          final DataRecord dataRecord = QuickShop.getInstance().getDatabaseHelper().getDataRecord(record.dataId()).join();

          if(i < start) {

            i++;

            continue;
          }
          if(i >= (start + items)) break;

          if(dataRecord == null) continue;

          ItemStack historyItem = QuickShop.getInstance().platform().decodeStack(dataRecord.getEncoded());
          if(historyItem == null) {

            //try the old serialization for old shops.
            try {
              historyItem = Util.deserialize(dataRecord.getItem());
            } catch(final Exception ignore) {
            }

            if(historyItem == null) {

              historyItem = new ItemStack(Material.STONE);
              final ItemMeta meta = historyItem.getItemMeta();
              if(meta != null) {

                meta.setDisplayName("Failed to deserialize item");
                historyItem.setItemMeta(meta);
              }
            }
          }
          final String type = historyItem.getType().getKey().getKey();
          final Component itemName = Util.getItemStackName(historyItem);
          final int max = historyItem.getMaxStackSize();

          final String timeFormat = "yyyy-MM-dd HH:mm";
          final SimpleDateFormat format = new SimpleDateFormat(timeFormat);
          final String dateStr = format.format(record.date());

          AbstractItemStack<ItemStack> stack = new BukkitItemStack();

          if(shops.size() == 1) {
            stack = stack.of("PLAYER_HEAD", Math.min(max, record.amount()));

            final Optional<OfflinePlayer> offline = getPlayer(record.buyer());
            if(offline.isPresent() && offline.get().hasPlayedBefore()) {

              final SkullProfile profile = new SkullProfile();
              profile.setUuid(record.buyer());
              stack = stack.profile(profile);
            }
          } else {
            stack = stack.of(type, Math.min(max, record.amount()));
          }
          stack = stack.display(getConfigDisplay(id, entryConfig, "<yellow>{0}</yellow>", dateStr));
          stack = stack.lore(getConfigLore(id, entryConfig, shopName, userName, itemName,
                                           record.amount(), record.money(), record.tax(),
                                           record.money() - record.tax()));

          callback.getPage().addIcon(new IconBuilder(stack).withSlot(listStartSlot + (i - start)).build());

          i++;
        }
      }
    }
  }

  private Component hours(final UUID id, final int hours) {

    return get(id, "timeunit.hours", hours);
  }

  private Component days(final UUID id, final int days) {

    return get(id, "timeunit.days", days);
  }
}