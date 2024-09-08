package com.ghostchu.quickshop.util;

import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple utility class for handling container items such as Shulker Boxes.
 * @author TauCubed
 */
public class ItemContainerUtil {

    /**
     * Checks if the provided ItemStack is a container, if so, flattens the contents of the container
     * @param stack the stack to check
     * @param recursive should the check function recursively? (chests inside chests inside che...)
     * @param includeInput should the input ItemStack be included in the results?
     * @return a list of flattened contents
     */
    public static List<ItemStack> flattenContents(ItemStack stack, boolean recursive, boolean includeInput) {
        final List<ItemStack> flat = new ArrayList<>();
        if (includeInput) {
            flat.add(stack);
        }

        flattenContents0(flat, stack, recursive);

        return flat;
    }

    private static void flattenContents0(List<ItemStack> flat, ItemStack stack, boolean recursive) {
        if (stack.getItemMeta() instanceof BlockStateMeta meta
                && meta.hasBlockState()
                && meta.getBlockState() instanceof Container container) {
            for (ItemStack containerStack : container.getInventory()) {
                if (containerStack != null) {
                    flat.add(containerStack);
                    if (recursive) {
                        flattenContents0(flat, containerStack, recursive);
                    }
                }
            }
        }
    }

}
