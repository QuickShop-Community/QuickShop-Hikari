package com.ghostchu.quickshop.shop.signhooker;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.GameVersion;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SignHooker {
    private final QuickShop PLUGIN;
    private final GameVersion VERSION = QuickShop.getInstance().getGameVersion();
    private final ProtocolManager PROTOCOL_MANAGER = ProtocolLibrary.getProtocolManager();

    private PacketAdapter chunkAdapter;

    private PacketAdapter signAdapter;

    public SignHooker(QuickShop plugin) {
        PLUGIN = plugin;
        registerListener();
    }

    public void registerListener() {
        chunkAdapter = new PacketAdapter(PLUGIN, ListenerPriority.HIGH, PacketType.Play.Server.MAP_CHUNK) {
            @Override
            public void onPacketSending(@NotNull PacketEvent event) {
                //is really full chunk data
                //In 1.17, this value was removed, so read safely
                Boolean boxedIsFull = event.getPacket().getBooleans().readSafely(0);
                boolean isFull = boxedIsFull == null || boxedIsFull;
                if (!isFull) {
                    return;
                }
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

                Map<Location, Shop> shops = PLUGIN.getShopManager().getShops(player.getWorld().getName(), x, z);
                if (shops == null) return;
                Bukkit.getScheduler().runTaskLater(PLUGIN, () -> shops.forEach((loc, shop) -> updatePerPlayerShopSign(player, loc, shop)), 2);
            }
        };

        signAdapter = new PacketAdapter(PLUGIN, ListenerPriority.HIGH, PacketType.Play.Server.TILE_ENTITY_DATA) {
            @Override
            public void onPacketSending(@NotNull PacketEvent event) {
                NbtBase<?> tField = event.getPacket().getNbtModifier().readSafely(0);
                // Server send standard sign NBT format for line1, line2, line3, line4 to client.
                // If we hook that packet and modify component text inside, we can dynamically change the text
                // that send to client and that will can prevent text blink.
                // TODO Location deserialize.
                Log.debug(tField.getName() + " => " + tField.getValue().toString());
            }
        };
        PROTOCOL_MANAGER.addPacketListener(chunkAdapter);
        Log.debug("SignHooker chunk adapter registered.");
        PROTOCOL_MANAGER.addPacketListener(signAdapter);
    }

    public void unload(){
        if(PROTOCOL_MANAGER == null)
            return;
        if(this.chunkAdapter != null){
            PROTOCOL_MANAGER.removePacketListener(chunkAdapter);
            PROTOCOL_MANAGER.removePacketListener(signAdapter);
        }
    }

    public void updatePerPlayerShopSign(Player player, Location location, Shop shop) {
        Util.ensureThread(false);
        if (!shop.isLoaded()) return;
        if (!Util.isLoaded(location)) return;
        Log.debug("Updating per-player packet sign: Player=" + player.getName() + ", Location=" + location + ", Shop=" + shop.getShopId());
        List<Component> lines = shop.getSignText(PLUGIN.getTextManager().findRelativeLanguages(player));
        Log.debug(lines.stream().map(component -> GsonComponentSerializer.gson().serialize(component)).toList().toString());
        for (Sign sign : shop.getSigns()) {
            Log.debug("Target block sign is " + sign.getLocation());
            PLUGIN.getPlatform().sendSignTextChange(player, sign, PLUGIN.getConfig().getBoolean("shop.sign-glowing"), lines);
        }
    }

    public void updatePerPlayerShopSignBroadcast(Location location, Shop shop) {
        Collection<Entity> nearbyPlayers = shop.getLocation().getWorld().getNearbyEntities(shop.getLocation(), PLUGIN.getServer().getViewDistance() * 16, shop.getLocation().getWorld().getMaxHeight(), PLUGIN.getServer().getViewDistance() * 16);
        for (Entity nearbyPlayer : nearbyPlayers) {
            if (nearbyPlayer instanceof Player player) {
                updatePerPlayerShopSign(player, location, shop);
            }
        }
    }
}
