//package com.ghostchu.quickshop.util.itempredicate.rules;
//
//import com.ghostchu.quickshop.util.itempredicate.method.MatchMethod;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import org.bukkit.inventory.ItemStack;
//import org.jetbrains.annotations.NotNull;
//
//@AllArgsConstructor
//@Data
//public class DisplayNameRule implements TestRule {
//    @NotNull
//    private final MatchMethod method;
//    @NotNull
//    private final String value;
//
//    public boolean test(@NotNull ItemStack tester) {
//        String display = tester.getItemMeta().getDisplayName();
//        return switch (method) {
//            case EQUALS -> value.equals(display);
//            case NOT_EQUALS -> !value.equals(display);
//            case EXCLUDE -> !value.contains(display);
//            case INCLUDE -> value.contains(display);
//            default -> throw new UnsupportedOperationException("Unsupported match method: " + method);
//        };
//    }
//
//}
