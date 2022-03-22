/*
 *  This file is a part of project QuickShop, the name is OTACacheControl.java
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

import com.ghostchu.quickshop.util.Util;
import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.locks.ReentrantLock;

public class OTACacheControl {
    private final File metadataFile = new File(Util.getCacheFolder(), "i18n.metadata");
    private final YamlConfiguration metadata;
    private final ReentrantLock LOCK = new ReentrantLock();

    public OTACacheControl() {
        this.metadata = YamlConfiguration.loadConfiguration(this.metadataFile);
    }

    private void save() {
        LOCK.lock();
        try {
            this.metadata.save(this.metadataFile);
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            LOCK.unlock();
        }
    }

    private String hash(String str) {
        return DigestUtils.sha1Hex(str);
    }

    public long readManifestTimestamp() {
        long l;
        LOCK.lock();
        try {
            l = this.metadata.getLong("manifest.timestamp", -1);
        } finally {
            LOCK.unlock();
        }
        return l;
    }

    public void writeManifestTimestamp(long timestamp) {
        LOCK.lock();
        try {
            this.metadata.set("manifest.timestamp", timestamp);
        } finally {
            LOCK.unlock();
        }

        save();
    }

    public long readCachedObjectTimestamp(String path) {
        String cacheKey = hash(path);
        long l;
        LOCK.lock();
        try {
            l = this.metadata.getLong("objects." + cacheKey + ".time", -1);
        } finally {
            LOCK.unlock();
        }
        return l;
    }

    public boolean isCachedObjectOutdated(String path, long manifestTimestamp) {
        return readCachedObjectTimestamp(path) != manifestTimestamp;
    }

    public byte[] readObjectCache(String path) throws IOException {
        String cacheKey = hash(path);
        return Files.readAllBytes(new File(Util.getCacheFolder(), cacheKey).toPath());
    }

    public void writeObjectCache(String path, byte[] data, long manifestTimestamp) throws IOException {
        String cacheKey = hash(path);
        Files.write(new File(Util.getCacheFolder(), cacheKey).toPath(), data);
        LOCK.lock();
        try {
            this.metadata.set("objects." + cacheKey + ".time", manifestTimestamp);
        } finally {
            LOCK.unlock();
            save();
        }

    }

}
