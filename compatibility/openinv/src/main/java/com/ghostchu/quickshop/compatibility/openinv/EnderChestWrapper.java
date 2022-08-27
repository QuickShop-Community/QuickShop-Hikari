package com.ghostchu.quickshop.compatibility.openinv;

import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperIterator;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperManager;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperType;
import com.lishid.openinv.IOpenInv;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EnderChestWrapper implements InventoryWrapper {
    private final UUID uuid;
    private final Player player;
    private final Main plugin;

    public EnderChestWrapper(UUID uuid, IOpenInv iOpenInv, Main plugin) {
        this.plugin = plugin;
        this.uuid = uuid;
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        this.player = iOpenInv.loadPlayer(offlinePlayer);

    }

    public UUID getUuid() {
        return uuid;
    }

    /**
     * Return the iterator for this inventory
     * It's not thread-safe, please use that in main-thread
     *
     * @return the iterator for this inventory
     */
    @Override
    public @NotNull InventoryWrapperIterator iterator() {
        return InventoryWrapperIterator.ofBukkitInventory(player.getEnderChest());
    }

    /**
     * Get the location of the block or entity which corresponds to this inventory. May return null if this container
     * was custom created or is a virtual / subcontainer.
     *
     * @return location or null if not applicable.
     */
    @Override
    public @Nullable Location getLocation() {
        return null;
    }

    /**
     * Gets the Inventory Type
     *
     * @return The Inventory Type
     */
    @Override
    public @NotNull InventoryWrapperType getInventoryType() {
        return InventoryWrapperType.PLUGIN;
    }

    /**
     * Gets the Inventory Wrapper Manager
     *
     * @return Wrapper Manager
     */
    @Override
    public @NotNull InventoryWrapperManager getWrapperManager() {
        return plugin.getManager();
    }

    /**
     * Clear the inventory
     */
    @Override
    public void clear() {
        player.getEnderChest().clear();
    }

    /**
     * Gets the block or entity belonging to the open inventory
     *
     * @return The holder of the inventory; null if it has no holder.
     */
    @Override
    public @Nullable InventoryHolder getHolder() {
        return null;
    }

    /**
     * Set the contents of inventory
     *
     * @param itemStacks the contents you want to set
     */
    @Override
    public void setContents(ItemStack[] itemStacks) {
        player.getEnderChest().setContents(itemStacks);
    }
}
