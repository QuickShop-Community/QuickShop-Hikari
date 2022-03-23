/*
 *  This file is a part of project QuickShop, the name is LanguageFilesManager.java
 *  Copyright (C) Ghost_chu and contributors
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

package com.ghostchu.quickshop.localization.text;

import com.ghostchu.quickshop.util.Util;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// No to-do anymore! This used for not only messages.yml! Keep the extent ability!
public class LanguageFilesManager {
    //distributionPath->[localeCode->OTA files]
    private final Map<String, FileConfiguration> locale2ContentMapping = new ConcurrentHashMap<>();

    /**
     * Reset TextMapper
     */
    public void reset() {
        this.locale2ContentMapping.clear();
    }

    /**
     * Deploy new locale to TextMapper with cloud values and bundle values
     *
     * @param locale           The locale code
     * @param distribution     The values from Distribution platform
     */
    public void deploy(@NotNull String locale, @NotNull FileConfiguration distribution) {
        Util.debugLog("Registered and deployed locale: " + locale);
        this.locale2ContentMapping.put(locale, distribution);
    }

    /**
     * Remove all locales data under specific distribution path
     *
     * @param distributionPath The distribution path
     */
    public void remove(@NotNull String distributionPath) {
        this.locale2ContentMapping.remove(distributionPath);
    }

    /**
     * Remove specific locales data under specific distribution path
     *
     * @param distributionPath The distribution path
     * @param locale           The locale
     */
    public void remove(@NotNull String distributionPath, @NotNull String locale) {
        if (this.locale2ContentMapping.containsKey(distributionPath)) {
            this.locale2ContentMapping.remove(locale);
        }
    }

    /**
     * Getting specific locale data under specific distribution data
     *
     * @param locale           The specific locale
     * @return The locale data, null if never deployed
     */
    public @Nullable FileConfiguration getDistribution(@NotNull String locale) {
        return this.locale2ContentMapping.get(locale);
    }

    /**
     * Getting specific locale data under specific distribution data
     *
     * @return The locale data, null if never deployed
     */
    public @NotNull Map<String, FileConfiguration> getDistributions() {
        return locale2ContentMapping;
    }


}
