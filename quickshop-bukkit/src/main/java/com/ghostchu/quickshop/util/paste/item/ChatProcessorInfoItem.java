package com.ghostchu.quickshop.util.paste.item;

import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.paste.util.HTMLTable;
import net.kyori.adventure.Adventure;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.platform.viaversion.ViaFacet;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class ChatProcessorInfoItem implements SubPasteItem {
    private static final String IMPL = "Adventure";
    private static final String FORMATTER = "MiniMessage";

    public ChatProcessorInfoItem() {
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
        table.insert("Processor", IMPL);
        table.insert("Formatter", FORMATTER);
        table.insert("Adventure API", CommonUtil.getClassPathRelative(Adventure.class));
        table.insert("Adventure Bukkit Platform", CommonUtil.getClassPathRelative(BukkitAudiences.class));
        table.insert("Adventure Text Serializer (Legacy)", CommonUtil.getClassPathRelative(LegacyComponentSerializer.class));
        table.insert("Adventure Text Serializer (Gson)", CommonUtil.getClassPathRelative(GsonComponentSerializer.class));
        table.insert("Adventure Text Serializer (Json)", CommonUtil.getClassPathRelative(JSONComponentSerializer.class));
        table.insert("Adventure Text Serializer (BungeeChat)", CommonUtil.getClassPathRelative(BungeeComponentSerializer.class));
        table.insert("Adventure Text Serializer (ViaVersion Facet)", CommonUtil.getClassPathRelative(ViaFacet.class));
        table.insert("Adventure Text Serializer (ANSI)", CommonUtil.getClassPathRelative(ANSIComponentSerializer.class));
        table.insert("Adventure Text Serializer (Plain)", CommonUtil.getClassPathRelative(PlainTextComponentSerializer.class));
        table.insert("Adventure MiniMessage", CommonUtil.getClassPathRelative(MiniMessage.class));
        return table.render();
    }
}
