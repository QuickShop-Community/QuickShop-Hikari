package com.ghostchu.quickshop.menu.browse;
/*
 * QuickShop-Hikari
 * Copyright (C) 2024 Daniel "creatorfromhell" Vidmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.ItemMatcher;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.shop.ContainerShop;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * MarketUtils - Utility class for market/browse operations Handles grouping shops by item,
 * filtering, sorting, and searching
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public final class MarketUtils {

  private MarketUtils() {
    // Utility class
  }

  /**
   * Group shops by item type using the ItemMatcher
   *
   * @param shops List of shops to group
   *
   * @return List of MarketItemGroups
   */
  @NotNull
  public static List<MarketItemGroup> groupShopsByItem(@NotNull final List<Shop> shops) {

    final List<MarketItemGroup> groups = new ArrayList<>();
    final Map<Material, List<MarketItemGroup>> groupsByMat = new EnumMap<>(Material.class);
    final ItemMatcher matcher = QuickShop.getInstance().getItemMatcher();

    for(final Shop shop : shops) {
      MarketItemGroup matchingGroup = null;
      final List<MarketItemGroup> matGroups = groupsByMat.computeIfAbsent(shop.getItem().getType(), k->new ArrayList<>());
      // Find existing group that matches this shop's item
      for(final MarketItemGroup group : matGroups) {
        if(matcher.matches(group.getRepresentativeItem(), shop.getItem())) {
          matchingGroup = group;
          break;
        }
      }

      // Create new group if no match found
      if(matchingGroup == null) {
        matchingGroup = new MarketItemGroup(shop.getItem());
        matGroups.add(matchingGroup);
        groups.add(matchingGroup);
      }

      matchingGroup.addShop(shop);
    }

    // Calculate statistics for all groups
    for(final MarketItemGroup group : groups) {
      group.calculateStatistics();
    }

    return groups;
  }

  /**
   * Filter shops based on filter mode
   *
   * @param shops      List of shops to filter
   * @param filterMode The filter mode to apply
   *
   * @return Filtered list of shops
   */
  @NotNull
  public static List<Shop> filterShops(@NotNull final List<Shop> shops,
                                       @NotNull final BrowseFilterMode filterMode) {

    final boolean disableUnlimitedBrowse = QuickShop.getInstance().getConfig().getBoolean("shop.disable-unlimited-browse", false);
    return switch(filterMode) {
      case ALL -> new ArrayList<>(shops.stream().filter(shop -> !disableUnlimitedBrowse || !shop.isUnlimited()).toList());
      case BUYING -> shops.stream()
              .filter(shop -> !disableUnlimitedBrowse || !shop.isUnlimited())
              .filter(Shop::isBuying)
              .toList();
      case SELLING -> shops.stream()
              .filter(shop -> !disableUnlimitedBrowse || !shop.isUnlimited())
              .filter(Shop::isSelling)
              .toList();
    };
  }

  /**
   * Filter shops to only show those with stock/space available. Uses database cache to avoid Folia
   * cross-region block access issues.
   *
   * @param shops     List of shops to filter
   * @param stockOnly Whether to filter to stock only
   *
   * @return Filtered list of shops
   */
  @NotNull
  public static List<Shop> filterByStock(@NotNull final List<Shop> shops, final boolean stockOnly) {

    if(!stockOnly) {
      return new ArrayList<>(shops);
    }
    return shops.stream()
            .filter(shop->{
              if(shop.isUnlimited()) return true;
              // For selling shops, check stock; for buying shops, check space
              // Use database cache to avoid Folia cross-region block access
              if(shop.isSelling()) {
                return getStockFromCache(shop) > 0;
              } else {
                return getSpaceFromCache(shop) > 0;
              }
            })
            .toList();
  }

  /**
   * Filter item groups based on filter mode
   *
   * @param groups     List of groups to filter
   * @param filterMode The filter mode to apply
   *
   * @return Filtered list of groups
   */
  @NotNull
  public static List<MarketItemGroup> filterGroups(@NotNull final List<MarketItemGroup> groups,
                                                   @NotNull final BrowseFilterMode filterMode) {

    return switch(filterMode) {
      case ALL -> new ArrayList<>(groups);
      case BUYING -> groups.stream()
              .filter(MarketItemGroup::hasBuyingShops)
              .toList();
      case SELLING -> groups.stream()
              .filter(MarketItemGroup::hasSellingShops)
              .toList();
    };
  }

  /**
   * Filter item groups to only show those with stock/space available. Uses database cache to avoid
   * Folia cross-region block access issues.
   *
   * @param groups    List of groups to filter
   * @param stockOnly Whether to filter to stock only
   *
   * @return Filtered list of groups
   */
  @NotNull
  public static List<MarketItemGroup> filterGroupsByStock(@NotNull final List<MarketItemGroup> groups,
                                                          final boolean stockOnly) {

    if(!stockOnly) {
      return new ArrayList<>(groups);
    }
    return groups.stream()
            .filter(group->{
              // Check if any shop in the group has stock/space
              // Use database cache to avoid Folia cross-region block access
              return group.getShops().stream().anyMatch(shop->{
                if(shop.isUnlimited()) return true;
                if(shop.isSelling()) {
                  return getStockFromCache(shop) > 0;
                } else {
                  return getSpaceFromCache(shop) > 0;
                }
              });
            })
            .toList();
  }

  /**
   * Sort shops based on sort mode. Uses database cache for stock sorting to avoid Folia
   * cross-region block access issues.
   *
   * @param shops    List of shops to sort
   * @param sortMode The sort mode to apply
   *
   * @return Sorted list of shops
   */
  @NotNull
  public static List<Shop> sortShops(@NotNull final List<Shop> shops,
                                     @NotNull final BrowseSortMode sortMode) {

    final List<Shop> sorted = new ArrayList<>(shops);

    switch(sortMode) {
      case PRICE_ASC -> {

        sorted.sort((a, b) -> a.comparePrice(b.price(), false));
      }
      case PRICE_DESC -> sorted.sort((a, b) -> a.comparePrice(b.price(), true));
      case STOCK -> sorted.sort(Comparator.comparingInt(MarketUtils::getStockFromCache).reversed());
      case NAME -> sorted.sort(Comparator.comparing(shop->
                                                            CommonUtil.prettifyText(shop.getItem().getType().name())));
    }

    return sorted;
  }

  /**
   * Sort item groups based on sort mode
   *
   * @param groups   List of groups to sort
   * @param sortMode The sort mode to apply
   *
   * @return Sorted list of groups
   */
  @NotNull
  public static List<MarketItemGroup> sortGroups(@NotNull final List<MarketItemGroup> groups,
                                                 @NotNull final BrowseSortMode sortMode) {

    final List<MarketItemGroup> sorted = new ArrayList<>(groups);

    switch(sortMode) {
      case PRICE_ASC -> sorted.sort(Comparator.comparingDouble(group->{
        // Use selling price if available, otherwise buying price
        if(group.hasSellingShops()) {
          return group.getSellingMinPrice();
        }
        return group.getBuyingMinPrice();
      }));
      case PRICE_DESC -> sorted.sort(Comparator.comparingDouble((final MarketItemGroup group)->{
        if(group.hasSellingShops()) {
          return group.getSellingMaxPrice();
        }
        return group.getBuyingMaxPrice();
      }).reversed());
      case STOCK ->
              sorted.sort(Comparator.comparingInt(MarketItemGroup::getSellingTotalStock).reversed());
      case NAME -> sorted.sort(Comparator.comparing(MarketItemGroup::getItemDisplayName));
    }

    return sorted;
  }

  /**
   * Search shops by item name
   *
   * @param shops       List of shops to search
   * @param searchQuery The search query (item name)
   *
   * @return List of shops matching the search query
   */
  @NotNull
  public static List<Shop> searchShops(@NotNull final List<Shop> shops,
                                       @Nullable final String searchQuery) {

    if(searchQuery == null || searchQuery.trim().isEmpty()) {
      return new ArrayList<>(shops);
    }

    final String query = searchQuery.toLowerCase(Locale.ROOT).trim();

    return shops.stream()
            .filter(shop->matchesSearch(shop.getItem(), query))
            .toList();
  }

  /**
   * Search item groups by item name
   *
   * @param groups      List of groups to search
   * @param searchQuery The search query (item name)
   *
   * @return List of groups matching the search query
   */
  @NotNull
  public static List<MarketItemGroup> searchGroups(@NotNull final List<MarketItemGroup> groups,
                                                   @Nullable final String searchQuery) {

    if(searchQuery == null || searchQuery.trim().isEmpty()) {
      return new ArrayList<>(groups);
    }

    final String query = searchQuery.toLowerCase(Locale.ROOT).trim();

    return groups.stream()
            .filter(group->matchesSearch(group.getRepresentativeItem(), query))
            .toList();
  }

  /**
   * Check if an item matches a search query
   *
   * @param item  The item to check
   * @param query The search query (lowercase)
   *
   * @return true if the item matches
   */
  private static boolean matchesSearch(@NotNull final ItemStack item, @NotNull final String query) {
    // Check material name
    final String materialName = item.getType().name().toLowerCase(Locale.ROOT).replace("_", " ");
    if(materialName.contains(query)) {
      return true;
    }

    // Check prettified name
    final String prettyName = CommonUtil.prettifyText(item.getType().name()).toLowerCase(Locale.ROOT);
    if(prettyName.contains(query)) {
      return true;
    }

    // Check custom display name if present
    if(item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
      final String displayName = item.getItemMeta().getDisplayName().toLowerCase(Locale.ROOT);
      return displayName.contains(query);
    }

    return false;
  }

  /**
   * Apply all filters, search, and sorting to shops
   *
   * @param shops       The original list of shops
   * @param filterMode  The filter mode
   * @param sortMode    The sort mode
   * @param searchQuery The search query (can be null)
   * @param stockOnly   Whether to only show shops with stock/space
   *
   * @return Processed list of shops
   */
  @NotNull
  public static List<Shop> processShops(@NotNull final List<Shop> shops,
                                        @NotNull final BrowseFilterMode filterMode,
                                        @NotNull final BrowseSortMode sortMode,
                                        @Nullable final String searchQuery,
                                        final boolean stockOnly) {

    List<Shop> result = new ArrayList<>(shops);
    result = filterShops(result, filterMode);
    result = filterByStock(result, stockOnly);
    result = searchShops(result, searchQuery);
    result = sortShops(result, sortMode);
    return result;
  }

  /**
   * Apply all filters, search, and sorting to item groups
   *
   * @param shops       The original list of shops
   * @param filterMode  The filter mode
   * @param sortMode    The sort mode
   * @param searchQuery The search query (can be null)
   * @param stockOnly   Whether to only show groups with stock/space
   *
   * @return Processed list of item groups
   */
  @NotNull
  public static List<MarketItemGroup> processGroups(@NotNull final List<Shop> shops,
                                                    @NotNull final BrowseFilterMode filterMode,
                                                    @NotNull final BrowseSortMode sortMode,
                                                    @Nullable final String searchQuery,
                                                    final boolean stockOnly) {
    // First filter shops, then group them
    List<Shop> filteredShops = filterShops(shops, filterMode);
    filteredShops = filterByStock(filteredShops, stockOnly);
    filteredShops = searchShops(filteredShops, searchQuery);

    // Group the filtered shops
    List<MarketItemGroup> groups = groupShopsByItem(filteredShops);

    // Sort the groups
    groups = sortGroups(groups, sortMode);

    return groups;
  }

  /**
   * Get stock count from database cache. This avoids Folia cross-region block access issues by
   * using cached data instead of directly accessing the shop's inventory.
   *
   * @param shop The shop to get stock for
   *
   * @return Stock count, or -1 for unlimited shops, 0 for errors/uninitialized
   */
  public static int getStockFromCache(@NotNull final Shop shop) {

    if(shop.isUnlimited()) {
      return -1;
    }

    return Math.max(((ContainerShop) shop).getInventoryCountCache().getStock(), 0);
  }

  /**
   * Get space count from database cache. This avoids Folia cross-region block access issues by
   * using cached data instead of directly accessing the shop's inventory.
   *
   * @param shop The shop to get space for
   *
   * @return Space count, or -1 for unlimited shops, 0 for errors/uninitialized
   */
  public static int getSpaceFromCache(@NotNull final Shop shop) {

    if(shop.isUnlimited()) {
      return -1;
    }

    return Math.max(((ContainerShop) shop).getInventoryCountCache().getSpace(), 0);
  }
}
