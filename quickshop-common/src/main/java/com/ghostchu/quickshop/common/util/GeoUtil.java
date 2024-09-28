package com.ghostchu.quickshop.common.util;

import com.ghostchu.quickshop.common.util.mirror.MavenCentralMirror;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class GeoUtil {

  private static volatile Boolean inChinaRegion = null;


  public static CompletableFuture<Integer> connectTest(final String ipAddress, final int port, final int timeout) {

    return CompletableFuture.supplyAsync(()->{
      try(Socket socket = new Socket()) {
        final long time = System.currentTimeMillis();
        socket.connect(new InetSocketAddress(InetAddress.getByName(ipAddress), port), timeout);
        return (int)(System.currentTimeMillis() - time);
      } catch(IOException ignored) {
        return Integer.MAX_VALUE;
      }
    });
  }

  private static long sendGetTest(final String urlStr) {

    HttpURLConnection connection = null;
    try {
      final URL url = new URL(urlStr);
      connection = (HttpURLConnection)url.openConnection();
      connection.setConnectTimeout((int)TimeUnit.SECONDS.toMillis(5));
      connection.setReadTimeout((int)TimeUnit.SECONDS.toMillis(5));
      connection.setInstanceFollowRedirects(true);
      connection.setRequestMethod("GET");

      final long time = System.currentTimeMillis();
      final int responseCode = connection.getResponseCode();
      if(responseCode != 200) {
        return Long.MAX_VALUE;
      }

      return System.currentTimeMillis() - time;
    } catch(Exception e) {
      return Long.MAX_VALUE;
    } finally {
      if(connection != null) {
        connection.disconnect();
      }
    }
  }

  @NotNull
  public static List<MavenCentralMirror> determineBestMirrorServer(final Logger logger) {

    final List<CompletableFuture<Void>> testEntry = new ArrayList<>();
    final Map<MavenCentralMirror, Long> mirrorPingMap = new ConcurrentSkipListMap<>();
    for(final MavenCentralMirror value : MavenCentralMirror.values()) {
      testEntry.add(CompletableFuture.supplyAsync(()->{
        mirrorPingMap.put(value, sendGetTest(value.getTestUrl()));
        return null;
      }));
    }
    testEntry.forEach(CompletableFuture::join);
    final List<Map.Entry<MavenCentralMirror, Long>> list = new ArrayList<>(mirrorPingMap.entrySet());
    list.sort(Map.Entry.comparingByValue());
    logger.info("Maven repository mirror test result:");
    list.forEach(e->{
      String cost = "DNF";
      if(e.getValue() != Long.MAX_VALUE) {
        cost = e.getValue() + "ms";
      }
      logger.info("[" + e.getKey().getRegion() + "] " + e.getKey().name() + ": " + cost);
    });
    if(list.isEmpty()) {
      return Collections.emptyList();
    }
    return list.stream().filter(e->e.getValue() != Long.MAX_VALUE).limit(3).map(Map.Entry::getKey).toList();
  }

  public static boolean inChinaRegion() {
    // Already know
    if(inChinaRegion != null) return inChinaRegion;
    final var client = HttpClient.newHttpClient();
    inChinaRegion = true;
    final var request = HttpRequest.newBuilder()
            .uri(URI.create("https://cloudflare.com/cdn-cgi/trace"))
            .timeout(Duration.ofSeconds(7))
            .build();
    try {
      final var response = client.send(request, HttpResponse.BodyHandlers.ofString());
      final String[] exploded = response.body().split("\n");
      for(final String s : exploded) {
        if(s.startsWith("loc=")) {
          final String[] kv = s.split("=");
          if(kv.length != 2) {
            continue;
          }
          final String key = kv[0];
          final String value = kv[1];
          if("loc".equalsIgnoreCase(key) && !"CN".equalsIgnoreCase(value)) {
            inChinaRegion = false;
            break;
          }
        }
      }
    } catch(IOException | InterruptedException e) {
      System.out.println("Cannot determine the server region: " + e.getClass().getName() + ": " + e.getMessage() + ", falling back to use CN mirror (Did your server behind the GFW, or no internet connection?)");
    }
    return inChinaRegion;
  }

}
