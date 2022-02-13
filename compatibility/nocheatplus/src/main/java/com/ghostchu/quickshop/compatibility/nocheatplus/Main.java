package com.ghostchu.quickshop.compatibility.nocheatplus;

import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import com.ghostchu.quickshop.api.event.ProtectionCheckStatus;
import com.ghostchu.quickshop.api.event.ShopProtectionCheckEvent;

public final class Main extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("QuickShop Compatibility Module - NoCheatPlus loaded");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler(ignoreCancelled = true)
    public void onFakeEventBegin(ShopProtectionCheckEvent event) {
        if (event.getStatus() == ProtectionCheckStatus.BEGIN)
            NCPExemptionManager.exemptPermanently(event.getPlayer().getUniqueId());
        else if(event.getStatus() == ProtectionCheckStatus.END)
            NCPExemptionManager.unexempt(event.getPlayer().getUniqueId());
    }
}
