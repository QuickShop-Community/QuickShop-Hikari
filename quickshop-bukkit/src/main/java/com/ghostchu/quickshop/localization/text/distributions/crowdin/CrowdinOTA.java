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
import com.ghostchu.quickshop.util.logger.Log;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrowdinOTA implements Distribution {
    //DO NOT final it! Unit-test needs to change it to prevent network flow
    protected static final String CROWDIN_OTA_HOST = "https://distributions.crowdin.net/17c22941a7edcba09821517xrm4/";
    private final QuickShop plugin;
    private final OTACacheControl otaCacheControl = new OTACacheControl();
    private Manifest manifest;
    private List<String> availableLanguages;

    public CrowdinOTA(QuickShop plugin) throws IOException, JsonSyntaxException {
        this.plugin = plugin;
        Util.getCacheFolder().mkdirs();
        init();
    }

    /**
     * Initialize Crowdin OTA module
     */
    public void init() throws IOException, JsonSyntaxException {
        plugin.getLogger().info("[CrowdinOTA] Initializing...");
        this.initManifest();
        this.initAvailableLanguages(this.manifest);

    }

    @Override
    public @NotNull List<String> getAvailableLanguages() {
        return this.availableLanguages;
    }

    @Override
    public @NotNull List<String> getAvailableFiles() {
        return this.manifest.getFiles();
    }

    @Override
    public @NotNull String getFile(String fileCrowdinPath, String crowdinLocale) throws Exception {
        return getFile(fileCrowdinPath, crowdinLocale, false);
    }

    @Override
    @NotNull
    public String getFile(String fileCrowdinPath, String crowdinLocale, boolean forceFlush) throws Exception {
        Manifest manifest = getManifest();
        if (manifest == null) {
            throw new IllegalStateException("Manifest didn't get loaded successfully yet!");
        }
        // Validate
        if (!getAvailableFiles().contains(fileCrowdinPath)) {
            throw new IllegalArgumentException("The file " + fileCrowdinPath.replace("%locale%", crowdinLocale) + " not exists on Crowdin");
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
                    Log.debug("Use local cache for " + postProcessingPath);
                    return new String(otaCacheControl.readObjectCache(postProcessingPath), StandardCharsets.UTF_8);
                } else {
                    Log.debug("Local cache outdated for " + postProcessingPath);
                    Log.debug("Excepted " + otaCacheControl.readCachedObjectTimestamp(postProcessingPath) + " actual: " + manifestTimestamp);
                }
            } catch (Exception exception) {
                MsgUtil.debugStackTrace(exception.getStackTrace());
            }
        } else {
            Log.debug("Manifest timestamp check failed " + postProcessingPath + " excepted:" + otaCacheControl.readManifestTimestamp() + " actual: " + getManifest().getTimestamp() + " forceUpdate: " + forceFlush);
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


    /**
     * Getting the Crowdin distribution manifest
     *
     * @return The distribution manifest, return null if failed to generate
     */

    @Nullable
    public Manifest getManifest() {
        return JsonUtil.regular().fromJson(getManifestJson(), Manifest.class);
    }

    /**
     * Getting the Crowdin distribution manifest json
     *
     * @return The distribution manifest json, return null if failed.
     */
    @Nullable
    public String getManifestJson() {
        String url = CROWDIN_OTA_HOST + "manifest.json";
        // TODO: This is hacky
        HttpResponse<String> response = Unirest.get(url).asString();
        if (response.isSuccess()) {
            return response.getBody();
        }
        return null;
    }

//
//    @Override
//    @NotNull
//    public List<String> getAvailableFiles() {
//        Manifest manifest = getManifest();
//        return manifest.getFiles();
//    }
//
//    @Override
//    public @NotNull String getFile(String fileCrowdinPath, String crowdinLocale) throws Exception {
//        return getFile(fileCrowdinPath, crowdinLocale, false);
//    }
//
//    @Override
//    @NotNull
//    public String getFile(String fileCrowdinPath, String crowdinLocale, boolean forceFlush) throws Exception {
//        Manifest manifest = getManifest();
//        // Validate
//        if (!manifest.getFiles().contains(fileCrowdinPath)) {
//            throw new IllegalArgumentException("The file " + fileCrowdinPath + " not exists on Crowdin");
//        }
//        //Local stub
//        if (manifest.isLocal()) {
//            return "{}";
//        }
//        // Post path (replaced with locale code)
//        String postProcessingPath = fileCrowdinPath.replace("%locale%", crowdinLocale);
//
//        // Validating the manifest
//        long manifestTimestamp = getManifest().getTimestamp();
//        if (otaCacheControl.readManifestTimestamp() == getManifest().getTimestamp() && !forceFlush) {
//            // Use cache
//            try {
//                // Check cache outdated
//                if (!otaCacheControl.isCachedObjectOutdated(postProcessingPath, manifestTimestamp)) {
//                    // Return the caches
//                    Log.debug("Use local cache for " + postProcessingPath);
//                    return new String(otaCacheControl.readObjectCache(postProcessingPath), StandardCharsets.UTF_8);
//                } else {
//                    Log.debug("Local cache outdated for " + postProcessingPath);
//                    Log.debug("Excepted " + otaCacheControl.readCachedObjectTimestamp(postProcessingPath) + " actual: " + manifestTimestamp);
//                }
//            } catch (Exception exception) {
//                MsgUtil.debugStackTrace(exception.getStackTrace());
//            }
//        } else {
//            Log.debug("Manifest timestamp check failed " + postProcessingPath + " excepted:" + otaCacheControl.readManifestTimestamp() + " actual: " + getManifest().getTimestamp() + " forceUpdate: " + forceFlush);
//        }
//        // Out of the cache
//        String url = CROWDIN_OTA_HOST + "content" + fileCrowdinPath.replace("%locale%", crowdinLocale);
//        url = UrlEncoderDecoder.encodeToLegalPath(url);
//        plugin.getLogger().info("Updating translation " + crowdinLocale + " from: " + url);
//        HttpResponse<String> response = Unirest.get(url).asString();
//        if (!response.isSuccess()) {
//            throw new OTAException("Failed to grab data: " + response.getStatus() + "/" + response.getStatusText());
//        }
//        String data = response.getBody();
//        // Successfully grab the data from the remote server
//        otaCacheControl.writeObjectCache(postProcessingPath, data.getBytes(StandardCharsets.UTF_8), manifestTimestamp);
//        otaCacheControl.writeManifestTimestamp(getManifest().getTimestamp());
//        return data;
//    }

    private void initAvailableLanguages(@NotNull Manifest manifest) {
        plugin.getLogger().info("[CrowdinOTA] Initializing available languages...");
        List<String> languages = new ArrayList<>();
        Map<String, String> mapping = genLanguageMapping();
        for (String language : manifest.getLanguages()) {
            languages.add(mapping.getOrDefault(language, language));
        }
        plugin.getLogger().info("[CrowdinOTA] Available languages: " + Util.list2String(languages));
        this.availableLanguages = languages;
    }

    @Nullable
    private Map<String, String> genLanguageMapping() {
        Map<String, String> mapping = new HashMap<>();
        String json = getManifestJson();
        if (json == null)
            return null;
        JsonElement parser = JsonParser.parseString(json);
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

    private void initManifest() throws IOException, JsonSyntaxException {
        plugin.getLogger().info("[CrowdinOTA] Downloading manifest...");
        String url = CROWDIN_OTA_HOST + "manifest.json";
        HttpResponse<String> response = Unirest.get(url).asString();
        if (!response.isSuccess()) {
            throw new IOException("Failed to get Crowdin OTA manifest: " + response.getStatus());
        }
        this.manifest = JsonUtil.regular().fromJson(response.getBody(), Manifest.class);
    }


    @AllArgsConstructor
    @Builder
    @Data
    public static class CrowdinGetFileRequest {
        private String fileCrowdinPath;
        private String crowdinLocale;
        private boolean forceFlush;
    }

}