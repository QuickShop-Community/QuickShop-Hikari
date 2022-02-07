/*
 * This file is a part of project QuickShop, the name is PastebinPaster.java
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

import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.nonquickshopstuff.com.sk89q.worldedit.util.net.HttpRequest;
import org.maxgamer.quickshop.util.Util;

import java.net.URL;

public class PastebinPaster implements PasteInterface {
    private final static String DEVELOPER_KEY = "kYoezdaN6Gg9c2VnY78NcpylWRwdzQdk";

    @Override
    public String pasteTheText(@NotNull String text) throws Exception {
        HttpRequest request = HttpRequest.post(new URL("https://pastebin.com/api/api_post.php"))
                .bodyUrlEncodedForm(HttpRequest.Form.create()
                        .add("api_option", "paste")
                        .add("api_dev_key", DEVELOPER_KEY)
                        //.add("api_paste_private", "1")
                        .add("api_paste_name", "quickshop.paste")
                        .add("api_paste_expire_date", "1Y")
                        //.add("api_user_key", "")
                        .add("api_paste_code", text)
                )
                .execute();
        String str = request.returnContent().asString("UTF-8");
        try {
            request.expectResponseCode(200);
        } catch (Exception ex) {
            Util.debugLog(str);
            throw ex;
        }
        return str;

    }
}
