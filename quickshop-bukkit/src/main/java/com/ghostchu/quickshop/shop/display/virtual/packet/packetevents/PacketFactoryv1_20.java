package com.ghostchu.quickshop.shop.display.virtual.packet.packetevents;
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

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.display.PacketFactory;
import com.ghostchu.quickshop.shop.SimpleShopChunk;
import com.ghostchu.quickshop.shop.display.virtual.VirtualDisplayItem;
import com.ghostchu.quickshop.shop.display.virtual.VirtualDisplayItemManager;
import com.ghostchu.quickshop.shop.display.virtual.packet.PacketEventsHandler;
import com.ghostchu.quickshop.util.Util;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUnloadChunk;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PacketFactoryv1_21
 *
 * @author creatorfromhell
 * @since 6.2.0.9
 */
public class PacketFactoryv1_20 implements PacketFactory<PacketWrapper<?>> {

  @Getter
  private PacketListenerCommon chunkSendingPacketAdapter;

  @Getter
  private PacketListenerCommon chunkUnloadingPacketAdapter;

  /**
   * Creates a spawn packet for the specified ID and display location.
   *
   * @param id              the ID of the packet to be created
   * @param displayLocation the display location where the packet will be spawned
   *
   * @return the spawn packet of type T
   */
  @Override
  public PacketWrapper<?> createSpawnPacket(final int id, @NotNull final Location displayLocation) {

    final EntityType type = EntityType.ITEM;
    final UUID identifier = UUID.nameUUIDFromBytes(("SHOP:" + id).getBytes(StandardCharsets.UTF_8));

    return new WrapperPlayServerSpawnEntity(id, identifier, SpigotConversionUtil.fromBukkitEntityType(type),
                                            SpigotConversionUtil.fromBukkitLocation(displayLocation),
                                            0F, 0, null);
  }

  /**
   * Creates a metadata packet with the specified ID and ItemStack.
   *
   * @param id        the ID of the metadata packet to be created
   * @param itemStack the ItemStack to include in the metadata packet
   *
   * @return the metadata packet of type T
   */
  @Override
  public PacketWrapper<?> createMetaDataPacket(final int id, @NotNull final ItemStack itemStack) {
    final List<EntityData> data = new ArrayList<>();
    data.add(new EntityData(5, EntityDataTypes.BOOLEAN, true));
    data.add(new EntityData(8, EntityDataTypes.ITEMSTACK, SpigotConversionUtil.fromBukkitItemStack(itemStack)));

    if(QuickShop.getInstance().getConfig().getBoolean("shop.display-item-use-name")) {

      final String itemName = GsonComponentSerializer.gson().serialize(Util.getItemStackName(itemStack));

      data.add(new EntityData(2, EntityDataTypes.OPTIONAL_COMPONENT, Optional.of(itemName)));
      data.add(new EntityData(3, EntityDataTypes.BOOLEAN, true));
    }

    return new WrapperPlayServerEntityMetadata(id, data);
  }

  /**
   * Creates a velocity packet with the specified ID.
   *
   * @param id the ID of the velocity packet to be created
   *
   * @return the velocity packet of type T
   */
  @Override
  public PacketWrapper<?> createVelocityPacket(final int id) {

    return null;
  }

  /**
   * Creates a destroy packet for the given ID.
   *
   * @param id the ID of the packet to be destroyed
   *
   * @return the destroy packet of type T
   */
  @Override
  public PacketWrapper<?> createDestroyPacket(final int id) {

    return new WrapperPlayServerDestroyEntities(id);
  }

  /**
   * Sends the specified packet to the given player.
   *
   * @param player the player to receive the packet, cannot be null
   * @param packet the packet of type T to be sent, cannot be null
   *
   * @return true if the packet was successfully sent, false otherwise
   */
  @Override
  public boolean sendPacket(@NotNull final Player player, @NotNull final PacketWrapper<?> packet) {

    PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    return true;
  }

  /**
   * Registers the method to listen to the packet sending chunk data.
   */
  @Override
  public void registerSendChunk() {

    final PacketListener sendChunk = new PacketListener() {
      @Override
      public void onPacketSend(final PacketSendEvent event) {

        //Identify what kind of packet it is.
        if(event.getPacketType() != PacketType.Play.Server.CHUNK_DATA) {

          return;
        }

        final WrapperPlayServerChunkData chunkData = new WrapperPlayServerChunkData(event);

        final Player player = event.getPlayer();
        if(player == null || !player.isOnline()) {

          return;
        }

        if(player.getClass().getName().contains("TemporaryPlayer")) {

          return;
        }

        final int x = chunkData.getColumn().getX();
        final int z = chunkData.getColumn().getZ();

        VirtualDisplayItemManager.instance().getChunksMapping().computeIfPresent(new SimpleShopChunk(player.getWorld().getName(), x, z), (chunkLoc, targetList)->{

          for(final VirtualDisplayItem<?> target : targetList) {
            if(!target.isSpawned()) {

              continue;
            }
            if(target.isApplicableForPlayer(player)) { // TODO: Refactor with better way

              target.getPacketSenders().add(player.getUniqueId());
              target.sendDestroyPacket(player);
              target.sendFakeItem(player);
            }
          }
          return targetList;
        });
      }
    };

    PacketEventsHandler.instance().internal().getEventManager().registerListener(sendChunk, PacketListenerPriority.NORMAL);
  }

  /**
   * Unregisters the method to listen to the packet sending chunk data.
   */
  @Override
  public void unregisterSendChunk() {

    if(chunkSendingPacketAdapter != null) {

      PacketEventsHandler.instance().internal().getEventManager().unregisterListener(chunkSendingPacketAdapter);
    }
  }

  /**
   * Registers the method to listen to the packet sending the unloading of a chunk.
   */
  @Override
  public void registerUnloadChunk() {

    final PacketListener chunkUnlock = new PacketListener() {

      @Override
      public void onPacketSend(final PacketSendEvent event) {

        //Identify what kind of packet it is.
        if(event.getPacketType() != PacketType.Play.Server.UNLOAD_CHUNK) {

          return;
        }

        final WrapperPlayServerUnloadChunk unloadChunk = new WrapperPlayServerUnloadChunk(event);

        final Player player = event.getPlayer();
        if(player == null || !player.isOnline()) {
          return;
        }
        if(player.getClass().getName().contains("TemporaryPlayer")) {
          return;
        }

        final int x = unloadChunk.getChunkX();
        final int z = unloadChunk.getChunkZ();

        VirtualDisplayItemManager.instance().getChunksMapping().computeIfPresent(new SimpleShopChunk(player.getWorld().getName(), x, z), (chunkLoc, targetList)->{
          for(final VirtualDisplayItem<?> target : targetList) {

            if(!target.isSpawned()) {

              continue;
            }
            target.sendDestroyPacket(player);
            target.getPacketSenders().remove(player.getUniqueId());
          }
          return targetList;
        });
      }
    };

    PacketEventsHandler.instance().internal().getEventManager().registerListener(chunkUnlock, PacketListenerPriority.NORMAL);
  }

  /**
   * Unregisters the method to listen to the packet sending the unloading of a chunk.
   */
  @Override
  public void unregisterUnloadChunk() {

    if(chunkUnloadingPacketAdapter != null) {

      PacketEventsHandler.instance().internal().getEventManager().unregisterListener(chunkUnloadingPacketAdapter);
    }
  }
}
