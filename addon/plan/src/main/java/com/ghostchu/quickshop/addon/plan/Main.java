package com.ghostchu.quickshop.addon.plan;

import com.ghostchu.quickshop.QuickShop;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements Listener {
    private QuickShop quickshop;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        quickshop = QuickShop.getInstance();
        new PlanHook(this).hookIntoPlan();
    }

    public QuickShop getQuickShop() {
        return quickshop;
    }
}
