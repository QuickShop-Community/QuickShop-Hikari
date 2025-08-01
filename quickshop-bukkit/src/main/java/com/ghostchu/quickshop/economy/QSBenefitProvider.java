package com.ghostchu.quickshop.economy;
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

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.economy.benefit.BenefitOverflowException;
import com.ghostchu.quickshop.api.economy.benefit.BenefitProvider;
import com.ghostchu.quickshop.api.economy.benefit.BenefitsAlreadyException;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.common.util.QuickExecutor;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * QSBenefitManager
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class QSBenefitProvider implements BenefitProvider {

  public static final QSBenefitProvider EMPTY = new QSBenefitProvider();

  private final Map<QUser, BigDecimal> benefits = new HashMap<>();

  public QSBenefitProvider() {

  }

  public QSBenefitProvider(final Map<QUser, BigDecimal> benefits) {

    this.benefits.putAll(benefits);
  }

  /**
   * Deserialize the given JSON string into a QSBenefitManager object.
   *
   * @param json The JSON string to be deserialized. Can be null.
   *
   * @return A QSBenefitManager object created from the deserialized JSON string.
   */
  @NotNull
  public static QSBenefitProvider deserialize(@Nullable final String json) {

    if(CommonUtil.isEmptyString(json)) {
      return new QSBenefitProvider();
    }
    final Map<String, BigDecimal> map = JsonUtil.regular().fromJson(json, new TypeToken<Map<String, Double>>() {
    }.getType());

    final Map<QUser, BigDecimal> parsed = new HashMap<>();
    for(final Map.Entry<String, BigDecimal> event : map.entrySet()) {

      final QUser qUser = QUserImpl.deserialize(QuickShop.getInstance().getPlayerFinder(), event.getKey(), QuickExecutor.getSecondaryProfileIoExecutor());
      parsed.put(qUser, event.getValue());
    }
    return new QSBenefitProvider(parsed);
  }

  /**
   * Returns a map of benefits associated with each user.
   *
   * @return A map where the key is a QUser object representing a user, and the value is a Double
   * representing the benefit amount.
   */
  @Override
  public Map<QUser, BigDecimal> benefits() {

    return this.benefits;
  }

  /**
   * Checks whether the given benefit amount will cause an overflow when added to the existing
   * benefits.
   *
   * @param benefit The benefit amount to be checked for overflow. Must be a double value.
   *
   * @return true if adding the benefit amount will cause an overflow, false otherwise.
   */
  @Override
  public boolean willOverflow(final BigDecimal benefit) {

    final BigDecimal sum = this.benefits.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add).add(benefit);

    return sum.compareTo(BigDecimal.ONE) > 0;
  }

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
  @Override
  public boolean add(final @NotNull QUser user, final BigDecimal percent) throws BenefitOverflowException, BenefitsAlreadyException {

    if(willOverflow(percent)) {

      throw new BenefitOverflowException(percent);
    }

    if(benefits.containsKey(user)) {

      throw new BenefitsAlreadyException();
    }

    benefits.put(user, percent);
    return true;
  }

  /**
   * Removes the specified user from this BenefitManager.
   *
   * @param user The QUser object representing the user to be removed.
   */
  @Override
  public void remove(final @NotNull QUser user) {

    benefits.remove(user);
  }
}