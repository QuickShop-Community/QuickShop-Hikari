package com.ghostchu.quickshop.api.event;

/*
 * QuickShop-Hikari
 * Copyright (C) 2024 Daniel "creatorfromhell" Vidmar
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

/**
 * EventLifeCycle
 *
 * @author creatorfromhell
 * @since 6.2.0.9
 */
public enum Phase {
  PRE(false),
  MAIN(true),
  POST(false);

  private final boolean cancellable;

  Phase(final boolean cancellable) {

    this.cancellable = cancellable;
  }

  public boolean cancellable() {

    return cancellable;
  }
}