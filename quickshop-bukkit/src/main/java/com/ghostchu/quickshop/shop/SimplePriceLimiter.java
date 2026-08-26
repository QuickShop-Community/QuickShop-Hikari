package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.registry.BuiltInRegistry;
import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionRegistry;
import com.ghostchu.quickshop.api.shop.IShopType;
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SimplePriceLimiter implements Reloadable, PriceLimiter, SubPasteItem {

  private final QuickShop plugin;
  private final Map<String, RuleSet> rules = new LinkedHashMap<>();
  private boolean wholeNumberOnly = false;
  private double globalSellingMin = -1;
  private double globalSellingMax = -1;
  private double globalBuyingMin = -1;
  private double globalBuyingMax = -1;
  private RuleScope defaultRuleScope = RuleScope.BUYING_AND_SELLING;

  public SimplePriceLimiter(@NotNull final QuickShop plugin) {

    this.plugin = plugin;
    loadConfiguration();
    plugin.getReloadManager().register(this);
    plugin.getPasteManager().register(plugin.getJavaPlugin(), this);
  }

  public void loadConfiguration() {

    this.rules.clear();
    final File configFile = new File(plugin.getDataFolder(), "price-restriction.yml");
    if(!configFile.exists()) {
      try {
        Files.copy(plugin.getJavaPlugin().getResource("price-restriction.yml"), configFile.toPath());
      } catch(final IOException e) {
        plugin.logger().warn("Failed to copy price-restriction.yml.yml to plugin folder!", e);
      }
    }

    final FileConfiguration configuration = YamlConfiguration.loadConfiguration(configFile);
    if(performMigrate(configuration)) {
      try {
        configuration.save(configFile);
      } catch(final IOException e) {
        plugin.logger().warn("Failed to save migrated  price-restriction.yml.yml to plugin folder!", e);
      }
    }
    this.globalSellingMax = configuration.getDouble("global.selling.max", -1);
    this.globalSellingMin = configuration.getDouble("global.selling.min", -1);
    this.globalBuyingMax = configuration.getDouble("global.buying.max", -1);
    this.globalBuyingMin = configuration.getDouble("global.buying.min", -1);
    this.defaultRuleScope = parseScope(configuration.getString("default-rule-scope"), RuleScope.BUYING_AND_SELLING,
                                       "default-rule-scope");
    this.wholeNumberOnly = configuration.getBoolean("whole-number-only", false);
    if(!configuration.getBoolean("enable", false)) {
      return;
    }
    final ConfigurationSection rules = configuration.getConfigurationSection("rules");
    if(rules == null) {
      plugin.logger().warn("Failed to read price-restriction.yml, syntax invalid!");
      return;
    }
    for(final String ruleName : rules.getKeys(false)) {
      final RuleSet rule = readRule(ruleName, rules.getConfigurationSection(ruleName));
      if(rule == null) {
        plugin.logger().warn("Failed to read rule {}, syntax invalid! Skipping...", ruleName);
        continue;
      }
      this.rules.put(ruleName, rule);
    }
    plugin.logger().info("Loaded {} price restriction rules!", this.rules.size());
  }

  private boolean performMigrate(@NotNull final FileConfiguration configuration) {

    boolean anyChanges = false;
    if(configuration.getInt("version", 1) == 1) {
      Log.debug("Migrating price-restriction.yml from version 1 to version 2");
      final ConfigurationSection rules = configuration.getConfigurationSection("rules");
      if(rules != null) {
        for(final String ruleName : rules.getKeys(false)) {
          final ConfigurationSection rule = rules.getConfigurationSection(ruleName);
          if(rule != null) {
            Log.debug("Migrating: Structure upgrading for rule " + ruleName);
            rule.set("items", rule.getStringList("materials"));
            rule.set("materials", null);
          }
        }
      }
      configuration.set("version", 2);
      anyChanges = true;
    }
    if(configuration.getInt("version") == 2) {
      if(configuration.getDouble("undefined.max") == -1) {
        configuration.set("undefined.max", 1.0E29); // DECIMAL (32,2) MAX
      }
      configuration.set("version", 3);
      anyChanges = true;
    }
    if(configuration.getInt("version") == 3) {
      Log.debug("Migrating price-restriction.yml from version 3 to version 4");
      final double min = configuration.getDouble("undefined.min", 0.01);
      final double max = configuration.getDouble("undefined.max", 1.0E29);
      configuration.set("global.selling.min", min);
      configuration.set("global.selling.max", max);
      configuration.set("global.buying.min", min);
      configuration.set("global.buying.max", max);
      configuration.set("default-rule-scope", RuleScope.BUYING_AND_SELLING.name());
      configuration.set("undefined", null);
      configuration.set("version", 4);
      anyChanges = true;
    }
    return anyChanges;
  }

  @Nullable
  @Contract("_,null -> null")
  private RuleSet readRule(@NotNull final String ruleName, @Nullable final ConfigurationSection section) {

    if(section == null) {
      return null;
    }
    final double min = section.getDouble("min", 0.0);
    final double max = section.getDouble("max", Double.MAX_VALUE);
    final RuleScope scope = parseScope(section.getString("scope"), defaultRuleScope, "rule " + ruleName);
    final String bypassPermission = "quickshop.price.restriction.bypass." + ruleName;
    final ItemExpressionRegistry itemExpressionRegistry = (ItemExpressionRegistry)plugin.getRegistry().getRegistry(BuiltInRegistry.ITEM_EXPRESSION);
    final List<Function<ItemStack, Boolean>> items = new ArrayList<>();
    for(final String item : section.getStringList("items")) {
      items.add(itemStack->itemExpressionRegistry.match(itemStack, item));
    }
    final List<Pattern> currency = new ArrayList<>();
    for(final String currencyStr1 : section.getStringList("currency")) {
      try {
        final Pattern pattern = Pattern.compile(currencyStr1.equals("*")? ".*" : currencyStr1);
        currency.add(pattern);
      } catch(final PatternSyntaxException e) {
        plugin.logger().warn("Failed to read rule {}'s a Currency option, invalid pattern {}! Skipping...", ruleName, currencyStr1);
      }
    }
    return new RuleSet(items, bypassPermission, currency, scope, min, max);
  }

  private RuleScope parseScope(@Nullable final String configuredScope, @NotNull final RuleScope fallback,
                               @NotNull final String settingName) {

    if(configuredScope == null) {
      return fallback;
    }
    try {
      return RuleScope.valueOf(configuredScope.toUpperCase(Locale.ROOT));
    } catch(final IllegalArgumentException exception) {
      plugin.logger().warn("{} has an invalid scope. Using {}.", settingName, fallback);
      return fallback;
    }
  }

  /**
   * Check the price restriction rules
   *
   * @param sender    the sender
   * @param itemStack the item to check
   * @param currency  the currency
   * @param price     the price
   *
   * @return the result
   */
    /*
    Use item stack to reserve the extent ability
     */
  @Override
  @NotNull
  public PriceLimiterCheckResult check(@NotNull final CommandSender sender, @NotNull final ItemStack itemStack,
                                       @Nullable final String currency, final double price) {

    return check(sender, itemStack, currency, price, null);
  }

  @Override
  @NotNull
  public PriceLimiterCheckResult check(@NotNull final CommandSender sender, @NotNull final ItemStack itemStack, @Nullable final String currency,
                                       final double price, @Nullable final IShopType shopType) {

    final double globalMin = globalMin(shopType);
    final double globalMax = globalMax(shopType);

    if(Double.isInfinite(price) || Double.isNaN(price)) {
      return new SimplePriceLimiterCheckResult(PriceLimiterStatus.NOT_VALID, globalMin, globalMax);
    }
    if(price < 0) {
      return new SimplePriceLimiterCheckResult(PriceLimiterStatus.REACHED_PRICE_MIN_LIMIT, 0.0d, globalMax);
    }
    if(wholeNumberOnly) {
      try {
        BigDecimal.valueOf(price).setScale(0, RoundingMode.UNNECESSARY);
      } catch(final ArithmeticException exception) {
        Log.debug(exception.getMessage());
        return new SimplePriceLimiterCheckResult(PriceLimiterStatus.NOT_A_WHOLE_NUMBER, globalMin, globalMax);
      }
    }

    double minPrice = 0;
    double maxPrice = 0;
    boolean hasMaxPrice = false;
    boolean hasMinPrice = false;
    final List<ItemStack> flattenedItems = ItemContainerUtil.flattenContents(itemStack, true, false);

    for(final RuleSet rule : rules.values()) {
      if(rule.canBypass(sender) || !rule.isApplicableCurrency(currency) || !rule.isApplicableShopType(shopType)) {
        continue;
      }

      // we'll manually add the fist item, as we calculate on a single item basis for the parent item.
      // otherwise we would be adding up all the items a player is holding, rather than one.
      int itemTally = rule.isApply(itemStack)? 1 : 0;
      itemTally += rule.tallyApplicableItems(flattenedItems);
      if(itemTally == 0) {
        continue;
      }

      if(rule.hasMinPrice()) {
        hasMinPrice = true;
        minPrice += rule.getMin() * itemTally;
      }
      if(rule.hasMaxPrice()) {
        hasMaxPrice = true;
        maxPrice += rule.getMax() * itemTally;
      }
    }

    if ((hasMinPrice && price < minPrice) || (hasMaxPrice && price > maxPrice)
        || (globalMin > 0 && price < globalMin) || (globalMax >= 0 && price > globalMax)) {

      return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PRICE_RESTRICTED,
                                               (hasMinPrice)? minPrice : globalMin,
                                               (hasMaxPrice)? maxPrice : globalMax);
    }
    return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PASS, globalMin, globalMax);
  }

  /**
   * Check the price restriction rules
   *
   * @param user      the user
   * @param itemStack the item to check
   * @param currency  the currency
   * @param price     the price
   *
   * @return the result
   */
    /*
    Use item stack to reserve the extent ability
     */
  @Override
  @NotNull
  public PriceLimiterCheckResult check(@NotNull final QUser user, @NotNull final ItemStack itemStack,
                                       @Nullable final String currency, final double price) {

    return check(user, itemStack, currency, price, null);
  }

  @Override
  @NotNull
  public PriceLimiterCheckResult check(@NotNull final QUser user, @NotNull final ItemStack itemStack, @Nullable final String currency,
                                       final double price, @Nullable final IShopType shopType) {

    final double globalMin = globalMin(shopType);
    final double globalMax = globalMax(shopType);

    if(Double.isInfinite(price) || Double.isNaN(price)) {
      return new SimplePriceLimiterCheckResult(PriceLimiterStatus.NOT_VALID, globalMin, globalMax);
    }
    if(price < 0) {
      return new SimplePriceLimiterCheckResult(PriceLimiterStatus.REACHED_PRICE_MIN_LIMIT, 0.0d, globalMax);
    }
    if(wholeNumberOnly) {
      try {
        BigDecimal.valueOf(price).setScale(0, RoundingMode.UNNECESSARY);
      } catch(final ArithmeticException exception) {
        Log.debug(exception.getMessage());
        return new SimplePriceLimiterCheckResult(PriceLimiterStatus.NOT_A_WHOLE_NUMBER, globalMin, globalMax);
      }
    }

    double minPrice = 0;
    double maxPrice = 0;
    boolean hasMaxPrice = false;
    boolean hasMinPrice = false;
    final List<ItemStack> flattenedItems = ItemContainerUtil.flattenContents(itemStack, true, false);

    for(final RuleSet rule : rules.values()) {
      if(rule.canBypass(user) || !rule.isApplicableCurrency(currency) || !rule.isApplicableShopType(shopType)) {
        continue;
      }

      // we'll manually add the fist item, as we calculate on a single item basis for the parent item.
      // otherwise we would be adding up all the items a player is holding, rather than one.
      int itemTally = rule.isApply(itemStack)? 1 : 0;
      itemTally += rule.tallyApplicableItems(flattenedItems);
      if(itemTally == 0) {
        continue;
      }

      if(rule.hasMinPrice()) {
        hasMinPrice = true;
        minPrice += rule.getMin() * itemTally;
      }
      if(rule.hasMaxPrice()) {
        hasMaxPrice = true;
        maxPrice += rule.getMax() * itemTally;
      }
    }

    if ((hasMinPrice && price < minPrice) || (hasMaxPrice && price > maxPrice)
        || (globalMin > 0 && price < globalMin) || (globalMax >= 0 && price > globalMax)) {

      return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PRICE_RESTRICTED,
                                               (hasMinPrice)? minPrice : globalMin,
                                               (hasMaxPrice)? maxPrice : globalMax);
    }
    return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PASS, globalMin, globalMax);
  }

  private double globalMin(@Nullable final IShopType shopType) {

    return shopType != null && shopType.isBuying()? globalBuyingMin : globalSellingMin;
  }

  private double globalMax(@Nullable final IShopType shopType) {

    return shopType != null && shopType.isBuying()? globalBuyingMax : globalSellingMax;
  }

  @Override
  public ReloadResult reloadModule() throws Exception {

    loadConfiguration();
    return Reloadable.super.reloadModule();
  }

  @Override
  @NotNull
  public String genBody() {

    final StringJoiner joiner = new StringJoiner("<br/>");
    joiner.add("<h5>Metadata</h5>");
    final HTMLTable meta = new HTMLTable(2, true);
    meta.insert("Buying Range", globalBuyingMin + " - " + globalBuyingMax);
    meta.insert("Selling Range", globalSellingMin + " - " + globalSellingMax);
    meta.insert("Only WholeNumber", wholeNumberOnly);
    meta.insert("Rules", rules.size());
    joiner.add(meta.render());
    joiner.add("<h5>Rules</h5>");
    final HTMLTable rules = new HTMLTable(5);
    rules.setTableTitle("Rule Name", "Bypass Permission", "Items", "Currency", "Price Range");
    for(final Map.Entry<String, RuleSet> entry : this.rules.entrySet()) {
      final RuleSet rule = entry.getValue();
      String currencies = CommonUtil.list2String(rule.getCurrency());
      if(CommonUtil.isEmptyString(currencies)) {
        currencies = "*";
      }
      rules.insert(entry.getKey(), rule.getBypassPermission(), rule.getItems().size(), currencies, rule.getMin() + " - " + rule.getMax());
    }
    joiner.add(rules.render());
    return joiner.toString();
  }

  @Override
  @NotNull
  public String getTitle() {

    return "Price Limiter";
  }

  static class RuleSet {

    private final List<Function<ItemStack, Boolean>> items;
    private final String bypassPermission;
    private final List<Pattern> currency;
    private final RuleScope scope;
    private final double min;
    private final double max;

    public RuleSet(final List<Function<ItemStack, Boolean>> items, final String bypassPermission, final List<Pattern> currency,
                   final RuleScope scope, final double min, final double max) {

      this.items = items;
      this.bypassPermission = bypassPermission;
      this.currency = currency;
      this.scope = scope;
      this.min = min;
      this.max = max;
    }

    /**
     * Check if the rule is allowed to apply to the given price.
     *
     * @param price the price
     *
     * @return true if the rule is allowed for given price
     */
    public boolean isAllowed(final double price) {

      if(hasMaxPrice() && price > getMax()) {
        return false;
      }
      if(hasMinPrice()) {
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
     *
     * @return the sum of the item counts this rules applies to
     */
    public int tallyApplicableItems(@NotNull final Iterable<ItemStack> stacks) {

      int tally = 0;
      for(final ItemStack is : stacks) {
        if(isApply(is)) {
          tally += is.getAmount();
        }
      }
      return tally;
    }

    /**
     * Checks if the provided CommandSender can bypass restrictions
     *
     * @param sender the CommandSender to check
     *
     * @return true if they can bypass, otherwise false.
     */
    public boolean canBypass(@NotNull final CommandSender sender) {

      return QuickShop.getPermissionManager().hasPermission(sender, this.bypassPermission);
    }

    /**
     * Checks if the provided QUser can bypass restrictions
     *
     * @param user the QUser to check
     *
     * @return true if they can bypass, otherwise false.
     */
    public boolean canBypass(@NotNull final QUser user) {

      return QuickShop.getPermissionManager().hasPermission(user, this.bypassPermission);
    }

    /**
     * Checks if the currency applies to this rule. Will return true if the currency is null
     *
     * @param currency the currency to check
     *
     * @return true if the currency either applies, or is null. false otherwise.
     */
    public boolean isApplicableCurrency(@Nullable final String currency) {

      if(currency != null) {
        return this.currency.stream().anyMatch(pattern->pattern.matcher(currency).matches());
      }
      return true;
    }

    public boolean isApplicableShopType(@Nullable final IShopType shopType) {

      return shopType == null || scope == RuleScope.BUYING_AND_SELLING
             || scope == RuleScope.BUYING && shopType.isBuying()
             || scope == RuleScope.SELLING && !shopType.isBuying();
    }

    /**
     * Check if the rule is allowed to apply to the given price.
     *
     * @param sender   the sender
     * @param item     the item
     * @param currency the currency
     *
     * @return true if the rule is allowed to apply
     */
    public boolean isApply(@NotNull final CommandSender sender, @NotNull final ItemStack item, @Nullable final String currency) {

      if(canBypass(sender) || !isApplicableCurrency(currency)) {
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
     *
     * @return true if the rule is allowed to apply
     */
    public boolean isApply(@NotNull final QUser user, @NotNull final ItemStack item, @Nullable final String currency) {

      if(canBypass(user) || !isApplicableCurrency(currency)) {
        return false;
      }
      return isApply(item);
    }

    /**
     * Check if a rule applies to an ItemStack
     *
     * @param stack the stack to check
     *
     * @return true if it applies, otherwise false.
     */
    public boolean isApply(@NotNull final ItemStack stack) {

      for(final Function<ItemStack, Boolean> fun : items) {
        if(fun.apply(stack)) {
          return true;
        }
      }
      return false;
    }

    public List<Function<ItemStack, Boolean>> getItems() {

      return this.items;
    }

    public String getBypassPermission() {

      return this.bypassPermission;
    }

    public List<Pattern> getCurrency() {

      return this.currency;
    }

    public double getMin() {

      return this.min;
    }

    public double getMax() {

      return this.max;
    }

    @Override
    public boolean equals(final Object o) {

      if(o == this) return true;
      if(!(o instanceof SimplePriceLimiter.RuleSet)) return false;
      final SimplePriceLimiter.RuleSet other = (SimplePriceLimiter.RuleSet)o;
      return Double.compare(this.getMin(), other.getMin()) == 0
             && Double.compare(this.getMax(), other.getMax()) == 0
             && Objects.equals(this.getItems(), other.getItems())
             && Objects.equals(this.getBypassPermission(), other.getBypassPermission())
             && Objects.equals(this.getCurrency(), other.getCurrency())
             && this.scope == other.scope;
    }

    @Override
    public int hashCode() {

      return Objects.hash(this.getMin(), this.getMax(), this.getItems(), this.getBypassPermission(), this.getCurrency(), this.scope);
    }

    @Override
    public String toString() {

      return "SimplePriceLimiter.RuleSet(items=" + this.getItems() + ", bypassPermission=" + this.getBypassPermission() + ", currency=" + this.getCurrency() + ", scope=" + this.scope + ", min=" + this.getMin() + ", max=" + this.getMax() + ")";
    }
  }

  private enum RuleScope {
    BUYING,
    SELLING,
    BUYING_AND_SELLING
  }
}
