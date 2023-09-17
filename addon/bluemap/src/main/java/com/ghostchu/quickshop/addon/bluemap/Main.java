package com.ghostchu.quickshop.addon.bluemap;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.localization.text.TextManager;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopType;
import com.ghostchu.quickshop.util.Util;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class Main extends JavaPlugin implements Listener {
    static Main instance;
    private QuickShop plugin;
    private BlueMapAPI blueMapAPI;
    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Plugin) this);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        plugin = QuickShop.getInstance();
        getLogger().info("Registering the per shop permissions...");
            BlueMapAPI.onEnable(blueMapAPI -> {
                getLogger().info("Found BlueMap loaded! Hooking!");
                createMarkerSet();
                Bukkit.getScheduler().runTaskTimerAsynchronously(this, ()->{
                    updateAllMarkers();
                }, 1, getConfig().getInt("refresh-per-seconds") * 20L);
            });

        BlueMapAPI.onDisable(api -> Bukkit.getScheduler().cancelTasks(this));
        }

    @NotNull
    public String plain(@NotNull Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @NotNull
    public TextManager text() {
        return plugin.getTextManager();
    }

    public MarkerSet createMarkerSet(){
        return MarkerSet.builder()
                .defaultHidden(true)
                .label(plain(text().of("addon.bluemap.markerset-title").forLocale()))
                .defaultHidden(getConfig().getBoolean("display-by-default"))
                .toggleable(true)
                .build();
    }
    private void updateAllMarkers() {
        blueMapAPI.getWorlds().forEach(bWorld-> bWorld.getMaps().forEach(bMap-> bMap.getMarkerSets().remove("quickshop-hikari-shops")));
        plugin.getShopManager().getAllShops().forEach(this::updateShopMarker);
    }

    public void updateShopMarker(Shop shop) {
        Optional<BlueMapWorld> bWorld = blueMapAPI.getWorld(shop.getLocation().getWorld());
        if(bWorld.isEmpty()) return;
        String shopName = shop.getShopName();
        String posStr = String.format("%s %s, %s, %s", shop.getLocation().getWorld().getName(), shop.getLocation().getBlockX(), shop.getLocation().getBlockY(), shop.getLocation().getBlockZ());
        if (shopName == null) {
            shopName = posStr;
        }
        for (BlueMapMap map : bWorld.get().getMaps()) {
            MarkerSet markerSet = map.getMarkerSets().computeIfAbsent("quickshop-hikari-shops",(key)-> createMarkerSet());
            String markerName = fillPlaceholders(getConfig().getString("marker-label"), shop);
            String desc = fillPlaceholders(getConfig().getString("marker-detail"), shop);
            POIMarker marker = POIMarker.builder()
                    .label(markerName)
                    .position(shop.getLocation().getX(),
                            shop.getLocation().getY(),
                            shop.getLocation().getZ())
                    .maxDistance(getConfig().getDouble("max-distance"))
                    .detail(desc)
                    .styleClasses()
                    .build();
            markerSet.getMarkers().put("quickshop-hikari-shop"+shop.getShopId(), marker);
        }
    }

    private String fillPlaceholders(String s, Shop shop){
        Location loc = shop.getLocation();
        String x = String.valueOf(loc.getX());
        String y = String.valueOf(loc.getY());
        String z = String.valueOf(loc.getZ());
        s = s.replace("%owner%", plain(shop.ownerName()));
        s = s.replace("%item%", shop.getItem().getType().name());
        s = s.replace("%price%", String.valueOf(shop.getPrice()));
        s = s.replace("%stock%", String.valueOf(shop.getRemainingStock()));
        s = s.replace("%type%", shop.getShopType().name());
        s = s.replace("%location%", x + "," + y + "," + z );
        return s;
    }
}
