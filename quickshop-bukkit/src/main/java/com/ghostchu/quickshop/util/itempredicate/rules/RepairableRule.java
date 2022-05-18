//package com.ghostchu.quickshop.util.itempredicate.rules;
//
//import com.ghostchu.quickshop.util.itempredicate.method.MatchMethod;
//import lombok.AllArgsConstructor;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.meta.Repairable;
//import org.jetbrains.annotations.NotNull;
//
//@AllArgsConstructor
//public class RepairableRule implements TestRule {
//    private final MatchMethod method;
//    private final int value;
//
//    @Override
//    public boolean test(@NotNull ItemStack tester) {
//        if (tester instanceof Repairable meta) {
//            return switch (method) {
//                case EQUALS, INCLUDE -> meta.getRepairCost() == value;
//                case EXCLUDE, NOT_EQUALS -> meta.getRepairCost() != value;
//                case BIGGER_THAN -> meta.getRepairCost() > value;
//                case SMALLER_THAN -> meta.getRepairCost() < value;
//            };
//        }
//        return true;
//    }
//}
