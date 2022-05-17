package com.ghostchu.quickshop.util.itempredicate.rules;

import com.ghostchu.quickshop.QuickShop;
import lombok.AllArgsConstructor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class ItemStackCompareRule implements TestRule<ItemStack> {
    @NotNull
    private final ItemStack itemStack;

    @Override
    public boolean test(@NotNull ItemStack tester) throws UnsupportedOperationException {
        return QuickShop.getInstance().getItemMatcher().matches(itemStack, tester);
    }
}
