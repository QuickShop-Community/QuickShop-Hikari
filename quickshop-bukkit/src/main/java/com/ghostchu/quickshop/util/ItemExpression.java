package com.ghostchu.quickshop.util;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.registry.BuiltInRegistry;
import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionRegistry;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

@Deprecated(forRemoval = true)
public class ItemExpression {

  private final String item;
//    public static final String ITEM_REFERENCE_MARKER = "@";
//    public static final String ENCHANTMENT_MARKER = "%";
//    private final QuickShop plugin;
//    @Nullable
//    private final Function<ItemStack, Boolean> function;

  public ItemExpression(@NotNull QuickShop plugin, @NotNull String item) {

    this.item = item;
//        this.plugin = plugin;
//        if (item.startsWith(ITEM_REFERENCE_MARKER)) {
//            this.function = handleItemReference(item);
//        } else if (item.startsWith(ENCHANTMENT_MARKER)) {
//            this.function = parseEnchantment(item);
//        } else {
//            this.function = handleItemMaterial(item);
//        }
  }
//
//
//    private @NotNull Function<ItemStack, Boolean> handleItemReference(@NotNull String item) {
//        String reference = item.substring(1);
//        ItemStack stack = plugin.getItemMarker().get(reference);
//        return itemStack -> plugin.getItemMatcher().matches(stack, itemStack);
//    }
//
//    @Nullable
//    private Function<ItemStack, Boolean> parseEnchantment(@NotNull String s) {
//        // minecraft:sharpness|min|max
//        String input = s.substring(1);
//        // spilt with |
//        String[] split = input.split("\\|");
//        if (split.length < 1) {
//            plugin.logger().warn(s + " is not a valid enchantment.  Check your spelling or ID");
//            return null;
//        }
//        String key = split[0];
//        int minLevel = -1;
//        int maxLevel;
//        if (split.length > 1) {
//            if (StringUtils.isNumeric(split[1])) {
//                minLevel = Integer.parseInt(split[1]);
//            } else {
//                plugin.logger().warn("{} enchantment syntax error. Skipping...", s);
//                return null;
//            }
//        }
//        if (split.length > 2) {
//            if (StringUtils.isNumeric(split[2])) {
//                maxLevel = Integer.parseInt(split[2]);
//            } else {
//                plugin.logger().warn("{} enchantment syntax error. Skipping...", s);
//                return null;
//            }
//        } else {
//            maxLevel = -1;
//        }
//        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.fromString(key));
//        if (enchantment == null) {
//            plugin.logger().warn("{} is not a valid enchantment namespaced key. Skipping...", key);
//            return null;
//        }
//
//        int finalMinLevel = minLevel;
//        return itemStack -> {
//            int level = itemStack.getEnchantmentLevel(enchantment);
//            if (level == 0) {
//                return false;
//            }
//            if (finalMinLevel != -1 && level < finalMinLevel) {
//                return false;
//            }
//            //noinspection RedundantIfStatement
//            if (maxLevel != -1 && level > maxLevel) {
//                return false;
//            }
//            return true;
//        };
//    }
//
//    @Nullable
//    private Function<ItemStack, Boolean> handleItemMaterial(@NotNull String item) {
//        Material mat = Material.matchMaterial(item);
//        if (mat == null) {
//            plugin.logger().warn("Failed to read a ItemRule option, invalid item {}! Skipping...", item);
//            return null;
//        }
//        return itemStack -> itemStack.getType() == mat;
//    }

  @NotNull
  public Optional<Function<ItemStack, Boolean>> getFunction() {

    return Optional.of(itemStack->((ItemExpressionRegistry)QuickShop.getInstance().getRegistry().getRegistry(BuiltInRegistry.ITEM_EXPRESSION)).match(itemStack, item));
  }
}
