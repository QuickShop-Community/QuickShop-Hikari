package com.ghostchu.quickshop.util.itempredicate.rules;

import com.ghostchu.quickshop.util.itempredicate.method.MatchMethod;
import lombok.AllArgsConstructor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@AllArgsConstructor
public class AttributeModifierRule implements TestRule<Map<Attribute, AttributeModifier>> {
    private final MatchMethod method;
    private final Map<Attribute, AttributeModifier> value;

    @Override
    public boolean test(@NotNull Map<Attribute, AttributeModifier> tester) {
        return switch (method) {
            case EQUALS -> tester.equals(value);
            case INCLUDE -> mapInclude(value, tester);
            case EXCLUDE -> mapExclude(value, tester);
            default -> throw new UnsupportedOperationException("Unsupported match method: " + method);
        };
    }
}
