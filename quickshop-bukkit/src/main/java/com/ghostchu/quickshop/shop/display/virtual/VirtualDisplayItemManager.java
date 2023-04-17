package com.ghostchu.quickshop.shop.display.virtual;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopChunk;
import com.ghostchu.quickshop.shop.display.virtual.packetfactory.VirtualDisplayPacketFactory;
import com.ghostchu.quickshop.shop.display.virtual.packetfactory.v1_18;
import com.ghostchu.quickshop.shop.display.virtual.packetfactory.v1_19_R1;
import com.ghostchu.quickshop.shop.display.virtual.packetfactory.v1_19_R2_UP;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class VirtualDisplayItemManager {
    private final QuickShop plugin;
    private final AtomicInteger entityIdCounter;
    private final ProtocolManager protocolManager;
    @Getter
    private final Map<ShopChunk, List<VirtualDisplayItem>> chunksMapping = new ConcurrentHashMap<>();
    @Getter
    private VirtualDisplayPacketFactory packetFactory;
    @Getter
    private PacketAdapter chunkSendingPacketAdapter;
    @Getter
    private PacketAdapter chunkUnloadingPacketAdapter;
    private boolean testPassed = true;

    public VirtualDisplayItemManager(QuickShop plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.entityIdCounter = new AtomicInteger(Integer.MAX_VALUE);
        load();
    }

    public void load() {
        String stringClassLoader = protocolManager.getClass().getClassLoader().toString();
        if (stringClassLoader.contains("pluginEnabled=true") && !stringClassLoader.contains("plugin=ProtocolLib")) {
            plugin.logger().warn("Warning! ProtocolLib seems provided by another plugin, This seems to be a wrong packaging problem, " +
                    "QuickShop can't ensure the ProtocolLib is working correctly! Info: {}", stringClassLoader);
        }
        Log.debug("Loading VirtualDisplayItem chunks mapping manager...");
        Log.debug("Load: PacketFactory...");

        this.packetFactory = switch (plugin.getGameVersion()) {
            case v1_18_R1, v1_18_R2 -> new v1_18(plugin, this);
            case v1_19_R1 -> new v1_19_R1(plugin, this);
            case v1_19_R2, v1_19_R3 -> new v1_19_R2_UP(plugin, this);
            default ->
                    throw new IllegalStateException("Unsupported Virtual Display Minecraft version: " + plugin.getGameVersion());
        };
        this.chunkSendingPacketAdapter = packetFactory.getChunkSendPacketAdapter();
        this.chunkUnloadingPacketAdapter = packetFactory.getChunkUnloadPacketAdapter();
        Log.debug("Registering the packet listener...");
        protocolManager.addPacketListener(chunkSendingPacketAdapter);
        protocolManager.addPacketListener(chunkUnloadingPacketAdapter);
    }

    public void put(@NotNull ShopChunk key, @NotNull VirtualDisplayItem value) {
        //Thread-safe was ensured by ONLY USE Map method to do something
        List<VirtualDisplayItem> virtualDisplayItems = new ArrayList<>(Collections.singletonList(value));
        chunksMapping.merge(key, virtualDisplayItems, (mapOldVal, mapNewVal) -> {
            mapOldVal.addAll(mapNewVal);
            return mapOldVal;
        });
    }

    public void remove(@NotNull ShopChunk key, @NotNull VirtualDisplayItem value) {
        chunksMapping.computeIfPresent(key, (mapOldKey, mapOldVal) -> {
            mapOldVal.remove(value);
            return mapOldVal;
        });
    }

    public void unload() {
        Log.debug("Unregistering the packet listener...");
        protocolManager.removePacketListener(chunkSendingPacketAdapter);
    }

    public int generateEntityId() {
        return entityIdCounter.getAndDecrement();
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    @NotNull
    public VirtualDisplayItem createVirtualDisplayItem(@NotNull Shop shop) {
        return new VirtualDisplayItem(this, packetFactory, shop);
    }

    public void setTestPassed(boolean testPassed) {
        this.testPassed = testPassed;
    }

    public boolean isTestPassed() {
        return testPassed;
    }
}
