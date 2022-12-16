package com.ghostchu.quickshop.addon.plan;

import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.annotation.*;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.table.Table;
import com.ghostchu.quickshop.addon.plan.util.DataUtil;
import com.ghostchu.quickshop.addon.plan.util.DateUtil;
import com.ghostchu.quickshop.addon.plan.util.MetricQuery;
import com.ghostchu.quickshop.api.database.ShopMetricRecord;
import com.ghostchu.quickshop.api.database.bean.DataRecord;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.database.SimpleDatabaseHelperV2;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@PluginInfo(name = "QuickShop-Hikari", iconName = "credit-card", iconFamily = Family.REGULAR, color = Color.LIGHT_BLUE)
@TabInfo(tab = "Summary", iconName = "info-circle", iconFamily = Family.SOLID, elementOrder = {})
@TabInfo(tab = "Purchases", iconName = "inbox", elementOrder = {})
@TabInfo(tab = "Shops", iconName = "list", iconFamily = Family.SOLID, elementOrder = {})
@TabOrder({"Summary", "Purchases", "Shops"})
public class HikariDataExtension implements DataExtension {
    private final Main main;
    private final MetricQuery metricQuery;
    private final DataUtil dataUtil;
    private final DecimalFormat df = new DecimalFormat("#.00");

    public HikariDataExtension(Main main) {
        this.main = main;
        this.dataUtil = new DataUtil(main);
        this.metricQuery = new MetricQuery(main.getQuickShop(), (SimpleDatabaseHelperV2) main.getQuickShop().getDatabaseHelper());
    }

    @Override
    public CallEvents[] callExtensionMethodsOn() {
        return new CallEvents[]{
                CallEvents.PLAYER_JOIN,
                CallEvents.PLAYER_LEAVE,
                CallEvents.SERVER_PERIODICAL};
    }

    // ======================= GLOBAL PROVIDERS =======================

    @StringProvider(text = "Total shops", description = "How many shops exists on this server", iconName = "hashtable", iconColor = Color.GREEN, priority = 100, showInPlayerTable = false)
    @Tab("Summary")
    public String shopCreated() {
        return String.valueOf(main.getQuickShop().getShopManager().getAllShops().size());
    }

    @Tab("Summary")
    @StringProvider(text = "Total purchases", description = "Number of purchases on this server", iconName = "hashtable", iconColor = Color.GREEN, priority = 100, showInPlayerTable = false)
    public String totalTransactions() {
        return String.valueOf(metricQuery.queryServerPurchaseCount());
    }

    @TableProvider(tableColor = Color.BLUE)
    @Tab("Purchases")
    public Table purchasesTab() {
        Table.Factory tableBuilder = Table.builder()
                .columnOne("Player", Icon.called("user").build())
                .columnTwo("Action", Icon.called("code-branch").build())
                .columnThree("Shop", Icon.called("shopping-cart").build())
                .columnFour("Item(amount)", Icon.called("box").build())
                .columnFive("Balance", Icon.called("money-bill-wave").build());
        List<ShopMetricRecord> records = this.metricQuery.queryServerPurchaseRecords(DateUtil.daysAgo(365), 1000, true).stream().filter(record -> switch (record.getType()) {
            //noinspection deprecation
            case PURCHASE, PURCHASE_BUYING_SHOP, PURCHASE_SELLING_SHOP -> true;
            default -> false;
        }).toList();
        try {
            LinkedHashMap<ShopMetricRecord, DataRecord> recordsMapped = this.metricQuery.mapToDataRecord(records);
            recordsMapped.forEach((metric, data) -> {
                String player = dataUtil.getPlayerName(metric.getPlayer());
                String action = CommonUtil.prettifyText(metric.getType().name());
                String shop = dataUtil.getShopName(metric, data);
                String item = dataUtil.getItemName(data) + " (" + metric.getAmount() + ")";
                String balance = dataUtil.formatEconomy(metric);
                tableBuilder.addRow(player, action, shop, item, balance);
            });
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return tableBuilder.build();
    }


    @TableProvider(tableColor = Color.CYAN)
    @Tab("Shops")
    public Table shopsTab() {
        Table.Factory tableBuilder = Table.builder()
                .columnOne("Owner", Icon.called("user").build())
                .columnTwo("Item", Icon.called("shopping-cart").build())
                .columnThree("Price", Icon.called("money-bill-wave").build())
                .columnFour("Type", Icon.called("code-branch").build())
                .columnFive("Location", Icon.called("location-arrow").build());
        for (Shop shop : main.getQuickShop().getShopManager().getAllShops()) {
            String owner = PlainTextComponentSerializer.plainText().serialize(shop.ownerName());
            String item = dataUtil.getItemName(shop.getItem()) + " x" + shop.getShopStackingAmount();
            String price = df.format(shop.getPrice());
            if (main.getQuickShop().getEconomy() != null) {
                price = main.getQuickShop().getEconomy().format(shop.getPrice(), shop.getLocation().getWorld(), shop.getCurrency());
            }
            String type = switch (shop.getShopType()) {
                case BUYING -> "Buying";
                case SELLING -> "Selling";
            };
            String location = dataUtil.loc2String(shop.getLocation());
            tableBuilder.addRow(owner, item, price, type, location);
        }
        return tableBuilder.build();
    }


    // ======================= PLAYER PROVIDERS =======================

    @StringProvider(text = "Owned shops", description = "How many shops created and exists by this player", iconName = "hashtable", iconColor = Color.GREEN, priority = 100, showInPlayerTable = true)
    public String shopCreatedPlayer(UUID playerUUID) {
        return String.valueOf(main.getQuickShop().getShopManager().getPlayerAllShops(playerUUID).size());
    }

    @TableProvider(tableColor = Color.BLUE)
    @Tab("Purchases")
    public Table purchasesTabPlayer(UUID playerUUID) {
        Table.Factory tableBuilder = Table.builder()
                .columnOne("Action", Icon.called("code-branch").build())
                .columnTwo("Location", Icon.called("location-arrow").build())
                .columnThree("Item(amount)", Icon.called("box").build())
                .columnFour("Balance", Icon.called("money-bill-wave").build());

        List<ShopMetricRecord> records = this.metricQuery.queryServerPurchaseRecords(DateUtil.daysAgo(365), 50, true).stream().filter(record -> switch (record.getType()) {
            //noinspection deprecation
            case PURCHASE, PURCHASE_BUYING_SHOP, PURCHASE_SELLING_SHOP -> true;
            default -> false;
        }).toList();
        try {
            LinkedHashMap<ShopMetricRecord, DataRecord> recordsMapped = this.metricQuery.mapToDataRecord(records);
            recordsMapped.forEach((metric, data) -> {
                String action = CommonUtil.prettifyText(metric.getType().name());
                String shop = dataUtil.getShopName(metric, data);
                String item = dataUtil.getItemName(data) + " (" + metric.getAmount() + ")";
                String balance = dataUtil.formatEconomy(metric);
                tableBuilder.addRow(action, shop, item, balance);
            });
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return tableBuilder.build();
    }

    @TableProvider(tableColor = Color.CYAN)
    @Tab("Shops")
    public Table shopsTabPlayer(UUID playerUUID) {
        Table.Factory tableBuilder = Table.builder()
                .columnOne("Item", Icon.called("shopping-cart").build())
                .columnTwo("Price", Icon.called("money-bill-wave").build())
                .columnThree("Type", Icon.called("code-branch").build())
                .columnFour("Location", Icon.called("location-arrow").build());
        for (Shop shop : main.getQuickShop().getShopManager().getPlayerAllShops(playerUUID)) {
            String item = dataUtil.getItemName(shop.getItem()) + " x" + shop.getShopStackingAmount();
            String price = df.format(shop.getPrice());
            if (main.getQuickShop().getEconomy() != null) {
                price = main.getQuickShop().getEconomy().format(shop.getPrice(), shop.getLocation().getWorld(), shop.getCurrency());
            }
            String type = switch (shop.getShopType()) {
                case BUYING -> "Buying";
                case SELLING -> "Selling";
            };
            String location = dataUtil.loc2String(shop.getLocation());
            tableBuilder.addRow(item, price, type, location);
        }
        return tableBuilder.build();
    }
}
