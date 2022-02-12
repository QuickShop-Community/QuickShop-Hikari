/*
 * This file is a part of project QuickShop, the name is ShopModerator.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.api.shop;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.util.JsonUtil;

import java.util.List;
import java.util.UUID;

/**
 * Contains shop's moderators infomations, owner, staffs etc.
 * You must save the ContainerShop after modify this
 */
public interface ShopModerator {
    /**
     * Deserialize a ShopModerator using Gson
     *
     * @param serilized ShopModerator object serilized Json String
     * @return Json String
     * @throws JsonSyntaxException incorrect json string
     */
    static ShopModerator deserialize(@NotNull String serilized) throws JsonSyntaxException {
        // Use Gson deserialize data
        Gson gson = JsonUtil.getGson();
        return gson.fromJson(serilized, ShopModerator.class);
    }

    /**
     * Serialize a ShopModerator using Gson
     *
     * @param shopModerator ShopModerator object
     * @return Json String
     */
    static String serialize(@NotNull ShopModerator shopModerator) {
        Gson gson = JsonUtil.getGson();
        return gson.toJson(shopModerator); // Use Gson serialize this class
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

    @NotNull ShopModerator clone();

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
