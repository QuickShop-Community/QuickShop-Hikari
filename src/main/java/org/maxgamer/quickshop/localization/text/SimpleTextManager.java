/*
 * This file is a part of project QuickShop, the name is SimpleTextManager.java
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

package org.maxgamer.quickshop.localization.text;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.localization.text.TextManager;
import org.maxgamer.quickshop.api.localization.text.postprocessor.PostProcessor;
import org.maxgamer.quickshop.localization.text.distributions.Distribution;
import org.maxgamer.quickshop.localization.text.distributions.crowdin.CrowdinOTA;
import org.maxgamer.quickshop.localization.text.postprocessing.impl.ColorProcessor;
import org.maxgamer.quickshop.localization.text.postprocessing.impl.FillerProcessor;
import org.maxgamer.quickshop.localization.text.postprocessing.impl.PlaceHolderApiProcessor;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;
import org.maxgamer.quickshop.util.reload.Reloadable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SimpleTextManager implements TextManager, Reloadable {
    private static final String CROWDIN_LANGUAGE_FILE_PATH = "/master/crowdin/lang/%locale%/messages.json";
    public final List<PostProcessor> postProcessors = new ArrayList<>();
    private final QuickShop plugin;
    private final Distribution distribution;
    // <File <Locale, Section>>
    private final LanguageFilesManager languageFilesManager = new LanguageFilesManager();


    public SimpleTextManager(QuickShop plugin) {
        this.plugin = plugin;
        plugin.getReloadManager().register(this);
        plugin.getLogger().info("Translation over-the-air platform selected: Crowdin");
        this.distribution = new CrowdinOTA(plugin);
    }

    /**
     * Generate the override files storage path
     *
     * @param path The distribution file path
     * @return Override files storage path
     */
    @SneakyThrows
    @NotNull
    private File getOverrideFilesFolder(@NotNull String path) {
        File file = new File(path);
        String module = file.getParentFile().getName();
        File moduleFolder = new File(new File(plugin.getDataFolder(), "overrides"), module);
        moduleFolder.mkdirs();
        File fileFolder = new File(moduleFolder, file.getName());
        if (fileFolder.isDirectory())
            Files.deleteIfExists(fileFolder.toPath()); //TODO Workaround for v5 beta stage a bug, delete it in future
        return moduleFolder;
    }

    private final List<String> availableLanguages = new CopyOnWriteArrayList<>();

    /**
     * Loading bundled files from Jar file
     *
     * @param file The Crowdin file path
     * @return The bundled file configuration object
     */
    private JsonConfiguration loadBundled(String file) {
        JsonConfiguration bundledLang = new JsonConfiguration();
        File fileObject = new File(file);
        Path parentPath = fileObject.toPath().getParent();
        String parentStr = parentPath != null ? parentPath.toFile().getName() : "";
        String fileName = fileObject.getName();
        try {
            InputStream stream = null;
            if (!StringUtils.isEmpty(parentStr)) {
                //Try to fetch matched i18n resources
                stream = plugin.getResource("crowdin/lang/" + parentStr + "/" + fileName);
            }
            if (stream == null) {
                //Fallback to default
                stream = plugin.getResource("lang/" + fileName);
            }
            if (stream != null) {
                bundledLang.loadFromString(new String(IOUtils.toByteArray(new InputStreamReader(stream, StandardCharsets.UTF_8), StandardCharsets.UTF_8), StandardCharsets.UTF_8));
            } else {
                plugin.getLogger().log(Level.WARNING, "Cannot load bundled language file from jar, bundled language files " + (parentStr == null ? "" : parentStr) + "/" + fileName + " not found.");
                bundledLang = new JsonConfiguration();
            }
        } catch (IOException | InvalidConfigurationException ex) {
            bundledLang = new JsonConfiguration();
            plugin.getLogger().log(Level.SEVERE, "Cannot load bundled language file from jar, some strings may missing!", ex);
        }
        return bundledLang;
    }

    private final Cache<String, String> languagesCache =
            CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();

    /**
     * Reset everything
     */
    private void reset() {
        languagesCache.cleanUp();
        languageFilesManager.reset();
        postProcessors.clear();
        availableLanguages.clear();
    }

    /**
     * Return the set of available Languages
     *
     * @return the set of available Languages
     */
    @Override
    public List<String> getAvailableLanguages() {
        return Collections.unmodifiableList(availableLanguages);
    }

    /**
     * Gets specific locale status
     *
     * @param locale The locale
     * @param regex  The regexes
     * @return The locale enabled status
     */
    @Override
    public boolean localeEnabled(@NotNull String locale, @NotNull List<String> regex) {
        for (String languagesRegex : regex) {
            try {
                if (Pattern.matches(Util.createRegexFromGlob(languagesRegex), locale)) {
                    return true;
                }
            } catch (PatternSyntaxException exception) {
                Util.debugLog("Pattern " + languagesRegex + " invalid, skipping...");
            }
        }
        return false;
    }


    /**
     * Merge override data into distribution configuration to override texts
     *
     * @param distributionConfiguration The configuration that from distribution (will override it)
     * @param overrideConfiguration     The configuration that from local
     */
    private void applyOverrideConfiguration(@NotNull JsonConfiguration distributionConfiguration, @NotNull JsonConfiguration overrideConfiguration) {
        for (String key : overrideConfiguration.getKeys(true)) {
            if ("language-version".equals(key) || "config-version".equals(key) || "_comment".equals(key) || "version".equals(key)) {
                continue;
            }
            Object content = overrideConfiguration.get(key);
            if (content instanceof ConfigurationSection) {
                continue;
            }
            //Util.debugLog("Override key " + key + " with content: " + content);
            distributionConfiguration.set(key, content);
        }
    }

    /**
     * Getting configuration from distribution platform
     *
     * @param distributionFile Distribution path
     * @param distributionCode Locale code on distribution platform
     * @return The configuration
     * @throws Exception Any errors when getting it
     */
    private JsonConfiguration getDistributionConfiguration(@NotNull String distributionFile, @NotNull String distributionCode) throws Exception {
        JsonConfiguration configuration = new JsonConfiguration();
        try {
            // Load the locale file from local cache if available
            // Or load the locale file from remote server if it had updates or not exists.
            configuration.loadFromString(distribution.getFile(distributionFile, distributionCode));
        } catch (InvalidConfigurationException exception) {
            // Force loading the locale file form remote server because file not valid.
            configuration.loadFromString(distribution.getFile(distributionFile, distributionCode, true));
        }
        return configuration;
    }

    /**
     * Loading Crowdin OTA module and i18n system
     */
    public void load() {
        plugin.getLogger().info("Checking for translation updates, this may need a while...");
        this.reset();
        List<String> enabledLanguagesRegex = plugin.getConfig().getStringList("enabled-languages");
        //Make sure is a lowercase regex, prevent case-sensitive and underscore issue
        for (int i = 0; i < enabledLanguagesRegex.size(); i++) {
            enabledLanguagesRegex.set(i, enabledLanguagesRegex.get(i).toLowerCase(Locale.ROOT).replace("-", "_"));
        }
        //====Multi File and Multi-Language loader start====
        //Init offline default file
        languageFilesManager.deployBundled(CROWDIN_LANGUAGE_FILE_PATH, loadBundled(CROWDIN_LANGUAGE_FILE_PATH));
        //Get language code first
        distribution.getAvailableLanguages().parallelStream().forEach(crowdinCode ->
                //Then load all the files in this language code
                distribution.getAvailableFiles().forEach(crowdinFile -> {
                    try {
                        // Minecraft client use lowercase
                        String minecraftCode = crowdinCode.toLowerCase(Locale.ROOT).replace("-", "_");
                        if (!localeEnabled(minecraftCode, enabledLanguagesRegex)) {
                            Util.debugLog("Locale: " + minecraftCode + " not enabled in configuration.");
                            return;
                        }
                        //Offline default file
                        JsonConfiguration defaultFile = loadBundled(crowdinFile);
                        //Add available language (minecraftCode)
                        availableLanguages.add(minecraftCode);
                        Util.debugLog("Loading translation for locale: " + crowdinCode + " (" + minecraftCode + ")");
                        // Deploy bundled to mapper
                        languageFilesManager.deployBundled(crowdinFile, defaultFile);
                        // Loading bundled file (for no internet connection or failed loading)
                        JsonConfiguration configuration = loadBundled(crowdinFile.replace("%locale%", crowdinCode));
                        JsonConfiguration remoteConfiguration = getDistributionConfiguration(crowdinFile, crowdinCode);
                        // Only apply right language-version for client
                        if (defaultFile.isSet("language-version") && defaultFile.getString("language-version", "0").equals(remoteConfiguration.getString("language-version", "0"))) {
                            applyOverrideConfiguration(configuration, remoteConfiguration);
                        }
                        // Loading override text (allow user modification the translation)
                        JsonConfiguration override = getOverrideConfiguration(crowdinFile, minecraftCode);
                        applyOverrideConfiguration(configuration, override);
                        // Deploy distribution to mapper
                        languageFilesManager.deploy(crowdinFile, minecraftCode, configuration, defaultFile);
                        Util.debugLog("Locale " + crowdinFile.replace("%locale%", crowdinCode) + " has been successfully loaded");
                    } // Key founds in available locales but not in custom mapping on crowdin platform
                    catch (IOException e) {
                        // Network error
                        plugin.getLogger().log(Level.WARNING, "Couldn't update the translation for locale " + crowdinCode + " please check your network connection.", e);
                    } catch (Exception e) {
                        // Translation syntax error or other exceptions
                        plugin.getLogger().log(Level.WARNING, "Couldn't update the translation for locale " + crowdinCode + ".", e);
                    }
                }));

        // Register post processor
        postProcessors.add(new ColorProcessor());
        postProcessors.add(new FillerProcessor());
        postProcessors.add(new PlaceHolderApiProcessor());
    }

    private String findRelativeLanguages(String langCode) {
        //langCode may null when some plugins providing fake player
        if (langCode == null || langCode.isEmpty()) {
            return "en_us";
        }
        String result = languagesCache.getIfPresent(langCode);
        if (result == null) {
            result = "en_us";
            if (availableLanguages.contains(langCode)) {
                result = langCode;
            } else {
                String[] splits = langCode.split("_", 2);
                if (splits.length != 2) {
                    for (String availableLanguage : availableLanguages) {
                        if (availableLanguage.startsWith(langCode) || availableLanguage.endsWith(langCode)) {
                            result = availableLanguage;
                            break;
                        }
                    }
                } else {
                    String start = splits[0] + "_";
                    String end = "_" + splits[1];
                    for (String availableLanguage : availableLanguages) {
                        if (availableLanguage.startsWith(start) || availableLanguage.endsWith(end)) {
                            result = availableLanguage;
                            break;
                        }
                    }
                }
            }
            languagesCache.put(langCode, result);
        }
        return result;
    }

    /**
     * Getting user's override configuration for specific distribution path
     *
     * @param overrideFile The distribution
     * @param locale       the locale
     * @return The override configuration
     * @throws IOException                   IOException
     */
    private JsonConfiguration getOverrideConfiguration(@NotNull String overrideFile, @NotNull String locale) throws IOException {
        File localOverrideFile = new File(getOverrideFilesFolder(overrideFile.replace("%locale%", locale)), new File(overrideFile.replace("%locale%", locale)).getName());
        if (!localOverrideFile.exists()) {
            Util.debugLog("Creating locale override file: " + localOverrideFile);
            localOverrideFile.getParentFile().mkdirs();
            localOverrideFile.createNewFile();
        }
        JsonConfiguration result = JsonConfiguration.loadConfiguration(localOverrideFile);
        //Add a comment for user guide if file is empty
        if (result.getKeys(false).isEmpty()) {
            result.set("_comment", "Please visit https://github.com/PotatoCraft-Studio/QuickShop-Reremake/wiki/Use-translation-override-system for override language file tutorial.");
            result.save(localOverrideFile);
        }
        return result;
    }

    /**
     * Getting the translation with path with default locale
     *
     * @param path THe path
     * @param args The arguments
     * @return The text object
     */
    @Override
    public @NotNull Text of(@NotNull String path, Object... args) {
        return new Text(this, (CommandSender) null, languageFilesManager.getDistribution(CROWDIN_LANGUAGE_FILE_PATH), languageFilesManager.getBundled(CROWDIN_LANGUAGE_FILE_PATH), path, args);
    }

    /**
     * Getting the translation with path with player's locale (if available)
     *
     * @param sender The sender
     * @param path   The path
     * @param args   The arguments
     * @return The text object
     */
    @Override
    public @NotNull Text of(@Nullable CommandSender sender, @NotNull String path, Object... args) {
        return new Text(this, sender, languageFilesManager.getDistribution(CROWDIN_LANGUAGE_FILE_PATH), languageFilesManager.getBundled(CROWDIN_LANGUAGE_FILE_PATH), path, args);
    }

    /**
     * Getting the translation with path with player's locale (if available)
     *
     * @param sender The player unique id
     * @param path   The path
     * @param args   The arguments
     * @return The text object
     */
    @Override
    public @NotNull Text of(@Nullable UUID sender, @NotNull String path, Object... args) {
        return new Text(this, sender, languageFilesManager.getDistribution(CROWDIN_LANGUAGE_FILE_PATH), languageFilesManager.getBundled(CROWDIN_LANGUAGE_FILE_PATH), path, args);
    }

    /**
     * Getting the translation with path with default locale (if available)
     *
     * @param path The path
     * @param args The arguments
     * @return The text object
     */
    @Override
    public @NotNull TextList ofList(@NotNull String path, Object... args) {
        return new TextList(this, (CommandSender) null, languageFilesManager.getDistribution(CROWDIN_LANGUAGE_FILE_PATH), languageFilesManager.getBundled(CROWDIN_LANGUAGE_FILE_PATH), path, args);
    }

    /**
     * Getting the translation with path  with player's locale (if available)
     *
     * @param sender The player unique id
     * @param path   The path
     * @param args   The arguments
     * @return The text object
     */
    @Override
    public @NotNull TextList ofList(@Nullable UUID sender, @NotNull String path, Object... args) {
        return new TextList(this, sender, languageFilesManager.getDistribution(CROWDIN_LANGUAGE_FILE_PATH), languageFilesManager.getBundled(CROWDIN_LANGUAGE_FILE_PATH), path, args);
    }

    /**
     * Getting the translation with path with player's locale (if available)
     *
     * @param sender The player
     * @param path   The path
     * @param args   The arguments
     * @return The text object
     */
    @Override
    public @NotNull TextList ofList(@Nullable CommandSender sender, @NotNull String path, Object... args) {
        return new TextList(this, sender, languageFilesManager.getDistribution(CROWDIN_LANGUAGE_FILE_PATH), languageFilesManager.getBundled(CROWDIN_LANGUAGE_FILE_PATH), path, args);
    }

    @Override
    public ReloadResult reloadModule() {
        this.load();
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }

    public static class TextList implements org.maxgamer.quickshop.api.localization.text.TextList {
        private final SimpleTextManager manager;
        private final String path;
        private final Map<String, JsonConfiguration> mapping;
        private final CommandSender sender;
        private final Object[] args;
        @Nullable
        private final JsonConfiguration bundled;

        private TextList(SimpleTextManager manager, CommandSender sender, Map<String, JsonConfiguration> mapping, @Nullable JsonConfiguration bundled, String path, Object... args) {
            this.manager = manager;
            this.sender = sender;
            this.mapping = mapping;
            this.bundled = bundled;
            this.path = path;
            this.args = args;
        }

        private TextList(SimpleTextManager manager, UUID sender, Map<String, JsonConfiguration> mapping, @Nullable JsonConfiguration bundled, String path, Object... args) {
            this.manager = manager;
            if (sender != null) {
                this.sender = Bukkit.getPlayer(sender);
            } else {
                this.sender = null;
            }
            this.mapping = mapping;
            this.bundled = bundled;
            this.path = path;
            this.args = args;
        }

        /**
         * Getting the bundled fallback text
         *
         * @return The bundled text
         */
        private @NotNull List<String> fallbackLocal() {
            return this.bundled != null ? this.bundled.getStringList(path) : Collections.emptyList();
        }

        /**
         * Post processes the text
         *
         * @param text The text
         * @return The text that processed
         */
        @NotNull
        private List<String> postProcess(@NotNull List<String> text) {
            List<String> texts = new ArrayList<>();
            for (PostProcessor postProcessor : this.manager.postProcessors) {
                for (String s : text) {
                    texts.add(postProcessor.process(s, sender, args));
                }
            }
            return texts;
        }
        /**
         * Getting the text that use specify locale
         *
         * @param locale The minecraft locale code (like en_us)
         * @return The text
         */
        @Override
        @NotNull
        public List<String> forLocale(@NotNull String locale) {
            JsonConfiguration index = mapping.get(manager.findRelativeLanguages(locale));
            if (index == null) {
                Util.debugLog("Fallback " + locale + " to default game-language locale caused by QuickShop doesn't support this locale");
                String languageCode = MsgUtil.getDefaultGameLanguageCode();
                if (languageCode.equals(locale)) {
                    List<String> str = fallbackLocal();
                    if (str.isEmpty()) {
                        return Collections.singletonList("Fallback Missing Language Key: " + path + ", report to QuickShop!");
                    }
                    return postProcess(str);
                } else {
                    return forLocale(languageCode);
                }
            } else {
                List<String> str = index.getStringList(path);
                if (str.isEmpty()) {
                    // Fallback
                    Util.debugLog("Fallback " + path + " to bundle translation caused OTA & User's override file doesn't contains this key");
                    str = fallbackLocal();
                    if (str.isEmpty()) {
                        return Collections.singletonList("Fallback Missing Language Key: " + path + ", report to QuickShop!");
                    }
                }
                return postProcess(str);
            }
        }

        /**
         * Getting the text for player locale
         *
         * @return Getting the text for player locale
         */
        @Override
        @NotNull
        public List<String> forLocale() {
            if (sender instanceof Player) {
                return forLocale(((Player) sender).getLocale());
            } else {
                return forLocale(MsgUtil.getDefaultGameLanguageCode());
            }
        }

        /**
         * Send text to the player
         */
        @Override
        public void send() {
            if (sender == null) {
                return;
            }
            for (String s : forLocale()) {
                MsgUtil.sendDirectMessage(sender, s);
            }
        }
    }

    public static class Text implements org.maxgamer.quickshop.api.localization.text.Text {
        private final SimpleTextManager manager;
        private final String path;
        private final Map<String, JsonConfiguration> mapping;
        private final CommandSender sender;
        private final Object[] args;
        @Nullable
        private final JsonConfiguration bundled;

        private Text(SimpleTextManager manager, CommandSender sender, Map<String, JsonConfiguration> mapping, @Nullable JsonConfiguration bundled, String path, Object... args) {
            this.manager = manager;
            this.sender = sender;
            this.mapping = mapping;
            this.path = path;
            this.bundled = bundled;
            this.args = args;
        }

        private Text(SimpleTextManager manager, UUID sender, Map<String, JsonConfiguration> mapping, @Nullable JsonConfiguration bundled, String path, Object... args) {
            this.manager = manager;
            if (sender != null) {
                this.sender = Bukkit.getPlayer(sender);
            } else {
                this.sender = null;
            }
            this.mapping = mapping;
            this.bundled = bundled;
            this.path = path;
            this.args = args;
        }

        /**
         * Getting the bundled fallback text
         *
         * @return The bundled text
         */
        @Nullable
        private String fallbackLocal() {
            return this.bundled != null ? this.bundled.getString(path) : null;
        }

        /**
         * Post processes the text
         *
         * @param text The text
         * @return The text that processed
         */
        @NotNull
        private String postProcess(@NotNull String text) {
            for (PostProcessor postProcessor : this.manager.postProcessors) {
                text = postProcessor.process(text, sender, args);
            }
            return text;
        }

        /**
         * Getting the text that use specify locale
         *
         * @param locale The minecraft locale code (like en_us)
         * @return The text
         */
        @Override
        @NotNull
        public String forLocale(@NotNull String locale) {
            JsonConfiguration index = mapping.get(manager.findRelativeLanguages(locale));
            if (index == null) {
                Util.debugLog("Fallback " + locale + " to default game-language locale caused by QuickShop doesn't support this locale");
                if (MsgUtil.getDefaultGameLanguageCode().equals(locale)) {
                    String str = fallbackLocal();
                    if (str == null) {
                        return "Fallback Missing Language Key: " + path + ", report to QuickShop!";
                    }
                    return postProcess(str);
                } else {
                    return forLocale(MsgUtil.getDefaultGameLanguageCode());
                }
            } else {
                String str = index.getString(path);
                if (str == null) {
                    // Fallback
                    Util.debugLog("Fallback " + path + " to bundle translation caused OTA & User's override file doesn't contains this key");
                    str = fallbackLocal();
                    if (str == null) {
                        return "Fallback Missing Language Key: " + path + ", report to QuickShop!";
                    }
                }
                return postProcess(str);
            }
        }

        /**
         * Getting the text for player locale
         *
         * @return Getting the text for player locale
         */
        @Override
        @NotNull
        public String forLocale() {
            if (sender instanceof Player) {
                return forLocale(((Player) sender).getLocale());
            } else {
                return forLocale(MsgUtil.getDefaultGameLanguageCode());
            }
        }

        /**
         * Send text to the player
         */
        @Override
        public void send() {
            if (sender == null) {
                return;
            }
            String lang = forLocale();
            MsgUtil.sendDirectMessage(sender, lang);
            // plugin.getQuickChat().send(sender, lang);
        }
    }
}
