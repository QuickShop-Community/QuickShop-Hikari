package com.ghostchu.quickshop.addon.dynmap;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.*;
import com.ghostchu.quickshop.api.localization.text.TextManager;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopType;
import com.ghostchu.quickshop.util.PackageUtil;
import com.ghostchu.quickshop.util.Util;
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
import org.dynmap.DynmapAPI;
import org.dynmap.markers.*;
import org.jetbrains.annotations.NotNull;

public final class Main extends JavaPlugin implements Listener {
    static Main instance;
    private QuickShop plugin;
    private DynmapAPI dynmapAPI;
    private MarkerAPI markerAPI;

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
        this.dynmapAPI = (DynmapAPI) Bukkit.getPluginManager().getPlugin("dynmap");
        this.markerAPI = this.dynmapAPI.getMarkerAPI();
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().runTaskLater(this, this::updateAllMarkers, 80);
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

    public MarkerSet getMarkerSet() {
        String id = PackageUtil.parsePackageProperly("marker-id").asString("quickshop-hikari-shops");
        MarkerSet markerSet = markerAPI.getMarkerSet(id);
        if (markerSet == null) {
            markerSet = markerAPI.createMarkerSet(id, plain(text().of("addon.dynmap.markerset-title").forLocale()), null, false);
        }
        markerSet.setHideByDefault(getConfig().getBoolean("display-by-default", true));
        return markerSet;
    }

    public MarkerIcon getShopMarkerIcon() {
        return markerAPI.getMarkerIcon(PackageUtil.parsePackageProperly("marker-icon").asString("chest"));
    }

    public void updateAllMarkers() {
        MarkerSet markerSet = getMarkerSet();
        markerSet.getMarkers().forEach(GenericMarker::deleteMarker);
        for (Shop shop : plugin.getShopManager().getAllShops()) {
            updateShopMarker(shop);
        }
    }

    public void updateShopMarker(Shop shop) {
        MarkerSet markerSet = getMarkerSet();
        String shopName = shop.getShopName();
        String posStr = String.format("%s %s, %s, %s", shop.getLocation().getWorld().getName(), shop.getLocation().getBlockX(), shop.getLocation().getBlockY(), shop.getLocation().getBlockZ());
        if (shopName == null) {
            shopName = posStr;
        }
        Marker marker = markerSet.findMarker("quickshop-hikari-shop-" + shop.getShopId());
        String markerName = plain(text().of("addon.dynmap.marker-name",
                shopName,
                plain(shop.ownerName()),
                plain(Util.getItemStackName(shop.getItem())),
                plugin.getShopManager().format(shop.getPrice(), shop),
                shop.getShopStackingAmount(),
                shop.getShopType() == ShopType.SELLING ? plain(text().of("shop-type.selling").forLocale()) : plain(text().of("shop-type.buying").forLocale()),
                shop.isUnlimited(),
                posStr
        ).forLocale());
        if (marker == null) {
            marker = markerSet.createMarker("quickshop-hikari-shop-" + shop.getShopId()
                    , markerName
                    , shop.getLocation().getWorld().getName(),
                    shop.getLocation().getX(),
                    shop.getLocation().getY(),
                    shop.getLocation().getZ(),
                    getShopMarkerIcon(), false);
        } else {
            marker.setLocation(shop.getLocation().getWorld().getName(),
                    shop.getLocation().getX(),
                    shop.getLocation().getY(),
                    shop.getLocation().getZ());
        }
        String desc = plain(text().of("addon.dynmap.marker-description",
                shopName,
                plain(shop.ownerName()),
                plain(Util.getItemStackName(shop.getItem())),
                plugin.getShopManager().format(shop.getPrice(), shop),
                shop.getShopStackingAmount(),
                shop.getShopType() == ShopType.SELLING ? plain(text().of("shop-type.selling").forLocale()) : plain(text().of("shop-type.buying").forLocale()),
                shop.isUnlimited(),
                posStr
        ).forLocale());
        marker.setDescription(desc.replace("\n", "<br/>"));
    }

}
