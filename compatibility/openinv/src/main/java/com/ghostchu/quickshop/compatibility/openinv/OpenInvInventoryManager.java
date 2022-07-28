package com.ghostchu.quickshop.compatibility.openinv;

import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperManager;
import com.lishid.openinv.IOpenInv;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class OpenInvInventoryManager implements InventoryWrapperManager {
    private final IOpenInv openInv;
    private final Main plugin;

    public OpenInvInventoryManager(IOpenInv openinv, Main plugin) {
        this.openInv = openinv;
        this.plugin = plugin;
    }


    /**
     * Create a symbol link for storage.
     *
     * @param wrapper Storage wrapper
     * @return Symbol Link that used for locate the Inventory
     * @throws IllegalArgumentException If cannot create symbol link for target Inventory.
     */
    @Override
    public @NotNull String mklink(@NotNull InventoryWrapper wrapper) throws IllegalArgumentException {
        if (wrapper instanceof EnderChestWrapper wrap) {
            return wrap.getUuid().toString();
        }
        throw new IllegalArgumentException("Cannot create symbol link for target Inventory: " + wrapper.getClass().getName());
    }

    /**
     * Locate an Inventory with symbol link
     * NOTICE: This can be call multiple times, and maybe multiple InventoryWrapper will exist in same time.
     * You must be sure all wrapper can be process any request in any time.
     *
     * @param symbolLink symbol link that created by InventoryWrapperManager#mklink
     * @return Symbol link
     * @throws IllegalArgumentException If symbol link invalid
     */
    @Override
    public @NotNull InventoryWrapper locate(@NotNull String symbolLink) throws IllegalArgumentException {
        return new EnderChestWrapper(UUID.fromString(symbolLink), this.openInv, plugin);
    }
}
