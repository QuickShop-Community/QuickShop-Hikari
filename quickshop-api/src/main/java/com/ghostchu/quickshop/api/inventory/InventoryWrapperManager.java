package com.ghostchu.quickshop.api.inventory;

import org.jetbrains.annotations.NotNull;

/**
 * Manager for InventoryWrappers
 */
public interface InventoryWrapperManager {
    /**
     * Locate an Inventory with symbol link
     * NOTICE: This can be call multiple times, and maybe multiple InventoryWrapper will exist in same time.
     * You must be sure all wrapper can be process any request in any time.
     *
     * @param symbolLink symbol link that created by InventoryWrapperManager#mklink
     * @return Symbol link
     * @throws IllegalArgumentException If symbol link invalid
     */
    @NotNull
    InventoryWrapper locate(@NotNull String symbolLink) throws IllegalArgumentException;

    /**
     * Create a symbol link for storage.
     *
     * @param wrapper Storage wrapper
     * @return Symbol Link that used for locate the Inventory
     * @throws IllegalArgumentException If cannot create symbol link for target Inventory.
     */
    @NotNull
    String mklink(@NotNull InventoryWrapper wrapper) throws IllegalArgumentException;


}
