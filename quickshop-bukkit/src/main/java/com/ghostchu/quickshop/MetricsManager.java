package com.ghostchu.quickshop;

import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import com.ghostchu.simplereloadlib.Reloadable;

public class MetricsManager implements Reloadable {
    private final Metrics metrics;
    private final QuickShop plugin;
    private boolean isMetricsEnabled;

    public MetricsManager(QuickShop plugin) {
        this.plugin = plugin;
        setupMetrics();
    }

    private void setupMetrics() {
        metrics = new Metrics(plugin.getJavaPlugin(), 14281);
        if (!isMetricsEnabled) {
            // Use internal Metric class not Maven for solve plugin name issues
            // Version
            metrics.addCustomChart(new Metrics.SimplePie("use_display_items", () -> CommonUtil.boolean2Status(getConfig().getBoolean("shop.display-items"))));
            metrics.addCustomChart(new Metrics.SimplePie("use_locks", () -> CommonUtil.boolean2Status(getConfig().getBoolean("shop.lock"))));
            metrics.addCustomChart(new Metrics.SimplePie("use_display_auto_despawn", () -> String.valueOf(getConfig().getBoolean("shop.display-auto-despawn"))));
            metrics.addCustomChart(new Metrics.SimplePie("display_type", () -> AbstractDisplayItem.getNowUsing().name()));
            metrics.addCustomChart(new Metrics.SimplePie("itemmatcher_type", () -> this.getItemMatcher().getName()));
            metrics.addCustomChart(new Metrics.SimplePie("use_stack_item", () -> String.valueOf(this.isAllowStack())));
            metrics.addCustomChart(new Metrics.SingleLineChart("shops_created_on_all_servers", () -> this.getShopManager().getAllShops().size()));
        } else {
            getLogger().info("You disabled metrics, Skipping...");
        }
    }

    private void load() {
        this.isMetricsEnabled = plugin.getConfig().getBoolean("disabled-metrics");
    }
}
