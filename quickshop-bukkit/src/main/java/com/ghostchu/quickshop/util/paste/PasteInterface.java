package com.ghostchu.quickshop.util.paste;

import org.jetbrains.annotations.NotNull;

public interface PasteInterface {
    /**
     * Paste the text to pastebin
     *
     * @param text The text need to paste
     * @return The paste link
     * @throws Exception IOException if paste failed
     */
    String pasteTheText(@NotNull String text) throws Exception;

    /**
     * Paste the text to pastebin
     *
     * @param text The text need to paste
     * @return The paste index
     * @throws Exception IOException if paste failed
     */
    String pasteTheTextJson(@NotNull String text) throws Exception;

}
