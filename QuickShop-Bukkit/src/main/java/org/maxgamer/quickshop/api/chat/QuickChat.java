/*
 * This file is a part of project QuickShop, the name is QuickChat.java
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

package org.maxgamer.quickshop.api.chat;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.api.shop.Shop;

/**
 * QuickChat is a system to allow us to process complex messages
 */
public interface QuickChat {
    /**
     * Send plain text to specified receiver
     *
     * @param receiver The receiver will receive this message
     * @param message  The plain text to be sent
     */
    void send(@NotNull CommandSender receiver, @Nullable String message);

    /**
     * Send QuickComponent to specified receiver
     *
     * @param receiver  The receiver will receive this message
     * @param component The plain text to be sent
     */
    void send(@NotNull CommandSender receiver, @Nullable QuickComponent component);

    /**
     * Send chat with hover-item to specified receiver
     *
     * @param player    The PLAYER will receive this message
     * @param text      The text will sent
     * @param itemStack The ItemStack will append to text in message
     */
    void sendItemHologramChat(
            @NotNull Player player,
            @NotNull String text,
            @NotNull ItemStack itemStack);

    /**
     * Generate the chat component with preview item hover event
     *
     * @param shop      The shop
     * @param itemStack The itemstack
     * @param player    The player
     * @param message   The message with hover event
     * @return The component
     */
    @NotNull QuickComponent getItemHologramChat(@NotNull Shop shop, @NotNull ItemStack itemStack, @NotNull Player player, @NotNull String message);

    /**
     * Create chat with hover-item to specified receiver
     *
     * @param player    The PLAYER will receive this message
     * @param text      The text will sent
     * @param itemStack The ItemStack will append to text in message
     * @return The QuickComponent created by this method
     */
    @NotNull QuickComponent getItemTextComponent(@NotNull Player player, @NotNull ItemStack itemStack, @NotNull String text);

    /**
     * Send click-run-command chat to specified receiver
     *
     * @param receiver  The PLAYER will receive this message
     * @param message   The text will sent
     * @param hoverText The will show when hover on chat
     * @param command   The command when click to be run
     */
    void sendExecutableChat(@NotNull CommandSender receiver, @NotNull String message, @NotNull String hoverText, @NotNull String command);

    /**
     * Send click-suggest-command chat to specified receiver
     *
     * @param receiver  The PLAYER will receive this message
     * @param message   The text will sent
     * @param command   The command when click to be suggested
     * @param hoverText The text while player hover on the text
     */
    void sendSuggestedChat(@NotNull CommandSender receiver, @NotNull String message, @NotNull String hoverText, @NotNull String command);

}
