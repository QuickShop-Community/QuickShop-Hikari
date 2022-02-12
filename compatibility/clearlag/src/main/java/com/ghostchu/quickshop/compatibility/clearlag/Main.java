package com.ghostchu.quickshop.compatibility.clearlag;

import me.minebuilders.clearlag.events.EntityRemoveEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.maxgamer.quickshop.api.shop.AbstractDisplayItem;

public final class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this,this);
        getLogger().info("QuickShop Compatibility Module - Clearlag loaded");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler(ignoreCancelled = true)
    public void onRemove(EntityRemoveEvent event){
        for (Entity entity : event.getEntityList()) {
            if(entity instanceof Item item){
                if(AbstractDisplayItem.checkIsGuardItemStack(item.getItemStack())){
                    event.removeEntity(entity);
                }
            }
        }
    }
}
