package com.ghostchu.quickshop.util.itempredicate.rules;

import com.ghostchu.quickshop.util.itempredicate.method.MatchMethod;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class IntegerRule implements TestRule<Integer> {
    private final MatchMethod method;
    private final int value;

    @Override
    public boolean test(@NotNull Integer tester) {
        return switch (method) {
            case EQUALS, INCLUDE -> tester == value;
            case EXCLUDE, NOT_EQUALS -> tester != value;
            case BIGGER_THAN -> tester > value;
            case SMALLER_THAN -> tester < value;
        };
    }
}
