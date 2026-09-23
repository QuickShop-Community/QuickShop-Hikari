package com.ghostchu.quickshop.compatibility.craftengine;

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

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.display.PacketFactory;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CraftEnginePacketFactory
 *
 * <p>Wraps the packet factory of the real packet library QuickShop picked (PacketEvents or
 * ProtocolLib) and delegates every packet to it, except {@link #createMetaDataPacket(int, ItemStack)}:
 * the display item metadata packet cannot be cached once anymore, it has to be built per receiving
 * player, so that the CraftEngine client bound item can be applied for that player.</p>
 *
 * <p>{@link #createMetaDataPacket(int, ItemStack)} therefore returns a plain placeholder which carries
 * the server side item stack, the actual packet is built (and the item converted) inside
 * {@link #sendPacket(Player, Object)}.</p>
 *
 * @author YuanYuanOwO
 * @since 6.3.0.3
 */
final class CraftEnginePacketFactory implements PacketFactory<Object> {

  /**
   * The wrapped packet factories which currently have their chunk listeners registered.
   *
   * <p>QuickShop rebuilds its display manager when the configuration is reloaded, which wraps the very
   * same packet factory again. Registering the chunk listeners twice would leak a listener per reload,
   * so registration is guarded here. A still registered listener of an older manager is harmless,
   * because it always resolves the current display manager through
   * {@code VirtualDisplayItemManager.instance()}.</p>
   */
  private static final Map<PacketFactory<?>, Boolean> CHUNK_LISTENERS = new ConcurrentHashMap<>();

  private final PacketFactory<Object> delegate;
  private final CraftEngineItemConverter converter;

  @SuppressWarnings("unchecked")
  CraftEnginePacketFactory(final PacketFactory<?> delegate, final CraftEngineItemConverter converter) {

    this.delegate = (PacketFactory<Object>) delegate;
    this.converter = converter;
  }

  @Override
  public Object createSpawnPacket(final int id, final Location displayLocation) {

    return this.delegate.createSpawnPacket(id, displayLocation);
  }

  @Override
  public Object createMetaDataPacket(final int id, final ItemStack itemStack) {

    //Do not build the packet yet: the client bound item depends on the player which receives it
    return new MetaPacketHolder(id, itemStack);
  }

  @Override
  public Object createTextDisplaySpawnPacket(final int id, final Location location) {

    return this.delegate.createTextDisplaySpawnPacket(id, location);
  }

  @Override
  public Object createTextDisplayVisiblePacket(final int id, final Shop shop, final ItemStack itemStack) {

    //The text display only carries the shop text, the item is not written into the packet
    return this.delegate.createTextDisplayVisiblePacket(id, shop, itemStack);
  }

  @Override
  public Object createVelocityPacket(final int id) {

    return this.delegate.createVelocityPacket(id);
  }

  @Override
  public Object createDestroyPacket(final int id) {

    return this.delegate.createDestroyPacket(id);
  }

  @Override
  public boolean sendPacket(final Player player, final Object packet) {

    if(!(packet instanceof final MetaPacketHolder holder)) {
      //Everything which was not created by this factory is passed through untouched
      return this.delegate.sendPacket(player, packet);
    }
    final ItemStack serverSide = holder.itemStack();
    final ItemStack clientBound = this.converter.toClientItem(player, serverSide);
    if(clientBound == serverSide) {
      //No conversion happened, reuse the cached packet of the raw item
      return this.delegate.sendPacket(player, holder.serverSidePacket(this.delegate));
    }
    return this.delegate.sendPacket(player, this.delegate.createMetaDataPacket(holder.entityId(), clientBound));
  }

  @Override
  public void registerSendChunk() {

    if(CHUNK_LISTENERS.putIfAbsent(this.delegate, Boolean.TRUE) == null) {

      this.delegate.registerSendChunk();
    }
  }

  @Override
  public void unregisterSendChunk() {

    if(CHUNK_LISTENERS.remove(this.delegate) != null) {

      this.delegate.unregisterSendChunk();
    }
  }

  @Override
  public void registerUnloadChunk() {

    this.delegate.registerUnloadChunk();
  }

  @Override
  public void unregisterUnloadChunk() {

    this.delegate.unregisterUnloadChunk();
  }

  /**
   * Placeholder which carries the server side display item until the packet is sent to a player.
   */
  private static final class MetaPacketHolder {

    private final int entityId;
    private final ItemStack itemStack;
    private volatile Object serverSidePacket;

    MetaPacketHolder(final int entityId, final ItemStack itemStack) {

      this.entityId = entityId;
      this.itemStack = itemStack;
    }

    int entityId() {

      return this.entityId;
    }

    ItemStack itemStack() {

      return this.itemStack;
    }

    /**
     * The packet which is used when CraftEngine does not convert the item, built lazily and cached.
     *
     * @param delegate the wrapped packet factory
     *
     * @return the metadata packet holding the server side item
     */
    Object serverSidePacket(final PacketFactory<Object> delegate) {

      Object cached = this.serverSidePacket;
      if(cached == null) {

        cached = delegate.createMetaDataPacket(this.entityId, this.itemStack);
        this.serverSidePacket = cached;
      }
      return cached;
    }
  }
}
