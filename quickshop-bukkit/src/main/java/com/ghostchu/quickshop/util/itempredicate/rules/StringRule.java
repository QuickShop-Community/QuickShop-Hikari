package com.ghostchu.quickshop.util.itempredicate.rules;

import com.ghostchu.quickshop.util.itempredicate.method.MatchMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Data
public class StringRule implements TestRule<String> {
    @NotNull
    private final MatchMethod method;
    @NotNull
    private final String value;

    public boolean test(@NotNull String tester) {
        return switch (method) {
            case EQUALS -> value.equals(tester);
            case EXCLUDE -> !value.contains(tester);
            case INCLUDE -> value.contains(tester);
            default -> throw new UnsupportedOperationException("Unsupported match method: " + method);
        };
    }

}
