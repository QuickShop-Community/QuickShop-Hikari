package com.ghostchu.quickshop.shop.builder;

import com.ghostchu.quickshop.api.economy.benefit.BenefitProvider;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.IShopType;
import com.ghostchu.quickshop.api.shop.ModernShop;
import com.ghostchu.quickshop.api.shop.builder.ShopMetaBuilder;
import com.ghostchu.quickshop.api.shop.components.ShopMeta;
import com.ghostchu.quickshop.api.shop.state.ShopState;
import com.ghostchu.quickshop.shop.components.SimpleShopMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.function.Consumer;

public class SimpleShopMetaBuilder implements ShopMetaBuilder {

  protected long shopId;
  private QUser owner;
  @Nullable
  private String shopName;
  private boolean unlimited;

  private IShopType shopType;
  private ShopState shopState;

  private QUser taxAccount;

  private BenefitProvider benefit;

  @Override
  public long shopId() {

    return shopId;
  }

  @Override
  public ShopMetaBuilder shopId(final long shopId) {

    this.shopId = shopId;
    return this;
  }

  @Override
  public @Nullable String shopName() {

    return shopName;
  }

  @Override
  public ShopMetaBuilder shopName(final @Nullable String shopName) {

    this.shopName = shopName;
    return null;
  }

  @Override
  public ShopState shopState() {

    return this.shopState;
  }

  @Override
  public ShopMetaBuilder shopState(final @NotNull ShopState shopState) {

    this.shopState = shopState;
    return this;
  }

  @Override
  public IShopType shopType() {

    return shopType;
  }

  @Override
  public ShopMetaBuilder shopType(final @NotNull IShopType shopType) {

    this.shopType = shopType;
    return this;
  }

  @Override
  public QUser owner() {

    return this.owner;
  }

  @Override
  public ShopMetaBuilder owner(final @NotNull QUser owner) {

    this.owner = owner;
    return this;
  }

  @Override
  public @Nullable QUser taxAccount() {

    return this.taxAccount;
  }

  @Override
  public ShopMetaBuilder taxAccount(final @Nullable QUser taxAccount) {

    this.taxAccount = taxAccount;
    return this;
  }

  @Override
  public boolean isUnlimited() {

    return this.unlimited;
  }

  @Override
  public ShopMetaBuilder isUnlimited(final boolean unlimited) {

    this.unlimited = unlimited;
    return this;
  }

  @Override
  public BenefitProvider benefit() {

    return this.benefit;
  }

  @Override
  public ShopMetaBuilder withBenefit(final @NotNull QUser user, final BigDecimal percent, final @NotNull Consumer<Boolean> result) {

    try {

      result.accept(benefit.add(user, percent));
    } catch(Exception ignore) {

      result.accept(false);
    }
    return this;
  }

  @Override
  public ShopMetaBuilder lessBenefit(final @NotNull QUser user) {

    benefit.remove(user);
    return this;
  }

  @Override
  public ShopMetaBuilder benefit(final @NotNull BenefitProvider benefit) {

    this.benefit = benefit;
    return this;
  }

  @Override
  public ShopMeta build(final ModernShop<?, ?, ?, ?> shop) {

    return new SimpleShopMeta(shop, this.shopId, this.owner, this.shopName, this.unlimited,
                              this.shopType, this.shopState, this.taxAccount, this.benefit);
  }
}