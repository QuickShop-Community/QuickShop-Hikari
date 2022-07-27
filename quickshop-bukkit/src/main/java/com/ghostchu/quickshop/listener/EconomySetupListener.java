package com.ghostchu.quickshop.listener;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.PluginEnableEvent;

public class EconomySetupListener extends AbstractQSListener {
    public EconomySetupListener(QuickShop plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (plugin.getEconomy() == null) {
            plugin.loadEcon();
        }
    }

    /**
     * Callback for reloading
     *
     * @return Reloading success
     */
    @Override
    public ReloadResult reloadModule() {
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }
}
