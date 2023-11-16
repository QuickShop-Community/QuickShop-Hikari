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
import org.bukkit.*;
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
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
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
     * Execute the Runnable in async thread.
     * If it already on main-thread, will be move to async thread.
     *
     * @param runnable The runnable
     */
    public static void asyncThreadRun(@NotNull Runnable runnable) {
        if (!plugin.getJavaPlugin().isEnabled()) {
            Log.debug(Level.WARNING, "Scheduler not available, executing task on current thread...");
            runnable.run();
            return;
        }
        if (!Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin.getJavaPlugin(), runnable);
        }
    }

    /**
     * Returns true if the given block could be used to make a shop out of.
     *
     * @param b The block to check, Possibly a chest, dispenser, etc.
     * @return True if it can be made into a shop, otherwise false.
     */
    public static boolean canBeShop(@NotNull Block b) {
        if (isBlacklistWorld(b.getWorld())) {
            return false;
        }
        // Specified types by configuration
        if (!isShoppables(b.getType())) {
            return false;
        }
        final BlockState bs = PaperLib.getBlockState(b, false).getState();
        boolean container = bs instanceof InventoryHolder;
        if (!container) {
            if (Util.isDevMode()) {
                Log.debug(b.getType() + " not a container");
            }
            return false;
        }
        return true;
    }

    public static boolean isBlacklistWorld(@NotNull World world) {
        return plugin.getConfig().getStringList("shop.blacklist-world").contains(world.getName());
    }

    /**
     * Check a material is possible become a shop
     *
     * @param material Mat
     * @return Can or not
     */
    public static boolean isShoppables(@NotNull Material material) {
        return SHOPABLES.contains(material);
    }

    /**
     * Counts the number of items in the given inventory where Util.matches(inventory item, item) is
     * true.
     *
     * @param inv  The inventory to search
     * @param item The ItemStack to search for
     * @return The number of items that match in this inventory.
     */
    public static int countItems(@Nullable InventoryWrapper inv, @NotNull ItemStack item) {
        if (inv == null) {
            return 0;
        }
        ItemMatcher matcher = plugin.getItemMatcher();
        if (inv instanceof CountableInventoryWrapper ciw) {

            return ciw.countItem(input -> matcher.matches(item, input));
        } else {
            int items = 0;
            for (final ItemStack iStack : inv) {
                if (iStack == null || iStack.getType() == Material.AIR) {
                    continue;
                }
                if (matcher.matches(item, iStack)) {
                    items += iStack.getAmount();
                }
            }
            return items / item.getAmount();
        }
    }

    /**
     * Counts the number of shop items in the given inventory where Util.matches(inventory item, item) is
     * true.
     *
     * @param inv  The inventory to search
     * @param shop The Shop for matching
     * @return The number of shop items that match in this inventory.
     */
    public static int countItems(@Nullable InventoryWrapper inv, @NotNull Shop shop) {
        if (inv == null) {
            return 0;
        }
        if (inv instanceof CountableInventoryWrapper ciw) {
            return ciw.countItem(shop::matches);
        } else {
            int items = 0;
            for (final ItemStack iStack : inv) {
                if (iStack == null || iStack.getType() == Material.AIR) {
                    continue;
                }
                if (shop.matches(iStack)) {
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
     * @param shop The shop containing item prototype. Material, durabiltiy and enchants must match for 'stackability'
     *             to occur.
     * @return The number of shop items that can be given to the inventory safely.
     */
    public static int countSpace(@Nullable InventoryWrapper inv, @NotNull Shop shop) {
        if (inv == null) {
            return 0;
        }
        if (inv instanceof CountableInventoryWrapper ciw) {
            return ciw.countSpace(shop::matches);
        } else {
            ItemStack item = shop.getItem();
            int space = 0;
            int itemMaxStackSize = getItemMaxStackSize(item.getType());
            for (ItemStack iStack : inv) {
                if (iStack == null || iStack.getType() == Material.AIR) {
                    space += itemMaxStackSize;
                } else if (shop.matches(iStack)) {
                    space += iStack.getAmount() >= itemMaxStackSize ? 0 : itemMaxStackSize - iStack.getAmount();
                }
            }
            return space / item.getAmount();
        }
    }

    /**
     * Returns a material max stacksize
     *
     * @param material Material
     * @return Game StackSize or Custom
     */
    public static int getItemMaxStackSize(@NotNull Material material) {
        return CUSTOM_STACKSIZE.getOrDefault(material, BYPASSED_CUSTOM_STACKSIZE == -1 ? material.getMaxStackSize() : BYPASSED_CUSTOM_STACKSIZE);
    }

    /**
     * Returns the number of items that can be given to the inventory safely.
     *
     * @param inv  The inventory to count
     * @param item The item prototype. Material, durabiltiy and enchants must match for 'stackability'
     *             to occur.
     * @return The number of items that can be given to the inventory safely.
     */
    public static int countSpace(@Nullable InventoryWrapper inv, @NotNull ItemStack item) {
        if (inv == null) {
            return 0;
        }
        ItemMatcher matcher = plugin.getItemMatcher();
        if (inv instanceof CountableInventoryWrapper ciw) {
            return ciw.countSpace(input -> matcher.matches(item, input));
        } else {
            int space = 0;
            int itemMaxStackSize = getItemMaxStackSize(item.getType());
            for (ItemStack iStack : inv) {
                if (iStack == null || iStack.getType() == Material.AIR) {
                    space += itemMaxStackSize;
                } else if (matcher.matches(item, iStack)) {
                    space += iStack.getAmount() >= itemMaxStackSize ? 0 : itemMaxStackSize - iStack.getAmount();
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
    public static void debugLog(@NotNull String... logs) {
        Log.Caller caller = Log.Caller.create();
        for (String log : logs) {
            Log.debug(Level.INFO, log, caller);
        }
    }

    /**
     * Covert YAML string to ItemStack.
     *
     * @param config serialized ItemStack
     * @return ItemStack iStack
     * @throws InvalidConfigurationException when failed deserialize config
     */
    @Nullable
    public static ItemStack deserialize(@NotNull String config) throws InvalidConfigurationException {
        if (yaml == null) {
            DumperOptions yamlOptions = new DumperOptions();
            yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            yamlOptions.setIndent(2);
            yaml = new Yaml(yamlOptions); //Caching it!
        }
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        Map<Object, Object> root = yaml.load(config);
        //noinspection unchecked
        Map<String, Object> item = (Map<String, Object>) root.get("item");
        int itemDataVersion = Integer.parseInt(String.valueOf(item.getOrDefault("v", "0")));
        try {
            // Try load the itemDataVersion to do some checks.
            //noinspection deprecation
            if (itemDataVersion > Bukkit.getUnsafe().getDataVersion()) {
                Log.debug("WARNING: DataVersion not matched with ItemStack: " + config);
                // okay we need some things to do
                if (plugin.getConfig().getBoolean("shop.force-load-downgrade-items.enable")) {
                    // okay it enabled
                    Log.debug("QuickShop is trying force loading " + config);
                    if (plugin.getConfig().getInt("shop.force-load-downgrade-items.method") == 0) { // Mode 0
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
        } catch (Exception e) {
            throw new InvalidConfigurationException("Exception in deserialize item: " + config, e);
        }
    }

    /**
     * Ensure this method is calling from specific thread
     *
     * @param async on async thread or main server thread.
     */
    public static void ensureThread(boolean async) {
        boolean isMainThread = Bukkit.isPrimaryThread();
        if (async) {
            if (isMainThread) {
                throw new IllegalStateException("#[Illegal Access] This method require runs on async thread.");
            }
        } else {
            if (!isMainThread) {
                throw new IllegalStateException("#[Illegal Access] This method require runs on server main thread.");
            }
        }
    }

    /**
     * Check two location is or not equals for the BlockPosition on 2D
     *
     * @param b1 block 1
     * @param b2 block 2
     * @return Equals or not.
     */
    private static boolean equalsBlockStateLocation(@NotNull Location b1, @NotNull Location b2) {
        return (b1.getBlockX() == b2.getBlockX()) && (b1.getBlockY() == b2.getBlockY()) && (b1.getBlockZ() == b2.getBlockZ());
    }

    /**
     * Call a event and check it is cancelled.
     *
     * @param event The event implement the Cancellable interface.
     * @return The event is cancelled.
     */
    public static boolean fireCancellableEvent(@NotNull Cancellable event) {
        if (!(event instanceof Event)) {
            throw new IllegalArgumentException("Cancellable must is event implement");
        }
        Bukkit.getPluginManager().callEvent((Event) event);
        return event.isCancelled();
    }

    /**
     * Get location that converted to block position (.0)
     *
     * @param loc location
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
        QuickShop qs = QuickShop.getInstance();
        if (qs != null) {
            File cache = new File(QuickShop.getInstance().getDataFolder(), "cache");
            if (!cache.exists()) {
                cache.mkdirs();
            }
            return cache;
        } else {
            File file = new File("cache");
            file.mkdirs();
            return file;
        }
    }

    /**
     * Use yaw to calc the BlockFace
     *
     * @param yaw Yaw (Player.getLocation().getYaw())
     * @return BlockFace blockFace
     * @deprecated Use Bukkit util not this one.
     */
    @NotNull
    public static BlockFace getYawFace(float yaw) {
        //noinspection ConstantValue
        if (yaw > 315 && yaw <= 45) {
            return BlockFace.NORTH;
        } else if (yaw > 45 && yaw <= 135) {
            return BlockFace.EAST;
        } else if (yaw > 135 && yaw <= 225) {
            return BlockFace.SOUTH;
        } else {
            return BlockFace.WEST;
        }
    }

    @NotNull
    public static Component getItemStackName(@NotNull ItemStack itemStack) {
        Component result = getItemCustomName(itemStack);
        if (isEmptyComponent(result)) {
            try {
                result = plugin.getPlatform().getTranslation(itemStack);
            } catch (Throwable th) {
                result = MsgUtil.setHandleFailedHover(null, Component.text(itemStack.getType().getKey().toString()));
                plugin.logger().warn("Failed to handle translation for ItemStack {}", Util.serialize(itemStack), th);
            }
        }
        return result;
    }

    @Nullable
    public static Component getItemCustomName(@NotNull ItemStack itemStack) {
        if (useEnchantmentForEnchantedBook() && itemStack.getType() == Material.ENCHANTED_BOOK) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta instanceof EnchantmentStorageMeta enchantmentStorageMeta && enchantmentStorageMeta.hasStoredEnchants()) {
                return getFirstEnchantmentName(enchantmentStorageMeta);
            }
        }
//        if (plugin.getConfig().getBoolean("shop.use-effect-for-potion-item") && itemStack.getType().name().endsWith("POTION")) {
//            ItemMeta meta = itemStack.getItemMeta();
//            if (meta instanceof PotionMeta potionMeta) {
//                PotionData potionData = potionMeta.getBasePotionData();
//                PotionEffectType potionEffectType = potionData.getType().getEffectType();
//                if (potionEffectType != null) {
//                    //Because the bukkit API limit, we can't get the actual effect level
//                    return plugin.getPlatform().getTranslation(potionEffectType);
//                } else if (potionMeta.hasCustomEffects()) {
//                    PotionEffect potionEffect = potionMeta.getCustomEffects().get(0);
//                    if (potionEffect != null) {
//                        int level = potionEffect.getAmplifier();
//                        return plugin.getPlatform().getTranslation(potionEffect.getType()).append(LegacyComponentSerializer.legacySection().deserialize(" " + (level <= 10 ? RomanNumber.toRoman(potionEffect.getAmplifier()) : level)));
//                    }
//                }
//            }
//        }
        if (itemStack.hasItemMeta() && Objects.requireNonNull(itemStack.getItemMeta()).hasDisplayName() && !QuickShop.getInstance().getConfig().getBoolean("shop.force-use-item-original-name")) {
            return plugin.getPlatform().getDisplayName(itemStack.getItemMeta());
        }
        return null;
    }

    public static boolean isEmptyComponent(@Nullable Component component) {
        if (component == null) {
            return true;
        }
        if (component.equals(Component.empty())) {
            return true;
        }
        return component.equals(Component.text(""));
    }

    public static boolean useEnchantmentForEnchantedBook() {
        return plugin.getConfig().getBoolean("shop.use-enchantment-for-enchanted-book");
    }

    @NotNull
    public static Component getFirstEnchantmentName(@NotNull EnchantmentStorageMeta meta) {
        if (!meta.hasStoredEnchants()) {
            throw new IllegalArgumentException("Item does not have an enchantment!");
        }
        Entry<Enchantment, Integer> entry = meta.getStoredEnchants().entrySet().iterator().next();
        Component name;
        try {
            name = plugin.getPlatform().getTranslation(entry.getKey());
        } catch (Throwable throwable) {
            name = MsgUtil.setHandleFailedHover(null, Component.text(entry.getKey().getKey().getKey()));
            plugin.logger().warn("Failed to handle translation for Enchantment {}", entry.getKey().getKey(), throwable);
        }
        if (entry.getValue() == 1 && entry.getKey().getMaxLevel() == 1) {
            return name;
        } else {
            return name.append(Component.text(" " + RomanNumber.toRoman(entry.getValue())));
        }
    }

    public static int getItemTotalAmountsInMap(@NotNull Map<Integer, ItemStack> map) {
        int total = 0;
        for (ItemStack value : map.values()) {
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
        List<String> tabList = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        if (plugin.getConfig().getBoolean("include-offlineplayer-list")) {
            tabList.addAll(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).filter(Objects::nonNull).toList());
        }
        return tabList;
    }

    /**
     * Gets a plugin's Jar file
     *
     * @param plugin The plugin instance
     * @return The plugin's Jar file
     * @throws FileNotFoundException If the plugin's Jar file could not be found
     */
    @NotNull
    public static File getPluginJarFile(@NotNull Plugin plugin) throws FileNotFoundException {
        String path = getPluginJarPath(plugin);
        File file = new File(path);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + path);
        }
        return file;
    }

    /**
     * Get class path of the given class.
     *
     * @param plugin Plugin plugin instance
     * @return Class path
     */
    @NotNull
    public static String getPluginJarPath(@NotNull Plugin plugin) {
        return CommonUtil.getClassPath(plugin.getClass());
    }

    /**
     * Returns the chest attached to the given chest. The given block must be a chest.
     *
     * @param block The chest block
     * @return the block which is also a chest and connected to b.
     */
    public static Block getSecondHalf(@NotNull Block block) {
        BlockData blockData = block.getBlockData();
        if (!(blockData instanceof org.bukkit.block.data.type.Chest chest)) {
            return null;
        }
        if (!isDoubleChest(chest)) {
            return null;
        }
        BlockFace towardsLeft = getRightSide(chest.getFacing());
        BlockFace actuallyBlockFace = chest.getType() == org.bukkit.block.data.type.Chest.Type.LEFT ? towardsLeft : towardsLeft.getOppositeFace();
        return block.getRelative(actuallyBlockFace);
    }

    public static boolean isDoubleChest(@Nullable BlockData blockData) {
        if (!(blockData instanceof org.bukkit.block.data.type.Chest chestBlockData)) {
            return false;
        }
        return chestBlockData.getType() != org.bukkit.block.data.type.Chest.Type.SINGLE;
    }

    /**
     * return the right side for given blockFace
     *
     * @param blockFace given blockFace
     * @return the right side for given blockFace, UP and DOWN will return itself
     */
    @NotNull
    public static BlockFace getRightSide(@NotNull BlockFace blockFace) {
        return switch (blockFace) {
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
     * @return the sender unique id if sender is a player, otherwise nil unique id
     */
    @NotNull
    public static UUID getSenderUniqueId(@Nullable CommandSender sender) {
        if (sender instanceof OfflinePlayer offlinePlayer) {
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
        Material signMaterial = Material.matchMaterial(plugin.getConfig().getString("shop.sign-material", "OAK_WALL_SIGN"));
        if (signMaterial != null) {
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
     * @return The percentage 'health' the tool has. (Opposite of total damage)
     */
    @NotNull
    public static String getToolPercentage(@NotNull ItemStack item) {
        if (!(item.getItemMeta() instanceof Damageable)) {
            Log.debug(item.getType().name() + " not Damageable.");
            return "Error: NaN";
        }
        double dura = ((Damageable) item.getItemMeta()).getDamage();
        double max = item.getType().getMaxDurability();
        DecimalFormat formatter = new DecimalFormat("0");
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
        } catch (NoSuchMethodException e) {
            plugin.logger().error("Failed to register Util initialize method to reload manager.", e);
        }
        SHOPABLES.clear();
        CUSTOM_STACKSIZE.clear();
        devMode = plugin.getConfig().getBoolean("dev-mode");

        for (String s : plugin.getConfig().getStringList("shop-blocks")) {
            Material mat = Material.matchMaterial(s.toUpperCase());
            if (mat == null) {
                mat = Material.matchMaterial(s);
            }
            if (mat == null) {
                plugin.logger().warn("Invalid shop-block: {}", s);
            } else {
                SHOPABLES.add(mat);
            }
        }

        for (String material : plugin.getConfig().getStringList("custom-item-stacksize")) {
            String[] data = material.split(":");
            if (data.length != 2) {
                continue;
            }

            if ("*".equalsIgnoreCase(data[0])) {
                BYPASSED_CUSTOM_STACKSIZE = Integer.parseInt(data[1]);
            }
            Material mat = Material.matchMaterial(data[0]);
            if (mat == null || mat == Material.AIR) {
                plugin.logger().warn("{} not a valid material in custom-item-stacksize section.", material);
                continue;
            }
            CUSTOM_STACKSIZE.put(mat, Integer.parseInt(data[1]));
        }
        try {
            dyeColor = DyeColor.valueOf(plugin.getConfig().getString("shop.sign-dye-color"));
        } catch (Exception ignored) {
        }
    }

    /**
     * Call this to check items in inventory and remove it.
     *
     * @param inv inv
     */
    public static void inventoryCheck(@Nullable InventoryWrapper inv) {
        if (inv == null) {
            return;
        }
        if (inv.getHolder() == null) {
            Log.debug("Skipped plugin gui inventory check.");
            return;
        }
        InventoryWrapperIterator iterator = inv.iterator();
        try {
            while (iterator.hasNext()) {
                ItemStack itemStack = iterator.next();
                if (itemStack == null) {
                    continue;
                }
                if (AbstractDisplayItem.checkIsGuardItemStack(itemStack)) {
                    // Found Item and remove it.
                    Location location = inv.getLocation();
                    if (location == null) {
                        return; // Virtual GUI
                    }
                    iterator.remove();
                    Log.debug("Found shop display item in an inventory" + location + ", Removing...");
                    MsgUtil.sendGlobalAlert(plugin.text().of("inventory-check-global-alert", location, inv.getHolder().getClass().getName(), Util.getItemStackName(itemStack)).forLocale(MsgUtil.getDefaultGameLanguageCode()));
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * @param stack The ItemStack to check if it is blacklisted
     * @return true if the ItemStack is black listed. False if not.
     * @deprecated Use QuickShopAPI#getShopItemBlackList() instead
     */
    @Deprecated(forRemoval = true)
    public static boolean isBlacklisted(@NotNull ItemStack stack) {
        if (plugin == null) {
            throw new IllegalStateException("Plugin not fully started yet");
        }
        if (plugin.getItemMarker() == null) {
            throw new IllegalStateException("Plugin not fully started yet");
        }
        if (plugin.getShopItemBlackList() == null) {
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
        if (devMode != null) {
            return devMode;
        } else {
            if (plugin != null) {
                devMode = plugin.getConfig().getBoolean("dev-mode");
                return devMode;
            } else {
                return false;
            }
        }
    }

    public static boolean isDisplayAllowBlock(@NotNull Material mat) {
        return mat.isTransparent() || isWallSign(mat);
    }

    /**
     * Check a material is or not a WALL_SIGN
     *
     * @param material mat
     * @return is or not a wall_sign
     */
    public static boolean isWallSign(@Nullable Material material) {
        if (material == null) {
            return false;
        }
        return Tag.WALL_SIGNS.isTagged(material);
    }

    /**
     * Get a material is a dye
     *
     * @param material The material
     * @return yes or not
     */
    public static boolean isDyes(@NotNull Material material) {
        return material.name().toUpperCase().endsWith("_DYE");
    }

    /**
     * Returns true if the given location is loaded or not.
     *
     * @param loc The location
     * @return true if the given location is loaded or not.
     */
    public static boolean isLoaded(@NotNull Location loc) {
        if (!loc.isWorldLoaded()) {
            return false;
        }
        // Calculate the chunks coordinates. These are 1,2,3 for each chunk, NOT
        // location rounded to the nearest 16.
        int x = (int) Math.floor((loc.getBlockX()) / 16.0);
        int z = (int) Math.floor((loc.getBlockZ()) / 16.0);
        return (loc.getWorld().isChunkLoaded(x, z));
    }

    /**
     * Get this method available or not
     *
     * @param className class qualifiedName
     * @param method    the name of method
     * @param args      the arg of method
     * @return boolean Available
     */
    public static boolean isMethodAvailable(@NotNull String className, String method, Class<?>... args) {// nosemgrep
        try {
            Class<?> clazz = Class.forName(className);
            try {
                clazz.getDeclaredMethod(method, args);
            } catch (NoSuchMethodException e) {
                clazz.getMethod(method, args);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks whether someone else's shop is within reach of a hopper being placed by a player.
     *
     * @param b The block being placed.
     * @param p The player performing the action.
     * @return true if a nearby shop was found, false otherwise.
     */
    public static boolean isOtherShopWithinHopperReach(@NotNull Block b, @NotNull Player p) {
        Block bshop = Util.getAttached(b);
        if (bshop == null) {
            return false;
        }
        Shop shop = plugin.getShopManager().getShopIncludeAttached(bshop.getLocation());
        if (shop == null) {
            shop = plugin.getShopManager().getShopIncludeAttached(bshop.getLocation().clone().add(0, 1, 0));
        }
        return shop != null && !shop.playerAuthorize(p.getUniqueId(), BuiltInShopPermission.ACCESS_INVENTORY);
    }

    /**
     * Fetches the block which the given sign is attached to
     *
     * @param b The block which is attached
     * @return The block the sign is attached to
     */
    @Nullable
    public static Block getAttached(@NotNull Block b) {
        final BlockData blockData = b.getBlockData();
        if (blockData instanceof final Directional directional) {
            return b.getRelative(directional.getFacing().getOppositeFace());
        } else {
            return null;
        }
    }

    /**
     * @param mat The material to check
     * @return Returns true if the item is a tool (Has durability) or false if it doesn't.
     */
    public static boolean isTool(@NotNull Material mat) {
        return mat.getMaxDurability() != 0;
    }

    /**
     * Returns loc with modified pitch/yaw angles so it faces lookat
     *
     * @param loc    The location a players head is
     * @param lookat The location they should be looking
     * @return The location the player should be facing to have their crosshairs on the location
     * lookAt Kudos to bergerkiller for most of this function
     */
    public static @NotNull Location lookAt(@NotNull Location loc, @NotNull Location lookat) {
        // Clone the loc to prevent applied changes to the input loc
        loc = loc.clone();
        // Values of change in distance (make it relative)
        double dx = lookat.getX() - loc.getX();
        double dy = lookat.getY() - loc.getY();
        double dz = lookat.getZ() - loc.getZ();
        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                loc.setYaw((float) (1.5 * Math.PI));
            } else {
                loc.setYaw((float) (0.5 * Math.PI));
            }
            loc.setYaw(loc.getYaw() - (float) Math.atan(dz / dx));
        } else if (dz < 0) {
            loc.setYaw((float) Math.PI);
        }
        // Get the distance from dx/dz
        double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));
        float pitch = (float) -Math.atan(dy / dxz);
        // Set values, convert to degrees
        // Minecraft yaw (vertical) angles are inverted (negative)
        loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI + 360);
        // But pitch angles are normal
        loc.setPitch(pitch * 180f / (float) Math.PI);
        return loc;
    }

    /**
     * Execute the Runnable in server main thread.
     * If it already on main-thread, will be executed directly.
     * or post to main-thread if came from any other thread.
     *
     * @param runnable The runnable
     */
    public static void mainThreadRun(@NotNull Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTask(plugin.getJavaPlugin(), runnable);
        }
    }

    /**
     * Covert ItemStack to YAML string.
     *
     * @param iStack target ItemStack
     * @return String serialized itemStack
     */
    @NotNull
    public static String serialize(@NotNull ItemStack iStack) {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("item", iStack);
        return cfg.saveToString();
    }

    /**
     * Unregister all listeners registered instances that belong to specified class
     *
     * @param plugin Plugin instance
     * @param clazz  Class to unregister
     */
    public static void unregisterListenerClazz(@NotNull Plugin plugin, @NotNull Class<? extends Listener> clazz) {
        for (RegisteredListener registeredListener : HandlerList.getRegisteredListeners(plugin)) {
            if (registeredListener.getListener().getClass().equals(clazz)) {
                HandlerList.unregisterAll(registeredListener.getListener());
            }
        }
    }

    public static boolean checkIfBungee() {
        if (PackageUtil.parsePackageProperly("forceBungeeCord").asBoolean(false)) {
            return true;
        }
        return Bukkit.getServer().spigot().getConfig().getBoolean("settings.bungeecord");
    }
}
