package com.ghostchu.quickshop.compatibility.worldedit;

import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public final class Main extends CompatibilityModule implements Listener {
    private WorldEditAdapter adapter;

    @Override
    public void onDisable() {
        adapter.unregister();
        super.onDisable();
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        WorldEditPlugin worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        adapter = new WorldEditAdapter(worldEditPlugin);
        Bukkit.getPluginManager().registerEvents(adapter, this);
        super.onEnable();
    }

    @Override
    public void init() {
        // There no init stuffs need to do
    }

}
