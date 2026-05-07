package com.ghostchu.quickshop.shop.components;

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
import com.ghostchu.quickshop.api.economy.benefit.BenefitProvider;
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.settings.type.ShopOwnerNameEvent;
import com.ghostchu.quickshop.api.event.settings.type.ShopStateEvent;
import com.ghostchu.quickshop.api.event.settings.type.ShopTaxAccountEvent;
import com.ghostchu.quickshop.api.event.settings.type.ShopTypeEnhancedEvent;
import com.ghostchu.quickshop.api.event.settings.type.benefit.ShopBenefitEvent;
import com.ghostchu.quickshop.api.localization.text.ProxiedLocale;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.IShopType;
import com.ghostchu.quickshop.api.shop.ModernShop;
import com.ghostchu.quickshop.api.shop.builder.ShopMetaBuilder;
import com.ghostchu.quickshop.api.shop.components.ShopMeta;
import com.ghostchu.quickshop.api.shop.service.result.ShopChangeType;
import com.ghostchu.quickshop.api.shop.state.ShopState;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.shop.SimpleShopManager;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.logger.Log;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Objects;

/**
 * SimpleShopMeta
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class SimpleShopMeta implements ShopMeta {

  private final ModernShop<?, ?, ?, ?> shop;

  protected long shopId;
  private QUser owner;
  @Nullable
  private String shopName;
  private boolean unlimited;

  private IShopType shopType;
  private ShopState shopState;

  private QUser taxAccount;

  @NotNull
  private BenefitProvider benefit;

  public SimpleShopMeta(@NotNull final ModernShop<?, ?, ?, ?> shop) {
    this.shop = shop;
  }

  public SimpleShopMeta(@NotNull final ModernShop<?, ?, ?, ?> shop,
                        @NotNull final QUser owner,
                        @Nullable final String shopName,
                        final boolean unlimited,
                        @NotNull final IShopType shopType,
                        @NotNull final ShopState shopState,
                        @Nullable final QUser taxAccount,
                        @NotNull final BenefitProvider benefit) {

    this(shop, shop.meta().getShopId(), owner, shopName, unlimited, shopType, shopState, taxAccount, benefit);
  }

  public SimpleShopMeta(@NotNull final ModernShop<?, ?, ?, ?> shop,
                        final long shopId,
                        @NotNull final QUser owner,
                        @Nullable final String shopName,
                        final boolean unlimited,
                        @NotNull final IShopType shopType,
                        @NotNull final ShopState shopState,
                        @Nullable final QUser taxAccount,
                        @NotNull final BenefitProvider benefit) {

    this.shop = shop;
    this.shopId = shopId;
    this.owner = owner;
    this.shopName = shopName;
    this.unlimited = unlimited;
    this.shopType = shopType;
    this.shopState = shopState;
    this.taxAccount = taxAccount;
    this.benefit = benefit;
  }

  /**
   * Gets the Shop ID to identify the shop.
   *
   * @return Shop ID -1 if shop in creating state.
   */
  @Override
  public long getShopId() {

    return this.shopId;
  }

  /**
   * Internal Only: Give shop that under id_waiting state an ShopId.
   *
   * @param newId The new shop id, once set will cannot change anymore.
   */
  @Override
  public void setShopId(final long newId) {

    if(this.shopId != -1) {
      throw new IllegalStateException("Cannot set shop id once it fully created.");
    }
    this.shopId = newId;

    //TODO: Determine how to mark as dirty. Maybe through shop service?
    //setDirty();
  }

  /**
   * Gets this shop name that set by player
   *
   * @return Shop name, or null if not set
   */
  @Override
  public @Nullable String getShopName() {

    return this.shopName;
  }

  /**
   * Sets shop name
   *
   * @param shopName shop name, null to remove currently name
   */
  @Override
  public void setShopName(@Nullable final String shopName) {

    if(CommonUtil.strEquals(this.shopName, shopName)) {
      return;
    }
    this.shopName = shopName;

    //TODO: Determine how to mark as dirty. Maybe through shop service?
    //setDirty();
  }

  /**
   * Retrieves the current state of the shop.
   *
   * @return the current state of the shop as a ShopState object
   */
  @Override
  public ShopState shopState() {

    final ShopStateEvent event = new ShopStateEvent(Phase.RETRIEVE, shop, this.shopState);
    event.callEvent();

    return event.updated();
  }

  /**
   * Updates the current state of the shop based on the provided {@code ShopState}.
   *
   * @param state the new state to set for the shop; must not be null
   */
  @Override
  public void shopState(@NotNull final ShopState state) {

    if(this.shopState.identifier().equalsIgnoreCase(state.identifier())) {

      return;
    }

    ShopStateEvent event = new ShopStateEvent(Phase.PRE, shop, this.shopState, state);
    event.callEvent();

    event = event.clone(Phase.MAIN);

    if(event.callCancellableEvent()) {

      Log.debug("Some addon cancelled shop state changes, target shop: " + this);
      return;
    }

    this.shopState = event.updated();

    event = event.clone(Phase.POST);
    event.callEvent();

    //TODO: Determine how to mark as dirty. Maybe through shop service?
    //setDirty();
    //setSignText();
  }

  /**
   * Updates or processes the state of a shop based on the provided identifier.
   *
   * @param shopStateIdentifier a non-null string representing the unique identifier for the shop
   *                            state to be updated or processed.
   */
  @Override
  public void shopState(@NotNull final String shopStateIdentifier) {

    shopState(QuickShop.getInstance().getShopManager().shopStateOrDefault(shopStateIdentifier));
  }

  /**
   * Retrieves the type of shop associated with this entity.
   *
   * @return an instance of IShopType representing the shop type
   */
  @Override
  public IShopType shopType() {

    final ShopTypeEnhancedEvent event = new ShopTypeEnhancedEvent(Phase.RETRIEVE, shop, this.shopType);
    event.callEvent();

    return event.updated();
  }

  /**
   * Sets the type of shop using the provided shop type parameter.
   *
   * @param newShopType the shop type to set, must not be null
   */
  @Override
  public void shopType(@NotNull final IShopType newShopType) {

    if(this.shopType.identifier().equalsIgnoreCase(newShopType.identifier())) {

      return;
    }

    ShopTypeEnhancedEvent event = new ShopTypeEnhancedEvent(Phase.PRE, shop, this.shopType, newShopType);
    event.callEvent();

    event = event.clone(Phase.MAIN);

    if(event.callCancellableEvent()) {

      Log.debug("Some addon cancelled shop type changes, target shop: " + this);
      return;
    }

    this.shopType = event.updated();

    event = event.clone(Phase.POST);
    event.callEvent();

    //TODO: Determine how to mark as dirty. Maybe through shop service?
    //setDirty();
    //setSignText();
  }

  /**
   * Specifies the type of shop based on the given identifier.
   *
   * @param shopTypeIdentifier the identifier representing the type of shop. Must not be null.
   */
  @Override
  public void shopType(@NotNull final String shopTypeIdentifier) {

    shopType(QuickShop.getInstance().getShopManager().shopTypeOrDefault(shopTypeIdentifier));
  }

  /**
   * Get shop's owner name, it will return owner name or Admin Shop(i18n) when it is unlimited
   *
   * @param forceUsername Force returns username of shop
   * @param locale        The locale to parse the message
   *
   * @return owner name
   */
  @Override
  public @NotNull Component ownerName(final boolean forceUsername, @NotNull final ProxiedLocale locale) {

    Component name;
    if(!forceUsername && isUnlimited()) {
      name = QuickShop.getInstance().text().of("admin-shop").forLocale(locale.getLocale());
    } else {
      final String playerName = this.getOwner().getUsername();
      if(playerName == null) {
        name = QuickShop.getInstance().text().of("unknown-owner").forLocale(locale.getLocale());
      } else {
        name = Component.text(playerName);
      }
    }
    if(getOwner().isRealPlayer()) {
      name = name.hoverEvent(
              QuickShop.getInstance().text().of("real-player-component-hover", getOwner().getUniqueId(), getOwner().getUsername(), getOwner().getDisplay()).forLocale(locale.getLocale())
                            );
    } else {
      name = name.hoverEvent(
              QuickShop.getInstance().text().of("virtual-player-component-hover", getOwner().getUniqueId(), getOwner().getUsername(), getOwner().getDisplay()).forLocale(locale.getLocale())
                            );

    }

    final ShopOwnerNameEvent event = new ShopOwnerNameEvent(Phase.RETRIEVE, this.shop, name);
    event.callEvent();

    name = event.updated();
    return name;
  }

  /**
   * Get shop's owner name, it will return owner name or Admin Shop(i18n) when it is unlimited
   *
   * @param locale The locale to parse the message
   *
   * @return owner name
   */
  @Override
  public @NotNull Component ownerName(@NotNull final ProxiedLocale locale) {

    return ownerName(false, locale);
  }

  /**
   * Get shop's owner name, it will return owner name or Admin Shop(i18n) when it is unlimited
   *
   * @return owner name
   */
  @Override
  public @NotNull Component ownerName() {

    return ownerName(false, MsgUtil.getDefaultGameLanguageLocale());
  }

  /**
   * Get shop's owner QUser
   *
   * @return Shop's owner QUser object, can use Bukkit.getOfflinePlayer to convert to the
   * OfflinePlayer.
   */
  @Override
  public @NotNull QUser getOwner() {

    return this.owner;
  }

  /**
   * Set new owner to the shop's owner
   *
   * @param owner New owner user
   */
  @Override
  public void setOwner(@NotNull final QUser owner) {

    if(this.owner.equals(owner)) {
      return;
    }
    this.owner = owner;

    //TODO: Determine how to mark as dirty. Maybe through shop service?
    //setDirty();
    //setSignText(plugin.getTextManager().findRelativeLanguages(owner, false));
  }

  /**
   * Getting the shop tax account for using, it can be specific uuid or general tax account
   *
   * @return Shop Tax Account or fallback to general tax account
   */
  @Override
  public @Nullable QUser getTaxAccount() {

    QUser uuid = null;
    if(taxAccount != null) {
      uuid = taxAccount;
    } else {
      if(((SimpleShopManager)QuickShop.getInstance().getShopManager()).getCacheTaxAccount() != null) {
        uuid = ((SimpleShopManager)QuickShop.getInstance().getShopManager()).getCacheTaxAccount();
      }
    }
    final ShopTaxAccountEvent event = new ShopTaxAccountEvent(Phase.RETRIEVE, this.shop, uuid);
    event.callEvent();

    return event.updated();
  }

  /**
   * Sets shop taxAccount
   *
   * @param taxAccount tax account, null to use general tax account
   */
  @Override
  public void setTaxAccount(@Nullable final QUser taxAccount) {

    if(Objects.equals(taxAccount, this.taxAccount)) {
      return;
    }

    ShopTaxAccountEvent event = new ShopTaxAccountEvent(Phase.PRE, this.shop, this.taxAccount, taxAccount);
    event.callEvent();

    event = event.clone(Phase.MAIN);
    if(event.callCancellableEvent()) {

      return;
    }

    this.taxAccount = event.updated();

    event = event.clone(Phase.POST);
    event.callEvent();

    //TODO: Determine how to mark as dirty. Maybe through shop service?
    //setDirty();
  }

  /**
   * Getting the shop tax account, it can be specific uuid or general tax account
   *
   * @return Shop Tax Account, null if use general tax account
   */
  @Override
  public @Nullable QUser getTaxAccountActual() {

    return taxAccount;
  }

  /**
   * Get shop is or not in Unlimited Mode (Admin Shop)
   *
   * @return yes or not
   */
  @Override
  public boolean isUnlimited() {

    return this.unlimited;
  }

  /**
   * Set shop is or not Unlimited Mode (Admin Shop)
   *
   * @param unlimited status
   */
  @Override
  public void setUnlimited(final boolean unlimited) {

    if(this.unlimited == unlimited) {
      return;
    }
    this.unlimited = unlimited;

    //TODO: Determine how to mark as dirty. Maybe through shop service?
    //setDirty();
    //this.setSignText();
  }

  /**
   * Gets the benefit in this shop
   */
  @Override
  public @NotNull BenefitProvider getShopBenefit() {

    final ShopBenefitEvent event = ShopBenefitEvent.RETRIEVE(this.shop, this.benefit);
    event.callEvent();

    return event.updated();
  }

  /**
   * Sets the benefit in this shop
   */
  @Override
  public void setShopBenefit(@NotNull final BenefitProvider benefit) {

    this.benefit = benefit;

    //TODO: Determine how to mark as dirty. Maybe through shop service?
    //setDirty();
  }

  @Override
  public EnumSet<ShopChangeType> diff(final @Nullable ShopMeta compare) {

    final EnumSet<ShopChangeType> changes = EnumSet.noneOf(ShopChangeType.class);

    if(compare == null || this.owner.getUniqueId() != compare.getOwner().getUniqueId()) {
      changes.add(ShopChangeType.OWNER);
    }

    if(compare == null && this.shopName != null
       || compare != null && this.shopName == null && compare.getShopName() != null
       || compare != null && this.shopName != null && !this.shopName.equals(compare.getShopName())) {
      changes.add(ShopChangeType.NAME);
    }

    if(compare == null || this.unlimited != compare.isUnlimited()) {
      changes.add(ShopChangeType.ADMIN_STATUS);
    }

    if(compare == null || !Objects.equals(this.shopType.identifier(), compare.shopType().identifier())) {
      changes.add(ShopChangeType.TYPE);
    }

    if(compare == null || !Objects.equals(this.shopState.identifier(), compare.shopState().identifier())) {
      changes.add(ShopChangeType.STATE);
    }

    if(compare == null || compare.getTaxAccount() == null && this.taxAccount != null
       || compare.getTaxAccount() != null && !Objects.equals(this.taxAccount.getUniqueId(), compare.getTaxAccount().getUniqueId())) {
      changes.add(ShopChangeType.TAX_ACCOUNT);
    }

    if(compare == null || !compare.getShopBenefit().serialize().equals(this.benefit.serialize())) {
      changes.add(ShopChangeType.BENEFITS);
    }
    return changes;
  }

  @Override
  public ShopMetaBuilder builder() {

    return null;
  }
}
