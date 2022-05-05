/*
 *  This file is a part of project QuickShop, the name is HTMLTable.java
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

package com.ghostchu.quickshop.util.paste.util;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class HTMLTable {
    private int columns;
    private String[] title;
    private final List<String[]> data = new LinkedList<>();

    private final boolean firstColumnBold;

    private static final String TEMPLATE = """           
            <table>
              {thead}
              {tbody}
            </table>
            """;

    public HTMLTable(int columns) {
        this.columns = columns;
        this.firstColumnBold = false;
    }

    public HTMLTable(int columns, boolean firstColumnBold) {
        this.columns = columns;
        this.firstColumnBold = firstColumnBold;
    }

    public void setTableTitle(@Nullable String... title) {
        this.title = title;
    }

    public void insert(@NotNull String... data) {
        String[] f = new String[columns];
        Arrays.fill(f, "");
        System.arraycopy(data, 0, f, 0, Math.min(data.length, columns));
        if (firstColumnBold) {
            if (!StringUtils.isEmpty(f[0])) {
                f[0] = "<b>" + f[0] + "</b>";
            }
        }
        this.data.add(f);
    }

    @NotNull
    public String render() {
        String thead = renderHead();
        String tbody = renderBody();
        return TEMPLATE
                .replace("{thead}", thead)
                .replace("{tbody}", tbody);
    }

    private String renderBody() {
        StringBuilder tdBuilder = new StringBuilder();
        for (String[] line : data) {
            tdBuilder.append("<tr>");
            for (String record : line) {
                tdBuilder.append("<td>").append(record).append("</td>");
            }
            tdBuilder.append("</tr>");
        }
        return "<tbody>" + tdBuilder + "</tbody>";
    }

    @NotNull
    private String renderHead() {
        StringBuilder tdBuilder = new StringBuilder();
        if (title == null || title.length == 0)
            return "";
        for (String title : title) {
            tdBuilder.append("<th>").append(title).append("</th>");
        }
        return """
                <thead>
                <tr>
                 {th}
                </tr>
                </thead>
                """.replace("{th}", tdBuilder.toString());
    }


}
