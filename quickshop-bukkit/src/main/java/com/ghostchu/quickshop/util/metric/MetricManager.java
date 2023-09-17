package com.ghostchu.quickshop.util.metric;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.logger.Log;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.CustomChart;

public class MetricManager {
    private final QuickShop plugin;
    private final Metrics metrics;

    public MetricManager(QuickShop plugin) {
        this.plugin = plugin;
        this.metrics = new Metrics(plugin.getJavaPlugin(), 14281);
    }

    public void registerChart(MetricDataType dataType, String moduleName, String reason, CustomChart chart) {
        plugin.getPrivacyController().privacyReview(dataType, moduleName, reason, () -> this.metrics.addCustomChart(chart), () -> Log.debug("Blocked chart register: failed privacy reviewing."));
    }
}
