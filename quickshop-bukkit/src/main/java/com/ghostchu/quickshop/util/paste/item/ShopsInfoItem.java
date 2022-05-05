/*
 *  This file is a part of project QuickShop, the name is ShopsInfoItem.java
 *  Copyright (C) Ghost_chu and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
        plugin.getShopManager().getAllShops().forEach(shop -> {
            List<Shop> worldShops = shopsMapping.get(shop.getLocation().getWorld().getName());
            if (worldShops == null) worldShops = new ArrayList<>();
            worldShops.add(shop);
            shopsMapping.put(shop.getLocation().getWorld().getName(), worldShops);
        });
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

    @Override
    public @NotNull String genBody() {
        return buildContent();
    }
}
