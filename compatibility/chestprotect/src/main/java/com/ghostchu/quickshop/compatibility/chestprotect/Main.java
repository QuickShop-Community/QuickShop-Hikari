//package com.ghostchu.quickshop.compatibility.chestprotect;
//
//import com.ghostchu.quickshop.api.event.ShopAuthorizeCalculateEvent;
//import com.ghostchu.quickshop.api.event.ShopCreateEvent;
//import com.ghostchu.quickshop.api.event.ShopPreCreateEvent;
//import com.ghostchu.quickshop.api.shop.Shop;
//import com.ghostchu.quickshop.compatibility.CompatibilityModule;
//import me.angeschossen.chestprotect.api.addons.ChestProtectAddon;
//import me.angeschossen.chestprotect.api.events.ProtectionChunkAddedEvent;
//import me.angeschossen.chestprotect.api.objects.BlockProtection;
//import org.bukkit.Bukkit;
//import org.bukkit.Location;
//import org.bukkit.World;
//import org.bukkit.event.EventHandler;
//import org.bukkit.plugin.Plugin;
//
//import java.util.Map;
//
//public final class Main extends CompatibilityModule {
//    private ChestProtectAddon chestProtectApi;
//
//    @Override
//    public void init() {
//        Plugin resPlug = getServer().getPluginManager().getPlugin("ChestProtect");
//        if (resPlug == null) {
//            getLogger().info("Dependency not found: ChestProtect");
//            Bukkit.getPluginManager().disablePlugin(this);
//            return;
//        }
//        chestProtectApi = new ChestProtectAddon(this);
//    }
//
//    @EventHandler(ignoreCancelled = true)
//    public void onCreation(ShopCreateEvent event) {
//        Location shopLoc = event.getShop().getLocation();
//        BlockProtection protection = chestProtectApi.getProtection(shopLoc);
//        if (protection == null) return;
//        if (protection.isTrusted(event.getCreator())) return;
//        event.setCancelled(true, getApi().getTextManager().of(event.getCreator(), "addon.chestprotect.protection-exists").forLocale());
//    }
//
//
//    @EventHandler(ignoreCancelled = true)
//    public void onPreCreation(ShopPreCreateEvent event) {
//        Location shopLoc = event.getLocation();
//        BlockProtection protection = chestProtectApi.getProtection(shopLoc);
//        if (protection == null) return;
//        if (protection.isTrusted(event.getPlayer().getUniqueId())) return;
//        event.setCancelled(true, getApi().getTextManager().of(event.getPlayer().getUniqueId(), "addon.chestprotect.protection-exists").forLocale());
//    }
//
//    @EventHandler(ignoreCancelled = true)
//    public void permissionOverride(ShopAuthorizeCalculateEvent event) {
//        if (!getConfig().getBoolean("allow-permission-override")) {
//            return;
//        }
//        BlockProtection protection = chestProtectApi.getProtection(event.getShop().getLocation());
//        if (protection == null) return;
//        if(protection.getOwnerUUID().equals(event.getAuthorizer())) {
//            event.setResult(true);
//        }
//    }
//
//    @EventHandler(ignoreCancelled = true)
//    public void protectionChunkAdding(ProtectionChunkAddedEvent event) {
//        World world = event.getProtectionChunk().getProtectionWorld().getWorld();
//        int x = event.getProtectionChunk().getX();
//        int z = event.getProtectionChunk().getZ();
//        Map<Location, Shop> shops = getApi().getShopManager().getShops(world.getName(),x,z);
//        if(shops == null || shops.isEmpty()) return;
//        for (Shop shop : shops.values()) {
//           BlockProtection blockProtection = chestProtectApi.getProtection(shop.getLocation());
//           if(blockProtection == null) continue;
//           if(blockProtection.getOwnerUUID().equals(shop.getOwner())) continue;
//        }
//
//
//}
