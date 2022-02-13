package com.ghostchu.quickshop.compatibility.worldedit;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Worldedit extends JavaPlugin implements Listener {
    private WorldEditAdapter adapter;
    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("QuickShop Compatibility Module - WorldEdit loaded");
        WorldEditPlugin worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        adapter = new WorldEditAdapter(worldEditPlugin);
        Bukkit.getPluginManager().registerEvents(adapter,this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        adapter.unregister();
    }
}
