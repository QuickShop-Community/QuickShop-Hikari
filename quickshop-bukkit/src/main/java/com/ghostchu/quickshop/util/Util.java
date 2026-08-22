package com.ghostchu.quickshop.util;

import com.destroystokyo.paper.MaterialTags;
import com.destroystokyo.paper.ParticleBuilder;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.management.ShopCreateEvent;
import com.ghostchu.quickshop.api.inventory.CountableInventoryWrapper;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperIterator;
import com.ghostchu.quickshop.api.localization.text.ProxiedLocale;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.ItemMatcher;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopAction;
import com.ghostchu.quickshop.api.shop.layout.ConditionalRenderComponent;
import com.ghostchu.quickshop.api.shop.layout.RenderComponent;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.common.util.RomanNumber;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.shop.SimpleInfo;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import com.ghostchu.quickshop.util.logger.Log;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

  private static final Map<Material, Integer> CUSTOM_STACKSIZE = new HashMap<>();
  private static final Set<Material> SHOPABLES = new HashSet<>();
  private static final List<BlockFace> VERTICAL_FACING = List.of(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);
  private static int BYPASSED_CUSTOM_STACKSIZE = -1;
  //add limit for vanilla values
  public static final int VANILLA_MAX_STACK_SIZE = 99;
  private static Yaml yaml = null;
  private static Boolean devMode = null;
  private static QuickShop plugin;
  @Nullable
  private static DyeColor dyeColor = null;

  private Util() {

  }

  @Deprecated
  @ApiStatus.Internal
  public static Map<Material, Integer> getCustomStacksize() {

    return CUSTOM_STACKSIZE;
  }

  @Deprecated
  @ApiStatus.Internal
  public static int getBypassedCustomStacksize() {

    return BYPASSED_CUSTOM_STACKSIZE;
  }

  /**
   * Use Util#isShopable instead
   */
  @ApiStatus.Internal
  @Deprecated
  public static Set<Material> getShopables() {

    return SHOPABLES;
  }

  /**
   * Execute the Runnable in async thread. If it already on main-thread, will be move to async
   * thread.
   *
   * @param runnable The runnable
   */
  public static void asyncThreadRun(@NotNull final Runnable runnable) {

    if(!plugin.getJavaPlugin().isEnabled()) {
      Log.debug(Level.WARNING, "Scheduler not available, executing task on current thread...");
      runnable.run();
      return;
    }

    QuickShop.folia().getScheduler().runLaterAsync(runnable, 0);
  }

  public static String locationToPDCString(final Location location) {

    return location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ();
  }

  public static Location locationFromPDCString(final World world, @Nullable final String locationString) {

    if(locationString == null) {
      return null;
    }
    final String[] split = locationString.split(";");
    if(split.length < 3) {
      return null;
    }

    return new Location(world, Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
  }

  public static void playClickSound(@NotNull final Player player) {

    if(plugin.getConfig().getBoolean("effect.sound.onclick")) {
      player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 80.0F, 1.0F);
    }
  }

  public static void playSound(@NotNull final Player player, @NotNull final String config) {

    final boolean globalEnabled = plugin.getConfig().getBoolean("effect.sound.enabled");
    if(!globalEnabled) {
      return;
    }

    final float globalVolume = plugin.getConfig().getFloat("effect.sound.volume");
    final float globalPitch = plugin.getConfig().getFloat("effect.sound.pitch");

    final Route route = Route.fromString(config);

    if(!plugin.getConfig().contains(route)) {
      return;
    }

    final Route parentEnabled = route.parent().add("enabled");
    if(plugin.getConfig().contains(parentEnabled) && !plugin.getConfig().getBoolean(parentEnabled)) {
      return;
    }

    final boolean enabled = plugin.getConfig().getBoolean(config + ".enabled", true);
    if(!enabled) {
      return;
    }

    final float volume = plugin.getConfig().getFloat(config + ".volume", globalVolume);
    final float pitch = plugin.getConfig().getFloat(config + ".pitch", globalPitch);

    //final Registry<Sound> registryAccess = RegistryAccess.registryAccess().getRegistry(RegistryKey.SOUND_EVENT);

    player.playSound(player.getLocation(), Sound.valueOf(plugin.getConfig().getString(config + ".sound")), volume, pitch);
  }

  public static void playParticle(@NotNull final Player player, @NotNull final String config) {

    if(!plugin.getConfig().getBoolean("effect.particle.enabled")) {
      return;
    }

    final Route route = Route.fromString(config);
    if(!plugin.getConfig().contains(route)) {
      return;
    }

    final Route parentEnabled = route.parent().add("enabled");
    if(plugin.getConfig().contains(parentEnabled) && !plugin.getConfig().getBoolean(parentEnabled)) {

      return;
    }

    if(!plugin.getConfig().getBoolean(config + ".enabled", true)) {

      return;
    }

    final String particleName = plugin.getConfig().getString(config + ".particle", "");
    if(particleName == null || particleName.isEmpty()) {

      return;
    }

    final Particle particle;
    try {

      particle = Particle.valueOf(particleName.toUpperCase());
    } catch (final Exception e) {

      plugin.logger().warn("Invalid particle: " + particleName);
      return;
    }

    final int count = plugin.getConfig().getInt(config + ".count", 1);
    final double extra = plugin.getConfig().getDouble(config + ".extra", 0.0);

    final double offsetX = plugin.getConfig().getDouble(config + ".offset.x", 0.0);
    final double offsetY = plugin.getConfig().getDouble(config + ".offset.y", 0.0);
    final double offsetZ = plugin.getConfig().getDouble(config + ".offset.z", 0.0);

    final boolean selfOnly = plugin.getConfig().getBoolean("effect.particle.self-only", true);
    final int receiverDistance = plugin.getConfig().getInt("effect.particle.receiver-distance", 24);
    final boolean byDistance = plugin.getConfig().getBoolean("effect.particle.receiver-by-distance", true);

    final Location loc = player.getLocation().add(0, 1, 0);

    final ParticleBuilder builder = new ParticleBuilder(particle)
            .location(loc)
            .count(count)
            .extra(extra)
            .offset(offsetX, offsetY, offsetZ);

    if(plugin.getConfig().contains(config + ".dust.color")) {

      final Color color = parseColor(plugin.getConfig().getString(config + ".dust.color"));
      final float scale = (float)plugin.getConfig().getFloat(config + ".dust.scale", 1.0F);
      builder.color(color, scale);
    }

    if(plugin.getConfig().contains(config + ".dust-transition.from")) {

      final Color from = parseColor(plugin.getConfig().getString(config + ".dust-transition.from"));
      final Color to = parseColor(plugin.getConfig().getString(config + ".dust-transition.to"));
      final float scale = (float)plugin.getConfig().getFloat(config + ".dust-transition.scale", 1.0F);
      builder.colorTransition(from, to, scale);
    }

    if(plugin.getConfig().contains(config + ".block.material")) {

      final Material mat = Material.matchMaterial(plugin.getConfig().getString(config + ".block.material"));
      if(mat != null) {

        builder.data(mat.createBlockData());
      }
    }

    if(plugin.getConfig().contains(config + ".item.material")) {

      final Material mat = Material.matchMaterial(plugin.getConfig().getString(config + ".item.material"));
      if(mat != null) {

        builder.data(new ItemStack(mat));
      }
    }

    if(selfOnly) {

      builder.receivers(player);
    } else {

      builder.receivers(receiverDistance, byDistance);
    }

    builder.spawn();
  }

  private static Color parseColor(String hex) {

    if(hex == null) return Color.WHITE;

    hex = hex.replace("#", "");

    try {
      final int rgb = Integer.parseInt(hex, 16);
      return Color.fromRGB(rgb);
    } catch (final Exception e) {
      return Color.WHITE;
    }
  }

  public static boolean createShop(@NotNull final Player player, @Nullable final Block block, @NotNull final BlockFace blockFace, @NotNull final EquipmentSlot hand, @NotNull final ItemStack item) {

    Log.debug("==== Entering Shop Creation ====");

    final QUser qUser = QUserImpl.createFullFilled(player);
    if(block == null) {
      Log.debug("Block is null");
      return false; // This shouldn't happen because we have checked action type.
    }
    if(player.getGameMode() != GameMode.SURVIVAL) {
      Log.debug("Not in survival mode");
      return false; // Only survival :)
    }

    final ItemStack stack = item.clone();

    final int maxSize = Util.getItemMaxStackSize(stack.getType());
    if(stack.getAmount() > maxSize) {
      stack.setAmount(maxSize);
    }

    if(stack.getType().isAir()) {
      Log.debug("Invalid trade item: air");
      return false; // Air cannot be used for trade
    }
    if(!Util.canBeShop(block)) {
      Log.debug("Invalid shop block");
      return false;
    }

    if(plugin.getConfig().getBoolean("disable-quick-create")) {
      Log.debug("quick create disabled");
      return false;
    }
    if(plugin.getConfig().getBoolean("shop.disable-quick-create")) {
      Log.debug("quick create disabled");
      return false;
    }

    ShopAction action = null;
    if(plugin.perm().hasPermission(player, "quickshop.create.sell")) {
      action = ShopAction.CREATE_SELL;
    } else if(plugin.perm().hasPermission(player, "quickshop.create.buy")) {
      action = ShopAction.CREATE_BUY;
    }
    if(action == null) {
      Log.debug("No permission");
      // No permission
      return false;
    }
    // Double chest creation permission check
    if(Util.isDoubleChest(block.getBlockData()) &&
       !plugin.perm().hasPermission(player, "quickshop.create.double")) {
      plugin.text().of(player, "no-double-chests").send();
      return false;
    }
    // Blacklist check
    if(plugin.getShopItemBlackList().isBlacklisted(stack)
       && !plugin.perm()
            .hasPermission(player, "quickshop.bypass." + stack.getType().name())) {
      plugin.text().of(player, "blacklisted-item").send();
      Log.debug("Invalid item - blacklisted");
      return false;
    }
    // Check if had enderchest shop creation permission
    if(block.getType() == Material.ENDER_CHEST
       && !plugin.perm().hasPermission(player, "quickshop.create.enderchest")) {
      Log.debug("Invalid permission for enderchest");
      return false;
    }
    // Check if block is a wall sign
    if(Util.isWallSign(block.getType())) {
      Log.debug("Block is wallsign");
      return false;
    }
    // Finds out where the sign should be placed for the shop
    final Block last;
    if(Util.getVerticalFacing().contains(blockFace)) {

      last = block.getRelative(blockFace);
    } else {

      final Location playerLocation = player.getLocation();
      final double x = playerLocation.getX() - block.getX();
      final double z = playerLocation.getZ() - block.getZ();
      if(Math.abs(x) > Math.abs(z)) {
        if(x > 0) {
          last = block.getRelative(BlockFace.EAST);
        } else {
          last = block.getRelative(BlockFace.WEST);
        }
      } else {
        if(z > 0) {
          last = block.getRelative(BlockFace.SOUTH);
        } else {
          last = block.getRelative(BlockFace.NORTH);
        }
      }
    }

    // Send creation menu.
    final SimpleInfo info = new SimpleInfo(block.getLocation(), action, stack, last, false);

    final ShopCreateEvent event = new ShopCreateEvent(Phase.PRE_CANCELLABLE, null, qUser, block.getLocation());

    if(event.callCancellableEvent()) {

      Log.debug("ShopCreateEvent PRE_CANCELLABLE phase cancelled");
      return false;
    }

    plugin.getShopManager().getInteractiveManager().put(player.getUniqueId(), info);
    plugin.text().of(player, "how-much-to-trade-for", Util.getItemStackName(stack),
                     plugin.isAllowStack() &&
                     plugin.perm().hasPermission(player, "quickshop.create.stacks")
                     ? stack.getAmount() : 1).send();
    Log.debug("==== Ending Shop Creation ====");
    return false;
  }

  /**
   * Returns true if the given block could be used to make a shop out of.
   *
   * @param b The block to check, Possibly a chest, dispenser, etc.
   *
   * @return True if it can be made into a shop, otherwise false.
   */
  public static boolean canBeShop(@NotNull final Block b) {

    if(isBlacklistWorld(b.getWorld())) {
      return false;
    }
    // Specified types by configuration
    if(!isShoppables(b.getType())) {
      return false;
    }
    final BlockState bs = b.getState(false);
    final boolean container = bs instanceof InventoryHolder;
    if(!container) {
      if(Util.isDevMode()) {
        Log.debug(b.getType() + " not a container");
      }
      return false;
    }
    return true;
  }

  public static boolean canBeShop(@NotNull final Block b, final BlockState bs) {

    if(isBlacklistWorld(b.getWorld())) {
      return false;
    }

    // Specified types by configuration
    if(!isShoppables(b.getType())) {
      return false;
    }

    if (!(bs instanceof InventoryHolder)) {
      if(Util.isDevMode()) {
        Log.debug(b.getType() + " not a container");
      }
      return false;
    }
    return true;
  }

  public static boolean isBlacklistWorld(@NotNull final World world) {

    final List<String> whitelist = plugin.getConfig().getStringList("shop.whitelist-world");
    if(!whitelist.isEmpty()) {
      return !whitelist.contains(world.getName());
    }
    // fall back to blacklist check
    return plugin.getConfig().getStringList("shop.blacklist-world").contains(world.getName());
  }

  /**
   * Check if a world is blacklisted for database loading
   *
   * @param worldName The name of the world to check
   *
   * @return true if the world should be skipped, false otherwise
   */
  public static boolean isDatabaseLoadingBlacklisted(@NotNull final String worldName) {

    final List<String> whitelist = plugin.getConfig().getStringList("database-loading-whitelist-worlds");
    if(!whitelist.isEmpty()) {
      return !whitelist.contains(worldName);
    }
    return plugin.getConfig().getStringList("database-loading-blacklist-worlds").contains(worldName);
  }

  /**
   * Check a material is possible become a shop
   *
   * @param material Mat
   *
   * @return Can or not
   */
  public static boolean isShoppables(@NotNull final Material material) {

    return SHOPABLES.contains(material);
  }

  /**
   * Counts the number of items in the given inventory where Util.matches(inventory item, item) is
   * true.
   *
   * @param inv  The inventory to search
   * @param item The ItemStack to search for
   *
   * @return The number of items that match in this inventory.
   */
  public static int countItems(@Nullable final InventoryWrapper inv, @NotNull final ItemStack item) {

    if(inv == null) {
      return 0;
    }
    final ItemMatcher matcher = plugin.getItemMatcher();
    if(inv instanceof CountableInventoryWrapper ciw) {
      return ciw.countItem(input->matcher.matches(item, input));
    } else {
      int items = 0;
      for(final ItemStack iStack : inv) {
        if(iStack == null || iStack.getType() == Material.AIR) {
          continue;
        }
        if(matcher.matches(item, iStack)) {
          items += iStack.getAmount();
        }
      }
      return items / item.getAmount();
    }
  }

  /**
   * Counts the number of shop items in the given inventory where Util.matches(inventory item, item)
   * is true.
   *
   * @param inv  The inventory to search
   * @param shop The Shop for matching
   *
   * @return The number of shop items that match in this inventory.
   */
  public static int countItems(@Nullable final InventoryWrapper inv, @NotNull final Shop shop) {

    if(inv == null) {
      return 0;
    }
    if(inv instanceof CountableInventoryWrapper ciw) {
      return ciw.countItem(shop::matches);
    } else {
      int items = 0;
      for(final ItemStack iStack : inv) {
        if(iStack == null || iStack.getType() == Material.AIR) {
          continue;
        }
        if(shop.matches(iStack)) {
          items += iStack.getAmount();
        }
      }
      return items / shop.getItem().getAmount();
    }
  }

  /**
   * Returns the number of shop items that can be given to the inventory safely.
   *
   * @param inv  The inventory to count
   * @param shop The shop containing item prototype. Material, durabiltiy and enchants must match
   *             for 'stackability' to occur.
   *
   * @return The number of shop items that can be given to the inventory safely.
   */
  public static int countSpace(@Nullable final InventoryWrapper inv, @NotNull final Shop shop) {

    if(inv == null) {
      return 0;
    }
    if(inv instanceof CountableInventoryWrapper ciw) {
      return ciw.countSpace(shop::matches);
    } else {
      final ItemStack item = shop.getItem();
      int space = 0;
      final int itemMaxStackSize = item.getMaxStackSize();
      for(final ItemStack iStack : inv) {
        if(iStack == null || iStack.getType() == Material.AIR) {
          space += itemMaxStackSize;
        } else if(shop.matches(iStack)) {
          space += iStack.getAmount() >= itemMaxStackSize? 0 : itemMaxStackSize - iStack.getAmount();
        }
      }
      return space / item.getAmount();
    }
  }

  /**
   * Returns a material max stacksize
   *
   * @param material Material
   *
   * @return Game StackSize or Custom
   */
  public static int getItemMaxStackSize(@NotNull final Material material) {

    return CUSTOM_STACKSIZE.getOrDefault(material, BYPASSED_CUSTOM_STACKSIZE == -1? material.getMaxStackSize() : BYPASSED_CUSTOM_STACKSIZE);
  }

  public static int[] getItemMaxStackSizes(@NotNull final ItemStack[] item) {

    final int[] stackSizes = new int[item.length];
    for(int i = 0; i < item.length; i++) {

      stackSizes[i] = getItemMaxStackSize(item[i].getType());
    }
    return stackSizes;
  }

  /**
   * Returns the number of items that can be given to the inventory safely.
   *
   * @param inv  The inventory to count
   * @param item The item prototype. Material, durabiltiy and enchants must match for 'stackability'
   *             to occur.
   *
   * @return The number of items that can be given to the inventory safely.
   */
  public static int countSpace(@Nullable final InventoryWrapper inv, @NotNull final ItemStack item) {

    if(inv == null) {
      return 0;
    }
    final ItemMatcher matcher = plugin.getItemMatcher();
    if(inv instanceof CountableInventoryWrapper ciw) {
      return ciw.countSpace(input->matcher.matches(item, input));
    } else {
      int space = 0;
      final int itemMaxStackSize = item.getMaxStackSize();
      for(final ItemStack iStack : inv) {
        if(iStack == null || iStack.getType() == Material.AIR) {
          space += itemMaxStackSize;
        } else if(matcher.matches(item, iStack)) {
          space += iStack.getAmount() >= itemMaxStackSize? 0 : itemMaxStackSize - iStack.getAmount();
        }
      }
      return space / item.getAmount();
    }
  }

  /**
   * Print debug log when plugin running on dev mode.
   *
   * @param logs logs
   */
  @Deprecated(forRemoval = true)
  public static void debugLog(@NotNull final String... logs) {

    final Log.Caller caller = Log.Caller.create();
    for(final String log : logs) {
      Log.debug(Level.INFO, log, caller);
    }
  }

  public static BigDecimal parse(final String input) {

    try {

      return new BigDecimal(input);
    } catch(final Exception ignore) {

      final String shortcuts = "kMGTPEZYXWVUN₮";
      final Matcher matcher = Pattern.compile("([0-9]+(?:\\.[0-9]*)?)[" + shortcuts + "]$").matcher(input);
      if(matcher.find()) {

        final BigDecimal baseValue = new BigDecimal(matcher.group(1));
        final char suffix = input.charAt(input.length() - 1);
        final int exponent = (shortcuts.indexOf(suffix) + 1) * 3; // Exponent based on position in the string

        if(exponent > 0) {

          final int digits = QuickShop.getInstance().getConfig().getInt("shop.maximum-digits-in-price", -1);
          final BigDecimal value = baseValue.multiply(BigDecimal.TEN.pow(exponent));
          if(digits == -1) {
            return value;
          }

          return value.setScale(digits, RoundingMode.HALF_UP);
        }
      }
    }
    return null;
  }

  /**
   * Covert YAML string to ItemStack.
   *
   * @param config serialized ItemStack
   *
   * @return ItemStack iStack
   *
   * @throws InvalidConfigurationException when failed deserialize config
   */
  @Nullable
  public static ItemStack deserialize(@NotNull String config) throws InvalidConfigurationException {

    if(yaml == null) {
      final DumperOptions yamlOptions = new DumperOptions();
      yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
      yamlOptions.setIndent(2);
      yaml = new Yaml(yamlOptions); //Caching it!
    }
    final YamlConfiguration yamlConfiguration = new YamlConfiguration();
    final Map<Object, Object> root = yaml.load(config);
    //noinspection unchecked
    final Map<String, Object> item = (Map<String, Object>)root.get("item");
    final int itemDataVersion = Integer.parseInt(String.valueOf(item.getOrDefault("v", "0")));
    try {
      // Try load the itemDataVersion to do some checks.
      //noinspection deprecation
      if(itemDataVersion > Bukkit.getUnsafe().getDataVersion()) {
        Log.debug("WARNING: DataVersion not matched with ItemStack: " + config);
        // okay we need some things to do
        if(plugin.getConfig().getBoolean("shop.force-load-downgrade-items.enable")) {
          // okay it enabled
          Log.debug("QuickShop is trying force loading " + config);
          if(plugin.getConfig().getInt("shop.force-load-downgrade-items.method") == 0) { // Mode 0
            //noinspection deprecation
            item.put("v", Bukkit.getUnsafe().getDataVersion() - 1);
          } else { // Mode other
            //noinspection deprecation
            item.put("v", Bukkit.getUnsafe().getDataVersion());
          }
          // Okay we have hacked the dataVersion, now put it back
          root.put("item", item);
          config = yaml.dump(root);
          Log.debug("Updated, we will try load as hacked ItemStack: " + config);
        } else {
          plugin.logger().warn("Cannot load ItemStack {} because it saved from higher Minecraft server version, the action will fail and you will receive a exception, PLEASE DON'T REPORT TO QUICKSHOP!", config);
          plugin.logger().warn("You can try force load this ItemStack by our hacked ItemStack read util (shop.force-load-downgrade-items), but beware, the data may corrupt if you load on this lower Minecraft server version, Please backup your world and database before enable!");
        }
      }
      yamlConfiguration.loadFromString(config);
      return yamlConfiguration.getItemStack("item");
    } catch(final Throwable th) {
      QuickShop.getInstance().logger().warn("Failed load shop data, because target config can't deserialize the ItemStack", th);
      Log.debug("Failed to load data to the ItemStack: " + config);
      return null;
    }
  }

  /**
   * Ensure this method is calling from specific thread
   *
   * @param async on async thread or main server thread.
   */
  public static void ensureThread(final boolean async) {

    final boolean isMainThread = Bukkit.isPrimaryThread();
    if(async) {
      if(isMainThread) {
        throw new IllegalStateException("#[Illegal Access] This method require runs on async thread.");
      }
    } else {
      if(!isMainThread) {
        throw new IllegalStateException("#[Illegal Access] This method require runs on server main thread.");
      }
    }
  }

  /**
   * Check two location is or not equals for the BlockPosition on 2D
   *
   * @param b1 block 1
   * @param b2 block 2
   *
   * @return Equals or not.
   */
  private static boolean equalsBlockStateLocation(@NotNull final Location b1, @NotNull final Location b2) {

    return (b1.getBlockX() == b2.getBlockX()) && (b1.getBlockY() == b2.getBlockY()) && (b1.getBlockZ() == b2.getBlockZ());
  }

  /**
   * Call a event and check it is cancelled.
   *
   * @param event The event implement the Cancellable interface.
   *
   * @return The event is cancelled.
   */
  public static boolean fireCancellableEvent(@NotNull final Cancellable event) {

    if(!(event instanceof Event)) {

      throw new IllegalArgumentException("Cancellable must is event implement");
    }
    Bukkit.getPluginManager().callEvent((Event)event);
    return event.isCancelled();
  }

  /**
   * Get location that converted to block position (.0)
   *
   * @param loc location
   *
   * @return blocked location
   */
  @NotNull
  public static Location getBlockLocation(@NotNull Location loc) {

    loc = loc.clone();
    loc.setX(loc.getBlockX());
    loc.setY(loc.getBlockY());
    loc.setZ(loc.getBlockZ());
    return loc;
  }

  /**
   * Get QuickShop caching folder
   *
   * @return The caching folder
   */
  public static File getCacheFolder() {

    final QuickShop qs = QuickShop.getInstance();
    if(qs != null) {
      final File cache = new File(QuickShop.getInstance().getDataFolder(), "cache");
      if(!cache.exists()) {
        cache.mkdirs();
      }
      return cache;
    } else {
      final File file = new File("cache");
      file.mkdirs();
      return file;
    }
  }

  /**
   * Use yaw to calc the BlockFace
   *
   * @param yaw Yaw (Player.getLocation().getYaw())
   *
   * @return BlockFace blockFace
   *
   * @deprecated Use Bukkit util not this one.
   */
  @NotNull
  public static BlockFace getYawFace(final float yaw) {
    //noinspection ConstantValue
    if(yaw > 315 && yaw <= 45) {
      return BlockFace.NORTH;
    } else if(yaw > 45 && yaw <= 135) {
      return BlockFace.EAST;
    } else if(yaw > 135 && yaw <= 225) {
      return BlockFace.SOUTH;
    } else {
      return BlockFace.WEST;
    }
  }

  public static Component getTextDisplay(@NotNull final Shop shop, @NotNull final ItemStack itemStack) {

    if(!plugin.getConfig().getBoolean("shop.text-display.enabled", false)) {
      return Component.empty();
    }

    Component display = Component.empty();
    final List<String> lines = plugin.getConfig().getStringList("shop.text-display.lines");
    final ProxiedLocale locale = plugin.text().findRelativeLanguages(plugin.text().getDefLocale());
    for(int i = 0; i < lines.size(); i++) {

      final String line = lines.get(i);

      boolean isFullLine = false;
      for (final RenderComponent component : plugin.getShopManager().shopLayoutProvider().fullLineRenderComponents()) {

        if (!component.appliesTo(line)) {
          continue;
        }

        display = display.append(component.render(shop, itemStack, locale));

        if(i < lines.size() - 1) {
          display = display.append(Component.newline());
        }
        isFullLine = true;
        break;

      }

      if (isFullLine) {
        continue;
      }

      Component lineComponent = MiniMessage.miniMessage().deserialize(line);
      for (final RenderComponent component : plugin.getShopManager().shopLayoutProvider().inlineRenderComponents()) {

        if (!component.appliesTo(line)) {
          continue;
        }
        if(component instanceof ConditionalRenderComponent conditionalComponent && conditionalComponent.isFullLine(shop)) {
          lineComponent = conditionalComponent.render(shop, itemStack, locale);
          break;
        }

        lineComponent = lineComponent.replaceText(builder -> builder
                .matchLiteral(component.placeholder())
                .replacement(component.render(shop, itemStack, locale)));
      }
      display = display.append(lineComponent);

      if(i < lines.size() - 1) {
        display = display.append(Component.newline());
      }
    }
    return display;
  }

  @NotNull
  public static Component getItemStackName(@NotNull final ItemStack itemStack) {

    return getItemStackName(itemStack, plugin.text().getDefLocale());
  }

  @NotNull
  public static Component getItemStackName(@NotNull final ItemStack itemStack, final String locale) {

    Component result = getItemCustomName(itemStack);
    if(isEmptyComponent(result)) {
      try {
        result = plugin.platform().getTranslation(itemStack);
      } catch(final Throwable th) {
        result = MsgUtil.setHandleFailedHover(null, Component.text(itemStack.getType().getKey().toString()));
        plugin.logger().warn("Failed to handle translation for ItemStack {}", itemStack.getType().getKey().asString(), th);
      }
    }
    return result;
  }

  @Nullable
  public static Component getItemCustomName(@NotNull final ItemStack itemStack) {

    return getItemCustomName(itemStack, plugin.text().getDefLocale());
  }

  @Nullable
  public static Component getItemCustomName(@NotNull final ItemStack itemStack, final String locale) {

    final ItemMeta meta = itemStack.getItemMeta();
    if(useEnchantmentForEnchantedBook() && itemStack.getType() == Material.ENCHANTED_BOOK) {
      if(meta instanceof EnchantmentStorageMeta enchantmentStorageMeta && enchantmentStorageMeta.hasStoredEnchants()) {
        return getFirstEnchantmentName(enchantmentStorageMeta);
      }
    }

    if(usePotionForPotionItem() && meta instanceof PotionMeta) {

      return getFirstPotionEffectName(itemStack, locale);
    }

    if(useSongForDiscItem() && MaterialTags.MUSIC_DISCS.isTagged(itemStack.getType())) {

      final Component component = Component.translatable("jukebox_song.minecraft." + itemStack.getType().name().toLowerCase(Locale.ROOT).replace("music_disc_", ""));
      final String[] asText = PlainTextComponentSerializer.plainText().serialize(component).split("-");
      final String songName = (asText.length > 1)? asText[1] : asText[0];

      return Component.text(songName).append(Component.text(" ")).append(plugin.platform().getTranslation(itemStack));
    }


    if(meta == null) {

      return null;
    }

    boolean itemName = false;

    try {
      itemName = meta.hasItemName();
    } catch(final NoSuchMethodError ignore) {
      //outdated
    }

    if(QuickShop.getInstance().getConfig().getBoolean("shop.force-use-item-original-name")) {

      try {
        return itemName ? meta.itemName() : null;
      } catch(final NoSuchMethodError ignored) {}

      return null;
    }

    if(meta.hasDisplayName() || itemName) {

      return plugin.platform().getDisplayName(meta);
    }
    return null;
  }

  public static boolean isEmptyComponent(@Nullable final Component component) {

    if(component == null) {
      return true;
    }
    return PlainTextComponentSerializer.plainText().serialize(component).isBlank();
  }

  /**
   * Find a string in a component
   *
   * @param component The component to check
   * @param find      The string to check for
   *
   * @return A boolean of whether the component contains the string
   */
  @NotNull
  public static boolean findStringInComponent(@NotNull final Component component, @NotNull final String find) {

    final String plainText = PlainTextComponentSerializer.plainText().serialize(component).toLowerCase();
    return plainText.replace(' ', '_').contains(find.toLowerCase());
  }

  /**
   * Check for a string in a List of Components
   *
   * @param components A List<Component> of the components to check
   * @param find       The string to look for amongst the components
   *
   * @return A boolean of whether the string was found in the list of components
   */
  @NotNull
  public static boolean findStringInList(@NotNull final List<Component> components, @NotNull final String find) {

    for(final Component name : components) {
      if(findStringInComponent(name, find)) { return true; }
    }

    return false;
  }

  /**
   * Get all enchants that can be found on an item stack
   *
   * @param itemStack The enchanted item
   *
   * @return The names of enchants contained on the enchanted item with levels
   */
  @NotNull
  public static List<Component> getEnchantsForItemStack(@NotNull final ItemStack itemStack) {

    final List<Component> enchants = new ArrayList<>();
    if(!itemStack.hasItemMeta()) {
      return enchants;
    }

    final ItemMeta meta = itemStack.getItemMeta();
    if(meta instanceof EnchantmentStorageMeta enchantmentStorageMeta && enchantmentStorageMeta.hasStoredEnchants()) {
      for(final Map.Entry<Enchantment, Integer> entry : enchantmentStorageMeta.getStoredEnchants().entrySet()) {
        final Component name = enchantmentDataToComponent(entry.getKey(), entry.getValue());
        enchants.add(name);
      }
    } else {
      for(final Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
        final Component name = enchantmentDataToComponent(entry.getKey(), entry.getValue());
        enchants.add(name);
      }
    }

    return enchants;
  }

  /**
   * Take enchantment and level and turn the Name and Level into a Component
   *
   * @param enchantment The enchantment to get the name of
   * @param level       The numeric level of the enchantment
   *
   * @return A component with the name of the Enchantment and it's Level as Roman Numerals
   */
  public static Component enchantmentDataToComponent(@NotNull final Enchantment enchantment, @NotNull final Integer level) {

    Component name;
    try {
      name = plugin.platform().getTranslation(enchantment);
    } catch(final Throwable throwable) {
      name = MsgUtil.setHandleFailedHover(null, Component.text(enchantment.getKey().getKey()));
      plugin.logger().warn("Failed to handle translation for Enchantment {}", enchantment.getKey(), throwable);
    }

    if(enchantment.getMaxLevel() > 1 || level > 1) {
      
      final String levelString = (plugin.getConfig().getBoolean("shop.use-roman-numeral-for-enchantments", true))? RomanNumber.toRoman(level) : "" + level;
      name = name.append(Component.text(" ")).append(Component.text(levelString));
    }
    return name;
  }

  public static boolean useEnchantmentForEnchantedBook() {

    return plugin.getConfig().getBoolean("shop.use-enchantment-for-enchanted-book");
  }

  @Nullable
  public static Entry<Enchantment, Integer> getFirstEnchantment(@NotNull final ItemStack itemStack) {

    final ItemMeta meta = itemStack.getItemMeta();
    if(meta instanceof EnchantmentStorageMeta enchantmentStorageMeta && enchantmentStorageMeta.hasStoredEnchants()) {
      return enchantmentStorageMeta.getStoredEnchants().entrySet().stream().findFirst().orElse(null);
    } else {

      return meta.getEnchants().entrySet().stream().findFirst().orElse(null);
    }
  }

  @NotNull
  public static Component getFirstEnchantmentName(@NotNull final EnchantmentStorageMeta meta) {

    if(!meta.hasStoredEnchants()) {
      throw new IllegalArgumentException("Item does not have an enchantment!");
    }
    final Entry<Enchantment, Integer> entry = meta.getStoredEnchants().entrySet().iterator().next();
    return enchantmentDataToComponent(entry.getKey(), entry.getValue());
  }

  public static boolean usePotionForPotionItem() {

    return plugin.getConfig().getBoolean("shop.use-effect-for-potion-item");
  }

  public static boolean useSongForDiscItem() {

    return plugin.getConfig().getBoolean("shop.use-song-for-disc-item");
  }

  @Nullable
  public static Component getFirstPotionEffectName(@NotNull final ItemStack item) {

    return getFirstPotionEffectName(item, plugin.text().getDefLocale());
  }

  @Nullable
  public static Component getFirstPotionEffectName(@NotNull final ItemStack item, final String locale) {

    Component name = null;
    final PotionEffect effect = getFirstPotionEffect(item);
    if(effect != null) {

      name = plugin.platform().getTranslation(effect.getType());

      name = name.append(Component.text(" " + RomanNumber.toRoman(effect.getAmplifier() + 1)));

      name = name.append(Component.text(" " + formatDuration(effect)));

      if(item.getType() == Material.SPLASH_POTION) {

        name = name.append(Component.text(" ")).append(plugin.text().of("signs.splash-potion").forLocale(locale));
      } else if(item.getType() == Material.LINGERING_POTION) {

        name = name.append(Component.text(" ")).append(plugin.text().of("signs.linger-potion").forLocale(locale));
      }
    }
    return name;
  }

  @Nullable
  public static PotionEffect getFirstPotionEffect(@NotNull final ItemStack item) {

    final ItemMeta meta = item.getItemMeta();
    if(meta instanceof PotionMeta potion && potion.getBasePotionType() != null && !potion.getBasePotionType().getPotionEffects().isEmpty()) {
      return potion.getBasePotionType().getPotionEffects().getFirst();
    }
    return null;
  }

  public static Component getPotionLevel(final PotionEffect effect) {

    return Component.text(RomanNumber.toRoman(effect.getAmplifier() + 1));
  }

  public static String getPotionDuration(final PotionEffect effect) {

    return formatDuration(effect);
  }

  public static String formatDuration(final PotionEffect effect) {

    if(effect.isInfinite()) {

      return "∞";
    }

    final int totalSeconds = effect.getDuration() / 20;
    final int minutes = totalSeconds / 60;
    final int seconds = totalSeconds % 60;

    return minutes + ":" + String.format("%02d", seconds);
  }

  public static int getItemTotalAmountsInMap(@NotNull final Map<Integer, ItemStack> map) {

    int total = 0;
    for(final ItemStack value : map.values()) {
      total += value.getAmount();
    }
    return total;
  }

  /**
   * Waits for the completion of a given {@link CompletableFuture} within a specified timeout
   * period. Throws appropriate exceptions if the future times out, encounters an execution error,
   * or the thread is interrupted.
   *
   * @param <T> The type of the result returned by the CompletableFuture.
   * @param future The CompletableFuture to wait for; must not be null.
   * @param timeout The maximum time to wait for the future to complete.
   * @param unit The time unit of the timeout argument.
   * @param description A description of the future operation, used for exception messages.
   *
   * @return The result of the completed CompletableFuture.
   *
   * @throws IllegalStateException If the provided future is null.
   * @throws RuntimeException      If the future times out, is interrupted, or encounters an
   *                               execution error.
   */
  public static <T> T waitForFuture(final CompletableFuture<T> future, final long timeout, final TimeUnit unit, final String description) throws RuntimeException {

    if(future == null) {

      throw new IllegalStateException("Future for " + description + " was null");
    }
    try {

      return future.get(timeout, unit);
    } catch(final TimeoutException e) {

      throw new RuntimeException("Timed out waiting for " + description, e);
    } catch(final ExecutionException e) {

      //Unwrap the cause so logs are more useful
      final Throwable cause = (e.getCause() != null)? e.getCause() : e;
      if(cause instanceof RuntimeException re) {
        throw re;
      }
      throw new RuntimeException("Error while waiting for " + description, cause);
    } catch(final InterruptedException e) {

      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted while waiting for " + description, e);
    }
  }

  /**
   * Return the player names based on the configuration
   *
   * @return the player names
   */
  @NotNull
  public static List<String> getPlayerList(final CommandSender sender) {

    final List<String> tabList = new ArrayList<>();
    if(sender instanceof Player player) {
      tabList.addAll(Bukkit.getOnlinePlayers().stream().filter(player::canSee).map(Player::getName).toList());
    } else {
      tabList.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
    }

    if(plugin.getConfig().getBoolean("include-offlineplayer-list")) {
      tabList.addAll(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).filter(Objects::nonNull).toList());
    }
    return tabList;
  }

  /**
   * Gets a plugin's Jar file
   *
   * @param plugin The plugin instance
   *
   * @return The plugin's Jar file
   *
   * @throws FileNotFoundException If the plugin's Jar file could not be found
   */
  @NotNull
  public static File getPluginJarFile(@NotNull final Plugin plugin) throws FileNotFoundException {

    final String path = getPluginJarPath(plugin);
    final File file = new File(path);
    if(!file.exists()) {
      throw new FileNotFoundException("File not found: " + path);
    }
    return file;
  }

  /**
   * Get class path of the given class.
   *
   * @param plugin Plugin plugin instance
   *
   * @return Class path
   */
  @NotNull
  public static String getPluginJarPath(@NotNull final Plugin plugin) {

    return CommonUtil.getClassPath(plugin.getClass());
  }

  /**
   * Returns the chest attached to the given chest. The given block must be a chest.
   *
   * @param block The chest block
   *
   * @return the block which is also a chest and connected to b.
   */
  public static Block getSecondHalf(@NotNull final Block block) {

    final BlockData blockData = block.getBlockData();
    if(!(blockData instanceof org.bukkit.block.data.type.Chest chest)) {
      return null;
    }
    if(!isDoubleChest(chest)) {
      return null;
    }
    final BlockFace towardsLeft = getRightSide(chest.getFacing());
    final BlockFace actuallyBlockFace = chest.getType() == org.bukkit.block.data.type.Chest.Type.LEFT? towardsLeft : towardsLeft.getOppositeFace();
    return block.getRelative(actuallyBlockFace);
  }

  public static boolean isDoubleChest(@Nullable final BlockData blockData) {

    if(!(blockData instanceof org.bukkit.block.data.type.Chest chestBlockData)) {
      return false;
    }
    return chestBlockData.getType() != org.bukkit.block.data.type.Chest.Type.SINGLE;
  }

  /**
   * return the right side for given blockFace
   *
   * @param blockFace given blockFace
   *
   * @return the right side for given blockFace, UP and DOWN will return itself
   */
  @NotNull
  public static BlockFace getRightSide(@NotNull final BlockFace blockFace) {

    return switch(blockFace) {
      case EAST -> BlockFace.SOUTH;
      case NORTH -> BlockFace.EAST;
      case SOUTH -> BlockFace.WEST;
      case WEST -> BlockFace.NORTH;
      default -> blockFace;
    };
  }

  /**
   * Gets the CommandSender unique id.
   *
   * @param sender the sender
   *
   * @return the sender unique id if sender is a player, otherwise nil unique id
   */
  @NotNull
  public static UUID getSenderUniqueId(@Nullable final CommandSender sender) {

    if(sender instanceof OfflinePlayer offlinePlayer) {
      return offlinePlayer.getUniqueId();
    }
    return CommonUtil.getNilUniqueId();
  }

  /**
   * Get the sign material using by plugin. With compatibly process.
   *
   * @return The material now using.
   */
  @NotNull
  public static Material getSignMaterial() {

    final Material signMaterial = Material.matchMaterial(plugin.getConfig().getString("shop.sign-material", "OAK_WALL_SIGN"));
    if(signMaterial != null) {
      return signMaterial;
    }
    return Material.OAK_WALL_SIGN;
  }

  /**
   * Getting startup flags
   *
   * @return Java startup flags without some JVM args
   */
  public static List<String> getStartupFlags() {

    return ManagementFactory.getRuntimeMXBean().getInputArguments();
  }

  /**
   * Gets the percentage (Without trailing %) damage on a tool.
   *
   * @param item The ItemStack of tools to check
   *
   * @return The percentage 'health' the tool has. (Opposite of total damage)
   */
  @NotNull
  public static String getToolPercentage(@NotNull final ItemStack item) {

    if(!(item.getItemMeta() instanceof Damageable)) {
      Log.debug(item.getType().name() + " not Damageable.");
      return "Error: NaN";
    }
    final double dura = ((Damageable)item.getItemMeta()).getDamage();
    final double max = item.getType().getMaxDurability();
    final DecimalFormat formatter = new DecimalFormat("0");
    return formatter.format((1 - dura / max) * 100.0);
  }

  /**
   * Get vertical BlockFace list
   *
   * @return vertical BlockFace list (unmodifiable)
   */
  @NotNull
  public static List<BlockFace> getVerticalFacing() {

    return VERTICAL_FACING;
  }

  /**
   * Initialize the Util tools.
   */
  public static void initialize() {

    plugin = QuickShop.getInstance();
    try {
      plugin.getReloadManager().unregister(Util.class.getDeclaredMethod("initialize"));
      plugin.getReloadManager().register(Util.class.getDeclaredMethod("initialize"));
    } catch(final NoSuchMethodException e) {
      plugin.logger().error("Failed to register Util initialize method to reload manager.", e);
    }
    SHOPABLES.clear();
    CUSTOM_STACKSIZE.clear();
    devMode = plugin.getConfig().getBoolean("dev-mode");

    for(final String s : plugin.getConfig().getStringList("shop-blocks")) {
      Material mat = Material.matchMaterial(s.toUpperCase());
      if(mat == null) {
        mat = Material.matchMaterial(s);
      }
      if(mat == null) {
        plugin.logger().warn("Invalid shop-block: {}", s);
      } else {
        SHOPABLES.add(mat);
      }
    }

    for(final String material : plugin.getConfig().getStringList("custom-item-stacksize")) {
      final String[] data = material.split(":");
      if(data.length != 2) {
        continue;
      }

      if("*".equalsIgnoreCase(data[0])) {
        try {

          BYPASSED_CUSTOM_STACKSIZE = Integer.parseInt(data[1]);
          if(BYPASSED_CUSTOM_STACKSIZE > VANILLA_MAX_STACK_SIZE) {

            BYPASSED_CUSTOM_STACKSIZE = VANILLA_MAX_STACK_SIZE;
            plugin.logger().warn("custom-item-stacksize for entry * was higher than the vanilla limit, resetting to maximum vanilla limit.", material);
          }
        } catch(final NumberFormatException ignore) {
        }
      }

      final Material mat = Material.matchMaterial(data[0]);
      if(mat == null || mat == Material.AIR) {
        plugin.logger().warn("{} not a valid material in custom-item-stacksize section.", material);
        continue;
      }

      try {

        final int stackSize = Integer.parseInt(data[1]);
        final boolean invalid = stackSize > VANILLA_MAX_STACK_SIZE;

        CUSTOM_STACKSIZE.put(mat, ((invalid)? VANILLA_MAX_STACK_SIZE : stackSize));

        if(invalid) {

          plugin.logger().warn("custom-item-stacksize for material {} was higher than the vanilla limit, resetting to maximum vanilla limit.", material);
        }
      } catch(final NumberFormatException ignore) {

      }
    }
    try {

      dyeColor = DyeColor.valueOf(plugin.getConfig().getString("shop.sign-dye-color"));
    } catch(final Exception ignored) {
    }
  }

  /**
   * Call this to check items in inventory and remove it.
   *
   * @param inv inv
   */
  public static void inventoryCheck(@Nullable final InventoryWrapper inv) {

    if(inv == null) {
      return;
    }
    if(inv.getHolder() == null) {
      Log.debug("Skipped plugin gui inventory check.");
      return;
    }
    final InventoryWrapperIterator iterator = inv.iterator();
    try {
      while(iterator.hasNext()) {
        final ItemStack itemStack = iterator.next();
        if(itemStack == null) {
          continue;
        }
        if(AbstractDisplayItem.checkIsGuardItemStack(itemStack)) {
          // Found Item and remove it.
          final Location location = inv.getLocation();
          if(location == null) {
            return; // Virtual GUI
          }
          iterator.remove();
          Log.debug("Found shop display item in an inventory" + location + ", Removing...");
          MsgUtil.sendGlobalAlert(plugin.text().of("inventory-check-global-alert", location, inv.getHolder().getClass().getName(), Util.getItemStackName(itemStack)).forLocale(MsgUtil.getDefaultGameLanguageCode()));
        }
      }
    } catch(final Exception ignored) {
    }
  }

  /**
   * @param stack The ItemStack to check if it is blacklisted
   *
   * @return true if the ItemStack is black listed. False if not.
   *
   * @deprecated Use QuickShopAPI#getShopItemBlackList() instead
   */
  @Deprecated(forRemoval = true)
  public static boolean isBlacklisted(@NotNull final ItemStack stack) {

    if(plugin == null) {
      throw new IllegalStateException("Plugin not fully started yet");
    }
    if(plugin.getItemMarker() == null) {
      throw new IllegalStateException("Plugin not fully started yet");
    }
    if(plugin.getShopItemBlackList() == null) {
      throw new IllegalStateException("Plugin not fully started yet");
    }
    return plugin.getShopItemBlackList().isBlacklisted(stack);
  }

  /**
   * Check QuickShop is running on dev edition or not.
   *
   * @return DevEdition status
   */
  public static boolean isDevEdition() {

    return !"origin/release".equalsIgnoreCase(QuickShop.getInstance().getBuildInfo().getGitInfo().getBranch());
  }

  /**
   * Get the plugin is under dev-mode(debug mode)
   *
   * @return under dev-mode
   */
  public static boolean isDevMode() {

    if(devMode != null) {
      return devMode;
    } else {
      if(plugin != null) {
        devMode = plugin.getConfig().getBoolean("dev-mode");
        return devMode;
      } else {
        return false;
      }
    }
  }

  /**
   * Check a material is or not a WALL_SIGN
   *
   * @param material mat
   *
   * @return is or not a wall_sign
   */
  public static boolean isWallSign(@Nullable final Material material) {

    if(material == null) {
      return false;
    }
    return Tag.WALL_SIGNS.isTagged(material);
  }

  /**
   * Get a material is a dye
   *
   * @param material The material
   *
   * @return yes or not
   */
  public static boolean isDyes(@NotNull final Material material) {

    return material.name().toUpperCase().endsWith("_DYE");
  }

  /**
   * Returns true if the given location is loaded or not.
   *
   * @param loc The location
   *
   * @return true if the given location is loaded or not.
   */
  public static boolean isLoaded(@NotNull final Location loc) {

    if(!loc.isWorldLoaded()) {
      return false;
    }
    // Calculate the chunks coordinates. These are 1,2,3 for each chunk, NOT
    // location rounded to the nearest 16.
    final int x = (int)Math.floor((loc.getBlockX()) / 16.0);
    final int z = (int)Math.floor((loc.getBlockZ()) / 16.0);
    return (loc.getWorld().isChunkLoaded(x, z));
  }

  /**
   * Get this method available or not
   *
   * @param className class qualifiedName
   * @param method    the name of method
   * @param args      the arg of method
   *
   * @return boolean Available
   */
  public static boolean isMethodAvailable(@NotNull final String className, final String method, final Class<?>... args) {// nosemgrep
    try {
      final Class<?> clazz = Class.forName(className);
      try {
        clazz.getDeclaredMethod(method, args);
      } catch(final NoSuchMethodException e) {
        clazz.getMethod(method, args);
      }
      return true;
    } catch(final Exception e) {
      return false;
    }
  }

  /**
   * Checks whether someone else's shop is within reach of a hopper being placed by a player.
   *
   * @param b The block being placed.
   * @param p The player performing the action.
   *
   * @return true if a nearby shop was found, false otherwise.
   */
  public static boolean isOtherShopWithinHopperReach(@NotNull final Block b, @NotNull final Player p) {

    final Block bshop = Util.getAttached(b);
    if(bshop == null) {
      return false;
    }
    Shop shop = plugin.getShopManager().getShopIncludeAttached(bshop.getLocation());
    if(shop == null) {
      shop = plugin.getShopManager().getShopIncludeAttached(bshop.getLocation().clone().add(0, 1, 0));
    }
    return shop != null && !shop.playerAuthorize(p.getUniqueId(), BuiltInShopPermission.ACCESS_INVENTORY);
  }

  /**
   * Fetches the block which the given sign is attached to
   *
   * @param b The block which is attached
   *
   * @return The block the sign is attached to
   */
  @Nullable
  public static Block getAttached(@NotNull final Block b) {

    final BlockData blockData = b.getBlockData();
    if(blockData instanceof Directional directional) {
      return b.getRelative(directional.getFacing().getOppositeFace());
    } else {
      return null;
    }
  }

  /**
   * Creates a byte representing a set of flags based on the provided boolean parameters. Each
   * parameter corresponds to a specific bit in the byte.
   *
   * @return a byte where each bit represents a corresponding flag set by the input parameters.
   */
  public static byte createTextDisplayFlags() {

    final int background = plugin.getConfig().getInt("shop.text-display.background-color", 1073741824);
    final boolean defaultBackground = background == 1073741824;
    final boolean hasShadow = plugin.getConfig().getBoolean("shop.text-display.shadow.enabled", true);
    final boolean seeThrough = plugin.getConfig().getBoolean("shop.text-display.see-through", false);

    byte flags = 0;

    if (hasShadow) {
      flags |= 1;
    }

    if (seeThrough) {
      flags |= 2;
    }

    if (defaultBackground) {
      flags |= 4;
    }

    return flags;
  }

  /**
   * @param mat The material to check
   *
   * @return Returns true if the item is a tool (Has durability) or false if it doesn't.
   */
  public static boolean isTool(@NotNull final Material mat) {

    return mat.getMaxDurability() != 0;
  }

  /**
   * Returns loc with modified pitch/yaw angles so it faces lookat
   *
   * @param loc    The location a players head is
   * @param lookat The location they should be looking
   *
   * @return The location the player should be facing to have their crosshairs on the location
   * lookAt Kudos to bergerkiller for most of this function
   */
  @NotNull
  public static Location lookAt(@NotNull Location loc, @NotNull final Location lookat) {
    // Clone the loc to prevent applied changes to the input loc
    loc = loc.clone();
    // Values of change in distance (make it relative)
    final double dx = lookat.getX() - loc.getX();
    final double dy = lookat.getY() - loc.getY();
    final double dz = lookat.getZ() - loc.getZ();
    // Set yaw
    if(dx != 0) {
      // Set yaw start value based on dx
      if(dx < 0) {
        loc.setYaw((float)(1.5 * Math.PI));
      } else {
        loc.setYaw((float)(0.5 * Math.PI));
      }
      loc.setYaw(loc.getYaw() - (float)Math.atan(dz / dx));
    } else if(dz < 0) {
      loc.setYaw((float)Math.PI);
    }
    // Get the distance from dx/dz
    final double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));
    final float pitch = (float)-Math.atan(dy / dxz);
    // Set values, convert to degrees
    // Minecraft yaw (vertical) angles are inverted (negative)
    loc.setYaw(-loc.getYaw() * 180.0F / (float)Math.PI + 360);
    // But pitch angles are normal
    loc.setPitch(pitch * 180.0F / (float)Math.PI);
    return loc;
  }

  /**
   * Execute the Runnable in server main thread. If it already on main-thread, will be executed
   * directly. or post to main-thread if came from any other thread.
   *
   * @param runnable The runnable
   */
  public static void regionThread(final Location location, @NotNull final Runnable runnable) {
    //QuickShop.folia().getScheduler().runLater(runnable, 1);
    QuickShop.folia().getScheduler().runAtLocationLater(location, runnable, 1);
  }

  /**
   * Execute the Runnable in server main thread. If it already on main-thread, will be executed
   * directly. or post to main-thread if came from any other thread.
   *
   * @param runnable The runnable
   */
  public static void mainThreadRun(@NotNull final Runnable runnable) {

    QuickShop.folia().getScheduler().runLater(runnable, 1);
  }

  /**
   * Execute the Runnable in server main thread. If it already on main-thread, will be executed
   * directly. or post to main-thread if came from any other thread.
   *
   * @param runnable The runnable
   */
  public static void mainThreadRun(@NotNull final Runnable runnable, final long delay) {

    QuickShop.folia().getScheduler().runLater(runnable, delay);
  }

  /**
   * Covert ItemStack to YAML string.
   *
   * @param iStack target ItemStack
   *
   * @return String serialized itemStack
   */
  @NotNull
  public static String serialize(@NotNull final ItemStack iStack) {

    final YamlConfiguration cfg = new YamlConfiguration();
    cfg.set("item", iStack);
    return cfg.saveToString();
  }

  public static boolean checkIfBungee() {

    if(plugin.getConfig().getBoolean("proxy.force-bungeecord", false)) {
      return true;
    }

    return Bukkit.getServer().spigot().getConfig().getBoolean("settings.bungeecord");
  }

  public static void setPlugin(final QuickShop plugin) {

    Util.plugin = plugin;
  }

  @Nullable
  public static DyeColor getDyeColor() {

    return Util.dyeColor;
  }
}
