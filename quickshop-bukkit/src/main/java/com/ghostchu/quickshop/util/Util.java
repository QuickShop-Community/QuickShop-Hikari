/*
 *  This file is a part of project QuickShop, the name is Util.java
 *  Copyright (C) Ghost_chu and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ghostchu.quickshop.util;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.inventory.CountableInventoryWrapper;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperIterator;
import com.ghostchu.quickshop.api.shop.AbstractDisplayItem;
import com.ghostchu.quickshop.api.shop.ItemMatcher;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.logger.Log;
import io.papermc.lib.PaperLib;
import lombok.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.StringUtils;
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
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Util {
    private static final EnumSet<Material> BLACKLIST = EnumSet.noneOf(Material.class);
    private static final EnumMap<Material, Integer> CUSTOM_STACKSIZE = new EnumMap<>(Material.class);
    private static final EnumSet<Material> SHOPABLES = EnumSet.noneOf(Material.class);
    private static final List<BlockFace> VERTICAL_FACING = List.of(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);
    @SuppressWarnings("UnstableApiUsage")
    private static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();
    private static int BYPASSED_CUSTOM_STACKSIZE = -1;
    private static Yaml yaml = null;
    private static Boolean devMode = null;
    @Setter
    private static QuickShop plugin;
    @Getter
    @Nullable
    private static DyeColor dyeColor = null;

    /**
     * Convert strArray to String. E.g "Foo, Bar"
     *
     * @param strArray Target array
     * @return str
     */
    @NotNull
    public static String array2String(@NotNull String[] strArray) {
        StringJoiner joiner = new StringJoiner(", ");
        for (String str : strArray) {
            joiner.add(str);
        }
        return joiner.toString();
    }

    /**
     * Convert boolean to string status
     *
     * @param bool Boolean
     * @return Enabled or Disabled
     */
    @NotNull
    public static String boolean2Status(boolean bool) {
        if (bool) {
            return "Enabled";
        } else {
            return "Disabled";
        }
    }

//    /**
//     * Backup shops.db
//     *
//     * @return The result for backup
//     */
//    // TODO: MySQL support
//    public static boolean backupDatabase() {
//        File dataFolder = plugin.getDataFolder();
//        File sqlfile = new File(dataFolder, "shops.db");
//        if (!sqlfile.exists()) {
//            plugin.getLogger().warning("Failed to backup! (File not found)");
//            return false;
//        }
//        String uuid = UUID.randomUUID().toString().replaceAll("_", "");
//        File bksqlfile = new File(dataFolder, "/shops_backup_" + uuid + ".db");
//        try {
//            Files.copy(sqlfile.toPath(), bksqlfile.toPath());
//        } catch (Exception e1) {
//            plugin.getLogger().log(Level.WARNING, "Failed to backup the database", e1);
//            return false;
//        }
//        return true;
//    }

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
        return bs instanceof InventoryHolder;
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

    public static boolean isBlacklistWorld(@NotNull World world) {
        return plugin.getConfig().getStringList("shop.blacklist-world").contains(world.getName());
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
     * Returns a material max stacksize
     *
     * @param material Material
     * @return Game StackSize or Custom
     */
    public static int getItemMaxStackSize(@NotNull Material material) {
        return CUSTOM_STACKSIZE.getOrDefault(material, BYPASSED_CUSTOM_STACKSIZE == -1 ? material.getMaxStackSize() : BYPASSED_CUSTOM_STACKSIZE);
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
                    plugin
                            .getLogger()
                            .warning(
                                    "Cannot load ItemStack "
                                            + config
                                            + " because it saved from higher Minecraft server version, the action will fail and you will receive a exception, PLELASE DON'T REPORT TO QUICKSHOP!");
                    plugin
                            .getLogger()
                            .warning(
                                    "You can try force load this ItemStack by our hacked ItemStack read util(shop.force-load-downgrade-items), but beware, the data may corrupt if you load on this lower Minecraft server version, Please backup your world and database before enable!");
                }
            }
            yamlConfiguration.loadFromString(config);
            return yamlConfiguration.getItemStack("item");
        } catch (Exception e) {
            throw new InvalidConfigurationException("Exception in deserialize item: " + config, e);
        }
    }


    private static final StackWalker stackWalker = StackWalker.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE), 2);

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
//        if (disableDebugLogger) {
//            return;
//        }
//        StringJoiner logEntry = new StringJoiner("\n");
//        if (!isDevMode()) {
//            for (String log : logs) {
//                logEntry.add("[DEBUG] " + log);
//            }
//        } else {
//            List<StackWalker.StackFrame> caller = stackWalker.walk(frames -> frames.limit(2).toList());
//            StackWalker.StackFrame frame = caller.get(1);
//            String threadName = Thread.currentThread().getName();
//            String className = frame.getClassName();
//            String methodName = frame.getMethodName();
//            int codeLine = frame.getLineNumber();
//            for (String log : logs) {
//                logEntry.add("[DEBUG/" + threadName + "] [" + className + "] [" + methodName + "] (" + codeLine + ") " + log);
//            }
//        }
//        String log = logEntry.toString();
//        if (isDevMode()) {
//            Objects.requireNonNullElseGet(plugin, QuickShop::getInstance).getLogger().info(log);
//        }
//        LOCK.writeLock().lock();
//        DEBUG_LOGS.add(log);
//        LOCK.writeLock().unlock();
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
     * Get vertical BlockFace list
     *
     * @return vertical BlockFace list (unmodifiable)
     */
    @NotNull
    public static List<BlockFace> getVerticalFacing() {
        return VERTICAL_FACING;
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
     * Return the Class name.
     *
     * @param c The class to get name
     * @return The class prefix
     */
    @NotNull
    public static String getClassPrefix(@NotNull Class<?> c) {
        String callClassName = Thread.currentThread().getStackTrace()[2].getClassName();
        String customClassName = c.getSimpleName();
        return "[" + callClassName + "-" + customClassName + "] ";
    }

    public static boolean useEnchantmentForEnchantedBook() {
        return plugin.getConfig().getBoolean("shop.use-enchantment-for-enchanted-book");
    }

    @Nullable
    public static Component getItemCustomName(@NotNull ItemStack itemStack) {
        if (useEnchantmentForEnchantedBook() && itemStack.getType() == Material.ENCHANTED_BOOK) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta instanceof EnchantmentStorageMeta enchantmentStorageMeta && enchantmentStorageMeta.hasStoredEnchants()) {
                return getFirstEnchantmentName(enchantmentStorageMeta);
            }
        }
        if (plugin.getConfig().getBoolean("shop.use-effect-for-potion-item") && itemStack.getType().name().endsWith("POTION")) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta instanceof PotionMeta potionMeta) {
                PotionData potionData = potionMeta.getBasePotionData();
                PotionEffectType potionEffectType = potionData.getType().getEffectType();
                if (potionEffectType != null) {
                    //Because the bukkit API limit, we can't get the actual effect level
                    return plugin.getPlatform().getTranslation(potionEffectType);
                } else if (potionMeta.hasCustomEffects()) {
                    PotionEffect potionEffect = potionMeta.getCustomEffects().get(0);
                    if (potionEffect != null) {
                        int level = potionEffect.getAmplifier();
                        return plugin.getPlatform().getTranslation(potionEffect.getType()).append(LegacyComponentSerializer.legacySection().deserialize(" " + (level <= 10 ? RomanNumber.toRoman(potionEffect.getAmplifier()) : level)));
                    }
                }
            }
        }
        if (itemStack.hasItemMeta()
                && Objects.requireNonNull(itemStack.getItemMeta()).hasDisplayName()
                && !QuickShop.getInstance().getConfig().getBoolean("shop.force-use-item-original-name")) {
            return plugin.getPlatform().getDisplayName(itemStack.getItemMeta());
        }
        return null;
    }

    @NotNull
    public static Component getItemStackName(@NotNull ItemStack itemStack) {
        Component result = getItemCustomName(itemStack);
        return isEmptyComponent(result) ? plugin.getPlatform().getTranslation(itemStack.getType()) : result;
    }

    @NotNull
    public static Component getFirstEnchantmentName(@NotNull EnchantmentStorageMeta meta) {
        if (!meta.hasStoredEnchants()) {
            throw new IllegalArgumentException("Item does not have an enchantment!");
        }
        Entry<Enchantment, Integer> entry = meta.getStoredEnchants().entrySet().iterator().next();
        Component name = plugin.getPlatform().getTranslation(entry.getKey());
        if (entry.getValue() == 1 && entry.getKey().getMaxLevel() == 1) {
            return name;
        } else {
            return name.append(LegacyComponentSerializer.legacySection().deserialize(" " + RomanNumber.toRoman(entry.getValue())));
        }
    }

    public static boolean isDoubleChest(@Nullable BlockData blockData) {
        if (!(blockData instanceof org.bukkit.block.data.type.Chest chestBlockData)) {
            return false;
        }
        return chestBlockData.getType() != org.bukkit.block.data.type.Chest.Type.SINGLE;
    }

//    /**
//     * Use yaw to calc the BlockFace
//     *
//     * @param yaw Yaw (Player.getLocation().getYaw())
//     * @return BlockFace blockFace
//     * @deprecated Use Bukkit util not this one.
//     */
//    @Deprecated
//    @NotNull
//    public static BlockFace getYawFace(float yaw) {
//        if (yaw > 315 && yaw <= 45) {
//            return BlockFace.NORTH;
//        } else if (yaw > 45 && yaw <= 135) {
//            return BlockFace.EAST;
//        } else if (yaw > 135 && yaw <= 225) {
//            return BlockFace.SOUTH;
//        } else {
//            return BlockFace.WEST;
//        }
//    }

    /**
     * Get how many shop in the target world.
     *
     * @param worldName Target world.
     * @return The shops.
     */
    public static int getShopsInWorld(@NotNull String worldName) {
        int cost = 0;
        Iterator<Shop> iterator = plugin.getShopManager().getShopIterator();
        while (iterator.hasNext()) {
            Shop shop = iterator.next();
            if (Objects.requireNonNull(shop.getLocation().getWorld()).getName().equals(worldName)) {
                cost++;
            }
        }
        return cost;
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
     * Initialize the Util tools.
     */
    public static void initialize() {
        plugin = QuickShop.getInstance();
        try {
            plugin.getReloadManager().register(Util.class.getDeclaredMethod("initialize"));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        BLACKLIST.clear();
        SHOPABLES.clear();
        // RESTRICTED_PRICES.clear();
        CUSTOM_STACKSIZE.clear();
        devMode = plugin.getConfig().getBoolean("dev-mode");

        for (String s : plugin.getConfig().getStringList("shop-blocks")) {
            Material mat = Material.matchMaterial(s.toUpperCase());
            if (mat == null) {
                mat = Material.matchMaterial(s);
            }
            if (mat == null) {
                plugin.getLogger().warning("Invalid shop-block: " + s);
            } else {
                SHOPABLES.add(mat);
            }
        }
        List<String> configBlacklist = plugin.getConfig().getStringList("blacklist");
        for (String s : configBlacklist) {
            Material mat = Material.getMaterial(s.toUpperCase());
            if (mat == null) {
                mat = Material.matchMaterial(s);
            }
            if (mat == null) {
                plugin.getLogger().warning(s + " is not a valid material.  Check your spelling or ID");
                continue;
            }
            BLACKLIST.add(mat);
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
                plugin.getLogger().warning(material + " not a valid material type in custom-item-stacksize section.");
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
     * Read the InputStream to the byte array.
     *
     * @param filePath Target file
     * @return Byte array
     */
    public static byte[] inputStream2ByteArray(@NotNull String filePath) {
        try (InputStream in = new FileInputStream(filePath)) {
            return toByteArray(in);
        } catch (IOException e) {
            return new byte[0];
        }
    }

    private static byte[] toByteArray(@NotNull InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int n;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
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
                    Log.debug("Found shop display item in an inventory, Removing...");
                    MsgUtil.sendGlobalAlert("[InventoryCheck] Found displayItem in inventory at " + location + ", Item is " + itemStack.getType().name());
                }
            }
        } catch (Exception ignored) {
        }
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

    /**
     * @param stack The ItemStack to check if it is blacklisted
     * @return true if the ItemStack is black listed. False if not.
     */
    public static boolean isBlacklisted(@NotNull ItemStack stack) {
        if (BLACKLIST.contains(stack.getType())) {
            return true;
        }
        if (!stack.hasItemMeta()) {
            return false;
        }
        if (!Objects.requireNonNull(stack.getItemMeta()).hasLore()) {
            return false;
        }
        for (String lore : Objects.requireNonNull(stack.getItemMeta().getLore())) {
            List<String> blacklistLores = plugin.getConfig().getStringList("shop.blacklist-lores");
            for (String blacklistLore : blacklistLores) {
                if (lore.contains(blacklistLore)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get this class available or not
     *
     * @param qualifiedName class qualifiedName
     * @return boolean Available
     */
    public static boolean isClassAvailable(@NotNull String qualifiedName) {
        try {
            Class.forName(qualifiedName);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
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
        return shop != null && !shop.getModerator().isModerator(p.getUniqueId());
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

    /**
     * Check two location is or not equals for the BlockPosition on 2D
     *
     * @param b1 block 1
     * @param b2 block 2
     * @return Equals or not.
     */
    private static boolean equalsBlockStateLocation(@NotNull Location b1, @NotNull Location b2) {
        return (b1.getBlockX() == b2.getBlockX())
                && (b1.getBlockY() == b2.getBlockY())
                && (b1.getBlockZ() == b2.getBlockZ());
    }

    /**
     * @param mat The material to check
     * @return Returns true if the item is a tool (Has durability) or false if it doesn't.
     */
    public static boolean isTool(@NotNull Material mat) {
        return mat.getMaxDurability() != 0;
    }

    /**
     * Check a string is or not a UUID string
     *
     * @param string Target string
     * @return is UUID
     */
    public static boolean isUUID(@NotNull String string) {
        final int length = string.length();
        if (length != 36 && length != 32) {
            return false;
        }
        final String[] components = string.split("-");
        return components.length == 5;
    }

    /**
     * Convert strList to String. E.g "Foo, Bar"
     *
     * @param strList Target list
     * @return str
     */
    @NotNull
    public static String list2String(@NotNull Collection<String> strList) {
        return String.join(", ", strList);
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
     * Converts a name like IRON_INGOT into Iron Ingot to improve readability
     *
     * @param ugly The string such as IRON_INGOT
     * @return A nicer version, such as Iron Ingot
     */
    @NotNull
    public static String prettifyText(@NotNull String ugly) {
        String[] nameParts = ugly.split("_");
        if (nameParts.length == 1) {
            return firstUppercase(ugly);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nameParts.length; i++) {
            if (!nameParts[i].isEmpty()) {
                sb.append(Character.toUpperCase(nameParts[i].charAt(0))).append(nameParts[i].substring(1).toLowerCase());
            }
            if (i + 1 != nameParts.length) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }

    /**
     * First uppercase for every words the first char for a text.
     *
     * @param string text
     * @return Processed text.
     */
    @NotNull
    public static String firstUppercase(@NotNull String string) {
        if (string.length() > 1) {
            return Character.toUpperCase(string.charAt(0)) + string.substring(1).toLowerCase();
        } else {
            return string.toUpperCase();
        }
    }

    /**
     * Read the file to the String
     *
     * @param fileName Target file.
     * @return Target file's content.
     */
    @NotNull
    public static String readToString(@NotNull String fileName) {
        File file = new File(fileName);
        return readToString(file);
    }

    /**
     * Read the file to the String
     *
     * @param file Target file.
     * @return Target file's content.
     */
    @NotNull
    public static String readToString(@NotNull File file) {
        byte[] filecontent = new byte[(int) file.length()];
        try (FileInputStream in = new FileInputStream(file)) {
            in.read(filecontent);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to read file: " + file, e);
        }
        return new String(filecontent, StandardCharsets.UTF_8);
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
     * Return the Class name.
     *
     * @return The class prefix
     */
    @NotNull
    public static String getClassPrefix() {
        String className = Thread.currentThread().getStackTrace()[2].getClassName();
        try {
            Class<?> c = Class.forName(className);
            className = c.getSimpleName();
            if (!c.getSimpleName().isEmpty()) {
                className = c.getSimpleName();
            }
        } catch (ClassNotFoundException ignored) {
        }
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        return "[" + className + "-" + methodName + "] ";
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

    @SneakyThrows
    public static void makeExportBackup(@Nullable String backupName) {
        if (StringUtils.isEmpty(backupName)) {
            backupName = "export.txt";
        }
        File file = new File(plugin.getDataFolder(), backupName + ".txt");
        if (file.exists()) {
            Files.move(file.toPath(), new File(file.getParentFile(), file.getName() + UUID.randomUUID().toString().replace("-", "")).toPath());
        }
        file.createNewFile();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            StringBuilder finalReport = new StringBuilder();
            plugin.getShopLoader()
                    .getOriginShopsInDatabase()
                    .forEach((shop -> finalReport.append(shop).append("\n")));
            try (BufferedWriter outputStream = new BufferedWriter(new FileWriter(file, false))) {
                outputStream.write(finalReport.toString());
            } catch (IOException exception) {
                plugin.getLogger().log(Level.WARNING, "Backup failed", exception);
            }

        });
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
     * Getting startup flags
     *
     * @return Java startup flags without some JVM args
     */
    public static List<String> getStartupFlags() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments();
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
        //F  return devMode != null ? devMode : (devMode = plugin.getConfig().getBoolean("dev-mode"));
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
     * Get QuickShop caching folder
     *
     * @return The caching folder
     */
    public static File getCacheFolder() {
        QuickShop qs = QuickShop.getInstance();
        //noinspection ConstantConditions
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
     * Return the player names based on the configuration
     *
     * @return the player names
     */
    @NotNull
    public static List<String> getPlayerList() {
        List<String> tabList = plugin.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        if (plugin.getConfig().getBoolean("include-offlineplayer-list")) {
            tabList.addAll(Arrays.stream(plugin.getServer().getOfflinePlayers()).map(OfflinePlayer::getName).filter(Objects::nonNull).toList());
        }
        return tabList;
    }

    /**
     * Merge args array to a String object with space
     *
     * @param args Args
     * @return String object
     */
    @NotNull
    public static String mergeArgs(@NotNull String[] args) {
        StringJoiner joiner = new StringJoiner(" ", "", "");
        for (String arg : args) {
            joiner.add(arg);
        }
        return joiner.toString();
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
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    /**
     * Convert timestamp to LocalDateTime instance
     *
     * @param timestamp Timestamp
     * @return LocalDateTime instance
     */
    // http://www.java2s.com/Tutorials/Java/Data_Type_How_to/Date_Convert/Convert_long_type_timestamp_to_LocalDate_and_LocalDateTime.htm
    @Nullable
    public static LocalDateTime getDateTimeFromTimestamp(long timestamp) {
        if (timestamp == 0) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), TimeZone
                .getDefault().toZoneId());
    }

    /**
     * Convert timestamp to LocalDate instance
     *
     * @param timestamp Timestamp
     * @return LocalDate instance
     */
    // http://www.java2s.com/Tutorials/Java/Data_Type_How_to/Date_Convert/Convert_long_type_timestamp_to_LocalDate_and_LocalDateTime.htm
    @Nullable
    public static LocalDate getDateFromTimestamp(long timestamp) {
        LocalDateTime date = getDateTimeFromTimestamp(timestamp);
        return date == null ? null : date.toLocalDate();
    }

    /**
     * Gets the nil unique id
     *
     * @return uuid which content is `00000000-0000-0000-0000-000000000000`
     */
    @NotNull
    public static UUID getNilUniqueId() {
        return new UUID(0, 0);
    }

    /**
     * Gets the CommandSender unique id.
     *
     * @param sender the sender
     * @return the sender unique id if sender is a player, otherwise nil unique id
     */
    @NotNull
    public static UUID getSenderUniqueId(@Nullable CommandSender sender) {
        if (sender instanceof OfflinePlayer) {
            return ((OfflinePlayer) sender).getUniqueId();
        }
        return getNilUniqueId();
    }

    /**
     * Create regex from glob
     *
     * @param glob glob
     * @return regex
     */
    // https://stackoverflow.com/questions/45321050/java-string-matching-with-wildcards
    @NotNull
    public static String createRegexFromGlob(@NotNull String glob) {
        StringBuilder out = new StringBuilder("^");
        for (int i = 0; i < glob.length(); ++i) {
            final char c = glob.charAt(i);
            switch (c) {
                case '*' -> out.append(".*");
                case '?' -> out.append('.');
                case '.' -> out.append("\\.");
                case '\\' -> out.append("\\\\");
                default -> out.append(c);
            }
        }
        out.append('$');
        return out.toString();
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
     * Matches the given lists but disordered.
     *
     * @param list1 List1
     * @param list2 List2
     * @return Lists matches or not
     */
    public static boolean listDisorderMatches(@NotNull Collection<?> list1, @NotNull Collection<?> list2) {
        return list1.containsAll(list2) && list2.containsAll(list1);
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

    /**
     * Gets the location of a class inside of a jar file.
     *
     * @param clazz The class to get the location of.
     * @return The jar path which given class at.
     */
    @NotNull
    public static String getClassPath(@NotNull Class<?> clazz) {
        String jarPath = clazz.getProtectionDomain().getCodeSource().getLocation().getFile();
        jarPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8);
        return jarPath;
    }

    /**
     * Get class path of the given class.
     *
     * @param plugin Plugin plugin instance
     * @return Class path
     */
    @NotNull
    public static String getPluginJarPath(@NotNull Plugin plugin) {
        return getClassPath(plugin.getClass());
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
        if (!file.exists())
            throw new FileNotFoundException("File not found: " + path);
        return file;
    }

    /**
     * Create a list which only contains the given elements at the tail of a list.
     *
     * @param list List
     * @param last The amount of elements from list tail to be added to the new list
     * @return The new list
     */
    @NotNull
    public static List<String> tail(@NotNull List<String> list, int last) {
        return list.subList(Math.max(list.size() - last, 0), list.size());
    }

    /**
     * Parse the given name with package.class prefix (all-lowercases) from property
     *
     * @param name name
     * @return ParseResult
     */
    @NotNull
    public static SysPropertiesParseResult parsePackageProperly(@NotNull String name) {
        Log.Caller caller = Log.Caller.create();
        String str = caller.getClassName() + "." + name;
        String value = System.getProperty(str);
        SysPropertiesParseResult result = new SysPropertiesParseResult(value);
        Log.debug("Parsing the system properly for " + str + ": " + result);
        return result;
    }

    @Data
    @AllArgsConstructor
    public static class SysPropertiesParseResult {
        private final String value;

        public boolean isPresent() {
            return value != null;
        }

        public boolean asBoolean() {
            return Boolean.parseBoolean(value);
        }

        public int asInteger(int def) {
            if (value == null) return def;
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException exception) {
                return def;
            }
        }

        public double asDouble(double def) {
            if (value == null) return def;
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException exception) {
                return def;
            }
        }

        public byte asByte(byte def) {
            if (value == null) return def;
            try {
                return Byte.parseByte(value);
            } catch (NumberFormatException exception) {
                return def;
            }
        }


        @Nullable
        public String asString(@NotNull String def) {
            if (value == null) return def;
            return value;
        }

        public long asLong(long def) {
            if (value == null) return def;
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException exception) {
                return def;
            }
        }

        public short asShort(short def) {
            if (value == null) return def;
            try {
                return Short.parseShort(value);
            } catch (NumberFormatException exception) {
                return def;
            }
        }
    }

}
