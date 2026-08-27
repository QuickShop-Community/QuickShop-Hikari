package com.ghostchu.quickshop.util.paste.item;

import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.paste.util.HTMLTable;
import net.kyori.adventure.Adventure;
import net.kyori.adventure.text.minimessage.MiniMessage;
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

    final HTMLTable table = new HTMLTable(2, true);
    table.insert("Processor", IMPL);
    table.insert("Formatter", FORMATTER);
    table.insert("Adventure API", CommonUtil.getClassPathRelative(Adventure.class));
    table.insert("Adventure Text Serializer (Legacy)", safePathRelative("net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer"));
    table.insert("Adventure Text Serializer (Gson)", safePathRelative("net.kyori.adventure.text.serializer.gson.GsonComponentSerializer"));
    table.insert("Adventure Text Serializer (Json)", safePathRelative("net.kyori.adventure.text.serializer.json.JSONComponentSerializer"));
    table.insert("Adventure Text Serializer (ANSI)", safePathRelative("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer"));
    table.insert("Adventure Text Serializer (Plain)", safePathRelative("net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer"));
    table.insert("Adventure MiniMessage", CommonUtil.getClassPathRelative(MiniMessage.class));
    return table.render();
  }

  private String safePathRelative(final String className) {

    try {
      return CommonUtil.getClassPathRelative(Class.forName(className));
    } catch (ClassNotFoundException e) {
      return "null";
    }
  }
}
