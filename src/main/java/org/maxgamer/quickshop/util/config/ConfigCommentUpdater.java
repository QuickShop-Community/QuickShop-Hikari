/*
 * This file is a part of project QuickShop, the name is ConfigCommentUpdater.java
 *  Copyright (C) PotatoCraft Studio and contributors
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

package org.maxgamer.quickshop.util.config;

import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.holder.QuickShopInstanceHolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;

/**
 * A node based yaml comment updater
 *
 * @author sandtechnology
 */
public class ConfigCommentUpdater extends QuickShopInstanceHolder {
    private static final String HEADER = "HEAD-" + UUID.randomUUID();
    private static final String FOOTER = "END-" + UUID.randomUUID();
    private final InputStream builtInConfig;
    private final File externalConfig;

    public ConfigCommentUpdater(QuickShop plugin, InputStream builtInConfig, File externalConfig) {
        super(plugin);
        this.builtInConfig = builtInConfig;
        this.externalConfig = externalConfig;
    }

    public void updateComment() {
        boolean hasBackup = false;
        boolean hasWrite = false;
        Path backupFile = externalConfig.toPath().resolve(UUID.randomUUID() + "-bak.yml");
        try {
            Files.copy(externalConfig.toPath(), backupFile, StandardCopyOption.REPLACE_EXISTING);
            hasBackup = true;
            BufferedReader reader = new BufferedReader(new InputStreamReader(builtInConfig, StandardCharsets.UTF_8));
            List<String> builtInContent = new ArrayList<>();
            while (true) {
                String result = reader.readLine();
                if (result == null) {
                    break;
                }
                builtInContent.add(result);
            }

            PathMarker pathMarker = new PathMarker();
            Map<String, Set<String>> key2NonKeyContentMap = new LinkedHashMap<>();
            Set<String> pendingComment = new LinkedHashSet<>();
            for (String s : builtInContent) {
                String trimmedStr = s.trim();
                if (trimmedStr.isEmpty()) {
                    pendingComment.add("");
                    continue;
                }
                if (trimmedStr.startsWith("#")) {
                    pendingComment.add(s);
                } else
                    //First node is header, so just use current
                    if (pathMarker.parseRawInput(s)) {
                        String pathNow = pathMarker.getPath();
                        key2NonKeyContentMap.merge(pathNow, new LinkedHashSet<>(pendingComment), (oldList, newList) -> {
                            oldList.addAll(newList);
                            return oldList;
                        });
                        pendingComment.clear();
                    }
            }
            if (!pendingComment.isEmpty()) {
                key2NonKeyContentMap.put(FOOTER, pendingComment);
            }

            List<String> externalContent = Files.readAllLines(externalConfig.toPath());
            List<String> output = new ArrayList<>(externalContent.size());
            int offset = 0;
            for (int i = 0; i < externalContent.size(); i++) {
                String s = externalContent.get(i);
                if (s.isEmpty() || s.trim().startsWith("#")) {
                    pendingComment.add(s);
                    offset--;
                    continue;
                }
                output.add(s);
                if (pathMarker.parseRawInput(s)) {
                    String pathNow = pathMarker.getPath();
                    Set<String> comments = key2NonKeyContentMap.getOrDefault(pathNow, Collections.emptySet());
                    pendingComment.addAll(comments);
                    for (String comment : pendingComment) {
                        output.add(i + offset, comment);
                        offset++;
                    }
                    pendingComment.clear();
                }
            }

            output.addAll(key2NonKeyContentMap.getOrDefault(FOOTER, Collections.emptySet()));
            hasWrite = true;
            Files.write(externalConfig.toPath(), output, StandardCharsets.UTF_8);
        } catch (Throwable e) {
            if (hasBackup && hasWrite) {
                plugin.getLogger().log(Level.WARNING, "Failed to update comment for config.yml, rollback...", e);
                try {
                    Files.copy(backupFile, externalConfig.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (Throwable ex) {
                    plugin.getLogger().log(Level.WARNING, "Failed to rollback config.yml! Please rollback it manually by renaming " + backupFile.getFileName() + " to config.yml!", ex);
                }
            } else {
                plugin.getLogger().log(Level.WARNING, "Failed to update comment for config.yml, no changes taken.", e);
            }
        }
    }

    private static class PathMarker {
        private final List<String> path = new ArrayList<>(Collections.singletonList(HEADER));
        private int pathDepth = 0;

        public void addNode(String s) {
            path.add(s);
        }

        private int countSpace(String s) {
            int i = 0;
            for (char c : s.toCharArray()) {
                if (c == ' ') {
                    i++;
                } else {
                    break;
                }
            }
            return i;
        }

        public boolean parseRawInput(String str) {
            String trimmedStr = str.trim();
            if (trimmedStr.startsWith("#") || !(trimmedStr.contains(": ") || trimmedStr.endsWith(":"))) {
                return false;
            }
            int pathDepthNow = countSpace(str);
            String nodeStr = trimmedStr.split(":", 2)[0];
            if (!nodeStr.isEmpty()) {
                if (pathDepthNow > pathDepth) {
                    addNode(nodeStr);
                } else if (pathDepthNow < pathDepth) {
                    for (int j = 0; j < pathDepth - pathDepthNow; j++) {
                        removeNode();
                    }
                    addNode(nodeStr);
                } else {
                    replaceNode(nodeStr);
                }
                pathDepth = pathDepthNow;
                return true;
            } else {
                return false;
            }
        }

        public void replaceNode(String s) {
            if (!path.isEmpty()) {
                path.set(path.size() - 1, s);
            } else {
                path.add(s);
            }
        }

        public void reset() {
            path.clear();
        }

        public void removeNode() {
            if (!path.isEmpty()) {
                path.remove(path.size() - 1);
            }
        }


        public String getPath() {
            StringJoiner stringJoiner = new StringJoiner(".");
            for (String s : path) {
                stringJoiner.add(s);
            }
            return stringJoiner.toString();
        }
    }
}

