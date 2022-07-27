package com.ghostchu.quickshop.api.inventory;

/**
 * The type of inventory wrapper.
 */
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
