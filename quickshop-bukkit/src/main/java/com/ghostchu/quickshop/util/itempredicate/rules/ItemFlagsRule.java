package com.ghostchu.quickshop.util.itempredicate.rules;

import com.ghostchu.quickshop.util.itempredicate.method.MatchMethod;
import lombok.AllArgsConstructor;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AllArgsConstructor
public class ItemFlagsRule implements TestRule<List<ItemFlag>> {
    @NotNull
    private final MatchMethod method;

    @NotNull
    private final ItemFlag value;

    public boolean test(@NotNull List<ItemFlag> tester) {
        return switch (method) {
            case EQUALS -> {
                if (tester.size() != 1) yield false;
                yield tester.get(0).equals(value);
            }
            case EXCLUDE -> !tester.contains(value);
            case INCLUDE -> tester.contains(value);
            default -> throw new UnsupportedOperationException("Unsupported match method: " + method);
        };
    }

}
