package com.ghostchu.quickshop.util.paste.item;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.FastPlayerFinder;
import com.ghostchu.quickshop.util.paste.GuavaCacheRender;
import com.google.common.cache.CacheStats;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;

public class CachePerformanceItem implements SubPasteItem {
    private final QuickShop plugin = QuickShop.getInstance();

    @Override
    public @NotNull String genBody() {
        return "<h5>Player Lookup Cache</h5>" +
                buildPlayerLookupCache();
    }


    private String buildPlayerLookupCache() {
        CacheStats stats = ((FastPlayerFinder) plugin.getPlayerFinder()).getNameCache().stats();
        return renderTable(stats);
    }

    @NotNull
    private String renderTable(@NotNull CacheStats stats) {
        return GuavaCacheRender.renderTable(stats);
    }

    @Override
    public @NotNull String getTitle() {
        return "Cache Performance";
    }

    @NotNull
    private String round(double d) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(3);
        return nf.format(d);
    }
}
