package com.ghostchu.quickshop.util.itempredicate.rules;

import lombok.AllArgsConstructor;
import org.bukkit.inventory.meta.CompassMeta;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class CompassMetaRule implements TestRule<CompassMeta> {
    private final boolean valueHasLodestone;

    @Override
    public boolean test(@NotNull CompassMeta tester) {
        return tester.hasLodestone() == valueHasLodestone;
    }
}
