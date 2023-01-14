package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Calling when new shop creating
 */
public class ShopCreateSuccessEvent extends AbstractQSEvent {

    @NotNull
    private final UUID creator;

    @Nullable
    private final Player player;

    @NotNull
    private final Shop shop;


    /**
     * Call when have a new shop was created.
     *
     * @param shop    Target shop
     * @param creator The player creating the shop, the player might offline/not exist if creating by a plugin.
     */
    public ShopCreateSuccessEvent(@NotNull Shop shop, @NotNull UUID creator) {
        this.shop = shop;
        this.creator = creator;
        this.player = Bukkit.getPlayer(creator);
    }


    /**
     * Gets the creator of this shop
     *
     * @return The creator, may be a online/offline/virtual player
     */
    public @NotNull UUID getCreator() {
        return this.creator;
    }

    /**
     * Gets the creator that is the shop
     *
     * @return Player or null when player not exists or offline
     */
    public @Nullable Player getPlayer() {
        return this.player;
    }

    /**
     * Gets the shop created
     *
     * @return The shop that created
     */
    public @NotNull Shop getShop() {
        return this.shop;
    }
}
