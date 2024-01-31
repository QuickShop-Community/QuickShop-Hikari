package com.ghostchu.quickshop.util;

import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.util.logger.Log;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class DonationInfo {
    private static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA52ZCRiVHe4SPnv7lo+gWwep9aO1qaF1Xmu59cVPrkJ1tPmqFXbdMQ01HX5MGjmcdw11I83Mj7JKR6amEkoGe0I9QfG8Gks89LTwFM1UydmN3v39VUCpSQwMzB7kCshqeynZhuiT0OCn6wa/vXs9lNOhlWsNXOQg9jKrdVatQ/n/V0+Vm4ZPO+D8EZ+NmIPE+wagKQfoJkdaWHHeVL1Z3TEYQJyidTLZj1lc4vPoVWPzbQeyMaNTTowdk3xNrIzANufn/R3i/NnkJ8rJlRt47wv1HsqIABPbclJ8GVr8BbGjs+Vt1jKc9XI/CRz9z2quJr/JvrVIQvUS3WaSt1RLvrQIDAQAB";
    private final String userKey;
    private DonationData data;

    public DonationInfo(String userKey) {
        this.userKey = userKey;
        parse();
    }


    public boolean isValid() {
        return data != null;
    }

    public String getName() {
        if (!isValid()) throw new IllegalStateException("Donation data not initialized");
        return data.getName();
    }

    public String getUrl() {
        if (!isValid()) throw new IllegalStateException("Donation data not initialized");
        return data.getUrl();
    }

    /*
    A really simple way to check if user donated, however QuickShop-Hikari is a free software
    and I just want to add something in paste, so I don't think it is required to use RSA or something like that.
     */
    private void parse() {
        try {
            String original = new String(Base64.getDecoder().decode(this.userKey), StandardCharsets.UTF_8);
            DonationData info = JsonUtil.getGson().fromJson(original, DonationData.class);
            if (StringUtils.isEmpty(info.getName())) {
                return;
            }
            if (StringUtils.isEmpty(info.getTimestamp())) {
                return;
            }
            if (StringUtils.isEmpty(info.getMessageId())) {
                return;
            }
            if (StringUtils.isEmpty(info.getUrl())) {
                return;
            }
            this.data = info;
            Log.debug("Success! Parsed donation key with values: " + info);
        } catch (Throwable err) {
            Log.debug("Couldn't load the donation info from donation key: " + err.getMessage());
        }
    }


    @NoArgsConstructor
    @Data
    public static class DonationData {
        @SerializedName("name")
        private String name;
        @SerializedName("message_id")
        private String messageId;
        @SerializedName("url")
        private String url;
        @SerializedName("timestamp")
        private String timestamp;
    }
}
