package com.ghostchu.quickshop.addon.reremakemigrator.migratecomponent;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.QuickShopBukkit;
import com.ghostchu.quickshop.addon.reremakemigrator.Main;

public abstract class AbstractMigrateComponent implements MigrateComponent {
    private final Main plugin;
    private final QuickShop hikari;
    private final org.maxgamer.quickshop.QuickShop reremake;

    public AbstractMigrateComponent(Main main, QuickShop hikari, org.maxgamer.quickshop.QuickShop reremake) {
        this.plugin = main;
        this.hikari = hikari;
        this.reremake = reremake;
    }

    @Override
    public QuickShop getHikari() {
        return this.hikari;
    }

    @Override
    public QuickShopBukkit getHikariJavaPlugin() {
        return this.hikari.getJavaPlugin();
    }

    @Override
    public org.maxgamer.quickshop.QuickShop getReremake() {
        return this.reremake;
    }

    @Override
    public Main getPlugin() {
        return plugin;
    }
}
