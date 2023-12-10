package com.ghostchu.quickshop.util;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.ShopControlPanelOpenEvent;
import com.ghostchu.quickshop.api.localization.text.ProxiedLocale;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.common.util.RomanNumber;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.logging.container.PluginGlobalAlertLog;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;


public class MsgUtil {
    private static final QuickShop PLUGIN = QuickShop.getInstance();
    private static DecimalFormat decimalFormat;
    private static volatile Entry<String, String> cachedGameLanguageCode = null;

    @NotNull
    public static Component addLeftLine(@NotNull CommandSender sender, @NotNull Component component) {
        return PLUGIN.text().of(sender, "tableformat.left_begin").forLocale().append(component);
    }

    /**
     * Translate boolean value to String, the symbon is changeable by language file.
     *
     * @param bool The boolean value
     * @return The result of translate.
     */
    public static Component bool2String(boolean bool) {
        if (bool) {
            return PLUGIN.text().of("booleanformat.success").forLocale();
        } else {
            return PLUGIN.text().of("booleanformat.failed").forLocale();
        }
    }

    /**
     * Deletes any messages that are older than a week in the database, to save on space.
     */
    public static void clean() {
        PLUGIN.logger()
                .info("Cleaning purchase messages from the database that are over a week old...");
        // 604800,000 msec = 1 week.
        PLUGIN.getDatabaseHelper().cleanMessage(System.currentTimeMillis() - 604800000)
                .thenAccept(result -> Log.debug("Cleaned " + result + " messages from the database"))
                .exceptionally(error -> {
                    Log.debug(Level.SEVERE, "Error cleaning purchase messages from the database:" + error.getMessage());
                    return null;
                });
    }

    public static void debugStackTrace(@NotNull StackTraceElement[] traces) {
        if (!Util.isDevMode()) {
            return;
        }
        for (StackTraceElement stackTraceElement : traces) {
            final String className = stackTraceElement.getClassName();
            final String methodName = stackTraceElement.getMethodName();
            final int codeLine = stackTraceElement.getLineNumber();
            final String fileName = stackTraceElement.getFileName();
            Log.debug("[TRACE]  at " + className + "." + methodName + " (" + fileName + ":" + codeLine + ") ");
        }
    }

    public static String decimalFormat(double value) {
        if (decimalFormat == null) {
            //lazy initialize
            try {
                String format = PLUGIN.getConfig().getString("dep.sendMessage(\"容器设置成功\");cimal-format");
                decimalFormat = format == null ? new DecimalFormat() : new DecimalFormat(format);
            } catch (Exception e) {
                QuickShop.getInstance().logger().warn("Error when processing decimal format, using system default!", e);
                decimalFormat = new DecimalFormat();
            }
        }
        return decimalFormat.format(value);
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

    /**
     * Replace args in origin to args
     *
     * @param origin origin
     * @param args   args
     * @return filled component
     */
    @NotNull
    public static Component fillArgs(@NotNull Component origin, @Nullable Component... args) {
        for (int i = 0; i < args.length; i++) {
            origin = origin.replaceText(TextReplacementConfig.builder()
                    .matchLiteral("{" + i + "}")
                    .replacement(args[i] == null ? Component.empty() : args[i])
                    .build());
        }
        return origin.compact();
    }

    /**
     * Empties the queue of messages a player has and sends them to the player.
     *
     * @param p The player to message
     * @return True if success, False if the player is offline or null
     */
    public static boolean flush(@NotNull OfflinePlayer p) {
        Player player = p.getPlayer();
        if (player == null) {
            return false;
        }
        UUID playerUniqueId = player.getUniqueId();
        PLUGIN.getDatabaseHelper().selectPlayerMessages(playerUniqueId)
                .thenAccept(msgs -> {
                    for (String msg : msgs) {
                        PLUGIN.getPlatform().sendMessage(player, GsonComponentSerializer.gson().deserialize(msg));
                    }
                    PLUGIN.getDatabaseHelper().cleanMessageForPlayer(playerUniqueId)
                            .exceptionally(error -> {
                                PLUGIN.logger().warn("Error on cleaning the purchase messages from the database", error);
                                return 0;
                            });
                })
                .exceptionally(th -> {
                    PLUGIN.logger().warn("Failed to retrieve player {} messages.", playerUniqueId, th);
                    return null;
                });
        return false;
    }

    @NotNull
    public static ProxiedLocale getDefaultGameLanguageLocale() {
        return PLUGIN.text().findRelativeLanguages(getDefaultGameLanguageCode());
    }

    @NotNull
    public static String getDefaultGameLanguageCode() {
        String languageCode = PLUGIN.getConfig().getString("game-language", "default");
        if (cachedGameLanguageCode != null && cachedGameLanguageCode.getKey().equals(languageCode)) {
            return cachedGameLanguageCode.getValue();
        }
        String result = getGameLanguageCode(languageCode);
        cachedGameLanguageCode = new AbstractMap.SimpleEntry<>(languageCode, result);
        return result;
    }
    // TODO: No hardcode

    @ApiStatus.Experimental
    @NotNull
    public static String getGameLanguageCode(@NotNull String languageCode) {
        if ("default".equalsIgnoreCase(languageCode)) {
            Locale locale = Locale.getDefault();
            String language = locale.getLanguage();
            String country = locale.getCountry();
            boolean isLanguageEmpty = StringUtils.isEmpty(language);
            boolean isCountryEmpty = StringUtils.isEmpty(country);
            if (isLanguageEmpty && isCountryEmpty) {
                languageCode = "en_us";
            } else {
                if (isCountryEmpty || isLanguageEmpty) {
                    languageCode = isLanguageEmpty ? country + '_' + country : language + '_' + language;
                    if ("en_en".equals(languageCode)) {
                        languageCode = "en_us";
                    }
                } else {
                    languageCode = language + '_' + country;
                }
            }
        }
        return languageCode.replace("-", "_").toLowerCase(Locale.ROOT);
    }

    @NotNull
    @Deprecated(since = "4.2.0.0")
    public static Component getTranslateText(@NotNull ItemStack stack) {
        //if (PLUGIN.getConfig().getBoolean("shop.force-use-item-original-name") || !stack.hasItemMeta() || !stack.getItemMeta().hasDisplayName()) {
        //    return PLUGIN.getPlatform().getTranslation(stack.getType());
        //} else {
        return Util.getItemStackName(stack);
        //}
    }

    @Deprecated
    public static boolean isJson(String str) {
        return CommonUtil.isJson(str);
    }

    public static void printEnchantment(@NotNull Shop shop, @NotNull ChatSheetPrinter chatSheetPrinter) {
        Map<Enchantment, Integer> enchantmentIntegerMap = new HashMap<>();
        if (shop.getItem().getItemMeta() != null) {
            enchantmentIntegerMap.putAll(shop.getItem().getItemMeta().getEnchants());
        }
        printEnchantment(chatSheetPrinter, enchantmentIntegerMap);
    }

    private static void printEnchantment(@NotNull ChatSheetPrinter chatSheetPrinter, @NotNull Map<Enchantment, Integer> enchs) {
        if (enchs.isEmpty()) {
            return;
        }
        chatSheetPrinter.printCenterLine(PLUGIN.text().of("menu.enchants").forLocale());
        for (Entry<Enchantment, Integer> entries : enchs.entrySet()) {
            //Use boxed object to avoid NPE
            Integer level = entries.getValue();
            Component component;
            try {
                component = Component.empty().color(NamedTextColor.YELLOW).append(PLUGIN.getPlatform().getTranslation(entries.getKey()));
            } catch (Throwable error) {
                component = MsgUtil.setHandleFailedHover(null, Component.text(entries.getKey().getKey().toString()));
                QuickShop.getInstance().logger().warn("Failed to handle translation for Enchantment {}", entries.getKey().getKey(), error);
            }
            chatSheetPrinter.printLine(component.append(Component.text(" " + RomanNumber.toRoman(level == null ? 1 : level))));
        }
    }

    @NotNull
    private static HoverEvent<Component> getHandleFailedHoverEvent(@Nullable CommandSender sender, @Nullable HoverEvent<Component> oldHoverEvent) {
        HoverEvent<Component> hoverEvent = HoverEvent.showText(PLUGIN.text().of(sender, "display-fallback").forLocale());
        if (oldHoverEvent != null) {
            hoverEvent = hoverEvent.value(hoverEvent.value().appendNewline().append(hoverEvent.value()));
        }
        return hoverEvent;
    }

    @NotNull
    public static Component setHandleFailedHover(@Nullable CommandSender sender, @NotNull Component component) {
        return Component.empty().append(component.hoverEvent(
                        getHandleFailedHoverEvent(sender, null))
                .decorate(TextDecoration.UNDERLINED)
                .color(NamedTextColor.RED)
        );
    }


    /**
     * @param shop The shop purchased
     * @param uuid The uuid of the player to message
     */
    public static void send(@NotNull Shop shop, @Nullable UUID uuid, @NotNull Component shopTransactionMessage) {
        send(uuid, shopTransactionMessage, shop.isUnlimited());
    }

    /**
     * @param shop  The shop purchased
     * @param qUser The uuid of the player to message
     */
    public static void send(@NotNull Shop shop, @NotNull QUser qUser, @NotNull Component shopTransactionMessage) {
        UUID uuid = qUser.getUniqueIdIfRealPlayer().orElse(null);
        send(uuid, shopTransactionMessage, shop.isUnlimited());
    }

    /**
     * @param uuid                   The uuid of the player to message
     * @param shopTransactionMessage The message to send them Sends the given player a message if they're online.
     *                               Else, if they're not online, queues it for them in the database.
     * @param isUnlimited            The shop is or unlimited
     *                               <p>
     *                               Deprecated for always use for bukkit deserialize method (costing ~145ms)
     */
    public static void send(@Nullable UUID uuid, @NotNull Component shopTransactionMessage, boolean isUnlimited) {
        if (isUnlimited && PLUGIN.getConfig().getBoolean("shop.ignore-unlimited-shop-messages")) {
            return; // Ignore unlimited shops messages.
        }
        if (uuid == null) {
            return;
        }
        String serialized = GsonComponentSerializer.gson().serialize(shopTransactionMessage);
        Log.debug(serialized);
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        if (!p.isOnline()) {
            PLUGIN.getDatabaseHelper().saveOfflineTransactionMessage(uuid, serialized, System.currentTimeMillis())
                    .thenAccept(v -> {
                    })
                    .exceptionally(err -> {
                        PLUGIN.logger().warn("Could not save transaction message to database", err);
                        return null;
                    });
            try {
                if (p.getName() != null && PLUGIN.getConfig().getBoolean("bungee-cross-server-msg", true)) {
                    PLUGIN.getDatabaseHelper().getPlayerLocale(uuid).whenCompleteAsync((locale, err) -> {
                        if (locale != null) {
                            sendBungeeMessage(p.getName(), shopTransactionMessage, locale);
                        }
                    });
                }
            } catch (Exception e) {
                Log.debug("Could not send shop transaction message to player " + p.getName() + " via BungeeCord: " + e.getMessage());
            }
        } else {
            Player player = p.getPlayer();
            if (player != null) {
                PLUGIN.getPlatform().sendMessage(player, shopTransactionMessage);
            }
        }
    }

    public static void sendBungeeMessage(@NotNull String playerName, @NotNull Component message, @NotNull String locale) {
        Component csmMessage = PLUGIN.text().of("bungee-cross-server-msg", message).forLocale(locale);
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("MessageRaw");
        out.writeUTF(playerName);
        out.writeUTF(GsonComponentSerializer.gson().serialize(csmMessage));
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (player != null) {
            player.sendPluginMessage(PLUGIN.getJavaPlugin(), "BungeeCord", out.toByteArray());
        }
    }

    /**
     * Send controlPanel infomation to sender
     *
     * @param sender Target sender
     * @param shop   Target shop
     */
    public static void sendControlPanelInfo(@NotNull CommandSender sender, @NotNull Shop shop) {
        if (!(sender instanceof Player)) {
            return;
        }
        if (!PLUGIN.perm().hasPermission(sender, "quickshop.use")) {
            return;
        }
        if (Util.fireCancellableEvent(new ShopControlPanelOpenEvent(shop, sender))) {
            Log.debug("ControlPanel blocked by 3rd-party");
            return;
        }
        PLUGIN.getShopManager().bakeShopRuntimeRandomUniqueIdCache(shop);
        PLUGIN.getShopControlPanelManager().openControlPanel((Player) sender, shop);

    }

    public static void sendDirectMessage(@NotNull UUID sender, @Nullable Component... messages) {
        sendDirectMessage(Bukkit.getPlayer(sender), messages);
    }

    public static void sendDirectMessage(@Nullable CommandSender sender, @Nullable Component... messages) {
        if (messages == null) {
            return;
        }
        if (sender == null) {
            return;
        }
        for (Component msg : messages) {
            if (msg == null) {
                return;
            }
            if (Util.isEmptyComponent(msg)) {
                return;
            }
            PLUGIN.getPlatform().sendMessage(sender, msg);
        }
    }

    public static void sendDirectMessage(@Nullable QUser sender, @Nullable Component... messages) {
        if (sender == null) {
            return;
        }
        UUID uuid = sender.getUniqueIdIfRealPlayer().orElse(null);
        if (uuid == null) {
            return;
        }
        sendDirectMessage(uuid, messages);
    }

    public static void sendDirectMessage(@Nullable CommandSender sender, @Nullable String... messages) {
        if (messages == null) {
            return;
        }
        if (sender == null) {
            return;
        }
        for (String msg : messages) {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            sendDirectMessage(sender, LegacyComponentSerializer.legacySection().deserialize(msg));
        }
    }

    /**
     * Send globalAlert to ops, console, log file.
     *
     * @param content The content to send.
     */
    public static void sendGlobalAlert(@Nullable String content) {
        if (content == null) {
            Log.debug("Content is null");
            Throwable throwable =
                    new Throwable("Known issue: Global Alert accepted null string, what the fuck");
            PLUGIN.getSentryErrorReporter().sendError(throwable, "NullCheck");
            return;
        }
        sendMessageToOps(content);
        PLUGIN.logger().warn(content);
        PLUGIN.logEvent(new PluginGlobalAlertLog(content));
    }

    /**
     * Send globalAlert to ops, console, log file.
     *
     * @param content The content to send.
     */
    public static void sendGlobalAlert(@Nullable Component content) {
        if (content == null) {
            Log.debug("Content is null");
            Throwable throwable =
                    new Throwable("Known issue: Global Alert accepted null string, what the fuck");
            PLUGIN.getSentryErrorReporter().sendError(throwable, "NullCheck");
            return;
        }
        sendMessageToOps(content);
        PLUGIN.logger().warn(LegacyComponentSerializer.legacySection().serialize(content));
        PLUGIN.logEvent(new PluginGlobalAlertLog(LegacyComponentSerializer.legacySection().serialize(content)));
    }

    /**
     * Send a message for all online Ops.
     *
     * @param message The message you want send
     */
    public static void sendMessageToOps(@NotNull String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (QuickShop.getPermissionManager().hasPermission(player, "quickshop.alerts")) {
                MsgUtil.sendDirectMessage(player, LegacyComponentSerializer.legacySection().deserialize(message));
            }
        }
    }

    /**
     * Send a message for all online Ops.
     *
     * @param message The message you want send
     */
    public static void sendMessageToOps(@NotNull Component message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (QuickShop.getPermissionManager().hasPermission(player, "quickshop.alerts")) {
                MsgUtil.sendDirectMessage(player, message);
            }
        }
    }

    @NotNull
    public static Component formatPlayerProfile(@Nullable Profile profile, @Nullable CommandSender sender) {
        String name = null;
        String uuid = null;
        if (profile != null) {
            name = profile.getName();
            uuid = profile.getUniqueId().toString();

        }
        if (name == null) {
            name = "Unknown";
        }
        if (uuid == null) {
            uuid = "N/A";
        }

        if (PLUGIN == null) {
            return Component.text(name).color(TextColor.color(NamedTextColor.AQUA))
                    .append(Component.text("(").color(TextColor.color(NamedTextColor.GOLD)))
                    .append(Component.text(uuid)).color(TextColor.color(NamedTextColor.YELLOW))
                    .append(Component.text(")").color(TextColor.color(NamedTextColor.GOLD)));
        } else {
            return PLUGIN.text().of(sender, "player-profile-format", name, uuid).forLocale();
        }
    }

}
