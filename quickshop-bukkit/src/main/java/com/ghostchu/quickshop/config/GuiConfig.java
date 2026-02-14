package com.ghostchu.quickshop.config;
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
import com.ghostchu.quickshop.api.config.QSConfig;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GuiConfig - Manages GUI configuration loading and access
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class GuiConfig extends QSConfig {

  private final QuickShop plugin;
  private static GuiConfig instance;
  private final Map<String, MenuConfig> menuConfigs = new HashMap<>();

  public GuiConfig(@NotNull final QuickShop plugin) {

    super("gui.yml", "gui.yml", Collections.emptyList(),
          LoaderSettings.builder().setAutoUpdate(true).build(),
          UpdaterSettings.builder().setAutoSave(true)
                  .setVersioning(new BasicVersioning("version")).build());

    instance = this;
    this.plugin = plugin;

    loadConfig();
    plugin.getReloadManager().register(this);
  }

  public void loadConfig() {

    Log.debug("GUI Config Loading.");
    menuConfigs.clear();

    this.load();

    // Load menu configurations
    loadMenuConfig("trade");
    loadMenuConfig("keeper");
    loadMenuConfig("staff");
    loadMenuConfig("history");
    loadMenuConfig("browse");

    Log.debug("GUI Config Loaded. Menus: " + menuConfigs.keySet());
  }

  private void loadMenuConfig(final String menuName) {

    final Section section = this.yaml.getSection(menuName);
    if(section != null) {
      menuConfigs.put(menuName, new MenuConfig(section));
    }
  }

  @Nullable
  public MenuConfig getMenuConfig(final String menuName) {

    return menuConfigs.get(menuName);
  }

  public static YamlDocument yaml() {

    return instance.getYaml();
  }

  /**
   * Gets a chat message from the gui.yml messages section.
   *
   * @param path         The path under "messages" section (e.g., "keeper.confirm-delete")
   * @param defaultValue The default value if not found
   *
   * @return The message string
   */
  @NotNull
  public String getMessage(@NotNull final String path, @NotNull final String defaultValue) {

    return yaml.getString("messages." + path, defaultValue);
  }

  /**
   * Gets a chat message from the gui.yml messages section.
   *
   * @param path The path under "messages" section (e.g., "keeper.confirm-delete")
   *
   * @return The message string, or the path if not found
   */
  @NotNull
  public String getMessage(@NotNull final String path) {

    return yaml.getString("messages." + path, path);
  }

  @Override
  public ReloadResult reloadModule() throws Exception {

    loadConfig();
    plugin.logger().info("GUI configuration reloaded successfully. Menus loaded: " + menuConfigs.keySet());
    plugin.logger().info("Note: Changes to menu row counts require a server restart to take effect.");
    return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
  }

  /**
   * Represents configuration for a single menu
   */
  public static class MenuConfig {

    private final Section section;
    private final Map<String, IconConfig> icons = new HashMap<>();

    public MenuConfig(final Section section) {

      this.section = section;
      loadIcons();
    }

    private void loadIcons() {

      for(final Object keyObj : section.getKeys()) {

        final String key = String.valueOf(keyObj);
        if(key.equals("title") || key.equals("rows")) continue;

        final Object value = section.get(key);
        if(value instanceof final Section iconSection) {
          icons.put(key, new IconConfig(iconSection));
        }
      }
    }

    public String getTitle() {

      return section.getString("title", "Menu");
    }

    public int getRows() {

      return section.getInt("rows", 6);
    }

    @Nullable
    public IconConfig getIcon(final String iconName) {

      return icons.get(iconName);
    }

    @NotNull
    public Section getSection() {

      return section;
    }
  }

  /**
   * Represents configuration for a single icon/button
   */
  public record IconConfig(Section section) {

    @NotNull
    public String getMaterial() {

      return section.getString("material", "STONE");
    }

    @Nullable
    public String getName() {

      return section.getString("name");
    }

    @NotNull
    public List<String> getLore() {

      final Object loreObj = section.get("lore");
      if(loreObj instanceof List<?>) {
        final List<String> result = new ArrayList<>();
        for(final Object item : (List<?>)loreObj) {
          if(item != null) {
            result.add(item.toString());
          }
        }
        return result;
      } else if(loreObj instanceof String) {
        final List<String> result = new ArrayList<>();
        result.add((String)loreObj);
        return result;
      }
      return new ArrayList<>();
    }

    public int getCustomModelData() {

      return section.getInt("custom-model-data", 0);
    }

    public int getSlot() {

      return section.getInt("slot", 0);
    }

    public int getAmount() {

      return section.getInt("amount", 1);
    }

    @NotNull
    public List<Integer> getSlots() {

      return section.getIntList("slots");
    }

    @NotNull
    public List<Integer> getQuantities() {

      return section.getIntList("quantities");
    }

    @NotNull
    public List<Integer> getRows() {

      return section.getIntList("rows");
    }

    public boolean hasCustomModelData() {

      return section.contains("custom-model-data") && getCustomModelData() != 0;
    }

    @Override
    @Nullable
    public Section section() {

      return section;
    }

    @Nullable
    public IconConfig getSubIcon(final String name) {

      final Section sub = section.getSection(name);
      return sub != null? new IconConfig(sub) : null;
    }
  }
}
