package com.ghostchu.quickshop.util.paste.item;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.ReflectFactory;
import com.ghostchu.quickshop.util.paste.util.HTMLTable;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class ServerInfoItem implements SubPasteItem {
    private final String serverName;
    private final String build;
    private final String nmsVersion;
    private final String dataVersion;
    private final String players;
    private final String onlineMode;
    private final String bukkitVersion;
    private final String mcVersion;
    private final String worldContainer;
    private String moddedServerType;
    private String platform;

    public ServerInfoItem() {
        QuickShop plugin = QuickShop.getInstance();
        this.serverName = Bukkit.getServer().getName();
        this.build = Bukkit.getServer().getVersion();
        this.nmsVersion = ReflectFactory.getNMSVersion();
        this.dataVersion = String.valueOf(Bukkit.getServer().getUnsafe().getDataVersion());
        this.moddedServerType = "Bukkit";
        if (PaperLib.isSpigot()) {
            this.moddedServerType = "Spigot";
        }
        if (PaperLib.isPaper()) {
            this.moddedServerType = "Paper";
        }
        if (plugin.getEnvironmentChecker().isFabricBasedServer()) {
            this.moddedServerType = "Fabric";
        }
        if (plugin.getEnvironmentChecker().isForgeBasedServer()) {
            this.moddedServerType = "Forge";
        }
        this.players = Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers();
        this.onlineMode = CommonUtil.boolean2Status(Bukkit.getOnlineMode());
        this.bukkitVersion = Bukkit.getServer().getVersion();
        this.mcVersion = plugin.getPlatform().getMinecraftVersion();
        this.worldContainer = Bukkit.getWorldContainer().getPath();
        this.platform = plugin.getPlatform().getClass().getName();
    }

    @Override
    public @NotNull String genBody() {
        return buildContent();
    }

    @Override
    public @NotNull String getTitle() {
        return "Server Information";
    }

    @NotNull
    private String buildContent() {
        HTMLTable table = new HTMLTable(2, true);
        table.insert("Server Name", serverName);
        table.insert("Build", build);
        table.insert("NMS Version", nmsVersion);
        table.insert("Data Version", dataVersion);
        table.insert("Modded Server Type", moddedServerType);
        table.insert("Players", players);
        table.insert("Online Mode", onlineMode);
        table.insert("Bukkit Version", bukkitVersion);
        table.insert("MC Version", mcVersion);
        table.insert("World Container", worldContainer);
        table.insert("Platform", platform);
        return table.render();
    }
}
