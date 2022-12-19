package com.ghostchu.quickshop.localization.text;

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
     * Deploy new locale to TextMapper with cloud values and bundle values
     *
     * @param locale          The locale code
     * @param newDistribution The values from Distribution platform
     */
    public void deploy(@NotNull String locale, @NotNull FileConfiguration newDistribution) {
        if (!this.locale2ContentMapping.containsKey(locale)) {
            this.locale2ContentMapping.put(locale, newDistribution);
        } else {
            FileConfiguration exists = this.locale2ContentMapping.get(locale);
            merge(exists, newDistribution);
        }
    }

    public void fillMissing(@NotNull FileConfiguration fallback) {
        for (FileConfiguration value : this.locale2ContentMapping.values()) {
            mergeMissing(value, fallback);
        }
    }


    public void destroy(@NotNull String locale) {
        this.locale2ContentMapping.remove(locale);
    }

    private void merge(@NotNull FileConfiguration alreadyRegistered, @NotNull FileConfiguration newConfiguration) {
        for (String key : newConfiguration.getKeys(true)) {
            if (newConfiguration.isConfigurationSection(key)) continue;
            alreadyRegistered.set(key, newConfiguration.get(key));
        }
    }

    private void mergeMissing(@NotNull FileConfiguration alreadyRegistered, @NotNull FileConfiguration newConfiguration) {
        for (String key : newConfiguration.getKeys(true)) {
            if (newConfiguration.isConfigurationSection(key)) continue;
            if (alreadyRegistered.isSet(key)) continue;
            alreadyRegistered.set(key, newConfiguration.get(key));
        }
    }

    /**
     * Getting specific locale data under specific distribution data
     *
     * @param locale The specific locale
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
     * Reset TextMapper
     */
    public void reset() {
        this.locale2ContentMapping.clear();
    }


}
