package org.maxgamer.quickshop.localization.text.distributions;

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
