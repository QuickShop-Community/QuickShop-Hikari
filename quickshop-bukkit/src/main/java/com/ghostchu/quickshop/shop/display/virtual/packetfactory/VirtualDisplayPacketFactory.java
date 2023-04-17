package com.ghostchu.quickshop.shop.display.virtual.packetfactory;

import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface VirtualDisplayPacketFactory {
    @Nullable
    Throwable testFakeItem();

    @NotNull
    PacketContainer createFakeItemSpawnPacket(int entityID, @NotNull Location displayLocation);

    @NotNull
    PacketContainer createFakeItemMetaPacket(int entityID, @NotNull ItemStack itemStack);

    @NotNull
    PacketContainer createFakeItemVelocityPacket(int entityID);

    @NotNull
    PacketContainer createFakeItemDestroyPacket(int entityID);

    @NotNull PacketAdapter getChunkSendPacketAdapter();

    @NotNull PacketAdapter getChunkUnloadPacketAdapter();
}
