/*
 *  This file is a part of project QuickShop, the name is ChatProcessorInfoItem.java
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

package com.ghostchu.quickshop.util.paste.item;

import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.paste.util.HTMLTable;
import net.kyori.adventure.Adventure;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class ChatProcessorInfoItem implements SubPasteItem {
    private final String impl = "Adventure";
    private final String formatter = "MiniMessage";
    private final String adventureSource;
    private final String adventureBukkitPlatformSource;
    private final String adventureTextSerializerLegacySource;
    private final String adventureTextSerializerGsonSource;
    private final String miniMessageSource;

    public ChatProcessorInfoItem() {
        this.adventureSource = Util.getClassPath(Adventure.class);
        this.adventureBukkitPlatformSource = Util.getClassPath(BukkitAudiences.class);
        this.adventureTextSerializerGsonSource = Util.getClassPath(GsonComponentSerializer.class);
        this.adventureTextSerializerLegacySource = Util.getClassPath(LegacyComponentSerializer.class);
        this.miniMessageSource = Util.getClassPath(MiniMessage.class);
    }


    @Override
    public @NotNull String getTitle() {
        return "Chat Processor";
    }

    @NotNull
    private String buildContent() {
        HTMLTable table = new HTMLTable(2, true);
        table.insert("Processor", impl);
        table.insert("Formatter", formatter);
        table.insert("Adventure", adventureSource);
        table.insert("Bukkit Platform", adventureBukkitPlatformSource);
        table.insert("Text Serializer(Gson)", adventureTextSerializerGsonSource);
        table.insert("Text Serializer(Legacy)", adventureTextSerializerLegacySource);
        table.insert("MiniMessage", miniMessageSource);
        return table.render();
    }


    @Override
    public @NotNull String genBody() {
        return buildContent();
    }
}
