package com.ghostchu.quickshop.shop;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

/**
 * Storage the extra data that QuickShop needs or from 3rd-addon
 *
 * @deprecated
 */
@Deprecated(since = "6.3.0.0", forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "6.4.0.0")
public class SimpleShopExtra {

  @NotNull
  private String namespace;
  @NotNull
  private Map<String, Object> data;

  public SimpleShopExtra(@NotNull final String namespace, @NotNull final Map<String, Object> data) {

    this.namespace = namespace;
    this.data = data;
  }

  @NotNull
  public String getNamespace() {

    return this.namespace;
  }

  @NotNull
  public Map<String, Object> getData() {

    return this.data;
  }

  public void setNamespace(@NotNull final String namespace) {

    if(namespace == null) {
      throw new NullPointerException("namespace is marked non-null but is null");
    }
    this.namespace = namespace;
  }

  public void setData(@NotNull final Map<String, Object> data) {

    if(data == null) {
      throw new NullPointerException("data is marked non-null but is null");
    }
    this.data = data;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof SimpleShopExtra)) return false;
    final SimpleShopExtra other = (SimpleShopExtra)o;
    return Objects.equals(this.getNamespace(), other.getNamespace())
           && Objects.equals(this.getData(), other.getData());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.getNamespace(), this.getData());
  }

  @Override
  public String toString() {

    return "SimpleShopExtra(namespace=" + this.getNamespace() + ", data=" + this.getData() + ")";
  }
}
