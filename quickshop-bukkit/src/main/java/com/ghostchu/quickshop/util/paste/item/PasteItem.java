package com.ghostchu.quickshop.util.paste.item;

import org.jetbrains.annotations.NotNull;

public interface PasteItem {
    /**
     * Render this item to HTML sources
     *
     * @return HTML sources
     */
    @NotNull
    String toHTML();
}
