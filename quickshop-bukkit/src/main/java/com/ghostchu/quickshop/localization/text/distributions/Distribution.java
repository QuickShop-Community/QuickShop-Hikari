/*
 *  This file is a part of project QuickShop, the name is Distribution.java
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

package com.ghostchu.quickshop.localization.text.distributions;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Distribution {
    /**
     * Gets all languages available on Distribution platform
     *
     * @return All available languages on Distribution platform
     */
    @NotNull List<String> getAvailableLanguages();

    /**
     * Gets all translation files available on Distribution platform
     *
     * @return All translation files on Distribution platform
     */
    @NotNull List<String> getAvailableFiles();

    /**
     * Gets the file from the Distribution platform
     *
     * @param fileDistributionPath The path on the platform
     * @param distributionLocale   The locale on the platform
     * @return The file content
     * @throws Exception The exception throws if any errors occurred while getting file
     */
    @NotNull String getFile(String fileDistributionPath, String distributionLocale) throws Exception;

    /**
     * Gets the file from the Distribution platform
     *
     * @param fileDistributionPath The path on the platform
     * @param distributionLocale   The locale on the platform
     * @param forceFlush           Forces update the file from the platform
     * @return The file content
     * @throws Exception The exception throws if any errors occurred while getting file
     */
    @NotNull String getFile(String fileDistributionPath, String distributionLocale, boolean forceFlush) throws Exception;
}
