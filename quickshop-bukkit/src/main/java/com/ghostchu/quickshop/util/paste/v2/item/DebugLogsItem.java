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

import com.ghostchu.quickshop.util.Util;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.StringJoiner;

public class DebugLogsItem implements SubPasteItem {


    public DebugLogsItem() {

    }


    @Override
    public @NotNull String getTitle() {
        return "Debug History";
    }

    @NotNull
    private String buildContent() {
        StringJoiner builder = new StringJoiner("\n");
        List<String> debugLogs = Util.getDebugLogs();
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
