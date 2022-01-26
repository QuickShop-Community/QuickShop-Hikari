/*
 * This file is a part of project QuickShop, the name is MsgUtil.java
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

import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.ServiceInjector;
import org.maxgamer.quickshop.api.database.WarpedResultSet;
import org.maxgamer.quickshop.api.event.ShopControlPanelOpenEvent;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.chat.QuickComponentImpl;
import org.maxgamer.quickshop.chat.platform.minedown.BungeeQuickChat;
import org.maxgamer.quickshop.localization.game.game.GameLanguage;
import org.maxgamer.quickshop.localization.game.game.MojangGameLanguageImpl;
import org.maxgamer.quickshop.shop.ShopTransactionMessageContainer;
import org.maxgamer.quickshop.util.logging.container.PluginGlobalAlertLog;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;


public class MsgUtil {
    private static final Map<UUID, List<ShopTransactionMessageContainer>> OUTGOING_MESSAGES = Maps.newConcurrentMap();
    public static GameLanguage gameLanguage;
    private static DecimalFormat decimalFormat;
    private static QuickShop plugin = QuickShop.getInstance();
    @Getter
    private static YamlConfiguration enchi18n;
    @Getter
    private static YamlConfiguration itemi18n;
    @Getter
    private static YamlConfiguration potioni18n;

    /**
     * Deletes any messages that are older than a week in the database, to save on space.
     */
    public static void clean() {
        plugin
                .getLogger()
                .info("Cleaning purchase messages from the database that are over a week old...");
        // 604800,000 msec = 1 week.
        plugin.getDatabaseHelper().cleanMessage(System.currentTimeMillis() - 604800000);
    }

    /**
     * Empties the queue of messages a player has and sends them to the player.
     *
     * @param p The player to message
     * @return True if success, False if the player is offline or null
     */
    public static boolean flush(@NotNull OfflinePlayer p) {
        Player player = p.getPlayer();
        if (player != null) {
            UUID pName = player.getUniqueId();
            List<ShopTransactionMessageContainer> msgs = OUTGOING_MESSAGES.get(pName);
            if (msgs != null) {
                for (ShopTransactionMessageContainer msg : msgs) {
                    Util.debugLog("Accepted the msg for player " + player.getName() + " : " + msg);
                    if (msg.getHoverItemStr() != null) {
                        try {
                            ItemStack data = Util.deserialize(msg.getHoverItemStr());
                            if (data == null) {
                                MsgUtil.sendDirectMessage(player, msg.getMessage(player.getLocale()));
                            } else {
                                plugin.getQuickChat().sendItemHologramChat(player, msg.getMessage(player.getLocale()), data);
                            }
                        } catch (InvalidConfigurationException e) {
                            MsgUtil.sendDirectMessage(p.getPlayer(), msg.getMessage(player.getLocale()));
                        }
                        }
                }
                plugin.getDatabaseHelper().cleanMessageForPlayer(pName);
                msgs.clear();
                return true;
            }
        }
        return false;
    }

    /**
     * Get item's i18n name, If you want get item name, use Util.getItemStackName
     *
     * @param itemBukkitName ItemBukkitName(e.g. Material.STONE.name())
     * @return String Item's i18n name.
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    public static String getItemi18n(@NotNull String itemBukkitName) {
        if (itemBukkitName.isEmpty()) {
            return "Item is empty";
        }
        String itemnameI18n = itemi18n.getString("itemi18n." + itemBukkitName);
        if (itemnameI18n != null && !itemnameI18n.isEmpty()) {
            return itemnameI18n;
        }
        Material material = Material.matchMaterial(itemBukkitName);
        if (material == null) {
            return "Material not exist";
        }
        return Util.prettifyText(material.name());
    }

    /**
     * Replace args in raw to args
     *
     * @param raw  text
     * @param args args
     * @return filled text
     */
    @NotNull
    public static String fillArgs(@Nullable String raw, @Nullable String... args) {
        if (StringUtils.isEmpty(raw)) {
            return "";
        }
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                raw = StringUtils.replace(raw, "{" + i + "}", args[i] == null ? "" : args[i]);
            }
        }
        return raw;
    }

    private volatile static Map.Entry<String, String> cachedGameLanguageCode = null;

    @NotNull
    public static String getDefaultGameLanguageCode() {
        String languageCode = plugin.getConfig().getString("game-language", "default");
        if (cachedGameLanguageCode != null && cachedGameLanguageCode.getKey().equals(languageCode)) {
            return cachedGameLanguageCode.getValue();
        }
        String result = getGameLanguageCode(languageCode);
        cachedGameLanguageCode = new AbstractMap.SimpleEntry<>(languageCode, result);
        return result;
    }

    @ApiStatus.Experimental
    @NotNull
    public static String getGameLanguageCode(String languageCode) {
        if ("default".equalsIgnoreCase(languageCode)) {
            Locale locale = Locale.getDefault();
            String language = locale.getLanguage();
            String country = locale.getCountry();
            boolean isLanguageEmpty = StringUtils.isEmpty(language);
            boolean isCountryEmpty = StringUtils.isEmpty(country);
            if (isLanguageEmpty && isCountryEmpty) {
                //plugin.getLogger().warning("Unable to get language code, fallback to en_us, please change game-language option in config.yml.");
                languageCode = "en_us";
            } else {
                if (isCountryEmpty || isLanguageEmpty) {
                    languageCode = isLanguageEmpty ? country + '_' + country : language + '_' + language;
                    if ("en_en".equals(languageCode)) {
                        languageCode = "en_us";
                    }
                    // plugin.getLogger().warning("Unable to get language code, guessing " + languageCode + " instead, If it's incorrect, please change game-language option in config.yml.");
                } else {
                    languageCode = language + '_' + country;
                }
            }
            languageCode = languageCode.replace("-", "_").toLowerCase(Locale.ROOT);
            return languageCode;
        } else {
            return languageCode.replace("-", "_").toLowerCase(Locale.ROOT);
        }
    }

    @ApiStatus.ScheduledForRemoval
    @Deprecated
    public static void loadGameLanguage(@NotNull String languageCode) {
        gameLanguage = ServiceInjector.getGameLanguage();
        if (gameLanguage == null) {
            gameLanguage = new MojangGameLanguageImpl(plugin, languageCode);
            ((MojangGameLanguageImpl) gameLanguage).load();
        }
    }

    public static String getTranslateText(ItemStack stack) {
        if (plugin.getConfig().getBoolean("force-use-item-original-name") || !stack.hasItemMeta() || !stack.getItemMeta().hasDisplayName()) {
            return convertItemStackToTranslateText(stack.getType());
        } else {
            return Util.getItemStackName(stack);
        }
    }

    public static String convertItemStackToTranslateText(Material mat) {
        return TextSplitter.bakeComponent(new BungeeQuickChat.BungeeComponentBuilder().append(Util.getTranslateComponentForMaterial(mat)).create());
    }

    @ApiStatus.ScheduledForRemoval
    @Deprecated
    public static void loadI18nFile() {
        //Update instance
        plugin = QuickShop.getInstance();
        plugin.getLogger().info("Loading plugin translations files...");

        //Load game language i18n
        loadGameLanguage(plugin.getConfig().getString("game-language", "default"));
    }

    @ApiStatus.ScheduledForRemoval
    @Deprecated
    public static void loadEnchi18n() {
        plugin.getLogger().info("Loading enchantments translations...");
        File enchi18nFile = new File(plugin.getDataFolder(), "enchi18n.yml");
        if (!enchi18nFile.exists()) {
            plugin.getLogger().info("Creating enchi18n.yml");
            plugin.saveResource("enchi18n.yml", false);
        }
        // Store it
        enchi18n = YamlConfiguration.loadConfiguration(enchi18nFile);
        enchi18n.options().copyDefaults(false);
        YamlConfiguration enchi18nYAML =
                YamlConfiguration.loadConfiguration(
                        new InputStreamReader(Objects.requireNonNull(plugin.getResource("enchi18n.yml")), StandardCharsets.UTF_8));
        enchi18n.setDefaults(enchi18nYAML);
        Util.parseColours(enchi18n);
        Enchantment[] enchsi18n = Enchantment.values();
        for (Enchantment ench : enchsi18n) {
            String enchi18nString = enchi18n.getString("enchi18n." + ench.getKey().getKey().trim());
            if (enchi18nString != null && !enchi18nString.isEmpty()) {
                continue;
            }
            String enchName = gameLanguage.getEnchantment(ench);
            enchi18n.set("enchi18n." + ench.getKey().getKey(), enchName);
            plugin.getLogger().info("Found new ench [" + enchName + "] , adding it to the config...");
        }
        try {
            enchi18n.save(enchi18nFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not load/save transaction enchname from enchi18n.yml. Skipping...", e);
        }
    }

    /**
     * Load Itemi18n fron file
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    public static void loadItemi18n() {
        plugin.getLogger().info("Loading items translations...");
        File itemi18nFile = new File(plugin.getDataFolder(), "itemi18n.yml");
        if (!itemi18nFile.exists()) {
            plugin.getLogger().info("Creating itemi18n.yml");
            plugin.saveResource("itemi18n.yml", false);
        }
        // Store it
        itemi18n = YamlConfiguration.loadConfiguration(itemi18nFile);
        itemi18n.options().copyDefaults(false);
        YamlConfiguration itemi18nYAML =
                YamlConfiguration.loadConfiguration(
                        new InputStreamReader(Objects.requireNonNull(plugin.getResource("itemi18n.yml")), StandardCharsets.UTF_8));
        itemi18n.setDefaults(itemi18nYAML);
        Util.parseColours(itemi18n);
        Material[] itemsi18n = Material.values();
        for (Material material : itemsi18n) {
            String itemi18nString = itemi18n.getString("itemi18n." + material.name());
            if (itemi18nString != null && !itemi18nString.isEmpty()) {
                continue;
            }
            String itemName = gameLanguage.getItem(material);
            itemi18n.set("itemi18n." + material.name(), itemName);
            plugin
                    .getLogger()
                    .info("Found new items/blocks [" + itemName + "] , adding it to the config...");
        }
        try {
            itemi18n.save(itemi18nFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not load/save transaction itemname from itemi18n.yml. Skipping...", e);
        }
    }

    @ApiStatus.ScheduledForRemoval
    @Deprecated
    public static void loadPotioni18n() {
        plugin.getLogger().info("Loading potions translations...");
        File potioni18nFile = new File(plugin.getDataFolder(), "potioni18n.yml");
        if (!potioni18nFile.exists()) {
            plugin.getLogger().info("Creating potioni18n.yml");
            plugin.saveResource("potioni18n.yml", false);
        }
        // Store it
        potioni18n = YamlConfiguration.loadConfiguration(potioni18nFile);
        potioni18n.options().copyDefaults(false);
        YamlConfiguration potioni18nYAML =
                YamlConfiguration.loadConfiguration(
                        new InputStreamReader(Objects.requireNonNull(plugin.getResource("potioni18n.yml")), StandardCharsets.UTF_8));
        potioni18n.setDefaults(potioni18nYAML);
        Util.parseColours(potioni18n);
        for (PotionEffectType potion : PotionEffectType.values()) {
            if (potion == null) {
                continue;
            }
            String potionI18n = potioni18n.getString("potioni18n." + potion.getName());
            if (potionI18n != null && !StringUtils.isEmpty(potionI18n)) {
                continue;
            }
            String potionName = gameLanguage.getPotion(potion);
            plugin.getLogger().info("Found new potion [" + potionName + "] , adding it to the config...");
            potioni18n.set("potioni18n." + potion.getName(), potionName);
        }
        try {
            potioni18n.save(potioni18nFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not load/save transaction potionname from potioni18n.yml. Skipping...", e);
        }
    }

    /**
     * loads all player purchase messages from the database.
     */
    public static void loadTransactionMessages() {
        OUTGOING_MESSAGES.clear(); // Delete old messages
        try (WarpedResultSet warpRS = plugin.getDatabaseHelper().selectAllMessages(); ResultSet rs = warpRS.getResultSet()) {
            while (rs.next()) {
                String owner = rs.getString("owner");
                UUID ownerUUID;
                if (Util.isUUID(owner)) {
                    ownerUUID = UUID.fromString(owner);
                } else {
                    ownerUUID = PlayerFinder.findUUIDByName(owner);
                }
                String message = rs.getString("message");
                List<ShopTransactionMessageContainer> msgs = OUTGOING_MESSAGES.computeIfAbsent(ownerUUID, k -> new LinkedList<>());
                msgs.add(ShopTransactionMessageContainer.fromJson(message));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Could not load transaction messages from database. Skipping.", e);
        }
    }

    /**
     * @param uuid                   The uuid of the player to message
     * @param shopTransactionMessage The message to send them Sends the given player a message if they're online.
     *                               Else, if they're not online, queues it for them in the database.
     * @param isUnlimited            The shop is or unlimited
     *                               <p>
     *                               Deprecated for always use for bukkit deserialize method (costing ~145ms)
     */
    @Deprecated
    public static void send(@NotNull UUID uuid, @NotNull ShopTransactionMessageContainer shopTransactionMessage, boolean isUnlimited) {
        if (isUnlimited && plugin.getConfig().getBoolean("shop.ignore-unlimited-shop-messages")) {
            return; // Ignore unlimited shops messages.
        }
        Util.debugLog(shopTransactionMessage.getMessage(null));
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        if (!p.isOnline()) {
            List<ShopTransactionMessageContainer> msgs = OUTGOING_MESSAGES.getOrDefault(uuid, new LinkedList<>());
            msgs.add(shopTransactionMessage);
            OUTGOING_MESSAGES.put(uuid, msgs);
            plugin.getDatabaseHelper().saveOfflineTransactionMessage(uuid, shopTransactionMessage.toJson(), System.currentTimeMillis());
        } else {
            Player player = p.getPlayer();
            if (player != null) {
                String locale = player.getLocale();
                String hoverItemStr = shopTransactionMessage.getHoverItemStr();
                if (hoverItemStr != null) {
                    try {
                        plugin.getQuickChat().sendItemHologramChat(player, shopTransactionMessage.getMessage(locale), Objects.requireNonNull(Util.deserialize(hoverItemStr)));
                    } catch (Exception any) {
                        Util.debugLog("Unknown error, send by plain text.");
                        // Normal msg
                        MsgUtil.sendDirectMessage(player, shopTransactionMessage.getMessage(locale));
                    }
                } else {
                    // Normal msg
                    MsgUtil.sendDirectMessage(player, shopTransactionMessage.getMessage(locale));
                }
            }
        }
    }

    /**
     * @param shop                            The shop purchased
     * @param uuid                            The uuid of the player to message
     * @param shopTransactionMessageContainer The message to send, if the given player are online it will be send immediately,
     *                                        Else, if they're not online, queues them in the database.
     */
    public static void send(@NotNull Shop shop, @NotNull UUID uuid, @NotNull ShopTransactionMessageContainer shopTransactionMessageContainer) {
        if (shop.isUnlimited() && plugin.getConfig().getBoolean("shop.ignore-unlimited-shop-messages")) {
            return; // Ignore unlimited shops messages.
        }
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        if (!p.isOnline()) {
            List<ShopTransactionMessageContainer> msgs = OUTGOING_MESSAGES.getOrDefault(uuid, new LinkedList<>());
            msgs.add(shopTransactionMessageContainer);
            OUTGOING_MESSAGES.put(uuid, msgs);
            plugin.getDatabaseHelper().saveOfflineTransactionMessage(uuid, shopTransactionMessageContainer.toJson(), System.currentTimeMillis());
        } else {
            Player player = p.getPlayer();
            if (player != null) {
                String locale = player.getLocale();
                String hoverItemStr = shopTransactionMessageContainer.getHoverItemStr();
                if (hoverItemStr != null) {
                    try {
                        plugin.getQuickChat().sendItemHologramChat(p.getPlayer(), shopTransactionMessageContainer.getMessage(locale), Objects.requireNonNull(Util.deserialize(hoverItemStr)));
                    } catch (Exception any) {
                        Util.debugLog("Unknown error, send by plain text.");
                        // Normal msg
                        MsgUtil.sendDirectMessage(p.getPlayer(), shopTransactionMessageContainer.getMessage(locale));
                    }
                } else {
                    // Normal msg
                    MsgUtil.sendDirectMessage(p.getPlayer(), shopTransactionMessageContainer.getMessage(locale));
                }
            }
        }
    }
    // TODO: No hardcode

    /**
     * Send controlPanel infomation to sender
     *
     * @param sender Target sender
     * @param shop   Target shop
     */
    public static void sendControlPanelInfo(@NotNull CommandSender sender, @NotNull Shop shop) {
        if ((sender instanceof Player)
                && !QuickShop.getPermissionManager().hasPermission(sender, "quickshop.use")
                && (shop.getOwner().equals(((Player) sender).getUniqueId()) || !QuickShop.getPermissionManager().hasPermission(sender, "quickshop.other.control"))
                && !InteractUtil.check(InteractUtil.Action.CONTROL, ((Player) sender).isSneaking())) {

            return;
        }
        if (Util.fireCancellableEvent(new ShopControlPanelOpenEvent(shop, sender))) {
            Util.debugLog("ControlPanel blocked by 3rd-party");
            return;
        }
        plugin.getShopManager().bakeShopRuntimeRandomUniqueIdCache(shop);
        ChatSheetPrinter chatSheetPrinter = new ChatSheetPrinter(sender);
        chatSheetPrinter.printHeader();
        chatSheetPrinter.printLine(plugin.text().of(sender, "controlpanel.infomation").forLocale());
        // Owner
        if (!QuickShop.getPermissionManager().hasPermission(sender, "quickshop.setowner")) {
            chatSheetPrinter.printLine(plugin.text().of(sender, "menu.owner", shop.ownerName()).forLocale());
        } else {
            chatSheetPrinter.printSuggestedCmdLine(
                    plugin.text().of(sender,
                            "controlpanel.setowner",
                            shop.ownerName()
                                    + ((plugin.getConfig().getBoolean("shop.show-owner-uuid-in-controlpanel-if-op")
                                    && shop.isUnlimited())
                                    ? (" (" + shop.getOwner() + ")")
                                    : "")).forLocale(),
                    plugin.text().of(sender, "controlpanel.setowner-hover").forLocale(),
                    "/qs setowner ");
        }


        // Unlimited
        if (QuickShop.getPermissionManager().hasPermission(sender, "quickshop.unlimited")) {
            String text =
                    plugin.text().of(sender, "controlpanel.unlimited", bool2String(shop.isUnlimited())).forLocale();
            String hoverText = plugin.text().of(sender, "controlpanel.unlimited-hover").forLocale();
            String clickCommand =
                    MsgUtil.fillArgs(
                            "/qs silentunlimited {0}",
                            shop.getRuntimeRandomUniqueId().toString());
            chatSheetPrinter.printExecutableCmdLine(text, hoverText, clickCommand);
        }
        // Always Counting
        if (QuickShop.getPermissionManager().hasPermission(sender, "quickshop.alwayscounting")) {
            String text =
                    plugin.text().of(sender, "controlpanel.alwayscounting", bool2String(shop.isAlwaysCountingContainer())).forLocale();
            String hoverText = plugin.text().of(sender, "controlpanel.alwayscounting-hover").forLocale();
            String clickCommand =
                    MsgUtil.fillArgs(
                            "/qs silentalwayscounting {0}",
                            shop.getRuntimeRandomUniqueId().toString());
            chatSheetPrinter.printExecutableCmdLine(text, hoverText, clickCommand);
        }
        // Buying/Selling Mode
        if (QuickShop.getPermissionManager().hasPermission(sender, "quickshop.create.buy")
                && QuickShop.getPermissionManager().hasPermission(sender, "quickshop.create.sell")) {
            if (shop.isSelling()) {
                String text = plugin.text().of(sender, "controlpanel.mode-selling").forLocale();
                String hoverText = plugin.text().of(sender, "controlpanel.mode-selling-hover").forLocale();
                String clickCommand =
                        MsgUtil.fillArgs(
                                "/qs silentbuy {0}",
                                shop.getRuntimeRandomUniqueId().toString());
                chatSheetPrinter.printExecutableCmdLine(text, hoverText, clickCommand);
            } else if (shop.isBuying()) {
                String text = plugin.text().of(sender, "controlpanel.mode-buying").forLocale();
                String hoverText = plugin.text().of(sender, "controlpanel.mode-buying-hover").forLocale();
                String clickCommand =
                        MsgUtil.fillArgs(
                                "/qs silentsell {0}",
                                shop.getRuntimeRandomUniqueId().toString());
                chatSheetPrinter.printExecutableCmdLine(text, hoverText, clickCommand);
            }
        }
        // Set Price
        if (QuickShop.getPermissionManager().hasPermission(sender, "quickshop.other.price")
                || shop.getOwner().equals(((OfflinePlayer) sender).getUniqueId())) {
            String text =
                    MsgUtil.fillArgs(
                            plugin.text().of(sender, "controlpanel.price").forLocale(),
                            (plugin.getConfig().getBoolean("use-decimal-format"))
                                    ? decimalFormat(shop.getPrice())
                                    : Double.toString(shop.getPrice()));
            String hoverText = plugin.text().of(sender, "controlpanel.price-hover").forLocale();
            String clickCommand = "/qs price ";
            chatSheetPrinter.printSuggestedCmdLine(text, hoverText, clickCommand);
        }
        //Set amount per bulk
        if (QuickShop.getInstance().isAllowStack()) {
            if (QuickShop.getPermissionManager().hasPermission(sender, "quickshop.other.amount") || shop.getOwner().equals(((OfflinePlayer) sender).getUniqueId()) && QuickShop.getPermissionManager().hasPermission(sender, "quickshop.create.changeamount")) {
                String text = plugin.text().of(sender, "controlpanel.stack", Integer.toString(shop.getItem().getAmount())).forLocale();
                String hoverText = plugin.text().of(sender, "controlpanel.stack-hover").forLocale();
                String clickCommand = "/qs size ";
                chatSheetPrinter.printSuggestedCmdLine(text, hoverText, clickCommand);

            }
        }
        if (!shop.isUnlimited()) {
            // Refill
            if (QuickShop.getPermissionManager().hasPermission(sender, "quickshop.refill")) {
                String text =
                        plugin.text().of(sender, "controlpanel.refill", String.valueOf(shop.getPrice())).forLocale();
                String hoverText = plugin.text().of(sender, "controlpanel.refill-hover").forLocale();
                String clickCommand = "/qs refill ";
                chatSheetPrinter.printSuggestedCmdLine(text, hoverText, clickCommand);
            }
            // Empty
            if (QuickShop.getPermissionManager().hasPermission(sender, "quickshop.empty")) {
                String text = plugin.text().of(sender, "controlpanel.empty", String.valueOf(shop.getPrice())).forLocale();
                String hoverText = plugin.text().of(sender, "controlpanel.empty-hover").forLocale();
                String clickCommand = MsgUtil.fillArgs("/qs silentempty {0}", shop.getRuntimeRandomUniqueId().toString());
                chatSheetPrinter.printExecutableCmdLine(text, hoverText, clickCommand);
            }
        }
        // Remove
        if (QuickShop.getPermissionManager().hasPermission(sender, "quickshop.other.destroy")
                || shop.getOwner().equals(((OfflinePlayer) sender).getUniqueId())) {
            String text = plugin.text().of(sender, "controlpanel.remove", String.valueOf(shop.getPrice())).forLocale();
            String hoverText = plugin.text().of(sender, "controlpanel.remove-hover").forLocale();
            String clickCommand = MsgUtil.fillArgs("/qs silentremove {0}", shop.getRuntimeRandomUniqueId().toString());
            chatSheetPrinter.printExecutableCmdLine(text, hoverText, clickCommand);
        }
        chatSheetPrinter.printFooter();
    }


    /**
     * Translate boolean value to String, the symbon is changeable by language file.
     *
     * @param bool The boolean value
     * @return The result of translate.
     */
    public static String bool2String(boolean bool) {
        if (bool) {
            return QuickShop.getInstance().text().of("booleanformat.success").forLocale();
        } else {
            return QuickShop.getInstance().text().of("booleanformat.failed").forLocale();
        }
    }

    public static String decimalFormat(double value) {
        if (decimalFormat == null) {
            //lazy initialize
            try {
                String format = plugin.getConfig().getString("decimal-format");
                decimalFormat = format == null ? new DecimalFormat() : new DecimalFormat(format);
            } catch (Exception e) {
                QuickShop.getInstance().getLogger().log(Level.WARNING, "Error when processing decimal format, using system default: " + e.getMessage());
                decimalFormat = new DecimalFormat();
            }
        }
        return decimalFormat.format(value);
    }

    /**
     * Send globalAlert to ops, console, log file.
     *
     * @param content The content to send.
     */
    public static void sendGlobalAlert(@Nullable String content) {
        if (content == null) {
            Util.debugLog("Content is null");
            Throwable throwable =
                    new Throwable("Known issue: Global Alert accepted null string, what the fuck");
            plugin.getSentryErrorReporter().sendError(throwable, "NullCheck");
            return;
        }
        sendMessageToOps(content);
        plugin.getLogger().warning(content);
        plugin.logEvent(new PluginGlobalAlertLog(content));
    }

    /**
     * Send a message for all online Ops.
     *
     * @param message The message you want send
     */
    public static void sendMessageToOps(@NotNull String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (QuickShop.getPermissionManager().hasPermission(player, "quickshop.alerts")) {
                MsgUtil.sendDirectMessage(player, message);
            }
        }
    }


    /**
     * Get Enchantment's i18n name.
     *
     * @param key The Enchantment.
     * @return Enchantment's i18n name.
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    public static String getEnchi18n(@NotNull Enchantment key) {
        String enchString = key.getKey().getKey();
        if (enchString.isEmpty()) {
            return "Enchantment key is empty";
        }
        String enchI18n = enchi18n.getString("enchi18n." + enchString);
        if (enchI18n != null && !enchI18n.isEmpty()) {
            return enchI18n;
        }
        return Util.prettifyText(enchString);
    }


    public static void printEnchantment(@NotNull Player p, @NotNull Shop shop, ChatSheetPrinter chatSheetPrinter) {
        if (shop.getItem().hasItemMeta() && shop.getItem().getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS) && plugin.getConfig().getBoolean("respect-item-flag")) {
            return;
        }
        Map<Enchantment, Integer> enchs = new HashMap<>();
        if (shop.getItem().hasItemMeta() && shop.getItem().getItemMeta().hasEnchants()) {
            enchs = shop.getItem().getItemMeta().getEnchants();
        }
        if (!enchs.isEmpty()) {
            chatSheetPrinter.printCenterLine(plugin.text().of(p, "menu.enchants").forLocale());
            printEnchantment(chatSheetPrinter, enchs);
        }
        if (shop.getItem().getItemMeta() instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta stor = (EnchantmentStorageMeta) shop.getItem().getItemMeta();
            stor.getStoredEnchants();
            enchs = stor.getStoredEnchants();
            if (!enchs.isEmpty()) {
                chatSheetPrinter.printCenterLine(plugin.text().of(p, "menu.stored-enchants").forLocale());
                printEnchantment(chatSheetPrinter, enchs);
            }
        }
    }

    private static void printEnchantment(ChatSheetPrinter chatSheetPrinter, Map<Enchantment, Integer> enchs) {
        for (Entry<Enchantment, Integer> entries : enchs.entrySet()) {
            //Use boxed object to avoid NPE
            Integer level = entries.getValue();
            chatSheetPrinter.printLine(ChatColor.YELLOW + MsgUtil.getEnchi18n(entries.getKey()) + " " + RomanNumber.toRoman(level == null ? 1 : level));
        }
    }

    /**
     * Get potion effect's i18n name.
     *
     * @param potion potionType
     * @return Potion's i18n name.
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    public static String getPotioni18n(@NotNull PotionEffectType potion) {
        String potionString = potion.getName().trim();
        if (potionString.isEmpty()) {
            return "Potion name is empty.";
        }
        String potionI18n = potioni18n.getString("potioni18n." + potionString);
        if (potionI18n != null && !potionI18n.isEmpty()) {
            return potionI18n;
        }
        return Util.prettifyText(potionString);
    }


    public static void debugStackTrace(StackTraceElement[] traces) {
        if (Util.isDisableDebugLogger()) {
            return;
        }
        for (StackTraceElement stackTraceElement : traces) {
            final String className = stackTraceElement.getClassName();
            final String methodName = stackTraceElement.getMethodName();
            final int codeLine = stackTraceElement.getLineNumber();
            final String fileName = stackTraceElement.getFileName();
            Util.debugLog("[TRACE]  [" + className + "] [" + methodName + "] (" + fileName + ":" + codeLine + ") ");
        }
    }

    public static void sendDirectMessage(@NotNull UUID sender, @Nullable String... messages) {
        sendDirectMessage(Bukkit.getPlayer(sender), messages);
    }

    public static void sendDirectMessage(@Nullable CommandSender sender, @Nullable String... messages) {
        if (messages == null) {
            Util.debugLog("INFO: null messages trying to be sent.");
            return;
        }
        if (sender == null) {
            Util.debugLog("INFO: Sending message to null sender.");
            return;
        }
        for (String msg : messages) {
            try {
                if (StringUtils.isEmpty(msg)) {
                    continue;
                }
                TextSplitter.SpilledString spilledString = TextSplitter.deBakeItem(msg);
                if (spilledString == null) {
                    plugin.getQuickChat().send(sender, msg);
                } else {
                    BungeeQuickChat.BungeeComponentBuilder builder = new BungeeQuickChat.BungeeComponentBuilder();
                    builder.appendLegacyAndItem(spilledString.getLeft()
                            , spilledString.getComponents(), spilledString.getRight());
                    plugin.getQuickChat().send(sender, new QuickComponentImpl(builder.create()));
                }
            } catch (Throwable throwable) {
                Util.debugLog("Failed to send formatted text.");
                if (!StringUtils.isEmpty(msg)) {
                    sender.sendMessage(msg);
                }
            }
        }
    }

    public static boolean isJson(String str) {
        try {
            JsonUtil.parser().parse(str);
            return true;
        } catch (JsonParseException exception) {
            return false;
        }
    }
}
