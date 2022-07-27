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
    protected static String CROWDIN_OTA_HOST = "https://distributions.crowdin.net/91b97508fdf19626f2977b7xrm4/";
    private final QuickShop plugin;
    private final OTACacheControl otaCacheControl = new OTACacheControl();
    private Manifest manifest;
    private List<String> availableLanguages;

    public CrowdinOTA(QuickShop plugin) throws IOException, JsonSyntaxException {
        String configDefine = plugin.getConfig().getString("custom-crowdin-ota-host");
        if(configDefine != null)
            CROWDIN_OTA_HOST = configDefine;
        Util.SysPropertiesParseResult parseResult = Util.parsePackageProperly("custom-crowdin-ota-host");
        if(parseResult.isPresent()){
            CROWDIN_OTA_HOST = parseResult.asString("https://distributions.crowdin.net/91b97508fdf19626f2977b7xrm4/");
        }
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
        }else{
            if(response.getStatus() == 400 || response.getStatus() == 404){
                plugin.getLogger().warning("Failed to initialize QuickShop i18n files, contact the developer to get support.");
            }
        }
        return null;
    }

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