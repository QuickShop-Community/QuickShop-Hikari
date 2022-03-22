/*
 *  This file is a part of project QuickShop, the name is CrowdinOTA.java
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

package com.ghostchu.quickshop.localization.text.distributions.crowdin;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.localization.text.distributions.Distribution;
import com.ghostchu.quickshop.localization.text.distributions.crowdin.bean.Manifest;
import com.ghostchu.quickshop.util.JsonUtil;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.UrlEncoderDecoder;
import com.ghostchu.quickshop.util.Util;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class CrowdinOTA implements Distribution {
    //DO NOT final it! Unit-test needs to change it to prevent network flow
    protected static final String CROWDIN_OTA_HOST = "https://distributions.crowdin.net/24ecd9a81c9f67d200825b7xrm4/";
    private final QuickShop plugin;
    private final OTACacheControl otaCacheControl = new OTACacheControl();

    public CrowdinOTA(QuickShop plugin) {
        this.plugin = plugin;
        Util.getCacheFolder().mkdirs();

    }

    /**
     * Getting the Crowdin distribution manifest
     *
     * @return The distribution manifest
     */

    @NotNull
    public Manifest getManifest() {
        return JsonUtil.regular().fromJson(getManifestJson(), Manifest.class);
    }

    /**
     * Getting the Crowdin distribution manifest json
     *
     * @return The distribution manifest json, return local one when failed
     */
    @NotNull
    public String getManifestJson() {
        String url = CROWDIN_OTA_HOST + "manifest.json";
        // TODO: This is hacky
        String data = """
                {"files":["\\/hikari\\/quickshop-bukkit\\/src\\/main\\/resources\\/crowdin\\/%locale%\\/messages.yml"],"languages":["af","ar","bg","ca","zh-CN","zh-TW","zh-HK","cs","da","nl","en","fi","fr","de","el","he","hu","it","ja","ko","lt","no","pl","pt-PT","pt-BR","ro","ru","sr","es-ES","sv-SE","th","tr","uk","vi"],"language_mapping":{"ro":{"locale":"ro-RO"},"fr":{"locale":"fr-FR"},"es-ES":{"locale":"es-ES"},"af":{"locale":"af-ZA"},"ar":{"locale":"ar-SA"},"bg":{"locale":"bg-BG"},"ca":{"locale":"ca-ES"},"cs":{"locale":"cs-CZ"},"da":{"locale":"da-DK"},"de":{"locale":"de-DE"},"el":{"locale":"el-GR"},"fi":{"locale":"fi-FI"},"he":{"locale":"he-IL"},"hu":{"locale":"hu-HU"},"it":{"locale":"it-IT"},"ja":{"locale":"ja-JP"},"ko":{"locale":"ko-KR"},"lt":{"locale":"lt-LT"},"nl":{"locale":"nl-NL"},"no":{"locale":"no-NO"},"pl":{"locale":"pl-PL"},"pt-PT":{"locale":"pt-PT"},"ru":{"locale":"ru-RU"},"sr":{"locale":"sr-SP"},"sv-SE":{"locale":"sv-SE"},"tr":{"locale":"tr-TR"},"uk":{"locale":"uk-UA"},"zh-CN":{"locale":"zh-CN"},"zh-TW":{"locale":"zh-TW"},"en":{"locale":"en-US"},"vi":{"locale":"vi-VN"},"pt-BR":{"locale":"pt-BR"},"th":{"locale":"th-TH"},"zh-HK":{"locale":"zh-HK"}},"custom_languages":[],"timestamp":1644838370}
                """;
        HttpResponse<String> response = Unirest.get(url).asString();
        if (!response.isSuccess()) {
            return data;
        }
        return response.getBody();

    }

    /**
     * Getting crowdin language mapping (crowdin code -> minecraft code)
     * Can be set on Crowdin platform
     *
     * @return The language mapping
     */
    public Map<String, String> genLanguageMapping() {
        Map<String, String> mapping = new HashMap<>();
        JsonElement parser = JsonParser.parseString(getManifestJson());
        for (Map.Entry<String, JsonElement> set : parser.getAsJsonObject().getAsJsonObject("language_mapping").entrySet()) {
            if (!set.getValue().isJsonObject()) {
                continue;
            }
            JsonPrimitive object = set.getValue().getAsJsonObject().getAsJsonPrimitive("locale");
            if (object == null) {
                continue;
            }
            mapping.put(set.getKey(), object.getAsString());
        }
        return mapping;
    }

    /**
     * Getting all languages available on crowdin, so we can use that as the key to read language mapping.
     *
     * @return The languages available
     */
    @Override
    @NotNull
    public List<String> getAvailableLanguages() {
        Manifest manifest = getManifest();
        List<String> languages = new ArrayList<>();
        Map<String, String> mapping = genLanguageMapping();
        for (String language : manifest.getLanguages()) {
            languages.add(mapping.getOrDefault(language, language));
        }
        return languages;
    }

    @Override
    @NotNull
    public List<String> getAvailableFiles() {
        Manifest manifest = getManifest();
        return manifest.getFiles();
    }

    @Override
    public @NotNull String getFile(String fileCrowdinPath, String crowdinLocale) throws Exception {
        return getFile(fileCrowdinPath, crowdinLocale, false);
    }

    @Override
    @NotNull
    public String getFile(String fileCrowdinPath, String crowdinLocale, boolean forceFlush) throws Exception {
        Manifest manifest = getManifest();
        // Validate
        if (!manifest.getFiles().contains(fileCrowdinPath)) {
            throw new IllegalArgumentException("The file " + fileCrowdinPath + " not exists on Crowdin");
        }
        //Local stub
        if (manifest.isLocal()) {
            return "{}";
        }
        // Post path (replaced with locale code)
        String postProcessingPath = fileCrowdinPath.replace("%locale%", crowdinLocale);

        // Validating the manifest
        long manifestTimestamp = getManifest().getTimestamp();
        if (otaCacheControl.readManifestTimestamp() == getManifest().getTimestamp() && !forceFlush) {
            // Use cache
            try {
                // Check cache outdated
                if (!otaCacheControl.isCachedObjectOutdated(postProcessingPath, manifestTimestamp)) {
                    // Return the caches
                    Util.debugLog("Use local cache for " + postProcessingPath);
                    return new String(otaCacheControl.readObjectCache(postProcessingPath), StandardCharsets.UTF_8);
                } else {
                    Util.debugLog("Local cache outdated for " + postProcessingPath);
                    Util.debugLog("Excepted " + otaCacheControl.readCachedObjectTimestamp(postProcessingPath) + " actual: " + manifestTimestamp);
                }
            } catch (Exception exception) {
                MsgUtil.debugStackTrace(exception.getStackTrace());
            }
        } else {
            Util.debugLog("Manifest timestamp check failed " + postProcessingPath + " excepted:" + otaCacheControl.readManifestTimestamp() + " actual: " + getManifest().getTimestamp() + " forceUpdate: " + forceFlush);
        }
        // Out of the cache
        String url = CROWDIN_OTA_HOST + "content" + fileCrowdinPath.replace("%locale%", crowdinLocale);
        url = UrlEncoderDecoder.encodeToLegalPath(url);
        plugin.getLogger().info("Updating translation " + crowdinLocale + " from: " + url);
        HttpResponse<String> response = Unirest.get(url).asString();
        if (!response.isSuccess()) {
            throw new OTAException("Failed to grab data: " + response.getStatus() + "/" + response.getStatusText());
        }
        String data = response.getBody();
        // Successfully grab the data from the remote server
        otaCacheControl.writeObjectCache(postProcessingPath, data.getBytes(StandardCharsets.UTF_8), manifestTimestamp);
        otaCacheControl.writeManifestTimestamp(getManifest().getTimestamp());
        return data;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class OTAException extends RuntimeException {
        public OTAException(String message) {
            super(message);
        }
    }

    @AllArgsConstructor
    @Builder
    @Data
    public static class CrowdinGetFileRequest {
        private String fileCrowdinPath;
        private String crowdinLocale;
        private boolean forceFlush;
    }

    public static class OTACacheControl {
        private final File metadataFile = new File(Util.getCacheFolder(), "i18n.metadata");
        private final YamlConfiguration metadata;
        private final ReentrantLock LOCK = new ReentrantLock();

        public OTACacheControl() {
            this.metadata = YamlConfiguration.loadConfiguration(this.metadataFile);
        }

        private void save() {
            LOCK.lock();
            try {
                this.metadata.save(this.metadataFile);
            } catch (Exception exception) {
                exception.printStackTrace();
            } finally {
                LOCK.unlock();
            }
        }

        private String hash(String str) {
            return DigestUtils.sha1Hex(str);
        }

        public long readManifestTimestamp() {
            long l;
            LOCK.lock();
            try {
                l = this.metadata.getLong("manifest.timestamp", -1);
            } finally {
                LOCK.unlock();
            }
            return l;
        }

        public void writeManifestTimestamp(long timestamp) {
            LOCK.lock();
            try {
                this.metadata.set("manifest.timestamp", timestamp);
            } finally {
                LOCK.unlock();
            }

            save();
        }

        public long readCachedObjectTimestamp(String path) {
            String cacheKey = hash(path);
            long l;
            LOCK.lock();
            try {
                l = this.metadata.getLong("objects." + cacheKey + ".time", -1);
            } finally {
                LOCK.unlock();
            }
            return l;
        }

        public boolean isCachedObjectOutdated(String path, long manifestTimestamp) {
            return readCachedObjectTimestamp(path) != manifestTimestamp;
        }

        public byte[] readObjectCache(String path) throws IOException {
            String cacheKey = hash(path);
            return Files.readAllBytes(new File(Util.getCacheFolder(), cacheKey).toPath());
        }

        public void writeObjectCache(String path, byte[] data, long manifestTimestamp) throws IOException {
            String cacheKey = hash(path);
            Files.write(new File(Util.getCacheFolder(), cacheKey).toPath(), data);
            LOCK.lock();
            try {
                this.metadata.set("objects." + cacheKey + ".time", manifestTimestamp);
            } finally {
                LOCK.unlock();
                save();
            }

        }


    }
}