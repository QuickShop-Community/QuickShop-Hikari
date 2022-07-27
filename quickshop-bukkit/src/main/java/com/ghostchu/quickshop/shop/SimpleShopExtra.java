package com.ghostchu.quickshop.shop;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Storage the extra data that QuickShop needs or from 3rd-addon
 */
@AllArgsConstructor
@Data
public class SimpleShopExtra {
    private @NotNull String namespace;
    private @NotNull Map<String, Object> data;
}
