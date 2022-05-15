///*
// *  This file is a part of project QuickShop, the name is PastebinPaster.java
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
//import kong.unirest.HttpResponse;
//import kong.unirest.Unirest;
//import org.jetbrains.annotations.NotNull;
//
//import java.io.IOException;
//
//public class PastebinPaster implements PasteInterface {
//    private final static String DEVELOPER_KEY = "kYoezdaN6Gg9c2VnY78NcpylWRwdzQdk";
//
//    @Override
//    public String pasteTheText(@NotNull String text) throws Exception {
//        HttpResponse<String> response = Unirest.post("https://pastebin.com/api/api_post.php")
//                .field("api_option","paste")
//                .field("api_dev_key",DEVELOPER_KEY)
//                .field("api_paste_name","quickshop.paste")
//                .field("api_paste_expire_date", "1Y")
//                .field("api_paste_code", text)
//                .asString();
//        if(response.isSuccess()){
//            return response.getBody();
//        }else{
//            throw new IOException(response.getStatus() + " " + response.getStatusText() + ": " + response.getBody());
//        }
//    }
//
//    @Override
//    public String pasteTheTextDirect(@NotNull String text) throws Exception {
//        return null;
//    }
//}
