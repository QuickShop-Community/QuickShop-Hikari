package com.ghostchu.quickshop.papi;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.papi.impl.MetadataPAPI;
import com.ghostchu.quickshop.papi.impl.PurchasesPAPI;
import com.ghostchu.quickshop.papi.impl.ShopManagerPAPI;
import com.ghostchu.quickshop.papi.impl.TransactionAmountPAPI;
import com.ghostchu.quickshop.util.paste.GuavaCacheRender;
import com.ghostchu.quickshop.util.paste.item.SubPasteItem;
import com.ghostchu.quickshop.util.paste.util.HTMLTable;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

public class PAPIManager implements SubPasteItem {
    private final QuickShop plugin;
    private final List<PAPISubHandler> handlers = new ArrayList<>();
    private final PAPICache cache = new PAPICache();

    public PAPIManager(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        plugin.getPasteManager().register(plugin.getJavaPlugin(), this);
        init();
    }

    private void init() {
        register(new MetadataPAPI(plugin));
        register(new PurchasesPAPI(plugin));
        register(new ShopManagerPAPI(plugin));
        register(new TransactionAmountPAPI(plugin));
    }

    public void register(@NotNull PAPISubHandler handler) {
        for (PAPISubHandler registered : handlers) {
            if (registered.getPrefix().equals(handler.getPrefix())) {
                throw new IllegalStateException("The prefix " + handler.getPrefix() + " is already registered by " + registered.getClass().getName() + "!");
            }
        }
        handlers.add(handler);
    }

    public void unregister(@NotNull PAPISubHandler handler) {
        handlers.remove(handler);
    }

    public void unregister(@NotNull String prefix) {
        handlers.removeIf(handler -> handler.getPrefix().equals(prefix));
    }

    @NotNull
    public List<PAPISubHandler> getHandlers() {
        return new ArrayList<>(handlers);
    }

    @Nullable
    public String handle(@NotNull OfflinePlayer player, @NotNull String params) {
        UUID playerUniqueId = player.getUniqueId();
        return cache.getCached(playerUniqueId, params, (uuid, parms) -> {
            for (PAPISubHandler handler : handlers) {
                if (params.startsWith(handler.getPrefix())) {
                    return handler.handle(player, params);
                }
            }
            return null;
        }).orElse(null);
    }

    @Override
    public @NotNull String genBody() {
        StringJoiner joiner = new StringJoiner("<br/>");
        joiner.add("<h5>Registered Placeholder Handler</h5>");
        HTMLTable table = new HTMLTable(2);
        table.setTableTitle("Prefix", "Handler");
        for (PAPISubHandler handler : handlers) {
            table.insert(handler.getPrefix(), handler.getClass().getName());
        }
        joiner.add(table.render());
        joiner.add("<h5>Caching</h5>");
        joiner.add(GuavaCacheRender.renderTable(cache.getStats()));
        return joiner.toString();
    }

    @Override
    public @NotNull String getTitle() {
        return "PAPI Manager";
    }
}
