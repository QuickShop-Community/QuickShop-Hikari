package com.ghostchu.quickshop.shop;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Storage the extra data that QuickShop needs or from 3rd-addon
 */
@Data
public class SimpleShopExtra {
    private @NotNull String namespace;
    private @NotNull Map<String, Object> data;

    public SimpleShopExtra(@NotNull String namespace, @NotNull Map<String, Object> data) {
        this.namespace = namespace;
        this.data = data;
    }
}
