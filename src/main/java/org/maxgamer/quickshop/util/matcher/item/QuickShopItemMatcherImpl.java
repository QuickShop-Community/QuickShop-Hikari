/*
 * This file is a part of project QuickShop, the name is QuickShopItemMatcherImpl.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.util.matcher.item;

import de.tr7zw.nbtapi.NBTItem;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.shop.ItemMatcher;
import org.maxgamer.quickshop.util.ReflectFactory;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;
import org.maxgamer.quickshop.util.reload.Reloadable;

import java.util.*;
import java.util.logging.Level;

@AllArgsConstructor
public class QuickShopItemMatcherImpl implements ItemMatcher, Reloadable {
    private final QuickShop plugin;

    private ItemMetaMatcher itemMetaMatcher;

    private int workType;


    public QuickShopItemMatcherImpl(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        plugin.getReloadManager().register(this);
        init();
    }

    private void init() {
        itemMetaMatcher = new ItemMetaMatcher(plugin.getConfig().getConfigurationSection("matcher.item"), this);
        workType = plugin.getConfig().getInt("matcher.work-type");
    }

    /**
     * Gets the ItemMatcher provider name
     *
     * @return Provider name
     */
    @Override
    public @NotNull String getName() {
        return plugin.getName();
    }

    /**
     * Gets the ItemMatcher provider plugin instance
     *
     * @return Provider Plugin instance
     */
    @Override
    public @NotNull Plugin getPlugin() {
        return plugin;
    }

    /**
     * Tests ItemStacks is matches
     * BEWARE: Different order of itemstacks you might will got different results
     *
     * @param requireStack The original ItemStack
     * @param givenStack   The ItemStack will test matches with original itemstack.
     * @return The result of tests
     */
    public boolean matches(@Nullable ItemStack[] requireStack, @Nullable ItemStack[] givenStack) {
        if (requireStack == null && givenStack == null) {
            return true;
        }

        if (requireStack == null || givenStack == null) {
            return false;
        }

        if (requireStack.length != givenStack.length) {
            return false;
        }
        //For performance, we just check really equals in each index,check isn't contain or match will cost n^n time in most
        for (int i = 0; i < requireStack.length; i++) {
            if ((requireStack[i] != null) && (givenStack[i] != null) &&
                    (requireStack[i].getAmount() != givenStack[i].getAmount())) {
                return false;
            }

            if (!matches(requireStack[i], givenStack[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares two items to each other. Returns true if they match. Rewrite it to use more faster
     * hashCode.
     *
     * @param requireStack The first item stack
     * @param givenStack   The second item stack
     * @return true if the itemstacks match. (Material, durability, enchants, name)
     */
    @Override
    public boolean matches(@Nullable ItemStack requireStack, @Nullable ItemStack givenStack) {
        if (requireStack == null && givenStack == null) {
            return true;
        }

        if (requireStack == null || givenStack == null) {
            Util.debugLog(
                    "Match failed: A stack is null: "
                            + "requireStack["
                            + requireStack
                            + "] givenStack["
                            + givenStack
                            + "]");
            return false; // One of them is null (Can't be both, see above)
        }

        requireStack = requireStack.clone();
        requireStack.setAmount(1);
        givenStack = givenStack.clone();
        givenStack.setAmount(1);
        if (plugin.getNbtapi() != null) {
            try {
                NBTItem nbtItemOriginal = new NBTItem(requireStack);
                NBTItem nbtItemTester = new NBTItem(givenStack);
                String tagOriginal = nbtItemOriginal.getString("shopItemId");
                String tagTester = nbtItemTester.getString("shopItemId");
                if (StringUtils.isNotEmpty(tagOriginal) && StringUtils.isNotEmpty(tagTester) && tagOriginal.equals(tagTester)) {
                    return true;
                }
            } catch (Exception e) {
                plugin.disableNBTAPI();
                plugin.getLogger().log(Level.WARNING, "NBTAPI support is broken, dsiable and fallback... (You can safely ignore this)", e);
                Util.debugLog("NBTAPI is broken, error: " + e.getMessage() + "\n stacktrace:  \n" + Arrays.toString(e.getStackTrace()));
            }
        }
        if (workType == 1) {
            return requireStack.isSimilar(givenStack);
        }
        if (workType == 2) {
            return requireStack.equals(givenStack);
        }

        if (!typeMatches(requireStack, givenStack)) {
            return false;
        }

        if (requireStack.isSimilar(givenStack)) {
            return true;
        }
        /* If they are the same type, they should also have item meta
        if (requireStack.hasItemMeta() != givenStack.hasItemMeta()) {
            return false;
        }*/
        if (requireStack.hasItemMeta()) {
            return itemMetaMatcher.matches(requireStack, givenStack);
        }

        return true;
    }

    private boolean typeMatches(ItemStack requireStack, ItemStack givenStack) {
        return requireStack.getType().equals(givenStack.getType());
    }

    /**
     * Callback for reloading
     *
     * @return Reloading success
     */
    @Override
    public ReloadResult reloadModule() {
        init();
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }

    private static class ItemMetaMatcher {

        private final List<Matcher> matcherList = new ArrayList<>();

        public ItemMetaMatcher(@NotNull ConfigurationSection itemMatcherConfig, @NotNull QuickShopItemMatcherImpl itemMatcher) {

            addIfEnable(itemMatcherConfig, "damage", (meta1, meta2) -> {
                if (meta1 instanceof Damageable != meta2 instanceof Damageable) {
                    return false;
                }
                if (meta1 instanceof Damageable) {
                    Damageable damage1 = (Damageable) meta1;
                    Damageable damage2 = (Damageable) meta2;
                    //Given item damaged but matching item doesn't, allow it
                    if (damage1.hasDamage() && !damage2.hasDamage()) {
                        return true;
                    }
                    //Given item NOT damaged but matching item damaged, denied it
                    if (!damage1.hasDamage() && damage2.hasDamage()) {
                        return false;
                    }
                    //last condition: Check them damages, if givenDamage >= requireDamage, allow it.
                    return damage2.getDamage() <= damage1.getDamage();
                }
                return true;
            });
            addIfEnable(itemMatcherConfig, "repaircost", (meta1, meta2) -> {
                if (meta1 instanceof Repairable != meta2 instanceof Repairable) {
                    return false;
                }
                if (meta1 instanceof Repairable) {
                    Repairable repairable1 = (Repairable) meta1;
                    Repairable repairable2 = (Repairable) meta2;
                    boolean hasRepairCost1 = repairable1.hasRepairCost();
                    boolean hasRepairCost2 = repairable2.hasRepairCost();
                    //Given item have repair cost but matching item doesn't, allow it
                    if (hasRepairCost1 && !hasRepairCost2) {
                        return true;
                    }
                    //Given item DOESN'T have repair cost but matching item have, denied it
                    if (!hasRepairCost1 && hasRepairCost2) {
                        return false;
                    }
                    //last condition: both having repair cost, so allow items lesser than or equals given item Repair Cost
                    return repairable2.getRepairCost() <= repairable1.getRepairCost();
                }
                return true;
            });
            addIfEnable(itemMatcherConfig, "displayname", ((meta1, meta2) -> {

                if (meta1.hasDisplayName() != meta2.hasDisplayName()) {
                    return false;
                }
                if (meta1.hasDisplayName()) {
                    return meta1.getDisplayName().equals(meta2.getDisplayName());
                }
                return true;
            }));
            // We didn't touch the loresMatches because many plugin use this check item.
            addIfEnable(itemMatcherConfig, "lores", ((meta1, meta2) -> {
                if (meta1.hasLore() != meta2.hasLore()) {
                    return false;
                }
                if (meta1.hasLore()) {
                    List<String> lores1 = meta1.getLore();
                    List<String> lores2 = meta2.getLore();
                    return Arrays.deepEquals(
                            Objects.requireNonNull(lores1).toArray(), Objects.requireNonNull(lores2).toArray());
                }

                return true;

            }));
            addIfEnable(itemMatcherConfig, "enchs", ((meta1, meta2) -> {

                if (meta1.hasEnchants() != meta2.hasEnchants()) {
                    return false;
                }
                if (meta1.hasEnchants()) {
                    Map<Enchantment, Integer> enchMap1 = meta1.getEnchants();
                    Map<Enchantment, Integer> enchMap2 = meta2.getEnchants();
                    return enchMap1.equals(enchMap2);
                }


                if (meta1 instanceof EnchantmentStorageMeta != meta2 instanceof EnchantmentStorageMeta) {
                    return false;
                }
                if (meta1 instanceof EnchantmentStorageMeta) {
                    Map<Enchantment, Integer> stor1 = ((EnchantmentStorageMeta) meta1).getStoredEnchants();
                    Map<Enchantment, Integer> stor2 = ((EnchantmentStorageMeta) meta2).getStoredEnchants();
                    return stor1.equals(stor2);
                }
                return true;
            }));
            addIfEnable(itemMatcherConfig, "potions", ((meta1, meta2) -> {

                if (meta1 instanceof PotionMeta != meta2 instanceof PotionMeta) {
                    return false;
                }
                if (meta1 instanceof PotionMeta) {
                    PotionMeta potion1 = (PotionMeta) meta1;
                    PotionMeta potion2 = (PotionMeta) meta2;

                    if (potion1.hasColor() != potion2.hasColor()) {
                        return false;
                    }
                    if (potion1.hasColor() && !Objects.equals(potion1.getColor(), potion2.getColor())) {
                        return false;
                    }

                    if (potion1.hasCustomEffects() != potion2.hasCustomEffects()) {
                        return false;
                    }
                    if (potion1.hasCustomEffects() && !Arrays.deepEquals(potion1.getCustomEffects().toArray(), potion2.getCustomEffects().toArray())) {
                        return false;
                    }

                    PotionData data1 = potion1.getBasePotionData();
                    PotionData data2 = potion2.getBasePotionData();

                    if (!data1.equals(data2)) {
                        return false;
                    }
                    if (!data2.getType().equals(data1.getType())) {
                        return false;
                    }
                    if (data1.isExtended() != data2.isExtended()) {
                        return false;
                    }
                    return data1.isUpgraded() == data2.isUpgraded();
                }
                return true;
            }));
            addIfEnable(itemMatcherConfig, "attributes", (meta1, meta2) -> {
                if (meta1.hasAttributeModifiers() != meta2.hasAttributeModifiers()) {
                    return false;
                }
                if (meta1.hasAttributeModifiers() && meta2.hasAttributeModifiers()) {
                    Set<Attribute> set1 = Objects.requireNonNull(meta1.getAttributeModifiers()).keySet();
                    Set<Attribute> set2 = Objects.requireNonNull(meta2.getAttributeModifiers()).keySet();
                    for (Attribute att : set1) {
                        if (!set2.contains(att)) {
                            return false;
                        } else if (!meta1
                                .getAttributeModifiers()
                                .get(att)
                                .equals(meta2.getAttributeModifiers().get(att))) {
                            return false;
                        }
                    }
                }
                return true;
            });
            addIfEnable(itemMatcherConfig, "itemflags", ((meta1, meta2) -> Arrays.deepEquals(meta1.getItemFlags().toArray(), meta2.getItemFlags().toArray())));
            addIfEnable(itemMatcherConfig, "books", ((meta1, meta2) -> {
                if (meta1 instanceof BookMeta != meta2 instanceof BookMeta) {
                    return false;
                }
                if (meta1 instanceof BookMeta) {
                    BookMeta book1 = (BookMeta) meta1;
                    BookMeta book2 = (BookMeta) meta2;
                    if (book1.hasTitle() != book2.hasTitle()) {
                        return false;
                    }
                    if (book1.hasTitle() && !Objects.equals(book1.getTitle(), book2.getTitle())) {
                        return false;
                    }

                    if (book1.hasPages() != book2.hasPages()) {
                        return false;
                    }
                    if (book1.hasPages() && !book1.getPages().equals(book2.getPages())) {
                        return false;
                    }

                    if (book1.hasAuthor() != book2.hasAuthor()) {
                        return false;
                    }
                    if (book1.hasAuthor() && !Objects.equals(book1.getAuthor(), book2.getAuthor())) {
                        return false;
                    }

                    if (book1.hasGeneration() != book2.hasGeneration()) {
                        return false;
                    }
                    return !book1.hasGeneration() || Objects.equals(book1.getGeneration(), book2.getGeneration());
                }
                return true;
            }));
            addIfEnable(itemMatcherConfig, "banner", ((meta1, meta2) -> {
                if (meta1 instanceof BannerMeta != meta2 instanceof BannerMeta) {
                    return false;
                }
                if (meta1 instanceof BannerMeta) {
                    BannerMeta bannerMeta1 = (BannerMeta) meta1;
                    BannerMeta bannerMeta2 = (BannerMeta) meta2;
                    if (bannerMeta1.numberOfPatterns() != bannerMeta2.numberOfPatterns()) {
                        return false;
                    }
                    return bannerMeta1.getPatterns().containsAll(bannerMeta2.getPatterns());
                }
                return true;
            }));
            addIfEnable(itemMatcherConfig, "skull", (meta1, meta2) -> {
                if (meta1 instanceof SkullMeta != meta2 instanceof SkullMeta) {
                    return false;
                }
                if (meta1 instanceof SkullMeta) {
                    //getOwningPlayer will let server query playerProfile in server thread
                    //Causing huge lag, so using String instead
                    String player1 = ((SkullMeta) meta1).getOwner(); //FIXME: Update this when drop 1.15 supports
                    String player2 = ((SkullMeta) meta2).getOwner(); //FIXME: Update this when drop 1.15 supports
                    return Objects.equals(player1, player2);
                }
                return true;
            });
            addIfEnable(itemMatcherConfig, "map", (meta1, meta2) -> {
                if (meta1 instanceof MapMeta != meta2 instanceof MapMeta) {
                    return false;
                }
                if (meta1 instanceof MapMeta) {
                    MapMeta mapMeta1 = ((MapMeta) meta1);
                    MapMeta mapMeta2 = ((MapMeta) meta2);
                    if (mapMeta1.hasMapView() != mapMeta2.hasMapView()) {
                        return false;
                    }
                    if (mapMeta1.hasMapView() && mapMeta2.hasMapView() && !Objects.equals(mapMeta1.getMapView(), mapMeta2.getMapView())) {
                        return false;
                    }

                    if (mapMeta1.hasColor() != mapMeta2.hasColor()) {
                        return false;
                    }
                    if (mapMeta1.hasColor() && mapMeta2.hasColor() && !Objects.equals(mapMeta1.getColor(), mapMeta2.getColor())) {
                        return false;
                    }

                    if (mapMeta1.hasLocationName() != mapMeta2.hasLocationName()) {
                        return false;
                    }
                    return !mapMeta1.hasLocationName() || !mapMeta2.hasLocationName() || Objects.equals(mapMeta1.getLocationName(), mapMeta2.getLocationName());
                }
                return true;
            });
            addIfEnable(itemMatcherConfig, "firework", (meta1, meta2) -> {
                if ((meta1 instanceof FireworkMeta) != (meta2 instanceof FireworkMeta)) {
                    return false;
                }
                if (meta1 instanceof FireworkMeta) {
                    FireworkMeta fireworkMeta1 = ((FireworkMeta) meta1);
                    FireworkMeta fireworkMeta2 = ((FireworkMeta) meta2);
                    if (fireworkMeta1.hasEffects() != fireworkMeta2.hasEffects()) {
                        return false;
                    }
                    if (!fireworkMeta1.getEffects().equals(fireworkMeta2.getEffects())) {
                        return false;
                    }
                    return fireworkMeta1.getPower() == fireworkMeta2.getPower();
                }
                return true;
            });
            addIfEnable(itemMatcherConfig, "leatherArmor", ((meta1, meta2) -> {
                if ((meta1 instanceof LeatherArmorMeta) != (meta2 instanceof LeatherArmorMeta)) {
                    return false;
                }
                if (meta1 instanceof LeatherArmorMeta) {
                    return ((LeatherArmorMeta) meta1).getColor().equals(((LeatherArmorMeta) meta2).getColor());
                }
                return true;
            }));
            addIfEnable(itemMatcherConfig, "fishBucket", (meta1, meta2) -> {
                if ((meta1 instanceof TropicalFishBucketMeta) != (meta2 instanceof TropicalFishBucketMeta)) {
                    return false;
                }
                if (meta1 instanceof TropicalFishBucketMeta) {
                    TropicalFishBucketMeta fishBucketMeta1 = ((TropicalFishBucketMeta) meta1);
                    TropicalFishBucketMeta fishBucketMeta2 = ((TropicalFishBucketMeta) meta2);
                    if (fishBucketMeta1.hasVariant() != fishBucketMeta2.hasVariant()) {
                        return false;
                    }
                    return !fishBucketMeta1.hasVariant()
                            || (fishBucketMeta1.getPattern() == fishBucketMeta2.getPattern()
                            && fishBucketMeta1.getBodyColor().equals(fishBucketMeta2.getBodyColor())
                            && fishBucketMeta1.getPatternColor().equals(fishBucketMeta2.getPatternColor()));
                }
                return true;
            });
            addIfEnable(itemMatcherConfig, "shulkerBox", ((meta1, meta2) -> {
                //https://www.spigotmc.org/threads/getting-the-inventory-of-a-shulker-box-itemstack.212369
                if ((meta1 instanceof BlockStateMeta) != (meta2 instanceof BlockStateMeta)) {
                    return false;
                }
                if (meta1 instanceof BlockStateMeta) {
                    if ((((BlockStateMeta) meta1).getBlockState() instanceof ShulkerBox) != ((BlockStateMeta) meta2).getBlockState() instanceof ShulkerBox) {
                        return false;
                    }
                    if (((BlockStateMeta) meta1).getBlockState() instanceof ShulkerBox) {
                        return itemMatcher.matches(((ShulkerBox) ((BlockStateMeta) meta1).getBlockState()).getInventory().getContents(), ((ShulkerBox) ((BlockStateMeta) meta2).getBlockState()).getInventory().getContents());
                    }
                }
                return true;
            }));
            if (!"v1_13_R1".equals(ReflectFactory.getNMSVersion()) && !"v1_13_R2".equals(ReflectFactory.getNMSVersion())) {
                addIfEnable(itemMatcherConfig, "custommodeldata", ((meta1, meta2) -> {
                    if (meta1.hasCustomModelData() != meta2.hasCustomModelData()) {
                        return false;
                    }
                    if (meta1.hasCustomModelData()) {
                        return meta1.getCustomModelData() == meta2.getCustomModelData();
                    }
                    return true;
                }));
                if (!"v1_14_R1".equals(ReflectFactory.getNMSVersion())) {
                    addIfEnable(itemMatcherConfig, "suspiciousStew", ((meta1, meta2) -> {
                        if ((meta1 instanceof SuspiciousStewMeta) != (meta2 instanceof SuspiciousStewMeta)) {
                            return false;
                        }
                        if (meta1 instanceof SuspiciousStewMeta) {
                            SuspiciousStewMeta stewMeta1 = ((SuspiciousStewMeta) meta1);
                            SuspiciousStewMeta stewMeta2 = ((SuspiciousStewMeta) meta2);
                            if (stewMeta1.hasCustomEffects() != stewMeta2.hasCustomEffects()) {
                                return false;
                            }
                            if (stewMeta1.hasCustomEffects()) {
                                return stewMeta1.getCustomEffects().equals(stewMeta2.getCustomEffects());
                            }
                        }
                        return true;
                    }));
                }
            }
        }

        private void addIfEnable(ConfigurationSection itemMatcherConfig, String path, Matcher matcher) {
            if (itemMatcherConfig.getBoolean(path)) {
                matcherList.add(matcher);
            }
        }

        boolean matches(ItemStack requireStack, ItemStack givenStack) {
            /* If they are the same type, they should also have item meta
            if (requireStack.hasItemMeta() != givenStack.hasItemMeta()) {
                return false;
            }*/
            if (!requireStack.hasItemMeta()) {
                return true; // Passed check. no meta need to check.
            }
            ItemMeta meta1 = requireStack.getItemMeta();
            ItemMeta meta2 = givenStack.getItemMeta();
            //If givenStack don't have meta, try to generate one
            if (meta2 == null) {
                meta2 = Bukkit.getItemFactory().getItemMeta(givenStack.getType());
                if (meta2 == null) {
                    return true; // Passed check. givenStack still have no meta need to check.
                }
            }
            for (Matcher matcher : matcherList) {
                if (!matcher.match(meta1, meta2)) {
                    return false;
                }
            }
            return true;
        }

        private boolean rootMatches(ItemMeta meta1, ItemMeta meta2) {
            return (meta1.hashCode() == meta2.hashCode());
        }


        interface Matcher {
            /**
             * Matches between ItemMeta
             *
             * @param meta1 ItemMeta 1
             * @param meta2 ItemMeta 2
             * @return is same
             */
            boolean match(ItemMeta meta1, ItemMeta meta2);

        }


    }
}