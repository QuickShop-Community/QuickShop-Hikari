package com.ghostchu.quickshop.util.updater;

/*
 * QuickShop-Hikari
 * Copyright (C) 2026 Daniel "creatorfromhell" Vidmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.ghostchu.quickshop.api.updater.UpdateMetadata;
import com.ghostchu.quickshop.api.updater.UpdateProvider;
import com.ghostchu.quickshop.util.logger.Log;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.UnirestParsingException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.SAXException;

import java.io.StringReader;
import java.util.Optional;

/**
 * NexuUpdateProvider
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class NexusUpdateProvider implements UpdateProvider {

  private static final String NEXUS_ROOT_METADATA_URL = "https://repo.codemc.io/repository/ghost-chu/com/ghostchu/quickshop-hikari/maven-metadata.xml";

  @Override
  public @NotNull String id() {
    return "nexus";
  }

  @Override
  public @NotNull String displayName() {
    return "CodeMC Nexus";
  }

  @Override
  public @NotNull String updateUrl() {
    return "https://modrinth.com/plugin/quickshop-hikari";
  }

  @Override
  public @Nullable UpdateMetadata fetchMetadata() {
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
      return parse(resp.getBody());
    } catch(final UnirestException e) {
      Log.debug("Failed to fetch version metadata from CodeMC.io Nexus: " + e.getMessage());
      return null;
    } catch(final Exception e) {
      Log.debug("Failed to parse metadata from Nexus: " + e.getMessage());
      return null;
    }
  }

  @NotNull
  private static UpdateMetadata parse(@NotNull final String xml)
          throws DocumentException, IllegalStateException, SAXException {

    final SAXReader reader = new SAXReader();
    reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

    final Document document = reader.read(new StringReader(xml));
    final Element metadataElement = document.getRootElement();
    if(metadataElement == null) throw new IllegalStateException("No root element found");

    final Element versioning = metadataElement.element("versioning");
    if(versioning == null) throw new IllegalStateException("No versioning element found");

    final Element latest = versioning.element("latest");
    if(latest == null) throw new IllegalStateException("No latest element found");

    final Element release = versioning.element("release");
    if(release == null) throw new IllegalStateException("No release element found");

    final Element lastUpdate = versioning.element("lastUpdated");
    if(lastUpdate == null) throw new IllegalStateException("No lastUpdated element found");

    return new UpdateMetadata(
            Long.parseLong(lastUpdate.getText()),
            latest.getText(),
            release.getText()
    );
  }
}