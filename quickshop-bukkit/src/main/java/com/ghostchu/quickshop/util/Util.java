package com.ghostchu.quickshop.util;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.inventory.CountableInventoryWrapper;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperIterator;
import com.ghostchu.quickshop.api.shop.ItemMatcher;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.common.util.RomanNumber;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import com.ghostchu.quickshop.util.logger.Log;
import io.papermc.lib.PaperLib;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Util {

  private static final EnumMap<Material, Integer> CUSTOM_STACKSIZE = new EnumMap<>(Material.class);
  private static final EnumSet<Material> SHOPABLES = EnumSet.noneOf(Material.class);
  private static final List<BlockFace> VERTICAL_FACING = List.of(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);
  private static int BYPASSED_CUSTOM_STACKSIZE = -1;
  private static Yaml yaml = null;
  private static Boolean devMode = null;
  @Setter
  private static QuickShop plugin;
  @Getter
  @Nullable
  private static DyeColor dyeColor = null;

  private Util() {

  }

  @Deprecated
  @ApiStatus.Internal
  public static EnumMap<Material, Integer> getCustomStacksize() {

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
  public static EnumSet<Material> getShopables() {

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

    QuickShop.folia().getImpl().runLaterAsync(runnable, 0);
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
    final BlockState bs = PaperLib.getBlockState(b, false).getState();
    final boolean container = bs instanceof InventoryHolder;
    if(!container) {
      if(Util.isDevMode()) {
        Log.debug(b.getType() + " not a container");
      }
      return false;
    }
    return true;
  }

  public static boolean isBlacklistWorld(@NotNull final World world) {

    return plugin.getConfig().getStringList("shop.blacklist-world").contains(world.getName());
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
    if(inv instanceof final CountableInventoryWrapper ciw) {

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
    if(inv instanceof final CountableInventoryWrapper ciw) {
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
    if(inv instanceof final CountableInventoryWrapper ciw) {
      return ciw.countSpace(shop::matches);
    } else {
      final ItemStack item = shop.getItem();
      int space = 0;
      final int itemMaxStackSize = getItemMaxStackSize(item.getType());
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
    if(inv instanceof final CountableInventoryWrapper ciw) {
      return ciw.countSpace(input->matcher.matches(item, input));
    } else {
      int space = 0;
      final int itemMaxStackSize = getItemMaxStackSize(item.getType());
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
    } catch(final Exception e) {
      throw new InvalidConfigurationException("Exception in deserialize item: " + config, e);
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

  @NotNull
  public static Component getItemStackName(@NotNull final ItemStack itemStack) {

    Component result = getItemCustomName(itemStack);
    if(isEmptyComponent(result)) {
      try {
        result = plugin.getPlatform().getTranslation(itemStack);
      } catch(final Throwable th) {
        result = MsgUtil.setHandleFailedHover(null, Component.text(itemStack.getType().getKey().toString()));
        plugin.logger().warn("Failed to handle translation for ItemStack {}", Util.serialize(itemStack), th);
      }
    }
    return result;
  }

  @Nullable
  public static Component getItemCustomName(@NotNull final ItemStack itemStack) {

    if(useEnchantmentForEnchantedBook() && itemStack.getType() == Material.ENCHANTED_BOOK) {
      final ItemMeta meta = itemStack.getItemMeta();
      if(meta instanceof final EnchantmentStorageMeta enchantmentStorageMeta && enchantmentStorageMeta.hasStoredEnchants()) {
        return getFirstEnchantmentName(enchantmentStorageMeta);
      }
    }

    if(itemStack.getType().getKey().getKey().toUpperCase(Locale.ROOT).contains("MUSIC_DISC")) {

      final String working = itemStack.getType().getKey().getKey().toUpperCase(Locale.ROOT);
      final String[] split = working.split("_");
      if(split.length >= 3) {

        return Component.text(split[1] + " " + split[2]);
      }
    }

    if(itemStack.getType().getKey().getKey().toUpperCase(Locale.ROOT).contains("SMITHING_TEMPLATE")) {

      final String working = itemStack.getType().getKey().getKey().toUpperCase(Locale.ROOT);
      final String[] split = working.split("_");
      if(split.length >= 2) {

        return Component.text(split[0] + " " + split[1]);
      }
    }

    if(itemStack.hasItemMeta() && Objects.requireNonNull(itemStack.getItemMeta()).hasDisplayName() && !QuickShop.getInstance().getConfig().getBoolean("shop.force-use-item-original-name")) {
      return plugin.getPlatform().getDisplayName(itemStack.getItemMeta());
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
    if(meta instanceof final EnchantmentStorageMeta enchantmentStorageMeta && enchantmentStorageMeta.hasStoredEnchants()) {
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
      name = plugin.getPlatform().getTranslation(enchantment);
    } catch(final Throwable throwable) {
      name = MsgUtil.setHandleFailedHover(null, Component.text(enchantment.getKey().getKey()));
      plugin.logger().warn("Failed to handle translation for Enchantment {}", enchantment.getKey(), throwable);
    }
    if(level > 1) {
      name.append(Component.text(" " + RomanNumber.toRoman(level)));
    }
    return name;
  }

  public static boolean useEnchantmentForEnchantedBook() {

    return plugin.getConfig().getBoolean("shop.use-enchantment-for-enchanted-book");
  }

  @NotNull
  public static Component getFirstEnchantmentName(@NotNull final EnchantmentStorageMeta meta) {

    if(!meta.hasStoredEnchants()) {
      throw new IllegalArgumentException("Item does not have an enchantment!");
    }
    final Entry<Enchantment, Integer> entry = meta.getStoredEnchants().entrySet().iterator().next();
    return enchantmentDataToComponent(entry.getKey(), entry.getValue());
  }

  public static int getItemTotalAmountsInMap(@NotNull final Map<Integer, ItemStack> map) {

    int total = 0;
    for(final ItemStack value : map.values()) {
      total += value.getAmount();
    }
    return total;
  }

  /**
   * Return the player names based on the configuration
   *
   * @return the player names
   */
  @NotNull
  public static List<String> getPlayerList() {

    final List<String> tabList = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
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
    if(!(blockData instanceof final org.bukkit.block.data.type.Chest chest)) {
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

    if(!(blockData instanceof final org.bukkit.block.data.type.Chest chestBlockData)) {
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

    if(sender instanceof final OfflinePlayer offlinePlayer) {
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
        BYPASSED_CUSTOM_STACKSIZE = Integer.parseInt(data[1]);
      }
      final Material mat = Material.matchMaterial(data[0]);
      if(mat == null || mat == Material.AIR) {
        plugin.logger().warn("{} not a valid material in custom-item-stacksize section.", material);
        continue;
      }
      CUSTOM_STACKSIZE.put(mat, Integer.parseInt(data[1]));
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

  public static boolean isDisplayAllowBlock(@NotNull final Material mat) {

    return mat.isTransparent() || isWallSign(mat);
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
    if(blockData instanceof final Directional directional) {
      return b.getRelative(directional.getFacing().getOppositeFace());
    } else {
      return null;
    }
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
  public static @NotNull Location lookAt(@NotNull Location loc, @NotNull final Location lookat) {
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
    loc.setYaw(-loc.getYaw() * 180f / (float)Math.PI + 360);
    // But pitch angles are normal
    loc.setPitch(pitch * 180f / (float)Math.PI);
    return loc;
  }

  /**
   * Execute the Runnable in server main thread. If it already on main-thread, will be executed
   * directly. or post to main-thread if came from any other thread.
   *
   * @param runnable The runnable
   */
  public static void regionThread(final Location location, @NotNull final Runnable runnable) {
    //QuickShop.folia().getImpl().runLater(runnable, 1);
    QuickShop.folia().getImpl().runAtLocationLater(location, runnable, 1);
  }

  /**
   * Execute the Runnable in server main thread. If it already on main-thread, will be executed
   * directly. or post to main-thread if came from any other thread.
   *
   * @param runnable The runnable
   */
  public static void mainThreadRun(@NotNull final Runnable runnable) {

    QuickShop.folia().getImpl().runLater(runnable, 1);
  }

  /**
   * Execute the Runnable in server main thread. If it already on main-thread, will be executed
   * directly. or post to main-thread if came from any other thread.
   *
   * @param runnable The runnable
   */
  public static void mainThreadRun(@NotNull final Runnable runnable, final long delay) {

    QuickShop.folia().getImpl().runLater(runnable, delay);
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

  /**
   * Unregister all listeners registered instances that belong to specified class
   *
   * @param plugin Plugin instance
   * @param clazz  Class to unregister
   */
  public static void unregisterListenerClazz(@NotNull final Plugin plugin, @NotNull final Class<? extends Listener> clazz) {

    for(final RegisteredListener registeredListener : HandlerList.getRegisteredListeners(plugin)) {
      if(registeredListener.getListener().getClass().equals(clazz)) {
        HandlerList.unregisterAll(registeredListener.getListener());
      }
    }
  }

  public static boolean checkIfBungee() {

    if(PackageUtil.parsePackageProperly("forceBungeeCord").asBoolean(false)) {
      return true;
    }
    return Bukkit.getServer().spigot().getConfig().getBoolean("settings.bungeecord");
  }
}
