package com.ghostchu.quickshop.util.itempredicate.rules;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface TestRule<T> {
    boolean test(@NotNull T tester) throws UnsupportedOperationException;

    default boolean mapInclude(@NotNull Map<?, ?> value, @NotNull Map<?, ?> tester) {
        for (Map.Entry<?, ?> entry : value.entrySet()) {
            if (!tester.containsKey(entry.getKey()) || !tester.get(entry.getKey()).equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    default boolean mapExclude(@NotNull Map<?, ?> value, @NotNull Map<?, ?> tester) {
        for (Map.Entry<?, ?> entry : value.entrySet()) {
            if (tester.containsKey(entry.getKey()) && tester.get(entry.getKey()).equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }
}
