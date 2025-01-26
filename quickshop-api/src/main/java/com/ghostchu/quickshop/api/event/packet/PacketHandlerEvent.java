package com.ghostchu.quickshop.api.event.packet;
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

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import com.ghostchu.quickshop.api.shop.display.PacketHandler;

/**
 * PacketHandlersAdded
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public abstract class PacketHandlerEvent extends AbstractQSEvent {

  protected PacketHandler<?> packetHandler;

  public PacketHandlerEvent(final PacketHandler<?> packetHandler) {

    this.packetHandler = packetHandler;
  }

  public PacketHandler<?> packetHandler() {

    return packetHandler;
  }
}