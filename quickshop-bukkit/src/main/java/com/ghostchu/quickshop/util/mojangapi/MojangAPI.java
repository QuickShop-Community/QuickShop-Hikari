package com.ghostchu.quickshop.util.mojangapi;

import com.ghostchu.quickshop.util.JsonUtil;
import com.ghostchu.quickshop.util.logger.Log;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.Data;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class MojangAPI {
    private final MojangApiMirror mirror;

    public MojangAPI(MojangApiMirror mirror) {
        this.mirror = mirror;
    }


    @NotNull
    public AssetsAPI getAssetsAPI(@NotNull String serverVersion) {
        return new AssetsAPI(mirror, serverVersion);
    }

    @NotNull
    public GameInfoAPI getGameInfoAPI(@NotNull String gameVersionJson) {
        return new GameInfoAPI(gameVersionJson);
    }

    @NotNull
    public MetaAPI getMetaAPI(@NotNull String serverVersion) {
        return new MetaAPI(mirror, serverVersion);
    }

    public ResourcesAPI getResourcesAPI() {
        return new ResourcesAPI(mirror);
    }


    @Data
    public static class AssetsFileData {
        private String content;
        private String sha1;
        private String id;

        public AssetsFileData(String content, String sha1, String id) {
            this.content = content;
            this.sha1 = sha1;
            this.id = id;
        }
    }

    @Data
    public static class ResourcesAPI {
        protected final Cache<String, String> requestCachePool = CacheBuilder.newBuilder()
                .expireAfterWrite(7, TimeUnit.DAYS)
                .build();
        private final MojangApiMirror apiMirror;

        public ResourcesAPI(MojangApiMirror mirror) {
            this.apiMirror = mirror;
        }

        public Optional<String> get(@NotNull String hash) {
            String url = apiMirror.getResourcesDownloadRoot() + "/" + hash.substring(0, 2) + "/" + hash;
            HttpResponse<String> response = Unirest.get(url).asString();
            if (!response.isSuccess()) {
                return Optional.empty();
            }
            return Optional.ofNullable(response.getBody());
        }
    }


    public static class AssetsAPI {
        private final MetaAPI metaAPI;

        AssetsAPI(@NotNull MojangApiMirror apiMirror, @NotNull String version) {
            this.metaAPI = new MetaAPI(apiMirror, version);
        }

        public boolean isAvailable() {
            return this.metaAPI.get().isPresent();
        }

        /**
         * Gets the GameAsset file content
         *
         * @return The file content
         */
        public Optional<AssetsFileData> getGameAssetsFile() {
            Optional<GameInfoAPI.DataBean> bean = getAssetsJson();
            if (bean.isEmpty()) {
                return Optional.empty();
            }
            GameInfoAPI.DataBean.AssetIndexBean assetIndexBean = bean.get().getAssetIndex();
            if (assetIndexBean == null || assetIndexBean.getUrl() == null || assetIndexBean.getId() == null) {
                return Optional.empty();
            }
            String data = Unirest.get(assetIndexBean.getUrl()).asString().getBody();
            return Optional.of(new AssetsFileData(data, assetIndexBean.getSha1(), assetIndexBean.getId()));
        }


        private Optional<GameInfoAPI.DataBean> getAssetsJson() {
            if (!isAvailable()) {
                return Optional.empty();
            }
            Optional<String> content = this.metaAPI.get();
            if (content.isEmpty()) {
                return Optional.empty();
            }
            GameInfoAPI gameInfoAPI = new GameInfoAPI(content.get());
            return Optional.of(gameInfoAPI.get());
        }


    }

    @Data
    public static class GameInfoAPI {
        protected final Cache<String, String> requestCachePool = CacheBuilder.newBuilder()
                .expireAfterWrite(7, TimeUnit.DAYS)
                .build();
        private final String json;
        private final Gson gson = JsonUtil.getGson();

        public GameInfoAPI(@NotNull String json) {
            this.json = json;
        }

        @NotNull
        public DataBean get() {
            return gson.fromJson(json, DataBean.class);
        }

        @Data
        static class DataBean {
            @Nullable
            private AssetIndexBean assetIndex;
            @Nullable
            private String assets;

            @Data
            public static class AssetIndexBean {
                /**
                 * id : 1.16
                 * sha1 : 3a5d110a6ab102c7083bae4296d2de4b8fcf92eb
                 * size : 295421
                 * totalSize : 330604420
                 * url : <a href="https://launchermeta.mojang.com/v1/packages/3a5d110a6ab102c7083bae4296d2de4b8fcf92eb/1.16.json">https://launchermeta.mojang.com/v1/packages/3a5d110a6ab102c7083bae4296d2de4b8fcf92eb/1.16.json</a>
                 */
                @Nullable
                private String id;
                @Nullable
                private String sha1;
                @Nullable
                private String url;
            }

        }


    }

    public static class MetaAPI {
        private final String metaEndpoint;
        private final String version;

        public MetaAPI(@NotNull MojangApiMirror mirror, @NotNull String version) {
            this.version = version;
            this.metaEndpoint = mirror.getLauncherMetaRoot() + "/mc/game/version_manifest.json";
        }

        /**
         * Gets the available status and the Game Version Meta Json File content.
         *
         * @return The meta data
         */
        @SneakyThrows
        public Optional<String> get() {
            HttpResponse<String> response = Unirest.get(metaEndpoint).asString();
            if (!response.isSuccess()) {
                Log.debug("Request Meta Endpoint failed.");
                return Optional.empty();
            }
            String result = response.getBody();
            try {
                JsonElement index = JsonParser.parseString(result);
                if (!index.isJsonObject()) {
                    return Optional.empty();
                }
                JsonElement availableVersions = index.getAsJsonObject().get("versions");
                if (!availableVersions.isJsonArray()) {
                    return Optional.empty();
                }
                for (JsonElement gameVersionData : availableVersions.getAsJsonArray()) {
                    if (gameVersionData.isJsonObject()) {
                        JsonElement gameId = gameVersionData.getAsJsonObject().get("id");
                        JsonElement gameIndexUrl = gameVersionData.getAsJsonObject().get("url");
                        if (Objects.equals(gameId.getAsString(), version)) {
                            HttpResponse<String> response1 = Unirest.get(gameIndexUrl.getAsString()).asString();
                            if (!response1.isSuccess()) {
                                return Optional.empty();
                            }
                            return Optional.ofNullable(response1.getBody());
                        }
                    }
                }
                return Optional.empty();
            } catch (RuntimeException exception) {
                return Optional.empty();
            }
        }
    }


}
