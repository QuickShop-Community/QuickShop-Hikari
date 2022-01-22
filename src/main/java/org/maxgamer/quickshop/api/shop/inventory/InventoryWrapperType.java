package org.maxgamer.quickshop.api.shop.inventory;

public enum InventoryWrapperType {
    /**
     * The Inventory belongs to a real Block/Entity in the world
     */
    BUKKIT,
    /**
     * The Inventory belongs to a virtual Plugin Inventory
     */
    PLUGIN
}
