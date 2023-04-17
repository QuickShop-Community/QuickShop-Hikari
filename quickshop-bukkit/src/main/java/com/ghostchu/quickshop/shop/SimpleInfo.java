package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.api.shop.Info;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopAction;
import com.ghostchu.quickshop.common.util.JsonUtil;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class contains shop's infomations
 */
@EqualsAndHashCode
@ToString
public class SimpleInfo implements Info {
    private final Block last;
    private final Location loc;
    private final boolean dirty;
    private final boolean bypass;
    private ShopAction action;
    private ItemStack item;
    private Shop shop;
    private String shopData;

    public SimpleInfo(
            @NotNull Location loc,
            @NotNull ShopAction action,
            @Nullable ItemStack item,
            @Nullable Block last,
            boolean bypass) {
        this.loc = loc;
        this.action = action;
        this.last = last;
        this.bypass = bypass;
        if (item != null) {
            this.item = item.clone();
        }
        this.dirty = true;
    }

    public SimpleInfo(
            @NotNull Location loc,
            @NotNull ShopAction action,
            @Nullable ItemStack item,
            @Nullable Block last,
            @Nullable Shop shop,
            boolean bypass) {
        this.loc = loc;
        this.action = action;
        this.last = last;
        this.bypass = bypass;
        if (item != null) {
            this.item = item.clone();
        }
        if (shop != null) {
            this.shop = shop;
            this.shopData = JsonUtil.getGson().toJson(shop.saveToInfoStorage());
            this.dirty = shop.isDirty();
        } else {
            this.dirty = true;
        }
    }

    /**
     * @return ShopAction action, Get shop action.
     */
    @Override
    public @NotNull ShopAction getAction() {
        return this.action;
    }

    @Override
    public void setAction(@NotNull ShopAction action) {
        this.action = action;
    }

    /**
     * @return ItemStack iStack, Get Shop's selling/buying item's ItemStack.
     */
    @Override
    public @NotNull ItemStack getItem() {
        return this.item;
    }


    /**
     * @return Location loc, Get shop's location,
     */
    @Override
    public @NotNull Location getLocation() {
        return this.loc;
    }

    /**
     * @return Block signBlock, Get block of shop's sign, may return the null.
     */
    @Override
    public @Nullable Block getSignBlock() {
        return this.last;
    }

    /**
     * Get shop is or not has changed.
     *
     * @param shop, The need checked with this shop.
     * @return hasChanged
     */
    @Override
    public boolean hasChanged(@NotNull Shop shop) {
        return !this.shopData.equals(JsonUtil.getGson().toJson(shop.saveToInfoStorage()));
    }

    @Override
    public boolean isBypassed() {
        return bypass;
    }

}
