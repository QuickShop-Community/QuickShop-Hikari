/*
 *  This file is a part of project QuickShop, the name is SimpleTextManager.java
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

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.localization.text.TextManager;
import com.ghostchu.quickshop.api.localization.text.postprocessor.PostProcessor;
import com.ghostchu.quickshop.localization.text.distributions.Distribution;
import com.ghostchu.quickshop.localization.text.distributions.crowdin.CrowdinOTA;
import com.ghostchu.quickshop.localization.text.postprocessing.impl.FillerProcessor;
import com.ghostchu.quickshop.localization.text.postprocessing.impl.PlaceHolderApiProcessor;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import com.ghostchu.simplereloadlib.Reloadable;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SimpleTextManager implements TextManager, Reloadable {
    private static String CROWDIN_LANGUAGE_FILE_PATH = "/hikari/crowdin/lang/%locale%/messages.yml";
    public final Set<PostProcessor> postProcessors = new LinkedHashSet<>();
    private final QuickShop plugin;
    @Nullable
    private Distribution distribution;
    // <File <Locale, Section>>
    private final LanguageFilesManager languageFilesManager = new LanguageFilesManager();
    private final Set<String> availableLanguages = new LinkedHashSet<>();
    private final Cache<String, String> languagesCache =
            CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();
    private final MiniMessage miniMessage = MiniMessage.builder().strict(false).build();
    public SimpleTextManager(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        plugin.getReloadManager().register(this);
        plugin.getLogger().info("Translation over-the-air platform selected: Crowdin");
        try {
            this.distribution = new CrowdinOTA(plugin);
        } catch (IOException e) {
            this.distribution = null;
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize Crowdin OTA distribution, cloud translations update failed.", e);
        }
    }


    /**
     * Generate the override files storage path
     *
     * @param localeCode The language localeCode
     * @return Override files storage path
     */
    @SneakyThrows
    @NotNull
    private File getOverrideFilesFolder(@NotNull String localeCode) {
        File moduleFolder = new File(new File(plugin.getDataFolder(), "overrides"), localeCode);
        moduleFolder.mkdirs();
        File fileFolder = new File(moduleFolder, localeCode);
        if (fileFolder.isDirectory()) {
            Files.deleteIfExists(fileFolder.toPath()); //TODO Workaround for v5 beta stage a bug, delete it in future
        }
        return moduleFolder;
    }


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
        return new ArrayList<>(availableLanguages);
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
                Log.debug("Pattern " + languagesRegex + " invalid, skipping...");
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
    private void applyOverrideConfiguration(@NotNull FileConfiguration distributionConfiguration, @NotNull FileConfiguration overrideConfiguration) {
        for (String key : overrideConfiguration.getKeys(true)) {
            if ("language-version".equals(key) || "config-version".equals(key) || "_comment".equals(key) || "version".equals(key)) {
                continue;
            }
            Object content = overrideConfiguration.get(key);
            if (content instanceof ConfigurationSection) {
                continue;
            }
            //Log.debug("Override key " + key + " with content: " + content);
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
    private FileConfiguration getDistributionConfiguration(@NotNull String distributionFile, @NotNull String distributionCode) throws Exception {
        FileConfiguration configuration = new YamlConfiguration();
        try {
            // Load the locale file from local cache if available
            // Or load the locale file from remote server if it had updates or not exists.
            if (distribution == null)
                throw new IllegalStateException("Distribution hadn't initialized yet!");
            configuration.loadFromString(distribution.getFile(distributionFile, distributionCode));
        } catch (InvalidConfigurationException exception) {
            // Force loading the locale file form remote server because file not valid.
            configuration.loadFromString(distribution.getFile(distributionFile, distributionCode, true));
            plugin.getLogger().log(Level.WARNING, "Cannot load language file from distribution platform, some strings may missing!", exception);
        }
        return configuration;
    }

    /**
     * Loading translations from bundled resources
     *
     * @return The bundled translations, empty hash map if nothing can be load.
     */
    @NotNull
    private Map<String, FileConfiguration> loadBundled() {
        File jarFile;
        try {
            jarFile = Util.getPluginJarFile(plugin);
        } catch (FileNotFoundException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load bundled translation: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
        try (ZipFile zipFile = new ZipFile(jarFile, "UTF-8")) {
            // jar/lang/<region_code>/
            Map<String, FileConfiguration> availableLang = new HashMap<>();
            zipFile.getEntries().asIterator().forEachRemaining(entry -> {
                if (entry.isDirectory())
                    return;
                if (!entry.getName().startsWith("lang/"))
                    return;
                if (!entry.getName().endsWith("messages.yml"))
                    return;
                String[] split = entry.getName().split("/");
                String locale = split[split.length - 2];
                if (zipFile.canReadEntryData(entry)) {
                    try {
                        YamlConfiguration configuration = new YamlConfiguration();
                        configuration.loadFromString(new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8));
                        availableLang.put(locale.toLowerCase(Locale.ROOT).replace("-", "_"), configuration);
                        Log.debug("Bundled language file: " + locale + " at " + entry.getName() + " loaded.");
                    } catch (IOException | InvalidConfigurationException e) {
                        plugin.getLogger().log(Level.WARNING, "Failed to load bundled translation.", e);
                    }
                }
            });
            return availableLang;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load bundled translation, jar invalid: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }

    }

    /**
     * Loading Crowdin OTA module and i18n system
     */
    public void load() {
        plugin.getLogger().info("Loading up translations from Crowdin OTA, this may need a while...");
        //TODO: This will break the message processing system in-game until loading finished, need to fix it.
        this.reset();
        List<String> enabledLanguagesRegex = plugin.getConfig().getStringList("enabled-languages");
        //Make sure is a lowercase regex, prevent case-sensitive and underscore issue
        enabledLanguagesRegex.replaceAll(s -> s.toLowerCase(Locale.ROOT).replace("-", "_"));
        // Load bundled translations
        loadBundled().forEach((locale, configuration) -> {
            if (localeEnabled(locale, enabledLanguagesRegex)) {
                Log.debug("Initializing language with bundled resource: " + locale);
                FileConfiguration override;
                try {
                    override = getOverrideConfiguration(locale);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to load bundled translation.", e);
                    return;
                }
                applyOverrideConfiguration(configuration, override);
                availableLanguages.add(locale);
                languageFilesManager.deploy(locale, configuration);
                Log.debug("Initialized language with bundled resource: " + locale);
            } else {
                Log.debug("Locale " + locale + " is disabled, skipping...");
            }
        });

        //====Multi File and Multi-Language loader start====
        //Get language code first
        if (distribution != null) {
            /* Workaround for Crowdin bug. */
            if (!distribution.getAvailableFiles().contains(CROWDIN_LANGUAGE_FILE_PATH)) {
                Log.debug("Warning! Illegal file path detected, trying auto fix...");
                List<String> messagesFiles = distribution.getAvailableFiles().stream().filter(s -> s.endsWith("messages.yml")).toList();
                if (!messagesFiles.isEmpty()) {
                    CROWDIN_LANGUAGE_FILE_PATH = messagesFiles.get(0);
                } else {
                    Log.debug("Auto fix failed :(");
                }
            }
            distribution.getAvailableLanguages().parallelStream().forEach(crowdinCode -> {
                //Then load all the files in this language code
                try {
                    // Minecraft client use lowercase
                    String minecraftCode = crowdinCode.toLowerCase(Locale.ROOT).replace("-", "_");
                    if (!localeEnabled(minecraftCode, enabledLanguagesRegex)) {
                        Log.debug("Locale: " + minecraftCode + " not enabled in configuration.");
                        return;
                    }
                    //Add available language (minecraftCode)
                    availableLanguages.add(minecraftCode);
                    Log.debug("Loading translation for locale: " + crowdinCode + " (" + minecraftCode + ")");
                    FileConfiguration remoteConfiguration = getDistributionConfiguration(CROWDIN_LANGUAGE_FILE_PATH, crowdinCode);
                    // Loading override text (allow user modification the translation)
                    FileConfiguration override = getOverrideConfiguration(minecraftCode);
                    applyOverrideConfiguration(remoteConfiguration, override);
                    // Deploy distribution to mapper
                    languageFilesManager.deploy(minecraftCode, remoteConfiguration);
                    Log.debug("Locale " + CROWDIN_LANGUAGE_FILE_PATH.replace("%locale%", crowdinCode) + " has been successfully loaded");
                } // Key founds in available locales but not in custom mapping on crowdin platform
                catch (IOException e) {
                    // Network error
                    plugin.getLogger().log(Level.WARNING, "Couldn't update the translation for locale " + crowdinCode + " please check your network connection.", e);
                } catch (Exception e) {
                    // Translation syntax error or other exceptions
                    plugin.getLogger().log(Level.WARNING, "Couldn't update the translation for locale " + crowdinCode + ".", e);
                }
            });
        }

        // Register post processor
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
                String start = langCode;
                String end = langCode;
                if (splits.length == 2) {
                    start = splits[0] + "_";
                    end = "_" + splits[1];
                }
                for (String availableLanguage : availableLanguages) {
                    if (availableLanguage.startsWith(start) || availableLanguage.endsWith(end)) {
                        result = availableLanguage;
                        break;
                    }
                }
            }
            Log.debug("Registering relative language " + langCode + " to " + result);
            languagesCache.put(langCode, result);
        }
        return result;
    }

    /**
     * Getting user's override configuration for specific distribution path
     *
     * @param locale the locale
     * @return The override configuration
     * @throws IOException IOException
     */
    private FileConfiguration getOverrideConfiguration(@NotNull String locale) throws IOException {
        File localOverrideFile = new File(getOverrideFilesFolder(locale), "messages.yml");
        if (!localOverrideFile.exists()) {
            Log.debug("Creating locale override file: " + localOverrideFile);
            localOverrideFile.getParentFile().mkdirs();
            localOverrideFile.createNewFile();
        }
        FileConfiguration result = YamlConfiguration.loadConfiguration(localOverrideFile);
        //Add a comment for user guide if file is empty
        if (result.getKeys(false).isEmpty()) {
          //  result.options().setHeader(List.of("Please visit https://github.com/PotatoCraft-Studio/QuickShop-Reremake/wiki/Use-translation-override-system for override language file tutorial."));
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
        return new Text(this, (CommandSender) null, languageFilesManager.getDistributions(), path, convert(args));
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
        return new Text(this, sender, languageFilesManager.getDistributions(), path, convert(args));
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
        return new Text(this, sender, languageFilesManager.getDistributions(), path, convert(args));
    }

    @NotNull
    private Component[] convert(@Nullable Object... args) {
        if (args == null || args.length == 0)
            return new Component[0];
        Component[] components = new Component[args.length];
        for (int i = 0; i < args.length; i++) {
            Object obj = args[i];
            if (obj == null) {
                components[i] = Component.empty();
                continue;
            }
            Class<?> clazz = obj.getClass();
            if (obj instanceof Component component) {
                components[i] = component;
                continue;
            }
            if (obj instanceof ComponentLike componentLike) {
                components[i] = componentLike.asComponent();
                continue;
            }
            // Check
            try {
                if (Character.class.equals(clazz)) {
                    components[i] = Component.text((char) obj);
                    continue;
                }
                if (Byte.class.equals(clazz)) {
                    components[i] = Component.text((Byte) obj);
                    continue;
                }
                if (Integer.class.equals(clazz)) {
                    components[i] = Component.text((Integer) obj);
                    continue;
                }
                if (Long.class.equals(clazz)) {
                    components[i] = Component.text((Long) obj);
                    continue;
                }
                if (Float.class.equals(clazz)) {
                    components[i] = Component.text((Float) obj);
                    continue;
                }
                if (Double.class.equals(clazz)) {
                    components[i] = Component.text((Double) obj);
                    continue;
                }
                if (Boolean.class.equals(clazz)) {
                    components[i] = Component.text((Boolean) obj);
                    continue;
                }
                if (String.class.equals(clazz)) {
                    components[i] = LegacyComponentSerializer.legacySection().deserialize((String) obj);
                    continue;
                }
                components[i] = LegacyComponentSerializer.legacySection().deserialize(obj.toString());
            } catch (Exception exception) {
                Log.debug("Failed to process the object: " + obj);
                if (plugin.getSentryErrorReporter() != null)
                    plugin.getSentryErrorReporter().sendError(exception, "Failed to process the object: " + obj);
                components[i] = LegacyComponentSerializer.legacySection().deserialize(obj.toString());
            }
            // undefined

        }
        return components;
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
        return new TextList(this, (CommandSender) null, languageFilesManager.getDistributions(), path, convert(args));
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
        return new TextList(this, sender, languageFilesManager.getDistributions(), path, convert(args));
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
        return new TextList(this, sender, languageFilesManager.getDistributions(), path, convert(args));
    }

    @Override
    public ReloadResult reloadModule() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::load);

        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }

    public static class TextList implements com.ghostchu.quickshop.api.localization.text.TextList {
        private final SimpleTextManager manager;
        private final String path;
        private final Map<String, FileConfiguration> mapping;
        private final CommandSender sender;
        private final Component[] args;

        private TextList(SimpleTextManager manager, CommandSender sender, Map<String, FileConfiguration> mapping, String path, Component... args) {
            this.manager = manager;
            this.sender = sender;
            this.mapping = mapping;
            this.path = path;
            this.args = args;
        }

        private TextList(SimpleTextManager manager, UUID sender, Map<String, FileConfiguration> mapping, String path, Component... args) {
            this.manager = manager;
            if (sender != null) {
                this.sender = Bukkit.getPlayer(sender);
            } else {
                this.sender = null;
            }
            this.mapping = mapping;
            this.path = path;
            this.args = args;
        }


        /**
         * Post processes the text
         *
         * @param text The text
         * @return The text that processed
         */
        @NotNull
        private List<Component> postProcess(@NotNull List<Component> text) {
            List<Component> texts = new ArrayList<>();
            for (PostProcessor postProcessor : this.manager.postProcessors) {
                for (Component s : text) {
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
        public List<Component> forLocale(@NotNull String locale) {
            FileConfiguration index = mapping.get(manager.findRelativeLanguages(locale));
            if (index == null) {
                Log.debug("Fallback " + locale + " to default game-language locale caused by QuickShop doesn't support this locale");
                String languageCode = MsgUtil.getDefaultGameLanguageCode();
                if (languageCode.equals(locale)) {
                    Log.debug("Fallback Missing Language Key: " + path + ", report to QuickShop!");
                    return Collections.singletonList(LegacyComponentSerializer.legacySection().deserialize(path));
                } else {
                    return forLocale(languageCode);
                }
            } else {
                List<String> str = index.getStringList(path);
                if (str.isEmpty()) {
                    Log.debug("Fallback Missing Language Key: " + path + ", report to QuickShop!");
                    return Collections.singletonList(LegacyComponentSerializer.legacySection().deserialize(path));
                }
                List<Component> components = str.stream().map(manager.miniMessage::deserialize).toList();
                return postProcess(components);
            }
        }

        /**
         * Getting the text for player locale
         *
         * @return Getting the text for player locale
         */
        @Override
        @NotNull
        public List<Component> forLocale() {
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
            for (Component s : forLocale()) {
                MsgUtil.sendDirectMessage(sender, s);
            }
        }
    }

    public static class Text implements com.ghostchu.quickshop.api.localization.text.Text {
        private final SimpleTextManager manager;
        private final String path;
        private final Map<String, FileConfiguration> mapping;
        private final CommandSender sender;
        private final Component[] args;

        private Text(SimpleTextManager manager, CommandSender sender, Map<String, FileConfiguration> mapping, String path, Component... args) {
            this.manager = manager;
            this.sender = sender;
            this.mapping = mapping;
            this.path = path;
            this.args = args;
        }

        private Text(SimpleTextManager manager, UUID sender, Map<String, FileConfiguration> mapping, String path, Component... args) {
            this.manager = manager;
            if (sender != null) {
                this.sender = Bukkit.getPlayer(sender);
            } else {
                this.sender = null;
            }
            this.mapping = mapping;
            this.path = path;
            this.args = args;
        }

        /**
         * Post processes the text
         *
         * @param text The text
         * @return The text that processed
         */
        @NotNull
        private Component postProcess(@NotNull Component text) {
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
        public Component forLocale(@NotNull String locale) {
            FileConfiguration index = mapping.get(manager.findRelativeLanguages(locale));
            if (index == null) {
                Log.debug("Index for " + locale + " is null");
                Log.debug("Fallback " + locale + " to default game-language locale caused by QuickShop doesn't support this locale");
                if (MsgUtil.getDefaultGameLanguageCode().equals(locale)) {
                    Log.debug("Fallback Missing Language Key: " + path + ", report to QuickShop!");
                    return LegacyComponentSerializer.legacySection().deserialize(path);
                } else {
                    return forLocale(MsgUtil.getDefaultGameLanguageCode());
                }
            } else {
                String str = index.getString(path);
                if (str == null) {
                    Log.debug("The value about index " + index + " is null");
                    Log.debug("Missing Language Key: " + path + ", report to QuickShop!");
                    return LegacyComponentSerializer.legacySection().deserialize(path);
                }
                Component component = manager.miniMessage.deserialize(str);
                return postProcess(component);
            }
        }

        /**
         * Getting the text for player locale
         *
         * @return Getting the text for player locale
         */
        @Override
        @NotNull
        public Component forLocale() {
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
            Component lang = forLocale();
            MsgUtil.sendDirectMessage(sender, lang);
            // plugin.getQuickChat().send(sender, lang);
        }
    }
}
