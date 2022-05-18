package com.ghostchu.quickshop.util.itempredicate;

import com.ghostchu.quickshop.util.itempredicate.rules.TestRule;
import lombok.AllArgsConstructor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AllArgsConstructor
public class ItemPredicate {
    private final List<TestRule> rules;

    public boolean test(@NotNull ItemStack itemStack) {
        if (itemStack.getType().isAir())
            throw new IllegalArgumentException("ItemStack is air");
        for (TestRule rule : rules) {
            if (!rule.test(itemStack)) {
                return false;
            }
        }
        return true;
    }
}
