package com.ghostchu.quickshop.api;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Utilities to help QuickShop quickly check server supported features
 *
 * @author Ghost_chu and sandtechnology
 */
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
    v1_13_R1(false, true, false, false, false, false),
    v1_13_R2(false, true, false, false, false, false),
    v1_14_R1(false, true, false, false, false, false),
    v1_14_R2(false, true, false, false, false, false),
    v1_15_R1(false, true, false, false, false, false),
    v1_15_R2(false, true, false, false, false, false),
    v1_16_R1(false, true, false, false, false, false),
    v1_16_R2(false, true, false, false, false, false),
    v1_16_R3(false, true, false, false, false, false),
    v1_16_R4(false, true, false, false, false, false),
    v1_17_R1(false, true, false, true, true, true),
    v1_18_R1(false, true, false, true, true, true),
    v1_18_R2(true, false, true, true, true, true),
    v1_19_R1(true, true, true, true, true, true),
    v1_19_R2(true, false, true, true, true, true),
    v1_19_R3(true, false, true, true, true, true),
    v1_20_R1(true, false, true, true, true, true),
    v1_20_R2(true, false, true, true, true, true),
    v1_20_R3(true, false, true, true, true, true),
    UNKNOWN(true, false, false, true, true, true);
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

    GameVersion(boolean coreSupports, boolean endOfLife, boolean virtualDisplaySupports, boolean newNmsName, boolean signGlowingSupport, boolean signTextDyeSupport) {
        this.coreSupports = coreSupports;
        this.endOfLife = endOfLife;
        this.virtualDisplaySupports = virtualDisplaySupports;
        this.newNmsName = newNmsName;
        this.signGlowingSupport = signGlowingSupport;
        this.signTextDyeSupport = signTextDyeSupport;
    }


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
