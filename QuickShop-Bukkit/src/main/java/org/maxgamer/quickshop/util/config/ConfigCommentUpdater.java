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
        final Set<String> EMPTY_SET = Collections.emptySet();
        //Make a backup first
        Path backupFile = externalConfig.toPath().getParent().resolve("config-" + UUID.randomUUID() + "-bak.yml");
        try {
            Files.copy(externalConfig.toPath(), backupFile, StandardCopyOption.REPLACE_EXISTING);
            hasBackup = true;
            //Read the built-in files
            BufferedReader reader = new BufferedReader(new InputStreamReader(builtInConfig, StandardCharsets.UTF_8));
            List<String> builtInContent = new ArrayList<>();
            while (true) {
                String result = reader.readLine();
                if (result == null) {
                    break;
                }
                builtInContent.add(result);
            }
            //Start reading built-in config file comment
            PathMarker pathMarker = new PathMarker();
            Map<String, Set<String>> key2CommentsMap = new LinkedHashMap<>();
            Set<String> pendingComment = new LinkedHashSet<>();
            for (String s : builtInContent) {
                //Trimmed for easy identify
                String trimmedStr = s.trim();
                //Just keep the only one empty line for per comment
                if (trimmedStr.isEmpty()) {
                    pendingComment.add("");
                    continue;
                }
                //Detecting comment
                if (trimmedStr.startsWith("#")) {
                    //Add comment
                    pendingComment.add(s);
                } else
                    //Detecting key and parsing to path
                    if (pathMarker.parseRawInput(s)) {
                        String pathNow = pathMarker.getPath();
                        //Merge or add comment in map
                        key2CommentsMap.merge(pathNow, new LinkedHashSet<>(pendingComment), (oldList, newList) -> {
                            oldList.addAll(newList);
                            return oldList;
                        });
                        //Also clean it
                        pendingComment.clear();
                    }
            }
            //Add footer comment
            if (!pendingComment.isEmpty()) {
                key2CommentsMap.put(FOOTER, pendingComment);
                pendingComment.clear();
            }

            //Reading file need to be updated
            List<String> externalContent = Files.readAllLines(externalConfig.toPath());
            List<String> output = new ArrayList<>(externalContent.size());
            //Offset for adding comment
            int offset = 0;
            for (int i = 0; i < externalContent.size(); i++) {
                String s = externalContent.get(i);
                //Parsing comment for file need to be updated
                if (s.isEmpty() || s.trim().startsWith("#")) {
                    pendingComment.add(s);
                    offset--;
                    continue;
                }
                //Add content for keys or configuration value
                output.add(s);
                //Got a valid key
                if (pathMarker.parseRawInput(s)) {
                    String pathNow = pathMarker.getPath();
                    //Get the comments in build-in path with the same node (empty if not existed)
                    Set<String> comments = key2CommentsMap.getOrDefault(pathNow, EMPTY_SET);
                    //Merge that
                    pendingComment.addAll(comments);
                    for (String comment : pendingComment) {
                        //Add comment just in the key was inserted
                        //Base on offset:
                        // Index    Value
                        //   0    foo: value
                        // ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓
                        //====================
                        // Index    Value
                        //   0    # comment
                        //   1    foo: value
                        output.add(i + offset, comment);
                        offset++;
                    }
                    //clear after added
                    pendingComment.clear();
                }
            }

            //Add footer comment
            Set<String> footerComments = key2CommentsMap.getOrDefault(FOOTER, EMPTY_SET);
            pendingComment.addAll(footerComments);
            output.addAll(footerComments);
            pendingComment.clear();

            //Write updated file
            hasWrite = true;
            Files.write(externalConfig.toPath(), output, StandardCharsets.UTF_8);
            //Delete backup
            backupFile.toFile().delete();
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

        /**
         * Add node for path
         *
         * @param nodeStr nodeStr to add
         */
        public void addNode(String nodeStr) {
            path.add(nodeStr);
        }


        private int countPrefixSpace(String s) {
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

        /**
         * Parse the raw yaml line input
         *
         * @param str the raw yaml line input
         * @return if a path parsed
         */
        public boolean parseRawInput(String str) {
            String trimmedStr = str.trim();
            if (trimmedStr.startsWith("#") || !(trimmedStr.contains(": ") || trimmedStr.endsWith(":"))) {
                return false;
            }
            int pathDepthNow = countPrefixSpace(str);
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

        /**
         * Replace current node
         *
         * @param nodeStr node to replace
         */
        public void replaceNode(String nodeStr) {
            if (!path.isEmpty()) {
                path.set(path.size() - 1, nodeStr);
            } else {
                // "" node
                path.add(nodeStr);
            }
        }

        /**
         * Reset current path
         */
        public void reset() {
            path.clear();
        }

        /**
         * Remove current node
         */
        public void removeNode() {
            if (!path.isEmpty()) {
                path.remove(path.size() - 1);
            }
        }


        /**
         * Get current path (Like foo.bar)
         *
         * @return Current path
         */
        public String getPath() {
            StringJoiner stringJoiner = new StringJoiner(".");
            for (String s : path) {
                stringJoiner.add(s);
            }
            return stringJoiner.toString();
        }
    }
}

