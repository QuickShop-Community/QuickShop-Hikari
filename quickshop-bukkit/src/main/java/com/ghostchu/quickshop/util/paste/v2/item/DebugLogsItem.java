/* V
 *  This file is a part of project QuickShop, the name is SystemInfoItem.java
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

package com.ghostchu.quickshop.util.paste.v2.item;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.logger.Log;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.StringJoiner;

public class DebugLogsItem implements SubPasteItem {
    private SimpleDateFormat format;

    public DebugLogsItem() {
        String timeFormat = LegacyComponentSerializer.legacySection().serialize(QuickShop.getInstance().text().of("timeunit.std-time-format").forLocale(MsgUtil.getDefaultGameLanguageCode()));
        try {
            format = new SimpleDateFormat(timeFormat);
        } catch (IllegalArgumentException e) {
            format = new SimpleDateFormat("HH:mm:ss");
        }
    }


    @Override
    public @NotNull String getTitle() {
        return "Debug History";
    }

    @NotNull
    private String buildContent() {
        StringJoiner builder = new StringJoiner("\n");
        List<String> debugLogs = Log.fetchLogs().stream().map(record -> "[" + format.format(record.getTimestamp()) + "] " + record).toList();
        List<String> tail = debugLogs.subList(Math.max(debugLogs.size() - 3000, 0), debugLogs.size());
        tail.forEach(builder::add);
        return "<textarea name=\"debuglogs\" style=\"height: 1000px; width: 100%;\">" +
                StringEscapeUtils.escapeHtml4(builder.toString()) +
                "</textarea><br />";
    }

    @Override
    public @NotNull String genBody() {
        return buildContent();
    }
}
