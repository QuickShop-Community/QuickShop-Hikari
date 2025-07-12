package com.ghostchu.quickshop.api.event.panel;
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

import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.PhasedEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.ControlComponent;
import com.ghostchu.quickshop.api.shop.Shop;
import org.jetbrains.annotations.NotNull;

/**
 * ControlComponentGenerateEvent
 *
 * @author creatorfromhell
 * @since 6.2.0.9
 */
public class ControlComponentGenerateEvent extends PhasedEvent {

  protected final Shop shop;
  protected final QUser user;

  protected ControlComponent old;
  protected ControlComponent updated;

  public ControlComponentGenerateEvent(final @NotNull Phase phase, final @NotNull Shop shop,
                                       final QUser user, final ControlComponent old) {

    super(phase);
    this.shop = shop;
    this.user = user;
    this.old = old;
    this.updated = old;
  }

  public ControlComponentGenerateEvent(final @NotNull Phase phase, final @NotNull Shop shop,
                                       final QUser user, final ControlComponent old, final ControlComponent updated) {

    super(phase);
    this.shop = shop;
    this.user = user;
    this.old = old;
    this.updated = updated;
  }

  /**
   * Creates a new instance of PhasedEvent with the specified newPhase.
   *
   * @param newPhase The new Phase for the cloned PhasedEvent
   *
   * @return A new instance of PhasedEvent with the specified newPhase
   */
  @Override
  public PhasedEvent clone(final Phase newPhase) {

    return new ControlComponentGenerateEvent(newPhase, shop, user, old, updated);
  }

  /**
   * Creates a clone of the ControlComponentGenerateEvent with the provided newPhase, old value, and updated value.
   *
   * @param newPhase The new phase for the cloned ControlComponentGenerateEvent
   * @param old The old value for the cloned ControlComponentGenerateEvent
   * @param updated The updated value for the cloned ControlComponentGenerateEvent
   * @return A new instance of ControlComponentGenerateEvent with the specified newPhase, old, and updated values
   */
  public ControlComponentGenerateEvent clone(final Phase newPhase, final ControlComponent old, final ControlComponent updated) {

    return new ControlComponentGenerateEvent(newPhase, shop, user, old, updated);
  }

  public Shop shop() {

    return shop;
  }

  public QUser user() {

    return user;
  }

  /**
   * Retrieves the old ControlComponent from the event.
   *
   * @return The old ControlComponent from the event
   */
  public ControlComponent old() {

    return old;
  }

  /**
   * Checks if the ControlComponent can be updated based on the current phase.
   *
   * @return the updated ControlComponent if the phase allows updates, otherwise returns the old ControlComponent
   */
  public ControlComponent updated() {

    if(!phase.allowUpdate()) {

      return old;
    }

    return updated;
  }

  /**
   * Updates the ControlComponent for this event, if allowed by the phase.
   *
   * @param updated The ControlComponent to update to
   */
  public void updated(final ControlComponent updated) {

    if(!phase.allowUpdate()) {

      return;
    }

    this.updated = updated;
  }
}