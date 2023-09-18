package com.ghostchu.quickshop.addon.reremakemigrator.migratecomponent;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.reremakemigrator.Main;
import com.ghostchu.quickshop.util.ProgressMonitor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

public class ConfigMigrate extends AbstractMigrateComponent {
    // tax-free-for-unlimited-shop
    //
    private final String[] DIRECT_COPY_KEYS = new String[]{
            "game-language", "enabled-languages", "mojangapi-mirror", "dev-mode", "tax", "tax-account", "unlimited-shop-owner-change",
            "unlimited-shop-owner-change-account", "show-tax", "respect-item-flag", "currency", "logging", "trying-fix-banlance-insuffient",
            "include-offlineplayer-list", "economy-type", "use-decimal-format", "decimal-format", "send-display-item-protection-alert",
            "send-shop-protection-alert", "chat-type", "limits", "shop-block", "shop.cost", "shop.refund",
            "shop.sending-stock-message-to-staffs", "shop.disable-creative-mode-trading", "shop.disable-super-tool",
            "shop.allow-owner-break-shop-sign", "shop.price-change-requires-fee", "shop.fee-for-price-change", "shop.lock",
            "shop.disable-quick-create", "shop.auto-sign", "shop.sign-glowing", "shop.sign-dye-color", "shop.pay-unlimited-shop-owners",
            "shop.display-items", "shop.display-items-check-ticks", "shop.display-type", "shop.display-auto-despawn",
            "shop.display-despawn-range", "shop.display-check-time", "shop.display-allow-stacks", "shop.finding", "shop.alternate-currency-symbol",
            "shop.alternate-currency-symbol-list", "shop.disable-vault-format", "shop.currency-symbol-on-right", "shop.ignore-unlimited-shop-messages",
            "shop.auto-fetch-shop-messages", "shop.ignore-cancel-chat-event", "shop.allow-shop-without-space-for-sign",
            "shop.maximum-digits-in-price", "shop.show-owner-uuid-in-controlpanel-if-op", "shop.sign-material",
            "shop.use-enchantment-for-enchanted-book", "shop.use-effect-for-potion-item", "shop.blacklist-world",
            "shop.blacklist-lores", "shop.protection-checking", "shop.protection-checking-blacklist", "shop.protection-checking-listener-blacklist",
            "shop.max-shops-checks-in-once", "shop.display-item-use-name", "shop.update-sign-when-inventory-moving", "shop.allow-economy-loan",
            "shop.word-for-trade-all-items", "shop.ongoing-fee", "shop.force-load-downgrade-items", "shop.remove-protection-trigger",
            "shop.allow-stacks", "shop.force-use-item-original-name", "blacklist", "plugin.Multiverse-Core", "plugin.WorldEdit",
            "effect", "matcher", "protect.explode", "protect.hopper", "protect.entity", "custom-item-stacksize", "purge", "debug"
    };
    private final CommandSender sender;

    public ConfigMigrate(Main main, QuickShop hikari, org.maxgamer.quickshop.QuickShop reremake, CommandSender sender) {
        super(main, hikari, reremake);
        this.sender = sender;
    }

    @Override
    public boolean migrate() {
        copyValues();
        migrateCommandAlias();
        migratePriceRestrictions();
        setFixedConfigValues();
        return true;
    }

    private void migrateCommandAlias() {
        List<String> commandAlias = getReremake().getConfig().getStringList("custom-commands");
        commandAlias.add("qs");
        getHikari().getConfig().set("custom-commands", commandAlias);
        getHikariJavaPlugin().saveConfig();
    }

    private void setFixedConfigValues() {
        getHikari().getConfig().set("legacy-updater.shop-sign", true);
    }

    private void migratePriceRestrictions() {
        // Generating price-restriction.yml, copy from SimplePriceLimiter.java
        getHikari().text().of(sender, "addon.reremake-migrator.modules.config.migrate-price-restriction").send();
        File configFile = new File(getHikariJavaPlugin().getDataFolder(), "price-restriction.yml");
        if (!configFile.exists()) {
            try {
                Files.copy(getHikariJavaPlugin().getResource("price-restriction.yml"), configFile.toPath());
            } catch (IOException e) {
                getHikari().logger().warn("Failed to copy price-restriction.yml.yml to plugin folder!", e);
            }
        }
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(configFile);
        // migrate the minimum-price and maximum-price
        configuration.set("undefined.min", getReremake().getConfig().get("shop.minimum-price", "0.01"));
        configuration.set("undefined.max", getReremake().getConfig().get("shop.maximum-price", "999999999999999999999999999999.99"));
        configuration.set("enable", true);
        // migrate the whole-number setting from Reremake configuration
        configuration.set("whole-number-only", getReremake().getConfig().get("shop.whole-number-prices-only"));
        List<String> str = getReremake().getConfig().getStringList("shop.price-restriction");
        for (String record : new ProgressMonitor<>(str, (triple) -> getHikari().text().of(sender, "addon.reremake-migrator.modules.migrate-price-restriction-entry", triple.getLeft(), triple.getMiddle()).send())) {
            try {
                String[] records = record.split(";");
                if (records.length != 3) continue;
                Material material = Material.matchMaterial(records[0]);
                double min = Double.parseDouble(records[1]);
                double max = Double.parseDouble(records[2]);
                String name = UUID.randomUUID().toString().replace("-", "");
                if (material == null) continue;
                configuration.set("rules." + name + ".items", List.of(material));
                configuration.set("rules." + name + ".currency", List.of("*"));
                configuration.set("rules." + name + ".min", min);
                configuration.set("rules." + name + ".max", max);
            } catch (Exception e) {
                getHikari().logger().warn("Failed to migrate rule {}", record, e);
            }
        }
        try {
            configuration.save(configFile);
        } catch (IOException e) {
            getHikari().logger().warn("Failed to save the price-restriction.yml", e);
        }
    }


    private void copyValues() {
        getHikari().text().of(sender, "addon.reremake-migrator.modules.config.copy-values", DIRECT_COPY_KEYS.length).send();
        for (String directCopyKey : new ProgressMonitor<>(DIRECT_COPY_KEYS, triple ->
                getHikari().text().of(sender, "addon.reremake-migrator.modules.config.copying-value",
                        triple.getRight(), triple.getLeft(), triple.getMiddle()).send())) {
            copyValue(directCopyKey);
        }
        getHikariJavaPlugin().saveConfig();
    }

    private void copyValue(String keyName) {
        getHikari().getConfig().set(keyName, getReremake().getConfig().get(keyName));
    }
}
