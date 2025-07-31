package com.ghostchu.quickshop.api.economyrevamp.benefit;
/*
 * QuickShop-Hikari
 * Copyright (C) 2025 Daniel "creatorfromhell" Vidmar
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

import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.common.util.JsonUtil;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BenefitManager
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public interface BenefitProvider {

  /**
   * Returns a map of benefits associated with each user.
   *
   * @return A map where the key is a QUser object representing a user, and the value is a
   * BigDecimal representing the benefit amount.
   */
  Map<QUser, BigDecimal> benefits();

  /**
   * Indicates whether there are no benefits added.
   *
   * @return true if there are no benefits, false otherwise.
   */
  default boolean none() {

    return benefits().isEmpty();
  }

  /**
   * Checks whether the specified user has a benefit associated with them.
   *
   * @param user The user to be checked for benefits. Must not be null.
   *
   * @return true if the user has a benefit associated with them, false otherwise.
   */
  default boolean hasBenefit(final @NotNull QUser user) {

    return benefits().containsKey(user);
  }

  /**
   * Checks whether the given benefit amount will cause an overflow when added to the existing
   * benefits.
   *
   * @param benefit The benefit amount to be checked for overflow. Must be a double value.
   *
   * @return true if adding the benefit amount will cause an overflow, false otherwise.
   */
  boolean willOverflow(final BigDecimal benefit);

  /**
   * Adds a benefit percentage to the specified user.
   *
   * @param user    the user to add the specified benefit percentage
   * @param percent the percentage of benefit to add
   *
   * @return true if the benefit was successfully added, false otherwise
   *
   * @throws BenefitOverflowException if adding the benefit causes an overflow
   * @throws BenefitsAlreadyException if benefits are already added for the user
   */
  boolean add(final @NotNull QUser user, final BigDecimal percent) throws BenefitOverflowException, BenefitsAlreadyException;

  /**
   * Removes the specified user from this BenefitManager.
   *
   * @param user The QUser object representing the user to be removed.
   */
  void remove(final @NotNull QUser user);

  /**
   * Serializes the benefits associated with each user into a JSON string.
   *
   * @return A JSON string representation of the benefits map, where the key is a serialized QUser
   * object and the value is a Double representing the benefit amount.
   */
  @NotNull
  default String serialize() {

    final Map<String, BigDecimal> map = new LinkedHashMap<>();
    for(final Map.Entry<QUser, BigDecimal> entry : this.benefits().entrySet()) {

      map.put(entry.getKey().serialize(), entry.getValue());
    }
    return JsonUtil.regular().toJson(map);
  }
}