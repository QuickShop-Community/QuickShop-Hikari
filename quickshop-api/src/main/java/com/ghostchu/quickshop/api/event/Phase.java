package com.ghostchu.quickshop.api.event;

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

/**
 * Phase represents different phases for an {@link PhasedEvent phased event} lifecycle.
 *
 * @author creatorfromhell
 * @since 6.2.0.9
 */
public enum Phase {

  /**
   * Represents the Pre phase, which happens before an action occurs on object/value.
   */
  PRE(false, false),

  /**
   * Represents the main phase, which is when the action is occurring.
   */
  MAIN(true, true),

  /**
   * Represents the post phase, which happens after the action occurs.
   */
  POST(false, false),

  /**
   * Represents the retrieval phase, which happens when calls are made to get an object/value
   */
  RETRIEVE(false, false);


  private final boolean allowUpdate;
  private final boolean cancellable;

  Phase(final boolean allowUpdate, final boolean cancellable) {

    this.allowUpdate = allowUpdate;

    this.cancellable = cancellable;
  }

  /**
   * Retrieves the cancellable status of the current phase.
   *
   * @return true if the phase is cancellable, false otherwise
   */
  public boolean cancellable() {

    return cancellable;
  }

  public boolean allowUpdate() {

    return allowUpdate;
  }
}