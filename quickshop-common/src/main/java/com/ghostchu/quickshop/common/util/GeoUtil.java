package com.ghostchu.quickshop.common.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class GeoUtil {
    private static volatile Boolean inChinaRegion = null;

    public static boolean inChinaRegion() {
        // Already know
        if (inChinaRegion != null) return inChinaRegion;
        var client = HttpClient.newHttpClient();
        inChinaRegion = true;
        var request = HttpRequest.newBuilder()
                .uri(URI.create("https://cloudflare.com/cdn-cgi/trace"))
                .timeout(Duration.ofSeconds(7))
                .build();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String[] exploded = response.body().split("\n");
            for (String s : exploded) {
                if (s.startsWith("loc=")) {
                    String[] kv = s.split("=");
                    if (kv.length != 2) {
                        continue;
                    }
                    String key = kv[0];
                    String value = kv[1];
                    if (key.equalsIgnoreCase("loc") && value.equalsIgnoreCase("CN")) {
                        inChinaRegion = true;
                        break;
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return inChinaRegion;
    }

}
