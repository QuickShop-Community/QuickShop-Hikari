package com.ghostchu.quickshop.compatibility.towny;

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.logger.Log;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TownyShopUtil {

  private TownyShopUtil() {

  }

  @Nullable
  public static Nation getShopNation(@NotNull Shop shop) {

    ConfigurationSection section = shop.getExtra(JavaPlugin.getPlugin(Main.class));
    String uuid = section.getString("towny-nation-uuid");
    if(uuid == null) {
      return null;
    }
    Nation nation = TownyAPI.getInstance().getNation(UUID.fromString(uuid));
    Log.debug("Nation finding for shop " + shop.getLocation() + " => nation uuid: " + uuid + " nation: " + nation);
    return nation;
  }

  @NotNull
  public static UUID getShopOriginalOwner(@NotNull Shop shop) {

    ConfigurationSection section = shop.getExtra(JavaPlugin.getPlugin(Main.class));
    if(section.isSet("towny-original-owner")) {
      return UUID.fromString(section.getString("towny-original-owner", CommonUtil.getNilUniqueId().toString()));
    } else {
      UUID uuid = shop.getOwner().getUniqueIdIfRealPlayer().orElse(null);
      if(uuid == null) {
        return CommonUtil.getNilUniqueId();
      }
      return uuid;
    }
  }

  @Nullable
  public static Town getShopTown(@NotNull Shop shop) {

    ConfigurationSection section = shop.getExtra(JavaPlugin.getPlugin(Main.class));
    String uuid = section.getString("towny-town-uuid");
    if(uuid == null) {
      return null;
    }
    Town town = TownyAPI.getInstance().getTown(UUID.fromString(uuid));
    Log.debug("Town finding for shop " + shop.getLocation() + " => town uuid: " + uuid + " town: " + town);
    return town;
  }

  public static void setShopNation(@NotNull Shop shop, @Nullable Nation nation) {

    ConfigurationSection section = shop.getExtra(JavaPlugin.getPlugin(Main.class));
    if(nation == null) {
      section.set("towny-nation-uuid", null);
    } else {
      section.set("towny-nation-uuid", nation.getUUID().toString());
    }
    shop.setExtra(JavaPlugin.getPlugin(Main.class), section);
  }

  public static void setShopOriginalOwner(@NotNull Shop shop, @Nullable UUID owner) {

    ConfigurationSection section = shop.getExtra(JavaPlugin.getPlugin(Main.class));
    if(owner == null) {
      section.set("towny-original-owner", null);
    } else {
      section.set("towny-original-owner", owner.toString());
    }
    shop.setExtra(JavaPlugin.getPlugin(Main.class), section);
  }

  public static void setShopTown(@NotNull Shop shop, @Nullable Town town) {

    ConfigurationSection section = shop.getExtra(JavaPlugin.getPlugin(Main.class));
    if(town == null) {
      section.set("towny-town-uuid", null);
    } else {
      section.set("towny-town-uuid", town.getUUID().toString());
    }
    shop.setExtra(JavaPlugin.getPlugin(Main.class), section);
  }
}

