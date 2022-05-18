//package com.ghostchu.quickshop.util.itempredicate.rules;
//
//import com.ghostchu.quickshop.util.itempredicate.method.MatchMethod;
//import lombok.AllArgsConstructor;
//import org.bukkit.Color;
//import org.bukkit.inventory.meta.LeatherArmorMeta;
//import org.jetbrains.annotations.NotNull;
//
//@AllArgsConstructor
//public class LeatherArmorRule implements TestRule<LeatherArmorMeta> {
//    private final MatchMethod method;
//    private final Color value;
//
//    @Override
//    public boolean test(@NotNull LeatherArmorMeta tester) throws UnsupportedOperationException {
//        return switch (method) {
//            case EQUALS, INCLUDE -> tester.getColor().equals(value);
//            case NOT_EQUALS, EXCLUDE -> !tester.getColor().equals(value);
//            default -> throw new UnsupportedOperationException("Unsupported match method: " + method);
//        };
//    }
//}
