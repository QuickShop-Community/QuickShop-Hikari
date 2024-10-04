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
  public void deploy(@NotNull final String locale, @NotNull final FileConfiguration newDistribution) {

    if(!this.locale2ContentMapping.containsKey(locale)) {
      this.locale2ContentMapping.put(locale, newDistribution);
    } else {
      final FileConfiguration exists = this.locale2ContentMapping.get(locale);
      merge(exists, newDistribution);
    }
  }

  private void merge(@NotNull final FileConfiguration alreadyRegistered, @NotNull final FileConfiguration newConfiguration) {

    for(final String key : newConfiguration.getKeys(true)) {
      if(newConfiguration.isConfigurationSection(key)) {
        continue;
      }
      alreadyRegistered.set(key, newConfiguration.get(key));
    }
  }

  public void fillMissing(@NotNull final FileConfiguration fallback) {

    for(final FileConfiguration value : this.locale2ContentMapping.values()) {
      mergeMissing(value, fallback);
    }
  }

  private void mergeMissing(@NotNull final FileConfiguration alreadyRegistered, @NotNull final FileConfiguration newConfiguration) {

    for(final String key : newConfiguration.getKeys(true)) {
      if(newConfiguration.isConfigurationSection(key)) {
        continue;
      }
      if(alreadyRegistered.isSet(key)) {
        continue;
      }
      alreadyRegistered.set(key, newConfiguration.get(key));
    }
  }

  public void destroy(@NotNull final String locale) {

    this.locale2ContentMapping.remove(locale);
  }

  /**
   * Getting specific locale data under specific distribution data
   *
   * @param locale The specific locale
   *
   * @return The locale data, null if never deployed
   */
  public @Nullable FileConfiguration getDistribution(@NotNull final String locale) {

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
  public void remove(@NotNull final String distributionPath) {

    this.locale2ContentMapping.remove(distributionPath);
  }

  /**
   * Remove specific locales data under specific distribution path
   *
   * @param distributionPath The distribution path
   * @param locale           The locale
   */
  public void remove(@NotNull final String distributionPath, @NotNull final String locale) {

    if(this.locale2ContentMapping.containsKey(distributionPath)) {
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
