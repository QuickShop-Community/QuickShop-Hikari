package com.ghostchu.quickshop.util.mojangapi;

import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.util.logger.Log;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class MojangAPI {

  private final MojangApiMirror mirror;

  public MojangAPI(final MojangApiMirror mirror) {

    this.mirror = mirror;
  }


  @NotNull
  public AssetsAPI getAssetsAPI(@NotNull final String serverVersion) {

    return new AssetsAPI(mirror, serverVersion);
  }

  @NotNull
  public GameInfoAPI getGameInfoAPI(@NotNull final String gameVersionJson) {

    return new GameInfoAPI(gameVersionJson);
  }

  @NotNull
  public MetaAPI getMetaAPI(@NotNull final String serverVersion) {

    return new MetaAPI(mirror, serverVersion);
  }

  public ResourcesAPI getResourcesAPI() {

    return new ResourcesAPI(mirror);
  }


  public static class AssetsFileData {

    private String content;
    private String sha1;
    private String id;

    public AssetsFileData(final String content, final String sha1, final String id) {

      this.content = content;
      this.sha1 = sha1;
      this.id = id;
    }

    public String getContent() {

      return this.content;
  }

    public String getSha1() {

      return this.sha1;
    }

    public String getId() {

      return this.id;
    }

    public void setContent(final String content) {

      this.content = content;
    }

    public void setSha1(final String sha1) {

      this.sha1 = sha1;
    }

    public void setId(final String id) {

      this.id = id;
    }

    @Override
    public boolean equals(final Object o) {

      if(o == this) return true;
      if(!(o instanceof MojangAPI.AssetsFileData)) return false;
      final MojangAPI.AssetsFileData other = (MojangAPI.AssetsFileData)o;
      return Objects.equals(this.getContent(), other.getContent())
             && Objects.equals(this.getSha1(), other.getSha1())
             && Objects.equals(this.getId(), other.getId());
    }

    @Override
    public int hashCode() {

      return Objects.hash(this.getContent(), this.getSha1(), this.getId());
    }

    @Override
    public String toString() {

      return "MojangAPI.AssetsFileData(content=" + this.getContent() + ", sha1=" + this.getSha1() + ", id=" + this.getId() + ")";
    }
  }

  public static class ResourcesAPI {

    protected final Cache<String, String> requestCachePool = CacheBuilder.newBuilder()
            .expireAfterWrite(7, TimeUnit.DAYS)
            .build();
    private final MojangApiMirror apiMirror;

    public ResourcesAPI(final MojangApiMirror mirror) {

      this.apiMirror = mirror;
    }

    public Optional<String> get(@NotNull final String hash) {

      final String url = apiMirror.getResourcesDownloadRoot() + "/" + hash.substring(0, 2) + "/" + hash;
      final HttpResponse<String> response = Unirest.get(url).asString();
      if(!response.isSuccess()) {
        return Optional.empty();
      }
      return Optional.ofNullable(response.getBody());
    }

    public Cache<String, String> getRequestCachePool() {

      return this.requestCachePool;
    }

    public MojangApiMirror getApiMirror() {

      return this.apiMirror;
    }

    @Override
    public boolean equals(final Object o) {

      if(o == this) return true;
      if(!(o instanceof MojangAPI.ResourcesAPI)) return false;
      final MojangAPI.ResourcesAPI other = (MojangAPI.ResourcesAPI)o;
      return Objects.equals(this.getRequestCachePool(), other.getRequestCachePool())
             && Objects.equals(this.getApiMirror(), other.getApiMirror());
    }

    @Override
    public int hashCode() {

      return Objects.hash(this.getRequestCachePool(), this.getApiMirror());
    }

    @Override
    public String toString() {

      return "MojangAPI.ResourcesAPI(requestCachePool=" + this.getRequestCachePool() + ", apiMirror=" + this.getApiMirror() + ")";
    }
  }


  public static class AssetsAPI {

    private final MetaAPI metaAPI;

    AssetsAPI(@NotNull final MojangApiMirror apiMirror, @NotNull final String version) {

      this.metaAPI = new MetaAPI(apiMirror, version);
    }

    /**
     * Gets the GameAsset file content
     *
     * @return The file content
     */
    public Optional<AssetsFileData> getGameAssetsFile() {

      final Optional<GameInfoAPI.DataBean> bean = getAssetsJson();
      if(bean.isEmpty()) {
        return Optional.empty();
      }
      final GameInfoAPI.DataBean.AssetIndexBean assetIndexBean = bean.get().getAssetIndex();
      if(assetIndexBean == null || assetIndexBean.getUrl() == null || assetIndexBean.getId() == null) {
        return Optional.empty();
      }
      final String data = Unirest.get(assetIndexBean.getUrl()).asString().getBody();
      return Optional.of(new AssetsFileData(data, assetIndexBean.getSha1(), assetIndexBean.getId()));
    }

    private Optional<GameInfoAPI.DataBean> getAssetsJson() {

      if(!isAvailable()) {
        return Optional.empty();
      }
      final Optional<String> content = this.metaAPI.get();
      if(content.isEmpty()) {
        return Optional.empty();
      }
      final GameInfoAPI gameInfoAPI = new GameInfoAPI(content.get());
      return Optional.of(gameInfoAPI.get());
    }

    public boolean isAvailable() {

      return this.metaAPI.get().isPresent();
    }

  }

  public static class GameInfoAPI {

    protected final Cache<String, String> requestCachePool = CacheBuilder.newBuilder()
            .expireAfterWrite(7, TimeUnit.DAYS)
            .build();
    private final String json;
    private final Gson gson = JsonUtil.getGson();

    public GameInfoAPI(@NotNull final String json) {

      this.json = json;
    }

    @NotNull
    public DataBean get() {

      return gson.fromJson(json, DataBean.class);
    }

    static class DataBean {

      @Nullable
      private AssetIndexBean assetIndex;
      @Nullable
      private String assets;

      public static class AssetIndexBean {

        /**
         * id : 1.16 sha1 : 3a5d110a6ab102c7083bae4296d2de4b8fcf92eb size : 295421 totalSize :
         * 330604420 url : <a
         * href="https://launchermeta.mojang.com/v1/packages/3a5d110a6ab102c7083bae4296d2de4b8fcf92eb/1.16.json">https://launchermeta.mojang.com/v1/packages/3a5d110a6ab102c7083bae4296d2de4b8fcf92eb/1.16.json</a>
         */
        @Nullable
        private String id;
        @Nullable
        private String sha1;
        @Nullable
        private String url;

        public AssetIndexBean() {

    }

        /**
         * id : 1.16 sha1 : 3a5d110a6ab102c7083bae4296d2de4b8fcf92eb size : 295421 totalSize :
         * 330604420 url : <a
         * href="https://launchermeta.mojang.com/v1/packages/3a5d110a6ab102c7083bae4296d2de4b8fcf92eb/1.16.json">https://launchermeta.mojang.com/v1/packages/3a5d110a6ab102c7083bae4296d2de4b8fcf92eb/1.16.json</a>
         */
        @Nullable
        public String getId() {

          return this.id;
        }

        @Nullable
        public String getSha1() {

          return this.sha1;
        }

        @Nullable
        public String getUrl() {

          return this.url;
        }

        /**
         * id : 1.16 sha1 : 3a5d110a6ab102c7083bae4296d2de4b8fcf92eb size : 295421 totalSize :
         * 330604420 url : <a
         * href="https://launchermeta.mojang.com/v1/packages/3a5d110a6ab102c7083bae4296d2de4b8fcf92eb/1.16.json">https://launchermeta.mojang.com/v1/packages/3a5d110a6ab102c7083bae4296d2de4b8fcf92eb/1.16.json</a>
         */
        public void setId(@Nullable final String id) {

          this.id = id;
        }

        public void setSha1(@Nullable final String sha1) {

          this.sha1 = sha1;
        }

        public void setUrl(@Nullable final String url) {

          this.url = url;
        }

        @Override
        public boolean equals(final Object o) {

          if(o == this) return true;
          if(!(o instanceof MojangAPI.GameInfoAPI.DataBean.AssetIndexBean)) return false;
          final MojangAPI.GameInfoAPI.DataBean.AssetIndexBean other = (MojangAPI.GameInfoAPI.DataBean.AssetIndexBean)o;
          return Objects.equals(this.getId(), other.getId())
                 && Objects.equals(this.getSha1(), other.getSha1())
                 && Objects.equals(this.getUrl(), other.getUrl());
        }

        @Override
        public int hashCode() {

          return Objects.hash(this.getId(), this.getSha1(), this.getUrl());
        }

        @Override
        public String toString() {

          return "MojangAPI.GameInfoAPI.DataBean.AssetIndexBean(id=" + this.getId() + ", sha1=" + this.getSha1() + ", url=" + this.getUrl() + ")";
        }
      }

      public DataBean() {

      }

      @Nullable
      public AssetIndexBean getAssetIndex() {

        return this.assetIndex;
      }

      @Nullable
      public String getAssets() {

        return this.assets;
      }

      public void setAssetIndex(@Nullable final AssetIndexBean assetIndex) {

        this.assetIndex = assetIndex;
      }

      public void setAssets(@Nullable final String assets) {

        this.assets = assets;
      }

      @Override
      public boolean equals(final Object o) {

        if(o == this) return true;
        if(!(o instanceof MojangAPI.GameInfoAPI.DataBean)) return false;
        final MojangAPI.GameInfoAPI.DataBean other = (MojangAPI.GameInfoAPI.DataBean)o;
        return Objects.equals(this.getAssetIndex(), other.getAssetIndex())
               && Objects.equals(this.getAssets(), other.getAssets());
      }

      @Override
      public int hashCode() {

        return Objects.hash(this.getAssetIndex(), this.getAssets());
      }

      @Override
      public String toString() {

        return "MojangAPI.GameInfoAPI.DataBean(assetIndex=" + this.getAssetIndex() + ", assets=" + this.getAssets() + ")";
      }
    }

    public Cache<String, String> getRequestCachePool() {

      return this.requestCachePool;
    }

    public String getJson() {

      return this.json;
    }

    public Gson getGson() {

      return this.gson;
    }

    @Override
    public boolean equals(final Object o) {

      if(o == this) return true;
      if(!(o instanceof MojangAPI.GameInfoAPI)) return false;
      final MojangAPI.GameInfoAPI other = (MojangAPI.GameInfoAPI)o;
      return Objects.equals(this.getRequestCachePool(), other.getRequestCachePool())
             && Objects.equals(this.getJson(), other.getJson())
             && Objects.equals(this.getGson(), other.getGson());
    }

    @Override
    public int hashCode() {

      return Objects.hash(this.getRequestCachePool(), this.getJson(), this.getGson());
    }

    @Override
    public String toString() {

      return "MojangAPI.GameInfoAPI(requestCachePool=" + this.getRequestCachePool() + ", json=" + this.getJson() + ", gson=" + this.getGson() + ")";
    }
  }

  public static class MetaAPI {

    private final String metaEndpoint;
    private final String version;

    public MetaAPI(@NotNull final MojangApiMirror mirror, @NotNull final String version) {

      this.version = version;
      this.metaEndpoint = mirror.getLauncherMetaRoot() + "/mc/game/version_manifest.json";
    }

    /**
     * Gets the available status and the Game Version Meta Json File content.
     *
     * @return The meta data
     */
    public Optional<String> get() {

      final HttpResponse<String> response = Unirest.get(metaEndpoint).asString();
      if(!response.isSuccess()) {
        Log.debug("Request Meta Endpoint failed.");
        return Optional.empty();
      }
      final String result = response.getBody();
      try {
        final JsonElement index = JsonParser.parseString(result);
        if(!index.isJsonObject()) {
          return Optional.empty();
        }
        final JsonElement availableVersions = index.getAsJsonObject().get("versions");
        if(!availableVersions.isJsonArray()) {
          return Optional.empty();
        }
        for(final JsonElement gameVersionData : availableVersions.getAsJsonArray()) {
          if(gameVersionData.isJsonObject()) {
            final JsonElement gameId = gameVersionData.getAsJsonObject().get("id");
            final JsonElement gameIndexUrl = gameVersionData.getAsJsonObject().get("url");
            if(Objects.equals(gameId.getAsString(), version)) {
              final HttpResponse<String> response1 = Unirest.get(gameIndexUrl.getAsString()).asString();
              if(!response1.isSuccess()) {
                return Optional.empty();
              }
              return Optional.ofNullable(response1.getBody());
            }
          }
        }
        return Optional.empty();
      } catch(RuntimeException exception) {
        return Optional.empty();
      }
    }
  }

}
