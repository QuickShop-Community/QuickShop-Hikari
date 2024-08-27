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
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.shop.history.ShopHistory;
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
import org.bukkit.configuration.InvalidConfigurationException;
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
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.getList;
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

  public MainPage(String returnMenu, String menuName,
                  final int menuPage, final int returnPage, String staffPageID,
                  final int menuRows, String iconLore, final IconAction... actions) {
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

        //header icon
        final Shop shop = shops.get(0);
        final String world = (shop.getLocation().getWorld() != null)? shop.getLocation().getWorld().getName() : "World";
        final String shopName = shop.getShopName() == null? world + " " + shop.getLocation().getBlockX() + ", " + shop.getLocation().getBlockY() + ", " + shop.getLocation().getBlockZ() : shop.getShopName();
        if(shops.size() == 1) {

          final QUser owner = shop.getOwner();
          SkullProfile ownerProfile = null;
          if(owner.isRealPlayer() && owner.getUniqueId() != null) {

            ownerProfile = new SkullProfile();
            ownerProfile.setUuid(owner.getUniqueId());
          }

          callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("PLAYER_HEAD", 1)
                  .display(LegacyComponentSerializer.legacySection().deserialize(shopName))
                  .lore(getList(id, "history.shop.header-icon-description",
                          shop.getShopType().name(),
                          shop.getOwner().getDisplay(),
                          Util.getItemStackName(shop.getItem()),
                          shop.getPrice(), shop.getShopStackingAmount(),
                          shop.getLocation().getWorld().getName() + " " + shop.getLocation().getBlockX()
                                  + ", " + shop.getLocation().getBlockY() + ", "
                                  + shop.getLocation().getBlockZ()))
                  .profile(ownerProfile))
                  .withActions(new SwitchPageAction(returnMenu, returnPage))
                  .withSlot(4)
                  .build());
        } else {

          callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("CHEST", 1)
                  .display(get(id, "history.shop.header-icon-multiple-shop", shops.size())))
                  .withSlot(4)
                  .build());
        }

        //summary icon
        final List<Component> description = new ArrayList<>();
        description.add(get(id, "history.shop.total-unique-purchasers", locale.getNumberFormat().format(summary.uniquePurchasers())));
        description.add(get(id, "history.shop.recent-purchases", hours(id, 24), locale.getNumberFormat().format(summary.recentPurchases24h())));
        description.add(get(id, "history.shop.recent-purchases", days(id, 3), locale.getNumberFormat().format(summary.recentPurchases3d())));
        description.add(get(id, "history.shop.recent-purchases", days(id, 7), locale.getNumberFormat().format(summary.recentPurchases7d())));
        description.add(get(id, "history.shop.recent-purchases", days(id, 30), locale.getNumberFormat().format(summary.recentPurchases30d())));
        description.add(get(id, "history.shop.total-purchases", locale.getNumberFormat().format(summary.totalPurchases())));
        description.add(get(id, "history.shop.recent-purchase-balance", hours(id, 24), locale.getNumberFormat().format(summary.recentPurchasesBalance24h())));
        description.add(get(id, "history.shop.recent-purchase-balance", days(id, 3), locale.getNumberFormat().format(summary.recentPurchasesBalance3d())));
        description.add(get(id, "history.shop.recent-purchase-balance", days(id, 7), locale.getNumberFormat().format(summary.recentPurchasesBalance7d())));
        description.add(get(id, "history.shop.recent-purchase-balance", days(id, 30), locale.getNumberFormat().format(summary.recentPurchasesBalance30d())));
        description.add(get(id, "history.shop.total-balances", locale.getNumberFormat().format(summary.totalBalance())));

        callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("OAK_SIGN", 1)
                .display(get(id, "history.shop.summary-icon-title"))
                .lore(description))
                .withSlot(0)
                .build());

        final List<Component> valuableDescription = new ArrayList<>(summary.valuableCustomers().size());
        for(Map.Entry<UUID, Long> entry : summary.valuableCustomers().entrySet()) {
          valuableDescription.add(get(id, "history.shop.top-n-valuable-customers-entry",
                  QUserImpl.createSync(QuickShop.getInstance().getPlayerFinder(),
                          entry.getKey()).getDisplay(), entry.getValue()));
        }

        callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("DIAMOND", 1)
                .display(get(id, "history.shop.top-n-valuable-customers-title", summary.valuableCustomers().size()))
                .lore(valuableDescription)).withSlot(8).build());

        if(maxPages > 1) {

          callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("RED_WOOL", 1)
                  .display(get(id, "gui.shared.previous-page"))
                  .lore(List.of(get(id, "history.shop.current-page", page))))
                  .withActions(new DataAction(staffPageID, prev), new SwitchPageAction(menuName, menuPage))
                  .withSlot(3)
                  .build());

          callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("GREEN_WOOL", 1)
                  .display(get(id, "gui.shared.next-page"))
                  .lore(List.of(get(id, "history.shop.current-page", page))))
                  .withActions(new DataAction(staffPageID, next), new SwitchPageAction(menuName, menuPage))
                  .withSlot(5)
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
          int max = 64;
          String type = "STONE";
          Component itemName;
          try {
            ItemStack historyItem = Util.deserialize(dataRecord.getItem());
            if(historyItem == null) {
              historyItem = new ItemStack(Material.STONE);
              final ItemMeta meta = historyItem.getItemMeta();
              if(meta != null) {
                meta.setDisplayName("Failed to deserialize item");
                historyItem.setItemMeta(meta);
              }
            }
            type = historyItem.getType().getKey().getKey();
            itemName = Util.getItemStackName(historyItem);
            max = historyItem.getMaxStackSize();
          } catch(InvalidConfigurationException e) {
            itemName = get(id, "internal-error");
            QuickShop.getInstance().logger().error("Failed to deserialize itemstack {}", dataRecord.getItem(), e);
          }

          final List<Component> lore = getList(id, "history.shop.log-icon-description-with-store-name",
                  shopName,
                  userName,
                  itemName, record.amount(),
                  record.money(),
                  record.tax());

          final String timeFormat = QuickShop.getInstance().text().of(player, "timeunit.std-format").plain();
          final SimpleDateFormat format = new SimpleDateFormat(timeFormat);
          AbstractItemStack<ItemStack> stack = new BukkitItemStack();

          if(shops.size() == 1) {
            stack = stack.of("PLAYER_HEAD", Math.min(max, record.amount()))
                    .display(get(id, "history.shop.log-icon-title",
                            format.format(record.date())))
                    .lore(lore);

            final Optional<OfflinePlayer> offline = getPlayer(record.buyer());
            if(offline.isPresent() && offline.get().hasPlayedBefore()) {

              final SkullProfile profile = new SkullProfile();
              profile.setUuid(record.buyer());
              stack = stack.profile(profile);
            }
          } else {
            stack = stack.of(type, Math.min(max, record.amount()));
          }
          stack = stack.lore(lore);
          stack = stack.display(get(id, "history.shop.log-icon-title", format.format(record.date())));

          callback.getPage().addIcon(new IconBuilder(stack).withSlot(offset + (i - start)).build());

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