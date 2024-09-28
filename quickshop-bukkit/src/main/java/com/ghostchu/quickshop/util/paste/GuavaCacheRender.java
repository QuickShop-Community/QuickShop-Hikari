package com.ghostchu.quickshop.util.paste;

import com.ghostchu.quickshop.util.paste.util.HTMLTable;
import com.google.common.cache.CacheStats;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;

public class GuavaCacheRender {

  @NotNull
  public static String renderTable(@NotNull CacheStats stats) {

    HTMLTable table = new HTMLTable(2, true);
    table.insert("Average Load Penalty", round(stats.averageLoadPenalty()));
    table.insert("Hit Rate", toPercentage(stats.hitRate()));
    table.insert("Miss Rate", toPercentage(stats.missRate()));
    table.insert("Hit Count", String.valueOf(stats.hitCount()));
    table.insert("Miss Count", String.valueOf(stats.missCount()));
    table.insert("Load Count", String.valueOf(stats.loadCount()));
    table.insert("Load Success Count", String.valueOf(stats.loadSuccessCount()));
    table.insert("Eviction Count", String.valueOf(stats.evictionCount()));
    table.insert("Request Count", String.valueOf(stats.requestCount()));
    table.insert("Total Loading Time", String.valueOf(stats.totalLoadTime()));
    return table.render();
  }

  @NotNull
  private static String round(double d) {

    NumberFormat nf = NumberFormat.getNumberInstance();
    nf.setMaximumFractionDigits(3);
    return nf.format(d);
  }

  private static String toPercentage(double n) {

    return String.format("%.000f", n * 100) + "%";
  }

}
