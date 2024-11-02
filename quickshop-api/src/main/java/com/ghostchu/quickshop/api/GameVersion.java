package com.ghostchu.quickshop.api;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Utilities to help QuickShop quickly check server supported features
 *
 * @author Ghost_chu and sandtechnology
 */
@Getter
public enum GameVersion {
  v1_18_R1(new String[]{ "1.18", "1.18.1" }, false, true, false, true, false),
  v1_18_R2(new String[]{ "1.18.2" }, true, true, true, true, false),
  v1_19_R1(new String[]{ "1.19", "1.19.1" }, true, true, true, true, false),
  v1_19_R2(new String[]{ "1.19.2" }, true, true, true, true, false),
  v1_19_R3(new String[]{ "1.19.3", "1.19.4" }, true, true, true, true, false),
  v1_20_R1(new String[]{ "1.20", "1.20.1" }, true, true, true, true, false),
  v1_20_R2(new String[]{ "1.20.2", "1.20.3" }, true, false, true, true, false),
  v1_20_R3(new String[]{ "1.20.4", "1.20.5" }, true, false, true, true, true),
  v1_20_R4(new String[]{ "1.20.6" }, true, false, true, true, true),
  v1_21_R1(new String[]{ "1.21", "1.21.1" }, true, false, true, true, true),
  v1_21_R2(new String[]{ "1.21.2", "1.21.3", "1.21.4" }, true, false, true, true, true),
  UNKNOWN(new String[0], true, false, false, true, true);
  private final String[] mcVersion;
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

  private final boolean newPotionAPI;

  GameVersion(final String[] mcVersion, final boolean coreSupports, final boolean endOfLife, final boolean virtualDisplaySupports, final boolean newNmsName, final boolean newPotionAPI) {

    this.mcVersion = mcVersion;
    this.coreSupports = coreSupports;
    this.endOfLife = endOfLife;
    this.virtualDisplaySupports = virtualDisplaySupports;
    this.newNmsName = newNmsName;
    this.newPotionAPI = newPotionAPI;
  }


  /**
   * Matches the version that QuickShop supports or not
   *
   * @param nmsVersion The Minecraft NMS version
   *
   * @return The object contains supports details for GameVersion
   */
  @NotNull
  public static GameVersion get(@NotNull final String nmsVersion) {

    for(final GameVersion version : GameVersion.values()) {
      if(version.name().equals(nmsVersion)) {
        return version;
      }
      if(Arrays.asList(version.mcVersion).contains(nmsVersion)) {
        return version;
      }
    }
    return UNKNOWN;
  }

}
