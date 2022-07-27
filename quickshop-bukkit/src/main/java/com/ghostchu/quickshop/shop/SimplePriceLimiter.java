package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.PriceLimiter;
import com.ghostchu.quickshop.api.shop.PriceLimiterCheckResult;
import com.ghostchu.quickshop.api.shop.PriceLimiterStatus;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.Reloadable;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SimplePriceLimiter implements Reloadable, PriceLimiter {
    private final QuickShop plugin;
    private boolean wholeNumberOnly = false;
    private double undefinedMin = 0.0d;
    private double undefinedMax = Double.MAX_VALUE;
    private final Map<String, RuleSet> rules = new LinkedHashMap<>();

    public SimplePriceLimiter(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        loadConfiguration();
        plugin.getReloadManager().register(this);
    }

    /**
     * Check the price restriction rules
     *
     * @param sender    the sender
     * @param itemStack the item to check
     * @param currency  the currency
     * @param price     the price
     * @return the result
     */
    /*
    Use item stack to reserve the extent ability
     */
    @Override
    @NotNull
    public PriceLimiterCheckResult check(@NotNull CommandSender sender, @NotNull ItemStack itemStack, @Nullable String currency, double price) {
        if (Double.isInfinite(price) || Double.isNaN(price)) {
            return new SimplePriceLimiterCheckResult(PriceLimiterStatus.NOT_VALID, undefinedMin, undefinedMax);
        }
        if (wholeNumberOnly) {
            try {
                BigDecimal.valueOf(price).setScale(0, RoundingMode.UNNECESSARY);
            } catch (ArithmeticException exception) {
                Log.debug(exception.getMessage());
                return new SimplePriceLimiterCheckResult(PriceLimiterStatus.NOT_A_WHOLE_NUMBER, undefinedMin, undefinedMax);
            }
        }
        for (RuleSet rule : rules.values()) {
            if (!rule.isApply(sender, itemStack, currency)) {
                continue;
            }
            if (rule.isAllowed(price)) {
                continue;
            }
            return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PRICE_RESTRICTED, rule.getMin(), rule.getMax());
        }
        if (undefinedMin != -1 && price < undefinedMin) {
            return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PRICE_RESTRICTED, undefinedMin, undefinedMax);
        }
        if (undefinedMax != -1 && price > undefinedMax) {
            return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PRICE_RESTRICTED, undefinedMin, undefinedMax);
        }
        return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PASS, undefinedMin, undefinedMax);
    }

    public void loadConfiguration() {
        this.rules.clear();
        File configFile = new File(plugin.getDataFolder(), "price-restriction.yml");
        if (!configFile.exists()) {
            try {
                //noinspection ConstantConditions
                Files.copy(plugin.getResource("price-restriction.yml"), configFile.toPath());
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to copy price-restriction.yml.yml to plugin folder!", e);
            }
        }

        FileConfiguration configuration = YamlConfiguration.loadConfiguration(configFile);
        if (performMigrate(configuration)) {
            try {
                configuration.save(configFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to save migrated  price-restriction.yml.yml to plugin folder!", e);
            }
        }
        this.undefinedMax = configuration.getDouble("undefined.max", Double.MAX_VALUE);
        this.undefinedMin = configuration.getDouble("undefined.min", 0.0d);
        this.wholeNumberOnly = configuration.getBoolean("whole-number-only", false);
        if (!configuration.getBoolean("enable", false)) {
            return;
        }
        ConfigurationSection rules = configuration.getConfigurationSection("rules");
        if (rules == null) {
            plugin.getLogger().warning("Failed to read price-restriction.yml, syntax invalid!");
            return;
        }
        for (String ruleName : rules.getKeys(false)) {
            RuleSet rule = readRule(ruleName, rules.getConfigurationSection(ruleName));
            if (rule == null) {
                plugin.getLogger().warning("Failed to read rule " + ruleName + ", syntax invalid! Skipping...");
                continue;
            }
            this.rules.put(ruleName, rule);
        }
        plugin.getLogger().info("Loaded " + this.rules.size() + " price restriction rules!");
    }

    private boolean performMigrate(@NotNull FileConfiguration configuration) {
        boolean anyChanges = false;
        if (configuration.getInt("version", 1) == 1) {
            Log.debug("Migrating price-restriction.yml from version 1 to version 2");
            ConfigurationSection rules = configuration.getConfigurationSection("rules");
            if (rules != null) {
                for (String ruleName : rules.getKeys(false)) {
                    ConfigurationSection rule = rules.getConfigurationSection(ruleName);
                    if (rule != null) {
                        Log.debug("Migrating: Structure upgrading for rule " + ruleName);
                        rule.set("items", rule.getStringList("materials"));
                        rule.set("materials", null);
                    }
                }
            }
            configuration.set("version", 2);
            anyChanges = true;
        }
        return anyChanges;
    }

    @Nullable
    @Contract("_,null -> null")
    private RuleSet readRule(@NotNull String ruleName, @Nullable ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        String bypassPermission = "quickshop.price.restriction.bypass." + ruleName;
        List<Function<ItemStack, Boolean>> items = new ArrayList<>();
        double min = section.getDouble("min", 0d);
        double max = section.getDouble("max", Double.MAX_VALUE);
        for (String item : section.getStringList("items")) {
            if (item.startsWith("@")) {
                String reference = item.substring(1);
                ItemStack stack = plugin.getItemMarker().get(reference);
                items.add(itemStack -> plugin.getItemMatcher().matches(stack, itemStack));
            } else {
                Material mat = Material.matchMaterial(item);
                if (mat == null) {
                    plugin.getLogger().warning("Failed to read rule " + ruleName + "'s a ItemRule option, invalid item " + item + "! Skipping...");
                    continue;
                }
                items.add(itemStack -> itemStack.getType() == mat);
            }
        }
        List<Pattern> currency = new ArrayList<>();
        for (String currencyStr1 : section.getStringList("currency")) {
            try {
                Pattern pattern = Pattern.compile(currencyStr1);
                currency.add(pattern);
            } catch (PatternSyntaxException e) {
                plugin.getLogger().warning("Failed to read rule " + ruleName + "'s a Currency option, invalid pattern " + currencyStr1 + "! Skipping...");
            }
        }
        return new RuleSet(items, bypassPermission, currency, min, max);
    }

    @Override
    public ReloadResult reloadModule() throws Exception {
        loadConfiguration();
        return Reloadable.super.reloadModule();
    }

    @AllArgsConstructor
    @Data
    static class RuleSet {
        private final List<Function<ItemStack, Boolean>> items;
        private final String bypassPermission;
        private final List<Pattern> currency;
        private final double min;
        private final double max;

        /**
         * Check if the rule is allowed to apply to the given price.
         *
         * @param sender   the sender
         * @param item     the item
         * @param currency the currency
         * @return true if the rule is allowed to apply
         */
        public boolean isApply(@NotNull CommandSender sender, @NotNull ItemStack item, @Nullable String currency) {
            if (QuickShop.getPermissionManager().hasPermission(sender, this.bypassPermission)) {
                return false;
            }
            if (currency != null) {
                if (this.currency.stream().noneMatch(pattern -> pattern.matcher(currency).matches()))
                    return false;
            }
            for (Function<ItemStack, Boolean> fun : items) {
                if (fun.apply(item)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Check if the rule is allowed to apply to the given price.
         *
         * @param price the price
         * @return true if the rule is allowed for given price
         */
        public boolean isAllowed(double price) {
            if (this.max != -1 && price > this.max) {
                return false;
            }
            if (this.min != -1) {
                return price >= this.min;
            }
            return true;
        }
    }
}
