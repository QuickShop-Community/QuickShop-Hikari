/*
 * This file is a part of project QuickShop, the name is ShopChestListener.java
 *  Copyright (C) PotatoCraft Studio and contributors
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

package org.maxgamer.quickshop.listener;
//
//import lombok.AllArgsConstructor;
//import org.bukkit.entity.Player;
//import org.bukkit.event.Event;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.Listener;
//import org.bukkit.event.inventory.InventoryClickEvent;
//import org.bukkit.event.inventory.InventoryDragEvent;
//import org.bukkit.event.inventory.InventoryMoveItemEvent;
//import org.bukkit.event.inventory.InventoryType;
//import org.bukkit.inventory.Inventory;
//import org.jetbrains.annotations.Nullable;
//import org.maxgamer.quickshop.QuickShop;
//import org.maxgamer.quickshop.api.shop.Shop;
//
//@AllArgsConstructor
//public class ShopChestListener implements Listener {
//    @EventHandler(ignoreCancelled = true)
//    public void onClick(InventoryClickEvent e){
//        Inventory inventory = e.getClickedInventory();
//        if(inventory == null){
//            return;
//        }
//        if(!(e.getWhoClicked() instanceof Player)){
//            return;
//        }
//        if(e.getCurrentItem() == null){
//            return;
//        }
//        if (e.getCursor() == null) {
//            return;
//        }
//        if (e.getSlotType() == InventoryType.SlotType.QUICKBAR) {
//            return;
//        }
//        Shop shop = getShop(inventory);
//        if (shop == null){
//            return;
//        }
////        if(!shop.matches(e.getCursor())){
////            e.setCancelled(true);
////            e.setResult(Event.Result.DENY);
////            return;
////        }
////        if(!shop.matches(e.getCurrentItem())){
////            e.setCancelled(true);
////            e.setResult(Event.Result.DENY);
////            return;
////        }
////        if(!shop.matches(e.getCursor())){
////            e.setCancelled(true);
////            e.setResult(Event.Result.DENY);
////            return;
////        }
//
//    }
//    @EventHandler(ignoreCancelled = true)
//    public void onDrag(InventoryMoveItemEvent e){
//        if(getShop(e.getDestination()) != null){
//            e.setCancelled(true);
//        }
//    }
//    @EventHandler(ignoreCancelled = true)
//    public void onDrag(InventoryDragEvent e){
//        if(getShop(e.getInventory()) != null){
//            e.setCancelled(true);
//            e.setResult(Event.Result.DENY);
//        }
//    }
//    @Nullable
//    private Shop getShop(@Nullable Inventory inventory){
//        if(inventory == null){
//            return null;
//        }
//        if(inventory.getLocation() == null){
//            return null;
//        }
//        return QuickShop.instance.getShopManager().getShopIncludeAttached(inventory.getLocation(),true);
//    }
//}
