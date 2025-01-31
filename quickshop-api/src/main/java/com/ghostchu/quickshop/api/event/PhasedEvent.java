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

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

/**
 * PhasedEvent represents an event that has different phases, and is called during different lifecycles.
 *
 * @author creatorfromhell
 * @since 6.2.0.9
 * @see Phase
 */
public abstract class PhasedEvent extends AbstractQSEvent implements QSCancellable {

  protected final Phase phase;

  //Cancellable variables.
  protected Component cancelReason = null;
  protected boolean cancelled = false;
  protected boolean canCancel = true;

  public PhasedEvent() {

    this.phase = Phase.MAIN;
  }

  public PhasedEvent(final boolean async) {

    super(async);

    this.phase = Phase.MAIN;
  }

  public PhasedEvent(final Phase phase) {

    this.phase = phase;
  }

  public PhasedEvent(final Phase phase, final boolean async) {

    super(async);

    this.phase = phase;
  }

  /**
   * Creates a new instance of PhasedEvent with the specified newPhase.
   *
   * @param newPhase The new Phase for the cloned PhasedEvent
   * @return A new instance of PhasedEvent with the specified newPhase
   */
  public abstract PhasedEvent clone(final Phase newPhase);

  public Phase phase() {

    return phase;
  }

  /**
   * Checks if the given phase is equal to the current phase of this PhasedEvent.
   *
   * @param phase The phase to compare with the current phase
   * @return true if the given phase is equal to the current phase, false otherwise
   */
  public boolean isPhase(final Phase phase) {

    return this.phase == phase;
  }

  @Override
  public @Nullable Component getCancelReason() {

    return cancelReason;
  }

  @Override
  public void setCancelled(final boolean cancel, @Nullable final Component reason) throws IllegalStateException {

    if(!phase().cancellable()) {

      throw new IllegalStateException("Attempted to cancel a PhasedEvent that has a Phase, which isn't cancellable. " + phase.name());
    }

    this.cancelled = cancel;
  }

  @Override
  public boolean isCancelled() {

    return this.cancelled && phase.cancellable() && canCancel;
  }


}