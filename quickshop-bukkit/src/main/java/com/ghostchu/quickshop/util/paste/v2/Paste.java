/*
 *  This file is a part of project QuickShop, the name is Paste.java
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

package com.ghostchu.quickshop.util.paste.v2;

import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.paste.LuckoPastebinPaster;
import com.ghostchu.quickshop.util.paste.PasteInterface;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Paste {

    @Nullable
    public static String paste(@NotNull String content) {
        PasteInterface paster;
        try {
            // Lucko Pastebin
            paster = new LuckoPastebinPaster();
            return paster.pasteTheTextJson(content);
        } catch (Exception ex) {
            Log.debug(ex.getMessage());
        }
//        try {
//            paster = new HelpChatPastebinPaster();
//            return paster.pasteTheTextJson(content);
//        } catch (Exception ex) {
//            Log.debug(ex.getMessage());
//        }
//        try {
//            // Ubuntu Pastebin
//            paster = new UbuntuPaster();
//            return paster.pasteTheTextDirect(content);
//        } catch (Exception ex) {
//            Util.debugLog(ex.getMessage());
//        }
        return null;
    }
}
