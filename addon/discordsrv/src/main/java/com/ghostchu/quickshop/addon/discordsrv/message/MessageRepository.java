package com.ghostchu.quickshop.addon.discordsrv.message;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.discordsrv.parser.EmbedMessageParser;
import com.ghostchu.quickshop.api.localization.text.ProxiedLocale;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MessageRepository {
    private static final String ADDON_TRANSLATION_KEY_PREFIX = "addon.discord.discord-messages.";
    private final QuickShop plugin;
    private final EmbedMessageParser parser = new EmbedMessageParser();
    private final static String ZERO_WIDTH_SPACE = "\u200E";

    public MessageRepository(QuickShop plugin) {
        this.plugin = plugin;
    }

    @AutoRegisterMessage(key = "sold-to-your-shop")
    public MessageEmbed soldToYourShop(@NotNull UUID langUser, @NotNull Map<String, String> placeHolders) {
        return generateFromTemplate("sold-to-your-shop", langUser, placeHolders);
    }

    @AutoRegisterMessage(key = "bought-from-your-shop")
    public MessageEmbed boughtFromYourShop(@NotNull UUID langUser, @NotNull Map<String, String> placeHolders) {
        return generateFromTemplate("bought-from-your-shop", langUser, placeHolders);
    }

    @AutoRegisterMessage(key = "out-of-space")
    public MessageEmbed outOfSpace(@NotNull UUID langUser, @NotNull Map<String, String> placeHolders) {
        return generateFromTemplate("out-of-space", langUser, placeHolders);
    }

    @AutoRegisterMessage(key = "out-of-stock")
    public MessageEmbed outOfStock(@NotNull UUID langUser, @NotNull Map<String, String> placeHolders) {
        return generateFromTemplate("out-of-stock", langUser, placeHolders);
    }


    @AutoRegisterMessage(key = "shop-transfer-to-you")
    public MessageEmbed shopTransferToYou(@NotNull UUID langUser, @NotNull Map<String, String> placeHolders) {
        return generateFromTemplate("shop-transfer-to-you", langUser, placeHolders);
    }


    @AutoRegisterMessage(key = "shop-price-changed")
    public MessageEmbed shopPriceChanged(@NotNull UUID langUser, @NotNull Map<String, String> placeHolders) {
        return generateFromTemplate("shop-price-changed", langUser, placeHolders);
    }

    @AutoRegisterMessage(key = "shop-permission-changed")
    public MessageEmbed shopPermissionChanged(@NotNull UUID langUser, @NotNull Map<String, String> placeHolders) {
        return generateFromTemplate("shop-permission-changed", langUser, placeHolders);
    }

    @AutoRegisterMessage(key = "mod-shop-created")
    public MessageEmbed modShopCreated(@NotNull UUID langUser, @NotNull Map<String, String> placeHolders) {
        return generateFromTemplate("mod-shop-created", langUser, placeHolders);
    }

    @AutoRegisterMessage(key = "mod-shop-transfer")
    public MessageEmbed modShopTransfer(@NotNull UUID langUser, @NotNull Map<String, String> placeHolders) {
        return generateFromTemplate("mod-shop-transfer", langUser, placeHolders);
    }

    @AutoRegisterMessage(key = "mod-remove-shop")
    public MessageEmbed modShopRemoved(@NotNull UUID langUser, @NotNull Map<String, String> placeHolders) {
        return generateFromTemplate("mod-remove-shop", langUser, placeHolders);
    }

    @AutoRegisterMessage(key = "mod-shop-price-changed")
    public MessageEmbed modShopPriceChanged(@NotNull UUID langUser, @NotNull Map<String, String> placeHolders) {
        return generateFromTemplate("mod-shop-price-changed", langUser, placeHolders);
    }

    @AutoRegisterMessage(key = "mod-shop-purchase")
    public MessageEmbed modShopPurchase(@NotNull UUID langUser, @NotNull Map<String, String> placeHolders) {
        return generateFromTemplate("mod-shop-purchase", langUser, placeHolders);
    }


    @NotNull
    private MessageEmbed generateFromTemplate(@NotNull String key, @NotNull UUID langUser, @NotNull Map<String, String> placeHolders) {
        return applyPlaceHolders(parser.parse(textOfString(langUser, key)), placeHolders);
    }


    @NotNull
    private MessageEmbed applyPlaceHolders(@NotNull MessageEmbed embed, @NotNull Map<String, String> placeholders) {
        EmbedBuilder builder = new EmbedBuilder(embed);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            if (StringUtils.isEmpty(entry.getValue())) {
                entry.setValue(ZERO_WIDTH_SPACE);
            }
        }
        builder.setTitle(applyPlaceHolders(embed.getTitle(), placeholders));
        builder.setDescription(applyPlaceHolders(embed.getDescription(), placeholders));
        if (embed.getAuthor() != null) {
            MessageEmbed.AuthorInfo authorInfo = applyPlaceHolders(embed.getAuthor(), placeholders);
            if (authorInfo != null) {
                builder.setAuthor(authorInfo.getName(), authorInfo.getUrl(), authorInfo.getIconUrl());
            }
        }
        if (embed.getImage() != null) {
            builder.setImage(applyPlaceHolders(embed.getImage().getUrl(), placeholders));
        }
        if (embed.getThumbnail() != null) {
            builder.setThumbnail(applyPlaceHolders(embed.getThumbnail().getUrl(), placeholders));
        }
        builder.setTimestamp(Instant.now());
        if (embed.getFooter() != null) {
            String text = applyPlaceHolders(embed.getFooter().getText(), placeholders);
            String url = applyPlaceHolders(embed.getFooter().getIconUrl(), placeholders);
            builder.setFooter(text, url);
        }
        List<MessageEmbed.Field> newFields = new ArrayList<>();
        for (MessageEmbed.Field field : builder.getFields()) {
            String title = applyPlaceHolders(field.getName(), placeholders);
            String value = applyPlaceHolders(field.getValue(), placeholders);
            newFields.add(new MessageEmbed.Field(title, value, field.isInline()));
        }
        builder.clearFields();
        newFields.forEach(builder::addField);
        return builder.build();
    }

    @Nullable
    private MessageEmbed.AuthorInfo applyPlaceHolders(@Nullable MessageEmbed.AuthorInfo info, @NotNull Map<String, String> placeholders) {
        if (info == null) return null;
        String iconUrl = info.getIconUrl();
        String name = info.getName();
        String url = info.getUrl();
        String proxyUrl = info.getProxyIconUrl();
        // apply
        iconUrl = applyPlaceHolders(iconUrl, placeholders);
        name = applyPlaceHolders(name, placeholders);
        url = applyPlaceHolders(url, placeholders);
        proxyUrl = applyPlaceHolders(proxyUrl, placeholders);
        return new MessageEmbed.AuthorInfo(name, url, iconUrl, proxyUrl);
    }

    @Nullable
    private String applyPlaceHolders(@Nullable String string, @NotNull Map<String, String> placeholders) {
        if (string == null) return null;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String value = entry.getValue();
            if (value == null) value = "";
            string = string.replace("%%" + entry.getKey() + "%%", value);
        }
        return string;
    }

    @NotNull
    private ProxiedLocale getPlayerLocale(@Nullable UUID uuid) {
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

    @NotNull
    private String textOfString(@Nullable UUID langUser, @NotNull String key) {
        return PlainTextComponentSerializer.plainText().serialize(plugin.text().of(ADDON_TRANSLATION_KEY_PREFIX + key).forLocale(getPlayerLocale(langUser).getLocale()));
    }
}
