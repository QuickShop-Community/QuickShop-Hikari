package com.ghostchu.quickshop.shop.datatype;

import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.shop.ShopSignStorage;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class ShopSignPersistentDataType
        implements PersistentDataType<String, ShopSignStorage> {

  public static final ShopSignPersistentDataType INSTANCE = new ShopSignPersistentDataType();

  @Override
  public @NotNull Class<String> getPrimitiveType() {

    return String.class;
  }

  @Override
  public @NotNull Class<ShopSignStorage> getComplexType() {

    return ShopSignStorage.class;
  }

  @NotNull
  @Override
  public String toPrimitive(
          @NotNull final ShopSignStorage complex, @NotNull final PersistentDataAdapterContext context) {

    return JsonUtil.getGson().toJson(complex);
  }

  @Override
  public @NotNull ShopSignStorage fromPrimitive(
          @NotNull final String primitive, @NotNull final PersistentDataAdapterContext context) {

    return JsonUtil.getGson().fromJson(primitive, ShopSignStorage.class);
  }

}
