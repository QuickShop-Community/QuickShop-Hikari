/*
 * This file is a part of project QuickShop, the name is LuckoPastebinPaster.java
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

package org.maxgamer.quickshop.util.paste;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.nonquickshopstuff.com.sk89q.worldedit.util.net.HttpRequest;
import org.maxgamer.quickshop.util.JsonUtil;

import java.io.IOException;
import java.net.URL;

/**
 * Paste the paste through https://bytebin.lucko.me/post
 * Website Author: Lucko (https://github.com/lucko)
 *
 * @author Ghost_chu
 */
public class LuckoPastebinPaster implements PasteInterface {
    @Override
    @NotNull
    public String pasteTheText(@NotNull String text) throws IOException {
        HttpRequest request = HttpRequest.post(new URL("https://bytebin.lucko.me/post"))
                .body(text)
                .header("User-Agent", "QuickShop-" + QuickShop.getFork() + "-" + QuickShop.getVersion())
                .execute();
        request.expectResponseCode(200, 201, 301, 302);
        String json = request.returnContent().asString("UTF-8");
        Response response = JsonUtil.getGson().fromJson(json, Response.class);
        return response.getKey();
    }

    @NoArgsConstructor
    @Data
    static class Response {
        private String key;
    }
}

