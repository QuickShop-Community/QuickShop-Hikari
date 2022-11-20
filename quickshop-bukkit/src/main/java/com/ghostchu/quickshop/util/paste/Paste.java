package com.ghostchu.quickshop.util.paste;

import com.ghostchu.quickshop.util.logger.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Paste {
    private Paste() {
    }

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
