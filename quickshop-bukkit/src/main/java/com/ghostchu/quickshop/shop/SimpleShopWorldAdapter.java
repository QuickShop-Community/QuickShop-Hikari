package com.ghostchu.quickshop.shop;

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
import com.ghostchu.quickshop.ServiceInjector;
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.general.ShopSignUpdateEvent;
import com.ghostchu.quickshop.api.event.settings.type.ShopSignLinesEvent;
import com.ghostchu.quickshop.api.localization.text.ProxiedLocale;
import com.ghostchu.quickshop.api.shop.ModernShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopWorldAdapter;
import com.ghostchu.quickshop.api.shop.display.DisplayType;
import com.ghostchu.quickshop.shop.datatype.ShopSignPersistentDataType;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.EqualsAndHashCode;
import net.kyori.adventure.text.Component;
import org.bukkit.DyeColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.ghostchu.quickshop.util.Util.plugin;

/**
 * SimpleShopWorldAdapter
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class SimpleShopWorldAdapter implements ShopWorldAdapter {

  /**
   * Checks whether the specified shop instance is valid. This method determines if the given shop
   * meets the necessary criteria for being considered a valid shop within the system.
   *
   * @param shop The shop instance to validate, represented by a {@code ModernShop<?, ?, ?, ?>}.
   *
   * @return {@code true} if the shop is valid, {@code false} otherwise.
   *
   * @since 6.3.0.0
   */
  @Override
  public boolean isValidShop(final @NotNull ModernShop<?, ?, ?, ?> shop) {

    Util.ensureThread(false);
    if(shop.lifecycle().isDeleted()) {
      return false;
    }
    return Util.canBeShop(shop.bukkitLocation().getBlock());
  }

  /**
   * Ensures that the display location for the specified shop is correctly handled. This method may
   * involve verifying the shop's display location, teleporting entities, or respawning display
   * objects as required to maintain consistency.
   *
   * @param shop The shop instance whose display location is to be checked and updated, represented
   *             by a {@code ModernShop<?, ?, ?, ?>}.
   *
   * @since 6.3.0.0
   */
  @Override
  public void checkDisplay(final @NotNull ModernShop<?, ?, ?, ?> shop) {

  }

  /**
   * Claim a sign as shop sign (modern method)
   *
   * @param sign The shop sign
   */
  @Override
  public void claimShopSign(final @NotNull ModernShop<?, ?, ?, ?> shop, @NotNull final Sign sign) {

    if(!sign.getPersistentDataContainer().has(Shop.SHOP_NAMESPACED_KEY, ShopSignPersistentDataType.INSTANCE)) {
      sign.getPersistentDataContainer().set(Shop.SHOP_NAMESPACED_KEY, ShopSignPersistentDataType.INSTANCE, saveToShopSignStorage());
      sign.update();
    }
  }

  /**
   * Get shop signs, may have multi signs
   *
   * @return Signs for the shop
   *
   * @since 6.3.0.0
   */
  @Override
  public @NotNull List<Sign> getSigns(final @NotNull ModernShop<?, ?, ?, ?> shop) {

    Util.ensureThread(false);
    final List<Sign> signs = new ArrayList<>(4);
    if(shop.bukkitLocation().getWorld() == null) {

      return Collections.emptyList();
    }

    final Block[] blocks = new Block[4];
    blocks[0] = shop.bukkitLocation().getBlock().getRelative(BlockFace.EAST);
    blocks[1] = shop.bukkitLocation().getBlock().getRelative(BlockFace.NORTH);
    blocks[2] = shop.bukkitLocation().getBlock().getRelative(BlockFace.SOUTH);
    blocks[3] = shop.bukkitLocation().getBlock().getRelative(BlockFace.WEST);
    for(final Block b : blocks) {

      if(b == null) {
        continue;
      }
      final BlockState state = b.getState(false);
      if(!(state instanceof final Sign sign)) {
        continue;
      }
      if(!shop.bukkitLocation().getBlock().equals(Util.getAttached(b))) {
        continue;
      }
      if(isShopSign(sign)) {
        claimShopSign(sign);
        signs.add(sign);
      }
    }

    return signs;
  }

  /**
   * Checks if a Sign is a ShopSign
   *
   * @param sign Target {@link Sign}
   *
   * @return Is shop info sign
   */
  @Override
  public boolean isShopSign(@NotNull final Sign sign) {

    return false;
  }

  /**
   * Retrieves the localized text to be displayed on a shop's sign. This method provides the sign
   * text in the form of a list of {@link Component}, customized according to the specified shop
   * instance and locale.
   *
   * @param shop   The shop instance for which the sign text is to be retrieved, represented by a
   *               {@code ModernShop<?, ?, ?, ?>}.
   * @param locale The locale to be used for generating the sign text, represented by a
   *               {@code ProxiedLocale}.
   *
   * @return A list of {@link Component} objects representing the text of the shop's sign, with each
   * list entry corresponding to a line of text.
   *
   * @since 6.3.0.0
   */
  @Override
  public List<Component> getSignText(final @NotNull ModernShop<?, ?, ?, ?> shop, final @NotNull ProxiedLocale locale) {


    Util.ensureThread(false);

    final LinkedList<Component> lines = QuickShop.getInstance().getShopManager().shopLayoutProvider().render(shop, locale);

    final ShopSignLinesEvent event = new ShopSignLinesEvent(Phase.RETRIEVE, shop, lines);
    event.callEvent();

    return event.updated();
  }

  /**
   * Generate new sign texts on shop's sign.
   *
   * @since 6.3.0.0
   */
  @Override
  public void setSignText(final @NotNull ModernShop<?, ?, ?, ?> shop) {

    Util.ensureThread(false);
    if(!Util.isLoaded(shop.bukkitLocation())) {
      return;
    }
    QuickShop.folia().getScheduler().runAtLocation(shop.bukkitLocation(), (consumer)->{
      this.setSignText(shop, getSignText(shop, QuickShop.getInstance().getTextManager().findRelativeLanguages(MsgUtil.getDefaultGameLanguageCode())));
    });
  }

  /**
   * Sets the text displayed on a shop's sign. This method allows for updating the sign text of the
   * specified shop using a list of {@link Component} objects where each entry represents a line of
   * text.
   *
   * @param shop               The shop instance whose sign text is to be updated, represented by a
   *                           {@code ModernShop<?, ?, ?, ?>}.
   * @param lines A list of {@link Component} objects representing the new sign text,
   *                           with each list entry corresponding to a line of text.
   *
   * @since 6.3.0.0
   */
  @Override
  public void setSignText(final @NotNull ModernShop<?, ?, ?, ?> shop, @NotNull final List<Component> lines) {

    Util.ensureThread(false);
    Log.debug("Globally sign text setting...");
    final List<Sign> signs = this.getSigns(shop);

    final ShopSignLinesEvent event = new ShopSignLinesEvent(Phase.POST, shop, lines);
    event.callEvent();

    for(final Sign sign : signs) {

      final DyeColor dyeColor = Util.getDyeColor();
      if(dyeColor != null) {
        sign.setColor(dyeColor);
      }
      final boolean isGlowing = QuickShop.getInstance().getConfig().getBoolean("shop.sign-glowing", false);
      final boolean isWaxed = QuickShop.getInstance().getConfig().getBoolean("shop.sign-wax", false);

      sign.setGlowingText(isGlowing);
      sign.setWaxed(isWaxed);
      sign.update(true);
      QuickShop.getInstance().platform().setLines(sign, event.updated());

      new ShopSignUpdateEvent(shop, sign).callEvent();
    }
    if(QuickShop.getInstance().getSignHooker() != null) {
      Log.debug("Start sign broadcast...");
      QuickShop.getInstance().getSignHooker().updatePerPlayerShopSignBroadcast(shop.bukkitLocation(), this);
      Log.debug("Sign broadcast completed.");
    }
  }

  /**
   * Updates the text displayed on a shop's sign with content localized to the given locale. This
   * method dynamically adjusts the shop sign's text based on the specified shop instance and target
   * locale for display purposes.
   *
   * @param shop   The shop instance whose sign text is to be updated, represented by a
   *               {@code ModernShop<?, ?, ?, ?>}.
   * @param locale The locale used to localize the text displayed on the shop's sign, represented by
   *               a {@code ProxiedLocale}.
   *
   * @since 6.3.0.0
   */
  @Override
  public void setSignText(final @NotNull ModernShop<?, ?, ?, ?> shop, @NotNull final ProxiedLocale locale) {

    //Util.ensureThread(false);
    if(!Util.isLoaded(shop.bukkitLocation())) {
      return;
    }

    QuickShop.folia().getScheduler().runAtLocation(shop.bukkitLocation(), (consumer)->{
      this.setSignText(shop, getSignText(shop, locale));
    });
  }
}
