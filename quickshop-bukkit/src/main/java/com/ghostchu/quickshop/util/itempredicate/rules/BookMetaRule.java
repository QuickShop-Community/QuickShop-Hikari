package com.ghostchu.quickshop.util.itempredicate.rules;

import lombok.AllArgsConstructor;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
public class BookMetaRule implements TestRule<BookMeta> {
    @Nullable
    private final String author;
    @Nullable
    private final String title;

    @Override
    public boolean test(@NotNull BookMeta tester) throws UnsupportedOperationException {
        if (author != null) {
            if (!author.equals(tester.getAuthor())) {
                return false;
            }
        }
        if (title != null) {
            return title.equals(tester.getTitle());
        }
        return true;
    }
}
