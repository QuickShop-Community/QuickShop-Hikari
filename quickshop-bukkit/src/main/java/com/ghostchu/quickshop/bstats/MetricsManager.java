package com.ghostchu.quickshop.bstats;

import com.ghostchu.quickshop.QuickShop;

public class MetricsManager {
    private Metrics metrics;
    private final QuickShop plugin;
    private boolean isMetricsEnabled;

    public MetricsManager(QuickShop plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        this.isMetricsEnabled = plugin.getConfig().getBoolean("disabled-metrics");
        if (isMetricsEnabled) {
            this.metrics = new Metrics(plugin.getJavaPlugin(), 14281);
        } else {
            plugin.logger().info("You have disabled metrics, Skipping...");
        }
    }
}
