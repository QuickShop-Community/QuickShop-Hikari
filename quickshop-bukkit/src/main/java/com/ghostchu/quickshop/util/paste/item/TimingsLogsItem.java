package com.ghostchu.quickshop.util.paste.item;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.logger.Log;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.StringJoiner;

public class TimingsLogsItem implements SubPasteItem {

  private SimpleDateFormat format;

  public TimingsLogsItem() {

    final String timeFormat = LegacyComponentSerializer.legacySection().serialize(QuickShop.getInstance().text().of("timeunit.std-time-format").forLocale(MsgUtil.getDefaultGameLanguageCode()));
    try {
      format = new SimpleDateFormat(timeFormat);
    } catch(final IllegalArgumentException e) {
      format = new SimpleDateFormat("HH:mm:ss");
    }
  }

  @Override
  public @NotNull String genBody() {

    return buildContent();
  }

  @Override
  public @NotNull String getTitle() {

    return "Timings History";
  }

  @NotNull
  private String buildContent() {

    final StringJoiner builder = new StringJoiner("\n");
    final List<String> debugLogs = Log.fetchLogs(Log.Type.TIMING).stream().map(recordEntry->"[" + format.format(recordEntry.getTimestamp()) + "] " + recordEntry).toList();
    final List<String> tail = CommonUtil.tail(debugLogs, 1000);
    tail.forEach(builder::add);
    return "<textarea readonly=\"true\" name=\"timingslogs\" style=\"height: 1000px; width: 100%;\">" +
           builder +
           "</textarea><br />";
  }
}
