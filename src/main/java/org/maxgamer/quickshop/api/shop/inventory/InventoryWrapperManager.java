package org.maxgamer.quickshop.api.shop.inventory;

import org.jetbrains.annotations.NotNull;

public interface InventoryWrapperManager {
    /**
     * Create a symbol link for storage.
     * @param wrapper Storage wrapper
     * @exception IllegalArgumentException If cannot create symbol link for target Inventory.
     * @return Symbol Link that used for locate the Inventory
     */
    @NotNull
    String mklink(@NotNull InventoryWrapper wrapper) throws IllegalArgumentException;

    /**
     * Locate an Inventory with symbol link
     * NOTICE: This can be call multiple times, and maybe multiple InventoryWrapper will exist in same time.
     * You must be sure all wrapper can be process any request in any time.
     * @param symbolLink symbol link that created by InventoryWrapperManager#mklink
     * @return Symbol link
     * @throws IllegalArgumentException If symbol link invalid
     */
    @NotNull
    InventoryWrapper locate(@NotNull String symbolLink) throws IllegalArgumentException;



}
