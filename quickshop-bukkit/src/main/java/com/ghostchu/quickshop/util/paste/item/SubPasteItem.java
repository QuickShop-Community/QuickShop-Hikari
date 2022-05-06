/*
 *  This file is a part of project QuickShop, the name is SubPasteItem.java
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

package com.ghostchu.quickshop.util.paste.item;

import org.jetbrains.annotations.NotNull;

public interface SubPasteItem extends PasteItem {
    /**
     * Generate and render the title part of this item
     *
     * @return the rendered title
     */
    @NotNull
    default String genTitle() {
        return "<h3># " + getTitle() + "</h3>";
    }

    /**
     * Returns this item's title (plain text), and will render to HTML
     *
     * @return the title
     */
    @NotNull
    String getTitle();

    /**
     * Generate and render the body part of this item
     *
     * @return the rendered body
     */
    @NotNull
    String genBody();

    /**
     * Render this item to HTML sources
     *
     * @return HTML sources
     */
    @Override
    default @NotNull String toHTML() {
        return genTitle() + genBody();
    }
}
