package com.ghostchu.quickshop.util.updater;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.paste.item.SubPasteItem;
import com.ghostchu.quickshop.util.paste.util.HTMLTable;
import com.vdurmont.semver4j.Semver;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.UnirestParsingException;
import lombok.Data;
import org.bukkit.Bukkit;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.SAXException;

import java.io.StringReader;
import java.util.Optional;
import java.util.logging.Level;

public class NexusManager implements SubPasteItem {

  private static final String NEXUS_ROOT_METADATA_URL = "https://repo.codemc.io/repository/maven-releases/com/ghostchu/quickshop-hikari/maven-metadata.xml";
  private final QuickShop plugin;
  private NexusMetadata cachedMetadata;

  private boolean cachedResult = true;
  private long lastCheck = 0;

  public NexusManager(final QuickShop plugin) {

    this.plugin = plugin;
    plugin.getPasteManager().register(plugin.getJavaPlugin(), this);
  }

  @NotNull
  public String getLatestVersion() {

    if(!plugin.getConfig().getBoolean("updater", false) || cachedMetadata == null) {
      return plugin.getVersion();
    }
    return cachedMetadata.getReleaseVersion();
  }

  public boolean isLatest() {

    if(Bukkit.isPrimaryThread()) {
      Log.debug(Level.WARNING, "Warning: isLatest shouldn't be called on PrimaryThread", Log.Caller.create());
      return cachedResult;
    }
    if(!plugin.getConfig().getBoolean("updater", false)) {
      cachedResult = true;
      return true;
    }
    updateCacheIfRequired();
    if(cachedMetadata == null) {
      cachedResult = true;
      return true;
    }
    final Semver localVer = plugin.getSemVersion();
    final Semver remoteReleaseVer = new Semver(cachedMetadata.getReleaseVersion());
    final Semver remoteLatestVer = new Semver(cachedMetadata.getLatestVersion());
    final Semver selectedRemoteVer;
    if(remoteReleaseVer.isGreaterThan(remoteLatestVer)) {
      selectedRemoteVer = remoteReleaseVer;
    } else {
      selectedRemoteVer = remoteLatestVer;
    }
    this.cachedResult = !selectedRemoteVer.isGreaterThan(localVer);
    return this.cachedResult;
  }

  private void updateCacheIfRequired() {

    if((lastCheck + 1000 * 60 * 60) < System.currentTimeMillis()) {
      lastCheck = System.currentTimeMillis();
      this.cachedMetadata = fetchMetadata();
    }
  }

  @Nullable
  private NexusMetadata fetchMetadata() {

    try {
      final HttpResponse<String> resp = Unirest.get(NEXUS_ROOT_METADATA_URL).asString();
      if(!resp.isSuccess()) {
        final Optional<UnirestParsingException> exceptionOptional = resp.getParsingError();
        if(exceptionOptional.isPresent()) {
          Log.debug("Failed to fetch metadata from Nexus: " + exceptionOptional.get().getMessage());
        } else {
          Log.debug("Failed to fetch metadata from CodeMC.io nexus:" + resp.getStatus() + "-" + resp.getStatusText());
        }
        return null;
      }
      return NexusMetadata.parse(resp.getBody());
    } catch(UnirestException e) {
      Log.debug("Failed to fetch version metadata from CodeMC.io Nexus: " + e.getMessage());
      return null;
    } catch(Exception e) {
      Log.debug("Failed to parse metadata from Nexus: " + e.getMessage());
      return null;
    }
  }

  @Override
  public @NotNull String genBody() {

    if(cachedMetadata == null) {
      return "<p>No metadata found.</p>";
    }
    final HTMLTable table = new HTMLTable(3);
    table.setTableTitle("Last Update", "Latest Version", "Release Version");
    table.insert(cachedMetadata.getLastUpdate(), cachedMetadata.getLatestVersion(), cachedMetadata.getReleaseVersion());
    return table.render();
  }

  @Override
  public @NotNull String getTitle() {

    return "NexusManager (updater)";
  }

  @Data
  static class NexusMetadata {

    private long lastUpdate;
    private String latestVersion;
    private String releaseVersion;

    public NexusMetadata(final long lastUpdate, final String latestVersion, final String releaseVersion) {

      this.lastUpdate = lastUpdate;
      this.latestVersion = latestVersion;
      this.releaseVersion = releaseVersion;
    }

    @NotNull
    public static NexusMetadata parse(@NotNull final String xml) throws DocumentException, IllegalStateException, SAXException {

      final SAXReader reader = new SAXReader();
      reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      final Document document = reader.read(new StringReader(xml));
      final Element metadataElement = document.getRootElement();
      if(metadataElement == null) {
        throw new IllegalStateException("No root element found");
      }
      final Element versioning = metadataElement.element("versioning");
      if(versioning == null) {
        throw new IllegalStateException("No versioning element found");
      }
      final Element latest = versioning.element("latest");
      if(latest == null) {
        throw new IllegalStateException("No latest element found");
      }
      final Element release = versioning.element("release");
      if(release == null) {
        throw new IllegalStateException("No release element found");
      }
      final Element lastUpdate = versioning.element("lastUpdated");
      if(lastUpdate == null) {
        throw new IllegalStateException("No lastUpdated element found");
      }
      final NexusMetadata metadata = new NexusMetadata(Long.parseLong(lastUpdate.getText()), latest.getText(), release.getText());
      Log.debug("Parsed NexusMetadata: " + metadata);
      return metadata;
    }
  }
}
