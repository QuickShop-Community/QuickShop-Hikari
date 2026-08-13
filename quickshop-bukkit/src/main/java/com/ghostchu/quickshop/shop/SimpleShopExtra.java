package com.ghostchu.quickshop.shop;

import lombok.Data;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Storage the extra data that QuickShop needs or from 3rd-addon
 *
 * @deprecated
 */
@Deprecated(since = "6.3.0.0", forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "6.4.0.0")
@Data
public class SimpleShopExtra {

  private @NotNull String namespace;
  private @NotNull Map<String, Object> data;

  public SimpleShopExtra(@NotNull final String namespace, @NotNull final Map<String, Object> data) {

    this.namespace = namespace;
    this.data = data;
  }
}
