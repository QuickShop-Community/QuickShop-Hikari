/*
 * This file is a part of project QuickShop, the name is MojangAPITest.java
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

package org.maxgamer.quickshop.util.mojangapi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class MojangAPITest_BMCLAPI {
    @Test
    public void testMojangMetaApi() {
        MojangAPI api = new MojangAPI(new MojangApiBmclApiMirror());
        Optional<String> metaData = api.getMetaAPI("1.16.5").get();
        Assertions.assertTrue(metaData.isPresent());
        Assertions.assertFalse(metaData.get().isEmpty());
    }

    @Test
    public void testMojangAssetsApi() {
        MojangAPI api = new MojangAPI(new MojangApiBmclApiMirror());
        MojangAPI.AssetsAPI assetsAPI = api.getAssetsAPI("1.16.5");
        Assertions.assertTrue(assetsAPI.isAvailable());
        Optional<MojangAPI.AssetsFileData> assetsFileData = assetsAPI.getGameAssetsFile();
        Assertions.assertTrue(assetsFileData.isPresent());
        Assertions.assertFalse(assetsFileData.get().getContent().isEmpty());
        Assertions.assertFalse(assetsFileData.get().getId().isEmpty());
        Assertions.assertFalse(assetsFileData.get().getSha1().isEmpty());
    }
}