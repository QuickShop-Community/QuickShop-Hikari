package com.ghostchu.quickshop.config;

/*
 * QuickShop-Hikari
 * Copyright (C) 2025 Daniel "creatorfromhell" Vidmar
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
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

import java.util.Collections;

/**
 * MainConfig
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class MainConfig extends QSConfig {

  private static MainConfig instance;

  public MainConfig(final QuickShop plugin) {

    super("config.yml", "config.yml", Collections.emptyList(),
          LoaderSettings.builder().setAutoUpdate(true).build(),
          UpdaterSettings.builder().setAutoSave(true)
                  .setVersioning(new BasicVersioning("config-version"))
                  .addIgnoredRoute("1032", "limits.ranks", '.')
                  .addIgnoredRoute("1033", "limits.ranks", '.')
                  .addIgnoredRoute("1034", "limits.ranks", '.')
                  .addIgnoredRoute("1035", "limits.ranks", '.')
                  .addIgnoredRoute("1036", "limits.ranks", '.')
                  .addIgnoredRoute("1037", "limits.ranks", '.')
                  .addIgnoredRoute("1038", "limits.ranks", '.')
                  .addIgnoredRoute("1039", "limits.ranks", '.')
                  .addIgnoredRoute("1040", "limits.ranks", '.')
                  .addIgnoredRoute("1041", "limits.ranks", '.')
                  .addIgnoredRoute("1042", "limits.ranks", '.')
                  .addIgnoredRoute("1043", "limits.ranks", '.')
                  .addIgnoredRoute("1044", "limits.ranks", '.')
                  .addIgnoredRoute("1045", "limits.ranks", '.')
                  .addIgnoredRoute("1046", "limits.ranks", '.')
                  .addIgnoredRoute("1047", "limits.ranks", '.')
                  .addIgnoredRoute("1048", "limits.ranks", '.')
                  .addIgnoredRoute("1035", "shop-tax.progressive.brackets", '.')
                  .addIgnoredRoute("1036", "shop-tax.progressive.brackets", '.')
                  .addIgnoredRoute("1037", "shop-tax.progressive.brackets", '.')
                  .addIgnoredRoute("1038", "shop-tax.progressive.brackets", '.')
                  .addIgnoredRoute("1039", "shop-tax.progressive.brackets", '.')
                  .addIgnoredRoute("1040", "shop-tax.progressive.brackets", '.')
                  .addIgnoredRoute("1041", "shop-tax.progressive.brackets", '.')
                  .addIgnoredRoute("1042", "shop-tax.progressive.brackets", '.')
                  .addIgnoredRoute("1043", "shop-tax.progressive.brackets", '.')
                  .addIgnoredRoute("1044", "shop-tax.progressive.brackets", '.')
                  .addIgnoredRoute("1045", "shop-tax.progressive.brackets", '.')
                  .addIgnoredRoute("1046", "shop-tax.progressive.brackets", '.')
                  .addIgnoredRoute("1047", "shop-tax.progressive.brackets", '.')
                  .addIgnoredRoute("1048", "shop-tax.progressive.brackets", '.')
                  .build());

    instance = this;
    plugin.getReloadManager().register(this);
  }

  public static YamlDocument yaml() {

    return instance.getYaml();
  }
}