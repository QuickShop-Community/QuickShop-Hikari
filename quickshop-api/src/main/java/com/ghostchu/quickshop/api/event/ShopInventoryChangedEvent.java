package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperManager;
import org.jetbrains.annotations.NotNull;

/**
 * Calling when shop Inventory changed.
 */
public class ShopInventoryChangedEvent extends AbstractQSEvent {

    @NotNull
    private final InventoryWrapper wrapper;
    @NotNull
    private final InventoryWrapperManager manager;

    /**
     * ShopInventoryChangedEvent constructor.
     *
     * @param wrapper InventoryWrapper
     * @param manager InventoryWrapperManager
     */
    public ShopInventoryChangedEvent(@NotNull InventoryWrapper wrapper, @NotNull InventoryWrapperManager manager) {
        this.wrapper = wrapper;
        this.manager = manager;
    }

    /**
     * Gets new InventoryWrapperManager.
     *
     * @return InventoryWrapperManager
     */
    @NotNull
    public InventoryWrapperManager getInventoryManager() {
        return manager;
    }

    /**
     * Gets new InventoryWrapper.
     *
     * @return InventoryWrapper
     */
    @NotNull
    public InventoryWrapper getInventoryWrapper() {
        return wrapper;
    }
}
