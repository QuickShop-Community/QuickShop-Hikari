package com.ghostchu.quickshop.compatibility.nocheatplus;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.maxgamer.quickshop.api.event.ShopProtectionCheckEvent;

public final class Main extends JavaPlugin implements Listener {
    private final BiMap<Event, Block> biMap = HashBiMap.create();
    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this,this);
        getLogger().info("QuickShop Compatibility Module - NoCheatPlus loaded");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    @EventHandler(ignoreCancelled = true)
    public void onFakeEventBegin(ShopProtectionCheckEvent event){
        biMap.put(event.getEvent(),event.getLocation().getBlock());
    }



}
