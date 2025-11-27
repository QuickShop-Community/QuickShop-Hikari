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

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * MarketItemGroup - Represents a group of shops selling/buying the same item
 * with aggregated price statistics for market overview
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class MarketItemGroup {
  
  private final ItemStack representativeItem;
  private final List<Shop> shops;
  private final List<Shop> sellingShops;
  private final List<Shop> buyingShops;
  
  // Price statistics for selling shops
  private double sellingMinPrice;
  private double sellingMaxPrice;
  private double sellingAvgPrice;
  private double sellingMedianPrice;
  private int sellingTotalStock;
  
  // Price statistics for buying shops
  private double buyingMinPrice;
  private double buyingMaxPrice;
  private double buyingAvgPrice;
  private double buyingMedianPrice;
  private int buyingTotalSpace;
  
  public MarketItemGroup(@NotNull final ItemStack representativeItem) {
    this.representativeItem = representativeItem.clone();
    this.shops = new ArrayList<>();
    this.sellingShops = new ArrayList<>();
    this.buyingShops = new ArrayList<>();
  }
  
  /**
   * Add a shop to this group
   * @param shop The shop to add
   */
  public void addShop(@NotNull final Shop shop) {
    shops.add(shop);
    if (shop.isSelling()) {
      sellingShops.add(shop);
    } else if (shop.isBuying()) {
      buyingShops.add(shop);
    }
  }
  
  /**
   * Calculate price statistics for all shops in this group
   * Should be called after all shops have been added
   */
  public void calculateStatistics() {
    // Calculate selling shop statistics
    if (!sellingShops.isEmpty()) {
      final List<Double> sellingPrices = sellingShops.stream()
              .map(Shop::getPrice)
              .toList();
      
      sellingMinPrice = CommonUtil.min(sellingPrices);
      sellingMaxPrice = CommonUtil.max(sellingPrices);
      sellingAvgPrice = CommonUtil.avg(sellingPrices);
      sellingMedianPrice = CommonUtil.med(sellingPrices);
      sellingTotalStock = sellingShops.stream()
              .mapToInt(shop -> Math.max(0, MarketUtils.getStockFromCache(shop)))
              .sum();
    }
    
    // Calculate buying shop statistics
    if (!buyingShops.isEmpty()) {
      final List<Double> buyingPrices = buyingShops.stream()
              .map(Shop::getPrice)
              .toList();
      
      buyingMinPrice = CommonUtil.min(buyingPrices);
      buyingMaxPrice = CommonUtil.max(buyingPrices);
      buyingAvgPrice = CommonUtil.avg(buyingPrices);
      buyingMedianPrice = CommonUtil.med(buyingPrices);
      buyingTotalSpace = buyingShops.stream()
              .mapToInt(shop -> Math.max(0, MarketUtils.getSpaceFromCache(shop)))
              .sum();
    }
  }
  
  /**
   * Get a price indicator string based on where a price falls relative to the average
   * @param price The price to check
   * @param isSelling Whether this is for a selling shop
   * @return Color indicator: "low" (below avg), "avg" (around avg), "high" (above avg)
   */
  public String getPriceIndicator(final double price, final boolean isSelling) {
    final double avg = isSelling ? sellingAvgPrice : buyingAvgPrice;
    if (avg == 0) return "avg";
    
    final double ratio = price / avg;
    if (ratio < 0.9) {
      return "low";
    } else if (ratio > 1.1) {
      return "high";
    }
    return "avg";
  }
  
  // Getters
  
  @NotNull
  public ItemStack getRepresentativeItem() {
    return representativeItem.clone();
  }
  
  @NotNull
  public List<Shop> getShops() {
    return new ArrayList<>(shops);
  }
  
  @NotNull
  public List<Shop> getSellingShops() {
    return new ArrayList<>(sellingShops);
  }
  
  @NotNull
  public List<Shop> getBuyingShops() {
    return new ArrayList<>(buyingShops);
  }
  
  public int getTotalShopCount() {
    return shops.size();
  }
  
  public int getSellingShopCount() {
    return sellingShops.size();
  }
  
  public int getBuyingShopCount() {
    return buyingShops.size();
  }
  
  // Selling statistics
  
  public double getSellingMinPrice() {
    return sellingMinPrice;
  }
  
  public double getSellingMaxPrice() {
    return sellingMaxPrice;
  }
  
  public double getSellingAvgPrice() {
    return sellingAvgPrice;
  }
  
  public double getSellingMedianPrice() {
    return sellingMedianPrice;
  }
  
  public int getSellingTotalStock() {
    return sellingTotalStock;
  }
  
  // Buying statistics
  
  public double getBuyingMinPrice() {
    return buyingMinPrice;
  }
  
  public double getBuyingMaxPrice() {
    return buyingMaxPrice;
  }
  
  public double getBuyingAvgPrice() {
    return buyingAvgPrice;
  }
  
  public double getBuyingMedianPrice() {
    return buyingMedianPrice;
  }
  
  public int getBuyingTotalSpace() {
    return buyingTotalSpace;
  }
  
  /**
   * Check if this group has any selling shops
   * @return true if there are selling shops
   */
  public boolean hasSellingShops() {
    return !sellingShops.isEmpty();
  }
  
  /**
   * Check if this group has any buying shops
   * @return true if there are buying shops
   */
  public boolean hasBuyingShops() {
    return !buyingShops.isEmpty();
  }
  
  /**
   * Get the item display name for this group
   * @return The item's display name or material name
   */
  @NotNull
  public String getItemDisplayName() {
    if (representativeItem.hasItemMeta() && representativeItem.getItemMeta().hasDisplayName()) {
      return representativeItem.getItemMeta().getDisplayName();
    }
    return CommonUtil.prettifyText(representativeItem.getType().name());
  }
}
