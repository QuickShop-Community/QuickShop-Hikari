/*
 * This file is a part of project QuickShop, the name is BungeeQuickChat.java
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

package org.maxgamer.quickshop.chat.platform.minedown;

import lombok.AllArgsConstructor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.chat.QuickChat;
import org.maxgamer.quickshop.api.chat.QuickComponent;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.chat.QuickComponentImpl;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.ReflectFactory;
import org.maxgamer.quickshop.util.TextSplitter;
import org.maxgamer.quickshop.util.Util;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.logging.Level;

/**
 * BungeeChat module to send complex chats and impl QuickChat
 *
 * @author Ghost_chu
 */
@AllArgsConstructor
public class BungeeQuickChat implements QuickChat {
    private final QuickShop plugin;

    public static BaseComponent[] fromLegacyText(String text) {
        return TextComponent.fromLegacyText(text, net.md_5.bungee.api.ChatColor.RESET);
    }

    @Override
    public void send(@NotNull CommandSender receiver, @Nullable QuickComponent component) {
        if (component == null) {
            return;
        }
        if (component.get() instanceof BaseComponent[]) {
            receiver.spigot().sendMessage((BaseComponent[]) component.get());
            return;
        }
        if (component.get() instanceof BaseComponent) {
            receiver.spigot().sendMessage((BaseComponent) component.get());
            return;
        }
        Util.debugLog("Illegal component " + component.get().getClass().getName() + " sending to " + this.getClass().getName() + " processor, trying force sending.");

    }

    public static String toLegacyText(BaseComponent[] components) {
        StringBuilder builder = new StringBuilder();
        BaseComponent lastComponent = null;
        for (BaseComponent component : components) {
            net.md_5.bungee.api.ChatColor color = component.getColorRaw();
            String legacyText = component.toLegacyText();
            if (color == null && legacyText.startsWith("§f")) {
                //Remove redundant §f added by toLegacyText
                legacyText = legacyText.substring(2);
            }
            if (lastComponent != null && (
                    lastComponent.isBold() != component.isBold() ||
                            lastComponent.isItalic() != component.isItalic() ||
                            lastComponent.isObfuscated() != component.isObfuscated() ||
                            lastComponent.isStrikethrough() != component.isStrikethrough() ||
                            lastComponent.isUnderlined() != lastComponent.isUnderlined()
            )) {
                builder.append("§r");
            }
            builder.append(legacyText);
            lastComponent = component;
        }
        return builder.toString();
    }

    @Override
    public void send(@NotNull CommandSender receiver, @Nullable String message) {
        if (StringUtils.isEmpty(message)) {
            return;
        }
        receiver.spigot().sendMessage(fromLegacyText(message));
    }

    @Override
    public void sendItemHologramChat(@NotNull Player player, @NotNull String text, @NotNull ItemStack itemStack) {
        sendItemHologramChat(player, text, itemStack, false);
    }


    private BungeeComponentBuilder appendItemHoloChat(@Nullable String itemJson, @NotNull String message) {
        BungeeComponentBuilder builder = new BungeeComponentBuilder();
        TextSplitter.SpilledString spilledString = TextSplitter.deBakeItem(message);
        if (itemJson == null) {
            if (spilledString == null) {
                builder.appendLegacy(message);
            } else {
                builder.appendLegacyAndItem(spilledString.getLeft()
                        , spilledString.getComponents()
                        , spilledString.getRight());
            }
        } else {
            HoverEvent itemHoverEvent = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ComponentBuilder(itemJson).create());
            if (spilledString == null) {
                builder.event(itemHoverEvent);
                builder.appendLegacy(message);
                //Dummy for not included hover event
                builder.append(new TextComponent());
                builder.event((HoverEvent) null);
            } else {
                //Only show item on item name side
                for (BaseComponent component : spilledString.getComponents()) {
                    component.setHoverEvent(itemHoverEvent);
                }
                builder.appendLegacyAndItem(spilledString.getLeft()
                        , spilledString.getComponents()
                        , spilledString.getRight());
            }
        }
        return builder;
    }

    private void sendItemHologramChat(@NotNull Player player, @NotNull String text, @NotNull ItemStack itemStack, boolean skipItemHoloChat) {
        TextComponent errorComponent = new TextComponent(plugin.text().of(player, "menu.item-holochat-error").forLocale());
        try {
            BungeeComponentBuilder builder;
            if (skipItemHoloChat) {
                builder = appendItemHoloChat(null, text);
            } else {
                builder = appendItemHoloChat(ReflectFactory.convertBukkitItemStackToJson(itemStack), text);
            }
            BaseComponent[] result = builder.create();
            String resultStr = ComponentSerializer.toString(result);
            Util.debugLog("Sending debug: " + resultStr);
            //The limit in vanilla server is 32767
            if (resultStr.getBytes(StandardCharsets.UTF_8).length > 32767) {
                if (skipItemHoloChat) {
                    //If still too large after skipItemHoloChat, just send the error message
                    plugin.text().of(player, "menu.item-holochat-data-too-large").send();
                } else {
                    sendItemHologramChat(player, text, itemStack, true);
                }
            } else {
                player.spigot().sendMessage(result);
            }
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException | InstantiationException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to process chat component", e);
            player.spigot().sendMessage(errorComponent);
        }
    }

    @Override
    public @NotNull QuickComponent getItemHologramChat(@NotNull Shop shop, @NotNull ItemStack itemStack, @NotNull Player player, @NotNull String message) {
        return getItemHologramChat(shop, itemStack, player, message, false);
    }

    private @NotNull QuickComponent getItemHologramChat(@NotNull Shop shop, @NotNull ItemStack itemStack, @NotNull Player player, @NotNull String message, boolean skipItemHoloChat) {
        TextComponent errorComponent = new TextComponent(plugin.text().of(player, "menu.item-holochat-error").forLocale());
        try {
            BungeeComponentBuilder builder;
            if (skipItemHoloChat) {
                builder = appendItemHoloChat(null, message);
            } else {
                builder = appendItemHoloChat(ReflectFactory.convertBukkitItemStackToJson(itemStack), message);
            }
            if (QuickShop.getPermissionManager().hasPermission(player, "quickshop.preview")) {
                builder.appendLegacy(" ", plugin.text().of(player, "menu.preview").forLocale());
                builder.event(new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        MsgUtil.fillArgs(
                                "/qs silentpreview {0}",
                                shop.getRuntimeRandomUniqueId().toString())));
            }
            BaseComponent[] result = builder.create();
            //The limit in vanilla server is 32767
            if (ComponentSerializer.toString(result).getBytes(StandardCharsets.UTF_8).length > 32767) {
                if (skipItemHoloChat) {
                    //If still too large after skipItemHoloChat, just send the error message
                    return new QuickComponentImpl(plugin.text().of(player, "menu.item-holochat-data-too-large").forLocale());
                } else {
                    return getItemHologramChat(shop, itemStack, player, message, true);
                }
            } else {
                return new QuickComponentImpl(result);
            }
        } catch (Exception t) {
            plugin.getLogger().log(Level.WARNING, "Failed to process chat component", t);
            return new QuickComponentImpl(errorComponent);
        }
    }


    public static class BungeeComponentBuilder {
        private final ComponentBuilder builder;

        public BungeeComponentBuilder() {
            builder = new ComponentBuilder("");
            builder.removeComponent(0);
        }

        public BungeeComponentBuilder append(BaseComponent component) {
            if (builder.getCursor() == -1) {
                builder.append(component, ComponentBuilder.FormatRetention.EVENTS);
            } else {
                builder.append(component);
            }
            return this;
        }


        public BungeeComponentBuilder append(BaseComponent[] components) {
            for (BaseComponent component : components) {
                append(component);
            }
            return this;
        }

        public BungeeComponentBuilder append(String text) {
            if (builder.getCursor() == -1) {
                builder.append(text, ComponentBuilder.FormatRetention.EVENTS);
            } else {
                builder.append(text);
            }
            return this;
        }

        public BungeeComponentBuilder appendLegacy(String... text) {
            if (text == null || text.length == 0) {
                return this;
            }
            StringBuilder stringBuilder = new StringBuilder(text[0]);
            for (int i = 1; i < text.length; i++) {
                stringBuilder.append(text[i]);
            }
            builder.append(fromLegacyText(stringBuilder.toString()), ComponentBuilder.FormatRetention.EVENTS);
            return this;
        }

        public BungeeComponentBuilder appendLegacyAndItem(String left, BaseComponent[] itemsComponent, String right) {
            String uuidStr = UUID.randomUUID().toString();
            BaseComponent[] components = fromLegacyText(left + uuidStr + right);
            boolean centerFound = false;
            for (BaseComponent component : components) {
                //Find center value
                if (!centerFound && component.toPlainText().contains(uuidStr)) {
                    centerFound = true;
                    String[] text = ((TextComponent) component).getText().split(uuidStr, 2);
                    TextComponent leftComponent = new TextComponent(text[0]);
                    leftComponent.copyFormatting(component);
                    TextComponent rightComponent = new TextComponent(text[1]);
                    rightComponent.copyFormatting(component);
                    for (BaseComponent baseComponent : itemsComponent) {
                        leftComponent.addExtra(baseComponent);
                    }
                    builder.append(leftComponent, ComponentBuilder.FormatRetention.EVENTS);
                    builder.append(rightComponent, ComponentBuilder.FormatRetention.EVENTS);
                } else {
                    builder.append(component, ComponentBuilder.FormatRetention.EVENTS);
                }
            }
            return this;
        }

        public BungeeComponentBuilder appendLegacy(String text) {
            builder.append(fromLegacyText(text), ComponentBuilder.FormatRetention.EVENTS);
            return this;
        }

        public BungeeComponentBuilder event(ClickEvent clickEvent) {
            builder.event(clickEvent);
            return this;
        }

        public BungeeComponentBuilder event(HoverEvent hoverEvent) {
            builder.event(hoverEvent);
            return this;
        }

        public BungeeComponentBuilder reset() {
            builder.reset();
            return this;
        }

        public BaseComponent[] create() {
            return builder.create();
        }

        public ComponentBuilder color(net.md_5.bungee.api.ChatColor color) {
            return builder.color(color);
        }
    }

    @Override
    public @NotNull QuickComponent getItemTextComponent(@NotNull Player player, @NotNull ItemStack itemStack, @NotNull String normalText) {
        TextComponent errorComponent = new TextComponent(plugin.text().of(player, "menu.item-holochat-error").forLocale());

        String json;
        try {
            json = ReflectFactory.convertBukkitItemStackToJson(itemStack);
        } catch (Exception throwable) {
            plugin.getLogger().log(Level.SEVERE, "Failed to saving item to json for holochat", throwable);
            return new QuickComponentImpl(errorComponent);
        }
        if (json == null) {
            return new QuickComponentImpl(errorComponent);
        }

        TextComponent component = new TextComponent(normalText + " " + plugin.text().of(player, "menu.preview").forLocale());
        ComponentBuilder cBuilder = new ComponentBuilder(json);
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, cBuilder.create()));
        return new QuickComponentImpl(component);

    }

    @Override
    public void sendExecutableChat(@NotNull CommandSender receiver, @NotNull String message, @NotNull String hoverText, @NotNull String command) {
        TextComponent component =
                new TextComponent(ChatColor.DARK_PURPLE + plugin.text().of(receiver, "tableformat.left_begin").forLocale() + message);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        component.setHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create())); //FIXME: Update this when drop 1.15 supports
        receiver.spigot().sendMessage(component);
    }

    @Override
    public void sendSuggestedChat(@NotNull CommandSender receiver, @NotNull String message, @NotNull String hoverText, @NotNull String command) {
        TextComponent component =
                new TextComponent(ChatColor.DARK_PURPLE + plugin.text().of(receiver, "tableformat.left_begin").forLocale() + message);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
        component.setHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create())); //FIXME: Update this when drop 1.15 supports
        receiver.spigot().sendMessage(component);
    }
}
