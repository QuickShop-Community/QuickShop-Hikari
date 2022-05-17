package com.ghostchu.quickshop.util.itempredicate.rules;

import lombok.AllArgsConstructor;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class SkullMetaRule implements TestRule<SkullMeta> {
    @NotNull
    private final String owner;

    @Override
    public boolean test(@NotNull SkullMeta tester) throws UnsupportedOperationException {
        if (tester.getOwnerProfile() == null) {
            //noinspection deprecation
            return owner.equals(tester.getOwner());
        } else {
            return owner.equals(tester.getOwnerProfile().getName());
        }
    }
}
