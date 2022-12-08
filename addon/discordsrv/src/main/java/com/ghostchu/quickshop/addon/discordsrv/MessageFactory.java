package com.ghostchu.quickshop.addon.discordsrv;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.localization.text.ProxiedLocale;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.enginehub.squirrelid.Profile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MessageFactory {
    private final QuickShop plugin;

    public MessageFactory(QuickShop plugin) {
        this.plugin = plugin;
    }

    private String wrap(@NotNull Component component) {
        return wrap(component, Collections.emptyMap());
    }

    private String wrap(@NotNull Component component, @NotNull Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            component = component.replaceText(b -> b.matchLiteral("%%" + entry.getKey() + "%%").replacement(entry.getValue()));
        }
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    private String getPlayerName(UUID uuid) {
        String name = uuid.toString();
        Player bukkitPlayer = Bukkit.getPlayer(uuid);
        if (bukkitPlayer != null) {
            name = bukkitPlayer.getName();
        } else {
            Profile profile = plugin.getPlayerFinder().find(uuid);
            if (profile != null) {
                if (profile.getName() != null) {
                    name = profile.getName();
                }
            }
        }
        return name;
    }

    @NotNull
    private ProxiedLocale getPlayerLocale(UUID uuid) {
        Util.ensureThread(true);
        ProxiedLocale locale;
        if (uuid != null) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                locale = plugin.getTextManager().findRelativeLanguages(uuid);
            } else {
                try {
                    locale = new ProxiedLocale(plugin.getDatabaseHelper().getPlayerLocale(uuid).get(10, TimeUnit.SECONDS), MsgUtil.getDefaultGameLanguageCode());
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    locale = MsgUtil.getDefaultGameLanguageLocale();
                }
            }
        } else {
            locale = MsgUtil.getDefaultGameLanguageLocale();
        }
        return locale;
    }

    private void applyPlaceHolders(@NotNull Shop shop, @NotNull Map<String, String> map) {
        applyPlaceHolders(shop, map, null);
    }

    private void applyPlaceHolders(@NotNull Shop shop, @NotNull Map<String, String> map, @Nullable UUID langUser) {
        map.put("shop.name", ChatColor.stripColor(shop.getShopName()));
        map.put("shop.owner.name", wrap(shop.ownerName(getPlayerLocale(langUser))));
        map.put("shop.location.world", shop.getLocation().getWorld().getName());
        map.put("shop.location.x", String.valueOf(shop.getLocation().getBlockX()));
        map.put("shop.location.y", String.valueOf(shop.getLocation().getBlockY()));
        map.put("shop.location.z", String.valueOf(shop.getLocation().getBlockY()));
        map.put("shop.location.id", String.valueOf(shop.getShopId()));
        map.put("shop.location.shop-name", String.valueOf(shop.getShopName() == null ? wrap(plugin.text().of(shop.getOwner(), "addon.discordsrv.shop.no-name").forLocale()) : shop.getShopName()));
        map.put("shop.item.name", wrap(Util.getItemStackName(shop.getItem())));
        map.put("shop.item.amount", String.valueOf(shop.getItem().getAmount()));
        map.put("shop.item.material", shop.getItem().getType().name());
        map.put("shop.owner.currency", shop.getCurrency());
        map.put("shop.remaining-space", String.valueOf(shop.getRemainingSpace()));
        map.put("shop.remaining-stock", String.valueOf(shop.getRemainingStock()));
        map.put("shop.stacking-amount", String.valueOf(shop.getShopStackingAmount()));
        map.put("shop.unlimited", String.valueOf(shop.isUnlimited()));
    }

    private Component locale(UUID langUser, String key) {
        return plugin.text().of(key).forLocale(getPlayerLocale(langUser).getLocale());
    }

    private String lang(UUID langUser, String key, Map<String, String> placeHolders) {
        return wrap(locale(langUser, key), placeHolders);
    }


    @NotNull
    public MessageEmbed somebodySellItemsToYourShop(@NotNull Shop shop, @NotNull UUID purchaser, @NotNull World world, int amount, double balance, double taxes) {
        String playerName = getPlayerName(purchaser);
        Map<String, String> placeHolders = new HashMap<>();
        applyPlaceHolders(shop, placeHolders, purchaser);
        placeHolders.put("purchase.uuid", purchaser.toString());
        placeHolders.put("purchase.name", playerName);
        placeHolders.put("purchase.world", world.getName());
        placeHolders.put("purchase.amount", String.valueOf(amount));
        placeHolders.put("purchase.balance", String.valueOf(balance));
        placeHolders.put("purchase.balanceFormatted", plugin.getEconomy().format(balance, world, shop.getCurrency()));
        placeHolders.put("purchase.taxes", String.valueOf(taxes));
        placeHolders.put("purchase.taxesFormatted", plugin.getEconomy().format(taxes, world, shop.getCurrency()));
        EmbedBuilder builder = createPurchaseTemplateEmbedMessage(shop, shop.getOwner(), playerName, world, amount, balance, taxes);
        builder.setTitle(lang(shop.getOwner(), "addon.discordsrv.sell-to-your-shop.title", placeHolders));
        if (shop.getShopName() != null) {
            builder.setDescription(lang(shop.getOwner(), "addon.discordsrv.sell-to-your-shop.description.name", placeHolders));
        } else {
            builder.setDescription(lang(shop.getOwner(), "addon.discordsrv.sell-to-your-shop.description.po", placeHolders));
        }
        builder.setColor(Color.MAGENTA);
        return builder.build();
    }

    @NotNull
    public MessageEmbed somebodyBoughtItemsToYourShop(@NotNull Shop shop, @NotNull UUID purchaser, @NotNull World world, int amount, double balance, double taxes) {
        String playerName = getPlayerName(purchaser);
        Map<String, String> placeHolders = new HashMap<>();
        applyPlaceHolders(shop, placeHolders, purchaser);
        placeHolders.put("purchase.uuid", purchaser.toString());
        placeHolders.put("purchase.name", playerName);
        placeHolders.put("purchase.world", world.getName());
        placeHolders.put("purchase.amount", String.valueOf(amount));
        placeHolders.put("purchase.balance", String.valueOf(balance));
        placeHolders.put("purchase.balanceFormatted", plugin.getEconomy().format(balance, world, shop.getCurrency()));
        placeHolders.put("purchase.taxes", String.valueOf(taxes));
        placeHolders.put("purchase.taxesFormatted", plugin.getEconomy().format(taxes, world, shop.getCurrency()));
        EmbedBuilder builder = createPurchaseTemplateEmbedMessage(shop, shop.getOwner(), playerName, world, amount, balance, taxes);
        builder.setTitle(lang(shop.getOwner(), "addon.discordsrv.bought-from-your-shop.title", placeHolders));
        if (shop.getShopName() != null) {
            builder.setDescription(lang(shop.getOwner(), "addon.discordsrv.bought-from-your-shop.description.name", placeHolders));
        } else {
            builder.setDescription(lang(shop.getOwner(), "addon.discordsrv.sell-to-your-shop.description.pos", placeHolders));
        }
        builder.setColor(Color.GREEN);
        return builder.build();
    }

    @NotNull
    public MessageEmbed runOutOfSpace(@NotNull Shop shop) {
        Map<String, String> placeHolders = new HashMap<>();
        applyPlaceHolders(shop, placeHolders, shop.getOwner());
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(lang(shop.getOwner(), "addon.discordsrv.out-of-space.title", placeHolders));
        if (shop.getShopName() != null) {
            builder.setDescription(lang(shop.getOwner(), "addon.discordsrv.out-of-space.description.name", placeHolders));
        } else {
            builder.setDescription(lang(shop.getOwner(), "addon.discordsrv.out-of-space.description.pos", placeHolders));
        }
        builder.setColor(Color.RED);
        return builder.build();
    }

    @NotNull
    public MessageEmbed runOutOfStock(@NotNull Shop shop) {
        Map<String, String> placeHolders = new HashMap<>();
        applyPlaceHolders(shop, placeHolders, shop.getOwner());
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(lang(shop.getOwner(), "addon.discordsrv.out-of-stock.title", placeHolders));
        if (shop.getShopName() != null) {
            builder.setDescription(lang(shop.getOwner(), "addon.discordsrv.out-of-stock.description.name", placeHolders));
        } else {
            builder.setDescription(lang(shop.getOwner(), "addon.discordsrv.out-of-stock.description.pos", placeHolders));
        }
        builder.setColor(Color.RED);
        return builder.build();
    }

//    @NotNull
//    public MessageEmbed ongoingFee(@NotNull Shop shop, double cost) {
//        Map<String, String> placeHolders = new HashMap<>();
//        applyPlaceHolders(shop, placeHolders,shop.getOwner());
//        placeHolders.put("ongoingfee.cost", String.valueOf(cost));
//        EmbedBuilder builder = new EmbedBuilder();
//        builder.setTitle(lang(shop.getOwner(), "addon.discordsrv.ongoingfee.collect.title", placeHolders));
//        if (shop.getShopName() != null) {
//            builder.setDescription(lang(shop.getOwner(), "addon.discordsrv.ongoingfee.collect.description.name", placeHolders));
//        } else {
//            builder.setDescription(lang(shop.getOwner(), "addon.discordsrv.ongoingfee.collect.description.pos", placeHolders));
//        }
//        builder.addField(lang(shop.getOwner(), "addon.discordsrv.field.cost", placeHolders), plugin.getEconomy().format(cost, shop.getLocation().getWorld(), plugin.getCurrency()), false);
//        builder.setColor(Color.ORANGE);
//        return builder.build();
//    }

    @NotNull
    private EmbedBuilder createPurchaseTemplateEmbedMessage(@NotNull Shop shop, @NotNull UUID langUser, @NotNull String playerName, @NotNull World world, int amount, double balance, double taxes) {
        EmbedBuilder builder = new EmbedBuilder();
        Map<String, String> placeHolders = new HashMap<>();
        applyPlaceHolders(shop, placeHolders, langUser);
        builder.addField(lang(langUser, "addon.discordsrv.field.player", placeHolders), playerName, false);
        builder.addField(lang(langUser, "addon.discordsrv.field.item", placeHolders), wrap(Util.getItemStackName(shop.getItem())), true);
        builder.addField(lang(langUser, "addon.discordsrv.field.amount", placeHolders), String.valueOf(amount), true);
        builder.addField(lang(langUser, "addon.discordsrv.field.balance", placeHolders), plugin.getEconomy().format(balance, world, shop.getCurrency()), true);
        if (plugin.getConfig().getBoolean("show-tax")) {
            builder.addField(lang(langUser, "addon.discordsrv.field.taxes", placeHolders), plugin.getEconomy().format(taxes, world, shop.getCurrency()), true);
        } else {
            builder.addBlankField(true);
        }
        return builder;
    }
}
