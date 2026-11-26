package com.ghostchu.quickshop.addon.chargetp;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandContainer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import com.ghostchu.quickshop.api.economy.EconomyProvider;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.management.ShopTeleportEvent;
import com.ghostchu.quickshop.obj.QUserImpl;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public final class Main extends JavaPlugin implements Listener {

  private static final String PERMISSION_PREFIX = "quickshopaddon.chargetp.";
  private static Main instance;
  private volatile Settings settings;
  private QuickShop plugin;

  public static Main getInstance() {

    return instance;
  }

  @Override
  public void onLoad() {

    instance = this;
  }

  @Override
  public void onEnable() {

    saveDefaultConfig();
    plugin = QuickShop.getInstance();
    getLogger().info("Loading Configurations...");
    try {
      settings = loadSettings();
    } catch(final Exception ex) {
      getLogger().log(Level.SEVERE, "Invalid ChargeTP configuration; teleport charges will be denied until corrected.", ex);
    }
    getLogger().info("Registering the listeners...");
    Bukkit.getPluginManager().registerEvents(this, this);
    plugin.getCommandManager().registerCmd(
            CommandContainer
                    .builder()
                    .prefix("chargetp")
                    .description((locale)->Component.text("Manage shop teleport pricing"))
                    .permission("quickshopaddon.chargetp.reload")
                    .executor(new ChargeTPCommand(this, plugin))
                    .build());
  }

  public boolean reloadSettings() {

    try {

      reloadConfig();
      settings = loadSettings();
      return true;

    } catch(final RuntimeException e) {

      getLogger().log(Level.WARNING, "Unable to reload ChargeTP configuration.", e);
      return false;
    }
  }

  @Override
  public void onDisable() {

    HandlerList.unregisterAll((Plugin)this);
    instance = null;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onShopTeleport(@NotNull final ShopTeleportEvent event) {

    if(!event.isPhase(Phase.MAIN) || event.isCancelled()) {

      return;
    }

    final Player player = event.player();
    final Settings currentSettings = settings;
    if(currentSettings == null) {

      event.setCancelled(true, "Teleport pricing is unavailable.");
      return;
    }
    if(!currentSettings.enabled || player.hasPermission(PERMISSION_PREFIX + "bypass")) {

      return;
    }

    final Shop shop = event.shop().orElse(null);
    if(shop == null) {

      event.setCancelled(true, currentSettings.error);
      return;
    }

    if(currentSettings.ownerFree && player.getUniqueId().equals(shop.getOwner().getUniqueId())) {

      return;
    }

    final Location from = player.getLocation();
    final Location to = event.teleportLocation();
    if(from.getWorld() == null || to == null || to.getWorld() == null) {

      event.setCancelled(true, currentSettings.error);
      return;
    }

    final String world = from.getWorld().getName();
    final boolean crossWorld = !from.getWorld().equals(to.getWorld());
    if(crossWorld && !currentSettings.crossWorldEnabled) {

      event.setCancelled(true, currentSettings.crossDenied);
      return;
    }

    final BigDecimal amount;
    try {

      BigDecimal price = currentSettings.worldPrices.getOrDefault(to.getWorld().getName(), currentSettings.base);
      if(crossWorld) {

        price = price.add(currentSettings.crossExtra);
      } else if(currentSettings.distance) {

        price = price.add(currentSettings.perBlock.multiply(BigDecimal.valueOf(from.distance(to))));
      }

      BigDecimal discount = BigDecimal.ZERO;
      for(final Map.Entry<String, BigDecimal> entry : currentSettings.discounts.entrySet()) {
        if(player.hasPermission(PERMISSION_PREFIX + "discount." + entry.getKey())) {

          discount = discount.max(entry.getValue());
        }
      }

      price = price.multiply(BigDecimal.ONE.subtract(discount.movePointLeft(2)));
      if(price.signum() == 0) {

        return;
      }

      price = price.max(currentSettings.min);
      if(currentSettings.max.signum() > 0) {

        price = price.min(currentSettings.max);
      }

      amount = price.setScale(2, RoundingMode.HALF_UP);
      if(amount.signum() == 0) {

        return;
      }

      final EconomyProvider provider = plugin.getEconomyManager().provider();
      if(provider == null || !provider.valid() || !provider.supportsCurrency(world, currentSettings.currency)) {

        event.setCancelled(true, currentSettings.error);
        return;
      }

      final QUser user = QUserImpl.createFullFilled(player);
      final String display = provider.format(amount, world, currentSettings.currency);
      if(provider.balance(user, world, currentSettings.currency).compareTo(amount) < 0) {

        event.setCancelled(true, currentSettings.insufficient.replace("{cost}", display));
        return;
      }

      if(!provider.withdraw(user, world, currentSettings.currency, amount)) {

        event.setCancelled(true, currentSettings.error);
        return;
      }

      player.sendMessage(currentSettings.charged.replace("{cost}", display));
      if(currentSettings.log) {

        getLogger().info("Charged " + player.getUniqueId() + " " + amount.toPlainString() + " in " + world);
      }
    } catch(final RuntimeException ex) {

      event.setCancelled(true, currentSettings.error);
      getLogger().log(Level.SEVERE, "Teleport payment failed or outcome uncertain for " + player.getUniqueId(), ex);
    }
  }

  private Settings loadSettings() {

    final FileConfiguration config = getConfig();
    final BigDecimal base = number(config, "tp-cost", "100");
    final BigDecimal min = number(config, "pricing.minimum", "0");
    final BigDecimal max = number(config, "pricing.maximum", "0");
    if(max.signum() > 0 && min.compareTo(max) > 0) {

      throw new IllegalArgumentException("minimum exceeds maximum");
    }

    final String mode = config.getString("pricing.mode", "FIXED");
    if(!Set.of("FIXED", "DISTANCE").contains(mode)) {

      throw new IllegalArgumentException("Unknown pricing mode");
    }

    final String currency = config.getString("currency", "").trim();
    return new Settings(config.getBoolean("enabled", true), config.getBoolean("owner-free", true), mode.equals("DISTANCE"),
                        config.getBoolean("cross-world.enabled", true), config.getBoolean("log-payments", false), base, min, max,
                        number(config, "pricing.per-block", "0.05"), number(config, "cross-world.extra-cost", "50"),
                        (currency.isEmpty())? null : currency, map(config.getConfigurationSection("world-prices"), false),
                        map(config.getConfigurationSection("discounts"), true),
                        config.getString("messages.charged", "Teleport fee charged: {cost}"),
                        config.getString("messages.insufficient", "You need {cost} to teleport to this shop."),
                        config.getString("messages.error", "Teleport payment unavailable."),
                        config.getString("messages.cross-world-denied", "Cross-world shop teleportation is disabled."));
  }

  private static BigDecimal number(final ConfigurationSection config, final String key, final String fallback) {

    final Object raw = config.get(key, fallback);
    final BigDecimal value = new BigDecimal(String.valueOf(raw));
    if(value.signum() < 0 || value.compareTo(new BigDecimal("1000000000000")) > 0) {

      throw new IllegalArgumentException("Invalid amount: " + key);
    }
    return value;
  }

  private static Map<String, BigDecimal> map(final ConfigurationSection section, final boolean discount) {

    final Map<String, BigDecimal> result = new HashMap<>();
    if(section != null) {

      for(final String key : section.getKeys(false)) {

        final BigDecimal value = number(section, key, "0");
        if(discount && value.compareTo(BigDecimal.valueOf(100)) > 0) {

          throw new IllegalArgumentException("Discount exceeds 100: " + key);
        }
        result.put(key, value);
      }
    }
    return Map.copyOf(result);
  }

  private record Settings(boolean enabled, boolean ownerFree, boolean distance, boolean crossWorldEnabled,
                          boolean log, BigDecimal base, BigDecimal min, BigDecimal max, BigDecimal perBlock, BigDecimal crossExtra,
                          String currency, Map<String, BigDecimal> worldPrices, Map<String, BigDecimal> discounts,
                          String charged, String insufficient, String error, String crossDenied) {}
}