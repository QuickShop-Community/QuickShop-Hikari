package com.ghostchu.quickshop.util.paste.item;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopsInfoItem implements SubPasteItem {
    private final String totalShops;
    private final Map<String, List<Shop>> shopsMapping = new HashMap<>();

    public ShopsInfoItem() {
        QuickShop plugin = QuickShop.getInstance();
        this.totalShops = String.valueOf(plugin.getShopManager().getAllShops().size());
        plugin.getShopManager().getAllShops().stream()
                .filter(shop -> shop.getLocation().getWorld() != null)
                .forEach(shop -> {
                    List<Shop> worldShops = shopsMapping.get(shop.getLocation().getWorld().getName());
                    if (worldShops == null) {
                        worldShops = new ArrayList<>();
                    }

                    worldShops.add(shop);
                    shopsMapping.put(shop.getLocation().getWorld().getName(), worldShops);
                });
    }

    @Override
    public @NotNull String genBody() {
        return buildContent();
    }

    @Override
    public @NotNull String getTitle() {
        return "Shops Information";
    }

    @NotNull
    private String buildContent() {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<p>Total Shops: ").append(totalShops).append("</p>");
        htmlBuilder.append("<h5>Shops in world</h5>");
        htmlBuilder.append("<ul>");
        shopsMapping.keySet().forEach(worldName -> htmlBuilder.append("<li>").append(worldName).append(": ").append(shopsMapping.get(worldName).size()).append("</li>"));
        htmlBuilder.append("</ul>");
        return htmlBuilder.toString();
    }
}
