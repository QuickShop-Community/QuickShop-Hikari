package com.ghostchu.quickshop.shop.display.virtual.packet.packetevents;

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

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
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
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUnloadChunk;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
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
public class PacketFactoryv1_21 implements PacketFactory<PacketWrapper<?>> {

  private PacketListenerCommon chunkSendingPacketAdapter;

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
    return new WrapperPlayServerSpawnEntity(id, identifier, SpigotConversionUtil.fromBukkitEntityType(type), SpigotConversionUtil.fromBukkitLocation(displayLocation), 0.0F, 0, Vector3d.zero());
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

    final List<EntityData<?>> data = new ArrayList<>();
    data.add(new EntityData<>(5, EntityDataTypes.BOOLEAN, true));
    data.add(new EntityData<>(8, EntityDataTypes.ITEMSTACK, SpigotConversionUtil.fromBukkitItemStack(itemStack)));

    if(QuickShop.getInstance().getDisplayManager().useItemName()) {

      data.add(new EntityData<>(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(Util.getItemStackName(itemStack))));
      data.add(new EntityData<>(3, EntityDataTypes.BOOLEAN, true));
    }

    return new WrapperPlayServerEntityMetadata(id, data);
  }

  public PacketWrapper<?> createTextDisplaySpawnPacket(final int id, @NotNull final Location location) {

    final UUID identifier = UUID.nameUUIDFromBytes(("SHOP_TEXT:" + id).getBytes(StandardCharsets.UTF_8));
    return new WrapperPlayServerSpawnEntity(id, identifier, SpigotConversionUtil.fromBukkitEntityType(EntityType.TEXT_DISPLAY), SpigotConversionUtil.fromBukkitLocation(location), 0.0F, 0, Vector3d.zero());
  }

  /**
   * Creates a name visibility packet for the given entity ID, item stack, and visibility state.
   *
   * @param id        the ID of the entity associated with the packet
   * @param itemStack the ItemStack to include in the packet, cannot be null
   *
   * @return the name visibility packet of type T
   */
  @Override
  public PacketWrapper<?> createTextDisplayVisiblePacket(final int id, @NotNull final Shop shop, @NotNull final ItemStack itemStack) {

    final List<EntityData<?>> data = new ArrayList<>();

    final int blockDistance = QuickShop.getInstance().getConfig().getInt("shop.text-display.range-blocks", 8);
    final Vector3f scaleVector = new Vector3f(QuickShop.getInstance().getConfig().getFloat("shop.text-display.scale.x", 1.0F), QuickShop.getInstance().getConfig().getFloat("shop.text-display.scale.y", 1.0F), QuickShop.getInstance().getConfig().getFloat("shop.text-display.scale.z", 1.0F));
    // Text Display text metadata
    data.add(new EntityData<>(12, EntityDataTypes.VECTOR3F, scaleVector));
    data.add(new EntityData<>(15, EntityDataTypes.BYTE, (byte)3));
    data.add(new EntityData<>(17, EntityDataTypes.FLOAT, blockDistance * 0.0125F));//0.0125 per block
    data.add(new EntityData<>(23, EntityDataTypes.ADV_COMPONENT, Util.getTextDisplay(shop, itemStack)));
    data.add(new EntityData<>(24, EntityDataTypes.INT, QuickShop.getInstance().getConfig().getInt("shop.text-display.line-width", 200)));
    data.add(new EntityData<>(25, EntityDataTypes.INT, QuickShop.getInstance().getConfig().getInt("shop.text-display.background-color", 1073741824)));
    data.add(new EntityData<>(26, EntityDataTypes.BYTE, QuickShop.getInstance().getConfig().getByte("shop.text-display.text-opacity", (byte)-1)));
    data.add(new EntityData<>(27, EntityDataTypes.BYTE, Util.createTextDisplayFlags()));

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

        final List<VirtualDisplayItem<?>> items = new ArrayList<>();
        VirtualDisplayItemManager.instance().chunksMapping().computeIfPresent(new SimpleShopChunk(player.getWorld().getName(), x, z), (chunkLoc, targetList)->{

          for(final VirtualDisplayItem<?> target : targetList.values()) {
            if(!target.isSpawned()) {

              continue;
            }
            if(target.isApplicableForPlayer(player)) { // TODO: Refactor with better way

              target.getPacketSenders().add(player.getUniqueId());
              items.add(target);
            }
          }
          return targetList;
        });

        for(final VirtualDisplayItem<?> target : items) {
          target.sendDestroyPacket(player);
          target.sendFakeItem(player);
        }
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

        final List<VirtualDisplayItem<?>> items = new ArrayList<>();
        VirtualDisplayItemManager.instance().chunksMapping().computeIfPresent(new SimpleShopChunk(player.getWorld().getName(), x, z), (chunkLoc, targetList)->{
          for(final VirtualDisplayItem<?> target : targetList.values()) {

            if(!target.isSpawned()) {

              continue;
            }
            items.add(target);
            target.getPacketSenders().remove(player.getUniqueId());
          }
          return targetList;
        });

        for(final VirtualDisplayItem<?> target : items) {
          target.sendDestroyPacket(player);
        }
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

  public PacketListenerCommon getChunkSendingPacketAdapter() {

    return this.chunkSendingPacketAdapter;
  }

  public PacketListenerCommon getChunkUnloadingPacketAdapter() {

    return this.chunkUnloadingPacketAdapter;
  }
}
