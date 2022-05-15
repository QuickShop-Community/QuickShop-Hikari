///*
// *  This file is a part of project QuickShop, the name is HelpChatPastebinPaster.java
// *  Copyright (C) Ghost_chu and contributors
// *
// *  This program is free software: you can redistribute it and/or modify it
// *  under the terms of the GNU General Public License as published by the
// *  Free Software Foundation, either version 3 of the License, or
// *  (at your option) any later version.
// *
// *  This program is distributed in the hope that it will be useful, but WITHOUT
// *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
// *  for more details.
// *
// *  You should have received a copy of the GNU General Public License
// *  along with this program. If not, see <http://www.gnu.org/licenses/>.
// *
// */
//
//package com.ghostchu.quickshop.util.paste;
//
//import com.ghostchu.quickshop.util.JsonUtil;
//import kong.unirest.HttpResponse;
//import kong.unirest.Unirest;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.jetbrains.annotations.NotNull;
//
//import java.io.IOException;
//
///**
// * Paste the paste through <a href="https://paste.helpch.at/">https://paste.helpch.at/</a>
// *
// * @author Ghost_chu
// */
//public class HelpChatPastebinPaster implements PasteInterface {
//    @Override
//    @NotNull
//    public String pasteTheText(@NotNull String text) throws IOException {
//        HttpResponse<String> response = Unirest.post("https://paste.helpch.at/documents")
//                .body(text)
//                .asString();
//        if (response.isSuccess()) {
//            String json = response.getBody();
//            Response resp = JsonUtil.getGson().fromJson(json, Response.class);
//            return "https://paste.helpch.at/" + resp.getKey();
//        } else {
//            throw new IOException(response.getStatus() + " " + response.getStatusText() + ": " + response.getBody());
//        }
//    }
//
//    @Override
//    public String pasteTheTextJson(@NotNull String text) throws Exception {
//        HttpResponse<String> response = Unirest.post("https://paste.helpch.at/documents")
//                .body(JsonUtil.getGson().toJson(new JsonPadding(text)))
//                .asString();
//        if (response.isSuccess()) {
//            String json = response.getBody();
//            Response resp = JsonUtil.getGson().fromJson(json, Response.class);
//            return "https://paste.helpch.at/documents/" + resp.getKey();
//        } else {
//            throw new IOException(response.getStatus() + " " + response.getStatusText() + ": " + response.getBody());
//        }
//    }
//
//    @NoArgsConstructor
//    @Data
//    static class Response {
//        private String key;
//    }
//
//    @AllArgsConstructor
//    @Data
//    static class JsonPadding {
//        private final String _paster = "QuickShop";
//        private String data;
//    }
//}
//
