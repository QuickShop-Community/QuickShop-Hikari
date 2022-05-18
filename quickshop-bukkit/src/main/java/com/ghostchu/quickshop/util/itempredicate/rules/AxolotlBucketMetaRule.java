//package com.ghostchu.quickshop.util.itempredicate.rules;
//
//import com.ghostchu.quickshop.util.itempredicate.method.MatchMethod;
//import lombok.AllArgsConstructor;
//import org.apache.commons.lang3.NotImplementedException;
//import org.bukkit.entity.Axolotl;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.meta.AxolotlBucketMeta;
//import org.jetbrains.annotations.NotNull;
//
//@AllArgsConstructor
//public class AxolotlBucketMetaRule implements TestRule {
//    @NotNull
//    private final MatchMethod method;
//    @NotNull
//    private final Axolotl.Variant value;
//
//    @Override
//    public boolean test(@NotNull ItemStack tester) {
//        if (tester.getItemMeta() instanceof AxolotlBucketMeta meta) {
//            return switch (method) {
//                case EQUALS, INCLUDE -> meta.getVariant() == value;
//                case NOT_EQUALS, EXCLUDE -> meta.getVariant() != value;
//                default -> throw new NotImplementedException("Method " + method + " is not implemented");
//            };
//        }
//        return true;
//    }
//}
