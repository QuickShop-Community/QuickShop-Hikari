//package com.ghostchu.quickshop.util.itempredicate.rules;
//
//import com.ghostchu.quickshop.util.itempredicate.method.MatchMethod;
//import lombok.AllArgsConstructor;
//import org.bukkit.inventory.meta.SuspiciousStewMeta;
//import org.bukkit.potion.PotionEffect;
//import org.bukkit.potion.PotionEffectType;
//import org.jetbrains.annotations.NotNull;
//
//@AllArgsConstructor
//public class SuspiciousStewMetaRule implements TestRule<SuspiciousStewMeta> {
//    @NotNull
//    private final MatchMethod method;
//    @NotNull
//    private final PotionEffectType value;
//
//    @Override
//    public boolean test(@NotNull SuspiciousStewMeta tester) throws UnsupportedOperationException {
//        return switch (method) {
//            case EQUALS -> {
//                if (tester.getCustomEffects().size() != 1) yield false;
//                yield tester.getCustomEffects().get(0).getType() == value;
//            }
//            case NOT_EQUALS ->
//                    tester.getCustomEffects().size() != 1 || tester.getCustomEffects().get(0).getType() != value;
//            case INCLUDE -> tester.getCustomEffects().stream().map(PotionEffect::getType).anyMatch(value::equals);
//            case EXCLUDE -> tester.getCustomEffects().stream().map(PotionEffect::getType).noneMatch(value::equals);
//            default -> throw new UnsupportedOperationException("Unsupported method: " + method);
//        };
//    }
//}
