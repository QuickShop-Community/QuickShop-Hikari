package com.ghostchu.quickshop.api.shop;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Contains shop's moderators infomations, owner, staffs etc.
 * You must save the ContainerShop after modify this
 *
 * @deprecated Replaced with {@link ShopPermissionManager}
 */
@Deprecated
public interface ShopModerator {
    Gson GSON = new Gson();

    /**
     * Deserialize a ShopModerator using Gson
     *
     * @param serilized ShopModerator object serilized Json String
     * @return Json String
     * @throws JsonSyntaxException incorrect json string
     */
    @NotNull
    static ShopModerator deserialize(@NotNull String serilized) throws JsonSyntaxException {
        // Use Gson deserialize data
        return GSON.fromJson(serilized, ShopModerator.class);
    }

    /**
     * Serialize a ShopModerator using Gson
     *
     * @param shopModerator ShopModerator object
     * @return Json String
     */
    @NotNull
    static String serialize(@NotNull ShopModerator shopModerator) {
        return GSON.toJson(shopModerator); // Use Gson serialize this class
    }

    /**
     * Add moderators staff to staff list
     *
     * @param player New staff
     * @return Success
     */
    boolean addStaff(@NotNull UUID player);

    /**
     * Remove all staffs
     */
    void clearStaffs();

    @Override
    @NotNull String toString();

    /**
     * Remove moderators staff from staff list
     *
     * @param player Staff
     * @return Success
     */
    boolean delStaff(@NotNull UUID player);

    /**
     * Get a player is or not moderators
     *
     * @param player Player
     * @return yes or no, return true when it is staff or owner
     */
    boolean isModerator(@NotNull UUID player);

    /**
     * Get a player is or not moderators owner
     *
     * @param player Player
     * @return yes or no
     */
    boolean isOwner(@NotNull UUID player);

    /**
     * Get a player is or not moderators a staff
     *
     * @param player Player
     * @return yes or no
     */
    boolean isStaff(@NotNull UUID player);

    /**
     * Get moderators owner (Shop Owner).
     *
     * @return Owner's UUID
     */
    @NotNull UUID getOwner();

    /**
     * Set moderators owner (Shop Owner)
     *
     * @param player Owner's UUID
     */
    void setOwner(@NotNull UUID player);

    /**
     * Get staffs list
     *
     * @return Staffs
     */
    @NotNull List<UUID> getStaffs();

    /**
     * Set moderators staffs
     *
     * @param players staffs list
     */
    void setStaffs(@NotNull List<UUID> players);

}
