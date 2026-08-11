package com.ghostchu.quickshop.api.event.user;

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

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import com.ghostchu.quickshop.api.obj.QUser;

/**
 * UserLimitCalculateEvent
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class UserLimitCalculateEvent extends AbstractQSEvent {

  private final QUser user;
  private int limit;

  public UserLimitCalculateEvent(final QUser user, final int limit) {
    this.user = user;
    this.limit = limit;
  }

  public QUser user() {
    return user;
  }

  public int limit() {
    return limit;
  }

  public void limit(final int limit) {
    this.limit = limit;
  }
}