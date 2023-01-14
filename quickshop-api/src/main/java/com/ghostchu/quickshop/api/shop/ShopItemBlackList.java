package com.ghostchu.quickshop.api.shop;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ShopItemBlackList {
    boolean isBlacklisted(@NotNull ItemStack itemStack);
}
