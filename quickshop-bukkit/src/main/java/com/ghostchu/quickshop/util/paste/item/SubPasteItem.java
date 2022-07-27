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
