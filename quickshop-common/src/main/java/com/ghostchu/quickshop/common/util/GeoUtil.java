package com.ghostchu.quickshop.common.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class GeoUtil {
    private static volatile Boolean inChinaRegion = null;


    public static CompletableFuture<Integer> connectTest(String ipAddress, int port, int timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try (Socket socket = new Socket()) {
                long time = System.currentTimeMillis();
                socket.connect(new InetSocketAddress(InetAddress.getByName(ipAddress), port), timeout);
                return (int) (System.currentTimeMillis() - time);
            } catch (IOException ignored) {
                return Integer.MAX_VALUE;
            }
        });
    }

    public static boolean preferChinaServers(){
        CompletableFuture<Integer> cfTest = connectTest("cloudflare.com", 443, 5*1000);
        CompletableFuture<Integer> weiboTest = connectTest("weibo.com", 443, 5*1000);
        int cf = cfTest.join();
        int weibo = weiboTest.join();
        return weibo < cf;
    }

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
                    if (key.equalsIgnoreCase("loc") && !value.equalsIgnoreCase("CN")) {
                        inChinaRegion = false;
                        break;
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Cannot determine the server region: " + e.getClass().getName() + ": " + e.getMessage() + ", falling back to use CN mirror (Did your server behind the GFW, or no internet connection?)");
        }
        return inChinaRegion;
    }

}
