/*
 *  This file is a part of project QuickShop, the name is Nova.java
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

package com.ghostchu.quickshop.compatibility.nova;

import com.ghostchu.quickshop.api.shop.AbstractDisplayItem;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.nova.api.protection.ProtectionIntegration;

public final class Main extends CompatibilityModule implements Listener, ProtectionIntegration {

    @Override
    public void onEnable() {
        xyz.xenondevs.nova.api.Nova.getNova().registerProtectionIntegration(this);
        super.onEnable();
    }

    public void init() {

    }

    @Override
    public boolean canBreak(@NotNull OfflinePlayer offlinePlayer, @Nullable ItemStack itemStack, @NotNull Location location) {
        Shop shop = getApi().getShopManager().getShopIncludeAttached(location);
        if(shop == null)
            return true;
        return shop.getOwner().equals(offlinePlayer.getUniqueId());
    }

    @Override
    public boolean canHurtEntity(@NotNull OfflinePlayer offlinePlayer, @NotNull Entity entity, @Nullable ItemStack itemStack) {
        if(itemStack != null)
            if(AbstractDisplayItem.checkIsGuardItemStack(itemStack))
                return false;
        if(entity instanceof Item item)
            return !AbstractDisplayItem.checkIsGuardItemStack(item.getItemStack());
        return true; // We don't care that
    }

    @Override
    public boolean canInteractWithEntity(@NotNull OfflinePlayer offlinePlayer, @NotNull Entity entity, @Nullable ItemStack itemStack) {
        if(itemStack != null)
            if(AbstractDisplayItem.checkIsGuardItemStack(itemStack))
                return false;
        if(entity instanceof Item item)
            return !AbstractDisplayItem.checkIsGuardItemStack(item.getItemStack());
        return true; // We don't care that
    }

    @Override
    public boolean canPlace(@NotNull OfflinePlayer offlinePlayer, @NotNull ItemStack itemStack, @NotNull Location location) {
        Shop shop = getApi().getShopManager().getShopIncludeAttached(location);
        if(shop == null)
            return true;
        return shop.getOwner().equals(offlinePlayer.getUniqueId());
    }

    @Override
    public boolean canUseBlock(@NotNull OfflinePlayer offlinePlayer, @Nullable ItemStack itemStack, @NotNull Location location) {
        Shop shop = getApi().getShopManager().getShopIncludeAttached(location);
        if(shop == null)
            return true;
        return shop.getOwner().equals(offlinePlayer.getUniqueId());
    }

    @Override
    public boolean canUseItem(@NotNull OfflinePlayer offlinePlayer, @NotNull ItemStack itemStack, @NotNull Location location) {
        Shop shop = getApi().getShopManager().getShopIncludeAttached(location);
        if(shop == null)
            return true;
        return shop.getOwner().equals(offlinePlayer.getUniqueId());
    }
}
