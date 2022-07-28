package com.ghostchu.quickshop.api.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Calling when player do some actions about shop
 */
public class ShopProtectionCheckEvent extends AbstractQSEvent {

    @NotNull
    private final Player player;

    @NotNull
    private final Location loc;

    @NotNull
    private final Event event; // Don't use getter, we have important notice need told dev in javadoc.

    @NotNull
    private final ProtectionCheckStatus status;

    /**
     * Will call when checking protection by other plugin,
     * This event was used to compatible with QuickShop protection checking
     * Which will fire a FAKE BlockBreakEvent
     *
     * @param location Target location will execute protect check.
     * @param status   The checking status
     * @param event    The event will call to check the permissions.
     * @param player   The player in was mentions in this event
     */
    public ShopProtectionCheckEvent(
            @NotNull Location location,
            @NotNull Player player,
            @NotNull ProtectionCheckStatus status,
            @NotNull Event event) {
        this.loc = location;
        this.player = player;
        this.status = status;
        this.event = event;
    }

    /**
     * Get the event will used for permission check. WARN: This might not only BlockBreakEvent, you
     * should check the event type before casting.
     *
     * @return The protection check event.
     */
    @NotNull
    public Event getEvent() {
        return event;
    }

    /**
     * Gets the player triggered this event
     *
     * @return player
     */
    public @NotNull Player getPlayer() {
        return this.player;
    }

    /**
     * Gets the location that player want do shop actions on
     *
     * @return location
     */
    public @NotNull Location getLocation() {
        return this.loc;
    }

    /**
     * Gets the status (start to checking or ended to checking)
     *
     * @return The status
     */
    public @NotNull ProtectionCheckStatus getStatus() {
        return this.status;
    }
}
