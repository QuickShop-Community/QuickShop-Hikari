package com.ghostchu.quickshop.util.itempredicate.rules;

import com.ghostchu.quickshop.util.itempredicate.method.MatchMethod;
import lombok.AllArgsConstructor;
import org.bukkit.inventory.meta.Repairable;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class RepairableRule implements TestRule<Repairable> {
    private final MatchMethod method;
    private final int repairCost;

    @Override
    public boolean test(@NotNull Repairable tester) throws UnsupportedOperationException {
        return switch (method) {
            case EQUALS, INCLUDE -> tester.getRepairCost() == repairCost;
            case NOT_EQUALS, EXCLUDE -> tester.getRepairCost() != repairCost;
            case SMALLER_THAN -> tester.getRepairCost() < repairCost;
            case BIGGER_THAN -> tester.getRepairCost() > repairCost;
        };
    }
}
