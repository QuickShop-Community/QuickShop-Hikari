package com.ghostchu.quickshop.compatibility.towny;

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.logger.Log;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TownyShopUtil {

  private TownyShopUtil() {

  }

  @Nullable
  public static Nation getShopNation(@NotNull final Shop shop) {

    final String uuid = shop.getExtra(new NamespacedKey(Main.getInstance(), "towny-nation-uuid"));
    if(uuid == null) {
      return null;
    }
    final Nation nation = TownyAPI.getInstance().getNation(UUID.fromString(uuid));
    Log.debug("Nation finding for shop " + shop.bukkitLocation() + " => nation uuid: " + uuid + " nation: " + nation);
    return nation;
  }

  @NotNull
  public static UUID getShopOriginalOwner(@NotNull final Shop shop) {

    final String owner = shop.getExtra(new NamespacedKey(Main.getInstance(), "towny-original-owner"));
    if(owner != null) {
      try {
        return UUID.fromString(owner);
      } catch (final IllegalArgumentException ignore) {
        Log.debug("Invalid UUID found in shop extra: " + owner);
        return CommonUtil.getNilUniqueId();
      }
    } else {
      final UUID uuid = shop.getOwner().getUniqueIdIfRealPlayer().orElse(null);
      if(uuid == null) {
        return CommonUtil.getNilUniqueId();
      }
      return uuid;
    }
  }

  @Nullable
  public static Town getShopTown(@NotNull final Shop shop) {

    final String uuid = shop.getExtra(new NamespacedKey(Main.getInstance(), "towny-town-uuid"));
    if(uuid == null) {
      return null;
    }
    final Town town = TownyAPI.getInstance().getTown(UUID.fromString(uuid));
    Log.debug("Town finding for shop " + shop.bukkitLocation() + " => town uuid: " + uuid + " town: " + town);
    return town;
  }

  public static void setShopNation(@NotNull final Shop shop, @Nullable final Nation nation) {

    if(nation == null) {
      shop.setExtra(new NamespacedKey(Main.getInstance(), "towny-nation-uuid"), null);
    } else {
      shop.setExtra(new NamespacedKey(Main.getInstance(), "towny-nation-uuid"), nation.getUUID().toString());
    }
  }

  public static void setShopOriginalOwner(@NotNull final Shop shop, @Nullable final UUID owner) {

    if(owner == null) {
      shop.setExtra(new NamespacedKey(Main.getInstance(), "towny-original-owner"), null);
    } else {
      shop.setExtra(new NamespacedKey(Main.getInstance(), "towny-original-owner"), owner.toString());
    }
  }

  public static void setShopTown(@NotNull final Shop shop, @Nullable final Town town) {

    if(town == null) {
      shop.setExtra(new NamespacedKey(Main.getInstance(), "towny-town-uuid"), null);
    } else {
      shop.setExtra(new NamespacedKey(Main.getInstance(), "towny-town-uuid"), town.getUUID().toString());
    }
  }
}

