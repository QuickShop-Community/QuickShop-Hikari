package com.ghostchu.quickshop.api.shop.meta;

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

import com.ghostchu.quickshop.api.economy.benefit.BenefitProvider;
import com.ghostchu.quickshop.api.localization.text.ProxiedLocale;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.IShopType;
import com.ghostchu.quickshop.api.shop.state.ShopState;
import com.ghostchu.quickshop.common.util.JsonUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * ShopIdentity
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface ShopMeta<U> extends ShopPrice<U> {

  String EXTRA_VERSION_KEY = "quickshop.json.extra.version";
  int EXTRA_VERSION = 1;

  /**
   * WARNING: This UUID will changed after plugin reload, shop reload or server restart DO NOT USE
   * IT TO STORE DATA!
   *
   * @return Random UUID
   */
  @NotNull
  UUID getRuntimeRandomUniqueId();

  /**
   * Gets the Shop ID to identify the shop.
   *
   * @return Shop ID -1 if shop in creating state.
   */
  long getShopId();

  /**
   * Internal Only: Give shop that under id_waiting state an ShopId.
   *
   * @param newId The new shop id, once set will cannot change anymore.
   */
  @ApiStatus.Internal
  void setShopId(long newId);

  /**
   * Retrieves additional data associated with the shop in the form of key-value pairs.
   *
   * @return A non-null map containing extra shop data. The map keys and values represent custom
   *         data associated with the shop, where both keys and values are strings.
   * @since 6.3.0.0
   */
  @NotNull
  Map<String, String> getExtra();

  /**
   * Retrieves the extra value associated with the specified key.
   *
   * @param key the NamespacedKey used to locate the extra value; must not be null
   * @return the associated extra value as a String, or null if no value is found
   * @since 6.3.0.0
   */
  default @Nullable String getExtra(@NotNull final NamespacedKey key) {
    return getExtra().get(key.asString());
  }

  /**
   * Retrieves the value associated with the specified {@link NamespacedKey} from the additional shop data.
   * If the specified key does not exist in the data, the provided default value is returned.
   *
   * @param key          The {@link NamespacedKey} representing the key used to retrieve the value.
   *                      This must not be null.
   * @param defaultValue The default value to return if the specified key is not found.
   *                      This can be null.
   * @return The value associated with the specified key, or the default value if the key is not found.
   *         Can return null if the default value is null.
   * @since 6.3.0.0
   */
  default @Nullable String getExtra(@NotNull final NamespacedKey key, @Nullable final String defaultValue) {
    return getExtra().getOrDefault(key.asString(), defaultValue);
  }

  /**
   * Retrieves an extra value associated with the given {@code key} and attempts
   * to parse it as an {@code Integer}. If no value is found or the value cannot
   * be parsed, the provided {@code defaultValue} is returned.
   *
   * @param key the namespaced key used to lookup the extra value, must not be null
   * @param defaultValue the default value to return if no value is found or parsing fails, can be null
   * @return the parsed integer value associated with the key, or {@code defaultValue} if no value exists or parsing fails
   * @since 6.3.0.0
   */
  default @Nullable Integer getExtra(@NotNull final NamespacedKey key, @Nullable final Integer defaultValue) {
    final String value = getExtra(key);

    if (value == null) {
      return defaultValue;
    }

    try {
      return Integer.parseInt(value);
    } catch (final NumberFormatException ignored) {
      return defaultValue;
    }
  }

  /**
   * Retrieves an extra value associated with the specified key, attempting to parse it as a {@code Long}.
   * If the value is not present or cannot be parsed, the provided default value is returned.
   *
   * @param key the {@link NamespacedKey} used to look up the associated value; must not be null
   * @param defaultValue the default value to return if the value is not found or cannot be parsed; may be null
   * @return the parsed {@code Long} value associated with the key, or the {@code defaultValue} if unavailable or unparseable
   * @since 6.3.0.0
   */
  default @Nullable Long getExtra(@NotNull final NamespacedKey key, @Nullable final Long defaultValue) {
    final String value = getExtra(key);

    if (value == null) {
      return defaultValue;
    }

    try {
      return Long.parseLong(value);
    } catch (final NumberFormatException ignored) {
      return defaultValue;
    }
  }

  /**
   * Retrieves the extra value associated with the specified key as a {@code Double}.
   * If the value is not present or cannot be parsed as a {@code Double}, the provided default value is returned.
   *
   * @param key the {@link NamespacedKey} used to look up the extra value; must not be null
   * @param defaultValue the default {@code Double} value to return if no value is present or parsing fails; may be null
   * @return the parsed {@code Double} value if present and valid, or the provided default value if not
   * @since 6.3.0.0
   */
  default @Nullable Double getExtra(@NotNull final NamespacedKey key, @Nullable final Double defaultValue) {
    final String value = getExtra(key);

    if (value == null) {
      return defaultValue;
    }

    try {
      return Double.parseDouble(value);
    } catch (final NumberFormatException ignored) {
      return defaultValue;
    }
  }

  /**
   * Retrieves a floating-point number associated with the given key from extra data.
   * If no value is found or the value cannot be parsed as a float, the specified default value is returned.
   *
   * @param key the key to look up in the extra data; must not be null
   * @param defaultValue the default value to return if the key is not found or cannot be parsed; can be null
   * @return the floating-point value associated with the key, or the default value if not found or invalid
   * @since 6.3.0.0
   */
  default @Nullable Float getExtra(@NotNull final NamespacedKey key, @Nullable final Float defaultValue) {
    final String value = getExtra(key);

    if (value == null) {
      return defaultValue;
    }

    try {
      return Float.parseFloat(value);
    } catch (final NumberFormatException ignored) {
      return defaultValue;
    }
  }

  /**
   * Retrieves an optional extra value associated with the provided {@code key}.
   * If the value is not present or cannot be parsed as a boolean, the {@code defaultValue} is returned.
   *
   * @param key the namespaced key used to retrieve the extra value; must not be null.
   * @param defaultValue the default boolean value to return if the extra value is absent or invalid; may be null.
   * @return the boolean value associated with the specified key, or {@code defaultValue} if the value is absent or invalid.
   * @since 6.3.0.0
   */
  default @Nullable Boolean getExtra(@NotNull final NamespacedKey key, @Nullable final Boolean defaultValue) {
    final String value = getExtra(key);

    if (value == null) {
      return defaultValue;
    }

    return Boolean.parseBoolean(value);
  }

  /**
   * Retrieves a Short value associated with the given NamespacedKey.
   * If no value is found or the value cannot be parsed as a Short, the defaultValue is returned.
   *
   * @param key the NamespacedKey used to lookup the value, must not be null
   * @param defaultValue the default Short value to return if no valid value is found or parsing fails, may be null
   * @return the Short value associated with the key, or the defaultValue if no valid value is found
   * @since 6.3.0.0
   */
  default @Nullable Short getExtra(@NotNull final NamespacedKey key, @Nullable final Short defaultValue) {
    final String value = getExtra(key);

    if (value == null) {
      return defaultValue;
    }

    try {
      return Short.parseShort(value);
    } catch (final NumberFormatException ignored) {
      return defaultValue;
    }
  }

  /**
   * Retrieves the extra value associated with the specified {@link NamespacedKey}.
   * If the value is not found or cannot be parsed as a {@link Byte}, the provided default value is returned.
   *
   * @param key the {@link NamespacedKey} used to retrieve the extra value, must not be null
   * @param defaultValue the default {@link Byte} value to return if no valid value is found or parsing fails, can be null
   * @return the parsed {@link Byte} value associated with the key, or the provided default value if not found or parsing fails
   * @since 6.3.0.0
   */
  default @Nullable Byte getExtra(@NotNull final NamespacedKey key, @Nullable final Byte defaultValue) {
    final String value = getExtra(key);

    if (value == null) {
      return defaultValue;
    }

    try {
      return Byte.parseByte(value);
    } catch (final NumberFormatException ignored) {
      return defaultValue;
    }
  }

  /**
   * Retrieves an extra value associated with the specified key.
   * If no value is found, the provided default value is returned.
   *
   * @param key the non-null key used to look up the extra value
   * @param defaultValue the value to return if no value is associated with the key
   * @return the extra value associated with the key, or the default value if none is found
   * @since 6.3.0.0
   */
  default int getExtra(@NotNull final NamespacedKey key, final int defaultValue) {
    return Objects.requireNonNullElse(getExtra(key, (Integer) null), defaultValue);
  }

  /**
   * Retrieves the extra value associated with the specified key.
   * If no value is found, the provided default value is returned.
   *
   * @param key          the key used to look up the extra value, must not be null
   * @param defaultValue the default value to return if no value is associated with the key
   * @return the extra value associated with the key, or the provided default value if no value is found
   * @since 6.3.0.0
   */
  default long getExtra(@NotNull final NamespacedKey key, final long defaultValue) {
    return Objects.requireNonNullElse(getExtra(key, (Long) null), defaultValue);
  }

  /**
   * Retrieves an extra value associated with the specified {@code NamespacedKey}.
   * If no value is found, the specified default value is returned.
   *
   * @param key the {@code NamespacedKey} used to retrieve the extra value; must not be null
   * @param defaultValue the default value to return if no value is found
   * @return the extra value associated with the given {@code NamespacedKey} or the specified default value if none is found
   * @since 6.3.0.0
   */
  default double getExtra(@NotNull final NamespacedKey key, final double defaultValue) {
    return Objects.requireNonNullElse(getExtra(key, (Double) null), defaultValue);
  }

  /**
   * Retrieves an extra value associated with the specified key. If the value is not present
   * or is null, the provided default value is returned instead.
   *
   * @param key the key used to retrieve the extra value; must not be null
   * @param defaultValue the default value to return if the key does not exist or the value is null
   * @return the boolean value associated with the given key, or the provided default value if no value is present
   * @since 6.3.0.0
   */
  default boolean getExtra(@NotNull final NamespacedKey key, final boolean defaultValue) {
    return Objects.requireNonNullElse(getExtra(key, (Boolean) null), defaultValue);
  }

  /**
   * Associates additional data with the shop using a plugin-specific namespace.
   * The provided data map contains key-value pairs that will be stored.
   *
   * @param plugin The plugin instance used to generate the namespace for the extra data.
   *               This ensures the data is associated with the correct plugin context.
   * @param data   A map containing key-value pairs of additional data to be stored.
   *               If null, no data will be set. Keys are prefixed with a namespaced key
   *               specific to the provided plugin.
   * @since 6.3.0.0
   */
  void setExtra(@NotNull final Plugin plugin, @Nullable final Map<String, String> data);

  /**
   * Associates additional data with the shop using a specified key and data value.
   * The key creates a unique namespace for the data entry.
   *
   * @param key The {@link NamespacedKey} representing the namespace for the data.
   * @param data A non-null string representing the additional data to be stored.
   * @since 6.3.0.0
   */
  void setExtra(@NotNull final NamespacedKey key, @NotNull final String data);

  /**
   * Associates a specified extra piece of data identified by a unique key.
   *
   * @param key  a {@link NamespacedKey} used to uniquely identify the extra data
   * @param data an integer value representing the data to be associated
   * @since 6.3.0.0
   */
  default void setExtra(@NotNull final NamespacedKey key, final int data) {
    setExtra(key, String.valueOf(data));
  }

  /**
   * Sets an extra piece of data associated with the specified key.
   *
   * @param key  the unique identifier for the data, must not be null
   * @param data the data value to be associated with the key
   * @since 6.3.0.0
   */
  default void setExtra(@NotNull final NamespacedKey key, final long data) {
    setExtra(key, String.valueOf(data));
  }

  /**
   * Associates additional metadata with a given key and value.
   * This method accepts a double value, which is internally converted to a string.
   *
   * @param key the key used to identify the metadata; must not be null
   * @param data the double value to be associated as metadata
   * @since 6.3.0.0
   */
  default void setExtra(@NotNull final NamespacedKey key, final double data) {
    setExtra(key, String.valueOf(data));
  }

  /**
   * Associates the specified key with a float value, converting the float value to its string representation.
   *
   * @param key  the unique identifier used to store the float value, must not be null
   * @param data the float value to be associated with the specified key
   * @since 6.3.0.0
   */
  default void setExtra(@NotNull final NamespacedKey key, final float data) {
    setExtra(key, String.valueOf(data));
  }

  /**
   * Sets an extra property identified by the given key to the specified boolean value.
   *
   * @param key The unique key representing the extra property. Must not be null.
   * @param data The boolean value to associate with the key.
   * @since 6.3.0.0
   */
  default void setExtra(@NotNull final NamespacedKey key, final boolean data) {
    setExtra(key, String.valueOf(data));
  }

  /**
   * Sets an extra property for the given key with the specified data.
   *
   * @param key The unique identifier (NamespacedKey) associated with the extra property. Must not be null.
   * @param data The data value (short) to be associated with the given key.
   * @since 6.3.0.0
   */
  default void setExtra(@NotNull final NamespacedKey key, final short data) {
    setExtra(key, String.valueOf(data));
  }

  /**
   * Sets an additional piece of information associated with the given key.
   *
   * @param key  the unique identifier for the extra data, must not be null
   * @param data the byte value to associate with the key
   * @since 6.3.0.0
   */
  default void setExtra(@NotNull final NamespacedKey key, final byte data) {
    setExtra(key, String.valueOf(data));
  }

  /**
   * Removes an extra entry identified by the specified key.
   *
   * @param key the identifier for the extra entry to be removed; must not be null
   * @since 6.3.0.0
   */
  void removeExtra(@NotNull final NamespacedKey key);

  /**
   * Removes all entries from the collection whose keys start with the specified prefix
   * derived from the provided plugin's name.
   *
   * @param plugin the plugin whose name is used to construct the prefix for removal;
   *               must not be null
   * @since 6.3.0.0
   */
  void removeAll(@NotNull final Plugin plugin);

  /**
   * Serializes the additional shop data into a JSON string representation.
   * The serialized data includes key-value pairs from the map returned by {@link #getExtra()},
   * with the addition of a version key and its corresponding value if not already present.
   *
   * @return A JSON string representation of the shop's additional data,
   *         including metadata and version information.
   * @since 6.3.0.0
   */
  default String serializeExtra() {

    final Map<String, String> json = new LinkedHashMap<>(getExtra());
    json.putIfAbsent(EXTRA_VERSION_KEY, EXTRA_VERSION + "");

    return JsonUtil.getGson().toJson(json);
  }

  /**
   * Gets this shop name that set by player
   *
   * @return Shop name, or null if not set
   */
  @Nullable
  String getShopName();

  /**
   * Sets shop name
   *
   * @param shopName shop name, null to remove currently name
   */
  void setShopName(@Nullable String shopName);

  /**
   * Get shop item's ItemStack
   *
   * @return The shop's ItemStack
   */
  @NotNull
  ItemStack getItem();

  /**
   * Set shop item's ItemStack
   *
   * @param item ItemStack to set
   */
  void setItem(@NotNull ItemStack item);

  /**
   * Get shop block
   *
   * @return The shop's block
   */
  @NotNull
  Block getShopBlock();

  /**
   * Gets the currency that shop use
   *
   * @return The currency name
   */
  @Nullable
  String getCurrency();

  /**
   * Sets the currency that shop use
   *
   * @param currency The currency name; null to use default currency
   */
  void setCurrency(@Nullable String currency);

  /**
   * Get shop's price
   *
   * @return Price
   * @deprecated Use {@link ShopPrice#price()} instead
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  double getPrice();

  /**
   * Set shop's new price
   *
   * @param paramDouble New price
   * @deprecated Use {@link ShopPrice#price(Object)} instead.
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  void setPrice(double paramDouble);

  /**
   * Retrieves the current state of the shop.
   *
   * @return the current state of the shop as a ShopState object
   */
  ShopState shopState();

  /**
   * Updates the current state of the shop based on the provided {@code ShopState}.
   *
   * @param state the new state to set for the shop; must not be null
   */
  void shopState(@NotNull ShopState state);

  /**
   * Updates or processes the state of a shop based on the provided identifier.
   *
   * @param shopStateIdentifier a non-null string representing the unique identifier
   *                             for the shop state to be updated or processed.
   */
  void shopState(@NotNull String shopStateIdentifier);

  /**
   * Retrieves the type of shop associated with this entity.
   *
   * @return an instance of IShopType representing the shop type
   */
  IShopType shopType();

  /**
   * Sets the type of shop using the provided shop type parameter.
   *
   * @param newShopType the shop type to set, must not be null
   */
  void shopType(@NotNull IShopType newShopType);

  /**
   * Specifies the type of shop based on the given identifier.
   *
   * @param shopTypeIdentifier the identifier representing the type of shop. Must not be null.
   */
  void shopType(@NotNull String shopTypeIdentifier);

  /**
   * Get shop's owner name, it will return owner name or Admin Shop(i18n) when it is unlimited
   *
   * @param forceUsername Force returns username of shop
   * @param locale        The locale to parse the message
   *
   * @return owner name
   */
  @NotNull
  Component ownerName(boolean forceUsername, @NotNull ProxiedLocale locale);

  /**
   * Get shop's owner name, it will return owner name or Admin Shop(i18n) when it is unlimited
   *
   * @param locale The locale to parse the message
   *
   * @return owner name
   */
  @NotNull
  Component ownerName(@NotNull ProxiedLocale locale);

  /**
   * Get shop's owner name, it will return owner name or Admin Shop(i18n) when it is unlimited
   *
   * @return owner name
   */
  @NotNull
  Component ownerName();


  /**
   * Get shop's owner QUser
   *
   * @return Shop's owner QUser object, can use Bukkit.getOfflinePlayer to convert to the
   * OfflinePlayer.
   */
  @NotNull
  QUser getOwner();

  /**
   * Set new owner to the shop's owner
   *
   * @param qUser New owner user
   */
  void setOwner(@NotNull QUser qUser);

  /**
   * Getting the shop tax account for using, it can be specific uuid or general tax account
   *
   * @return Shop Tax Account or fallback to general tax account
   */
  @Nullable
  QUser getTaxAccount();

  /**
   * Sets shop taxAccount
   *
   * @param taxAccount tax account, null to use general tax account
   */
  void setTaxAccount(@Nullable QUser taxAccount);

  /**
   * Getting the shop tax account, it can be specific uuid or general tax account
   *
   * @return Shop Tax Account, null if use general tax account
   */

  @Nullable
  QUser getTaxAccountActual();

  /**
   * Gets shop status is stacking shop
   *
   * @return The shop stacking status
   */
  boolean isStackingShop();

  /**
   * Get shop is or not in Unlimited Mode (Admin Shop)
   *
   * @return yes or not
   */
  boolean isUnlimited();

  /**
   * Set shop is or not Unlimited Mode (Admin Shop)
   *
   * @param paramBoolean status
   */
  void setUnlimited(boolean paramBoolean);

  /**
   * Gets the benefit in this shop
   */
  @NotNull
  BenefitProvider getShopBenefit();

  /**
   * Sets the benefit in this shop
   */
  void setShopBenefit(@NotNull BenefitProvider benefit);
}