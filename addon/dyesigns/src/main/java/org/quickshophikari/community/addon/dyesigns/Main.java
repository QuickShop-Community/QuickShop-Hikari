package org.quickshophikari.community.addon.dyesigns;

/*
 * QuickShop-Hikari
 * Copyright (C) 2026 Daniel "creatorfromhell" Vidmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.general.ShopSignUpdateEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermissionGroup;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public final class Main extends JavaPlugin implements Listener {

  private NamespacedKey DYE_NAMESPACE_KEY;
  private NamespacedKey GLOW_NAMESPACE_KEY;
  private NamespacedKey CLEAR_NAMESPACE_KEY;

  static Main instance;
  private QuickShop plugin;

  @Override
  public void onLoad() {

    instance = this;
    DYE_NAMESPACE_KEY = new NamespacedKey(QuickShop.getInstance().getJavaPlugin(), "sign-dye");
    GLOW_NAMESPACE_KEY = new NamespacedKey(QuickShop.getInstance().getJavaPlugin(), "sign-glow");
    CLEAR_NAMESPACE_KEY = new NamespacedKey(QuickShop.getInstance().getJavaPlugin(), "sign-clear");
  }

  @Override
  public void onDisable() {

    HandlerList.unregisterAll((Plugin)this);
  }

  @Override
  public void onEnable() {

    saveDefaultConfig();
    plugin = QuickShop.getInstance();
    getLogger().info("Registering the per shop permissions...");
    plugin.getShopPermissionManager().registerPermission(BuiltInShopPermissionGroup.STAFF.getNamespacedNode(), this, "dye_sign");
    plugin.getShopPermissionManager().registerPermission(BuiltInShopPermissionGroup.STAFF.getNamespacedNode(), this, "glow_sign");
    plugin.getShopPermissionManager().registerPermission(BuiltInShopPermissionGroup.STAFF.getNamespacedNode(), this, "clear_sign");
    getLogger().info("Registering the listeners...");
    Bukkit.getPluginManager().registerEvents(this, this);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onSignChange(@NotNull final ShopSignUpdateEvent event) {

    if (event.getSign().getPersistentDataContainer().has(CLEAR_NAMESPACE_KEY, PersistentDataType.STRING)) {
      return;
    }

    final DyeColor signColor = dyeFromSign(event.getSign());
    if (signColor != null) {
      event.getSign().getSide(Side.FRONT).setColor(signColor);
    }

    event.getSign().getSide(Side.FRONT).setGlowingText(event.getSign().getPersistentDataContainer().getOrDefault(GLOW_NAMESPACE_KEY,
                                                                                                                 PersistentDataType.BOOLEAN,
                                                                                                                 event.getSign().getSide(Side.FRONT).isGlowingText()));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerDye(final PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {

      return;
    }

    final Player player = event.getPlayer();

    final ItemStack item = event.getItem();
    if (item == null) {
      return;
    }

    getShopSign(event.getClickedBlock(), (shop, sign)->{
      if (sign == null) {
        return;
      }

      final DyeColor dyeColor = getDyeColor(item.getType());
      if (dyeColor != null) {

        if (!player.hasPermission("quickshopaddon.dyesigns.dye")) {
          return;
        }

        if (!player.getUniqueId().equals(shop.getOwner().getUniqueId())
            && !shop.playerAuthorize(player.getUniqueId(), this, "sign-dye")
            && !player.hasPermission("quickshopaddon.dyesigns.dye.other")) {
          return;
        }

        sign.getSide(Side.FRONT).setColor(dyeColor);
        sign.getPersistentDataContainer().set(DYE_NAMESPACE_KEY, PersistentDataType.STRING, dyeColor.name());
        sign.getPersistentDataContainer().remove(CLEAR_NAMESPACE_KEY);
      } else if(item.getType() == Material.GLOW_INK_SAC) {

        if (!player.hasPermission("quickshopaddon.dyesigns.glow")) {
          return;
        }

        if (!player.getUniqueId().equals(shop.getOwner().getUniqueId())
            && !shop.playerAuthorize(player.getUniqueId(), this, "sign-glow")
            && !player.hasPermission("quickshopaddon.dyesigns.glow.other")) {
          return;
        }

        final boolean glow = !sign.getSide(Side.FRONT).isGlowingText();

        sign.getSide(Side.FRONT).setGlowingText(glow);
        sign.getPersistentDataContainer().set(GLOW_NAMESPACE_KEY, PersistentDataType.BOOLEAN, glow);
        sign.getPersistentDataContainer().remove(CLEAR_NAMESPACE_KEY);
      } else if(item.getType().name().contains("_SIGN")) {

        if (!player.hasPermission("quickshopaddon.dyesigns.clear")) {
          return;
        }

        if (!player.getUniqueId().equals(shop.getOwner().getUniqueId())
            && !shop.playerAuthorize(player.getUniqueId(), this, "sign-clear")
            && !player.hasPermission("quickshopaddon.dyesigns.clear.other")) {
          return;
        }

        sign.getSide(Side.FRONT).setGlowingText(false);
        sign.getPersistentDataContainer().set(CLEAR_NAMESPACE_KEY, PersistentDataType.STRING, "nothingtoseehere");
        sign.getPersistentDataContainer().remove(GLOW_NAMESPACE_KEY);
        sign.getPersistentDataContainer().remove(DYE_NAMESPACE_KEY);
      }
      sign.update(true);
    });
  }

  public void getShopSign(final Block block, final BiConsumer<Shop, Sign> consumer) {

    if (block == null) {
      return;
    }

    if (!Util.isWallSign(block.getType())) {
      return;
    }

    final Block attached = Util.getAttached(block);
    if (attached == null) {
      return;
    }

    final Shop shop = plugin.getShopManager().getShop(attached.getLocation());
    if (shop == null) {
      return;
    }

    if (!(block.getState() instanceof final Sign sign)) {
      return;
    }

    if (consumer != null) {
      consumer.accept(shop, sign);
    }
  }

  @Nullable
  private DyeColor getDyeColor(final Material material) {
    try {
      return DyeColor.valueOf(material.name().replace("_DYE", ""));
    } catch (final IllegalArgumentException ignored) {
      return null;
    }
  }

  @Nullable
  private DyeColor dyeFromSign(final Sign sign) {
    if (!sign.getPersistentDataContainer().has(DYE_NAMESPACE_KEY, PersistentDataType.STRING)) {
      return null;
    }
    try {
      return DyeColor.valueOf(sign.getPersistentDataContainer().get(DYE_NAMESPACE_KEY, PersistentDataType.STRING));
    } catch(final Exception ignored) {
      return null;
    }
  }
}
