/*
 *  This file is a part of project QuickShop, the name is JenkinsUpdater.java
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

package com.ghostchu.quickshop.util.updater.impl;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.ghostchu.quickshop.BuildInfo;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.nonquickshopstuff.com.sk89q.worldedit.util.net.HttpRequest;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.updater.QuickUpdater;
import com.ghostchu.quickshop.util.updater.VersionType;

import java.io.*;
import java.net.URL;
import java.util.UUID;

public class JenkinsUpdater implements QuickUpdater {
    private final BuildInfo pluginBuildInfo;
    private final String jobUrl;
    private BuildInfo lastRemoteBuildInfo;
    private File updatedJar;

    public JenkinsUpdater(BuildInfo pluginBuildInfo) {
        this.pluginBuildInfo = pluginBuildInfo;
        this.jobUrl = pluginBuildInfo.getCiInfo().getProjectUrl();
    }

    @Override
    public @NotNull VersionType getCurrentRunning() {
        return switch (pluginBuildInfo.getGitInfo().getBranch()) {
            case "origin/release" -> VersionType.STABLE;
            case "origin/rc" -> VersionType.RC;
            case "origin/beta" -> VersionType.BETA;
            case "origin/lts" -> VersionType.LTS;
            default -> VersionType.SNAPSHOT;
        };
    }

    @Override
    public int getRemoteServerBuildId() {
        if (this.lastRemoteBuildInfo == null) {
            isLatest(getCurrentRunning());
        }
        if (this.lastRemoteBuildInfo == null) {
            return -1;
        }
        return this.lastRemoteBuildInfo.getCiInfo().getId();
    }

    @Override
    public @NotNull String getRemoteServerVersion() {
        if (this.lastRemoteBuildInfo == null) {
            isLatest(getCurrentRunning());
        }
        if (this.lastRemoteBuildInfo == null) {
            return "Error";
        }
        return this.lastRemoteBuildInfo.getGitInfo().getBuildVersion();
    }

    @Override
    public boolean isLatest(@NotNull VersionType versionType) {
        if (QuickShop.getInstance().getGameVersion().isEndOfLife()) { // EOL server won't receive future updates
            return true;
        }
        //Not checking updates for incorrect type
        if (versionType != getCurrentRunning()) {
            return true;
        }
        try (InputStream inputStream = HttpRequest.get(new URL(jobUrl + "lastSuccessfulBuild/artifact/quickshop-bukkit/target/extra-resources/BUILDINFO"))
                .header("User-Agent", "Java-QuickShop-" + QuickShop.getFork() + " " + QuickShop.getVersion())
                .execute()
                .expectResponseCode(200)
                .getInputStream()) {
            this.lastRemoteBuildInfo = new BuildInfo(inputStream);
            return lastRemoteBuildInfo.getCiInfo().getId() <= pluginBuildInfo.getCiInfo().getId() || lastRemoteBuildInfo.getGitInfo().getId().equalsIgnoreCase(pluginBuildInfo.getGitInfo().getId());
        } catch (IOException ioException) {
            MsgUtil.sendDirectMessage(Bukkit.getConsoleSender(), Component.text( "[QuickShop] Failed to check for an update on build server! It might be an internet issue or the build server host is down. If you want disable the update checker, you can disable in config.yml, but we still high-recommend check for updates on SpigotMC.org often, Error: " + ioException.getMessage()).color(NamedTextColor.RED));
            return true;
        }
    }

    @Override
    public byte[] update(@NotNull VersionType versionType) throws IOException {
        try (InputStream is = HttpRequest.get(new URL(jobUrl + "lastSuccessfulBuild/artifact/target/QuickShop.jar")).header("User-Agent", "QuickShop-" + QuickShop.getFork() + " " + QuickShop.getVersion()).execute().getInputStream(); ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            byte[] buff = new byte[1024];
            int len;
            long downloaded = 0;
            if (is == null) {
                throw new IOException("Failed downloading: Cannot open connection with remote server.");
            }
            while ((len = is.read(buff)) != -1) {
                os.write(buff, 0, len);
                downloaded += len;
                Util.debugLog("File Downloader: " + downloaded + " bytes.");
            }
            is.close();
            byte[] file = os.toByteArray();
            os.close();
            return file;
        }
    }

    @Override
    public void install(byte[] bytes) throws IOException {
        File pluginFolder = new File("plugins");
        if (!pluginFolder.exists()) {
            throw new IOException("Can't find the plugins folder.");
        }
        if (!pluginFolder.isDirectory()) {
            throw new IOException("Plugins not a folder.");
        }
        File[] plugins = pluginFolder.listFiles();
        if (plugins == null) {
            throw new IOException("Can't get the files in plugins folder");
        }
        File newJar = new File(pluginFolder, "QuickShop-Hikari-" + UUID.randomUUID().toString().replace("-", "") + ".jar");

        for (File pluginJar : plugins) {
            try { //Delete all old jar files
                PluginDescriptionFile desc = QuickShop.getInstance().getPluginLoader().getPluginDescription(pluginJar);
                if (!desc.getName().equals(QuickShop.getInstance().getDescription().getName())) {
                    continue;
                }
                Util.debugLog("Deleting: " + pluginJar.getPath());
                if (!pluginJar.delete()) {
                    Util.debugLog("Delete failed, using replacing method");
                    try (OutputStream outputStream = new FileOutputStream(pluginJar, false)) {
                        outputStream.write(bytes);
                        outputStream.flush();
                        updatedJar = pluginJar;
                    }
                } else {
                    try (OutputStream outputStream = new FileOutputStream(newJar, false)) {
                        outputStream.write(bytes);
                        outputStream.flush();
                        updatedJar = newJar;
                    }
                }
            } catch (InvalidDescriptionException ignored) {
            }
        }
    }

    @Override
    public @Nullable File getUpdatedJar() {
        return updatedJar;
    }
}
