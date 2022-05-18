//package com.ghostchu.quickshop.util.itempredicate;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import org.bukkit.Material;
//import org.bukkit.attribute.Attribute;
//import org.bukkit.attribute.AttributeModifier;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.Map;
//import java.util.Optional;
//
///**
// * The predicate rule contiainer.
// * Every getter returns an Optional.
// * If Optional is empty, the target rule should be skipped.
// * Otherwise the target rule should be applied.
// */
//@Data
//@AllArgsConstructor
//public class PredicateRule {
//    @Nullable
//    private final Material material;
//    private int amount;
//    private Map<Attribute, AttributeModifier> attributes;
//    private int customModelData;
//
//    /**
//     * Gets the material of the rule.
//     *
//     * @return The material of the rule.
//     */
//    @NotNull
//    public Optional<Material> getMaterial() {
//        return Optional.ofNullable(material);
//    }
//
//    /**
//     * Gets the item amount restriction of the rule.
//     *
//     * @return The item amount restriction of the rule.
//     */
//    @NotNull
//    public Optional<Integer> getAmount() {
//        if (amount < 1)
//            return Optional.empty();
//        return Optional.of(amount);
//    }
//
//    /**
//     * Gets the item attributes of the rule.
//     *
//     * @return The item attributes of the rule.
//     */
//    @NotNull
//    public Optional<Map<Attribute, AttributeModifier>> getAttributes() {
//        if (attributes.isEmpty())
//            return Optional.empty();
//        return Optional.of(attributes);
//    }
//
//    /**
//     * Gets the custom model data of the rule.
//     *
//     * @return The custom model data of the rule.
//     */
//    @NotNull
//    public Optional<Integer> getCustomModelData() {
//        if (customModelData < 1)
//            return Optional.empty();
//        return Optional.of(customModelData);
//    }
//}
