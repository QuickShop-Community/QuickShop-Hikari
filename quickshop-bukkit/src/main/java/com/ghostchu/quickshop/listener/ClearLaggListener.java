///*
// * This file is a part of project QuickShop, the name is ClearLaggListener.java
// *  Copyright (C) PotatoCraft Studio and contributors
// *
// *  This program is free software: you can redistribute it and/or modify it
// *  under the terms of the GNU General Public License as published by the
// *  Free Software Foundation, either version 3 of the License, or
// *  (at your option) any later version.
// *
// *  This program is distributed in the hope that it will be useful, but WITHOUT
// *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
// *  for more details.
// *
// *  You should have received a copy of the GNU General Public License
// *  along with this program. If not, see <http://www.gnu.org/licenses/>.
// *
// */
//
//package com.ghostchu.quickshop.listener;
//
//import com.ghostchu.simplereloadlib.ReloadResult;
//import com.ghostchu.simplereloadlib.ReloadStatus;
//import org.bukkit.entity.Entity;
//import org.bukkit.entity.Item;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.EventPriority;
//import com.ghostchu.quickshop.QuickShop;
//import com.ghostchu.quickshop.api.shop.AbstractDisplayItem;
//import com.ghostchu.quickshop.util.Util;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ClearLaggListener extends AbstractQSListener {
//
//    public ClearLaggListener(QuickShop plugin) {
//        super(plugin);
//    }
//
//    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
//    public void plugin(me.minebuilders.clearlag.events.EntityRemoveEvent clearlaggEvent) {
//        final List<Entity> entities = clearlaggEvent.getEntityList();
//        final List<Entity> pendingExclude = new ArrayList<>();
//
//        for (final Entity entity : entities) {
//            if (!(entity instanceof Item)
//                    || !AbstractDisplayItem.checkIsGuardItemStack(((Item) entity).getItemStack())) {
//                continue;
//            }
//
//            pendingExclude.add(entity);
//        }
//
//        for (final Entity entity : pendingExclude) {
//            clearlaggEvent.removeEntity(entity);
//        }
//        Util.debugLog("Prevent " + pendingExclude.size() + " displays removal by ClearLagg.");
//    }
//
//    /**
//     * Callback for reloading
//     *
//     * @return Reloading success
//     */
//    @Override
//    public ReloadResult reloadModule() {
//        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
//    }
//}
