package com.ghostchu.quickshop.shop.display.virtual.packetfactory;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.shop.SimpleShopChunk;
import com.ghostchu.quickshop.shop.display.virtual.VirtualDisplayItem;
import com.ghostchu.quickshop.shop.display.virtual.VirtualDisplayItemManager;
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public class v1_19_R1 implements VirtualDisplayPacketFactory {
    private final QuickShop plugin;
    private final VirtualDisplayItemManager manager;

    public v1_19_R1(QuickShop plugin, VirtualDisplayItemManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public @Nullable Throwable testFakeItem() {
        try {
            createFakeItemSpawnPacket(0, new Location(Bukkit.getServer().getWorlds().get(0), 0, 0, 0));
            createFakeItemMetaPacket(0, new ItemStack(Material.values()[0]));
            createFakeItemVelocityPacket(0);
            createFakeItemDestroyPacket(0);
            return null;
        } catch (Exception throwable) {
            return throwable;
        }
    }

    @Override
    public @NotNull PacketContainer createFakeItemSpawnPacket(int entityID, @NotNull Location displayLocation) {
        //First, create a new packet to spawn item
        PacketContainer fakeItemPacket = manager.getProtocolManager().createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        //and add data based on packet class in NMS  (global scope variable)
        //Reference: https://wiki.vg/Protocol#Spawn_Object
        fakeItemPacket.getIntegers()
                //Entity ID
                .write(0, entityID);
        fakeItemPacket.getEntityTypeModifier().write(0, EntityType.DROPPED_ITEM);
        //UUID
        fakeItemPacket.getUUIDs().write(0, UUID.randomUUID());
        //Location
        fakeItemPacket.getDoubles()
                //X
                .write(0, displayLocation.getX())
                //Y
                .write(1, displayLocation.getY())
                //Z
                .write(2, displayLocation.getZ());
        return fakeItemPacket;
    }

    @Override
    public @NotNull PacketContainer createFakeItemMetaPacket(int entityID, @NotNull ItemStack itemStack) {
        //Next, create a new packet to update item data (default is empty)
        PacketContainer fakeItemMetaPacket = manager.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        //Entity ID
        fakeItemMetaPacket.getIntegers().write(0, entityID);

        //List<DataWatcher$Item> Type are more complex
        //Create a DataWatcher
        WrappedDataWatcher wpw = new WrappedDataWatcher();
        //https://wiki.vg/index.php?title=Entity_metadata#Entity
        if (plugin.getConfig().getBoolean("shop.display-item-use-name")) {
            String itemName = GsonComponentSerializer.gson().serialize(Util.getItemStackName(itemStack));
            wpw.setObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true), Optional.of(WrappedChatComponent.fromJson(itemName).getHandle()));
            wpw.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class)), true);
        }

        //Must in the certain slot:https://wiki.vg/Entity_metadata#Item
        //1.17+ is 8
        wpw.setObject(8, WrappedDataWatcher.Registry.getItemStackSerializer(false), itemStack);
        //Add it
        fakeItemMetaPacket.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());
        return fakeItemMetaPacket;
    }

    @Override
    public @NotNull PacketContainer createFakeItemVelocityPacket(int entityID) {
        //And, create a entity velocity packet to make it at a proper location (otherwise it will fly randomly)
        PacketContainer fakeItemVelocityPacket = manager.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_VELOCITY);
        fakeItemVelocityPacket.getIntegers()
                //Entity ID
                .write(0, entityID)
                //Velocity x
                .write(1, 0)
                //Velocity y
                .write(2, 0)
                //Velocity z
                .write(3, 0);
        return fakeItemVelocityPacket;
    }

    @Override
    public @NotNull PacketContainer createFakeItemDestroyPacket(int entityID) {
        //Also make a DestroyPacket to remove it
        PacketContainer fakeItemDestroyPacket = manager.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        //On 1.17.1 (may be 1.17.1+? it's enough, Mojang, stop the changes), we need add the int list
        //Entity to remove
        try {
            fakeItemDestroyPacket.getIntLists().write(0, Collections.singletonList(entityID));
        } catch (NoSuchMethodError e) {
            throw new IllegalStateException("Unable to initialize packet, ProtocolLib update needed", e);
        }
        // }
        return fakeItemDestroyPacket;
    }

    @Override
    public @NotNull PacketAdapter getChunkSendPacketAdapter() {
        return new PacketAdapter(plugin.getJavaPlugin(), ListenerPriority.HIGH, PacketType.Play.Server.MAP_CHUNK) {
            @Override
            public void onPacketSending(@NotNull PacketEvent event) {
                Player player = event.getPlayer();
                if (player == null || !player.isOnline()) {
                    return;
                }
                if (player.getClass().getName().contains("TemporaryPlayer")) {
                    return;
                }
                StructureModifier<Integer> integerStructureModifier = event.getPacket().getIntegers();
                //chunk x
                int x = integerStructureModifier.read(0);
                //chunk z
                int z = integerStructureModifier.read(1);

                manager.getChunksMapping().computeIfPresent(new SimpleShopChunk(player.getWorld().getName(), x, z), (chunkLoc, targetList) -> {
                    for (VirtualDisplayItem target : targetList) {
                        if (!target.isSpawned()) {
                            continue;
                        }
                        target.getPacketSenders().add(player.getUniqueId());
                        target.sendDestroyItem(player);
                        target.sendFakeItem(player);
                    }
                    return targetList;
                });
            }
        };
    }

    @Override
    public @NotNull PacketAdapter getChunkUnloadPacketAdapter() {
        return new PacketAdapter(plugin.getJavaPlugin(), ListenerPriority.HIGH, PacketType.Play.Server.UNLOAD_CHUNK) {
            @Override
            public void onPacketSending(@NotNull PacketEvent event) {
                Player player = event.getPlayer();
                if (player == null || !player.isOnline()) {
                    return;
                }
                if (player.getClass().getName().contains("TemporaryPlayer")) {
                    return;
                }
                StructureModifier<Integer> integerStructureModifier = event.getPacket().getIntegers();
                //chunk x
                int x = integerStructureModifier.read(0);
                //chunk z
                int z = integerStructureModifier.read(1);

                manager.getChunksMapping().computeIfPresent(new SimpleShopChunk(player.getWorld().getName(), x, z), (chunkLoc, targetList) -> {
                    for (VirtualDisplayItem target : targetList) {
                        if (!target.isSpawned()) {
                            continue;
                        }
                        target.sendDestroyItem(player);
                        target.getPacketSenders().remove(player.getUniqueId());
                    }
                    return targetList;
                });
            }
        };
    }
}
