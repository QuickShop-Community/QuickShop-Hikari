package com.ghostchu.quickshop.shop.display;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.display.DisplayType;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.shop.datatype.ShopProtectionFlag;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import com.ghostchu.simplereloadlib.Reloadable;
import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Netherfoam
 * A display item, that spawns a block above the chest and cannot be interacted
 * with.
 */
public abstract class AbstractDisplayItem implements Reloadable {

    protected static final QuickShop PLUGIN = QuickShop.getInstance();
    protected final ItemStack originalItemStack;
    protected final Shop shop;
    @Nullable
    protected ItemStack guardedIstack;
    private boolean pendingRemoval;

    protected AbstractDisplayItem(Shop shop) {
        this.shop = shop;
        this.originalItemStack = shop.getItem().clone();
        PLUGIN.getReloadManager().register(this);
        init();
    }

    /**
     * Get PLUGIN now is using which one DisplayType
     *
     * @return Using displayType.
     */
    @NotNull
    public static DisplayType getNowUsing() {
        return DisplayType.fromID(PLUGIN.getConfig().getInt("shop.display-type"));
    }

    /**
     * Check the itemStack is contains protect flag.
     *
     * @param itemStack Target ItemStack
     * @return Contains protect flag.
     */
    public static boolean checkIsGuardItemStack(@Nullable final ItemStack itemStack) {
        if (!PLUGIN.isDisplayEnabled()) {
            return false;
        }
        if (getNowUsing() == DisplayType.VIRTUALITEM) {
            return false;
        }
        Util.ensureThread(false);
        if (itemStack == null) {
            return false;
        }
        if (!itemStack.hasItemMeta()) {
            return false;
        }
        ItemMeta iMeta = itemStack.getItemMeta();
        if (!iMeta.hasLore()) {
            return false;
        }
        String defaultMark = ShopProtectionFlag.getDefaultMark();
        for (String lore : iMeta.getLore()) {
            try {
                if (!MsgUtil.isJson(lore)) {
                    continue;
                }
                ShopProtectionFlag shopProtectionFlag = JsonUtil.getGson().fromJson(lore, ShopProtectionFlag.class);
                if (shopProtectionFlag == null) {
                    continue;
                }
                if (defaultMark.equals(ShopProtectionFlag.getMark())) {
                    return true;
                }
                if (shopProtectionFlag.getShopLocation() != null) {
                    return true;
                }
                if (shopProtectionFlag.getItemStackString() != null) {
                    return true;
                }
            } catch (JsonSyntaxException e) {
                // Ignore
            }
        }

        return false;
    }

    protected void init() {
        if (PLUGIN.getConfig().getBoolean("shop.display-allow-stacks")) {
            //Prevent stack over the normal size
            originalItemStack.setAmount(Math.min(originalItemStack.getAmount(), originalItemStack.getMaxStackSize()));
        } else {
            this.originalItemStack.setAmount(1);
        }
    }

    /**
     * Check the itemStack is target shop's display
     *
     * @param itemStack Target ItemStack
     * @param shop      Target shop
     * @return Is target shop's display
     */
    public static boolean checkIsTargetShopDisplay(@NotNull final ItemStack itemStack, @NotNull Shop shop) {
        if (!PLUGIN.isDisplayEnabled()) {
            return false;
        }
        if (getNowUsing() == DisplayType.VIRTUALITEM) {
            return false;
        }
        if (!itemStack.hasItemMeta()) {
            return false;
        }
        ItemMeta iMeta = itemStack.getItemMeta();
        if (!iMeta.hasLore()) {
            return false;
        }
        String defaultMark = ShopProtectionFlag.getDefaultMark();
        String shopLocation = shop.getLocation().toString();
        for (String lore : iMeta.getLore()) {
            try {
                if (!MsgUtil.isJson(lore)) {
                    continue;
                }
                ShopProtectionFlag shopProtectionFlag = JsonUtil.getGson().fromJson(lore, ShopProtectionFlag.class);
                if (shopProtectionFlag == null) {
                    continue;
                }
                if (!ShopProtectionFlag.getMark().equals(defaultMark)) {
                    continue;
                }
                if (shopProtectionFlag.getShopLocation().equals(shopLocation)) {
                    return true;
                }
            } catch (JsonSyntaxException e) {
                // Ignore
            }
        }
        return false;
    }

    /**
     * Create a new itemStack with protect flag.
     *
     * @param itemStack Old itemStack
     * @param shop      The shop
     * @return New itemStack with protect flag.
     */
    @NotNull
    public static ItemStack createGuardItemStack(@NotNull ItemStack itemStack, @NotNull Shop shop) {
        itemStack = itemStack.clone();
        ItemMeta iMeta = itemStack.getItemMeta();
        if (iMeta == null) {
            iMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
        }
        if (iMeta == null) {
            Log.debug("ItemStack " + itemStack + " cannot getting or creating ItemMeta, failed to create guarded ItemStack.");
            return itemStack;
        }
        if (!PLUGIN.getConfig().getBoolean("shop.display-item-use-name")) {
            iMeta.setDisplayName(null);
        }
        ShopProtectionFlag shopProtectionFlag = createShopProtectionFlag(itemStack, shop);
        String protectFlag = JsonUtil.getGson().toJson(shopProtectionFlag);
        iMeta.setLore(Lists.newArrayList(protectFlag));
        itemStack.setItemMeta(iMeta);
        return itemStack;
    }

    /**
     * Create the shop protection flag for display item.
     *
     * @param itemStack The item stack
     * @param shop      The shop
     * @return ShopProtectionFlag obj
     */
    @NotNull
    public static ShopProtectionFlag createShopProtectionFlag(
            @NotNull ItemStack itemStack, @NotNull Shop shop) {
        return new ShopProtectionFlag(shop.getLocation().toString(), Util.serialize(itemStack));
    }

    /**
     * Check the display is or not moved.
     *
     * @return Moved
     */
    public abstract boolean checkDisplayIsMoved();

    /**
     * Check the display is or not need respawn
     *
     * @return Need
     */
    public abstract boolean checkDisplayNeedRegen();

    /**
     * Check target Entity is or not a QuickShop display Entity.
     *
     * @param entity Target entity
     * @return Is or not
     */
    public abstract boolean checkIsShopEntity(Entity entity);

    /**
     * Fix the display moved issue.
     */
    public abstract void fixDisplayMoved();

    /**
     * Fix display need respawn issue.
     */
    public abstract void fixDisplayNeedRegen();

    /**
     * Get the display entity
     *
     * @return Target entity
     */
    public abstract Entity getDisplay();

    /**
     * Gets the display location for an item. If it is a double shop and it is not the left shop,
     * it will average the locations of the two chests comprising it to be perfectly in the middle.
     * If it is the left shop, it will return null since the left shop does not spawn an item.
     * Otherwise, it will give you the middle of the single chest.
     *
     * @return The Location that the item *should* be displaying at.
     */
    public @Nullable Location getDisplayLocation() {
        Util.ensureThread(false);
        return this.shop.getLocation().clone().add(0.5, 1.2, 0.5);
    }

    /**
     * Gets the original ItemStack (without protection mark, should same with shop trading item.
     *
     * @return ItemStack
     */
    @NotNull
    public ItemStack getOriginalItemStack() {
        return originalItemStack;
    }

    /**
     * Gets this display item should be remove
     *
     * @return the status
     */
    public boolean isPendingRemoval() {
        return pendingRemoval;
    }

    /**
     * Check the display is or not already spawned
     *
     * @return Spawned
     */
    public abstract boolean isSpawned();

    /**
     * Sets this display item should be remove
     */
    public void pendingRemoval() {
        pendingRemoval = true;
    }

    @Override
    public ReloadResult reloadModule() {
        init();
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }

    /**
     * Remove the display entity.
     */
    public abstract void remove();

    /**
     * Remove this shop's display in the whole world.(Not whole server)
     *
     * @return Success
     */
    public abstract boolean removeDupe();

    /**
     * Respawn the displays, if it not exist, it will spawn new one.
     */
    public abstract void respawn();

    /**
     * Add the protect flags for entity or entity's hand item. Target entity will got protect by
     * QuickShop
     *
     * @param entity Target entity
     */
    public abstract void safeGuard(@NotNull Entity entity);

    /**
     * Spawn new Displays
     */
    public abstract void spawn();

    /**
     * Gets the shop that display holding
     *
     * @return The shop that display holding
     */
    @NotNull
    public Shop getShop() {
        return shop;
    }
}
