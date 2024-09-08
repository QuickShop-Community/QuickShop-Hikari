package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.registry.BuiltInRegistry;
import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionRegistry;
import com.ghostchu.quickshop.api.shop.PriceLimiter;
import com.ghostchu.quickshop.api.shop.PriceLimiterCheckResult;
import com.ghostchu.quickshop.api.shop.PriceLimiterStatus;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.ItemContainerUtil;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.paste.item.SubPasteItem;
import com.ghostchu.quickshop.util.paste.util.HTMLTable;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.Reloadable;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
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
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SimplePriceLimiter implements Reloadable, PriceLimiter, SubPasteItem {
    private final QuickShop plugin;
    private final Map<String, RuleSet> rules = new LinkedHashMap<>();
    private boolean wholeNumberOnly = false;
    private double undefinedMin = 0.0d;
    private double undefinedMax = Double.MAX_VALUE;

    public SimplePriceLimiter(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        loadConfiguration();
        plugin.getReloadManager().register(this);
        plugin.getPasteManager().register(plugin.getJavaPlugin(), this);
    }

    public void loadConfiguration() {
        this.rules.clear();
        File configFile = new File(plugin.getDataFolder(), "price-restriction.yml");
        if (!configFile.exists()) {
            try {
                Files.copy(plugin.getJavaPlugin().getResource("price-restriction.yml"), configFile.toPath());
            } catch (IOException e) {
                plugin.logger().warn("Failed to copy price-restriction.yml.yml to plugin folder!", e);
            }
        }

        FileConfiguration configuration = YamlConfiguration.loadConfiguration(configFile);
        if (performMigrate(configuration)) {
            try {
                configuration.save(configFile);
            } catch (IOException e) {
                plugin.logger().warn("Failed to save migrated  price-restriction.yml.yml to plugin folder!", e);
            }
        }
        this.undefinedMax = configuration.getDouble("undefined.max", 99999999999999999999999999999.99d);
        this.undefinedMin = configuration.getDouble("undefined.min", 0.0d);
        this.wholeNumberOnly = configuration.getBoolean("whole-number-only", false);
        if (!configuration.getBoolean("enable", false)) {
            return;
        }
        ConfigurationSection rules = configuration.getConfigurationSection("rules");
        if (rules == null) {
            plugin.logger().warn("Failed to read price-restriction.yml, syntax invalid!");
            return;
        }
        for (String ruleName : rules.getKeys(false)) {
            RuleSet rule = readRule(ruleName, rules.getConfigurationSection(ruleName));
            if (rule == null) {
                plugin.logger().warn("Failed to read rule {}, syntax invalid! Skipping...", ruleName);
                continue;
            }
            this.rules.put(ruleName, rule);
        }
        plugin.logger().info("Loaded {} price restriction rules!", this.rules.size());
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
        if (configuration.getInt("version") == 2) {
            if (configuration.getDouble("undefined.max") == -1) {
                configuration.set("undefined.max", 99999999999999999999999999999.99d); // DECIMAL (32,2) MAX
            }
            configuration.set("version", 3);
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
        ItemExpressionRegistry itemExpressionRegistry = (ItemExpressionRegistry) plugin.getRegistry().getRegistry(BuiltInRegistry.ITEM_EXPRESSION);
        for (String item : section.getStringList("items")) {
            items.add(itemStack->itemExpressionRegistry.match(itemStack, item));
        }
        List<Pattern> currency = new ArrayList<>();
        for (String currencyStr1 : section.getStringList("currency")) {
            try {
                Pattern pattern = Pattern.compile(currencyStr1);
                currency.add(pattern);
            } catch (PatternSyntaxException e) {
                plugin.logger().warn("Failed to read rule {}'s a Currency option, invalid pattern {}! Skipping...", ruleName, currencyStr1);
            }
        }
        return new RuleSet(items, bypassPermission, currency, min, max);
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

        double minPrice = 0;
        double maxPrice = 0;
        boolean hasMaxPrice = false;
        final List<ItemStack> flattenedItems = ItemContainerUtil.flattenContents(itemStack, true, false);

        for (RuleSet rule : rules.values()) {
            if (rule.canBypass(sender) || !rule.isApplicableCurrency(currency)) {
                continue;
            }

            // we'll manually add the fist item, as we calculate on a single item basis for the parent item.
            // otherwise we would be adding up all the items a player is holding, rather than one.
            int itemTally = rule.isApply(itemStack) ? 1 : 0;
            itemTally += rule.tallyApplicableItems(flattenedItems);
            if (itemTally == 0) {
                continue;
            }

            if (rule.hasMinPrice()) {
                minPrice += rule.getMin() * itemTally;
            }
            if (rule.hasMaxPrice()) {
                hasMaxPrice = true;
                maxPrice += rule.getMax() * itemTally;
            }
        }

        if (price < minPrice || (hasMaxPrice && price > maxPrice)) {
            return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PRICE_RESTRICTED, minPrice, maxPrice);
        }
        if (undefinedMin != -1 && price < undefinedMin) {
            return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PRICE_RESTRICTED, undefinedMin, undefinedMax);
        }
        if (undefinedMax != -1 && price > undefinedMax) {
            return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PRICE_RESTRICTED, undefinedMin, undefinedMax);
        }
        return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PASS, undefinedMin, undefinedMax);
    }

    /**
     * Check the price restriction rules
     *
     * @param user    the user
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
    public PriceLimiterCheckResult check(@NotNull QUser user, @NotNull ItemStack itemStack, @Nullable String currency, double price) {
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

        double minPrice = 0;
        double maxPrice = 0;
        boolean hasMaxPrice = false;
        final List<ItemStack> flattenedItems = ItemContainerUtil.flattenContents(itemStack, true, false);

        for (RuleSet rule : rules.values()) {
            if (rule.canBypass(user) || !rule.isApplicableCurrency(currency)) {
                continue;
            }

            // we'll manually add the fist item, as we calculate on a single item basis for the parent item.
            // otherwise we would be adding up all the items a player is holding, rather than one.
            int itemTally = rule.isApply(itemStack) ? 1 : 0;
            itemTally += rule.tallyApplicableItems(flattenedItems);
            if (itemTally == 0) {
                continue;
            }

            if (rule.hasMinPrice()) {
                minPrice += rule.getMin() * itemTally;
            }
            if (rule.hasMaxPrice()) {
                hasMaxPrice = true;
                maxPrice += rule.getMax() * itemTally;
            }
        }

        if (price < minPrice || (hasMaxPrice && price > maxPrice)) {
            return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PRICE_RESTRICTED, minPrice, maxPrice);
        }
        if (undefinedMin != -1 && price < undefinedMin) {
            return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PRICE_RESTRICTED, undefinedMin, undefinedMax);
        }
        if (undefinedMax != -1 && price > undefinedMax) {
            return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PRICE_RESTRICTED, undefinedMin, undefinedMax);
        }
        return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PASS, undefinedMin, undefinedMax);
    }

    @Override
    public ReloadResult reloadModule() throws Exception {
        loadConfiguration();
        return Reloadable.super.reloadModule();
    }

    @Override
    public @NotNull String genBody() {
        StringJoiner joiner = new StringJoiner("<br/>");
        joiner.add("<h5>Metadata</h5>");
        HTMLTable meta = new HTMLTable(2, true);
        meta.insert("Undefined Minimum", undefinedMin);
        meta.insert("Undefined Maximum", undefinedMax);
        meta.insert("Only WholeNumber", wholeNumberOnly);
        meta.insert("Rules", rules.size());
        joiner.add(meta.render());
        joiner.add("<h5>Rules</h5>");
        HTMLTable rules = new HTMLTable(5);
        rules.setTableTitle("Rule Name", "Bypass Permission", "Items", "Currency", "Price Range");
        for (Map.Entry<String, RuleSet> entry : this.rules.entrySet()) {
            RuleSet rule = entry.getValue();
            String currencies = CommonUtil.list2String(rule.getCurrency());
            if (StringUtils.isEmpty(currencies)) {
                currencies = "*";
            }
            rules.insert(entry.getKey(), rule.getBypassPermission(), rule.getItems().size(), currencies, rule.getMin() + " - " + rule.getMax());
        }
        joiner.add(rules.render());
        return joiner.toString();
    }

    @Override
    public @NotNull String getTitle() {
        return "Price Limiter";
    }

    @Data
    static class RuleSet {
        private final List<Function<ItemStack, Boolean>> items;
        private final String bypassPermission;
        private final List<Pattern> currency;
        private final double min;
        private final double max;

        public RuleSet(List<Function<ItemStack, Boolean>> items, String bypassPermission, List<Pattern> currency, double min, double max) {
            this.items = items;
            this.bypassPermission = bypassPermission;
            this.currency = currency;
            this.min = min;
            this.max = max;
        }

        /**
         * Check if the rule is allowed to apply to the given price.
         *
         * @param price the price
         * @return true if the rule is allowed for given price
         */
        public boolean isAllowed(double price) {
            if (hasMaxPrice() && price > getMax()) {
                return false;
            }
            if (hasMinPrice()) {
                return price >= getMin();
            }
            return true;
        }

        /**
         * @return if this rule has a min price set.
         */
        public boolean hasMinPrice() {
            return getMin() > 0;
        }

        /**
         * @return if this rule has a max price set.
         */
        public boolean hasMaxPrice() {
            return getMax() >= 0;
        }

        /**
         * Tallies the number of items this rules applies to.
         *
         * @param stacks the items to tally
         * @return the sum of the item counts this rules applies to
         */
        public int tallyApplicableItems(@NotNull Iterable<ItemStack> stacks) {
            int tally = 0;
            for (ItemStack is : stacks) {
                if (isApply(is)) {
                    tally += is.getAmount();
                }
            }
            return tally;
        }

        /**
         * Checks if the provided CommandSender can bypass restrictions
         *
         * @param sender the CommandSender to check
         * @return true if they can bypass, otherwise false.
         */
        public boolean canBypass(@NotNull CommandSender sender) {
            return QuickShop.getPermissionManager().hasPermission(sender, this.bypassPermission);
        }

        /**
         * Checks if the provided QUser can bypass restrictions
         *
         * @param user the QUser to check
         * @return true if they can bypass, otherwise false.
         */
        public boolean canBypass(@NotNull QUser user) {
            return QuickShop.getPermissionManager().hasPermission(user, this.bypassPermission);
        }

        /**
         * Checks if the currency applies to this rule.
         * Will return true if the currency is null
         *
         * @param currency the currency to check
         * @return true if the currency either applies, or is null. false otherwise.
         */
        public boolean isApplicableCurrency(@Nullable String currency) {
            if (currency != null) {
                return this.currency.stream().anyMatch(pattern -> pattern.matcher(currency).matches());
            }
            return true;
        }

        /**
         * Check if the rule is allowed to apply to the given price.
         *
         * @param sender   the sender
         * @param item     the item
         * @param currency the currency
         * @return true if the rule is allowed to apply
         */
        public boolean isApply(@NotNull CommandSender sender, @NotNull ItemStack item, @Nullable String currency) {
            if (canBypass(sender) || !isApplicableCurrency(currency)) {
                return false;
            }
            return isApply(item);
        }

        /**
         * Check if the rule is allowed to apply to the given price.
         *
         * @param user     the user
         * @param item     the item
         * @param currency the currency
         * @return true if the rule is allowed to apply
         */
        public boolean isApply(@NotNull QUser user, @NotNull ItemStack item, @Nullable String currency) {
            if (canBypass(user) || !isApplicableCurrency(currency)) {
                return false;
            }
            return isApply(item);
        }

        /**
         * Check if a rule applies to an ItemStack
         *
         * @param stack the stack to check
         * @return true if it applies, otherwise false.
         */
        public boolean isApply(@NotNull ItemStack stack) {
            for (Function<ItemStack, Boolean> fun : items) {
                if (fun.apply(stack)) {
                    return true;
                }
            }
            return false;
        }

    }
}
