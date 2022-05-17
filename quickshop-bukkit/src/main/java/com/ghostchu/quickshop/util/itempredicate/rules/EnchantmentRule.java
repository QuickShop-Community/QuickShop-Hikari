package com.ghostchu.quickshop.util.itempredicate.rules;

import com.ghostchu.quickshop.util.itempredicate.method.MatchMethod;
import lombok.AllArgsConstructor;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

@AllArgsConstructor
public class EnchantmentRule implements TestRule<Map<Enchantment, Integer>> {
    private final MatchMethod method;
    private final Enchantment enchantment;
    private final int level;

    @Override
    public boolean test(@NotNull Map<Enchantment, Integer> tester) {
        return switch (method) {
            case EQUALS -> {
                Set<Map.Entry<Enchantment, Integer>> set = tester.entrySet();
                if (set.size() != 1) yield false;
                Map.Entry<Enchantment, Integer> entry = set.iterator().next();
                yield entry.getKey().equals(enchantment) && entry.getValue() == level;
            }
            case NOT_EQUALS -> {
                Set<Map.Entry<Enchantment, Integer>> set = tester.entrySet();
                if (set.size() != 1) yield true;
                Map.Entry<Enchantment, Integer> entry = set.iterator().next();
                yield !entry.getKey().equals(enchantment) || entry.getValue() != level;
            }
            case EXCLUDE -> mapExclude(Map.of(enchantment, level), tester);
            case INCLUDE -> mapInclude(Map.of(enchantment, level), tester);
            default -> throw new UnsupportedOperationException("Unsupported match method: " + method);
        };
    }
}
