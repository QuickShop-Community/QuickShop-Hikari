package com.ghostchu.quickshop.database.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@Data
public class DataRecord {
    private final String item;
    private final String name;
    private final int type;
    private final String currency;
    private final double price;
    private final boolean unlimited;
    private final boolean hologram;
    private final UUID taxAccount;
    private final String permissions;
    private final String inventoryWrapper;
    private final String inventorySymbolLink;
    private final long createTime = System.currentTimeMillis();

    @NotNull
    public Map<String, Object> generateParams() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("item", item);
        map.put("name", name);
        map.put("type", type);
        map.put("currency", currency);
        map.put("price", price);
        map.put("unlimited", unlimited);
        map.put("hologram", hologram);
        map.put("tax_account", taxAccount);
        map.put("permissions", permissions);
        map.put("inv_wrapper", inventoryWrapper);
        map.put("inv_symbol_link", inventorySymbolLink);
        map.put("create_time", createTime);
        return map;
    }
}
