package com.ghostchu.quickshop.util.config;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.Util;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

@SuppressWarnings("unused")
public class ConfigUpdateScript {
    private final QuickShop plugin;
    @Getter
    private final FileConfiguration config;

    public ConfigUpdateScript(@NotNull FileConfiguration config, @NotNull QuickShop plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    @UpdateScript(version = 1024)
    public void displayVirtualStatusReset() {
        getConfig().set("shop.display-type", 2);
    }
    @UpdateScript(version = 1025)
    public void disableCSMByDefault() {
        getConfig().set("bungee-cross-server-msg", false);
    }

    @UpdateScript(version = 1023)
    public void allowPublicKeyRetrieve() {
        if (!getConfig().isSet("database.properties.allowPublicKeyRetrieval")) {
            getConfig().set("database.properties.allowPublicKeyRetrieval", true);
        }
    }

    @UpdateScript(version = 1022)
    public void privacySystem() {
        getConfig().set("shop.use-cache", true);
        getConfig().set("use-caching", null);
        getConfig().set("disabled-metrics", null);
        getConfig().set("privacy.type.STATISTIC", true);
        getConfig().set("privacy.type.RESEARCH", true);
        getConfig().set("privacy.type.DIAGNOSTIC", true);
    }

    @UpdateScript(version = 1021)
    public void cacheSystemReworked() {
        getConfig().set("shop.use-cache", true);
        getConfig().set("use-caching", null);
    }

    @UpdateScript(version = 1020)
    public void protectionCheckMonitorListeners() {
        getConfig().set("shop.cancel-protection-fake-event-before-reach-monitor-listeners", true);
    }


    @UpdateScript(version = 1019)
    public void oldConfigCleanup() {
        getConfig().set("plugin.OpenInv", null);
        getConfig().set("plugin.LWC", null);
        getConfig().set("plugin.BlockHub", null);
        getConfig().set("plugin.NBTAPI", null);
    }

    @UpdateScript(version = 1018)
    public void tweakBackupPolicy() {
        getConfig().set("purge.backup", null);
        getConfig().set("backup-policy.shops-auto-purge", false);
        getConfig().set("backup-policy.database-upgrade", true);
        getConfig().set("backup-policy.startup", false);
        getConfig().set("backup-policy.recovery", true);
    }

    @UpdateScript(version = 1017)
    public void addQsToCommands() {
        List<String> getAlias = getConfig().getStringList("custom-commands");
        getAlias.add("qs");
        getConfig().set("custom-commands", getAlias);
    }

    @UpdateScript(version = 1016)
    public void disableDefaultShopCorruptDeletion() {
        getConfig().set("debug.delete-corrupt-shops", false);
    }

    @UpdateScript(version = 1015)
    public void removePurgerRefund() {
        getConfig().set("purge.return-create-fee", null);
        getConfig().set("shop.async-owner-name-fetch", null);
    }

    @UpdateScript(version = 1014)
    public void removeDisplayCenterConfig() {
        getConfig().set("shop.display-center", null);
    }

    @UpdateScript(version = 1013)
    public void asyncOwnerNameFetch() {
        getConfig().set("shop.async-owner-name-fetch", false);
    }

    @UpdateScript(version = 1012)
    public void displayCenterControl() {
        getConfig().set("shop.display-center", false);
    }

    @UpdateScript(version = 1011)
    public void removeTryingFixBanlanceInsuffientFeature() {
        getConfig().set("trying-fix-banlance-insuffient", null);
    }

    @UpdateScript(version = 1010)
    public void allowDisableQsSizeCommandMaxStackSizeCheck() {
        getConfig().set("shop.disable-max-size-check-for-size-command", false);
    }

    @UpdateScript(version = 1009)
    public void deleteSqlitePlayerMapping() {
        File f = new File(Util.getCacheFolder(), "player_mapping.db");
        if (f.exists()) {
            f.delete();
        }
    }

    @UpdateScript(version = 1008)
    public void disableTaxForUnlimitedShopAndPerPlayerSignShop() {
        getConfig().set("shop.per-player-shop-sign", false);
        getConfig().set("tax-free-for-unlimited-shop", false);
    }


    @UpdateScript(version = 1007)
    public void refundFromTaxAccountOption() {
        getConfig().set("shop.refund-from-tax-account", false);
    }
}
