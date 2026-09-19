package com.ghostchu.quickshop.shop.display.virtual.packet.protocollib;

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

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.display.PacketFactory;
import com.ghostchu.quickshop.shop.SimpleShopChunk;
import com.ghostchu.quickshop.shop.display.virtual.VirtualDisplayItem;
import com.ghostchu.quickshop.shop.display.virtual.VirtualDisplayItemManager;
import com.ghostchu.quickshop.shop.display.virtual.packet.ProtocolLibHandler;
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PacketFactoryv26_3
 *
 * @author YuanYuanOwO
 * @since 6.3.0.3
 */
public class PacketFactoryv26_3 implements PacketFactory<PacketContainer> {

  private static final WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.getItemStackSerializer(false);

  private PacketAdapter chunkSendingPacketAdapter;

  private PacketAdapter chunkUnloadingPacketAdapter;

  @Override
  public PacketContainer createSpawnPacket(final int id, @NotNull final Location displayLocation) {

    final UUID identifier = UUID.nameUUIDFromBytes(("SHOP:" + id).getBytes(StandardCharsets.UTF_8));
    final PacketContainer fakeItemPacket = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);

    fakeItemPacket.getIntegers().write(0, id);
    fakeItemPacket.getUUIDs().write(0, identifier);
    fakeItemPacket.getEntityTypeModifier().write(0, EntityType.ITEM);
    fakeItemPacket.getDoubles().write(0, displayLocation.getX());
    fakeItemPacket.getDoubles().write(1, displayLocation.getY());
    fakeItemPacket.getDoubles().write(2, displayLocation.getZ());
    fakeItemPacket.getVectors().write(0, new Vector(0, 0, 0));
    return fakeItemPacket;
  }

  @Override
  public PacketContainer createMetaDataPacket(final int id, @NotNull final ItemStack itemStack) {

    final List<WrappedDataValue> values = new ArrayList<>();
    values.add(new WrappedDataValue(5, WrappedDataWatcher.Registry.get((Type)Boolean.class), true));
    values.add(new WrappedDataValue(8, serializer, MinecraftReflection.getMinecraftItemStack(itemStack)));

    if(QuickShop.getInstance().getDisplayManager().useItemName()) {
      final String itemName = GsonComponentSerializer.gson().serialize(Util.getItemStackName(itemStack));
      values.add(new WrappedDataValue(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true), Optional.of(WrappedChatComponent.fromJson(itemName).getHandle())));
      values.add(new WrappedDataValue(3, WrappedDataWatcher.Registry.get((Type)Boolean.class), true));
    }

    final PacketContainer fakeItemMetaPacket = ProtocolLibHandler.instance().internal().createPacket(PacketType.Play.Server.ENTITY_METADATA);
    fakeItemMetaPacket.getIntegers().write(0, id);
    fakeItemMetaPacket.getDataValueCollectionModifier().write(0, values);
    try {
      Class.forName("com.comphenix.protocol.wrappers.WrappedDataValue");
    } catch(final ClassNotFoundException e) {
      throw new RuntimeException("Unable to initialize packet, ProtocolLib update needed", e);
    }
    return fakeItemMetaPacket;
  }

  @Override
  public PacketContainer createTextDisplaySpawnPacket(final int id, @NotNull final Location location) {

    final UUID identifier = UUID.nameUUIDFromBytes(("SHOP_TEXT:" + id).getBytes(StandardCharsets.UTF_8));
    final PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);

    packet.getIntegers().write(0, id);
    packet.getUUIDs().write(0, identifier);
    packet.getEntityTypeModifier().write(0, EntityType.TEXT_DISPLAY);
    packet.getDoubles().write(0, location.getX());
    packet.getDoubles().write(1, location.getY());
    packet.getDoubles().write(2, location.getZ());
    packet.getVectors().write(0, new Vector(0, 0, 0));
    return packet;
  }

  @Override
  public PacketContainer createTextDisplayVisiblePacket(final int id, @NotNull final Shop shop, @NotNull final ItemStack itemStack) {

    final PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
    packet.getIntegers().write(0, id);
    final int blockDistance = QuickShop.getInstance().getConfig().getInt("shop.text-display.range-blocks", 8);
    final Vector3f scaleVector = new Vector3f(QuickShop.getInstance().getConfig().getFloat("shop.text-display.scale.x", 1.0F), QuickShop.getInstance().getConfig().getFloat("shop.text-display.scale.y", 1.0F), QuickShop.getInstance().getConfig().getFloat("shop.text-display.scale.z", 1.0F));
    final WrappedChatComponent component = WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(Util.getTextDisplay(shop, itemStack)));

    final List<WrappedDataValue> data = new ArrayList<>();
    data.add(new WrappedDataValue(15, WrappedDataWatcher.Registry.get((Type)Byte.class), (byte)3));
    data.add(new WrappedDataValue(17, WrappedDataWatcher.Registry.get((Type)Float.class), blockDistance * 0.0125f));
    data.add(new WrappedDataValue(23, WrappedDataWatcher.Registry.getChatComponentSerializer(), component.getHandle()));
    data.add(new WrappedDataValue(24, WrappedDataWatcher.Registry.get((Type)Integer.class), QuickShop.getInstance().getConfig().getInt("shop.text-display.line-width", 200)));
    data.add(new WrappedDataValue(25, WrappedDataWatcher.Registry.get((Type)Integer.class), QuickShop.getInstance().getConfig().getInt("shop.text-display.background-color", 1073741824)));
    data.add(new WrappedDataValue(26, WrappedDataWatcher.Registry.get((Type)Byte.class), QuickShop.getInstance().getConfig().getByte("shop.text-display.text-opacity", (byte)-1)));
    data.add(new WrappedDataValue(27, WrappedDataWatcher.Registry.get((Type)Byte.class), Util.createTextDisplayFlags()));
    packet.getDataValueCollectionModifier().write(0, data);
    return packet;
  }

  @Override
  public PacketContainer createVelocityPacket(final int id) {

    return null;
  }

  @Override
  public PacketContainer createDestroyPacket(final int id) {

    final PacketContainer fakeItemDestroyPacket = ProtocolLibHandler.instance().internal().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
    try {
      fakeItemDestroyPacket.getIntLists().write(0, Collections.singletonList(id));
    } catch(final NoSuchMethodError e) {
      throw new IllegalStateException("Unable to initialize packet, ProtocolLib update needed", e);
    }
    return fakeItemDestroyPacket;
  }

  @Override
  public boolean sendPacket(@NotNull final Player player, @NotNull final PacketContainer packet) {

    ProtocolLibHandler.instance().internal().sendServerPacket(player, packet);
    return true;
  }

  @Override
  public void registerSendChunk() {

    this.chunkSendingPacketAdapter = createChunkPacketAdapter();
    ProtocolLibHandler.instance().internal().addPacketListener(chunkSendingPacketAdapter);
  }

  @Override
  public void unregisterSendChunk() {

    if(chunkSendingPacketAdapter != null) {
      ProtocolLibHandler.instance().internal().removePacketListener(chunkSendingPacketAdapter);
    }
  }

  @Override
  public void registerUnloadChunk() {

    this.chunkUnloadingPacketAdapter = createUnloadChunkPacketAdapter();
    ProtocolLibHandler.instance().internal().addPacketListener(chunkUnloadingPacketAdapter);
  }

  @Override
  public void unregisterUnloadChunk() {

    if(chunkUnloadingPacketAdapter != null) {
      ProtocolLibHandler.instance().internal().removePacketListener(chunkUnloadingPacketAdapter);
    }
  }

  private PacketAdapter createChunkPacketAdapter() {

    return new PacketAdapter(QuickShop.getInstance().getJavaPlugin(), ListenerPriority.HIGH, PacketType.Play.Server.MAP_CHUNK) {
      @Override
      public void onPacketSending(@NotNull final PacketEvent event) {

        final Player player = event.getPlayer();
        if(player == null || !player.isOnline() || player.getClass().getName().contains("TemporaryPlayer")) {
          return;
        }
        final ChunkCoordIntPair pair = event.getPacket().getChunkCoordIntPairs().read(0);
        VirtualDisplayItemManager.instance().chunksMapping().computeIfPresent(new SimpleShopChunk(player.getWorld().getName(), pair.getChunkX(), pair.getChunkZ()), (chunkLoc, targetList)->{
          for(final VirtualDisplayItem<?> target : targetList.values()) {
            if(target.isSpawned() && target.isApplicableForPlayer(player)) {
              target.getPacketSenders().add(player.getUniqueId());
              target.sendDestroyPacket(player);
              target.sendFakeItem(player);
            }
          }
          return targetList;
        });
      }
    };
  }

  private PacketAdapter createUnloadChunkPacketAdapter() {

    return new PacketAdapter(QuickShop.getInstance().getJavaPlugin(), ListenerPriority.HIGH, PacketType.Play.Server.UNLOAD_CHUNK) {
      @Override
      public void onPacketSending(@NotNull final PacketEvent event) {

        final Player player = event.getPlayer();
        if(player == null || !player.isOnline() || player.getClass().getName().contains("TemporaryPlayer")) {
          return;
        }
        final ChunkCoordIntPair pair = event.getPacket().getChunkCoordIntPairs().read(0);
        VirtualDisplayItemManager.instance().chunksMapping().computeIfPresent(new SimpleShopChunk(player.getWorld().getName(), pair.getChunkX(), pair.getChunkZ()), (chunkLoc, targetList)->{
          for(final VirtualDisplayItem<?> target : targetList.values()) {
            if(target.isSpawned()) {
              target.sendDestroyPacket(player);
              target.getPacketSenders().remove(player.getUniqueId());
            }
          }
          return targetList;
        });
      }
    };
  }

  public PacketAdapter getChunkSendingPacketAdapter() {

    return this.chunkSendingPacketAdapter;
  }

  public PacketAdapter getChunkUnloadingPacketAdapter() {

    return this.chunkUnloadingPacketAdapter;
  }
}