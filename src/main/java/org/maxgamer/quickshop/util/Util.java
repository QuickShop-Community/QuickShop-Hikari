/*
 * This file is a part of project QuickShop, the name is Util.java
 *  Copyright (C) PotatoCraft Studio and contributors
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

package org.maxgamer.quickshop.util;

import com.google.common.collect.EvictingQueue;
import de.themoep.minedown.MineDown;
import de.themoep.minedown.MineDownParser;
import io.papermc.lib.PaperLib;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.EnderChest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.shop.AbstractDisplayItem;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.api.shop.inventory.InventoryWrapper;
import org.maxgamer.quickshop.database.MySQLCore;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static org.maxgamer.quickshop.chat.platform.minedown.BungeeQuickChat.fromLegacyText;
import static org.maxgamer.quickshop.chat.platform.minedown.BungeeQuickChat.toLegacyText;

public class Util {
    private static final EnumSet<Material> BLACKLIST = EnumSet.noneOf(Material.class);
    private static final EnumMap<Material, Entry<Double, Double>> RESTRICTED_PRICES = new EnumMap<>(Material.class);
    private static final EnumMap<Material, Integer> CUSTOM_STACKSIZE = new EnumMap<>(Material.class);
    private static final EnumSet<Material> SHOPABLES = EnumSet.noneOf(Material.class);
    private static final List<BlockFace> VERTICAL_FACING = Collections.unmodifiableList(Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST));
    @SuppressWarnings("UnstableApiUsage")
    private static final EvictingQueue<String> DEBUG_LOGS = EvictingQueue.create(500);
    private static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();

    private static final ThreadLocal<MineDown> MINEDOWN = ThreadLocal.withInitial(() -> new MineDown(""));
    private static int BYPASSED_CUSTOM_STACKSIZE = -1;
    private static Yaml yaml = null;
    private volatile static Boolean devMode = null;
    @Setter
    private static QuickShop plugin;
    @Getter
    private static boolean disableDebugLogger = false;
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

    /**
     * Backup shops.db
     *
     * @return The result for backup
     */
    // TODO: MySQL support
    public static boolean backupDatabase() {
        if (plugin.getDatabaseManager().getDatabase() instanceof MySQLCore) {
            return true; // Backup and logs by MySQL
        }
        File dataFolder = plugin.getDataFolder();
        File sqlfile = new File(dataFolder, "shops.db");
        if (!sqlfile.exists()) {
            plugin.getLogger().warning("Failed to backup! (File not found)");
            return false;
        }
        String uuid = UUID.randomUUID().toString().replaceAll("_", "");
        File bksqlfile = new File(dataFolder, "/shops_backup_" + uuid + ".db");
        try {
            Files.copy(sqlfile.toPath(), bksqlfile.toPath());
        } catch (Exception e1) {
            plugin.getLogger().log(Level.WARNING, "Failed to backup the database", e1);
            return false;
        }
        return true;
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
        if (bs instanceof EnderChest) { // BlockState for Mod supporting
            return plugin.getOpenInvPlugin() != null;
        }
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
    @ApiStatus.Experimental
    public static int countItems(@Nullable InventoryWrapper inv, @NotNull ItemStack item) {
        if (inv == null) {
            return 0;
        }
        int items = 0;
        for (final ItemStack iStack : inv.getStorageContents()) {
            //noinspection ConstantConditions
            if (iStack == null || iStack.getType() == Material.AIR) {
                continue;
            }
            if (plugin.getItemMatcher().matches(item, iStack)) {
                items += iStack.getAmount();
            }
        }
        return items / item.getAmount();
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
        int items = 0;
        for (final ItemStack iStack : inv.getStorageContents()) {
            //noinspection ConstantConditions
            if (iStack == null || iStack.getType() == Material.AIR) {
                continue;
            }
            if (shop.matches(iStack)) {
                items += iStack.getAmount();
            }
        }
        return items / shop.getItem().getAmount();
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
        ItemStack item = shop.getItem();
        int space = 0;
        int itemMaxStackSize = getItemMaxStackSize(item.getType());
        ItemStack[] contents = inv.getStorageContents();
        for (ItemStack iStack : contents) {
            if (iStack == null || iStack.getType() == Material.AIR) {
                space += itemMaxStackSize;
            } else if (shop.matches(iStack)) {
                space += iStack.getAmount() >= itemMaxStackSize ? 0 : itemMaxStackSize - iStack.getAmount();
            }
        }
        return space / item.getAmount();
    }

    /**
     * Returns the number of items that can be given to the inventory safely.
     *
     * @param inv  The inventory to count
     * @param item The item prototype. Material, durabiltiy and enchants must match for 'stackability'
     *             to occur.
     * @return The number of items that can be given to the inventory safely.
     */
    @ApiStatus.Experimental
    public static int countSpace(@Nullable InventoryWrapper inv, @NotNull ItemStack item) {
        if (inv == null) {
            return 0;
        }
        int space = 0;
        int itemMaxStackSize = getItemMaxStackSize(item.getType());
        ItemStack[] contents = inv.getStorageContents();
        for (ItemStack iStack : contents) {
            if (iStack == null || iStack.getType() == Material.AIR) {
                space += itemMaxStackSize;
            } else if (plugin.getItemMatcher().matches(item, iStack)) {
                space += iStack.getAmount() >= itemMaxStackSize ? 0 : itemMaxStackSize - iStack.getAmount();
            }
        }
        return space / item.getAmount();
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
                Util.debugLog("WARNING: DataVersion not matched with ItemStack: " + config);
                // okay we need some things to do
                if (plugin.getConfig().getBoolean("shop.force-load-downgrade-items.enable")) {
                    // okay it enabled
                    Util.debugLog("QuickShop is trying force loading " + config);
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
                    Util.debugLog("Updated, we will try load as hacked ItemStack: " + config);
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
            throw new InvalidConfigurationException("Exception in deserialize item", e);
        }
    }

    @NotNull
    public static List<String> getDebugLogs() {
        LOCK.readLock().lock();
        List<String> strings = new ArrayList<>(DEBUG_LOGS);
        LOCK.readLock().unlock();
        return strings;
    }

    /**
     * Print debug log when plugin running on dev mode.
     *
     * @param logs logs
     */
    public static void debugLog(@NotNull String... logs) {
        if (disableDebugLogger) {
            return;
        }
        LOCK.writeLock().lock();
        if (!isDevMode()) {
            for (String log : logs) {
                DEBUG_LOGS.add("[DEBUG] " + log);
            }
            LOCK.writeLock().unlock();
            return;
        }
        final StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        final String className = stackTraceElement.getClassName();
        final String methodName = stackTraceElement.getMethodName();
        final int codeLine = stackTraceElement.getLineNumber();
        for (String log : logs) {
            DEBUG_LOGS.add("[DEBUG] [" + className + "] [" + methodName + "] (" + codeLine + ") " + log);
            QuickShop.getInstance().getLogger().info("[DEBUG] [" + className + "] [" + methodName + "] (" + codeLine + ") " + log);
        }
        LOCK.writeLock().unlock();
    }


    /**
     * return the right side for given blockFace
     *
     * @param blockFace given blockFace
     * @return the right side for given blockFace, UP and DOWN will return itself
     */
    @NotNull
    public static BlockFace getRightSide(@NotNull BlockFace blockFace) {
        switch (blockFace) {
            case EAST:
                return BlockFace.SOUTH;
            case NORTH:
                return BlockFace.EAST;
            case SOUTH:
                return BlockFace.WEST;
            case WEST:
                return BlockFace.NORTH;
            default:
                return blockFace;
        }
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
        if (blockData instanceof Directional) {
            final Directional directional = (Directional) blockData;
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

    @NotNull
    public static BaseComponent[] getTranslateComponentForItem(ItemStack stack) {
        String result = ReflectFactory.getMaterialMinecraftNamespacedKey(stack.getType());
        return result == null ? fromLegacyText(getItemStackName(stack)) : new BaseComponent[]{new TranslatableComponent(result)};
    }

    @NotNull
    public static BaseComponent[] getTranslateComponentForMaterial(Material material) {
        String result = ReflectFactory.getMaterialMinecraftNamespacedKey(material);
        return result == null ? fromLegacyText(MsgUtil.getItemi18n(material.name())) : new BaseComponent[]{new TranslatableComponent(result)};
    }

    @Nullable
    public static String getItemCustomName(@NotNull ItemStack itemStack) {
        if (useEnchantmentForEnchantedBook() && itemStack.getType() == Material.ENCHANTED_BOOK) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta instanceof EnchantmentStorageMeta && ((EnchantmentStorageMeta) meta).hasStoredEnchants()) {
                return getFirstEnchantmentName((EnchantmentStorageMeta) meta);
            }
        }
        if (itemStack.hasItemMeta()
                && Objects.requireNonNull(itemStack.getItemMeta()).hasDisplayName()
                && !QuickShop.getInstance().getConfig().getBoolean("shop.force-use-item-original-name")) {
            return itemStack.getItemMeta().getDisplayName();
        }
        return null;
    }

    @NotNull
    public static String getItemStackName(@NotNull ItemStack itemStack) {
        String result = getItemCustomName(itemStack);
        return result == null ? MsgUtil.getItemi18n(itemStack.getType().name()) : result;
    }

    @NotNull
    public static String getFirstEnchantmentName(@NotNull EnchantmentStorageMeta meta) {
        if (!meta.hasStoredEnchants()) {
            throw new IllegalArgumentException("Item does not have an enchantment!");
        }
        Map.Entry<Enchantment, Integer> entry = meta.getStoredEnchants().entrySet().iterator().next();
        String name = MsgUtil.getEnchi18n(entry.getKey());
        if (entry.getValue() == 1 && entry.getKey().getMaxLevel() == 1) {
            return name;
        } else {
            return name + " " + RomanNumber.toRoman(entry.getValue());
        }
    }

    /**
     * Get ItemStack's local name, return null if failed to get.
     *
     * @param itemStack Target ItemStack
     * @return LocalName or NULL
     */
    @Nullable
    public static String getLocalizedName(@NotNull ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return null;
        }
        if (!itemMeta.hasLocalizedName()) {
            return null;
        }
        return itemMeta.getLocalizedName();
    }

    /**
     * Return an entry with min and max prices, but null if there isn't a price restriction
     *
     * @param material mat
     * @return min, max
     */
    @Nullable
    public static Entry<Double, Double> getPriceRestriction(@NotNull Material material) {
        return RESTRICTED_PRICES.get(material);
    }

    public static boolean isDoubleChest(@Nullable BlockData blockData) {
        if (!(blockData instanceof org.bukkit.block.data.type.Chest)) {
            return false;
        }
        org.bukkit.block.data.type.Chest chestBlockData = (org.bukkit.block.data.type.Chest) blockData;
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
            Util.debugLog(item.getType().name() + " not Damageable.");
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
        RESTRICTED_PRICES.clear();
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

        for (String s : plugin.getConfig().getStringList("shop.price-restriction")) {
            String[] sp = s.split(";");
            if (sp.length == 3) {
                try {
                    Material mat = Material.matchMaterial(sp[0]);
                    if (mat == null) {
                        plugin.getLogger().warning("Material " + sp[0] + " in config.yml can't match with a valid Materials, check your config.yml!");
                        continue;
                    }
                    RESTRICTED_PRICES.put(mat, new SimpleEntry<>(Double.valueOf(sp[1]), Double.valueOf(sp[2])));
                } catch (Exception e) {
                    plugin.getLogger().warning("Invalid price restricted material: " + s);
                }
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
                plugin.getLogger().warning(material + " not a valid material type in custom-item-stacksize section.");
                continue;
            }
            CUSTOM_STACKSIZE.put(mat, Integer.parseInt(data[1]));
        }
        disableDebugLogger = plugin.getConfig().getBoolean("debug.disable-debuglogger", false);
        try {
            dyeColor = DyeColor.valueOf(plugin.getConfig().getString("shop.sign-dye-color"));
        } catch (Exception ignored) {
        }

        InteractUtil.init(plugin.getConfig().getConfigurationSection("shop.interact"));
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
            return null;
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
     * Read the InputStream to the byte array.
     *
     * @param inputStream Target stream
     * @return Byte array
     */
    public static byte[] inputStream2ByteArray(@NotNull InputStream inputStream) {
        try {
            byte[] data = toByteArray(inputStream);
            inputStream.close();
            return data;
        } catch (IOException e) {
            return null;
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
            Util.debugLog("Skipped plugin gui inventory check.");
            return;
        }
        try {
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack itemStack = inv.getItem(i);
                if (itemStack == null) {
                    continue;
                }
                if (AbstractDisplayItem.checkIsGuardItemStack(itemStack)) {
                    // Found Item and remove it.
                    Location location = inv.getLocation();
                    if (location == null) {
                        return; // Virtual GUI
                    }
                    inv.setItem(i, new ItemStack(Material.AIR));
                    Util.debugLog("Found shop display item in an inventory, Removing...");
                    MsgUtil.sendGlobalAlert("[InventoryCheck] Found displayItem in inventory at " + location + ", Item is " + itemStack.getType().name());
                }
            }
        } catch (Exception ignored) {
        }
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
        if (!(blockData instanceof org.bukkit.block.data.type.Chest)) {
            return null;
        }
        org.bukkit.block.data.type.Chest chest = (org.bukkit.block.data.type.Chest) blockData;
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
    public static String list2String(@NotNull List<String> strList) {
        StringJoiner joiner = new StringJoiner(", ", "", "");
        strList.forEach(joiner::add);
        return joiner.toString();
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
     * Parse colors for the YamlConfiguration.
     *
     * @param config yaml config
     */
    @Deprecated
    public static void parseColours(@NotNull YamlConfiguration config) {
        parseColours((ConfigurationSection) config);
    }

    /**
     * Parse colors for the YamlConfiguration.
     *
     * @param config yaml config
     */
    public static void parseColours(@NotNull ConfigurationSection config) {
        Set<String> keys = config.getKeys(true);
        for (String key : keys) {
            String filtered = config.getString(key);
            if (filtered == null) {
                continue;
            }
            if (filtered.startsWith("MemorySection")) {
                continue;
            }
            filtered = parseColours(filtered);
            config.set(key, filtered);
        }
    }

    /**
     * Parse colors for the Text.
     *
     * @param text the text
     * @return parsed text
     */
    @NotNull
    public static String parseColours(@Nullable String text) {
        if (StringUtils.isEmpty(text)) {
            return "";
        }
        MineDownParser parser = MINEDOWN.get().parser();
        parser.reset();
        //A hack for saving reset character
        text = text.replace("&r", "&l&r").replace("§r", "§l§r");
        return toLegacyText(parser.enable(MineDownParser.Option.LEGACY_COLORS).backwardsCompatibility(true).parse(text).create());
    }

    /**
     * Parse colors for the List.
     *
     * @param list the list
     * @return parsed list
     */
    @NotNull
    public static List<String> parseColours(@NotNull List<String> list) {
        final List<String> newList = new ArrayList<>();

        for (String s : list) {
            newList.add(parseColours(s));
        }

        return newList;
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

    /**
     * Convert component to json
     *
     * @param components Chat Component
     * @return Json
     */
    public static String componentsToJson(BaseComponent[] components) {
        return ComponentSerializer.toString(components);
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
        return !"release".equalsIgnoreCase(QuickShop.getInstance().getBuildInfo().getGitBranch());
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
            tabList.addAll(Arrays.stream(plugin.getServer().getOfflinePlayers()).map(OfflinePlayer::getName).filter(Objects::nonNull).collect(Collectors.toList()));
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

    public static void mainThreadRun(@NotNull Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTask(QuickShop.getInstance(), runnable);
        }
    }

    // http://www.java2s.com/Tutorials/Java/Data_Type_How_to/Date_Convert/Convert_long_type_timestamp_to_LocalDate_and_LocalDateTime.htm
    public static LocalDateTime getDateTimeFromTimestamp(long timestamp) {
        if (timestamp == 0) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), TimeZone
                .getDefault().toZoneId());
    }

    // http://www.java2s.com/Tutorials/Java/Data_Type_How_to/Date_Convert/Convert_long_type_timestamp_to_LocalDate_and_LocalDateTime.htm
    public static LocalDate getDateFromTimestamp(long timestamp) {
        LocalDateTime date = getDateTimeFromTimestamp(timestamp);
        return date == null ? null : date.toLocalDate();
    }

    public static UUID getNilUniqueId() {
        return new UUID(0, 0);
    }

    public static UUID getSenderUniqueId(CommandSender sender) {
        if (sender instanceof OfflinePlayer) {
            return ((OfflinePlayer) sender).getUniqueId();
        }
        return getNilUniqueId();
    }

    // https://stackoverflow.com/questions/45321050/java-string-matching-with-wildcards
    public static String createRegexFromGlob(@NotNull String glob) {
        StringBuilder out = new StringBuilder("^");
        for (int i = 0; i < glob.length(); ++i) {
            final char c = glob.charAt(i);
            switch (c) {
                case '*':
                    out.append(".*");
                    break;
                case '?':
                    out.append('.');
                    break;
                case '.':
                    out.append("\\.");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                default:
                    out.append(c);
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

}
