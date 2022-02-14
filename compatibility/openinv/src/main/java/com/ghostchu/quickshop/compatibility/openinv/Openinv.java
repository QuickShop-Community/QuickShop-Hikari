package com.ghostchu.quickshop.compatibility.openinv;

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.command.CommandContainer;
import com.lishid.openinv.IOpenInv;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Openinv extends JavaPlugin implements Listener {
    public IOpenInv openInv;
    public OpenInvInventoryManager manager;
    private QuickShopAPI api;

    @Override
    public void onLoad() {
        saveDefaultConfig();
        this.api = (QuickShopAPI) Bukkit.getPluginManager().getPlugin("QuickShop-Hikari");
        openInv = (IOpenInv) Bukkit.getPluginManager().getPlugin("OpenInv");
        manager = new OpenInvInventoryManager(openInv, this);
        this.api.getInventoryWrapperRegistry().register(this, manager);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.api.getCommandManager().registerCmd(CommandContainer.builder().prefix("echest").permission("quickshop.echest").description(LegacyComponentSerializer.legacySection().deserialize(getConfig().getString("messages.description"))).executor(new OpenInvCommand(this)).build());
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public QuickShopAPI getApi() {
        return api;
    }

    public IOpenInv getOpenInv() {
        return openInv;
    }

    public OpenInvInventoryManager getManager() {
        return manager;
    }
}
