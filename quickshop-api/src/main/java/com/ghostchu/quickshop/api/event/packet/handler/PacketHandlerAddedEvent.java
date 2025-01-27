package com.ghostchu.quickshop.api.event.packet.handler;
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

import com.ghostchu.quickshop.api.event.QSCancellable;
import com.ghostchu.quickshop.api.event.packet.PacketHandlerEvent;
import com.ghostchu.quickshop.api.shop.display.PacketHandler;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

/**
 * PacketHandlerLoadEvent
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class PacketHandlerAddedEvent extends PacketHandlerEvent implements QSCancellable {

  private boolean cancelled;
  private @Nullable Component cancelReason;

  public PacketHandlerAddedEvent(final PacketHandler<?> packetHandler) {

    super(packetHandler);
  }

  @Override
  public @Nullable Component getCancelReason() {

    return cancelReason;
  }

  @Override
  public void setCancelled(final boolean cancel, @Nullable final Component reason) {

    this.cancelled = cancel;
    this.cancelReason = reason;
  }

  @Override
  public boolean isCancelled() {

    return this.cancelled;
  }
}