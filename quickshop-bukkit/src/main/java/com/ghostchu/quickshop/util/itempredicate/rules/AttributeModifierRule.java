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
    private final Attribute attribute;
    private final AttributeModifier modifier;

    @Override
    public boolean test(@NotNull Map<Attribute, AttributeModifier> tester) {
        return switch (method) {
            case EQUALS -> {
                if (tester.size() != 1) yield false;
                AttributeModifier testerModifier = tester.get(attribute);
                yield testerModifier != null && testerModifier.getAmount() == modifier.getAmount() && testerModifier.getName().equals(modifier.getName());
            }
            case NOT_EQUALS -> {
                if (tester.size() == 1) {
                    AttributeModifier testerModifier = tester.get(attribute);
                    yield testerModifier != null && testerModifier.equals(modifier);
                }
                yield true;
            }
            case INCLUDE -> tester.get(attribute) != null && tester.get(attribute).equals(modifier);
            case EXCLUDE -> tester.get(attribute) == null || !tester.get(attribute).equals(modifier);
            default -> throw new UnsupportedOperationException("Unsupported match method: " + method);
        };
    }
}
