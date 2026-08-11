package com.ghostchu.quickshop.api.inventory;

import org.bukkit.inventory.ItemStack;

import java.util.Map;

public record ItemRemoveResult(Map<Integer, ItemStack> leftovers, Map<Integer, ItemStack> removed) {

}