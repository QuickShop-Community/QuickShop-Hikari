package com.ghostchu.quickshop.util.paste.item;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.FastPlayerFinder;
import com.ghostchu.quickshop.util.paste.GuavaCacheRender;
import com.google.common.cache.CacheStats;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;

public class CachePerformanceItem implements SubPasteItem {
    private final QuickShop plugin = QuickShop.getInstance();

    @Override
    public @NotNull String genBody() {
        return "<h5>Shop Cache</h5>" +
                buildShopCacheContent() +
                "<h5>Player Lookup Cache</h5>" +
                buildPlayerLookupCache() +
                "<h5>QUser Reuse Memory Cache</h5>" +
                buildQUserReuseCache();
    }

    private String buildQUserReuseCache() {
        CacheStats stats = QUserImpl.getQuserCache().stats();
        return renderTable(stats);
    }

    @NotNull
    private String buildShopCacheContent() {
        if (plugin.getShopCache() == null) {
            return "<p>Shop Cache disabled.</p>";
        }
        CacheStats stats = plugin.getShopCache().getStats();
        return renderTable(stats);
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
