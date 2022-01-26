/*
 * This file is a part of project QuickShop, the name is ShopTransactionMessageContainer.java
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

package org.maxgamer.quickshop.shop;

import com.google.gson.*;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.localization.LocalizedMessagePair;
import org.maxgamer.quickshop.util.JsonUtil;
import org.maxgamer.quickshop.util.MsgUtil;

import java.lang.reflect.Type;

public class ShopTransactionMessageContainer {
    private static final Gson gson = new GsonBuilder().registerTypeAdapter(ShopTransactionMessageContainer.class, new Deserializer()).create();
    @Getter
    //Version for deserializing
    private final int version;
    private final ShopTransactionMessage shopTransactionMessage;

    private ShopTransactionMessageContainer(@NotNull ShopTransactionMessage holder) {
        this.shopTransactionMessage = holder;
        version = holder.getVersion();
    }

    public static ShopTransactionMessageContainer fromJson(String json) {
        try {
            if (MsgUtil.isJson(json)) {
                return gson.fromJson(json, ShopTransactionMessageContainer.class);
            }
        } catch (Exception ignored) {
        }
        //Plain V1 message
        return new ShopTransactionMessageContainer(new ShopTransactionMessage.V1(json));
    }

    public static ShopTransactionMessageContainer ofPlainStr(String message) {
        return new ShopTransactionMessageContainer(new ShopTransactionMessage.V1(message));
    }

    public static ShopTransactionMessageContainer ofStrWithItem(String message, String hoverItem, String hoverText) {
        return new ShopTransactionMessageContainer(new ShopTransactionMessage.V2(message, hoverItem, hoverText));
    }

    public static ShopTransactionMessageContainer ofLocalizedMessageWithItem(@NotNull LocalizedMessagePair message, @Nullable String hoverItem, @Nullable LocalizedMessagePair hoverText) {
        return new ShopTransactionMessageContainer(new ShopTransactionMessage.V3(message, hoverItem, hoverText));
    }

    private static class Deserializer implements JsonDeserializer<ShopTransactionMessageContainer> {

        @Override
        public ShopTransactionMessageContainer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            //Modern format
            if (jsonObject.has("shopTransactionMessage")) {
                JsonObject message = jsonObject.getAsJsonObject("shopTransactionMessage");
                if (jsonObject.has("version")) {
                    int version = jsonObject.get("version").getAsInt();
                    switch (version) {
                        case 1:
                            return new ShopTransactionMessageContainer(context.deserialize(message, ShopTransactionMessage.V1.class));
                        case 2:
                            return new ShopTransactionMessageContainer(context.deserialize(message, ShopTransactionMessage.V2.class));
                        case 3:
                            return new ShopTransactionMessageContainer(context.deserialize(message, ShopTransactionMessage.V3.class));
                        default:
                            throw new JsonParseException("Unknown ShopTransactionMessage version +" + version + "+!");
                    }
                } else {
                    //Some users may use snapshot, this time is V3 message
                    return new ShopTransactionMessageContainer(context.deserialize(message, ShopTransactionMessage.V3.class));
                }
            } else {
                //Plain V2 message
                return new ShopTransactionMessageContainer(context.deserialize(json, ShopTransactionMessage.V2.class));
            }
        }
    }

    public @NotNull String getMessage(@Nullable String locale) {
        switch (shopTransactionMessage.getVersion()) {
            case 1:
                return ((ShopTransactionMessage.V1) shopTransactionMessage).getMessage();
            case 2:
                return ((ShopTransactionMessage.V2) shopTransactionMessage).getMessage();
            case 3:
                return ((ShopTransactionMessage.V3) shopTransactionMessage).getMessage().getLocalizedMessage(locale);
            default:
                return "";
        }
    }

    public @Nullable String getHoverItemStr() {
        switch (shopTransactionMessage.getVersion()) {
            case 2:
                return ((ShopTransactionMessage.V2) shopTransactionMessage).getHoverItem();
            case 3:
                return ((ShopTransactionMessage.V3) shopTransactionMessage).getHoverItem();
            default:
                //V1 does not have hover item
                return null;
        }
    }

    public @Nullable String getHoverText(@Nullable String locale) {
        switch (shopTransactionMessage.getVersion()) {
            case 2:
                return ((ShopTransactionMessage.V2) shopTransactionMessage).getHoverText();
            case 3:
                LocalizedMessagePair localizedMessagePair = ((ShopTransactionMessage.V3) shopTransactionMessage).getHoverText();
                if (localizedMessagePair != null) {
                    return localizedMessagePair.getLocalizedMessage(locale);
                } else {
                    return null;
                }
            default:
                //V1 does not have hover item
                return null;
        }
    }

    @NotNull
    public String toJson() {
        return JsonUtil.getGson().toJson(this);
    }
}
