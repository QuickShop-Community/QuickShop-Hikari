package com.ghostchu.quickshop.shop.datatype;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ShopProtectionFlag {
    private static final String MARK = "QuickShop DisplayItem";
    private final String itemStackString;
    private final String shopLocation;

    public ShopProtectionFlag(@NotNull String shopLocation, @NotNull String itemStackString) {
        this.shopLocation = shopLocation;
        this.itemStackString = itemStackString;
    }

    public static String getDefaultMark() {
        return MARK;
    }

    public static String getMark() {
        return getDefaultMark();
    }
}
