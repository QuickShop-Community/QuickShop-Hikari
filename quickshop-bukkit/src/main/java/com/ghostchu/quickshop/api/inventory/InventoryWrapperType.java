package com.ghostchu.quickshop.api.inventory;

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
