package com.ghostchu.quickshop.util.itempredicate.rules;

import com.ghostchu.quickshop.util.itempredicate.method.MatchMethod;
import lombok.AllArgsConstructor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class DamageableRule implements TestRule {
    private final MatchMethod method;
    private final int value;

    @Override
    public boolean test(@NotNull ItemStack tester) {
        if (tester instanceof Damageable damageable) {
            int damage = damageable.getDamage();
            return switch (method) {
                case EQUALS, INCLUDE -> damage == value;
                case EXCLUDE, NOT_EQUALS -> damage != value;
                case BIGGER_THAN -> damage > value;
                case SMALLER_THAN -> damage < value;
            };
        }
        return true;
    }
}
