package com.ghostchu.quickshop.api.event;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemPreviewComponentPrePopulateEvent extends AbstractQSEvent {

    private ItemStack itemStack;

    public ItemPreviewComponentPrePopulateEvent(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
}
