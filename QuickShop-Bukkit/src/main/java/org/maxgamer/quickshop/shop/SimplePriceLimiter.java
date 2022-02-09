package org.maxgamer.quickshop.shop;

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
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.shop.PriceLimiter;
import org.maxgamer.quickshop.api.shop.PriceLimiterCheckResult;
import org.maxgamer.quickshop.api.shop.PriceLimiterStatus;
import org.maxgamer.quickshop.util.Util;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        Material material = itemStack.getType();
        if (Double.isInfinite(price) || Double.isNaN(price)) {
            return new SimplePriceLimiterCheckResult(PriceLimiterStatus.NOT_VALID, undefinedMin, undefinedMax);
        }
        if (wholeNumberOnly) {
            try {
                BigDecimal.valueOf(price).setScale(0, RoundingMode.UNNECESSARY);
            } catch (ArithmeticException exception) {
                Util.debugLog(exception.getMessage());
                return new SimplePriceLimiterCheckResult(PriceLimiterStatus.NOT_A_WHOLE_NUMBER, undefinedMin, undefinedMax);
            }
        }
        for (RuleSet rule : rules.values()) {
            if (!rule.isApply(sender, material, currency)) {
                continue;
            }
            if (rule.isAllowed(price)) {
                continue;
            }
            return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PRICE_RESTRICTED, rule.getMin(), rule.getMax());
        }
        if (undefinedMin != -1) {
            if (price < undefinedMin) {
                return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PRICE_RESTRICTED, undefinedMin, undefinedMax);
            }
        }
        if (undefinedMax != -1) {
            if (price > undefinedMin) {
                return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PRICE_RESTRICTED, undefinedMin, undefinedMax);
            }
        }
        return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PASS, undefinedMin, undefinedMax);
    }

    public void loadConfiguration() {
        this.rules.clear();
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "price-restriction.yml"));
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

    @Nullable
    @Contract("_,null -> null")
    private RuleSet readRule(@NotNull String ruleName, @Nullable ConfigurationSection section) {
        if (section == null) return null;
        String bypassPermission = "quickshop.price.restriction.bypass." + ruleName;
        List<Material> materials = new ArrayList<>();
        double min = section.getDouble("min", 0d);
        double max = section.getDouble("max", Double.MAX_VALUE);
        for (String material : section.getStringList("materials")) {
            Material mat = Material.matchMaterial(material);
            if (mat == null) {
                plugin.getLogger().warning("Failed to read rule " + ruleName + "'s a Material option, invalid material " + material + "! Skipping...");
                continue;
            }
            materials.add(mat);
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
        return new RuleSet(materials, bypassPermission, currency, min, max);
    }

    @Override
    public ReloadResult reloadModule() throws Exception {
        loadConfiguration();
        return Reloadable.super.reloadModule();
    }

    @AllArgsConstructor
    @Data
    static class RuleSet {
        private final List<Material> materials;
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
        public boolean isApply(@NotNull CommandSender sender, @NotNull Material item, @Nullable String currency) {
            if (sender.hasPermission(this.bypassPermission)) return false;
            if (!this.materials.contains(item)) return false;
            if (currency == null) return true;
            return this.currency.stream().anyMatch(pattern -> pattern.matcher(currency).matches());
        }

        /**
         * Check if the rule is allowed to apply to the given price.
         *
         * @param price the price
         * @return true if the rule is allowed for given price
         */
        public boolean isAllowed(double price) {
            if (this.max != -1) {
                if (price > this.max) return false;
            }
            if (this.min != -1) {
                return !(price < this.min);
            }
            return true;
        }
    }
}
