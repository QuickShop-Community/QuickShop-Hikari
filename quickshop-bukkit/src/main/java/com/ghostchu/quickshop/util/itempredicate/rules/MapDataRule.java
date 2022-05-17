package com.ghostchu.quickshop.util.itempredicate.rules;

import com.ghostchu.quickshop.util.itempredicate.method.MatchMethod;
import lombok.AllArgsConstructor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class MapDataRule implements TestRule {
    private final MatchMethod method;
    private final int value;

    @Override
    public boolean test(@NotNull ItemStack tester) {
        if (tester instanceof MapMeta mapMeta) {
            //noinspection deprecation
            int id = mapMeta.getMapId();
            return switch (method) {
                case EQUALS, INCLUDE -> id == value;
                case EXCLUDE, NOT_EQUALS -> id != value;
                case BIGGER_THAN -> id > value;
                case SMALLER_THAN -> id < value;
            };
        }
        return true;
    }
}
