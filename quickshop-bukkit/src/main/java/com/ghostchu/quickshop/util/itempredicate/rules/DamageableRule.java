package com.ghostchu.quickshop.util.itempredicate.rules;

import com.ghostchu.quickshop.util.itempredicate.method.MatchMethod;
import lombok.AllArgsConstructor;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class DamageableRule implements TestRule<Damageable> {
    private final MatchMethod method;
    private final int value;

    @Override
    public boolean test(@NotNull Damageable tester) {
        return switch (method) {
            case EQUALS, INCLUDE -> tester.getDamage() == value;
            case BIGGER_THAN -> tester.getDamage() > value;
            case SMALLER_THAN -> tester.getDamage() < value;
            case EXCLUDE -> tester.getDamage() != value;
        };
    }
}
