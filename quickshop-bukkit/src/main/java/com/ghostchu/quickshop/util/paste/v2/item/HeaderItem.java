/*
 *  This file is a part of project QuickShop, the name is TitleItem.java
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
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@AllArgsConstructor
@Data
public class HeaderItem implements PasteItem {
    private final long timestamp;
    private final Map<String, String> items;
    private static final String TEMPLATE = """
            <h1>{title}</h1>
            <blockquote>
            <p>
            <b>Warning!</b><br />
            Don't send paste to public channel or anyone unless you trust them.
            </p>
            </blockquote>
            <table>
                <tbody>
                   {content}
                </tbody>
            </table>
            """;

    @NotNull
    private String buildContent() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : items.entrySet()) {
            builder.append("<tr>");
            builder.append("<td>").append(entry.getKey()).append("</td>");
            builder.append("<td>").append(entry.getValue()).append("</td>");
            builder.append("</tr>");
        }
        return builder.toString();
    }

    @Override
    public @NotNull String toHTML() {
        return TEMPLATE
                .replace("{title}", "QuickShop-" + QuickShop.getFork() + " // Paste")
                .replace("{content}", buildContent());
    }
}
