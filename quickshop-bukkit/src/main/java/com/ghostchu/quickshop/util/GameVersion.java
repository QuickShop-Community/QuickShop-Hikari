/*
 *  This file is a part of project QuickShop, the name is GameVersion.java
 *  Copyright (C) Ghost_chu and contributors
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

package com.ghostchu.quickshop.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Utilities to help QuickShop quickly check server supported features
 *
 * @author Ghost_chu and sandtechnology
 */
@AllArgsConstructor
@Getter
public enum GameVersion {
    v1_5_R1(false, true, false, false, false, false),
    v1_5_R2(false, true, false, false, false, false),
    v1_5_R3(false, true, false, false, false, false),
    v1_6_R1(false, true, false, false, false, false),
    v1_6_R2(false, true, false, false, false, false),
    v1_6_R3(false, true, false, false, false, false),
    v1_7_R1(false, true, false, false, false, false),
    v1_7_R2(false, true, false, false, false, false),
    v1_7_R3(false, true, false, false, false, false),
    v1_7_R4(false, true, false, false, false, false),
    v1_8_R1(false, true, false, false, false, false),
    v1_8_R2(false, true, false, false, false, false),
    v1_8_R3(false, true, false, false, false, false),
    v1_9_R1(false, true, false, false, false, false),
    v1_9_R2(false, true, false, false, false, false),
    v1_10_R1(false, true, false, false, false, false),
    v1_11_R1(false, true, false, false, false, false),
    v1_12_R1(false, true, false, false, false, false),
    v1_12_R2(false, true, false, false, false, false),
    v1_13_R1(false, true, true, false, false, false),
    v1_13_R2(false, true, true, false, false, false),
    v1_14_R1(false, true, true, false, false, false),
    v1_14_R2(false, true, true, false, false, false),
    v1_15_R1(false, true, true, false, false, false),
    v1_15_R2(false, true, true, false, false, false),
    v1_16_R1(false, true, true, false, false, false),
    v1_16_R2(false, true, true, false, false, false),
    v1_16_R3(false, true, true, false, false, false),
    v1_16_R4(false, true, true, false, false, false),
    v1_17_R1(false, true, true, true, true, true),
    v1_18_R1(true, true, true, true, true, true),
    v1_18_R2(true,false,true,true,true,true),
    UNKNOWN(true, false, true, true, true, true);
    /**
     * CoreSupports - Check does QuickShop most features supports this server version
     */
    private final boolean coreSupports;

    /**
     * EndOfLife - It will disable update checker or some else checks
     */
    private final boolean endOfLife;

    /**
     * VirtualDisplaySupports - Check does QuickShop VirtualDisplayItem feature this server version
     */
    private final boolean virtualDisplaySupports;

    /**
     * NewNmsName - Use 1.17+ nms class name mapping
     */
    private final boolean newNmsName;

    /**
     * Sign Glowing Support
     */
    private final boolean signGlowingSupport;

    /**
     * Sign Glowing Support
     */
    private final boolean signTextDyeSupport;


    /**
     * Matches the version that QuickShop supports or not
     *
     * @param nmsVersion The Minecraft NMS version
     * @return The object contains supports details for GameVersion
     */
    @NotNull
    public static GameVersion get(@NotNull String nmsVersion) {
        for (GameVersion version : GameVersion.values()) {
            if (version.name().equals(nmsVersion)) {
                return version;
            }
        }
        return UNKNOWN;
    }

}
