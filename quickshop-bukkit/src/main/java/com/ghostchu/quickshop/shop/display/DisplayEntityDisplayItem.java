package com.ghostchu.quickshop.shop.display;

import com.ghostchu.quickshop.api.event.ShopDisplayItemDespawnEvent;
import com.ghostchu.quickshop.api.event.ShopDisplayItemSafeGuardEvent;
import com.ghostchu.quickshop.api.event.ShopDisplayItemSpawnEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.display.DisplayType;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DisplayEntityDisplayItem extends AbstractDisplayItem {
    private static final Vector axis = new Vector(0, 1, 0);
    ItemDisplay displayEntity;

    /**
     * ZZ Creates a new display item.
     *
     * @param shop The shop (See Shop)
     */
    public DisplayEntityDisplayItem(@NotNull Shop shop) {
        super(shop);
    }

    @Override
    public boolean checkDisplayIsMoved() {
        return false; // Display won't move unless somebody teleport it....
    }

    @Override
    public boolean checkDisplayNeedRegen() {
        Util.ensureThread(false);
        if (this.displayEntity == null) {
            return false;
        }
        return !this.displayEntity.isValid();
    }

    @Override
    public boolean checkIsShopEntity(@NotNull Entity entity) {
        Util.ensureThread(false);
        if (!(entity instanceof ItemDisplay display)) {
            return false;
        }
        return AbstractDisplayItem.checkIsGuardItemStack(display.getItemStack());
    }

    @Override
    public void fixDisplayMoved() {
        Util.ensureThread(false);
        Location location = this.getDisplayLocation();
        if (this.displayEntity != null && location != null) {
            PaperLib.teleportAsync(this.displayEntity, location);
            return;
        }
        fixDisplayMovedOld();
    }

    public void fixDisplayMovedOld() {
        Util.ensureThread(false);
        for (Entity entity : Objects.requireNonNull(this.shop.getLocation().getWorld())
                .getEntities()) {
            if (!(entity instanceof Display display)) {
                continue;
            }
            if (display.getUniqueId().equals(Objects.requireNonNull(this.displayEntity).getUniqueId())) {
                Log.debug("Fixing moved Item displayItem " + display.getUniqueId() + " at " + display.getLocation());
                PaperLib.teleportAsync(entity, Objects.requireNonNull(getDisplayLocation()), PlayerTeleportEvent.TeleportCause.UNKNOWN);
                return;
            }
        }
    }

    @Override
    public void fixDisplayNeedRegen() {
        Util.ensureThread(false);
        respawn();
    }

    @Override
    public @Nullable Entity getDisplay() {
        return this.displayEntity;
    }

    @Override
    public boolean isSpawned() {
        if (this.displayEntity == null) {
            return false;
        }
        return this.displayEntity.isValid();
    }

    @Override
    public boolean isApplicableForPlayer(Player player) {
        return true;
    }

    @Override
    public void remove(boolean dontTouchWorld) {
        Util.ensureThread(false);
        if (this.displayEntity == null) {
            Log.debug("Ignore the Item removing because the Item is already gone or it's a left shop.");
            return;
        }
        if (dontTouchWorld) {
            return;
        }
        this.displayEntity.remove();
        this.displayEntity = null;
        this.guardedIstack = null;
        new ShopDisplayItemDespawnEvent(shop, originalItemStack, DisplayType.ENTITY_DISPLAY).callEvent();
    }

    @Override
    public boolean removeDupe() {
        Util.ensureThread(false);
        if (this.displayEntity == null) {
            Log.debug("Warning: Trying to removeDupe for a null display shop.");
            return false;
        }

        boolean removed = false;

        List<Entity> elist = new ArrayList<>(displayEntity.getNearbyEntities(1.5, 1.5, 1.5));
        for (Entity entity : elist) {
            if (entity.getType() != EntityType.ITEM_DISPLAY) {
                continue;
            }
            ItemDisplay eItem = (ItemDisplay) entity;
            UUID displayUUID = this.displayEntity.getUniqueId();
            if (eItem.getItemStack() == null) {
                continue;
            }
            if(AbstractDisplayItem.checkIsGuardItemStack(eItem.getItemStack())) {
                if (!eItem.getUniqueId().equals(displayUUID)) {
                    Log.debug("Removing a duped ItemEntity " + eItem.getUniqueId() + " at " + eItem.getLocation());
                    entity.remove();
                    removed = true;
                }
            }
        }
        return removed;
    }

    @Override
    public void respawn() {
        Util.ensureThread(false);
        remove(false);
        spawn();
    }

    @Override
    public void safeGuard(@NotNull Entity entity) {
        Util.ensureThread(false);
        if (!(entity instanceof ItemDisplay display)) {
            Log.debug(
                    "Failed to safeGuard " + entity.getLocation() + ", cause target not a Item");
            return;
        }
        // Set item protect in the armorstand's hand

        if (PLUGIN.getConfig().getBoolean("shop.display-item-use-name")) {
            PLUGIN.getPlatform().setDisplayName(display, Util.getItemStackName(this.originalItemStack));
            display.setCustomNameVisible(true);
        } else {
            display.setCustomNameVisible(false);
        }

        display.setSilent(true);
        display.setInvulnerable(true);
        display.setPortalCooldown(Integer.MAX_VALUE);
        display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GROUND);
        display.setBillboard(Display.Billboard.VERTICAL);
    }

    @Override
    public void spawn() {
        Util.ensureThread(false);
        if (!shop.isLoaded()) {
            return;
        }
        if (shop.getLocation().getWorld() == null) {
            Log.debug("Canceled the itemDisplay spawning because the location in the world is null.");
            return;
        }

        if (originalItemStack == null) {
            Log.debug("Canceled the itemDisplay spawning because the ItemStack is null.");
            return;
        }
        if (displayEntity != null && displayEntity.isValid()) {
            Log.debug("Warning: Spawning the itemDisplay for DisplayItem when there is already an existing Dropped Item, May cause a duplicated Dropped Item!");
            MsgUtil.debugStackTrace(Thread.currentThread().getStackTrace());
        }
        if (!Util.isDisplayAllowBlock(
                Objects.requireNonNull(getDisplayLocation()).getBlock().getType())) {
            Log.debug("Can't spawn the itemDisplay because there is not an AIR block above the shopblock.");
            return;
        }


        if (new ShopDisplayItemSpawnEvent(shop, originalItemStack, DisplayType.REALITEM).callCancellableEvent()) {
            Log.debug("Canceled the itemDisplay spawning because a plugin setCancelled the spawning event, usually this is a QuickShop Add on");
            return;
        }
        this.guardedIstack = AbstractDisplayItem.createGuardItemStack(this.originalItemStack, this.shop);
        this.displayEntity = (ItemDisplay) this.shop.getLocation().getWorld().spawnEntity(getDisplayLocation(), EntityType.ITEM_DISPLAY, false);
        this.displayEntity.setItemStack(this.guardedIstack);
        safeGuard(displayEntity);
        new ShopDisplayItemSafeGuardEvent(shop, this.displayEntity).callEvent();
    }

    /**
     * Gets either the item spawn location of this item's chest.
     * Used for checking for duplicates.
     *
     * @return The display location of the item.
     */
    public @Nullable Location getDoubleShopDisplayLocations() {
        Util.ensureThread(false);
        return shop.getLocation().clone().add(0.5, 1.1, 0.5);
    }

    @Override
    public @Nullable Location getDisplayLocation() {
        return this.shop.getLocation().clone().add(0.5, 1.1, 0.5);
    }
}
