package com.ghostchu.quickshop.util.itempredicate.rules;

import com.ghostchu.quickshop.util.itempredicate.method.MatchMethod;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;

@AllArgsConstructor
public class LoresRule implements TestRule<List<String>> {
    @NotNull
    private final MatchMethod method;

    @NotNull
    private final List<String> value;

    public boolean test(@NotNull List<String> tester) {
        return switch (method) {
            case EQUALS -> value.equals(tester);
            case EXCLUDE -> {
                for (String s : tester) {
                    if (value.contains(s)) {
                        yield false;
                    }
                }
                yield true;
            }
            case INCLUDE -> new HashSet<>(tester).containsAll(value);
            default -> throw new UnsupportedOperationException("Unsupported match method: " + method);
        };
    }

}
