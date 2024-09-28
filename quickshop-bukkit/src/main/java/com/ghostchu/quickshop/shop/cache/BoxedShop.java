package com.ghostchu.quickshop.shop.cache;

import com.ghostchu.quickshop.api.shop.Shop;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class BoxedShop {

  @Nullable
  private final WeakReference<Shop> shopWeakRef;

  public BoxedShop(@Nullable Shop shop) {

    if(shop != null) {
      this.shopWeakRef = new WeakReference<>(shop);
    } else {
      shopWeakRef = null;
    }
  }

  @Nullable
  public Shop getShop() {

    return shopWeakRef == null? null : shopWeakRef.get();
  }

  public boolean isValid() {

    if(shopWeakRef != null) {
      Shop shop = shopWeakRef.get();
      if(shop != null) {
        return shop.isValid();
      }
    }
    return false;
  }
}
