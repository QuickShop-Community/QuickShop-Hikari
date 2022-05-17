package com.ghostchu.quickshop.util.itempredicate.rules;

import lombok.AllArgsConstructor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
public class BookMetaRule implements TestRule {
    @Nullable
    private final String author;
    @Nullable
    private final String title;

    @Override
    public boolean test(@NotNull ItemStack tester) throws UnsupportedOperationException {
        if (tester instanceof BookMeta meta) {
            if (author != null) {
                if (!author.equals(meta.getAuthor())) {
                    return false;
                }
            }
            if (title != null) {
                return title.equals(meta.getTitle());
            }
        }
        return true;
    }
}
