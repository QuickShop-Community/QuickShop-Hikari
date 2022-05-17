package com.ghostchu.quickshop.util.itempredicate.rules;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Axolotl;
import org.bukkit.inventory.meta.AxolotlBucketMeta;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class AxolotlBucketMetaRule implements TestRule<AxolotlBucketMeta> {
    private final Axolotl.Variant value;

    @Override
    public boolean test(@NotNull AxolotlBucketMeta tester) {
        return tester.getVariant() == value;
    }
}
