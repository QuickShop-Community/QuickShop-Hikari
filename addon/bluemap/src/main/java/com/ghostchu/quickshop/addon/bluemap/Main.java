package com.ghostchu.quickshop.addon.bluemap;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.*;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
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
        Bukkit.getScheduler().runTaskAsynchronously(this,()->{
            while(BlueMapAPI.getInstance().isEmpty()){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            getLogger().info("Found Pl3xMap loaded! Hooking!");
            blueMapAPI = BlueMapAPI.getInstance().orElseThrow();
            Bukkit.getPluginManager().registerEvents(this, this);
            Bukkit.getScheduler().runTaskLater(this, this::updateAllMarkers, 80);
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onEvent(WorldLoadEvent event){
        Util.mainThreadRun(this::updateAllMarkers);
    }
    @EventHandler(ignoreCancelled = true)
    public void onEvent(WorldUnloadEvent event){
        Util.mainThreadRun(this::updateAllMarkers);
    }
    @EventHandler(ignoreCancelled = true)
    public void onEvent(QSConfigurationReloadEvent event){
        Util.mainThreadRun(this::updateAllMarkers);
    }
    @EventHandler(ignoreCancelled = true)
    public void onEvent(ShopCreateSuccessEvent event){
        updateShopMarker(event.getShop());
    }
    @EventHandler(ignoreCancelled = true)
    public void onEvent(ShopDeleteEvent event){
        Util.mainThreadRun(()->updateShopMarker(event.getShop()));
    }
    @EventHandler(ignoreCancelled = true)
    public void onEvent(ShopPriceChangeEvent event){
        Util.mainThreadRun(()->updateShopMarker(event.getShop()));
    }
    @EventHandler(ignoreCancelled = true)
    public void onEvent(ShopItemChangeEvent event){
        Util.mainThreadRun(()->updateShopMarker(event.getShop()));
    }
    @EventHandler(ignoreCancelled = true)
    public void onEvent(ShopOwnershipTransferEvent event){
        Util.mainThreadRun(()->updateShopMarker(event.getShop()));
    }
    @EventHandler(ignoreCancelled = true)
    public void onEvent(ShopSuccessPurchaseEvent event){
        Util.mainThreadRun(()->updateShopMarker(event.getShop()));
    }
    @EventHandler(ignoreCancelled = true)
    public void onEvent(ShopTypeChangeEvent event){
        Util.mainThreadRun(()->updateShopMarker(event.getShop()));
    }
    @EventHandler(ignoreCancelled = true)
    public void onEvent(ShopNamingEvent event){
        Util.mainThreadRun(()->updateShopMarker(event.getShop()));
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
            String markerName = plain(text().of("addon.bluemap.marker-name",
                    shopName,
                    plain(shop.ownerName()),
                    plain(Util.getItemStackName(shop.getItem())),
                    plugin.getShopManager().format(shop.getPrice(), shop),
                    shop.getShopStackingAmount(),
                    shop.getShopType() == ShopType.SELLING ? plain(text().of("shop-type.selling").forLocale()) : plain(text().of("shop-type.buying").forLocale()),
                    shop.isUnlimited(),
                    posStr
            ).forLocale());
            String desc = plain(text().of("addon.bluemap.marker-description",
                    shopName,
                    plain(shop.ownerName()),
                    plain(Util.getItemStackName(shop.getItem())),
                    plugin.getShopManager().format(shop.getPrice(), shop),
                    shop.getShopStackingAmount(),
                    shop.getShopType() == ShopType.SELLING ? plain(text().of("shop-type.selling").forLocale()) : plain(text().of("shop-type.buying").forLocale()),
                    shop.isUnlimited(),
                    posStr
            ).forLocale());
            POIMarker marker = POIMarker.builder()
                    .label(markerName)
                    .position(shop.getLocation().getX(),
                            shop.getLocation().getY(),
                            shop.getLocation().getZ())
                    .maxDistance(1000)
                    .detail(desc)
                    .styleClasses()
                    .build();
            markerSet.getMarkers().put("quickshop-hikari-shop"+shop.getShopId(), marker);
        }
    }

}
