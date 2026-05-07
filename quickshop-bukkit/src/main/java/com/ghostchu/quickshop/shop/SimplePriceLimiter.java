package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.registry.BuiltInRegistry;
import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionRegistry;
import com.ghostchu.quickshop.api.shop.PriceLimiterStatus;
import com.ghostchu.quickshop.api.shop.limit.PriceLimiter;
import com.ghostchu.quickshop.api.shop.limit.PriceLimiterCheckResult;
import com.ghostchu.quickshop.api.shop.limit.RuleSet;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.shop.limit.SimpleRuleSet;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SimplePriceLimiter implements Reloadable, PriceLimiter<Double>, SubPasteItem {

  private final Map<String, SimpleRuleSet> rules = new LinkedHashMap<>();

  private final QuickShop plugin;
  private boolean wholeNumberOnly = false;
  private double undefinedMin = 0.0d;
  private double undefinedMax = Double.MAX_VALUE;

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
    this.undefinedMax = configuration.getDouble("undefined.max", 99999999999999999999999999999.99d);
    this.undefinedMin = configuration.getDouble("undefined.min", 0.0d);
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
      final SimpleRuleSet rule = readRule(ruleName, rules.getConfigurationSection(ruleName));
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
        configuration.set("undefined.max", 99999999999999999999999999999.99d); // DECIMAL (32,2) MAX
      }
      configuration.set("version", 3);
      anyChanges = true;
    }
    return anyChanges;
  }

  @Nullable
  @Contract("_,null -> null")
  private SimpleRuleSet readRule(@NotNull final String ruleName, @Nullable final ConfigurationSection section) {

    if(section == null) {
      return null;
    }
    final String bypassPermission = "quickshop.price.restriction.bypass." + ruleName;
    final List<Function<ItemStack, Boolean>> items = new ArrayList<>();
    final double min = section.getDouble("min", 0d);
    final double max = section.getDouble("max", Double.MAX_VALUE);
    final ItemExpressionRegistry itemExpressionRegistry = (ItemExpressionRegistry)plugin.getRegistry().getRegistry(BuiltInRegistry.ITEM_EXPRESSION);
    for(final String item : section.getStringList("items")) {
      items.add(itemStack->itemExpressionRegistry.match(itemStack, item));
    }
    final List<Pattern> currency = new ArrayList<>();
    for(final String currencyStr1 : section.getStringList("currency")) {
      try {
        final Pattern pattern = Pattern.compile(currencyStr1);
        currency.add(pattern);
      } catch(final PatternSyntaxException e) {
        plugin.logger().warn("Failed to read rule {}'s a Currency option, invalid pattern {}! Skipping...", ruleName, currencyStr1);
      }
    }
    return new SimpleRuleSet(items, bypassPermission, currency, min, max);
  }

  @Override
  public Set<RuleSet<Double>> findApplicableRules(@NotNull final ItemStack stack) {

    final Set<RuleSet<Double>> applicableRules = new HashSet<>();

    for(final SimpleRuleSet rule : rules.values()) {

      if(rule.isApplicable(stack)) {
        applicableRules.add(rule);
      }
    }
    return applicableRules;
  }

  @Override
  public Set<RuleSet<Double>> findApplicableRules(@NotNull final CommandSender sender, @NotNull final ItemStack stack) {

    final Set<RuleSet<Double>> applicableRules = new HashSet<>();

    for(final SimpleRuleSet rule : rules.values()) {

      if(rule.isApplicable(sender, stack)) {
        applicableRules.add(rule);
      }
    }
    return applicableRules;
  }

  @Override
  public Set<RuleSet<Double>> findApplicableRules(@NotNull final QUser user, @NotNull final ItemStack stack) {

    final Set<RuleSet<Double>> applicableRules = new HashSet<>();

    for(final SimpleRuleSet rule : rules.values()) {

      if(rule.isApplicable(user, stack)) {
        applicableRules.add(rule);
      }
    }
    return applicableRules;
  }

  @Override
  public Set<RuleSet<Double>> findApplicableRules(@NotNull final ItemStack stack, @NotNull final String currency) {

    final Set<RuleSet<Double>> applicableRules = new HashSet<>();

    for(final SimpleRuleSet rule : rules.values()) {

      if(rule.isApplicable(stack, currency)) {
        applicableRules.add(rule);
      }
    }
    return applicableRules;
  }

  @Override
  public Set<RuleSet<Double>> findApplicableRules(@NotNull final CommandSender sender, @NotNull final ItemStack stack, @NotNull final String currency) {

    final Set<RuleSet<Double>> applicableRules = new HashSet<>();

    for(final SimpleRuleSet rule : rules.values()) {

      if(rule.isApplicable(sender, stack, currency)) {
        applicableRules.add(rule);
      }
    }
    return applicableRules;
  }

  @Override
  public Set<RuleSet<Double>> findApplicableRules(@NotNull final QUser user, @NotNull final ItemStack stack, @NotNull final String currency) {

    final Set<RuleSet<Double>> applicableRules = new HashSet<>();

    for(final SimpleRuleSet rule : rules.values()) {

      if(rule.isApplicable(user, stack, currency)) {
        applicableRules.add(rule);
      }
    }
    return applicableRules;
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
  public PriceLimiterCheckResult check(@NotNull final CommandSender sender, @NotNull final ItemStack itemStack, @Nullable final String currency, final double price) {

    if(Double.isInfinite(price) || Double.isNaN(price)) {
      return new SimplePriceLimiterCheckResult(PriceLimiterStatus.NOT_VALID, undefinedMin, undefinedMax);
    }
    if(wholeNumberOnly) {
      try {
        BigDecimal.valueOf(price).setScale(0, RoundingMode.UNNECESSARY);
      } catch(final ArithmeticException exception) {
        Log.debug(exception.getMessage());
        return new SimplePriceLimiterCheckResult(PriceLimiterStatus.NOT_A_WHOLE_NUMBER, undefinedMin, undefinedMax);
      }
    }

    double minPrice = 0;
    double maxPrice = 0;
    boolean hasMaxPrice = false;
    final List<ItemStack> flattenedItems = ItemContainerUtil.flattenContents(itemStack, true, false);

    for(final RuleSet<Double> rule : rules.values()) {
      if(rule.canBypass(sender) || !rule.isApplicableCurrency(currency)) {
        continue;
      }

      // we'll manually add the fist item, as we calculate on a single item basis for the parent item.
      // otherwise we would be adding up all the items a player is holding, rather than one.
      int itemTally = rule.isApplicable(itemStack)? 1 : 0;
      itemTally += rule.tallyApplicableItems(flattenedItems);
      if(itemTally == 0) {
        continue;
      }

      if(rule.hasMinPrice()) {
        minPrice += rule.minPrice() * itemTally;
      }
      if(rule.hasMaxPrice()) {
        hasMaxPrice = true;
        maxPrice += rule.maxPrice() * itemTally;
      }
    }

    if(price < minPrice || (hasMaxPrice && price > maxPrice)) {
      return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PRICE_RESTRICTED, minPrice, maxPrice);
    }
    if(undefinedMin != -1 && price < undefinedMin) {
      return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PRICE_RESTRICTED, undefinedMin, undefinedMax);
    }
    if(undefinedMax != -1 && price > undefinedMax) {
      return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PRICE_RESTRICTED, undefinedMin, undefinedMax);
    }
    return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PASS, undefinedMin, undefinedMax);
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
  public PriceLimiterCheckResult check(@NotNull final QUser user, @NotNull final ItemStack itemStack, @Nullable final String currency, final double price) {

    if(Double.isInfinite(price) || Double.isNaN(price)) {
      return new SimplePriceLimiterCheckResult(PriceLimiterStatus.NOT_VALID, undefinedMin, undefinedMax);
    }
    if(wholeNumberOnly) {
      try {
        BigDecimal.valueOf(price).setScale(0, RoundingMode.UNNECESSARY);
      } catch(final ArithmeticException exception) {
        Log.debug(exception.getMessage());
        return new SimplePriceLimiterCheckResult(PriceLimiterStatus.NOT_A_WHOLE_NUMBER, undefinedMin, undefinedMax);
      }
    }

    double minPrice = 0;
    double maxPrice = 0;
    boolean hasMaxPrice = false;
    final List<ItemStack> flattenedItems = ItemContainerUtil.flattenContents(itemStack, true, false);

    for(final RuleSet<Double> rule : rules.values()) {
      if(rule.canBypass(user) || !rule.isApplicableCurrency(currency)) {
        continue;
      }

      // we'll manually add the fist item, as we calculate on a single item basis for the parent item.
      // otherwise we would be adding up all the items a player is holding, rather than one.
      int itemTally = rule.isApplicable(itemStack)? 1 : 0;
      itemTally += rule.tallyApplicableItems(flattenedItems);
      if(itemTally == 0) {
        continue;
      }

      if(rule.hasMinPrice()) {
        minPrice += rule.minPrice() * itemTally;
      }
      if(rule.hasMaxPrice()) {
        hasMaxPrice = true;
        maxPrice += rule.maxPrice() * itemTally;
      }
    }

    if(price < minPrice || (hasMaxPrice && price > maxPrice)) {
      return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PRICE_RESTRICTED, minPrice, maxPrice);
    }
    if(undefinedMin != -1 && price < undefinedMin) {
      return new SimplePriceLimiterCheckResult(PriceLimiterStatus.PRICE_RESTRICTED, undefinedMin, undefinedMax);
    }
    if(undefinedMax != -1 && price > undefinedMax) {
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

    final StringJoiner joiner = new StringJoiner("<br/>");
    joiner.add("<h5>Metadata</h5>");
    final HTMLTable meta = new HTMLTable(2, true);
    meta.insert("Undefined Minimum", undefinedMin);
    meta.insert("Undefined Maximum", undefinedMax);
    meta.insert("Only WholeNumber", wholeNumberOnly);
    meta.insert("Rules", rules.size());
    joiner.add(meta.render());
    joiner.add("<h5>Rules</h5>");
    final HTMLTable rules = new HTMLTable(5);
    rules.setTableTitle("Rule Name", "Bypass Permission", "Items", "Currency", "Price Range");

    for(final Map.Entry<String, SimpleRuleSet> entry : this.rules.entrySet()) {

      final SimpleRuleSet rule = entry.getValue();
      String currencies = CommonUtil.list2String(rule.currencies());
      if(CommonUtil.isEmptyString(currencies)) {
        currencies = "*";
      }
      rules.insert(entry.getKey(), rule.bypassPermission(), rule.items().size(), currencies, rule.minPrice() + " - " + rule.maxPrice());
    }
    joiner.add(rules.render());
    return joiner.toString();
  }

  @Override
  public @NotNull String getTitle() {

    return "Price Limiter";
  }
}