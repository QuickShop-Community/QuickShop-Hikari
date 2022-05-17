package com.ghostchu.quickshop.util.itempredicate.rules;

import com.ghostchu.quickshop.util.itempredicate.method.MatchMethod;
import lombok.AllArgsConstructor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

@AllArgsConstructor
public class EnchantmentRule implements TestRule {
    private final MatchMethod method;
    private final Enchantment enchantment;
    private final int level;

    public boolean test(@NotNull ItemStack tester) {
        Map<org.bukkit.enchantments.Enchantment, Integer> enchants = tester.getEnchantments();
        Set<Map.Entry<Enchantment, Integer>> set = enchants.entrySet();
        return switch (method) {
            case EQUALS -> {
                if (set.size() != 1) yield false;
                Map.Entry<Enchantment, Integer> entry = set.iterator().next();
                yield entry.getKey().equals(enchantment) && entry.getValue() == level;
            }
            case NOT_EQUALS -> {
                if (set.size() != 1) yield true;
                Map.Entry<Enchantment, Integer> entry = set.iterator().next();
                yield !entry.getKey().equals(enchantment) || entry.getValue() != level;
            }
            case EXCLUDE -> mapExclude(Map.of(enchantment, level), enchants);
            case INCLUDE -> mapInclude(Map.of(enchantment, level), enchants);
            default -> throw new UnsupportedOperationException("Unsupported match method: " + method);
        };
    }
}
