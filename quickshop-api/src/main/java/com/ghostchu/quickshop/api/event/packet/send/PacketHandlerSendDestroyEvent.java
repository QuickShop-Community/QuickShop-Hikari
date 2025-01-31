package com.ghostchu.quickshop.api.event.packet.send;
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

import com.ghostchu.quickshop.api.event.packet.PacketHandlerEvent;
import com.ghostchu.quickshop.api.shop.display.PacketFactory;
import com.ghostchu.quickshop.api.shop.display.PacketHandler;

/**
 * PacketHandlerSendDestroyEvent
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class PacketHandlerSendDestroyEvent<T> extends PacketHandlerEvent {

  protected PacketFactory<T> packetFactory;
  protected T destroyPacket;

  public PacketHandlerSendDestroyEvent(final PacketHandler<?> packetHandler,
                                    final PacketFactory<T> packetFactory,
                                    final T destroyPacket) {

    super(packetHandler);

    this.packetFactory = packetFactory;
    this.destroyPacket = destroyPacket;
  }

  public PacketFactory<T> packetFactory() {

    return packetFactory;
  }

  public void packetFactory(final PacketFactory<T> packetFactory) {

    this.packetFactory = packetFactory;
  }

  public T destroyPacket() {

    return destroyPacket;
  }

  public void destroyPacket(final T destroyPacket) {

    this.destroyPacket = destroyPacket;
  }
}