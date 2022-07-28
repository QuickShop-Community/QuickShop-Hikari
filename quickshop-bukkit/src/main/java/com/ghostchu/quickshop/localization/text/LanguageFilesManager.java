package com.ghostchu.quickshop.localization.text;

import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
     * @param locale       The locale code
     * @param distribution The values from Distribution platform
     */
    public void deploy(@NotNull String locale, @NotNull FileConfiguration distribution) {
        Log.debug("Registered and deployed locale: " + locale);
        FileConfiguration alreadyRegistered = this.locale2ContentMapping.get(locale);
        if (alreadyRegistered != null) {
            Log.debug("Applying and merging two different translations. (prefer cloud)");
            this.locale2ContentMapping.put(locale, merge(alreadyRegistered, distribution));
        } else {
            this.locale2ContentMapping.put(locale, distribution);
        }
    }

    @NotNull
    private FileConfiguration merge(@NotNull FileConfiguration alreadyRegistered, @NotNull FileConfiguration distribution) {
        FileConfiguration fileConfiguration = new YamlConfiguration();
        alreadyRegistered.getKeys(true).forEach(key -> fileConfiguration.set(key, alreadyRegistered.get(key)));
        distribution.getKeys(true).forEach(key -> fileConfiguration.set(key, distribution.get(key)));
        return fileConfiguration;
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


}
