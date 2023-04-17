package com.ghostchu.quickshop.shop.display.virtual;

import com.comphenix.protocol.events.PacketContainer;
import com.ghostchu.quickshop.api.event.ShopDisplayItemSpawnEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopChunk;
import com.ghostchu.quickshop.api.shop.display.DisplayType;
import com.ghostchu.quickshop.shop.SimpleShopChunk;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import com.ghostchu.quickshop.shop.display.virtual.packetfactory.VirtualDisplayPacketFactory;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.simplereloadlib.Reloadable;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

public class VirtualDisplayItem extends AbstractDisplayItem implements Reloadable {
    private final int entityID;
    //The List which store packet sender
    private final Set<UUID> packetSenders = new ConcurrentSkipListSet<>();
    private final VirtualDisplayPacketFactory virtualDisplayPacketFactory;
    private final VirtualDisplayItemManager manager;
    private final PacketContainer fakeItemSpawnPacket;
    private final PacketContainer fakeItemMetaPacket;
    private final PacketContainer fakeItemVelocityPacket;
    private final PacketContainer fakeItemDestroyPacket;
    //cache chunk x and z
    private ShopChunk chunkLocation;
    //If packet initialized
    private boolean isSpawned = false;
    //packets

    VirtualDisplayItem(VirtualDisplayItemManager manager, VirtualDisplayPacketFactory packetFactory, Shop shop) {
        super(shop);
        this.entityID = manager.generateEntityId();
        this.manager = manager;
        this.virtualDisplayPacketFactory = packetFactory;
        this.fakeItemSpawnPacket = virtualDisplayPacketFactory.createFakeItemSpawnPacket(entityID, getDisplayLocation());
        this.fakeItemMetaPacket = virtualDisplayPacketFactory.createFakeItemMetaPacket(entityID, getOriginalItemStack().clone());
        this.fakeItemVelocityPacket = virtualDisplayPacketFactory.createFakeItemVelocityPacket(entityID);
        this.fakeItemDestroyPacket = virtualDisplayPacketFactory.createFakeItemDestroyPacket(entityID);
        load();
    }

    @Override
    public boolean checkDisplayIsMoved() {
        return false;
    }

    @Override
    public boolean checkDisplayNeedRegen() {
        return false;
    }

    @Override
    public boolean checkIsShopEntity(@NotNull Entity entity) {
        return false;
    }

    @Override
    public void fixDisplayMoved() {

    }

    @Override
    public void fixDisplayNeedRegen() {

    }

    @Override
    public @Nullable Entity getDisplay() {
        return null;
    }

    @Override
    public boolean isSpawned() {
        return isSpawned;
    }

    @Override
    public void remove() {
        if (isSpawned()) {
            sendPacketToAll(fakeItemDestroyPacket);
            unload();
            isSpawned = false;
        }
    }

    @Override
    public boolean removeDupe() {
        return false;
    }

    @Override
    public void respawn() {
        Util.ensureThread(false);
        remove();
        spawn();
    }

    @Override
    public void safeGuard(@Nullable Entity entity) {

    }

    @Override
    public void spawn() {
        Util.ensureThread(false);
        if (isSpawned || shop.isDeleted() || !shop.isLoaded()) {
            return;
        }
        if (new ShopDisplayItemSpawnEvent(shop, originalItemStack, DisplayType.VIRTUALITEM).callCancellableEvent()) {
            Log.debug(
                    "Canceled the displayItem spawning because a plugin setCancelled the spawning event, usually this is a QuickShop Add on");
            return;
        }
        load();

        // Can't rely on the attachedShop cache to be accurate
        // So just try it and if it fails, no biggie
        /*try {
            shop.getAttachedShop().updateAttachedShop();
        } catch (NullPointerException ignored) {
        }*/

        sendFakeItemToAll();
        isSpawned = true;
    }


    //Due to the delay task in ChunkListener
    //We must move load task to first spawn to prevent some bug and make the check lesser
    private void load() {
        Util.ensureThread(false);
        //some time shop can be loaded when world isn't loaded
        Chunk chunk = shop.getLocation().getChunk();
        chunkLocation = new SimpleShopChunk(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
        manager.put(chunkLocation, this);
        if (Util.isLoaded(shop.getLocation())) {
            //Let nearby player can saw fake item
            Collection<Entity> entityCollection = shop.getLocation().getWorld().getNearbyEntities(shop.getLocation(), Bukkit.getViewDistance() * 16, shop.getLocation().getWorld().getMaxHeight(), Bukkit.getViewDistance() * 16);
            for (Entity entity : entityCollection) {
                if (entity instanceof Player) {
                    packetSenders.add(entity.getUniqueId());
                }
            }
        }
    }

    public void sendFakeItem(@NotNull Player player) {
        sendPacket(player, fakeItemSpawnPacket);
        sendPacket(player, fakeItemMetaPacket);
        sendPacket(player, fakeItemVelocityPacket);
    }

    public void sendDestroyItem(@NotNull Player player) {
        sendPacket(player, fakeItemDestroyPacket);
    }

    private void sendPacket(@NotNull Player player, @NotNull PacketContainer packet) {
        manager.getProtocolManager().sendServerPacket(player, packet);
    }

    public void sendFakeItemToAll() {
        sendPacketToAll(fakeItemSpawnPacket);
        sendPacketToAll(fakeItemMetaPacket);
        sendPacketToAll(fakeItemVelocityPacket);
    }

    private void sendPacketToAll(@NotNull PacketContainer packet) {
        Iterator<UUID> iterator = packetSenders.iterator();
        while (iterator.hasNext()) {
            Player nextPlayer = Bukkit.getPlayer(iterator.next());
            if (nextPlayer == null) {
                iterator.remove();
            } else {
                sendPacket(nextPlayer, packet);
            }
        }
    }

    private void unload() {
        packetSenders.clear();
        manager.remove(chunkLocation, this);
    }

    @NotNull
    public Set<UUID> getPacketSenders() {
        return packetSenders;
    }
}
