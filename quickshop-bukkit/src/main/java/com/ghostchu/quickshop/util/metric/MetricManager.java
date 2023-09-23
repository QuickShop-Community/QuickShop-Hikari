package com.ghostchu.quickshop.util.metric;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.metric.collect.BuiltInCollects;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.CustomChart;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Locale;

public class MetricManager {
    private final QuickShop plugin;
    private final Metrics metrics;

    public MetricManager(QuickShop plugin) {
        this.plugin = plugin;
        this.metrics = new Metrics(plugin.getJavaPlugin(), 14281);
        initCollects();
    }

    public void registerChart(MetricDataType dataType, String moduleName, String reason, CustomChart chart) {
        if (chart == null) return; // ignore
        plugin.getPrivacyController().privacyReview(dataType, moduleName.replace(" ", "_").replace("-", "_").toUpperCase(Locale.ROOT), reason, () -> this.metrics.addCustomChart(chart), () -> Log.debug("Blocked chart register: failed privacy reviewing."));
    }

    public void initCollects() {
        registerMetricCollector(new BuiltInCollects(plugin));
    }

    public void registerMetricCollector(@NotNull Object object) {
        for (Method method : object.getClass().getDeclaredMethods()) {
            MetricCollectEntry collectEntry = method.getAnnotation(MetricCollectEntry.class);
            if (collectEntry == null) {
                continue;
            }
            if (method.getReturnType() != CustomChart.class) {
                plugin.logger().warn("Failed loading MetricCollectEntry [{}]: Illegal test returns", method.getName());
                continue;
            }
            try {
                Object result = method.invoke(object, (Object[]) null);
                if (result != null) {
                    registerChart(collectEntry.dataType(), collectEntry.moduleName(), collectEntry.description(),
                            (CustomChart) result);
                    Log.debug("Registered metrics collector: " + collectEntry.moduleName());
                }
            } catch (Throwable th) {
                plugin.logger().warn("Failed to register metrics chart", th);
            }
        }
    }
}
