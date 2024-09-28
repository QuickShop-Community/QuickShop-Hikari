package com.ghostchu.quickshop;

import com.ghostchu.quickshop.common.util.JsonUtil;
import com.google.common.hash.Hashing;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NbtApiInitializer {

  public NbtApiInitializer(Logger logger) {

    if(checkNeedDownloadNbtApi()) {
      try {
        init(logger);
      } catch(Throwable e) {
        logger.log(Level.WARNING, "Failed to download/install the NBT-API, please download and install it by manual. https://modrinth.com/plugin/nbtapi", e);
        logger.warning("Automatic installation failed, abort.");
      }
    }
  }

  private boolean checkNeedDownloadNbtApi() {

    return Bukkit.getPluginManager().getPlugin("NBTAPI") == null;
  }

  private void init(Logger logger) throws IOException, InvalidPluginException, InvalidDescriptionException {

    logger.info("QuickShop-Hikari needs NBT-API on Spigot platform, downloading it from Modrinth...");
    HttpResponse<String> response = Unirest
            .get("https://api.modrinth.com/v2/project/nbtapi/version")
            .header("Content-Type", "application/json")
            .asString();
    if(!response.isSuccess()) {
      throw new IOException("Failed to download NBT-API from Modrinth: " + response.getStatus() + " - " + response.getStatusText() + ": " + response.getBody());
    }
    ResponseProject[] responseProjects;
    try {
      responseProjects = JsonUtil.standard().fromJson(response.getBody(), ResponseProject[].class);
    } catch(JsonSyntaxException e) {
      throw new IOException("Failed to parse NBT-API response: " + response.getBody(), e);
    }
    ResponseProject.FilesDTO dto = getFilesDTO(responseProjects);
    logger.info("Selected: " + dto);
    Path path = Files.createTempDirectory("quickshop-nbtapi-tmp");
    File nbtapiTempFile = path.toFile();
    if(!nbtapiTempFile.exists()) {
      nbtapiTempFile.mkdirs();
    }
    nbtapiTempFile = new File(nbtapiTempFile, dto.getFilename());

    logger.info("Downloading NBT-API from Modrinth...");
    HttpResponse<File> fileHttpResponse = Unirest.get(dto.getUrl()).asFile(nbtapiTempFile.getAbsolutePath());
    if(!fileHttpResponse.isSuccess()) {
      throw new IOException("Failed to download NBT-API from Modrinth: " + fileHttpResponse.getStatus() + " - " + fileHttpResponse.getStatusText() + ": " + fileHttpResponse.getBody());
    }
    logger.info("Checking hash...");

    String downloadedHash = Hashing.sha512().hashBytes(Files.readAllBytes(nbtapiTempFile.toPath())).toString();
    if(!downloadedHash.equalsIgnoreCase(dto.getHashes().getSha512())) {
      logger.warning("Excepted: " + dto.getHashes().getSha512());
      logger.warning("Actual: " + downloadedHash);
      throw new IOException("Failed to download NBT-API from Modrinth: Hash not matched.");
    }

    logger.info("Installing NBT-API...");

    File rootDirectory = new File("./");
    File pluginsDirectory = new File(rootDirectory, "plugins");
    if(!pluginsDirectory.exists()) {
      throw new IOException("Failed to install NBT-API: Plugins directory not found.");
    }
    File finalPluginFile = new File(pluginsDirectory, nbtapiTempFile.getName());
    Files.move(nbtapiTempFile.toPath(), finalPluginFile.toPath());

    logger.info("Loading NBT-API...");
    Bukkit.getPluginManager().loadPlugin(finalPluginFile);
  }

  @NotNull
  private ResponseProject.FilesDTO getFilesDTO(ResponseProject[] responseProjects) {

    if(responseProjects.length == 0) {
      throw new IllegalStateException("Failed to download NBT-API: No matched version found.");
    }
    ResponseProject project = responseProjects[0];
    ResponseProject.FilesDTO dto = null;
    for(ResponseProject.FilesDTO file : project.getFiles()) {
      if(!file.getPrimary()) {
        continue;
      }
      dto = file;
      break;
    }
    if(dto == null) {
      throw new IllegalStateException("Failed to download NBT-API: No matched file found.");
    }
    return dto;
  }

  @NoArgsConstructor
  @Data
  @ToString
  static class ResponseProject {

    @SerializedName("id")
    private String id;
    @SerializedName("project_id")
    private String projectId;
    @SerializedName("author_id")
    private String authorId;
    @SerializedName("featured")
    private Boolean featured;
    @SerializedName("name")
    private String name;
    @SerializedName("version_number")
    private String versionNumber;
    @SerializedName("changelog")
    private String changelog;
    @SerializedName("changelog_url")
    private Object changelogUrl;
    @SerializedName("date_published")
    private String datePublished;
    @SerializedName("downloads")
    private Integer downloads;
    @SerializedName("version_type")
    private String versionType;
    @SerializedName("status")
    private String status;
    @SerializedName("requested_status")
    private Object requestedStatus;
    @SerializedName("files")
    private List<FilesDTO> files;
    @SerializedName("dependencies")
    private List<?> dependencies;
    @SerializedName("game_versions")
    private List<String> gameVersions;
    @SerializedName("loaders")
    private List<String> loaders;

    @NoArgsConstructor
    @Data
    public static class FilesDTO {

      @SerializedName("hashes")
      private HashesDTO hashes;
      @SerializedName("url")
      private String url;
      @SerializedName("filename")
      private String filename;
      @SerializedName("primary")
      private Boolean primary;
      @SerializedName("size")
      private Integer size;
      @SerializedName("file_type")
      private Object fileType;

      @NoArgsConstructor
      @Data
      public static class HashesDTO {

        @SerializedName("sha512")
        private String sha512;
        @SerializedName("sha1")
        private String sha1;
      }
    }

  }
}
