/*
 * This file is a part of project QuickShop, the name is ChatSheetPrinter.java
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

package org.maxgamer.quickshop.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;


@AllArgsConstructor
@Getter
@Setter
/*
 A utils for print sheet on chat.
*/
public class ChatSheetPrinter {
    private final CommandSender p;

    public void printCenterLine(@NotNull Component text) {
        if (Util.isEmptyComponent(text))
            return;

        MsgUtil.sendDirectMessage(p,
                QuickShop.getInstance().text().of(p, "tableformat.left_half_line").forLocale()
                        .append(text)
                        .append(QuickShop.getInstance().text().of(p, "tableformat.right_half_line").forLocale()));
    }

    public void printExecutableCmdLine(
            @NotNull Component text, @NotNull Component hoverText, @NotNull String executeCmd) {
        MsgUtil.sendDirectMessage(p,
                text.hoverEvent(HoverEvent.showText(hoverText))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, executeCmd)));


    }

    private void printFullLine() {
        MsgUtil.sendDirectMessage(p, QuickShop.getInstance().text().of(p, "tableformat.full_line").forLocale());
    }

    public void printFooter() {
        printFullLine();
    }

    public void printHeader() {
        printFullLine();
    }

    public void printLine(@NotNull Component component) {
        if (Util.isEmptyComponent(component))
            return;
        MsgUtil.sendDirectMessage(p, QuickShop.getInstance().text().of(p, "tableformat.left_begin").forLocale()
                .append(component));
    }

    public void printSuggestedCmdLine(
            @NotNull Component text, @NotNull Component hoverText, @NotNull String suggestCmd) {

        MsgUtil.sendDirectMessage(p,
                text.hoverEvent(HoverEvent.showText(hoverText))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestCmd)));
    }

}
