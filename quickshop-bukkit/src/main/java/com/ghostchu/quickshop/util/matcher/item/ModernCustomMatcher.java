package com.ghostchu.quickshop.util.matcher.item;

/*
 * QuickShop-Hikari
 * Copyright (C) 2026 Daniel "creatorfromhell" Vidmar
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
import com.ghostchu.quickshop.api.event.general.ShopItemMatchEvent;
import com.ghostchu.quickshop.api.shop.ItemMatcher;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import com.ghostchu.simplereloadlib.Reloadable;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * ModernCustomMatcher
 *
 * Config-driven ItemStack matcher using Paper's Data Component API.
 *
 * Whitelist-based:
 * - Only enabled keys under matcher.components.* are compared.
 * - Keys can be either:
 *   1) A direct DataComponentTypeKeys field (e.g. DAMAGE, LORE, ENCHANTMENTS)
 *   2) A group key that expands into multiple component keys (e.g. BOOKS, SHULKER_BOX)
 *
 * ViaVersion compatibility:
 * - CUSTOM_DATA is NEVER compared (ViaVersion writes VV|Protocol tags into minecraft:custom_data).
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class ModernCustomMatcher implements ItemMatcher, Reloadable {

  private final QuickShop plugin;

  /**
   * If true, stack amount is ignored.
   */
  private boolean ignoreCount;

  /**
   * If true, ItemStack#getType must match.
   */
  private boolean checkMaterial;

  /**
   * Cached list of valued component types that must match (whitelist).
   * Built on init/reload from config.
   */
  private volatile List<DataComponentType.Valued<?>> compareTypes = List.of();

  public ModernCustomMatcher(@NotNull final QuickShop plugin) {

    this.plugin = plugin;
    plugin.getReloadManager().register(this);
    init();
  }

  private void init() {

    this.ignoreCount = plugin.getConfig().getBoolean("matcher.ignore-count", true);
    this.checkMaterial = plugin.getConfig().getBoolean("matcher.check-material", true);

    this.compareTypes = Collections.unmodifiableList(loadCompareTypes());
  }

  @Override
  public ReloadResult reloadModule() {

    init();
    return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
  }

  @Override
  public @NotNull String getName() {

    return "modern-matcher";
  }

  @Override
  public @NotNull Plugin getPlugin() {

    return plugin.getJavaPlugin();
  }

  @Override
  public boolean matches(@Nullable final ItemStack original, @Nullable final ItemStack tester) {

    //System.out.println("ModernCustomMatcher.matches");

    if(original == null && tester == null) {
      return true;
    }

    if(original == null || tester == null) {
      return false;
    }

    if(original.isSimilar(tester)) {
      return true;
    }

    final ShopItemMatchEvent shopItemMatchEvent = new ShopItemMatchEvent(original.clone(), tester.clone());
    shopItemMatchEvent.callEvent();

    if(shopItemMatchEvent.matches()) {
      return true;
    }

    //System.out.println("ModernCustomMatcher.matches: checking material");

    // Fast-fail: material mismatch (recommended)
    if(checkMaterial && original.getType() != tester.getType()) {
      return false;
    }

    //System.out.println("ModernCustomMatcher.matches: checking amount");
    // Optional: amount mismatch
    if(!ignoreCount && original.getAmount() != tester.getAmount()) {
      return false;
    }

    // Whitelist compare: only configured component types
    for(final DataComponentType.Valued<?> type : compareTypes) {

      //System.out.println("ModernCustomMatcher.matches: checking type: " + type.key().asString());
      final Object a = original.getData(type);
      final Object b = tester.getData(type);

      if(!Objects.equals(a, b)) {
        //System.out.println("ModernCustomMatcher.matches: type mismatch: " + type.key().asString());
        return false;
      }
    }

    return true;
  }

  /**
   * Loads and resolves the whitelist valued component types from config.
   *
   * Config:
   * matcher.components:
   *   DAMAGE: true
   *   LORE: true
   *   BOOKS: true
   *   SHULKER_BOX: false
   */
  private List<DataComponentType.Valued<?>> loadCompareTypes() {

    final var registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.DATA_COMPONENT_TYPE);

    final LinkedHashSet<DataComponentType.Valued<?>> out = new LinkedHashSet<>();

    final Section section = plugin.getConfig().getSection("matcher.components");
    if(section == null) {
      return List.of();
    }

    for(final String rawKey : section.getRoutesAsStrings(false)) {

      //System.out.println("ModernCustomMatcher.loadCompareTypes: checking key: " + rawKey);

      if(!section.getBoolean(rawKey, false)) {
        //System.out.println("ModernCustomMatcher.loadCompareTypes: key disabled: " + rawKey);
        continue;
      }

      final String key = rawKey.toLowerCase(Locale.ROOT);

      //Try to convert the naming directly to the DataComponentType key
      final DataComponentType.Valued<?> valued = resolveTypeKeyFromDataComponentTypeKeys(key);
      if(valued != null) {

        //System.out.println("ModernCustomMatcher.loadCompareTypes: adding type: " + key);
        out.add(valued);
        continue;
      }
      //System.out.println("ModernCustomMatcher.loadCompareTypes: key not found: " + key);

      //might be a grouped type, which includes multiples.
      expandGroup(out, registry, key);
    }

    return new ArrayList<>(out);
  }

  private void expandGroup(final Set<DataComponentType.Valued<?>> out,
                           final Registry<DataComponentType> registry,
                           final String groupName) {

    switch(groupName) {

      // Book content (editable + written)
      case "books" -> add(out, registry,
                          "WRITABLE_BOOK_CONTENT",
                          "WRITTEN_BOOK_CONTENT"
                         );

      // Banner patterns
      case "banner" -> add(out, registry,
                           "BANNER_PATTERNS"
                          );

      // Skull owner profile/texture
      case "skull" -> add(out, registry,
                          "PROFILE"
                         );

      // Firework rocket + star
      case "firework" -> add(out, registry,
                             "FIREWORKS",
                             "FIREWORK_EXPLOSION"
                            );

      // Map metadata
      case "map" -> add(out, registry,
                        "MAP_ID",
                        "MAP_DECORATIONS",
                        "MAP_POST_PROCESSING"
                       );

      // Leather armor dyed color
      case "leather_armor" -> add(out, registry,
                                  "DYED_COLOR"
                                 );

      // Fish bucket entity data, and various fish variants
      case "fish_bucket" -> add(out, registry,
                                "BUCKET_ENTITY_DATA",
                                "AXOLOTL/VARIANT",
                                "SALMON/SIZE",
                                "TROPICAL_FISH/PATTERN",
                                "TROPICAL_FISH/BASE_COLOR",
                                "TROPICAL_FISH/BASE_COLOR"
                               );

      // Suspicious stew effects
      case "suspicious_stew" -> add(out, registry,
                                    "SUSPICIOUS_STEW_EFFECTS"
                                   );

      // Shulker container contents (+ loot table marker if present)
      case "shulker_box" -> add(out, registry,
                                "CONTAINER",
                                "CONTAINER_LOOT"
                               );

      // Bundle contents
      case "bundle" -> add(out, registry,
                           "BUNDLE_CONTENTS"
                          );

      default -> {
      }
    }
  }

  private void add(final Set<DataComponentType.Valued<?>> out,
                   final Registry<DataComponentType> registry,
                   final String... keyNames) {

    for(final String rawKey : keyNames) {

      final String key = rawKey.toLowerCase(Locale.ROOT);

      final DataComponentType.Valued<?> valued = resolveTypeKeyFromDataComponentTypeKeys(key);
      if(valued == null) {

        plugin.logger().warn("[ModernCustomMatcher] Unknown DataComponentTypeKeys field: " + key);
        continue;
      }

      if(valued != null) {
        //System.out.println("ModernCustomMatcher.loadCompareTypes: adding type: " + key);
        out.add(valued);
      }
    }
  }

  /**
   * Reflectively resolves a DataComponentTypeKeys.<FIELD> to a DataComponentType.Valued.
   *
   * This avoids hard depending on every constant at compile-time and lets config drive selection.
   */
  @SuppressWarnings("unchecked")
  private static @Nullable DataComponentType.Valued resolveTypeKeyFromDataComponentTypeKeys(final String name) {

    try {
      //System.out.println("ModernCustomMatcher.resolveTypeKeyFromDataComponentTypeKeys: " + name);
      final DataComponentType.Valued dataType = (DataComponentType.Valued)Registry.DATA_COMPONENT_TYPE.get(NamespacedKey.minecraft(name.toLowerCase(Locale.ROOT)));

      return dataType;
    } catch(final ClassCastException ex) {
      //System.out.println("ModernCustomMatcher.resolveTypeKeyFromDataComponentTypeKeys: " + name + " is not a valued type!");
      return null;
    }
  }
}