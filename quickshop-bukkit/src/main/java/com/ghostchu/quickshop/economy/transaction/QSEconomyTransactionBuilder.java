package com.ghostchu.quickshop.economy.transaction;
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

import com.ghostchu.quickshop.api.economy.benefit.BenefitProvider;
import com.ghostchu.quickshop.api.obj.QUser;

import java.math.BigDecimal;

/**
 * QSEconomyTransactionBuilder
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class QSEconomyTransactionBuilder {

  private BenefitProvider benefitManager;
  private String world;
  private String currency;
  private BigDecimal amount;
  private BigDecimal tax;
  private QUser from;
  private QUser to;
  private QUser taxer;

  public QSEconomyTransactionBuilder() {

  }

  public QSEconomyTransactionBuilder benefitManager(final BenefitProvider benefitManager) {

    this.benefitManager = benefitManager;
    return this;
  }

  public QSEconomyTransactionBuilder world(final String world) {

    this.world = world;
    return this;
  }

  public QSEconomyTransactionBuilder currency(final String currency) {

    this.currency = currency;
    return this;
  }

  public QSEconomyTransactionBuilder amount(final BigDecimal amount) {

    this.amount = amount;
    return this;
  }

  public QSEconomyTransactionBuilder tax(final BigDecimal tax) {

    this.tax = tax;
    return this;
  }

  public QSEconomyTransactionBuilder from(final QUser from) {

    this.from = from;
    return this;
  }

  public QSEconomyTransactionBuilder to(final QUser to) {

    this.to = to;
    return this;
  }

  public QSEconomyTransactionBuilder taxer(final QUser taxer) {

    this.taxer = taxer;
    return this;
  }

  public QSEconomyTransaction build() {

    return new QSEconomyTransaction(
            benefitManager,
            world,
            currency,
            amount,
            (tax != null)? tax : BigDecimal.ZERO,
            from,
            to,
            taxer
    );
  }
}