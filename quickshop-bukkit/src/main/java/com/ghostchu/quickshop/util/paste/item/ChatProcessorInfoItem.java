package com.ghostchu.quickshop.util.paste.item;

import com.ghostchu.quickshop.common.util.CommonUtil;
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
        this.adventureSource = CommonUtil.getClassPathRelative(Adventure.class);
        this.adventureBukkitPlatformSource = CommonUtil.getClassPathRelative(BukkitAudiences.class);
        this.adventureTextSerializerGsonSource = CommonUtil.getClassPathRelative(GsonComponentSerializer.class);
        this.adventureTextSerializerLegacySource = CommonUtil.getClassPathRelative(LegacyComponentSerializer.class);
        this.miniMessageSource = CommonUtil.getClassPathRelative(MiniMessage.class);
    }

    @Override
    public @NotNull String genBody() {
        return buildContent();
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
}
