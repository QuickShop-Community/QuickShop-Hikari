package com.ghostchu.quickshop.compatibility.advancedchests;

import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import us.lynuxcraft.deadsilenceiv.advancedchests.AdvancedChestsAPI;
import us.lynuxcraft.deadsilenceiv.advancedchests.chest.AdvancedChest;

import java.util.UUID;

public class AdvancedChestsInventoryManager implements InventoryWrapperManager {
    private final Main plugin;

    public AdvancedChestsInventoryManager(Main plugin) {
        this.plugin = plugin;
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
        String[] spilt = symbolLink.split(";");
        if (spilt.length < 5) {
            throw new IllegalArgumentException("Invalid symbol link: " + symbolLink);
        }
        UUID aChestUniqueId = UUID.fromString(spilt[0]);
        String worldName = spilt[1];
        int x = Integer.parseInt(spilt[2]);
        int y = Integer.parseInt(spilt[3]);
        int z = Integer.parseInt(spilt[4]);
        Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
        AdvancedChest<?, ?> achest = AdvancedChestsAPI.getChestManager().getAdvancedChest(location);
        if (achest == null)
            throw new IllegalArgumentException("Cannot found AdvancedChest at " + location.toString() + " with UUID " + aChestUniqueId.toString() + ".");
        return new AdvancedChestsWrapper(achest, plugin);
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
        if (wrapper instanceof AdvancedChestsWrapper advancedChestsWrapper) {
            AdvancedChest<?, ?> achest = advancedChestsWrapper.getAdvancedChest();
            Location achestLoc = achest.getLocation();
            return achest.getUniqueId().toString() + ";" + achestLoc.getWorld().getName() + ";" + achestLoc.getBlockX() + ";" + achestLoc.getBlockY() + ";" + achestLoc.getBlockZ();
        }
        throw new IllegalArgumentException("Cannot create symbol link for target Inventory: " + wrapper.getClass().getName());
    }
}
