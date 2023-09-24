package com.ghostchu.quickshop.addon.shopitemonly;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class Main extends JavaPlugin implements Listener {
    static Main instance;
    private QuickShop plugin;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Plugin) this);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        plugin = QuickShop.getInstance();
        Bukkit.getPluginManager().registerEvents(this, this);
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void invClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        //noinspection ConstantConditions
        if (inventory == null) {
            return;
        }
        Location invLocation = inventory.getLocation();
        if (invLocation == null) {
            return;
        }
        Shop shop = plugin.getShopManager().getShopIncludeAttached(invLocation);
        if (shop == null) {
            return;
        }
        List<ItemStack> pendingForRemoval = new ArrayList<>();
        for (ItemStack stack : inventory.getStorageContents()) {
            if (stack == null) {
                continue;
            }
            if (stack.getType() == Material.AIR) {
                continue;
            }
            if (shop.matches(stack)) {
                continue;
            }
            pendingForRemoval.add(stack);
        }
        if (pendingForRemoval.isEmpty()) {
            return;
        }
        for (ItemStack item : pendingForRemoval) {
            inventory.remove(item);
            invLocation.getWorld().dropItemNaturally(invLocation.add(0, 1, 0), item);
        }
        plugin.text().of(event.getPlayer(), "addon.shopitemonly.message", pendingForRemoval.size()).send();
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void invMove(InventoryMoveItemEvent event) {
        //noinspection ConstantConditions
        if (event.getDestination() == null) { //Stupid CMIGUI plugin
            return;
        }
        if (event.getDestination().getLocation() == null) {
            return;
        }
        Shop shop = plugin.getShopManager().getShopIncludeAttached(event.getDestination().getLocation());
        if (shop == null) {
            return;
        }
        if (event.getItem().getType() == Material.AIR) {
            return;
        }
        if (shop.matches(event.getItem())) {
            return;
        }
        event.setCancelled(true);
    }
}
