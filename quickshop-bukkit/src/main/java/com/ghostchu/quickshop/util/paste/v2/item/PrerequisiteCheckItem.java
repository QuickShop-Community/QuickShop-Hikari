/*
 *  This file is a part of project QuickShop, the name is PrerequisiteCheckItem.java
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

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static j2html.TagCreator.*;

@AllArgsConstructor
@Data
public class PrerequisiteCheckItem implements SubPasteItem {
    private List<String> failedEntries;

    @Override
    public @NotNull String getTitle() {
        return "Prerequisite Check Warning";
    }

    @NotNull
    private String buildContent() {
        return div(
                p("""
                        We found that some of the prerequisite checks did not pass.
                        Since the failed checks may affect the accuracy of this report, we recommend that you first eliminate the errors listed below and create a new report.
                            """),
                each(failedEntries, entry -> p(span(entry)))
        ).render();
    }

    @Override
    public @NotNull String genBody() {
        return buildContent();
    }


}
