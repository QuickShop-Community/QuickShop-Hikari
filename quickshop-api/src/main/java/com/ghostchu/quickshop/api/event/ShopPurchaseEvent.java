package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Calling when purchaser purchased a shop
 */
public class ShopPurchaseEvent extends AbstractQSEvent implements QSCancellable {

    @NotNull
    private final Shop shop;

    @NotNull
    private final UUID purchaser;

    @Nullable
    @Deprecated
    private final Player player;

    @NotNull
    private final InventoryWrapper purchaserInventory;

    private final int amount;
    private double total;

    private boolean cancelled;
    private @Nullable Component cancelReason;

    /**
     * Builds a new shop purchase event
     * Will called when purchase starting
     * For recording purchase, please listen to ShopSuccessPurchaseEvent.
     *
     * @param shop               The shop bought from
     * @param purchaser          The player buying, may offline if purchase by plugin
     * @param purchaserInventory The purchaseing target inventory, *MAY NOT A PLAYER INVENTORY IF PLUGIN PURCHASE THIS*
     * @param amount             The amount they're buying
     * @param total              The total balance in this purchase
     */
    public ShopPurchaseEvent(@NotNull Shop shop, @NotNull UUID purchaser, @NotNull InventoryWrapper purchaserInventory, int amount, double total) {
        this.shop = shop;
        this.purchaser = purchaser;
        this.purchaserInventory = purchaserInventory;
        this.amount = amount * shop.getItem().getAmount();
        this.total = total;
        this.player = Bukkit.getPlayer(purchaser);
    }

    /**
     * Gets the item stack amounts
     *
     * @return Item stack amounts
     */
    public int getAmount() {
        return this.amount;
    }

    @Override
    public @Nullable Component getCancelReason() {
        return this.cancelReason;
    }

    @Override
    public void setCancelled(boolean cancel, @Nullable Component reason) {
        this.cancelled = cancel;
        this.cancelReason = reason;
    }

    /**
     * Gets the purchaser
     *
     * @return Player or null if purchaser is offline/virtual player.
     */
    public @Nullable Player getPlayer() {
        return this.player;
    }

    /**
     * Gets the purchaser, that maybe is a online/offline/virtual player.
     *
     * @return The purchaser uuid
     */
    public @NotNull UUID getPurchaser() {
        return this.purchaser;
    }

    /**
     * Gets the inventory of purchaser (the item will put to)
     *
     * @return The inventory
     */
    public @NotNull InventoryWrapper getPurchaserInventory() {
        return this.purchaserInventory;
    }

    /**
     * Gets the shop
     *
     * @return the shop
     */
    public @NotNull Shop getShop() {
        return this.shop;
    }

    /**
     * Gets the total money trans for
     *
     * @return Total money
     */
    public double getTotal() {
        return this.total;
    }

    /**
     * Sets new total money trans for
     *
     * @param total Total money
     */
    public void setTotal(double total) {
        this.total = total;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }
}
